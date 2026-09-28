import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { KnowledgeBasePage } from './KnowledgeBasePage'

const search = vi.fn()
vi.mock('../api/knowledgeBaseApi', async () => {
  const actual = await vi.importActual<typeof import('../api/knowledgeBaseApi')>('../api/knowledgeBaseApi')
  return { ...actual, knowledgeBaseApi: { ...actual.knowledgeBaseApi, search: (q?: string) => search(q) } }
})

const article = { id: 1, title: 'Getting started', body: 'How to sign in to your account.', status: 'PUBLISHED' as const, version: 1, updatedAt: '2027-01-01T00:00:00Z' }

describe('KnowledgeBasePage', () => {
  beforeEach(() => {
    search.mockReset()
  })

  it('lists published articles', async () => {
    search.mockResolvedValue([article])
    renderWithProviders(<KnowledgeBasePage />)
    expect(await screen.findByText('Getting started')).toBeInTheDocument()
  })

  it('opens an article and can navigate back', async () => {
    search.mockResolvedValue([article])
    renderWithProviders(<KnowledgeBasePage />)

    const user = userEvent.setup()
    await user.click(await screen.findByText('Getting started'))
    expect(await screen.findByText('How to sign in to your account.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Back to knowledge base' }))
    expect(await screen.findByText('Getting started')).toBeInTheDocument()
  })

  it('shows an empty state when nothing matches', async () => {
    search.mockResolvedValue([])
    renderWithProviders(<KnowledgeBasePage />)
    expect(await screen.findByText('No articles match your search.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    search.mockResolvedValue([article])
    const { container } = renderWithProviders(<KnowledgeBasePage />)
    await screen.findByText('Getting started')
    expect(await axe(container)).toHaveNoViolations()
  })
})
