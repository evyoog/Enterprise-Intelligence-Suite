import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Box, Button, Chip, Collapse, Container, Grid, InputAdornment, MenuItem, Paper, Skeleton, Snackbar, TextField, ToggleButton, ToggleButtonGroup, Typography,
} from '@mui/material'
import { Heart, LayoutGrid, List, Plus, Search, SlidersHorizontal } from 'lucide-react'
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { catalogApi, type CatalogPlatform } from '../api/catalogApi'
import { productsApi, type ProductSearchResponse } from '../api/productsApi'
import { reviewsApi } from '../api/reviewsApi'
import { searchHistoryApi } from '../api/searchHistoryApi'
import { useAuth } from '../auth/AuthProvider'
import { useBuy } from '../components/cart/useBuy'
import { canAddToCart, useAddToCart } from '../components/cart/useAddToCart'
import { CatalogFilterPanel } from '../components/catalog/CatalogFilterPanel'
import { CatalogGrid } from '../components/catalog/CatalogGrid'
import { CatalogHero } from '../components/catalog/CatalogHero'
import { CatalogProductCard } from '../components/catalog/CatalogProductCard'
import { CategoryTiles } from '../components/catalog/CategoryTiles'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { useInAppShell } from '../components/layout/appShellContext'
import { useWishlist } from '../components/wishlist/WishlistContext'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { ShowcaseCard } from '../components/ui/ShowcaseCard'
import { SupportCTA } from '../components/ui/SupportCTA'
import {
  activeFilterCount, applyFilters, buildFacets, EMPTY_FILTERS, sortProducts, type CatalogFilters, type CatalogSort, type Rating,
} from '../utils/catalogFilters'

const DEBOUNCE_MS = 300
const VIEW_KEY = 'eis.catalog.view'
type View = 'grid' | 'list'

function matches(text: string | null | undefined, query: string) {
  return !!text && text.toLowerCase().includes(query.toLowerCase())
}

function readView(): View {
  try { return localStorage.getItem(VIEW_KEY) === 'list' ? 'list' : 'grid' } catch { return 'grid' }
}

/**
 * "/products" — the Product Catalog (C94 design). A hero, category tiles with counts, a filter panel (platform, pricing
 * model, features, saved) and the product cards in a grid or a list, sortable by name, price, newest and rating.
 * Platforms come from the public catalog API, applications from the search API; ratings from the reviews summary.
 * Filtering by platform, pricing model and features, and the sort, work on the loaded products. Nothing is hard-coded.
 */
