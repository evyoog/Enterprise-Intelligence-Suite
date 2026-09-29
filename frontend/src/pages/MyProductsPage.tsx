import { useEffect, useState } from 'react'
import {
  Alert, Box, Button, Chip, CircularProgress, Container, Grid, IconButton,
  Paper, Switch, Typography,
} from '@mui/material'
import { ChevronDown, ChevronUp, Settings2, Star } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../api/client'
import { dashboardApi, type Dashboard, type DashboardProduct } from '../api/dashboardApi'
import { myProductsApi } from '../api/registrationApi'
import { recommendationsApi, type Recommendations, type RecommendedProduct } from '../api/recommendationsApi'

type ViewState =
  | { kind: 'loading' }
  | { kind: 'loaded'; dashboard: Dashboard }
  | { kind: 'error'; message: string }

const WIDGET_LABELS: Record<string, string> = {
  organization: 'Organization overview',
  alerts: 'Alerts',
  recentlyUsed: 'Recently used',
  favorites: 'Favorites',
  recommendations: 'Recommended for you',
  products: 'All products',
}
const ALL_WIDGETS = Object.keys(WIDGET_LABELS)

// Guards against a preference row saved before a widget existed — it's
// appended at the end rather than silently disappearing from the page.
const withKnownWidgets = (order: string[]) => [...order, ...ALL_WIDGETS.filter((w) => !order.includes(w))]

/** "/my/products" — Phase 16: the personalized dashboard. Everything shown
 * here comes from GET /me/dashboard — real organization/entitlement data
 * (reused from earlier phases), real alerts derived from real facts (seat
 * count, subscription expiry, PAM requests, org MFA policy), and this
 * customer's own favorites/launch history. "recentlyUsed"/"favorites" are
 * both just different views over the SAME product list the backend returns
 * — not separate data sources. */
