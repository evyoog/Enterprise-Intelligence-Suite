import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminKnowledgeBasePage } from './AdminKnowledgeBasePage'

const listAll = vi.fn()
const create = vi.fn()
const publish = vi.fn()
const unpublish = vi.fn()
const deleteArticle = vi.fn()

vi.mock('../../api/knowledgeBaseApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/knowledgeBaseApi')>('../../api/knowledgeBaseApi')
  return {
    ...actual,
    adminKnowledgeBaseApi: {
      ...actual.adminKnowledgeBaseApi,
      listAll: () => listAll(),
      create: (payload: { title: string; body: string }) => create(payload),
      publish: (id: number) => publish(id),
      unpublish: (id: number) => unpublish(id),
      delete: (id: number) => deleteArticle(id),
    },
  }
})

const draft = { id: 1, title: 'Draft article', body: 'Not yet public.', status: 'DRAFT' as const, version: 1, updatedAt: '2027-01-01T00:00:00Z' }
const published = { id: 2, title: 'Published article', body: 'Public content.', status: 'PUBLISHED' as const, version: 2, updatedAt: '2027-01-02T00:00:00Z' }

describe('AdminKnowledgeBasePage', () => {
  beforeEach(() => {
    for (const m of [listAll, create, publish, unpublish, deleteArticle]) m.mockReset()
  })

  it('creates a new article', async () => {
    listAll.mockResolvedValueOnce([]).mockResolvedValueOnce([draft])
    create.mockResolvedValue(draft)
    renderWithProviders(<AdminKnowledgeBasePage />)

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: 'New article' }))
    await user.type(await screen.findByLabelText('Title'), 'Draft article')
    await user.type(screen.getByLabelText('Body'), 'Not yet public.')
    await user.click(screen.getByRole('button', { name: 'Save' }))
    expect(create).toHaveBeenCalledWith({ title: 'Draft article', body: 'Not yet public.' })
  })

  it('publishes a draft article', async () => {
    listAll.mockResolvedValue([draft])
    publish.mockResolvedValue({ ...draft, status: 'PUBLISHED' })
    renderWithProviders(<AdminKnowledgeBasePage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Publish' }))
    expect(publish).toHaveBeenCalledWith(1)
  })

  it('unpublishes a published article', async () => {
    listAll.mockResolvedValue([published])
    unpublish.mockResolvedValue({ ...published, status: 'DRAFT' })
    renderWithProviders(<AdminKnowledgeBasePage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Unpublish' }))
    expect(unpublish).toHaveBeenCalledWith(2)
  })

  it('deletes an article', async () => {
    listAll.mockResolvedValue([draft]);
    deleteArticle.mockResolvedValue(undefined)
    renderWithProviders(<AdminKnowledgeBasePage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Delete' }))
    expect(deleteArticle).toHaveBeenCalledWith(1)
  })

  it('has no detectable a11y violations', async () => {
    listAll.mockResolvedValue([draft, published])
    const { container } = renderWithProviders(<AdminKnowledgeBasePage />)
    await screen.findByText('Draft article')
    expect(await axe(container)).toHaveNoViolations()
  })
})
