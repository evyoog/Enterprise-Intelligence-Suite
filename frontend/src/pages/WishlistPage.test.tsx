import '../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import type { Product } from '../api/productsApi'
import { CartProvider } from '../components/cart/CartContext'
import { WishlistProvider } from '../components/wishlist/WishlistContext'
import { WishlistPage } from './WishlistPage'

const list = vi.fn()
const wishlistList = vi.fn()
const wishlistRemove = vi.fn()

vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@example.com' } }) }))
vi.mock('../api/productsApi', () => ({ productsApi: { list: () => list() } }))
vi.mock('../api/reviewsApi', () => ({ reviewsApi: { listRatings: () => Promise.resolve([{ productId: 1, averageRating: 4.5, reviewCount: 2 }]) } }))
vi.mock('../api/cartApi', () => ({ cartApi: { get: () => Promise.resolve({ itemCount: 0 }), add: vi.fn() } }))
vi.mock('../api/wishlistApi', () => ({ wishlistApi: { list: () => wishlistList(), add: vi.fn(), remove: (id: number) => wishlistRemove(id) } }))

const product = (id: number, name: string): Product => ({
  id, name, description: '', category: 'ERP', price: 0, status: 'ACTIVE', ssoConnected: false, featured: false, platforms: [],
  plans: [{ id: id * 10, name: 'Basic', price: 25, billingPeriod: 'MONTHLY', currency: 'USD' }], version: 1, dependsOnProductIds: [], featureTags: [],
}) as Product

const renderPage = () => renderWithProviders(
  <CartProvider><WishlistProvider><Routes><Route path="*" element={<WishlistPage />} /></Routes></WishlistProvider></CartProvider>, { route: '/my/wishlist' })

// TC-MKT-037 to TC-MKT-039 (C94, REQ-MKT-004)
describe('WishlistPage', () => {
  beforeEach(() => {
    for (const m of [list, wishlistList, wishlistRemove]) m.mockReset()
    list.mockResolvedValue([product(1, 'Alpha'), product(2, 'Beta'), product(3, 'Gamma')])
  })

  it('shows the saved products, newest saved first, and leaves out a saved product that is no longer in the catalog', async () => {
    wishlistList.mockResolvedValue([3, 99, 1])
    renderPage()
    await screen.findByRole('heading', { level: 3, name: 'Gamma' })
    expect(screen.getAllByRole('heading', { level: 3 }).map((h) => h.textContent)).toEqual(['Gamma', 'Alpha'])
    expect(screen.getByLabelText('Rated 4.5 out of 5 from 2 reviews')).toBeInTheDocument()
  })

  it('removes a product with the heart and shows the empty state with Browse products', async () => {
    const user = userEvent.setup()
    wishlistList.mockResolvedValue([1])
    wishlistRemove.mockResolvedValue([])
    renderPage()
    await user.click(await screen.findByRole('button', { name: 'Remove Alpha from wishlist' }))
    await waitFor(() => expect(wishlistRemove).toHaveBeenCalledWith(1))
    expect(await screen.findByText('Your wishlist is empty')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Browse products' })).toHaveAttribute('href', '/products')
  })

  it('has no detectable a11y violations', async () => {
    wishlistList.mockResolvedValue([1, 2])
    const { container } = renderPage()
    await screen.findByRole('heading', { level: 3, name: 'Alpha' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
