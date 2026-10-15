import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import type { Product } from '../../api/productsApi'
import { CatalogProductCard } from './CatalogProductCard'
import { COMPARE_STORAGE_KEY, CompareProvider, resetStandaloneCompareStore, useCompare } from './CompareContext'
import { CompareTray } from './CompareTray'

const product = (id: number, name: string): Product => ({
  id, name, description: '', category: 'ERP', price: 0, status: 'ACTIVE', ssoConnected: false, featured: false, platforms: [],
  plans: [{ id: id * 10, name: 'Plan', price: 10, billingPeriod: 'MONTHLY', currency: 'USD' }], version: 1, dependsOnProductIds: [], featureTags: [],
}) as Product
const products = [1, 2, 3, 4, 5].map((id) => product(id, `App ${id}`))
const names = new Map(products.map((p) => [p.id, p.name]))

/** Cards with the Compare tick, as the catalog page renders them, plus the tray. */
function Catalog() {
  const compare = useCompare()
  return (
    <>
      {products.map((p) => (
        <CatalogProductCard key={p.id} product={p} saved={false} onToggleSaved={vi.fn()} onSubscribe={vi.fn()} onAddToCart={vi.fn()}
          addToCartUnavailable={false} addingToCart={false}
          compared={compare.has(p.id)} onToggleCompare={() => compare.toggle(p.id)} compareFull={compare.full} />
      ))}
      <CompareTray names={names} />
    </>
  )
}
const tick = (n: number) => screen.getByRole('checkbox', { name: `Compare App ${n}` })
const stored = () => JSON.parse(sessionStorage.getItem(COMPARE_STORAGE_KEY) ?? 'null')

// TC-MKT-: Compare tick on the catalog cards and the compare tray (C102, REQ-MKT-005)
describe.each([
  ['with a CompareProvider', (ui: React.ReactElement) => <CompareProvider>{ui}</CompareProvider>],
  ['standalone, without a provider', (ui: React.ReactElement) => ui],
])('Compare tray %s', (_label, wrap) => {
  beforeEach(() => {
    sessionStorage.clear()
    resetStandaloneCompareStore()
  })

  it('shows nothing until a card is ticked, then the count, the chosen products and a link that needs 2', async () => {
    const user = userEvent.setup()
    renderWithProviders(wrap(<Catalog />))
    expect(screen.queryByRole('region', { name: 'Compare tray' })).not.toBeInTheDocument()
    await user.click(tick(1))
    const tray = screen.getByRole('region', { name: 'Compare tray' })
    expect(tray).toHaveTextContent('Compare (1)')
    expect(tray).toHaveTextContent('App 1')
    expect(screen.getByRole('button', { name: 'Compare now' })).toBeDisabled()
    expect(screen.getByText('Choose at least 2 products')).toBeInTheDocument()
    await user.click(tick(3))
    expect(tray).toHaveTextContent('Compare (2)')
    expect(screen.getByRole('link', { name: 'Compare now' })).toHaveAttribute('href', '/compare?items=1,3')
    expect(stored()).toEqual([1, 3])
  })

  it('unticks a card, removes a chip and clears the tray', async () => {
    const user = userEvent.setup()
    renderWithProviders(wrap(<Catalog />))
    await user.click(tick(1))
    await user.click(tick(2))
    await user.click(tick(1))
    expect(tick(1)).not.toBeChecked()
    expect(screen.getByRole('region', { name: 'Compare tray' })).toHaveTextContent('Compare (1)')
    await user.click(screen.getByRole('button', { name: 'Remove App 2 from the comparison' }))
    expect(screen.queryByRole('region', { name: 'Compare tray' })).not.toBeInTheDocument()
    await user.click(tick(4))
    await user.click(tick(5))
    await user.click(screen.getByRole('button', { name: 'Clear' }))
    expect(screen.queryByRole('region', { name: 'Compare tray' })).not.toBeInTheDocument()
    expect(stored()).toEqual([])
  })

  it('allows at most 4: the unticked cards are disabled once 4 are chosen', async () => {
    const user = userEvent.setup()
    renderWithProviders(wrap(<Catalog />))
    for (const n of [1, 2, 3, 4]) await user.click(tick(n))
    expect(tick(5)).toBeDisabled()
    expect(tick(2)).toBeEnabled()
    await user.click(tick(2))
    expect(tick(5)).toBeEnabled()
  })

  it('remembers the choice for the browser tab (sessionStorage) and survives a broken value', () => {
    sessionStorage.setItem(COMPARE_STORAGE_KEY, JSON.stringify([2, 4]))
    const first = renderWithProviders(wrap(<Catalog />))
    expect(tick(2)).toBeChecked()
    expect(tick(4)).toBeChecked()
    first.unmount()
    sessionStorage.setItem(COMPARE_STORAGE_KEY, '{not json')
    resetStandaloneCompareStore()
    renderWithProviders(wrap(<Catalog />))
    expect(tick(2)).not.toBeChecked()
    expect(screen.queryByRole('region', { name: 'Compare tray' })).not.toBeInTheDocument()
  })

  it('has no a11y violations', async () => {
    const user = userEvent.setup()
    const { container } = renderWithProviders(wrap(<Catalog />))
    await user.click(tick(1))
    await user.click(tick(2))
    expect(await axe(container)).toHaveNoViolations()
  })
})

describe('Compare tray without names', () => {
  beforeEach(() => { sessionStorage.clear(); resetStandaloneCompareStore() })
  it('falls back to a generic label for an unknown product', () => {
    sessionStorage.setItem(COMPARE_STORAGE_KEY, JSON.stringify([7, 8]))
    renderWithProviders(<CompareTray />)
    expect(screen.getByText('Product 7')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Compare now' })).toHaveAttribute('href', '/compare?items=7,8')
  })
})