export function ProductsPage() {
  const auth = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const inShell = useInAppShell()
  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [category, setCategory] = useState<string | null>(null)
  const [sortBy, setSortBy] = useState<CatalogSort>('name')
  const [filters, setFilters] = useState<CatalogFilters>(EMPTY_FILTERS)
  const [filtersOpen, setFiltersOpen] = useState(false)
  const [view, setView] = useState<View>(readView)
  const [result, setResult] = useState<ProductSearchResponse | null>(null)
  const [totalApps, setTotalApps] = useState<number | null>(null)
  const [platforms, setPlatforms] = useState<CatalogPlatform[] | null>(null)
  const [ratings, setRatings] = useState<ReadonlyMap<number, Rating>>(new Map())
  const [error, setError] = useState<string | null>(null)
  const [recentSearches, setRecentSearches] = useState<string[]>([])
  const [searchFocused, setSearchFocused] = useState(false)
  const [reload, setReload] = useState(0)
  const [toast, setToast] = useState<string | null>(null)
  const handleSubscribeClick = useBuy()
  const { addToCart, busyId, notice } = useAddToCart()
  const wishlist = useWishlist()

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedQuery(query), DEBOUNCE_MS)
    return () => clearTimeout(timeout)
  }, [query])

  useEffect(() => {
    productsApi.search({ q: debouncedQuery || undefined, category: category ?? undefined })
      .then((r) => { setResult(r); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('catalog.loadError')))
  }, [debouncedQuery, category, reload, t])

  // Totals for the "All" tile, independent of the current filter; platforms; ratings.
  useEffect(() => {
    productsApi.list().then((all) => setTotalApps(all.length)).catch(() => setTotalApps(0))
    catalogApi.listPlatforms().then(setPlatforms).catch(() => setPlatforms([]))
    reviewsApi.listRatings()
      .then((list) => setRatings(new Map(list.map((r) => [r.productId, { averageRating: r.averageRating, reviewCount: r.reviewCount }]))))
      .catch(() => setRatings(new Map()))
  }, [reload])

  useEffect(() => {
    if (!auth.isAuthenticated) return
    searchHistoryApi.list().then((entries) => setRecentSearches(entries.map((e) => e.query))).catch(() => {})
  }, [auth.isAuthenticated])

  // Recorded once the user has settled on a query (after the debounce).
  useEffect(() => {
    if (!auth.isAuthenticated || !debouncedQuery.trim()) return
    searchHistoryApi.record(debouncedQuery.trim())
      .then(() => searchHistoryApi.list())
      .then((entries) => setRecentSearches(entries.map((e) => e.query)))
      .catch(() => {})
  }, [debouncedQuery, auth.isAuthenticated])

  const changeView = (_: unknown, next: View | null) => {
    if (!next) return
    setView(next)
    try { localStorage.setItem(VIEW_KEY, next) } catch { /* the choice is just not remembered */ }
  }

  const loaded = result?.items ?? null
  const apps = useMemo(
    () => (loaded ? sortProducts(applyFilters(loaded, filters, wishlist.ids), sortBy, ratings) : null),
    [loaded, filters, wishlist.ids, sortBy, ratings])
  const facets = useMemo(() => buildFacets(loaded ?? []), [loaded])
  const categoryFacets = result?.facets.categories ?? []
  const visiblePlatforms = useMemo(() => (platforms ?? []).filter((p) =>
    (!debouncedQuery || matches(p.name, debouncedQuery) || matches(p.description, debouncedQuery))
    && (!category || p.categories.includes(category))), [platforms, debouncedQuery, category])
  const filterCount = activeFilterCount(filters)
  const filtersActive = Boolean(query || category || filterCount)
  const clearFilters = useCallback(() => { setQuery(''); setCategory(null); setFilters(EMPTY_FILTERS) }, [])

  const toggleSaved = (productId: number) => {
    if (!auth.isAuthenticated) {
      navigate(`/login?returnTo=${encodeURIComponent(location.pathname + location.search)}`)
      return
    }
    wishlist.toggle(productId).catch((e) => setToast(e instanceof ApiError ? e.message : t('wishlist.error')))
  }

  const content = (
    <>
      <CatalogHero actions={(
        <>
          <Button component={RouterLink} to="/offerings" variant="outlined">{t('catalog.viewOfferings')}</Button>
          {auth.isAuthenticated && (
            <Button component={RouterLink} to="/my/wishlist" variant="outlined" startIcon={<Heart size={16} aria-hidden />}>
              {wishlist.ids.size > 0 ? t('wishlist.openCount', { count: wishlist.ids.size }) : t('wishlist.open')}
            </Button>
          )}
          {auth.isAdmin && (
            <Button component={RouterLink} to="/admin/platforms/new" variant="contained" startIcon={<Plus size={16} />}>{t('catalog.addProduct')}</Button>
          )}
        </>
      )} />

      <CategoryTiles facets={categoryFacets} total={totalApps ?? loaded?.length ?? 0} selected={category} onSelect={setCategory} />

      {error && <Box sx={{ mb: 3 }}><ErrorState title={t('catalog.loadErrorTitle')} message={error} onRetry={() => setReload((n) => n + 1)} /></Box>}

      {/* Products (platforms) */}
      {visiblePlatforms.length > 0 && (
        <Box component="section" aria-labelledby="catalog-products" sx={{ mb: 4 }}>
          <Typography id="catalog-products" component="h2" variant="h6" sx={{ mb: 1.5 }}>
            {t('catalog.productsHeading')} <Typography component="span" sx={{ color: 'text.secondary', fontWeight: 500 }}>({visiblePlatforms.length})</Typography>
          </Typography>
          <Grid container spacing={2.5}>
            {visiblePlatforms.map((p) => (
              <Grid key={p.id} size={{ xs: 12, sm: 6, lg: 4, xl: 3 }}>
                <ShowcaseCard
                  name={p.name}
                  typeLabel={t('catalog.platformType')}
                  logoUrl={p.imageUrl}
                  color={p.primaryColor}
                  meta={t('catalog.appCount', { count: p.appCount })}
                  description={p.description}
                  features={p.featureTags}
                  status={{ label: t('catalog.status.available'), tone: 'success' }}
                  actionLabel={t('catalog.viewDetails')}
                  to={`/catalog/platforms/${p.id}`}
                />
              </Grid>
            ))}
          </Grid>
        </Box>
      )}

      {/* Applications: filter panel + toolbar + cards */}
      <Box component="section" aria-labelledby="catalog-apps">
        <Typography id="catalog-apps" component="h2" variant="h6" sx={{ mb: 1.5 }}>{t('catalog.appsHeading')}</Typography>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Button sx={{ display: { xs: 'inline-flex', md: 'none' }, mb: 1 }} variant="outlined" startIcon={<SlidersHorizontal size={16} />}
              aria-expanded={filtersOpen} onClick={() => setFiltersOpen((o) => !o)}>
              {t('catalog.filters.title')}{filterCount > 0 ? ` (${filterCount})` : ''}
            </Button>
            <Box sx={{ display: { xs: 'none', md: 'block' } }}>
              <CatalogFilterPanel facets={facets} filters={filters} onChange={setFilters} canFilterSaved={auth.isAuthenticated} />
            </Box>
            <Collapse in={filtersOpen} sx={{ display: { md: 'none' } }} unmountOnExit>
              <CatalogFilterPanel facets={facets} filters={filters} onChange={setFilters} canFilterSaved={auth.isAuthenticated} />
            </Collapse>
          </Grid>
          <Grid size={{ xs: 12, md: 9 }}>
            <Paper variant="outlined" sx={{ p: 1.5, mb: 2.5, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
              <Typography aria-live="polite" sx={{ flex: 1, minWidth: 140, fontSize: 14, color: 'text.secondary' }}>
                {apps ? t('catalog.toolbar.showing', { count: apps.length }) : <Skeleton width={120} />}
              </Typography>
              <Box sx={{ position: 'relative', width: { xs: '100%', sm: 260 } }}>
                <TextField
                  fullWidth size="small"
                  placeholder={t('catalog.searchPlaceholder')}
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onFocus={() => setSearchFocused(true)}
                  onBlur={() => setTimeout(() => setSearchFocused(false), 150)}
                  slotProps={{ htmlInput: { 'aria-label': t('catalog.searchPlaceholder') }, input: { startAdornment: <InputAdornment position="start"><Search size={16} aria-hidden /></InputAdornment> } }}
                />
                {searchFocused && !query && recentSearches.length > 0 && (
                  <Paper elevation={4} sx={{ position: 'absolute', top: '110%', left: 0, right: 0, zIndex: 5, p: 1.5 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('catalog.recentSearches')}</Typography>
                      <Button size="small" onClick={() => searchHistoryApi.clear().then(() => setRecentSearches([])).catch(() => {})}>
                        {t('catalog.clear')}
                      </Button>
                    </Box>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75, mt: 0.5 }}>
                      {recentSearches.map((term) => <Chip key={term} size="small" label={term} onClick={() => setQuery(term)} />)}
                    </Box>
                  </Paper>
                )}
              </Box>
              <TextField select size="small" label={t('catalog.sortBy')} value={sortBy} onChange={(e) => setSortBy(e.target.value as CatalogSort)} sx={{ minWidth: 150 }}>
                {(['name', 'price', 'newest', 'rating'] as CatalogSort[]).map((key) => <MenuItem key={key} value={key}>{t(`catalog.sort.${key}`)}</MenuItem>)}
              </TextField>
              <ToggleButtonGroup size="small" exclusive value={view} onChange={changeView} aria-label={t('catalog.toolbar.view')}>
                <ToggleButton value="grid" aria-label={t('catalog.toolbar.grid')}><LayoutGrid size={16} aria-hidden /></ToggleButton>
                <ToggleButton value="list" aria-label={t('catalog.toolbar.list')}><List size={16} aria-hidden /></ToggleButton>
              </ToggleButtonGroup>
            </Paper>

            {!error && apps === null && (
              <CatalogGrid>
                {[0, 1, 2, 3, 4, 5].map((i) => <Skeleton key={i} variant="rounded" height={320} />)}
              </CatalogGrid>
            )}
            {apps && apps.length === 0 && (
              <EmptyState title={t('catalog.emptyTitle')} description={t('catalog.emptyBody')}
                action={filtersActive ? <Button onClick={clearFilters}>{t('catalog.clearFilters')}</Button> : undefined} />
            )}
            {apps && apps.length > 0 && (
              <CatalogGrid list={view === 'list'}>
                {apps.map((product) => (
                  <CatalogProductCard
                    key={product.id}
                    product={product}
                    variant={view}
                    rating={ratings.get(product.id)}
                    saved={wishlist.has(product.id)}
                    onToggleSaved={() => toggleSaved(product.id)}
                    onSubscribe={() => handleSubscribeClick(product.id)}
                    onAddToCart={() => addToCart(product)}
                    addToCartUnavailable={!canAddToCart(product)}
                    addingToCart={busyId === product.id}
                  />
                ))}
              </CatalogGrid>
            )}
          </Grid>
        </Grid>
      </Box>

      <Box sx={{ mt: 4 }}><SupportCTA /></Box>
      {notice}
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </>
  )

  if (inShell) return content
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container component="main" id="main-content" maxWidth="xl" sx={{ pt: '112px', pb: 8 }}>
        {content}
      </Container>
    </Box>
  )
}
