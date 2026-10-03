import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Box, Button, Chip, Container, Grid, InputAdornment, MenuItem, Paper, Skeleton, TextField, Typography,
} from '@mui/material'
import { AppWindow, Layers, Plus, Search, Star, Tags } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../api/client'
import { catalogApi, type CatalogPlatform } from '../api/catalogApi'
import { productsApi, type Product, type ProductSearchResponse } from '../api/productsApi'
import { searchHistoryApi } from '../api/searchHistoryApi'
import { useAuth } from '../auth/AuthProvider'
import { useBuy } from '../components/cart/useBuy'
import { PageHeader } from '../components/layout/PageHeader'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { useInAppShell } from '../components/layout/appShellContext'
import { ProductTile } from '../components/products/ProductTile'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { ShowcaseCard } from '../components/ui/ShowcaseCard'
import { SummaryCard } from '../components/ui/SummaryCard'
import { SupportCTA } from '../components/ui/SupportCTA'

const DEBOUNCE_MS = 300
type SortKey = 'name' | 'price'

function matches(text: string | null | undefined, query: string) {
  return !!text && text.toLowerCase().includes(query.toLowerCase())
}

/**
 * "/products" — the Product Catalog (C66 design). Platforms (product
 * families) come from the public catalog API, applications from the existing
 * search API (Phase 17: real backend search, category facets and sorting).
 * Every number on the page is counted from that data; nothing is hard-coded.
 */
