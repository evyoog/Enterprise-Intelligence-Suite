import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Box, Chip, CircularProgress, Container, Grid, InputAdornment,
  MenuItem, Select, TextField, Typography,
} from '@mui/material'
import { Search } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { productsApi, type Product, type ProductSearchResponse } from '../api/productsApi'
import { searchHistoryApi } from '../api/searchHistoryApi'
import { useAuth } from '../auth/AuthProvider'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { useInAppShell } from '../components/layout/appShellContext'
import { ProductTile } from '../components/products/ProductTile'
import { accentFor, iconFor } from '../utils/accentColor'

const UNCATEGORIZED = 'Other'
const DEBOUNCE_MS = 300

function groupByCategory(products: Product[]): Map<string, Product[]> {
  const groups = new Map<string, Product[]>()
  for (const product of products) {
    const key = product.category?.trim() || UNCATEGORIZED
    const bucket = groups.get(key)
    if (bucket) {
      bucket.push(product)
    } else {
      groups.set(key, [product])
    }
  }
  return groups
}

/**
 * "/products" — the public catalog. Phase 17: search, category/platform
 * filters, and sorting are all now real backend queries (ProductController#
 * searchProducts) instead of a client-side substring filter over the whole
 * catalog — facet chips below the search box come from the same response, so
 * they can never show a filter option with zero real matches.
 */
