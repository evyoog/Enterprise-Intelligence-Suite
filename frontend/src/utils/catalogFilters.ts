import type { Product, ProductPlan } from '../api/productsApi'

/** C94 (REQ-CAT-003.11 to .14): the catalog's filters, sort and badges, all worked out from data the products already carry. */
export type PricingModel = 'SUBSCRIPTION' | 'ONE_TIME' | 'FREE'
export type CatalogSort = 'name' | 'price' | 'newest' | 'rating'

export interface CatalogFilters {
  platformIds: number[]
  pricingModels: PricingModel[]
  features: string[]
  savedOnly: boolean
}

export const EMPTY_FILTERS: CatalogFilters = { platformIds: [], pricingModels: [], features: [], savedOnly: false }

export const NEW_PRODUCT_DAYS = 30

const paidPlans = (product: Pick<Product, 'plans'>): ProductPlan[] => product.plans.filter((p) => p.price > 0)

/** Subscription: some paid plan repeats (monthly or yearly). One-time: only one-time paid plans, or no plans but a flat price. Free: otherwise. */
export function pricingModelOf(product: Pick<Product, 'plans' | 'price'>): PricingModel {
  const paid = paidPlans(product)
  if (paid.some((p) => p.billingPeriod !== 'ONE_TIME')) return 'SUBSCRIPTION'
  if (paid.length > 0) return 'ONE_TIME'
  return product.plans.length === 0 && product.price > 0 ? 'ONE_TIME' : 'FREE'
}

/** The plan the card's "From" price shows: the cheapest paid plan. */
export function fromPlan(product: Pick<Product, 'plans'>): ProductPlan | undefined {
  return paidPlans(product).sort((a, b) => a.price - b.price)[0]
}

export function isNewProduct(product: Pick<Product, 'createdAt'>, now = Date.now()): boolean {
  if (!product.createdAt) return false
  const added = Date.parse(product.createdAt)
  return !Number.isNaN(added) && now - added <= NEW_PRODUCT_DAYS * 86_400_000 && added <= now + 86_400_000
}

export function activeFilterCount(filters: CatalogFilters): number {
  return filters.platformIds.length + filters.pricingModels.length + filters.features.length + (filters.savedOnly ? 1 : 0)
}

export function applyFilters(products: Product[], filters: CatalogFilters, savedIds: ReadonlySet<number>): Product[] {
  return products.filter((p) =>
    (filters.platformIds.length === 0 || p.platforms.some((pl) => filters.platformIds.includes(pl.id)))
    && (filters.pricingModels.length === 0 || filters.pricingModels.includes(pricingModelOf(p)))
    && (filters.features.length === 0 || filters.features.every((f) => (p.featureTags ?? []).includes(f)))
    && (!filters.savedOnly || savedIds.has(p.id)))
}

export interface Rating { averageRating: number; reviewCount: number }

const startingPrice = (p: Product) => fromPlan(p)?.price ?? (p.plans.length === 0 ? p.price : 0)

export function sortProducts(products: Product[], sort: CatalogSort, ratings: ReadonlyMap<number, Rating>): Product[] {
  const byName = (a: Product, b: Product) => a.name.localeCompare(b.name)
  const copy = [...products]
  switch (sort) {
    case 'price': return copy.sort((a, b) => startingPrice(a) - startingPrice(b) || byName(a, b))
    case 'newest': return copy.sort((a, b) => Date.parse(b.createdAt ?? '') - Date.parse(a.createdAt ?? '') || byName(a, b))
    case 'rating': return copy.sort((a, b) => (ratings.get(b.id)?.averageRating ?? 0) - (ratings.get(a.id)?.averageRating ?? 0)
      || (ratings.get(b.id)?.reviewCount ?? 0) - (ratings.get(a.id)?.reviewCount ?? 0) || byName(a, b))
    default: return copy.sort(byName)
  }
}

export interface FacetCount<T> { value: T; label?: string; count: number }

/** Counts for the filter panel, over the products the filters apply to (before filtering). */
export function buildFacets(products: Product[]) {
  const platforms = new Map<number, FacetCount<number>>()
  const models = new Map<PricingModel, number>()
  const features = new Map<string, number>()
  for (const p of products) {
    for (const pl of p.platforms) platforms.set(pl.id, { value: pl.id, label: pl.name, count: (platforms.get(pl.id)?.count ?? 0) + 1 })
    const model = pricingModelOf(p)
    models.set(model, (models.get(model) ?? 0) + 1)
    for (const f of new Set(p.featureTags ?? [])) features.set(f, (features.get(f) ?? 0) + 1)
  }
  return {
    platforms: [...platforms.values()].sort((a, b) => (a.label ?? '').localeCompare(b.label ?? '')),
    pricingModels: (['SUBSCRIPTION', 'ONE_TIME', 'FREE'] as PricingModel[]).filter((m) => models.has(m)).map((m) => ({ value: m, count: models.get(m)! })),
    features: [...features.entries()].map(([value, count]) => ({ value, count })).sort((a, b) => b.count - a.count || a.value.localeCompare(b.value)),
  }
}
