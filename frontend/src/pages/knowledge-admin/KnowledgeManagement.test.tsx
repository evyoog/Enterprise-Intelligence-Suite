import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { UploadProgressCard } from '../../components/knowledge/UploadProgressCard'
import { content } from '../../test/knowledgeFixtures'
import { renderWithProviders } from '../../test/renderWithProviders'
import { KnowledgeAdminLayout } from './KnowledgeAdminLayout'
import { KnowledgeContentEditorPage } from './KnowledgeContentEditorPage'
import { KnowledgeMediaLibraryPage } from './KnowledgeMediaLibraryPage'
import { KnowledgeVideoEditorPage } from './KnowledgeVideoEditorPage'

const admin = {
  me: vi.fn(), taxonomy: vi.fn(), get: vi.fn(), create: vi.fn(), update: vi.fn(), action: vi.fn(), versions: vi.fn(), preview: vi.fn(),
  publish: vi.fn(), returnToDraft: vi.fn(), remove: vi.fn(), compare: vi.fn(), restoreVersion: vi.fn(),
}
const media = { storage: vi.fn(), list: vi.fn(), remove: vi.fn(), get: vi.fn(), previewUrl: vi.fn(), uploadUrl: vi.fn(), complete: vi.fn(), abort: vi.fn() }
const video = { fetchYouTube: vi.fn(), create: vi.fn(), update: vi.fn(), uploadUrl: vi.fn(), complete: vi.fn(), previewPlay: vi.fn() }
type Fns = Record<string, (...a: unknown[]) => unknown>
vi.mock('../../api/knowledgeApi', async (orig) => ({
  ...(await orig<typeof import('../../api/knowledgeApi')>()),
  knowledgeAdminApi: new Proxy({}, { get: (_t, k: string) => (...args: unknown[]) => (admin as Fns)[k](...args) }),
  knowledgeMediaApi: new Proxy({}, { get: (_t, k: string) => (...args: unknown[]) => (media as Fns)[k](...args) }),
  knowledgeVideoApi: new Proxy({}, { get: (_t, k: string) => (...args: unknown[]) => (video as Fns)[k](...args) }),
}))
vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => ({ isAuthenticated: true, isAdmin: false }) }))

const taxonomy = {
  products: [{ id: 1, name: 'Valam.ai', slug: 'valam', description: null, catalogProductId: null, displayOrder: 0, active: true, contentCount: 0,
    modules: [{ id: 11, productId: 1, name: 'Inventory', slug: 'inventory', displayOrder: 0, active: true, contentCount: 0 }] }],
  categories: [{ id: 5, name: 'Product tutorial', slug: 'product-tutorial', scope: 'VIDEO', displayOrder: 0, active: true }],
}

function renderAdmin(path: string) {
  return renderWithProviders(
    <Routes>
      <Route path="/knowledge-management" element={<KnowledgeAdminLayout />}>
        <Route path="content/:id" element={<KnowledgeContentEditorPage />} />
        <Route path="videos/:id" element={<KnowledgeVideoEditorPage />} />
        <Route path="media" element={<KnowledgeMediaLibraryPage />} />
      </Route>
    </Routes>, { route: path })
}

