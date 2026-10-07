import '../../i18n'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { AdminContent, AdminItem, PublicContent, PublicItem } from '../../api/productContentApi'
import { ProductContentManager } from './ProductContentManager'
import { ProductResources } from './ProductResources'

const api = vi.hoisted(() => ({
  list: vi.fn(), uploadUrl: vi.fn(), create: vi.fn(), update: vi.fn(), remove: vi.fn(), publish: vi.fn(), unpublish: vi.fn(),
  reorder: vi.fn(), documentationOptions: vi.fn(), download: vi.fn(), upload: vi.fn(),
}))
vi.mock('../../api/productContentApi', () => ({
  productContentAdminApi: {
    list: api.list, uploadUrl: api.uploadUrl, create: api.create, update: api.update, remove: api.remove, publish: api.publish,
    unpublish: api.unpublish, reorder: api.reorder, documentationOptions: api.documentationOptions, previewUrl: vi.fn(),
  },
  productContentApi: { get: vi.fn(), download: api.download },
  uploadProductFile: api.upload,
}))

const STORAGE = { configured: true, imageMaxSize: 5 * 1024 * 1024, documentMaxSize: 20 * 1024 * 1024, imageTypes: ['jpg', 'jpeg', 'png', 'webp'], documentTypes: ['pdf'] }

const item = (over: Partial<AdminItem>): AdminItem => ({
  id: 1, kind: 'VIDEO', title: 'Product tour', description: null, status: 'DRAFT', displayOrder: 1, version: 1,
  updatedAt: '2026-10-07T10:00:00Z', publishedAt: null, file: null, fileUrl: null, altText: null, logo: null, logoUrl: null,
  videoProvider: 'YOUTUBE', videoUrl: 'https://youtu.be/dQw4w9WgXcQ', embedUrl: 'https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ',
  thumbnailUrl: 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', customerName: null, problem: null, result: null,
  articleId: null, articleTitle: null, articleAvailable: false, ...over,
})

const sheet = item({ id: 2, kind: 'DATASHEET', title: 'Platform datasheet', status: 'PUBLISHED', version: 3, videoProvider: null, videoUrl: null,
  embedUrl: null, thumbnailUrl: null, file: { fileName: 'datasheet.pdf', size: 2048, mimeType: 'application/pdf' } })
const tour = item({})
const tour2 = item({ id: 3, title: 'Webinar', videoProvider: 'EXTERNAL', embedUrl: null, thumbnailUrl: null, videoUrl: 'https://videos.example.com/w' })

function content(items: AdminItem[], storage = STORAGE): AdminContent {
  return { items, storage }
}

function renderManager() {
  return render(<MemoryRouter><ProductContentManager productId={7} /></MemoryRouter>)
}

