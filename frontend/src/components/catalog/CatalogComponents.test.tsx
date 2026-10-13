import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import type { Product } from '../../api/productsApi'
import { EMPTY_FILTERS } from '../../utils/catalogFilters'
import { CatalogFilterPanel } from './CatalogFilterPanel'
import { CatalogProductCard } from './CatalogProductCard'
import { CategoryTiles } from './CategoryTiles'

const plan = (id: number, price: number, billingPeriod: 'MONTHLY' | 'ONE_TIME' = 'MONTHLY') =>
  ({ id, name: `Plan ${id}`, price, billingPeriod, currency: 'USD' as const })
const product = (over: Partial<Product> = {}): Product => ({
  id: 1, name: 'Valam.ai', description: 'Business management for HR and finance.', category: 'ERP', price: 0, status: 'ACTIVE',
  ssoConnected: false, featured: true, platforms: [{ id: 7, name: 'Valam' }], plans: [plan(1, 99), plan(2, 29)], version: 1,
  dependsOnProductIds: [], featureTags: ['HRM', 'Finance', 'Inventory', 'Reports'], createdAt: new Date().toISOString(), ...over,
}) as Product

const card = (over: Partial<Parameters<typeof CatalogProductCard>[0]> = {}) => (
  <CatalogProductCard product={product()} rating={{ averageRating: 4.8, reviewCount: 120 }} saved={false} onToggleSaved={vi.fn()}
    onSubscribe={vi.fn()} onAddToCart={vi.fn()} addToCartUnavailable={false} addingToCart={false} {...over} />
)

// TC-CAT-052 (C94, REQ-CAT-003.13)
describe('CatalogProductCard', () => {
  it('shows the badges, rating, feature tags and the cheapest paid plan as the From price', () => {
    renderWithProviders(card())
    expect(screen.getByText('Featured')).toBeInTheDocument()
    expect(screen.getByText('New')).toBeInTheDocument()
    expect(screen.getByLabelText('Rated 4.8 out of 5 from 120 reviews')).toBeInTheDocument()
    expect(screen.getByText('HRM')).toBeInTheDocument()
    expect(screen.getByText('+1')).toBeInTheDocument()
    expect(screen.getByText(/^From .*29\.00\/mo/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Valam.ai' })).toHaveAttribute('href', '/products/1')
  })

  it('shows no rating without reviews, no New badge for an old product and Free for a product without a paid plan', () => {
    renderWithProviders(card({ rating: undefined, product: product({ featured: false, createdAt: '2020-01-01T00:00:00Z', plans: [plan(3, 0)] }) }))
    expect(screen.queryByLabelText(/Rated/)).not.toBeInTheDocument()
    expect(screen.queryByText('New')).not.toBeInTheDocument()
    expect(screen.queryByText('Featured')).not.toBeInTheDocument()
    expect(screen.getByText('Free')).toBeInTheDocument()
  })

  it('has a heart that says whether the product is saved, and a disabled Add to cart when there is no paid plan', async () => {
    const user = userEvent.setup()
    const onToggleSaved = vi.fn()
    const onSubscribe = vi.fn()
    renderWithProviders(card({ onToggleSaved, onSubscribe, addToCartUnavailable: true }))
    const heart = screen.getByRole('button', { name: 'Save Valam.ai to wishlist' })
    expect(heart).toHaveAttribute('aria-pressed', 'false')
    await user.click(heart)
    expect(onToggleSaved).toHaveBeenCalledTimes(1)
    expect(screen.getByRole('button', { name: 'Add Valam.ai to cart' })).toBeDisabled()
    await user.click(screen.getByRole('button', { name: 'Subscribe' }))
    expect(onSubscribe).toHaveBeenCalledTimes(1)
  })

  it('shows a saved product with a pressed heart that removes it', () => {
    renderWithProviders(card({ saved: true }))
    expect(screen.getByRole('button', { name: 'Remove Valam.ai from wishlist' })).toHaveAttribute('aria-pressed', 'true')
  })

  it('has no detectable a11y violations in grid and list view', async () => {
    const { container } = renderWithProviders(<>{card()}{card({ variant: 'list', product: product({ id: 2, name: 'Varthan.ai' }) })}</>)
    expect(await axe(container)).toHaveNoViolations()
  })
})

// TC-CAT-053
describe('CategoryTiles', () => {
  it('shows All with the total and one tile per category with its count, and picks one at a time', async () => {
    const user = userEvent.setup()
    const onSelect = vi.fn()
    const { container } = renderWithProviders(<CategoryTiles facets={[{ category: 'ERP', count: 8 }, { category: 'CRM', count: 1 }]} total={9} selected="ERP" onSelect={onSelect} />)
    expect(screen.getByRole('button', { name: /^All 9 products/ })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByRole('button', { name: /^ERP 8 products/ })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByText('1 product')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /^All/ }))
    expect(onSelect).toHaveBeenCalledWith(null)
    await user.click(screen.getByRole('button', { name: /^CRM/ }))
    expect(onSelect).toHaveBeenCalledWith('CRM')
    expect(await axe(container)).toHaveNoViolations()
  })
})

// TC-CAT-054
describe('CatalogFilterPanel', () => {
  const facets = {
    platforms: [{ value: 7, label: 'Valam', count: 3 }],
    pricingModels: [{ value: 'SUBSCRIPTION' as const, count: 2 }, { value: 'FREE' as const, count: 1 }],
    features: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'].map((value, i) => ({ value, count: 8 - i })),
  }

  it('toggles filters, clears them all, and shows the first features with Show more', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()
    renderWithProviders(<CatalogFilterPanel facets={facets} filters={EMPTY_FILTERS} onChange={onChange} canFilterSaved />)
    expect(screen.getByRole('button', { name: 'Clear all' })).toBeDisabled()
    await user.click(screen.getByRole('checkbox', { name: /Valam/ }))
    expect(onChange).toHaveBeenLastCalledWith({ ...EMPTY_FILTERS, platformIds: [7] })
    await user.click(screen.getByRole('checkbox', { name: /Free/ }))
    expect(onChange).toHaveBeenLastCalledWith({ ...EMPTY_FILTERS, pricingModels: ['FREE'] })
    await user.click(screen.getByRole('checkbox', { name: /Saved products only/ }))
    expect(onChange).toHaveBeenLastCalledWith({ ...EMPTY_FILTERS, savedOnly: true })
    expect(screen.queryByRole('checkbox', { name: /^H/ })).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Show more' }))
    expect(screen.getByRole('checkbox', { name: /^H/ })).toBeInTheDocument()
  })

  it('offers Saved only to signed-in users only, enables Clear all when a filter is set and has no a11y violations', async () => {
    const { container } = renderWithProviders(<CatalogFilterPanel facets={facets} filters={{ ...EMPTY_FILTERS, features: ['A'] }} onChange={vi.fn()} canFilterSaved={false} />)
    expect(screen.queryByRole('checkbox', { name: /Saved products only/ })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Clear all' })).toBeEnabled()
    expect(await axe(container)).toHaveNoViolations()
  })
})
