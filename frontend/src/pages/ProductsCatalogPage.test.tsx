import '../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import type { Product } from '../api/productsApi'
import { CartProvider } from '../components/cart/CartContext'
import { WishlistProvider } from '../components/wishlist/WishlistContext'
import { ProductsPage } from './ProductsPage'

const search = vi.fn()
const list = vi.fn()
const listRatings = vi.fn()
const wishlistList = vi.fn()
const wishlistAdd = vi.fn()
const wishlistRemove = vi.fn()
let signedIn = true

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ isAuthenticated: signedIn, isAdmin: false, user: { username: 'ada@example.com' } }),
}))
vi.mock('../auth/AuthModalContext', () => ({ useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }) }))
vi.mock('../api/productsApi', () => ({ productsApi: { search: (...a: unknown[]) => search(...a), list: () => list() } }))
vi.mock('../api/catalogApi', () => ({ catalogApi: { listPlatforms: () => Promise.resolve([]) } }))
vi.mock('../api/reviewsApi', () => ({ reviewsApi: { listRatings: () => listRatings() } }))
vi.mock('../api/searchHistoryApi', () => ({ searchHistoryApi: { list: () => Promise.resolve([]), record: vi.fn(), clear: vi.fn() } }))
vi.mock('../api/cartApi', () => ({ cartApi: { get: () => Promise.resolve({ itemCount: 0 }), add: vi.fn() } }))
vi.mock('../api/wishlistApi', () => ({
  wishlistApi: { list: () => wishlistList(), add: (id: number) => wishlistAdd(id), remove: (id: number) => wishlistRemove(id) },
}))

const plan = (id: number, price: number, billingPeriod: 'MONTHLY' | 'ONE_TIME' = 'MONTHLY') =>
  ({ id, name: `Plan ${id}`, price, billingPeriod, currency: 'USD' as const })
const product = (id: number, name: string, over: Partial<Product> = {}): Product => ({
  id, name, description: `${name} description`, category: 'ERP', price: 0, status: 'ACTIVE', ssoConnected: false, featured: false,
  platforms: [], plans: [plan(id * 10, 50)], version: 1, dependsOnProductIds: [], featureTags: [], createdAt: '2025-01-01T00:00:00Z', ...over,
}) as Product

const alpha = product(1, 'Alpha', { platforms: [{ id: 7, name: 'Valam' }], featureTags: ['HRM'], plans: [plan(11, 90)] })
const beta = product(2, 'Beta', { category: 'CRM', platforms: [{ id: 8, name: 'Thittam' }], plans: [plan(21, 0)], createdAt: '2026-10-10T00:00:00Z' })
const gamma = product(3, 'Gamma', { featureTags: ['HRM', 'Payroll'], plans: [plan(31, 20)] })

function Where() {
  const where = useLocation()
  return <output data-testid="where">{where.pathname}{where.search}</output>
}
const renderPage = () => renderWithProviders(
  <CartProvider><WishlistProvider>
    <Routes><Route path="*" element={<><ProductsPage /><Where /></>} /></Routes>
  </WishlistProvider></CartProvider>, { route: '/products' })

const names = () => screen.getAllByRole('heading', { level: 3 }).map((h) => h.textContent)