describe('Product content — admin Content tab (REQ-CAT-004)', () => {
  beforeEach(() => {
    Object.values(api).forEach((m) => m.mockReset())
    api.list.mockResolvedValue(content([sheet, tour, tour2]))
    api.documentationOptions.mockResolvedValue([{ id: 40, title: 'Getting started', type: 'PRODUCT_GUIDE', forThisProduct: true }])
  })

  it('shows the five sections with their items, status, version and file', async () => {
    const { container } = renderManager()
    expect(await screen.findByRole('heading', { name: 'Datasheet' })).toBeInTheDocument()
    for (const name of ['Documentation', 'Images', 'Videos', 'Case studies']) expect(screen.getByRole('heading', { name })).toBeInTheDocument()
    expect(screen.getByText('Platform datasheet')).toBeInTheDocument()
    expect(screen.getByText(/Version 3/)).toBeInTheDocument()
    expect(screen.getByText(/datasheet\.pdf · 2 KB/)).toBeInTheDocument()
    expect(screen.getByText('Published')).toBeInTheDocument()
    expect(screen.getAllByText('Draft')).toHaveLength(2)
    expect(screen.getByText('No images yet.')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('without storage the file sections cannot add, but videos and documentation still can', async () => {
    api.list.mockResolvedValue(content([], { ...STORAGE, configured: false }))
    renderManager()
    expect(await screen.findByText(/File storage is not configured yet/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add datasheet' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Add image' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Add video' })).toBeEnabled()
    expect(screen.getByRole('button', { name: 'Link documentation' })).toBeEnabled()
  })

  it('adds a video: validates the link, then saves it as a draft', async () => {
    api.create.mockResolvedValue(item({ id: 9 }))
    renderManager()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Add video' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByLabelText(/^Title/), 'Tour')
    await user.type(within(dialog).getByLabelText(/^Video link/), 'http://insecure.example.com/x')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(await within(dialog).findByText('Enter a secure https video link.')).toBeInTheDocument()
    expect(api.create).not.toHaveBeenCalled()

    await user.clear(within(dialog).getByLabelText(/^Video link/))
    await user.type(within(dialog).getByLabelText(/^Video link/), 'https://youtu.be/dQw4w9WgXcQ')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(api.create).toHaveBeenCalledWith(7, expect.objectContaining({ kind: 'VIDEO', title: 'Tour', videoUrl: 'https://youtu.be/dQw4w9WgXcQ' })))
    expect(await screen.findByText('Saved.')).toBeInTheDocument()
  })

  it('uploads a datasheet straight to storage, then attaches it on Save', async () => {
    api.uploadUrl.mockResolvedValue({ uploadKey: 'product-content/7/datasheet/abc.pdf', uploadUrl: 'https://s3.test/put', expiresAt: '2026-10-07T10:15:00Z', maxSize: 20971520 })
    api.upload.mockReturnValue({ promise: Promise.resolve(), cancel: vi.fn() })
    api.create.mockResolvedValue(item({ id: 10, kind: 'DATASHEET' }))
    const { baseElement } = renderManager()
    const user = userEvent.setup({ applyAccept: false })
    await user.click(await screen.findByRole('button', { name: 'Add datasheet' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByLabelText(/^Title/), 'Datasheet 2026')

    // no file yet: Save explains what is missing
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(await within(dialog).findByText('Choose and upload the file first.')).toBeInTheDocument()

    const input = baseElement.querySelector('input[type="file"]') as HTMLInputElement
    await user.upload(input, new File(['%PDF-1.4'], 'sheet.pdf', { type: 'application/pdf' }))
    await within(dialog).findByText('Ready: sheet.pdf')
    expect(api.uploadUrl).toHaveBeenCalledWith(7, expect.objectContaining({ kind: 'DATASHEET', purpose: 'file', fileName: 'sheet.pdf', contentType: 'application/pdf', size: 8 }))
    expect(api.upload).toHaveBeenCalledTimes(1)

    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(api.create).toHaveBeenCalledWith(7, expect.objectContaining({
      kind: 'DATASHEET', file: { uploadKey: 'product-content/7/datasheet/abc.pdf', fileName: 'sheet.pdf', contentType: 'application/pdf', size: 8 },
    })))
  })

  it('refuses a wrong file type or an oversized file before uploading anything', async () => {
    const { baseElement } = renderManager()
    const user = userEvent.setup({ applyAccept: false })
    await user.click(await screen.findByRole('button', { name: 'Add image' }))
    const dialog = await screen.findByRole('dialog')
    const input = baseElement.querySelector('input[type="file"]') as HTMLInputElement
    await user.upload(input, new File(['<svg/>'], 'logo.svg', { type: 'image/svg+xml' }))
    expect(await within(dialog).findByText(/This file type is not allowed here\. Allowed: JPG, JPEG, PNG, WEBP\./)).toBeInTheDocument()
    const big = new File(['x'], 'big.png', { type: 'image/png' })
    Object.defineProperty(big, 'size', { value: 6 * 1024 * 1024 })
    await user.upload(input, big)
    expect(await within(dialog).findByText(/The file is too large\. The maximum is 5 MB\./)).toBeInTheDocument()
    expect(api.uploadUrl).not.toHaveBeenCalled()
  })

  it('an image needs alt text', async () => {
    renderManager()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Add image' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByLabelText(/^Title/), 'Hero')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(await within(dialog).findByText('Enter alt text for the image.')).toBeInTheDocument()
  })

  it('links documentation from the live articles', async () => {
    api.create.mockResolvedValue(item({ id: 11, kind: 'DOCUMENTATION' }))
    renderManager()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Link documentation' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByLabelText(/^Title/), 'Start here')
    await user.click(within(dialog).getByLabelText(/^Knowledge article/))
    await user.click(await screen.findByRole('option', { name: /Getting started \(for this application\)/ }))
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(api.create).toHaveBeenCalledWith(7, expect.objectContaining({ kind: 'DOCUMENTATION', articleId: 40 })))
  })

  it('publishes, unpublishes, reorders and deletes (with confirmation)', async () => {
    api.publish.mockResolvedValue(item({ status: 'PUBLISHED' }))
    api.unpublish.mockResolvedValue(item({ id: 2 }))
    api.reorder.mockResolvedValue(content([sheet, tour2, tour]))
    api.remove.mockResolvedValue(undefined)
    renderManager()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Publish Product tour' }))
    expect(api.publish).toHaveBeenCalledWith(7, 1)
    expect(await screen.findByText('Published.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Unpublish Platform datasheet' }))
    expect(api.unpublish).toHaveBeenCalledWith(7, 2)

    // the first video moves down: the order of every item is sent
    await user.click(screen.getByRole('button', { name: 'Move Product tour down' }))
    expect(api.reorder).toHaveBeenCalledWith(7, [2, 3, 1])
    expect(screen.getByRole('button', { name: 'Move Product tour up' })).toBeDisabled()

    await user.click(screen.getByRole('button', { name: 'Delete Webinar' }))
    expect(api.remove).not.toHaveBeenCalled()
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Delete' }))
    await waitFor(() => expect(api.remove).toHaveBeenCalledWith(7, 3))
  })

  it('shows the backend message when a save is refused', async () => {
    const { ApiError } = await import('../../api/client')
    api.create.mockRejectedValue(new ApiError(400, 'The uploaded file does not match what was declared.'))
    renderManager()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Add video' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByLabelText(/^Title/), 'Tour')
    await user.type(within(dialog).getByLabelText(/^Video link/), 'https://youtu.be/dQw4w9WgXcQ')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(await within(dialog).findByRole('alert')).toHaveTextContent('The uploaded file does not match what was declared.')
  })
})

describe('Product content — Resources tab (REQ-CAT-004.9)', () => {
  const pub = (over: Partial<PublicItem>): PublicItem => ({
    id: 1, kind: 'DATASHEET', title: 'Datasheet', description: null, version: 2, updatedAt: '2026-10-07T10:00:00Z', file: null,
    imageUrl: null, altText: null, logoUrl: null, videoProvider: null, videoUrl: null, embedUrl: null, thumbnailUrl: null,
    customerName: null, problem: null, result: null, articleRef: null, articleType: null, ...over,
  })
  const all: PublicContent = {
    datasheets: [pub({ file: { fileName: 'd.pdf', size: 3 * 1024 * 1024, mimeType: 'application/pdf' } })],
    documentation: [pub({ id: 2, kind: 'DOCUMENTATION', title: 'Getting started guide', articleRef: 'getting-started', articleType: 'PRODUCT_GUIDE' })],
    images: [pub({ id: 3, kind: 'IMAGE', title: 'Dashboard', imageUrl: 'https://s3.test/img.png?sig=1', altText: 'The dashboard with three charts' })],
    videos: [
      pub({ id: 4, kind: 'VIDEO', title: 'Tour', videoProvider: 'YOUTUBE', embedUrl: 'https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ', thumbnailUrl: 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', videoUrl: 'https://youtu.be/dQw4w9WgXcQ' }),
      pub({ id: 5, kind: 'VIDEO', title: 'Webinar', videoProvider: 'EXTERNAL', videoUrl: 'https://videos.example.com/w' }),
    ],
    caseStudies: [pub({ id: 6, kind: 'CASE_STUDY', title: 'How Acme saved time', customerName: 'Acme', problem: 'Slow month-end', result: 'Closed 3 days faster',
      file: { fileName: 'acme.pdf', size: 1000, mimeType: 'application/pdf' } })],
  }

  beforeEach(() => { api.download.mockReset() })

  it('shows every kind of content and passes the accessibility checks', async () => {
    const { container } = render(<MemoryRouter><ProductResources productId={7} content={all} /></MemoryRouter>)
    for (const name of ['Datasheets', 'Documentation', 'Gallery', 'Videos', 'Case studies']) expect(screen.getByRole('heading', { name })).toBeInTheDocument()
    expect(screen.getByText('Version 2 · updated Oct 7, 2026')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Getting started guide' })).toHaveAttribute('href', '/knowledge/content/getting-started')
    expect(screen.getByAltText('The dashboard with three charts')).toHaveAttribute('src', 'https://s3.test/img.png?sig=1')
    expect(screen.getByRole('link', { name: /Open video in a new tab/ })).toHaveAttribute('href', 'https://videos.example.com/w')
    expect(screen.getByRole('link', { name: /Open video in a new tab/ })).toHaveAttribute('rel', 'noopener noreferrer')
    expect(screen.getByText('Slow month-end')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('asks for a fresh download link only when the visitor clicks', async () => {
    api.download.mockResolvedValue({ url: 'https://s3.test/d.pdf?sig=2', expiresAt: '2026-10-07T10:05:00Z', fileName: 'd.pdf', size: 3 })
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    render(<MemoryRouter><ProductResources productId={7} content={all} /></MemoryRouter>)
    expect(api.download).not.toHaveBeenCalled()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Download Datasheet (PDF, 3 MB)' }))
    await waitFor(() => expect(api.download).toHaveBeenCalledWith(7, 1))
    await waitFor(() => expect(click).toHaveBeenCalled())
    click.mockRestore()
  })

  it('says so when a download cannot start', async () => {
    api.download.mockRejectedValue(new Error('x'))
    render(<MemoryRouter><ProductResources productId={7} content={all} /></MemoryRouter>)
    await userEvent.setup().click(screen.getByRole('button', { name: /Download Datasheet/ }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Could not start the download.')
  })

  it('loads the YouTube player only after the visitor presses play', async () => {
    render(<MemoryRouter><ProductResources productId={7} content={all} /></MemoryRouter>)
    expect(document.querySelector('iframe')).toBeNull()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Watch Tour' }))
    const frame = document.querySelector('iframe')!
    expect(frame).toHaveAttribute('src', 'https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ')
    expect(frame).toHaveAttribute('title', 'Video: Tour')
  })
})
