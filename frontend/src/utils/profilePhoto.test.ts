import { afterEach, describe, expect, it, vi } from 'vitest'
import { PHOTO_MAX_BYTES, PHOTO_MAX_SOURCE_BYTES, PhotoError, checkPhotoFile, prepareProfilePhoto, squareCrop } from './profilePhoto'

describe('checkPhotoFile', () => {
  it('accepts JPEG, PNG and WebP', () => {
    for (const type of ['image/jpeg', 'image/png', 'image/webp']) expect(checkPhotoFile({ type, size: 1000 })).toBeNull()
  })
  it('refuses SVG, GIF, PDF and an unknown type', () => {
    for (const type of ['image/svg+xml', 'image/gif', 'application/pdf', '']) expect(checkPhotoFile({ type, size: 1000 })).toBe('type')
  })
  it('refuses a source file above the processing limit', () => {
    expect(checkPhotoFile({ type: 'image/jpeg', size: PHOTO_MAX_SOURCE_BYTES + 1 })).toBe('sourceTooLarge')
    expect(checkPhotoFile({ type: 'image/jpeg', size: PHOTO_MAX_SOURCE_BYTES })).toBeNull()
  })
})

describe('squareCrop', () => {
  it('cuts a centred square from a landscape image and scales to 512', () => {
    expect(squareCrop(4000, 3000)).toEqual({ sx: 500, sy: 0, side: 3000, out: 512 })
  })
  it('cuts a centred square from a portrait image', () => {
    expect(squareCrop(1000, 2000)).toEqual({ sx: 0, sy: 500, side: 1000, out: 512 })
  })
  it('never upscales a small image', () => {
    expect(squareCrop(200, 300)).toEqual({ sx: 0, sy: 50, side: 200, out: 200 })
  })
})

describe('prepareProfilePhoto', () => {
  const drawImage = vi.fn()
  function stubBrowser(sizes: Record<string, number[]>, width = 3000, height = 2000) {
    vi.stubGlobal('createImageBitmap', vi.fn().mockResolvedValue({ width, height, close: vi.fn() }))
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue({
      fillRect: vi.fn(), drawImage, set fillStyle(_v: string) {}, set imageSmoothingQuality(_v: string) {},
    } as unknown as CanvasRenderingContext2D)
    const calls: string[] = []
    vi.spyOn(HTMLCanvasElement.prototype, 'toBlob').mockImplementation(function (cb, type, quality) {
      calls.push(`${type}@${quality}`)
      const list = sizes[type ?? '']
      const size = list?.shift()
      // A browser without WebP support returns PNG instead.
      cb(size === undefined ? new Blob(['x'], { type: 'image/png' }) : new Blob([new Uint8Array(size)], { type }))
    })
    return calls
  }
  afterEach(() => { vi.unstubAllGlobals(); vi.restoreAllMocks(); drawImage.mockReset() })

  const file = (type = 'image/jpeg') => new File([new Uint8Array(8)], 'me.jpg', { type })

  it('crops to a centred square, scales to 512 and returns a WebP of at most 1 MB', async () => {
    stubBrowser({ 'image/webp': [200_000] })
    const { blob, filename } = await prepareProfilePhoto(file())
    expect(blob.type).toBe('image/webp')
    expect(filename).toBe('photo.webp')
    expect(drawImage).toHaveBeenCalledWith(expect.anything(), 500, 0, 2000, 2000, 0, 0, 512, 512)
  })
  it('lowers the quality until the result fits in 1 MB', async () => {
    const calls = stubBrowser({ 'image/webp': [PHOTO_MAX_BYTES + 1, 900_000] })
    const { blob } = await prepareProfilePhoto(file())
    expect(blob.size).toBe(900_000)
    expect(calls).toEqual(['image/webp@0.86', 'image/webp@0.72'])
  })
  it('falls back to JPEG when the browser cannot write WebP', async () => {
    stubBrowser({ 'image/jpeg': [150_000] })
    const { blob, filename } = await prepareProfilePhoto(file('image/png'))
    expect(blob.type).toBe('image/jpeg')
    expect(filename).toBe('photo.jpg')
  })
  it('gives up with tooLarge when no quality fits', async () => {
    stubBrowser({ 'image/webp': [2e6, 2e6, 2e6] })
    await expect(prepareProfilePhoto(file())).rejects.toMatchObject({ code: 'tooLarge' })
  })
  it('refuses the wrong type before reading the file', async () => {
    const create = vi.fn()
    vi.stubGlobal('createImageBitmap', create)
    await expect(prepareProfilePhoto(file('image/svg+xml'))).rejects.toBeInstanceOf(PhotoError)
    await expect(prepareProfilePhoto(file('image/svg+xml'))).rejects.toMatchObject({ code: 'type' })
    expect(create).not.toHaveBeenCalled()
  })
})