export function MyProductsPage() {
  const [state, setState] = useState<ViewState>({ kind: 'loading' })
  const [subscribingId, setSubscribingId] = useState<number | null>(null)
  const [customizing, setCustomizing] = useState(false)
  const [recommendations, setRecommendations] = useState<Recommendations | null>(null)

  const load = () => {
    setState({ kind: 'loading' })
    dashboardApi.get()
      .then((dashboard) => setState({ kind: 'loaded', dashboard }))
      .catch((e) => setState({ kind: 'error', message: e instanceof ApiError ? e.message : 'Could not load your dashboard.' }))
  }

  useEffect(load, [])
  useEffect(() => { recommendationsApi.get().then(setRecommendations).catch(() => setRecommendations({ featured: [], popular: [] })) }, [])

  if (state.kind === 'loading') {
    return (
      <Box>
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
      </Box>
    )
  }

  if (state.kind === 'error') {
    return (
      <Box>
        <Container maxWidth="md" disableGutters sx={{ pb: 4 }}>
          <Alert severity="error">{state.message}</Alert>
        </Container>
      </Box>
    )
  }

  const { dashboard } = state
  const preferences = { ...dashboard.preferences, widgetOrder: withKnownWidgets(dashboard.preferences.widgetOrder) }

  const subscribe = async (productId: number) => {
    if (subscribingId !== null) return
    setSubscribingId(productId)
    try {
      await myProductsApi.subscribe(productId)
      load()
    } finally {
      setSubscribingId(null)
    }
  }

  const toggleFavorite = async (product: DashboardProduct) => {
    // Optimistic — this is a personalization convenience, not a security
    // boundary, so a brief stale flash on failure is an acceptable tradeoff
    // for not blocking the click on a round trip.
    setState({
      kind: 'loaded',
      dashboard: { ...dashboard, products: dashboard.products.map((p) => p.productId === product.productId ? { ...p, favorite: !p.favorite } : p) },
    })
    try {
      if (product.favorite) await dashboardApi.removeFavorite(product.productId)
      else await dashboardApi.addFavorite(product.productId)
    } catch {
      load()
    }
  }

  const launch = (product: DashboardProduct) => {
    dashboardApi.recordLaunch(product.productId).then(load).catch(() => {})
    if (product.launchUrl) window.open(product.launchUrl, '_blank', 'noopener')
  }

  const isEntitled = (product: DashboardProduct) =>
    product.myAccessAssigned === true || (product.myAccessAssigned === null && product.subscriptionStatus === 'ACTIVE')

  const updatePreferences = (next: typeof preferences) => {
    setState({ kind: 'loaded', dashboard: { ...dashboard, preferences: next } })
    dashboardApi.updatePreferences(next).catch(() => {})
  }

  const toggleHidden = (widget: string) => {
    const hidden = preferences.hiddenWidgets.includes(widget)
      ? preferences.hiddenWidgets.filter((w) => w !== widget)
      : [...preferences.hiddenWidgets, widget]
    updatePreferences({ ...preferences, hiddenWidgets: hidden })
  }

  const moveWidget = (widget: string, direction: -1 | 1) => {
    const order = [...preferences.widgetOrder]
    const index = order.indexOf(widget)
    const swapWith = index + direction
    if (swapWith < 0 || swapWith >= order.length) return
    ;[order[index], order[swapWith]] = [order[swapWith], order[index]]
    updatePreferences({ ...preferences, widgetOrder: order })
  }

  const recentlyUsed = [...dashboard.products]
    .filter((p) => p.lastLaunchedAt)
    .sort((a, b) => (b.lastLaunchedAt ?? '').localeCompare(a.lastLaunchedAt ?? ''))
    .slice(0, 5)
  const favorites = dashboard.products.filter((p) => p.favorite)

  const visibleWidgets = preferences.widgetOrder.filter((w) => !preferences.hiddenWidgets.includes(w))

  return (
    <Box>
      <Container maxWidth="md" disableGutters sx={{ pb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3, gap: 1, flexWrap: 'wrap' }}>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>Your dashboard</Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button component={RouterLink} to="/my/subscriptions" size="small" variant="outlined">
              Manage subscriptions
            </Button>
            <Button startIcon={<Settings2 size={16} />} size="small" onClick={() => setCustomizing((v) => !v)}>
              Customize
            </Button>
          </Box>
        </Box>

        {customizing && (
          <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>Dashboard sections</Typography>
            {preferences.widgetOrder.map((widget, i) => (
              <Box key={widget} sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.5 }}>
                <Switch
                  size="small"
                  checked={!preferences.hiddenWidgets.includes(widget)}
                  onChange={() => toggleHidden(widget)}
                />
                <Typography sx={{ flex: 1 }}>{WIDGET_LABELS[widget] ?? widget}</Typography>
                <IconButton size="small" disabled={i === 0} onClick={() => moveWidget(widget, -1)}><ChevronUp size={16} /></IconButton>
                <IconButton size="small" disabled={i === preferences.widgetOrder.length - 1} onClick={() => moveWidget(widget, 1)}><ChevronDown size={16} /></IconButton>
              </Box>
            ))}
          </Paper>
        )}

        {visibleWidgets.map((widget) => {
          if (widget === 'organization' && dashboard.organization) {
            const org = dashboard.organization
            return (
              <Paper key={widget} variant="outlined" sx={{ p: 2.5, mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
                <Box>
                  <Typography sx={{ fontWeight: 700 }}>{org.name}</Typography>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                    {org.activeMemberCount} / {org.licensedSeats} seats used
                    {org.mfaRequired && ' · MFA required'}
                  </Typography>
                </Box>
                <Button component={RouterLink} to="/organization/business-dashboard" size="small" variant="outlined">
                  Business dashboard
                </Button>
              </Paper>
            )
          }
          if (widget === 'alerts' && dashboard.alerts.length > 0) {
            return (
              <Box key={widget} sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 3 }}>
                {dashboard.alerts.map((alert, i) => (
                  <Alert key={i} severity={alert.severity as 'info' | 'warning' | 'error'}>{alert.message}</Alert>
                ))}
              </Box>
            )
          }
          if (widget === 'recentlyUsed' && recentlyUsed.length > 0) {
            return <ProductRail key={widget} title="Recently used" products={recentlyUsed} onToggleFavorite={toggleFavorite} onLaunch={launch} isEntitled={isEntitled} />
          }
          if (widget === 'favorites' && favorites.length > 0) {
            return <ProductRail key={widget} title="Favorites" products={favorites} onToggleFavorite={toggleFavorite} onLaunch={launch} isEntitled={isEntitled} />
          }
          if (widget === 'recommendations' && recommendations && (recommendations.featured.length > 0 || recommendations.popular.length > 0)) {
            return (
              <Box key={widget}>
                {recommendations.featured.length > 0 && <RecommendationRail title="Featured" products={recommendations.featured} />}
                {recommendations.popular.length > 0 && <RecommendationRail title="Popular" products={recommendations.popular} />}
              </Box>
            )
          }
          if (widget === 'products') {
            return (
              <Box key={widget} sx={{ mb: 3 }}>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 1.5 }}>All products</Typography>
                <Grid container spacing={2}>
                  {dashboard.products.map((product) => (
                    <Grid size={{ xs: 12, sm: 6 }} key={product.productId}>
                      <ProductCard
                        product={product}
                        entitled={isEntitled(product)}
                        isOrgMember={dashboard.organization !== null}
                        subscribing={subscribingId === product.productId}
                        onSubscribe={() => subscribe(product.productId)}
                        onToggleFavorite={() => toggleFavorite(product)}
                        onLaunch={() => launch(product)}
                      />
                    </Grid>
                  ))}
                </Grid>
              </Box>
            )
          }
          return null
        })}
      </Container>
    </Box>
  )
}