export function ProductsPage() {
  const auth = useAuth()
  const navigate = useNavigate()
  const { t } = useTranslation()
  // Signed in, this is the tool's catalog view inside the AppShell: no website
  // header and no marketing copy, just the search and filter controls.
  const inShell = useInAppShell()
  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [category, setCategory] = useState<string | null>(null)
  const [sortBy, setSortBy] = useState<'name' | 'price'>('name')
  const [result, setResult] = useState<ProductSearchResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [recentSearches, setRecentSearches] = useState<string[]>([])
  const [searchFocused, setSearchFocused] = useState(false)

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedQuery(query), DEBOUNCE_MS)
    return () => clearTimeout(timeout)
  }, [query])

  useEffect(() => {
    productsApi.search({ q: debouncedQuery || undefined, category: category ?? undefined, sortBy })
      .then(setResult)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load products.'))
  }, [debouncedQuery, category, sortBy])

  useEffect(() => {
    if (!auth.isAuthenticated) return
    searchHistoryApi.list().then((entries) => setRecentSearches(entries.map((e) => e.query))).catch(() => {})
  }, [auth.isAuthenticated])

  // Recorded once the user has settled on a query (debounce already did the
  // "stopped typing" wait) — not on every keystroke, and only for real,
  // non-empty search terms (see SearchHistoryService's own javadoc).
  useEffect(() => {
    if (!auth.isAuthenticated || !debouncedQuery.trim()) return
    searchHistoryApi.record(debouncedQuery.trim())
      .then(() => searchHistoryApi.list())
      .then((entries) => setRecentSearches(entries.map((e) => e.query)))
      .catch(() => {})
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedQuery, auth.isAuthenticated])

  const products = result?.items ?? null
  const groups = useMemo(() => products ? groupByCategory(products) : null, [products])
  const categoryFacets = result?.facets.categories ?? []
  const totalCount = categoryFacets.reduce((sum, f) => sum + f.count, 0)

  // C48: a quick-action Subscribe right on the catalog card — same rule as
  // the product detail page and the homepage cards: checkout when signed
  // in, a full-page sign-in (with a way back here) when not.
  const handleSubscribeClick = (productId: number) => {
    const checkoutPath = `/checkout/${productId}`
    if (!auth.isAuthenticated) {
      navigate(`/login?returnTo=${encodeURIComponent(checkoutPath)}`)
      return
    }
    navigate(checkoutPath)
  }

  return (
    <Box sx={inShell ? undefined : { minHeight: '100vh', bgcolor: 'background.default' }}>
      {!inShell && <SiteNavbar />}

      {/* SiteNavbar is position:fixed at 72px tall (landing.css's .navbar) —
          this banner's own top padding both clears it and gives the banner
          its height, so no separate spacer is needed. */}
      <Box
        sx={{
          ...(inShell ? { pt: 3, pb: 3, px: 1, borderRadius: 3 } : { pt: '112px', pb: 6, px: 2 }),
          background: `
            radial-gradient(ellipse 70% 140% at 85% 0%, rgba(115, 61, 255, 0.35), transparent 60%),
            radial-gradient(ellipse 60% 120% at 10% 100%, rgba(66, 216, 255, 0.18), transparent 65%),
            linear-gradient(160deg, #05060f 0%, #0c0e2b 55%, #171246 100%)
          `,
          color: '#fff',
        }}
      >
        <Container maxWidth="lg">
          {inShell ? (
            <>
              <Typography variant="h5" component="h1" sx={{ fontWeight: 700, mb: 0.5 }}>
                {t('appShell.catalogTitle')}
              </Typography>
              <Typography sx={{ color: '#c3c6d4', mb: 2.5 }}>{t('appShell.catalogSubtitle')}</Typography>
            </>
          ) : (
            <>
              <Typography variant="overline" sx={{ color: '#9da2af', letterSpacing: '.08em' }}>
                Vyoog Product Catalog
              </Typography>
              <Typography variant="h3" sx={{ fontWeight: 800, letterSpacing: '-0.02em', mb: 1 }}>
                Everything you can launch, in one place.
              </Typography>
              <Typography sx={{ color: '#c3c6d4', maxWidth: 560, mb: 3 }}>
                Browse every Vyoog product your organization can run — sign in once and each one you're connected to opens with no second login.
              </Typography>
            </>
          )}

          <Box sx={{ position: 'relative', maxWidth: 480 }}>
            <TextField
              fullWidth
              placeholder="Search products by name or category…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onFocus={() => setSearchFocused(true)}
              onBlur={() => setTimeout(() => setSearchFocused(false), 150)}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search size={18} color="#616B85" />
                    </InputAdornment>
                  ),
                },
              }}
              sx={{
                bgcolor: '#fff',
                borderRadius: 999,
                '& .MuiOutlinedInput-root': { borderRadius: 999 },
                '& fieldset': { border: 'none' },
              }}
            />
            {searchFocused && !query && recentSearches.length > 0 && (
              <Box
                sx={{
                  position: 'absolute', top: '110%', left: 0, right: 0, zIndex: 5,
                  bgcolor: '#fff', borderRadius: 2, boxShadow: 4, p: 1.5,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Typography variant="caption" sx={{ color: 'text.secondary', px: 0.5 }}>
                    Recent searches
                  </Typography>
                  {/* Phase 9 (2026.3.3): the backend/API client already
                      supported clearing search history (SearchHistoryController's
                      own DELETE /me/search-history) — there was simply no
                      button anywhere calling it. */}
                  <Typography
                    component="button"
                    type="button"
                    variant="caption"
                    onClick={() => searchHistoryApi.clear().then(() => setRecentSearches([])).catch(() => {})}
                    sx={{
                      color: 'text.secondary', textDecoration: 'underline', cursor: 'pointer',
                      background: 'none', border: 0, font: 'inherit', p: 0,
                    }}
                  >
                    Clear
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75, mt: 0.5 }}>
                  {recentSearches.map((term) => (
                    <Chip key={term} size="small" label={term} onClick={() => setQuery(term)} />
                  ))}
                </Box>
              </Box>
            )}
          </Box>

          {categoryFacets.length > 0 && (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 2.5 }}>
              <Chip
                size="small"
                label={`All (${totalCount})`}
                onClick={() => setCategory(null)}
                sx={{
                  bgcolor: category === null ? '#fff' : 'rgba(255,255,255,0.12)',
                  color: category === null ? '#171246' : '#fff',
                  fontWeight: category === null ? 700 : 400,
                }}
              />
              {categoryFacets.map((facet) => (
                <Chip
                  key={facet.category}
                  size="small"
                  label={`${facet.category} (${facet.count})`}
                  onClick={() => setCategory(facet.category)}
                  sx={{
                    bgcolor: category === facet.category ? '#fff' : 'rgba(255,255,255,0.12)',
                    color: category === facet.category ? '#171246' : '#fff',
                    fontWeight: category === facet.category ? 700 : 400,
                  }}
                />
              ))}
            </Box>
          )}

          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 2.5 }}>
            {products && (
              <Typography variant="body2" sx={{ color: '#9da2af' }}>
                {products.length} product{products.length === 1 ? '' : 's'}
                {category ? ` in ${category}` : ''}
              </Typography>
            )}
            <Select
              size="small"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as 'name' | 'price')}
              sx={{ bgcolor: '#fff', borderRadius: 999, fontSize: 13, '& fieldset': { border: 'none' } }}
            >
              <MenuItem value="name">Sort: Name</MenuItem>
              <MenuItem value="price">Sort: Price</MenuItem>
            </Select>
          </Box>
        </Container>
      </Box>

      {/* C54: full width in the signed-in catalog view — the app shell's
          main content area already has no cap; the public marketing page
          below keeps its considered reading-width layout. */}
      <Container
        component={inShell ? 'div' : 'main'}
        id={inShell ? undefined : 'main-content'}
        maxWidth={inShell ? false : 'lg'}
        disableGutters={inShell}
        sx={{ py: inShell ? 3 : 5 }}
      >
        {error && <Typography color="error" role="alert">{error}</Typography>}

        {!error && products === null && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress size={28} />
          </Box>
        )}

        {groups && groups.size === 0 && (
          <Typography sx={{ color: 'text.secondary' }}>
            No products match "{debouncedQuery}".
          </Typography>
        )}

        {groups && Array.from(groups.entries()).map(([groupCategory, items], groupIndex) => {
          const accent = accentFor(groupCategory)
          const CategoryIcon = iconFor(groupCategory)
          return (
            <Box key={groupCategory} sx={{ mb: 5 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 2 }}>
                <Box
                  sx={{
                    width: 30, height: 30, borderRadius: 1.5,
                    bgcolor: accent.bg, color: accent.fg,
                    display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                  }}
                >
                  <CategoryIcon size={16} />
                </Box>
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  {groupCategory}
                </Typography>
                <Chip size="small" label={`${items.length} app${items.length === 1 ? '' : 's'}`} sx={{ fontSize: 11 }} />
              </Box>

              <Grid container spacing={2.5}>
                {items.map((product, index) => (
                  <Grid key={product.id} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
                    <ProductTile
                      product={product}
                      animationDelay={(groupIndex * items.length + index) * 40}
                      onSubscribe={() => handleSubscribeClick(product.id)}
                    />
                  </Grid>
                ))}
              </Grid>
            </Box>
          )
        })}
      </Container>
    </Box>
  )
}
