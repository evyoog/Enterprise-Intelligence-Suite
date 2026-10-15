import '../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { ApiError } from '../api/client'
import type { CompareItem, CompareTax, Comparison } from '../api/compareApi'
import type { Product, ProductPlan } from '../api/productsApi'
import { CartProvider } from '../components/cart/CartContext'
import { ComparePage } from './ComparePage'

const compare = vi.fn()
const list = vi.fn()
const cartAdd = vi.fn()
let signedIn = true

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ isAuthenticated: signedIn, isAdmin: false, user: { username: 'ada@example.com' } }),
}))
vi.mock('../auth/AuthModalContext', () => ({ useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }) }))
vi.mock('../api/compareApi', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../api/compareApi')>()),
  compareApi: { compare: (...a: unknown[]) => compare(...a) },
}))
vi.mock('../api/productsApi', () => ({ productsApi: { list: () => list() } }))
vi.mock('../api/cartApi', () => ({ cartApi: { get: () => Promise.resolve({ itemCount: 0 }), add: (...a: unknown[]) => cartAdd(...a) } }))

const plan = (id: number, name: string, price: number, over: Partial<ProductPlan> = {}): ProductPlan =>
  ({ id, name, price, billingPeriod: 'MONTHLY', currency: 'USD', ...over })
const product = (id: number, name: string, plans: ProductPlan[], over: Partial<Product> = {}): Product => ({
  id, name, description: `${name} description`, category: 'ERP', price: 0, status: 'ACTIVE', ssoConnected: false, featured: false,
  platforms: [], plans, version: 1, dependsOnProductIds: [], featureTags: [], ...over,
}) as Product

const alphaPlans = [plan(11, 'Starter', 10), plan(12, 'Pro', 40)]
const alpha = product(1, 'Alpha', alphaPlans)
const beta = product(2, 'Beta', [plan(21, 'Basic', 20, { currency: 'EUR' })], { category: 'CRM' })
const gamma = product(3, 'Gamma', [plan(31, 'Free', 0)])
const delta = product(4, 'Delta', [], { price: 15 })
const all = [alpha, beta, gamma, delta]

const estimated: CompareTax = { status: 'ESTIMATED', amount: 3.6, total: 23.6, message: null }
const item = (p: Product, pl: ProductPlan | null, seats: number, tax: CompareTax, over: Partial<CompareItem> = {}): CompareItem => {
  const unit = pl ? pl.price : p.price
  return {
    product: p, plan: pl, currency: pl?.currency ?? 'USD', unitPrice: unit, billingPeriod: pl?.billingPeriod ?? null, seats, subtotal: unit * seats, tax,
    includedFeatures: [], trialDays: null, ...over,
  }
}
const comparison = (seats: number, items: CompareItem[]): Comparison => ({ seats, items })

function Where() {
  const where = useLocation()
  return <output data-testid="where">{where.pathname}{where.search}</output>
}
const renderPage = (route: string) => renderWithProviders(
  <CartProvider><Routes><Route path="*" element={<><ComparePage /><Where /></>} /></Routes></CartProvider>, { route })
const where = () => decodeURIComponent(screen.getByTestId('where').textContent ?? '')

