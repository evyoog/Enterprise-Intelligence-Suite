import '../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { GlobalSearchPage } from './GlobalSearchPage'

const search = vi.fn()
vi.mock('../api/globalSearchApi', async () => {
  const actual = await vi.importActual<typeof import('../api/globalSearchApi')>('../api/globalSearchApi')
  return { ...actual, globalSearchApi: { ...actual.globalSearchApi, search: (q: string, o?: unknown) => search(q, o) } }
})
const historyList = vi.fn()
const historyRecord = vi.fn()
const historyClear = vi.fn()
vi.mock('../api/searchHistoryApi', () => ({
  searchHistoryApi: { list: () => historyList(), record: (q: string) => historyRecord(q), clear: () => historyClear() },
}))
let signedIn = true
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ isAuthenticated: signedIn }) }))

const emptyResult = { products: [], knowledgeArticles: [], tickets: [] }

describe('GlobalSearchPage', () => {
  beforeEach(() => {
    search.mockReset()
    historyList.mockReset().mockResolvedValue([])
    historyRecord.mockReset().mockResolvedValue(undefined)
    historyClear.mockReset().mockResolvedValue(undefined)
    signedIn = true
  })

  it('shows matching products and knowledge articles (responses without a ranked list)', async () => {
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

  it('shows ranked results with match labels and highlighted words', async () => {
    search.mockResolvedValue({
      ...emptyResult,
      results: [
        { type: 'KNOWLEDGE', id: 2, title: 'Paying invoices', snippet: 'Pay an invoice by card.', reference: '#2', matchType: 'EXACT_PHRASE',
          titleHighlights: [{ start: 7, length: 8 }], snippetHighlights: [{ start: 7, length: 7 }] },
        { type: 'PRODUCT', id: 5, title: 'Varthan.ai', reference: '#5', matchType: 'SEMANTIC' },
      ],
      semanticStatus: 'USED',
    })
    renderWithProviders(<GlobalSearchPage />, { route: '/search?q=invoices' })
    const results = await screen.findByRole('list', { name: 'Search results' })
    const items = await within(results).findAllByRole('listitem')
    expect(items).toHaveLength(2)
    expect(within(items[0]).getByText('Exact phrase')).toBeInTheDocument()
    expect(within(items[1]).getByText('Similar meaning')).toBeInTheDocument()
    expect(items[0].querySelectorAll('mark')[0].textContent).toBe('invoices')
    expect(screen.getByText(/2 results · includes results with a similar meaning/)).toBeInTheDocument()
  })

  it('a search arriving from the URL is saved to history and tracked', async () => {
    search.mockResolvedValue(emptyResult)
    renderWithProviders(<GlobalSearchPage />, { route: '/search?q=factura' })
    await screen.findByText('No results for "factura"')
    expect(search).toHaveBeenCalledWith('factura', { type: undefined, track: true })
    expect(historyRecord).toHaveBeenCalledWith('factura')
  })

  it('typing searches without tracking; Enter tracks', async () => {
    search.mockResolvedValue(emptyResult)
    renderWithProviders(<GlobalSearchPage />)
    const user = userEvent.setup()
    await user.type(screen.getByRole('textbox', { name: 'Search' }), 'sso')
    await vi.waitFor(() => expect(search).toHaveBeenCalledWith('sso', { type: undefined, track: false }))
    expect(historyRecord).not.toHaveBeenCalled()
    await user.keyboard('{Enter}')
    expect(search).toHaveBeenCalledWith('sso', { type: undefined, track: true })
    expect(historyRecord).toHaveBeenCalledWith('sso')
  })

  it('filters by type', async () => {
    search.mockResolvedValue(emptyResult)
    renderWithProviders(<GlobalSearchPage />, { route: '/search?q=guide' })
    await screen.findByText('No results for "guide"')
    await userEvent.setup().click(screen.getByRole('button', { name: 'Knowledge base' }))
    await vi.waitFor(() => expect(search).toHaveBeenCalledWith('guide', { type: 'KNOWLEDGE', track: false }))
  })

  it('offers "Did you mean" and runs the corrected search', async () => {
    search.mockResolvedValueOnce({ ...emptyResult, results: [], didYouMean: 'invoice' }).mockResolvedValue(emptyResult)
    renderWithProviders(<GlobalSearchPage />, { route: '/search?q=invoise' })
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'invoice' }))
    expect(search).toHaveBeenCalledWith('invoice', { type: undefined, track: true })
  })

  it('helps when nothing matches', async () => {
    search.mockResolvedValue(emptyResult)
    renderWithProviders(<GlobalSearchPage />, { route: '/search?q=zzz' })
    expect(await screen.findByText('No results for "zzz"')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Browse the product catalog' })).toHaveAttribute('href', '/products')
    expect(screen.getByRole('link', { name: 'Contact support' })).toBeInTheDocument()
  })

  it('tells the user when meaning search is unavailable', async () => {
    search.mockResolvedValue({ ...emptyResult, results: [{ type: 'PRODUCT', id: 1, title: 'Valam.ai' }], semanticStatus: 'UNAVAILABLE' })
    renderWithProviders(<GlobalSearchPage />, { route: '/search?q=valam' })
    expect(await screen.findByText(/keyword results only/)).toBeInTheDocument()
  })

  it('shows recent searches and clears them', async () => {
    search.mockResolvedValue(emptyResult)
    historyList.mockResolvedValue([{ query: 'renewals', searchedAt: '2026-10-05T10:00:00Z' }])
    renderWithProviders(<GlobalSearchPage />)
    expect(await screen.findByRole('button', { name: 'renewals' })).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Clear history' }))
    expect(historyClear).toHaveBeenCalled()
    await vi.waitFor(() => expect(screen.queryByRole('button', { name: 'renewals' })).not.toBeInTheDocument())
  })

  it('hides the tickets filter and history when signed out', async () => {
    signedIn = false
    search.mockResolvedValue(emptyResult)
    renderWithProviders(<GlobalSearchPage />)
    await screen.findByText('Nothing matches your search yet.')
    expect(screen.queryByRole('button', { name: 'Your tickets' })).not.toBeInTheDocument()
    expect(historyList).not.toHaveBeenCalled()
  })

  it('has no detectable a11y violations', async () => {
    search.mockResolvedValue({
      ...emptyResult,
      results: [{ type: 'PRODUCT', id: 1, title: 'Valam.ai', matchType: 'KEYWORD', titleHighlights: [{ start: 0, length: 5 }] }],
    })
    const { container } = renderWithProviders(<GlobalSearchPage />, { route: '/search?q=valam' })
    await screen.findByText('Keyword match')
    expect(await axe(container)).toHaveNoViolations()
  })
})