export function ProductsPage() {
  const auth = useAuth()
  const { t } = useTranslation()
  const inShell = useInAppShell()
  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [category, setCategory] = useState<string | null>(null)
  const [sortBy, setSortBy] = useState<SortKey>('name')
  const [result, setResult] = useState<ProductSearchResponse | null>(null)
  const [allApps, setAllApps] = useState<Product[] | null>(null)
  const [platforms, setPlatforms] = useState<CatalogPlatform[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [recentSearches, setRecentSearches] = useState<string[]>([])
  const [searchFocused, setSearchFocused] = useState(false)
  const [reload, setReload] = useState(0)
  const handleSubscribeClick = useBuy()

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedQuery(query), DEBOUNCE_MS)
    return () => clearTimeout(timeout)
  }, [query])

  useEffect(() => {
    productsApi.search({ q: debouncedQuery || undefined, category: category ?? undefined, sortBy })
      .then((r) => { setResult(r); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('catalog.loadError')))
  }, [debouncedQuery, category, sortBy, reload, t])

  // Totals for the summary cards, independent of the current filter.
  useEffect(() => {
    productsApi.list().then(setAllApps).catch(() => setAllApps([]))
    catalogApi.listPlatforms().then(setPlatforms).catch(() => setPlatforms([]))
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

  const apps = result?.items ?? null
  const categoryFacets = result?.facets.categories ?? []
  const visiblePlatforms = useMemo(() => (platforms ?? []).filter((p) =>
    (!debouncedQuery || matches(p.name, debouncedQuery) || matches(p.description, debouncedQuery))
    && (!category || p.categories.includes(category))), [platforms, debouncedQuery, category])
  const categoryCount = useMemo(() => new Set((allApps ?? []).map((a) => a.category?.trim()).filter(Boolean)).size, [allApps])
  const featuredCount = (allApps ?? []).filter((a) => a.featured).length
  const filtersActive = Boolean(query || category)
  const clearFilters = useCallback(() => { setQuery(''); setCategory(null) }, [])

  const content = (
    <>
      <PageHeader
        eyebrow={t('catalog.label')}
        title={t('catalog.title')}
        subtitle={t('catalog.subtitle')}
        action={auth.isAdmin ? (
          <Button component={RouterLink} to="/admin/settings/platform" variant="contained" startIcon={<Plus size={16} />}>
            {t('catalog.addProduct')}
          </Button>
        ) : undefined}
      />

      <Grid container spacing={2} sx={{ mb: 3 }}>
        {[
          { icon: Layers, label: t('catalog.summary.products'), value: platforms?.length, hint: t('catalog.summary.productsHint'), color: '#6366F1' },
          { icon: AppWindow, label: t('catalog.summary.apps'), value: allApps?.length, hint: t('catalog.summary.appsHint', { count: categoryCount }), color: '#3B82F6' },
          { icon: Tags, label: t('catalog.summary.categories'), value: allApps ? categoryCount : undefined, hint: t('catalog.summary.categoriesHint'), color: '#10B981' },
          { icon: Star, label: t('catalog.summary.featured'), value: allApps ? featuredCount : undefined, hint: t('catalog.summary.featuredHint'), color: '#F97316' },
        ].map((card) => (
          <Grid key={card.label} size={{ xs: 12, sm: 6, lg: 3 }}>
            <SummaryCard icon={card.icon} label={card.label} color={card.color} hint={card.hint}
              value={card.value === undefined ? <Skeleton width={32} /> : card.value} />
          </Grid>
        ))}
      </Grid>

      {/* Filter bar: categories (from the search facets), sort and search. */}
      <Paper variant="outlined" sx={{ p: 1.5, mb: 3, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
        <Box role="group" aria-label={t('catalog.filterLabel')} sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap', flex: 1, minWidth: 0 }}>
          <Chip label={t('catalog.all')} onClick={() => setCategory(null)} color={category === null ? 'primary' : 'default'}
            variant={category === null ? 'filled' : 'outlined'} aria-pressed={category === null} />
          {categoryFacets.map((facet) => (
            <Chip key={facet.category} label={`${facet.category} (${facet.count})`} onClick={() => setCategory(facet.category)}
              color={category === facet.category ? 'primary' : 'default'} variant={category === facet.category ? 'filled' : 'outlined'}
              aria-pressed={category === facet.category} />
          ))}
        </Box>
        <TextField select size="small" label={t('catalog.sortBy')} value={sortBy} onChange={(e) => setSortBy(e.target.value as SortKey)} sx={{ minWidth: 160 }}>
          <MenuItem value="name">{t('catalog.sort.name')}</MenuItem>
          <MenuItem value="price">{t('catalog.sort.price')}</MenuItem>
        </TextField>
        <Box sx={{ position: 'relative', width: { xs: '100%', sm: 280 } }}>
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
      </Paper>

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

      {/* Applications */}
      <Box component="section" aria-labelledby="catalog-apps" sx={{ mb: 4 }}>
        <Typography id="catalog-apps" component="h2" variant="h6" sx={{ mb: 1.5 }}>
          {t('catalog.appsHeading')}
          {apps && <Typography component="span" sx={{ color: 'text.secondary', fontWeight: 500 }}> ({apps.length}{category ? ` · ${category}` : ''})</Typography>}
        </Typography>
        {!error && apps === null && (
          <Grid container spacing={2.5}>
            {[0, 1, 2, 3].map((i) => <Grid key={i} size={{ xs: 12, sm: 6, lg: 4, xl: 3 }}><Skeleton variant="rounded" height={260} /></Grid>)}
          </Grid>
        )}
        {apps && apps.length === 0 && (
          <EmptyState title={t('catalog.emptyTitle')} description={t('catalog.emptyBody')}
            action={filtersActive ? <Button onClick={clearFilters}>{t('catalog.clearFilters')}</Button> : undefined} />
        )}
        {apps && apps.length > 0 && (
          <Grid container spacing={2.5}>
            {apps.map((product) => (
              <Grid key={product.id} size={{ xs: 12, sm: 6, lg: 4, xl: 3 }}>
                <ProductTile product={product} onSubscribe={() => handleSubscribeClick(product.id)} />
              </Grid>
            ))}
          </Grid>
        )}
      </Box>

      <SupportCTA />
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