describe('Knowledge Management', () => {
  beforeEach(() => {
    ;[admin, media, video].forEach((group) => Object.values(group).forEach((f) => f.mockReset()))
    admin.me.mockResolvedValue({ contributor: true, publisher: false, contentTypes: ['ARTICLE', 'FAQ', 'VIDEO'] })
    admin.taxonomy.mockResolvedValue(taxonomy)
    admin.versions.mockResolvedValue([])
    media.storage.mockResolvedValue({ configured: false, provider: 'NONE', videoMaxSize: 5368709120, documentMaxSize: 1, imageMaxSize: 1, audioMaxSize: 1,
      multipartThreshold: 1, videoTypes: ['mp4'], documentTypes: [], imageTypes: [], audioTypes: [] })
  })

  it('tells people without a knowledge permission that they have no access', async () => {
    admin.me.mockRejectedValue(new ApiError(403, 'You do not have permission to manage knowledge content.'))
    renderAdmin('/knowledge-management/content/new')
    expect(await screen.findByText('You do not have permission to manage knowledge content.')).toBeInTheDocument()
  })

  it('lets a contributor draft and submit, but shows no publisher actions', async () => {
    admin.get.mockResolvedValue(content())
    admin.update.mockResolvedValue(content({ title: 'Draft guide v2' }))
    admin.action.mockResolvedValue(content({ workflowState: 'IN_REVIEW' }))
    const { container } = renderAdmin('/knowledge-management/content/9')
    const user = userEvent.setup()
    expect(await screen.findByRole('heading', { name: 'Edit Article' })).toBeInTheDocument()
    expect(screen.queryByRole('tab', { name: 'Search index' })).toBeNull()
    expect(screen.queryByRole('link', { name: 'Products & categories' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Publish' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Delete' })).toBeNull()
    await user.click(screen.getByRole('button', { name: 'Save draft' }))
    expect(admin.update).toHaveBeenCalledWith(9, expect.objectContaining({ title: 'Draft guide', contentType: 'ARTICLE' }))
    await user.click(screen.getByRole('button', { name: 'Submit for review' }))
    expect(admin.action).toHaveBeenCalledWith(9, 'submit')
    expect(await screen.findByText('This content is in review — only a publisher can change it now.')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('adds and reorders blocks without a mouse', async () => {
    admin.get.mockResolvedValue(content({ blocks: [{ type: 'paragraph', text: 'First' }, { type: 'paragraph', text: 'Second' }] }))
    admin.update.mockImplementation((_id: number, input: { blocks: unknown[] }) => Promise.resolve(content({ blocks: input.blocks as never })))
    renderAdmin('/knowledge-management/content/9')
    const user = userEvent.setup()
    const moveDown = await screen.findAllByRole('button', { name: 'Move down' })
    await user.click(moveDown[0])
    await user.click(screen.getByRole('button', { name: 'Add block' }))
    await user.click(screen.getByRole('button', { name: 'Save draft' }))
    const saved = admin.update.mock.calls[0][1].blocks
    expect(saved.map((b: { text?: string }) => b.text)).toEqual(['Second', 'First', undefined])
  })

  it('publishers see review and publish actions with minor or major versions', async () => {
    admin.me.mockResolvedValue({ contributor: true, publisher: true, contentTypes: ['ARTICLE'] })
    admin.get.mockResolvedValue(content({ workflowState: 'APPROVED' }))
    admin.publish.mockResolvedValue(content({ workflowState: 'PUBLISHED', live: true, liveVersion: '1.0' }))
    renderAdmin('/knowledge-management/content/9')
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Publish' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('radio', { name: 'Major version (1.x → 2.0)' }))
    await user.click(within(dialog).getByRole('button', { name: 'Publish' }))
    expect(admin.publish).toHaveBeenCalledWith(9, 'MAJOR', null)
    expect(await screen.findByText('Live version 1.0')).toBeInTheDocument()
  })

  it('fetches YouTube details with the oEmbed fallback and says storage is off for uploads', async () => {
    video.fetchYouTube.mockResolvedValue({ videoId: 'dQw4w9WgXcQ', title: 'Inventory basics', description: null, durationSeconds: null, channel: 'eVyoog',
      thumbnailUrl: 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', source: 'OEMBED' })
    const { container } = renderAdmin('/knowledge-management/videos/new')
    const user = userEvent.setup()
    await user.type(await screen.findByLabelText('YouTube link'), 'https://youtu.be/dQw4w9WgXcQ')
    await user.click(screen.getByRole('button', { name: 'Fetch video details' }))
    expect(await screen.findByText(/No YouTube API key/)).toBeInTheDocument()
    expect(screen.getByLabelText(/^Title/)).toHaveValue('Inventory basics')
    await user.click(screen.getByRole('button', { name: 'Save video' }))
    expect(await screen.findByText('Title, product, module and category are required.')).toBeInTheDocument()
    expect(video.create).not.toHaveBeenCalled()
    await user.click(screen.getByRole('button', { name: 'Upload (AWS S3)' }))
    expect(screen.getByText('File storage is not configured yet. YouTube and external videos still work.')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('asks for confirmation before deleting a file that is in use', async () => {
    admin.me.mockResolvedValue({ contributor: true, publisher: true, contentTypes: ['ARTICLE'] })
    media.storage.mockResolvedValue({ configured: true, provider: 'AWS_S3', videoMaxSize: 1, documentMaxSize: 1, imageMaxSize: 1, audioMaxSize: 1,
      multipartThreshold: 1, videoTypes: [], documentTypes: [], imageTypes: [], audioTypes: [] })
    media.list.mockResolvedValue({ items: [{ id: 3, kind: 'DOCUMENT', fileName: 'guide.pdf', mimeType: 'application/pdf', size: 2048, status: 'READY', version: 1,
      productId: null, moduleId: null, createdAt: '2026-10-01T00:00:00Z', uploadedAt: '2026-10-01T00:00:00Z', usedBy: [{ contentId: 9, title: 'Draft guide', contentType: 'DOCUMENT' }], storage: null }],
      totalElements: 1, page: 0, size: 50 })
    media.remove.mockImplementation((_id: number, confirm: boolean) => confirm ? Promise.resolve(undefined)
      : Promise.reject(new ApiError(409, 'In use', { code: 'IN_USE', usedBy: [{ contentId: 9, title: 'Draft guide', contentType: 'DOCUMENT' }] })))
    renderAdmin('/knowledge-management/media')
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Delete' }))
    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByRole('link', { name: 'Draft guide' })).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Delete anyway' }))
    await waitFor(() => expect(media.remove).toHaveBeenLastCalledWith(3, true))
  })

  it('shows upload progress with speed, remaining size and cancel', async () => {
    const onCancel = vi.fn()
    const { container } = renderWithProviders(
      <UploadProgressCard fileName="setup.mp4" progress={{ state: 'uploading', loaded: 50 * 1024 * 1024, total: 100 * 1024 * 1024, speed: 5 * 1024 * 1024 }} onCancel={onCancel} />)
    expect(screen.getByText(/50%/)).toBeInTheDocument()
    expect(screen.getByText(/5\.0 MB\/s/)).toBeInTheDocument()
    expect(screen.getByText(/50\.0 MB left/)).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Cancel upload' }))
    expect(onCancel).toHaveBeenCalled()
    expect(await axe(container)).toHaveNoViolations()
  })
})
