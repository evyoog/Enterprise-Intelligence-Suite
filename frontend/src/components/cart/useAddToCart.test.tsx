import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import type { Product } from '../../api/productsApi'
import { ProductTile } from '../products/ProductTile'
import { canAddToCart, cheapestPaidPlan, useAddToCart } from './useAddToCart'
import { CartProvider } from './CartContext'
import { CartButton } from './CartButton'

const add = vi.fn()
const get = vi.fn()
let signedIn = true

vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => ({ isAuthenticated: signedIn }) }))
vi.mock('../../api/cartApi', () => ({ cartApi: { get: () => get(), add: (p: number, plan: number) => add(p, plan) } }))

const plan = (id: number, price: number, name = `Plan ${id}`) => ({ id, name, price, billingPeriod: 'MONTHLY' as const, currency: 'USD' as const })
const product = (id: number, name: string, plans: ReturnType<typeof plan>[]): Product => ({
  id, name, description: '', category: 'AI', price: 0, status: 'ACTIVE', plans, platforms: [], featureTags: [],
} as unknown as Product)

const paid = product(1, 'Valam.ai', [plan(10, 99), plan(11, 29, 'Starter'), plan(12, 0, 'Free')])
const second = product(2, 'Varthan.ai', [plan(20, 49)])
const free = product(3, 'Free app', [plan(30, 0)])

function Cards() {
  const { addToCart, busyId, notice } = useAddToCart()
  const where = useLocation()
  return (
    <>
      <CartButton />
      {[paid, second, free].map((p) => (
        <ProductTile key={p.id} product={p} onAddToCart={() => addToCart(p)} addToCartUnavailable={!canAddToCart(p)} addingToCart={busyId === p.id} />
      ))}
      <output data-testid="where">{where.pathname}{where.search}</output>
      {notice}
    </>
  )
}
const renderCards = () => renderWithProviders(
  <CartProvider><Routes><Route path="*" element={<Cards />} /></Routes></CartProvider>, { route: '/products' })

// TC-MKT-027 to TC-MKT-030 (C93, REQ-MKT-003.10)
describe('Add to cart on the product card', () => {
  beforeEach(() => { add.mockReset(); get.mockReset(); get.mockResolvedValue({ itemCount: 0 }); signedIn = true })

  it('picks the cheapest paid plan and only offers the button for a product with a paid plan', () => {
    expect(cheapestPaidPlan(paid)?.id).toBe(11)
    expect(canAddToCart(paid)).toBe(true)
    expect(canAddToCart(free)).toBe(false)
  })

  it('adds in place, stays on the page, updates the badge and lets several products be added in a row', async () => {
    const user = userEvent.setup()
    add.mockResolvedValueOnce({ itemCount: 1 }).mockResolvedValueOnce({ itemCount: 2 })
    renderCards()
    // Every card shows the button; a product with no paid plan has it disabled.
    expect(screen.getByRole('button', { name: 'Add Free app to cart' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Add Valam.ai to cart' })).toBeEnabled()

    await user.click(screen.getByRole('button', { name: 'Add Valam.ai to cart' }))
    await waitFor(() => expect(add).toHaveBeenCalledWith(1, 11))
    expect(await screen.findByText('Added Valam.ai — Starter to your cart')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'View cart' })).toHaveAttribute('href', '/cart')
    expect(await screen.findByRole('button', { name: 'Cart, 1 items' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Add Varthan.ai to cart' }))
    await waitFor(() => expect(add).toHaveBeenCalledWith(2, 20))
    expect(await screen.findByRole('button', { name: 'Cart, 2 items' })).toBeInTheDocument()
    expect(screen.getByTestId('where')).toHaveTextContent('/products')
  })

  it('shows the cart message when the add is refused', async () => {
    const { ApiError } = await import('../../api/client')
    add.mockRejectedValue(new ApiError(409, 'You already have an active subscription to Valam.ai.'))
    renderCards()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Add Valam.ai to cart' }))
    expect(await screen.findByText('You already have an active subscription to Valam.ai.')).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'View cart' })).not.toBeInTheDocument()
  })

  it('sends a signed-out visitor to sign in and back to the cart add', async () => {
    signedIn = false
    renderCards()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Add Valam.ai to cart' }))
    expect(add).not.toHaveBeenCalled()
    expect(screen.getByTestId('where')).toHaveTextContent(`/login?returnTo=${encodeURIComponent('/cart?add=1&plan=11')}`)
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderCards()
    await screen.findByRole('button', { name: 'Add Valam.ai to cart' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
