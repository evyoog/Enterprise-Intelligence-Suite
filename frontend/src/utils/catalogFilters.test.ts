import { describe, expect, it } from 'vitest'
import type { Product } from '../api/productsApi'
import { applyFilters, buildFacets, EMPTY_FILTERS, fromPlan, isNewProduct, pricingModelOf, sortProducts } from './catalogFilters'

const plan = (id: number, price: number, billingPeriod: 'MONTHLY' | 'YEARLY' | 'ONE_TIME' = 'MONTHLY') =>
  ({ id, name: `P${id}`, price, billingPeriod, currency: 'USD' as const })
const product = (id: number, name: string, over: Partial<Product> = {}): Product => ({
  id, name, price: 0, status: 'ACTIVE', ssoConnected: false, featured: false, platforms: [], plans: [], version: 1,
  dependsOnProductIds: [], featureTags: [], ...over,
}) as Product

// TC-CAT-048 to TC-CAT-051 (C94)
describe('catalog filters', () => {
  it('works out the pricing model from the plans', () => {
    expect(pricingModelOf(product(1, 'a', { plans: [plan(1, 0), plan(2, 10, 'YEARLY')] }))).toBe('SUBSCRIPTION')
    expect(pricingModelOf(product(2, 'b', { plans: [plan(3, 50, 'ONE_TIME')] }))).toBe('ONE_TIME')
    expect(pricingModelOf(product(3, 'c', { plans: [], price: 20 }))).toBe('ONE_TIME')
    expect(pricingModelOf(product(4, 'd', { plans: [plan(4, 0)] }))).toBe('FREE')
    expect(pricingModelOf(product(5, 'e'))).toBe('FREE')
  })

  it('shows the cheapest paid plan as the From price', () => {
    expect(fromPlan(product(1, 'a', { plans: [plan(1, 99), plan(2, 29), plan(3, 0)] }))?.id).toBe(2)
    expect(fromPlan(product(2, 'b', { plans: [plan(4, 0)] }))).toBeUndefined()
  })

  it('marks a product New for 30 days after it was added', () => {
    const now = Date.parse('2026-10-13T00:00:00Z')
    expect(isNewProduct({ createdAt: '2026-10-01T00:00:00Z' }, now)).toBe(true)
    expect(isNewProduct({ createdAt: '2026-08-01T00:00:00Z' }, now)).toBe(false)
    expect(isNewProduct({}, now)).toBe(false)
  })

  it('filters by platform, pricing model, feature (all chosen) and saved, and counts the facets', () => {
    const a = product(1, 'Alpha', { platforms: [{ id: 7, name: 'Valam' }], plans: [plan(1, 10)], featureTags: ['HRM', 'Payroll'] })
    const b = product(2, 'Beta', { platforms: [{ id: 8, name: 'Thittam' }], plans: [plan(2, 0)], featureTags: ['HRM'] })
    const saved = new Set([2])
    expect(applyFilters([a, b], { ...EMPTY_FILTERS, platformIds: [7] }, saved)).toEqual([a])
    expect(applyFilters([a, b], { ...EMPTY_FILTERS, pricingModels: ['FREE'] }, saved)).toEqual([b])
    expect(applyFilters([a, b], { ...EMPTY_FILTERS, features: ['HRM', 'Payroll'] }, saved)).toEqual([a])
    expect(applyFilters([a, b], { ...EMPTY_FILTERS, savedOnly: true }, saved)).toEqual([b])
    const facets = buildFacets([a, b])
    expect(facets.platforms.map((p) => [p.label, p.count])).toEqual([['Thittam', 1], ['Valam', 1]])
    expect(facets.features[0]).toEqual({ value: 'HRM', count: 2 })
    expect(facets.pricingModels.map((m) => m.value)).toEqual(['SUBSCRIPTION', 'FREE'])
  })

  it('sorts by name, price, newest and rating', () => {
    const a = product(1, 'Alpha', { plans: [plan(1, 50)], createdAt: '2026-01-01T00:00:00Z' })
    const b = product(2, 'Beta', { plans: [plan(2, 10)], createdAt: '2026-09-01T00:00:00Z' })
    const c = product(3, 'Gamma', { plans: [plan(3, 30)], createdAt: '2026-05-01T00:00:00Z' })
    const ratings = new Map([[3, { averageRating: 4.9, reviewCount: 10 }], [1, { averageRating: 4.1, reviewCount: 3 }]])
    expect(sortProducts([c, a, b], 'name', ratings).map((p) => p.name)).toEqual(['Alpha', 'Beta', 'Gamma'])
    expect(sortProducts([c, a, b], 'price', ratings).map((p) => p.name)).toEqual(['Beta', 'Gamma', 'Alpha'])
    expect(sortProducts([c, a, b], 'newest', ratings).map((p) => p.name)).toEqual(['Beta', 'Gamma', 'Alpha'])
    expect(sortProducts([c, a, b], 'rating', ratings).map((p) => p.name)).toEqual(['Gamma', 'Alpha', 'Beta'])
  })
})