function ProductRail({ title, products, onToggleFavorite, onLaunch, isEntitled }: {
  title: string
  products: DashboardProduct[]
  onToggleFavorite: (p: DashboardProduct) => void
  onLaunch: (p: DashboardProduct) => void
  isEntitled: (p: DashboardProduct) => boolean
}) {
  return (
    <Box sx={{ mb: 3 }}>
      <Typography variant="h6" sx={{ fontWeight: 700, mb: 1.5 }}>{title}</Typography>
      <Box sx={{ display: 'flex', gap: 1.5, overflowX: 'auto', pb: 1 }}>
        {products.map((product) => (
          <Paper key={product.productId} variant="outlined" sx={{ p: 1.5, minWidth: 200, flexShrink: 0 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Typography sx={{ fontWeight: 600, fontSize: 14 }} noWrap>{product.productName}</Typography>
              <IconButton size="small" onClick={() => onToggleFavorite(product)}>
                <Star size={14} fill={product.favorite ? 'currentColor' : 'none'} />
              </IconButton>
            </Box>
            {isEntitled(product) && (
              <Button size="small" fullWidth sx={{ mt: 1 }} variant="outlined" onClick={() => onLaunch(product)}>Launch</Button>
            )}
          </Paper>
        ))}
      </Box>
    </Box>
  )
}

function RecommendationRail({ title, products }: { title: string; products: RecommendedProduct[] }) {
  return (
    <Box sx={{ mb: 3 }}>
      <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{title}</Typography>
      <Box sx={{ display: 'flex', gap: 1.5, overflowX: 'auto', pb: 1 }}>
        {products.map((product) => (
          <Paper key={product.id} variant="outlined" sx={{ p: 1.5, minWidth: 200, flexShrink: 0 }}>
            <Typography sx={{ fontWeight: 600, fontSize: 14 }} noWrap>{product.name}</Typography>
            {product.category && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{product.category}</Typography>}
          </Paper>
        ))}
      </Box>
    </Box>
  )
}

function ProductCard({ product, entitled, isOrgMember, subscribing, onSubscribe, onToggleFavorite, onLaunch }: {
  product: DashboardProduct
  entitled: boolean
  isOrgMember: boolean
  subscribing: boolean
  onSubscribe: () => void
  onToggleFavorite: () => void
  onLaunch: () => void
}) {
  return (
    <Paper variant="outlined" sx={{ p: 2 }}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <Box>
          <Typography sx={{ fontWeight: 700 }}>{product.productName}</Typography>
          {product.category && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{product.category}</Typography>}
        </Box>
        <IconButton size="small" onClick={onToggleFavorite} aria-label="Toggle favorite">
          <Star size={16} fill={product.favorite ? 'currentColor' : 'none'} />
        </IconButton>
      </Box>

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, mt: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{isOrgMember ? 'Org subscription' : 'Subscription'}</Typography>
          <SubscriptionBadge status={product.subscriptionStatus} />
        </Box>
        {isOrgMember && (
          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>My access</Typography>
            {product.myAccessAssigned ? (
              <Chip size="small" color="success" label={product.myProductRole ?? 'Assigned'} />
            ) : (
              <Chip size="small" variant="outlined" label="Not assigned — contact your org admin" />
            )}
          </Box>
        )}
        {product.launchCount > 0 && (
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>
            Launched {product.launchCount} time{product.launchCount === 1 ? '' : 's'}
          </Typography>
        )}
      </Box>

      <Box sx={{ mt: 1.5, display: 'flex', gap: 1 }}>
        {entitled && <Button size="small" variant="contained" onClick={onLaunch}>Launch</Button>}
        {!isOrgMember && product.subscriptionStatus !== 'ACTIVE' && (
          <Button size="small" variant="outlined" disabled={subscribing} onClick={onSubscribe}>
            {subscribing ? 'Subscribing…' : 'Subscribe'}
          </Button>
        )}
      </Box>
    </Paper>
  )
}

function SubscriptionBadge({ status }: { status: DashboardProduct['subscriptionStatus'] }) {
  if (status === 'ACTIVE') return <Chip size="small" color="success" label="Active" />
  if (status === 'PENDING_SUBSCRIPTION') return <Chip size="small" color="warning" label="Pending" />
  if (status === 'EXPIRED') return <Chip size="small" variant="outlined" label="Expired" />
  if (status === 'CANCELLED') return <Chip size="small" variant="outlined" label="Cancelled" />
  return <Chip size="small" variant="outlined" label="Not subscribed" />
}