// TC-MKT-: compare page (C102, REQ-MKT-005, TR-2 A)
describe('Compare page (C102)', () => {
  beforeEach(() => {
    signedIn = true
    for (const m of [compare, list, cartAdd]) m.mockReset()
    list.mockResolvedValue(all)
  })

  it('asks for at least 2 items when the URL has fewer, without calling the API', async () => {
    renderPage('/compare?items=1')
    expect(await screen.findByText('Choose at least 2 products to compare')).toBeInTheDocument()
    expect(compare).not.toHaveBeenCalled()
    expect(screen.getByRole('link', { name: 'Browse the catalog' })).toHaveAttribute('href', '/products')
  })

  it('reads the items and seats from the URL, calls the API and shows price x seats and the tax estimate', async () => {
    compare.mockResolvedValue(comparison(3, [
      item(alpha, alphaPlans[0], 3, { status: 'ESTIMATED', amount: 3, total: 33, message: null }),
      item(beta, beta.plans[0], 3, { status: 'ESTIMATED', amount: 6, total: 66, message: null }),
    ]))
    renderPage('/compare?items=1,2:21&seats=3')
    const table = await screen.findByRole('table')
    expect(compare).toHaveBeenCalledWith([{ productId: 1 }, { productId: 2, planId: 21 }], 3)
    expect(within(table).getByRole('columnheader', { name: /Alpha/ })).toBeInTheDocument()
    expect(within(table).getByRole('columnheader', { name: /Beta/ })).toBeInTheDocument()
    const row = (name: string) => within(table).getByRole('row', { name: new RegExp(`^${name}`) })
    expect(within(row('Price per seat')).getByText('$10.00')).toBeInTheDocument()
    expect(within(row('Price per seat')).getByText('€20.00')).toBeInTheDocument()
    expect(within(row('Seats')).getAllByText('3')).toHaveLength(2)
    expect(within(row('Subtotal')).getByText('$30.00')).toBeInTheDocument()
    expect(within(row('Subtotal')).getByText('€60.00')).toBeInTheDocument()
    expect(within(row('Estimated tax')).getByText('$3.00')).toBeInTheDocument()
    expect(within(row('Estimated total')).getByText('$33.00')).toBeInTheDocument()
    expect(within(row('Estimated total')).getByText('€66.00')).toBeInTheDocument()
    expect(within(row('Billing period')).getAllByText('Monthly')).toHaveLength(2)
    expect(screen.getByText('Estimates only. Final tax and totals are shown at checkout.')).toBeInTheDocument()
    expect(screen.getByText('Comparison updated: 2 items for 3 seats.')).toBeInTheDocument()
    expect(screen.getByLabelText('Seats')).toHaveValue(3)
  })

  it('shows each tax status: sign in, billing details, and tax worked out at checkout', async () => {
    compare.mockResolvedValue(comparison(1, [
      item(alpha, alphaPlans[0], 1, { status: 'NOT_SIGNED_IN', amount: null, total: null, message: 'x' }),
      item(beta, beta.plans[0], 1, { status: 'NEEDS_BILLING_DETAILS', amount: null, total: null, message: 'y' }),
      item(gamma, gamma.plans[0], 1, { status: 'AT_CHECKOUT', amount: null, total: null, message: 'z' }),
    ]))
    renderPage('/compare?items=1,2,3')
    await screen.findByRole('table')
    expect(screen.getByRole('link', { name: 'Sign in to see the tax estimate' }))
      .toHaveAttribute('href', `/login?returnTo=${encodeURIComponent('/compare?items=1,2,3')}`)
    expect(screen.getByRole('link', { name: 'Add your billing details to see the tax estimate' })).toHaveAttribute('href', '/billing')
    expect(screen.getByText('Tax is worked out at checkout')).toBeInTheDocument()
    // Without an estimate the total is the subtotal, marked "before tax".
    expect(screen.getAllByText('before tax')).toHaveLength(3)
  })

  it('shows a free trial badge, the feature grid with check marks, and no-plan products at their flat price', async () => {
    compare.mockResolvedValue(comparison(1, [
      item(alpha, alphaPlans[1], 1, estimated, { includedFeatures: ['SSO', 'Reports'], trialDays: 14 }),
      item(delta, null, 1, estimated, { includedFeatures: ['reports'] }),
    ]))
    renderPage('/compare?items=1:12,4')
    const table = await screen.findByRole('table')
    expect(screen.getByText('14-day free trial')).toBeInTheDocument()
    const sso = within(table).getByRole('row', { name: /^SSO/ })
    expect(within(sso).getByText('Included')).toBeInTheDocument()
    expect(within(sso).getByText('Not included')).toBeInTheDocument()
    // Feature names are matched without regard to case.
    expect(within(within(table).getByRole('row', { name: /^Reports/ })).getAllByText('Included')).toHaveLength(2)
    expect(within(within(table).getByRole('row', { name: /^Plan/ })).getByText('No plan (flat price)')).toBeInTheDocument()
    expect(within(within(table).getByRole('row', { name: /^Price per seat/ })).getByText('$15.00')).toBeInTheDocument()
  })

  it('shows the API validation message with Retry, and retries', async () => {
    const user = userEvent.setup()
    compare.mockRejectedValueOnce(new ApiError(400, 'The same item is chosen twice.'))
    renderPage('/compare?items=1,1')
    expect(await screen.findByText(/The same item is chosen twice\./)).toBeInTheDocument()
    expect(screen.getByText('The comparison could not be loaded')).toBeInTheDocument()
    compare.mockResolvedValue(comparison(1, [item(alpha, alphaPlans[0], 1, estimated), item(beta, beta.plans[0], 1, estimated)]))
    await user.click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByRole('table')).toBeInTheDocument()
    expect(compare).toHaveBeenCalledTimes(2)
  })

  it('shows a 404 for an unknown product', async () => {
    compare.mockRejectedValue(new ApiError(404, 'Product not found: 99'))
    renderPage('/compare?items=1,99')
    expect(await screen.findByText(/Product not found: 99/)).toBeInTheDocument()
    // The chosen items stay listed so the bad one can be removed.
    expect(screen.getByRole('button', { name: 'Remove Product 99' })).toBeInTheDocument()
  })

  it('shows a skeleton while loading', async () => {
    compare.mockReturnValue(new Promise(() => {}))
    renderPage('/compare?items=1,2')
    await waitFor(() => expect(compare).toHaveBeenCalled())
    expect(screen.queryByRole('table')).not.toBeInTheDocument()
    expect(screen.getByText('Loading the comparison')).toBeInTheDocument()
  })

  it('validates the seats: out of range shows an error and asks nothing; a valid number goes to the URL', async () => {
    const user = userEvent.setup()
    compare.mockResolvedValue(comparison(1, [item(alpha, alphaPlans[0], 1, estimated), item(beta, beta.plans[0], 1, estimated)]))
    renderPage('/compare?items=1,2')
    await screen.findByRole('table')
    expect(compare).toHaveBeenCalledTimes(1)
    const seats = screen.getByLabelText('Seats')
    await user.clear(seats)
    await user.click(seats)
    await user.paste('100001')
    expect(screen.getByText('Seats must be a whole number between 1 and 100000.')).toBeInTheDocument()
    expect(compare).toHaveBeenCalledTimes(1)
    await user.clear(seats)
    await user.type(seats, '25')
    await waitFor(() => expect(where()).toBe('/compare?items=1,2&seats=25'))
    await waitFor(() => expect(compare).toHaveBeenLastCalledWith([{ productId: 1 }, { productId: 2 }], 25))
  })

  it('treats an invalid seats value in the URL as an error', async () => {
    renderPage('/compare?items=1,2&seats=0')
    expect(await screen.findByText('Seats must be a whole number between 1 and 100000.')).toBeInTheDocument()
    expect(compare).not.toHaveBeenCalled()
  })

  it('adds a product from the picker to the URL, up to 4, and offers another plan of a product already chosen', async () => {
    const user = userEvent.setup()
    compare.mockResolvedValue(comparison(1, [item(alpha, alphaPlans[0], 1, estimated), item(beta, beta.plans[0], 1, estimated)]))
    renderPage('/compare?items=1,2')
    await screen.findByRole('table')
    await user.click(screen.getByRole('combobox', { name: 'Add a product' }))
    await user.click(await screen.findByRole('option', { name: 'Gamma' }))
    await waitFor(() => expect(where()).toBe('/compare?items=1,2,3'))
    // Alpha is chosen already: adding it again picks its first plan not yet in the comparison.
    await user.click(screen.getByRole('combobox', { name: 'Add a product' }))
    await user.click(await screen.findByRole('option', { name: 'Alpha' }))
    await waitFor(() => expect(where()).toBe('/compare?items=1,2,3,1:11'))
    expect(screen.getByRole('combobox', { name: 'Add a product' })).toBeDisabled()
    expect(screen.getByText('You can compare up to 4 items. Remove one to add another.')).toBeInTheDocument()
  })

  it('changes a plan and removes an item, updating the URL and the request', async () => {
    const user = userEvent.setup()
    compare.mockResolvedValue(comparison(1, [item(alpha, alphaPlans[0], 1, estimated), item(beta, beta.plans[0], 1, estimated), item(gamma, gamma.plans[0], 1, estimated)]))
    renderPage('/compare?items=1,2,3')
    await screen.findByRole('table')
    await user.click(screen.getByRole('combobox', { name: 'Plan for Alpha' }))
    await user.click(await screen.findByRole('option', { name: /Pro/ }))
    await waitFor(() => expect(where()).toBe('/compare?items=1:12,2,3'))
    await waitFor(() => expect(compare).toHaveBeenLastCalledWith([{ productId: 1, planId: 12 }, { productId: 2 }, { productId: 3 }], 1))
    await user.click(screen.getByRole('combobox', { name: 'Plan for Alpha' }))
    await user.click(await screen.findByRole('option', { name: 'Entry plan (lowest price)' }))
    await waitFor(() => expect(where()).toBe('/compare?items=1,2,3'))
    await user.click(screen.getByRole('button', { name: 'Remove Gamma' }))
    await waitFor(() => expect(where()).toBe('/compare?items=1,2'))
  })

  it('adds the chosen plan to the cart with the existing cart call, and disables it for a free plan', async () => {
    const user = userEvent.setup()
    cartAdd.mockResolvedValue({ itemCount: 1 })
    compare.mockResolvedValue(comparison(1, [item(alpha, alphaPlans[1], 1, estimated), item(gamma, gamma.plans[0], 1, estimated)]))
    renderPage('/compare?items=1:12,3')
    await screen.findByRole('table')
    expect(screen.getByRole('button', { name: 'Add to cart: Gamma' })).toBeDisabled()
    expect(screen.getByRole('link', { name: 'View product Alpha' })).toHaveAttribute('href', '/products/1')
    await user.click(screen.getByRole('button', { name: 'Add to cart: Alpha' }))
    await waitFor(() => expect(cartAdd).toHaveBeenCalledWith(1, 12))
    expect(await screen.findByText('Added Alpha — Pro to your cart')).toBeInTheDocument()
  })

  it('sends a signed-out visitor to sign in before the cart add', async () => {
    signedIn = false
    const user = userEvent.setup()
    compare.mockResolvedValue(comparison(1, [item(alpha, alphaPlans[1], 1, estimated), item(beta, beta.plans[0], 1, estimated)]))
    renderPage('/compare?items=1:12,2')
    await screen.findByRole('table')
    await user.click(screen.getByRole('button', { name: 'Add to cart: Alpha' }))
    expect(cartAdd).not.toHaveBeenCalled()
    expect(where()).toBe('/login?returnTo=/cart?add=1&plan=12')
  })

  it('has no a11y violations, empty or filled', async () => {
    compare.mockResolvedValue(comparison(1, [
      item(alpha, alphaPlans[0], 1, estimated, { includedFeatures: ['SSO'], trialDays: 7 }),
      item(beta, beta.plans[0], 1, { status: 'NOT_SIGNED_IN', amount: null, total: null, message: null }),
    ]))
    const empty = renderPage('/compare')
    await screen.findByText('Choose at least 2 products to compare')
    expect(await axe(empty.container)).toHaveNoViolations()
    empty.unmount()
    const filled = renderPage('/compare?items=1,2')
    await screen.findByRole('table')
    expect(await axe(filled.container)).toHaveNoViolations()
  })
})