// TC-CAT-055 to TC-CAT-058 (C94, REQ-CAT-003.11 to .14)
describe('Product catalog page (C94)', () => {
  beforeEach(() => {
    signedIn = true
    for (const m of [search, list, listRatings, wishlistList, wishlistAdd, wishlistRemove]) m.mockReset()
    search.mockResolvedValue({ items: [alpha, beta, gamma], facets: { categories: [{ category: 'ERP', count: 2 }, { category: 'CRM', count: 1 }], platforms: [] } })
    list.mockResolvedValue([alpha, beta, gamma])
    listRatings.mockResolvedValue([{ productId: 3, averageRating: 4.9, reviewCount: 10 }])
    wishlistList.mockResolvedValue([])
    try { localStorage.clear() } catch { /* ignore */ }
  })

  it('shows the hero, the category tiles with counts and every product card with its rating', async () => {
    renderPage()
    expect(await screen.findByRole('heading', { level: 1, name: 'Discover the right software for your business' })).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: /^All 3 products/ })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^ERP 2 products/ })).toBeInTheDocument()
    expect(await screen.findByText('Showing 3 products')).toBeInTheDocument()
    expect(names()).toEqual(['Alpha', 'Beta', 'Gamma'])
    expect(screen.getByLabelText('Rated 4.9 out of 5 from 10 reviews')).toBeInTheDocument()
  })

  it('narrows by category through the server, and by pricing model and feature on the loaded products', async () => {
    const user = userEvent.setup()
    renderPage()
    await screen.findByText('Showing 3 products')
    await user.click(screen.getByRole('button', { name: /^CRM 1 product/ }))
    await waitFor(() => expect(search).toHaveBeenLastCalledWith({ q: undefined, category: 'CRM' }))
    await user.click(screen.getByRole('button', { name: /^All/ }))
    await screen.findByText('Showing 3 products')
    await user.click(screen.getByRole('checkbox', { name: /Free/ }))
    expect(await screen.findByText('Showing 1 product')).toBeInTheDocument()
    expect(names()).toEqual(['Beta'])
    await user.click(screen.getByRole('button', { name: 'Clear all' }))
    await user.click(screen.getByRole('checkbox', { name: /Payroll/ }))
    expect(names()).toEqual(['Gamma'])
  })

  it('sorts by newest and by rating, and remembers the list view', async () => {
    const user = userEvent.setup()
    renderPage()
    await screen.findByText('Showing 3 products')
    await user.click(screen.getByRole('combobox', { name: 'Sort by' }))
    await user.click(screen.getByRole('option', { name: 'Newest' }))
    expect(names()[0]).toBe('Beta')
    await user.click(screen.getByRole('combobox', { name: 'Sort by' }))
    await user.click(screen.getByRole('option', { name: 'Highest rated' }))
    expect(names()[0]).toBe('Gamma')
    await user.click(screen.getByRole('button', { name: 'List view' }))
    expect(localStorage.getItem('eis.catalog.view')).toBe('list')
    expect(screen.getByRole('button', { name: 'List view' })).toHaveAttribute('aria-pressed', 'true')
  })

  it('saves a product with the heart, shows the count on the Wishlist button and filters to saved products', async () => {
    const user = userEvent.setup()
    wishlistAdd.mockResolvedValue([2])
    renderPage()
    await screen.findByText('Showing 3 products')
    expect(screen.getByRole('link', { name: 'Wishlist' })).toHaveAttribute('href', '/my/wishlist')
    await user.click(screen.getByRole('button', { name: 'Save Beta to wishlist' }))
    await waitFor(() => expect(wishlistAdd).toHaveBeenCalledWith(2))
    expect(await screen.findByRole('button', { name: 'Remove Beta from wishlist' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('link', { name: 'Wishlist (1)' })).toBeInTheDocument()
    await user.click(screen.getByRole('checkbox', { name: /Saved products only/ }))
    expect(names()).toEqual(['Beta'])
  })

  it('sends a signed-out visitor to sign in when the heart is clicked, and offers no wishlist', async () => {
    signedIn = false
    const user = userEvent.setup()
    renderPage()
    await screen.findByText('Showing 3 products')
    expect(screen.queryByRole('link', { name: /^Wishlist/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('checkbox', { name: /Saved products only/ })).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Save Alpha to wishlist' }))
    expect(wishlistAdd).not.toHaveBeenCalled()
    expect(screen.getByTestId('where')).toHaveTextContent(`/login?returnTo=${encodeURIComponent('/products')}`)
  })

  it('shows an empty state with Clear filters, and has no a11y violations', async () => {
    const user = userEvent.setup()
    const { container } = renderPage()
    await screen.findByText('Showing 3 products')
    expect(await axe(container)).toHaveNoViolations()
    await user.click(screen.getByRole('checkbox', { name: /Free/ }))
    await user.click(screen.getByRole('checkbox', { name: /HRM/ }))
    expect(await screen.findByText('No products found')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Clear filters' }))
    expect(await screen.findByText('Showing 3 products')).toBeInTheDocument()
  })
})
