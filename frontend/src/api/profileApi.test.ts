import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from './client'
import { profileApi, profileFieldErrors } from './profileApi'

describe('profileFieldErrors', () => {
  it('reads the server\'s per-field messages from a 400', () => {
    const e = new ApiError(400, 'Validation failed', { fieldErrors: { 'personal.firstName': 'Required', 'address.postalCode': 'Bad', ignored: 3 } })
    expect(profileFieldErrors(e)).toEqual({ 'personal.firstName': 'Required', 'address.postalCode': 'Bad' })
  })
  it('is null for anything else', () => {
    expect(profileFieldErrors(new ApiError(400, 'Validation failed', {}))).toBeNull()
    expect(profileFieldErrors(new ApiError(500, 'x', { fieldErrors: { a: 'b' } }))).toBeNull()
    expect(profileFieldErrors(new Error('x'))).toBeNull()
    expect(profileFieldErrors(null)).toBeNull()
  })
})

describe('profileApi requests', () => {
  const fetchMock = vi.fn()
  const ok = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
  beforeEach(() => { fetchMock.mockReset(); vi.stubGlobal('fetch', fetchMock) })
  afterEach(() => { vi.unstubAllGlobals() })
  const lastCall = () => ({ url: String(fetchMock.mock.calls.at(-1)![0]), init: fetchMock.mock.calls.at(-1)![1] as RequestInit })

  it('uses the /me endpoints for the caller and never sends an id', async () => {
    fetchMock.mockImplementation(() => Promise.resolve(ok({ exists: true })))
    await profileApi.get()
    expect(lastCall().url).toMatch(/\/me\/profile$/)
    await profileApi.completion()
    expect(lastCall().url).toMatch(/\/me\/profile\/completion$/)
    await profileApi.update({ personal: {} as never, work: {} as never, address: {} as never, online: {} as never })
    expect(lastCall().url).toMatch(/\/me\/profile$/)
    expect(lastCall().init.method).toBe('PUT')
    await profileApi.removePhoto()
    expect(lastCall().url).toMatch(/\/me\/profile\/photo$/)
    expect(lastCall().init.method).toBe('DELETE')
  })

  it('uploads the photo as multipart form data in the field "file", without a manual content type', async () => {
    fetchMock.mockImplementation(() => Promise.resolve(ok({ exists: true })))
    await profileApi.uploadPhoto(new Blob(['x'], { type: 'image/webp' }), 'photo.webp')
    const { url, init } = lastCall()
    expect(url).toMatch(/\/me\/profile\/photo$/)
    expect(init.method).toBe('PUT')
    expect(init.body).toBeInstanceOf(FormData)
    expect((init.body as FormData).get('file')).toBeInstanceOf(File)
    expect(((init.body as FormData).get('file') as File).name).toBe('photo.webp')
    expect((init.headers as Record<string, string>)['Content-Type']).toBeUndefined()
  })

  it('reads the photo bytes, or null on 404', async () => {
    const bytes = new Blob(['img'], { type: 'image/webp' })
    fetchMock.mockResolvedValueOnce({ ok: true, status: 200, blob: async () => bytes })
    expect(await profileApi.photo()).toBe(bytes)
    expect(lastCall().init.cache).toBe('no-cache')
    fetchMock.mockResolvedValueOnce(ok({ status: 404, message: 'No photo' }, 404))
    expect(await profileApi.photo()).toBeNull()
    fetchMock.mockResolvedValueOnce(ok({ message: 'boom' }, 500))
    await expect(profileApi.photo()).rejects.toBeInstanceOf(ApiError)
  })

  it('uses the admin endpoints for another person', async () => {
    fetchMock.mockImplementation(() => Promise.resolve(ok({ exists: true })))
    await profileApi.getForCustomer(42)
    expect(lastCall().url).toMatch(/\/admin\/registrations\/individuals\/42\/profile$/)
    await profileApi.updateForCustomer(42, { personal: {} as never, work: {} as never, address: {} as never, online: {} as never })
    expect(lastCall().url).toMatch(/\/admin\/registrations\/individuals\/42\/profile$/)
    expect(lastCall().init.method).toBe('PUT')
  })
})
