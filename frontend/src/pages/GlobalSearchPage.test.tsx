import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { GlobalSearchPage } from './GlobalSearchPage'

const search = vi.fn()
vi.mock('../api/globalSearchApi', async () => {
  const actual = await vi.importActual<typeof import('../api/globalSearchApi')>('../api/globalSearchApi')
  return { ...actual, globalSearchApi: { ...actual.globalSearchApi, search: (q: string) => search(q) } }
})

const emptyResult = { products: [], knowledgeArticles: [], tickets: [] }

describe('GlobalSearchPage', () => {
  beforeEach(() => {
    search.mockReset()
  })

  it('shows matching products and knowledge articles grouped by type', async () => {
    search.mockResolvedValue({
      products: [{ type: 'PRODUCT', id: 1, title: 'Valam.ai', snippet: 'Analytics' }],
      knowledgeArticles: [{ type: 'KNOWLEDGE', id: 2, title: 'Getting started', snippet: 'How to sign in.' }],
      tickets: [],
    })
    renderWithProviders(<GlobalSearchPage />)

    const user = userEvent.setup()
    await user.type(screen.getByPlaceholderText('Search products, knowledge base and your tickets…'), 'valam')
    expect(await screen.findByText('Valam.ai')).toBeInTheDocument()
    expect(await screen.findByText('Getting started')).toBeInTheDocument()
  })

  it('shows an empty state when nothing matches', async () => {
    search.mockResolvedValue(emptyResult)
    renderWithProviders(<GlobalSearchPage />)
    expect(await screen.findByText('Nothing matches your search yet.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    search.mockResolvedValue({
      products: [{ type: 'PRODUCT', id: 1, title: 'Valam.ai' }],
      knowledgeArticles: [],
      tickets: [],
    })
    const { container } = renderWithProviders(<GlobalSearchPage />)
    await screen.findByText('Valam.ai')
    expect(await axe(container)).toHaveNoViolations()
  })
})
