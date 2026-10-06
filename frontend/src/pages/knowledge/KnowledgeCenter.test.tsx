import { ApiError } from '../../api/client'
import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { home, item, summary } from '../../test/knowledgeFixtures'
import { KnowledgeHomePage } from './KnowledgeHomePage'
import { KnowledgeItemPage } from './KnowledgeItemPage'
import { KnowledgeSearchPage } from './KnowledgeSearchPage'
import { KnowledgeFaqsPage, KnowledgeGlossaryPage } from './KnowledgeSectionPage'
import { LegacyKnowledgeBaseRedirect } from './LegacyKnowledgeBaseRedirect'

const api = {
  home: vi.fn(), releaseNotes: vi.fn(), personal: vi.fn(), get: vi.fn(), glossary: vi.fn(), search: vi.fn(), faqs: vi.fn(),
  products: vi.fn(), feedback: vi.fn(), bookmark: vi.fn(), unbookmark: vi.fn(), downloadUrl: vi.fn(), playUrl: vi.fn(), event: vi.fn(), progress: vi.fn(),
}
vi.mock('../../api/knowledgeApi', async (orig) => ({
  ...(await orig<typeof import('../../api/knowledgeApi')>()),
  knowledgeApi: new Proxy({}, { get: (_t, k: string) => (...args: unknown[]) => (api as Record<string, (...a: unknown[]) => unknown>)[k](...args) }),
}))
const auth = { isAuthenticated: false, isAdmin: false }
vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => auth }))

describe('Knowledge Center', () => {
  beforeEach(() => {
    Object.values(api).forEach((f) => f.mockReset())
    auth.isAuthenticated = false
    api.home.mockResolvedValue(home())
    api.releaseNotes.mockResolvedValue([])
    api.personal.mockResolvedValue({ continueLearning: [], recentlyViewed: [], bookmarks: [] })
    api.glossary.mockResolvedValue([{ id: 50, slug: 'bom', term: 'BOM', definition: 'Bill of materials', synonyms: [] }])
    api.products.mockResolvedValue([])
    api.feedback.mockResolvedValue(undefined)
  })

  it('shows the hero, quick access, recommended, products and videos', async () => {
    const { container } = renderWithProviders(<KnowledgeHomePage />)
    expect(await screen.findByRole('heading', { name: 'Learn. Configure. Solve.' })).toBeInTheDocument()
    expect(await screen.findByText('Create a purchase order')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Quick access' })).toBeInTheDocument()
    expect(screen.getAllByText('Varthan.ai').length).toBeGreaterThan(0)
    expect(screen.getByRole('link', { name: 'Play Getting started tour' })).toBeInTheDocument()
    expect(screen.getAllByRole('link', { name: 'purchase order' })[0]).toHaveAttribute('href', '/knowledge/search?q=purchase%20order')
    expect(await axe(container)).toHaveNoViolations()
  })

  it('renders blocks as text, links glossary terms and asks signed-out readers to sign in for feedback', async () => {
    api.get.mockResolvedValue(item())
    const { container } = renderWithProviders(
      <Routes><Route path="/knowledge/content/:idOrSlug" element={<KnowledgeItemPage />} /></Routes>, { route: '/knowledge/content/reset' })
    expect(await screen.findByRole('heading', { name: 'Reset your password' })).toBeInTheDocument()
    expect(screen.getByText(/<script>alert\(1\)<\/script>/)).toBeInTheDocument()
    expect(container.querySelector('script')).toBeNull()
    expect(await screen.findByRole('link', { name: 'BOM' })).toHaveAttribute('href', '/knowledge/glossary#bom')
    expect(screen.getByRole('link', { name: 'Open security' })).toHaveAttribute('href', '/account/security')
    expect(screen.getByText('Sign in to give feedback.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Copy' })).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('sends a No vote only with a reason', async () => {
    auth.isAuthenticated = true
    api.get.mockResolvedValue(item())
    renderWithProviders(<Routes><Route path="/knowledge/content/:idOrSlug" element={<KnowledgeItemPage />} /></Routes>, { route: '/knowledge/content/reset' })
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'No' }))
    const send = screen.getByRole('button', { name: 'Send' })
    expect(send).toBeDisabled()
    await user.click(screen.getByRole('radio', { name: 'Outdated' }))
    await user.click(send)
    expect(api.feedback).toHaveBeenCalledWith(1, { kind: 'VOTE', helpful: false, reason: 'OUTDATED', comment: undefined })
    expect(await screen.findByText('Thank you for your feedback.')).toBeInTheDocument()
  })

  it('shows a missing or hidden item as not available', async () => {
    api.get.mockRejectedValue(new ApiError(404, 'Content not found'))
    renderWithProviders(<Routes><Route path="/knowledge/content/:idOrSlug" element={<KnowledgeItemPage />} /></Routes>, { route: '/knowledge/content/99' })
    expect(await screen.findByText('This content is not available.')).toBeInTheDocument()
  })

  it('retries a failed load and shows the item instead of "not available"', async () => {
    api.get.mockRejectedValueOnce(new ApiError(503, 'Service unavailable')).mockResolvedValue(item())
    renderWithProviders(<Routes><Route path="/knowledge/content/:idOrSlug" element={<KnowledgeItemPage />} /></Routes>, { route: '/knowledge/content/reset' })
    expect(await screen.findByRole('heading', { level: 1 }, { timeout: 3000 })).toBeInTheDocument()
    expect(screen.queryByText('This content is not available.')).not.toBeInTheDocument()
    expect(api.get).toHaveBeenCalledTimes(2)
  })

  it('groups search results by type and offers a ticket with the query', async () => {
    api.search.mockResolvedValue({
      query: 'purchase', results: [summary()], byType: { PRODUCT_GUIDE: [summary()] }, products: [], modules: [
        { id: 21, name: 'Purchase', slug: 'purchase', productName: 'Varthan.ai', productSlug: 'varthan' }], didYouMean: null, semanticStatus: 'USED', tookMs: 12,
    })
    const { container } = renderWithProviders(<KnowledgeSearchPage />, { route: '/knowledge/search?q=purchase' })
    expect(await screen.findByRole('heading', { name: 'Product guides (1)' })).toBeInTheDocument()
    expect(screen.getByText(/Includes results that match the meaning/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Create support ticket' }).getAttribute('href')).toContain('subject=purchase')
    expect(await axe(container)).toHaveNoViolations()
  })

  it('lists FAQs as accordions and the glossary A–Z', async () => {
    api.faqs.mockResolvedValue([item({ id: 7, contentType: 'FAQ', title: 'How do I reset MFA?', typeFields: { question: 'How do I reset MFA?', answer: 'Ask your admin.' }, blocks: [] })])
    const { container, unmount } = renderWithProviders(<KnowledgeFaqsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'How do I reset MFA?' }))
    expect(await screen.findByText('Ask your admin.')).toBeVisible()
    expect(await axe(container)).toHaveNoViolations()
    unmount()
    renderWithProviders(<KnowledgeGlossaryPage />)
    expect(await screen.findByText('Bill of materials')).toBeInTheDocument()
  })

  it('sends old Knowledge Base links to the Knowledge Center', async () => {
    renderWithProviders(
      <Routes>
        <Route path="/knowledge-base" element={<LegacyKnowledgeBaseRedirect />} />
        <Route path="/knowledge" element={<p>Center home</p>} />
        <Route path="/knowledge/content/:id" element={<p>Article page</p>} />
      </Routes>, { route: '/knowledge-base?article=12' })
    expect(await screen.findByText('Article page')).toBeInTheDocument()
  })
})
