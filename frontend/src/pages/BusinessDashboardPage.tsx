import { useEffect, useMemo, useState, type ComponentType } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Avatar, Box, Button, ButtonBase, Chip, Grid, IconButton, MenuItem, Paper, Skeleton, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, TableSortLabel, TextField, Tooltip, Typography,
} from '@mui/material'
import { alpha } from '@mui/material/styles'
import {
  AlertTriangle, AppWindow, ArrowDownRight, ArrowRight, ArrowUpRight, CheckCircle2, CreditCard, ExternalLink, Info,
  LifeBuoy, RefreshCw, Rocket, Star, Store, Users, UsersRound,
} from 'lucide-react'
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { businessDashboardApi, type BusinessApplication, type BusinessDashboard } from '../api/businessDashboardApi'
import { dashboardApi, type DashboardAlert, type DashboardProduct } from '../api/dashboardApi'
import { serviceStatusApi } from '../api/serviceStatusApi'
import { renewalsApi } from '../api/renewalsApi'
import { organizationApi } from '../api/registrationApi'
import { organizationAuditLogApi } from '../api/auditLogApi'
import { sessionsApi } from '../api/sessionsApi'
import { myPermissionsApi } from '../api/myPermissionsApi'
import { mfaApi } from '../api/mfaApi'
import { samlApi } from '../api/samlApi'
import { oidcApi } from '../api/oidcApi'
import { useAuth } from '../auth/AuthProvider'
import { AnimatedBars } from '../components/dashboard/AnimatedBars'
import { AnimatedDonut, type DonutDatum } from '../components/dashboard/AnimatedDonut'
import { DashPanel, PanelEmpty, PanelLink } from '../components/dashboard/DashPanel'
import { KpiCard } from '../components/dashboard/KpiCard'
import { MeterBar } from '../components/dashboard/MeterBar'
import {
  ALERT_ROUTES, SERVICE_TONE, activityLabel, permissionLabel, relativeTime, worstStatus, type Tone,
} from '../components/dashboard/dashboardFormat'
import { fadeUp, useMotionAllowed } from '../components/dashboard/motion'
import { useResource } from '../components/dashboard/useResource'
import { StatusBadge } from '../components/ui/StatusBadge'
import { useLocalePreference } from '../theming/LocalePreferenceProvider'

/** A trend needs one currency present on both sides to mean anything — with
 * more than one currency, or a zero last-period base (nothing to compare a
 * percentage against), this returns no trend rather than a misleading one. */
function spendTrend(thisPeriod: Record<string, number>, lastPeriod: Record<string, number>) {
  const currencies = new Set([...Object.keys(thisPeriod), ...Object.keys(lastPeriod)])
  if (currencies.size !== 1) return null
  const currency = [...currencies][0]
  const last = lastPeriod[currency] ?? 0
  const current = thisPeriod[currency] ?? 0
  if (last === 0) return null
  return { currency, current, trendPercent: ((current - last) / last) * 100 }
}

type SortKey = 'productName' | 'subscriptionStatus' | 'assignedMembers' | 'totalLaunches' | 'lastUsedAt'
type StatusFilter = 'ACTIVE' | 'OTHER' | null

const SUB_TONE: Record<string, Tone> = { ACTIVE: 'success', PENDING_SUBSCRIPTION: 'info', SUSPENDED: 'warning', CANCELLED: 'neutral', EXPIRED: 'neutral' }

function StatusDot({ tone, label }: { tone: Tone; label: string }) {
  return <StatusBadge tone={tone} label={label} />
}

/**
 * "/organization/business-dashboard" — the organization admin's dashboard
 * (C69). A workspace of status, summary, analytics, activity and actions,
 * every value read from an existing API (no invented numbers):
 *
 * - business dashboard (`/organization/me/business-dashboard`): seats,
 *   applications with launches and members, subscriptions, spend, alerts;
 * - personal dashboard (`/me/dashboard`): launch URLs, recently used,
 *   favourites (add/remove), and the user's own alerts;
 * - service status, renewals, members (the signed-in user's role and status),
 *   the organization audit log, sessions (current sign-in), permissions, MFA
 *   status and identity providers.
 *
 * Each source loads on its own and fails on its own (DashPanel shows Retry).
 * Launches are stored as totals per user and application — there is no
 * launch history — so there is no launches-over-time chart or date-range
 * filter; see C69. Organization administration (members, groups, MFA policy,
 * privileged access) lives on /organization/settings.
 */
export function BusinessDashboardPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const auth = useAuth()
  const motion = useMotionAllowed()
  const { formatDate: fmtDate, formatDateTime } = useLocalePreference()
  const formatDate = (iso?: string | null) => (iso ? fmtDate(iso) : '—')
  const ago = (iso: string) => relativeTime(iso, i18n.language, fmtDate)

  const { hash } = useLocation()
  const [dashboard, setDashboard] = useState<BusinessDashboard | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [reloadToken, setReloadToken] = useState(0)
  const [refreshing, setRefreshing] = useState(false)

  useEffect(() => {
    businessDashboardApi.get()
      .then((d) => { setDashboard(d); setError(null) })
      .catch((e) => {
        // ORG_ADMIN-only (MANAGE_ORGANIZATION, enforced server-side). A member
        // without it (403) or an individual with no organization (404) is sent
        // to their own personal dashboard rather than shown a permission error.
        if (e instanceof ApiError && (e.status === 403 || e.status === 404)) {
          navigate('/my/products', { replace: true })
          return
        }
        setError(e instanceof ApiError ? e.message : t('bizDash.loadError'))
      })
      .finally(() => setRefreshing(false))
  }, [navigate, reloadToken, t])

  const ready = dashboard !== null
  // In-page links (e.g. the Launches KPI → #usage-analytics).
  useEffect(() => {
    if (!hash || !ready) return
    document.getElementById(hash.slice(1))?.scrollIntoView({ behavior: motion ? 'smooth' : 'auto', block: 'start' })
  }, [hash, ready, motion])
  const me = useResource(dashboardApi.get, t('bizDash.loadError'), ready)
  const status = useResource(serviceStatusApi.get, t('bizDash.loadError'), ready)
  const renewals = useResource(renewalsApi.myRenewals, t('bizDash.loadError'), ready)
  const members = useResource(organizationApi.listMyOrgUsers, t('bizDash.loadError'), ready)
  const audit = useResource(() => organizationAuditLogApi.search({ size: 8 }), t('bizDash.loadError'), ready)
  const sessions = useResource(sessionsApi.list, t('bizDash.loadError'), ready)
  const permissions = useResource(myPermissionsApi.get, t('bizDash.loadError'), ready)
  const mfa = useResource(mfaApi.getStatus, t('bizDash.loadError'), ready)
  const saml = useResource(samlApi.list, t('bizDash.loadError'), ready)
  const oidc = useResource(oidcApi.list, t('bizDash.loadError'), ready)

  const refresh = () => { setRefreshing(true); setReloadToken((n) => n + 1); me.reload(); status.reload() }

  // Filters over the applications the page already has (C54 behaviour kept).
  const [appFilter, setAppFilter] = useState<number | ''>('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>(null)
  const [sortKey, setSortKey] = useState<SortKey>('productName')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc')
  const toggleSort = (key: SortKey) => {
    if (sortKey === key) setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'))
    else { setSortKey(key); setSortDir('asc') }
  }

  const applications = useMemo(() => dashboard?.applications ?? [], [dashboard])
  const filtered = useMemo(() => {
    let rows = applications
    if (appFilter !== '') rows = rows.filter((a) => a.productId === appFilter)
    if (statusFilter) rows = rows.filter((a) => (statusFilter === 'ACTIVE' ? a.subscriptionStatus === 'ACTIVE' : a.subscriptionStatus !== 'ACTIVE'))
    const dir = sortDir === 'asc' ? 1 : -1
    return [...rows].sort((a, b) => {
      switch (sortKey) {
        case 'productName': return dir * a.productName.localeCompare(b.productName)
        case 'subscriptionStatus': return dir * (a.subscriptionStatus ?? '').localeCompare(b.subscriptionStatus ?? '')
        case 'assignedMembers': return dir * (a.assignedMembers - b.assignedMembers)
        case 'totalLaunches': return dir * (a.totalLaunches - b.totalLaunches)
        case 'lastUsedAt': return dir * ((a.lastUsedAt ?? '').localeCompare(b.lastUsedAt ?? ''))
        default: return 0
      }
    })
  }, [applications, appFilter, statusFilter, sortKey, sortDir])
  const filtersActive = appFilter !== '' || statusFilter !== null
  const clearFilters = () => { setAppFilter(''); setStatusFilter(null) }

  // Personal products by id: launch URLs, favourites, own last launch.
  const myProducts = useMemo(() => new Map((me.data?.products ?? []).map((p) => [p.productId, p])), [me.data])
  const [favoriteOverrides, setFavoriteOverrides] = useState<Record<number, boolean>>({})
  const isFavorite = (p: DashboardProduct) => favoriteOverrides[p.productId] ?? p.favorite
  const toggleFavorite = (p: DashboardProduct) => {
    const next = !isFavorite(p)
    setFavoriteOverrides((o) => ({ ...o, [p.productId]: next }))
    ;(next ? dashboardApi.addFavorite(p.productId) : dashboardApi.removeFavorite(p.productId))
      .catch(() => setFavoriteOverrides((o) => ({ ...o, [p.productId]: !next })))
  }
  const launch = (productId: number) => {
    const product = myProducts.get(productId)
    if (!product?.launchUrl) { navigate(`/products/${productId}`); return }
    dashboardApi.recordLaunch(productId).then(me.reload).catch(() => {})
    window.open(product.launchUrl, '_blank', 'noopener')
  }

  if (error) return <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>

  // ---- derived values (all from loaded data) ----
  const org = dashboard?.organization
  const seats = dashboard?.seatUsage
  const unusedSeats = seats ? Math.max(0, seats.licensedSeats - seats.activeMemberCount) : 0
  const overLimit = seats ? seats.activeMemberCount > seats.licensedSeats : false
  const activeSubs = applications.filter((a) => a.subscriptionStatus === 'ACTIVE').length
  const totalLaunches = applications.reduce((s, a) => s + a.totalLaunches, 0)

  const myEmail = (auth.user?.email ?? auth.user?.username ?? '').toLowerCase()
  const myMember = members.data?.find((m) => (m.email ?? '').toLowerCase() === myEmail)
  const myName = myMember ? [myMember.firstName, myMember.lastName].filter(Boolean).join(' ') : ''
  const displayName = myName || auth.user?.username || ''

  const purchased = (status.data?.products ?? []).filter((p) => p.purchased)
  const platformUp = dashboard?.serviceHealth.platformStatus === 'UP'
  const worst = status.data?.enabled ? worstStatus(purchased.map((p) => p.status)) : 'OPERATIONAL'
  const overall: { tone: Tone; label: string } = !platformUp
    ? { tone: 'error', label: t('bizDash.status.platformDown') }
    : worst === 'OPERATIONAL'
      ? { tone: 'success', label: t('bizDash.status.allOperational') }
      : { tone: SERVICE_TONE[worst], label: t(`bizDash.status.overall.${worst}`) }
  const providers = [...(saml.data ?? []), ...(oidc.data ?? [])]
  const federationOn = providers.some((p) => p.enabled)

  const hour = new Date().getHours()
  const greeting = t(hour < 12 ? 'bizDash.welcome.morning' : hour < 18 ? 'bizDash.welcome.afternoon' : 'bizDash.welcome.evening', { name: displayName })

  const attention: DashboardAlert[] = (() => {
    const seen = new Set<string>()
    return [...(dashboard?.alerts ?? []), ...(me.data?.alerts ?? [])].filter((a) => {
      const key = `${a.type}:${a.message}`
      if (seen.has(key) || (a.type === 'SEAT_LIMIT_REACHED' && seen.has('SEAT'))) return false
      seen.add(key)
      if (a.type === 'SEAT_LIMIT_REACHED') seen.add('SEAT')
      return true
    })
  })()

  const upcoming = [...(renewals.data ?? [])].sort((a, b) => a.renewalDate.localeCompare(b.renewalDate))
  const nextRenewal = upcoming[0]
  const spend = dashboard && Object.keys(dashboard.billing.spentThisPeriodByCurrency).length > 0
    ? { text: Object.entries(dashboard.billing.spentThisPeriodByCurrency).map(([cur, amt]) => `${(amt / 100).toFixed(2)} ${cur}`).join(', '),
      trend: spendTrend(dashboard.billing.spentThisPeriodByCurrency, dashboard.billing.spentLastPeriodByCurrency) }
    : null

  const donutData: DonutDatum[] = [
    { key: 'ACTIVE', label: t('bizDash.subs.active'), value: activeSubs, tone: 'success' },
    { key: 'OTHER', label: t('bizDash.subs.other'), value: applications.length - activeSubs, tone: 'neutral' },
  ]

  const recent = [...(me.data?.products ?? [])].filter((p) => p.lastLaunchedAt).sort((a, b) => (b.lastLaunchedAt ?? '').localeCompare(a.lastLaunchedAt ?? '')).slice(0, 5)
  const favorites = (me.data?.products ?? []).filter(isFavorite)
  const currentSession = sessions.data?.find((s) => s.current)

  const quickActions: { label: string; to: string; icon: ComponentType<{ size?: number | string }> }[] = [
    { label: t('bizDash.quick.myApps'), to: '/my/products', icon: AppWindow },
    { label: t('bizDash.quick.catalog'), to: '/products', icon: Store },
    { label: t('bizDash.quick.members'), to: '/organization/settings#members', icon: UsersRound },
    { label: t('bizDash.quick.billing'), to: '/organization/billing', icon: CreditCard },
    { label: t('bizDash.quick.support'), to: '/support/tickets', icon: LifeBuoy },
  ]

  const sortHeader = (key: SortKey, label: string, align: 'left' | 'right' = 'left') => (
    <TableCell align={align} sortDirection={sortKey === key ? sortDir : false}>
      <TableSortLabel active={sortKey === key} direction={sortKey === key ? sortDir : 'asc'} onClick={() => toggleSort(key)}>{label}</TableSortLabel>
    </TableCell>
  )

  return (
    <Box sx={{ pb: 4, display: 'flex', flexDirection: 'column', gap: 2.5 }}>
      {/* 4. Organization header */}
      <Box component="header" sx={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap', ...fadeUp(motion) }}>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="overline" component="p" sx={{ color: 'primary.main', lineHeight: 1.6 }}>{t('bizDash.organization')}</Typography>
          {org ? <Typography component="h1" variant="h4" sx={{ fontSize: { xs: 24, sm: 28 } }}>{org.name}</Typography> : <Skeleton width={220} height={40} />}
          <Typography sx={{ color: 'text.secondary' }}>{t('bizDash.subtitle')}</Typography>
        </Box>
        <Button variant="outlined" onClick={() => navigate('/organization/identity-federation')}>{t('bizDash.identityFederation')}</Button>
      </Box>

      {/* 5. Welcome + platform status */}
      <Paper variant="outlined" sx={{ p: { xs: 2, sm: 2.5 }, borderRadius: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap', ...fadeUp(motion, 1) }}>
        <Box sx={{ minWidth: 0 }}>
          {members.loading && !displayName ? <Skeleton width={260} /> : (
            <Typography sx={{ fontWeight: 600, fontSize: 17 }}>{greeting}</Typography>
          )}
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('bizDash.welcome.body')}</Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
          {ready && !status.loading
            ? <ButtonBase component={RouterLink} to="/status" sx={{ borderRadius: 2, px: 1, py: 0.5, '&:hover': { bgcolor: 'action.hover' } }}
              aria-label={`${overall.label}. ${t('bizDash.health.viewStatus')}`}>
              <StatusDot tone={overall.tone} label={overall.label} />
            </ButtonBase>
            : <Skeleton width={180} />}
          {seats && (unusedSeats > 0 || overLimit) && (
            <Button component={RouterLink} to="/organization/settings#members" size="small" variant="outlined" color={overLimit ? 'warning' : 'primary'}
              endIcon={<ArrowRight size={14} aria-hidden />}>
              {overLimit ? t('bizDash.welcome.overLimit') : t('bizDash.welcome.unusedSeats', { count: unusedSeats })}
            </Button>
          )}
        </Box>
      </Paper>

      {/* 6. Quick actions */}
      <Box component="nav" aria-label={t('bizDash.quick.label')} sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', ...fadeUp(motion, 2) }}>
        {quickActions.map(({ label, to, icon: Icon }) => (
          <Button key={to} component={RouterLink} to={to} variant="outlined" startIcon={<Icon size={16} />}
            sx={{ bgcolor: 'background.paper', color: 'text.primary', borderColor: 'divider', transition: 'border-color 150ms, background-color 150ms',
              '& .MuiButton-startIcon': { color: 'primary.main' } }}>
            {label}
          </Button>
        ))}
      </Box>

      {/* 7. Overview KPIs */}
      <Box component="section" aria-label={t('bizDash.kpi.label')}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
            <KpiCard index={0} motion={motion} icon={Users} label={t('bizDash.kpi.seats')} value={seats?.licensedSeats}
              sub={seats ? t('bizDash.kpi.seatsUsed', { percent: Math.round(seats.utilizationPercent) }) : undefined}
              footer={seats && <MeterBar motion={motion} value={seats.utilizationPercent} tone={overLimit ? 'error' : seats.utilizationPercent >= 90 ? 'warning' : 'primary'} label={t('bizDash.seats.utilization')} />}
              to="/organization/settings#members" actionLabel={t('bizDash.kpi.manage')} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
            <KpiCard index={1} motion={motion} icon={UsersRound} label={t('bizDash.kpi.members')} value={seats?.activeMemberCount}
              sub={seats ? t('bizDash.kpi.membersOf', { count: seats.licensedSeats }) : undefined}
              to="/organization/settings#members" actionLabel={t('bizDash.kpi.view')} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
            <KpiCard index={2} motion={motion} icon={AppWindow} label={t('bizDash.kpi.apps')} value={dashboard ? applications.length : undefined}
              sub={dashboard ? t('bizDash.kpi.appsActive', { count: activeSubs }) : undefined}
              to="/my/products" actionLabel={t('bizDash.kpi.explore')} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
            <KpiCard index={3} motion={motion} icon={Rocket} label={t('bizDash.kpi.launches')} value={dashboard ? totalLaunches : undefined}
              sub={dashboard ? t('bizDash.kpi.launchesAllTime') : undefined}
              to="/organization/business-dashboard#usage-analytics" actionLabel={t('bizDash.kpi.analytics')} />
          </Grid>
        </Grid>
      </Box>

      {/* 8–10. Usage analytics */}
      <Box id="usage-analytics" component="section" aria-labelledby="usage-analytics-title" sx={{ scrollMarginTop: 88 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1.5, flexWrap: 'wrap', mb: 1.5 }}>
          <Typography id="usage-analytics-title" component="h2" sx={{ fontWeight: 600, fontSize: 17 }}>{t('bizDash.analytics.title')}</Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
            <TextField select size="small" label={t('bizDash.analytics.application')} value={appFilter} sx={{ minWidth: 190 }}
              onChange={(e) => setAppFilter(e.target.value === '' ? '' : Number(e.target.value))}>
              <MenuItem value="">{t('bizDash.analytics.allApps')}</MenuItem>
              {applications.map((a) => <MenuItem key={a.productId} value={a.productId}>{a.productName}</MenuItem>)}
            </TextField>
            <TextField select size="small" label={t('bizDash.analytics.subscription')} value={statusFilter ?? ''} sx={{ minWidth: 170 }}
              onChange={(e) => setStatusFilter((e.target.value || null) as StatusFilter)}>
              <MenuItem value="">{t('bizDash.analytics.allStatuses')}</MenuItem>
              <MenuItem value="ACTIVE">{t('bizDash.subs.active')}</MenuItem>
              <MenuItem value="OTHER">{t('bizDash.subs.other')}</MenuItem>
            </TextField>
            <Tooltip title={t('bizDash.analytics.refresh')}>
              <span>
                <IconButton onClick={refresh} disabled={refreshing} aria-label={t('bizDash.analytics.refresh')} sx={{ border: '1px solid', borderColor: 'divider' }}>
                  <RefreshCw size={16} />
                </IconButton>
              </span>
            </Tooltip>
          </Box>
        </Box>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, lg: 8 }}>
            <DashPanel id="usage-by-app" title={t('bizDash.analytics.usageByApp')} loading={!ready} sx={{ height: '100%' }}
              action={<PanelLink to="/my/products">{t('bizDash.analytics.viewAll')}</PanelLink>}>
              {filtered.length === 0
                ? <PanelEmpty title={applications.length ? t('bizDash.table.noMatch') : t('bizDash.empty.noApps')}
                    action={applications.length ? <Button size="small" onClick={clearFilters}>{t('bizDash.table.clear')}</Button> : <PanelLink to="/products">{t('bizDash.empty.exploreCatalog')}</PanelLink>} />
                : <AnimatedBars motion={motion} ariaLabel={t('bizDash.analytics.usageByApp')} actionLabel={t('bizDash.analytics.openDetails')}
                    data={[...filtered].sort((a, b) => b.totalLaunches - a.totalLaunches).map((a) => ({
                      key: a.productId, label: a.productName, value: a.totalLaunches,
                      hint: t('bizDash.analytics.barHint', { count: a.totalLaunches, members: a.assignedMembers }),
                    }))}
                    onSelect={(d) => navigate(`/products/${d.key}`)} />}
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 1.5 }}>{t('bizDash.analytics.allTimeNote')}</Typography>
            </DashPanel>
          </Grid>
          <Grid size={{ xs: 12, lg: 4 }}>
            <DashPanel id="adoption" title={t('bizDash.adoption.title')} loading={!ready} sx={{ height: '100%' }}>
              {applications.length === 0 ? <PanelEmpty title={t('bizDash.empty.noApps')} /> : (() => {
                const launched = applications.filter((a) => a.totalLaunches > 0)
                const top = [...applications].sort((a, b) => b.totalLaunches - a.totalLaunches)[0]
                const unused = applications.filter((a) => a.totalLaunches === 0)
                const pct = Math.round((launched.length / applications.length) * 100)
                return (
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.75 }}>
                        <Typography variant="body2">{t('bizDash.adoption.rate')}</Typography>
                        <Typography variant="body2" sx={{ fontWeight: 700 }}>{pct}%</Typography>
                      </Box>
                      <MeterBar motion={motion} value={pct} label={t('bizDash.adoption.rate')} />
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('bizDash.adoption.rateHint', { launched: launched.length, total: applications.length })}</Typography>
                    </Box>
                    {top && top.totalLaunches > 0 && (
                      <Box>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('bizDash.adoption.mostUsed')}</Typography>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>{top.productName} · {t('bizDash.adoption.launches', { count: top.totalLaunches })}</Typography>
                      </Box>
                    )}
                    <Box>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('bizDash.adoption.notLaunched')}</Typography>
                      {unused.length === 0
                        ? <Typography variant="body2">{t('bizDash.adoption.allLaunched')}</Typography>
                        : <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mt: 0.5 }}>{unused.map((a) => <Chip key={a.productId} size="small" variant="outlined" label={a.productName} />)}</Box>}
                    </Box>
                  </Box>
                )
              })()}
            </DashPanel>
          </Grid>
        </Grid>
      </Box>

      {/* 11. Organization health */}
      <Grid container spacing={2} component="section" aria-label={t('bizDash.health.label')}>
        <Grid size={{ xs: 12, md: 6, lg: 4 }}>
          <DashPanel id="service-health" title={t('bizDash.health.service')} loading={!ready || status.loading} error={status.error} onRetry={status.reload} sx={{ height: '100%' }}
            action={<PanelLink to="/status">{t('bizDash.health.viewStatus')}</PanelLink>}>
            <Box sx={{ mb: 1.5 }}><StatusDot tone={overall.tone} label={overall.label} /></Box>
            <Box component="ul" sx={{ listStyle: 'none', m: 0, p: 0 }}>
              <HealthRow label={t('bizDash.health.platform')} tone={platformUp ? 'success' : 'error'} status={platformUp ? t('serviceStatus.values.OPERATIONAL') : t('bizDash.health.down')} />
              {status.data?.enabled && purchased.map((p) => (
                <HealthRow key={p.productId} label={p.productName} tone={SERVICE_TONE[p.status]} status={t(`serviceStatus.values.${p.status}`)} />
              ))}
              {(saml.data || oidc.data) && (
                <HealthRow label={t('bizDash.health.federation')} tone={federationOn ? 'success' : 'neutral'}
                  status={federationOn ? t('bizDash.health.connected') : t('bizDash.health.notConfigured')} />
              )}
            </Box>
          </DashPanel>
        </Grid>
        <Grid size={{ xs: 12, md: 6, lg: 4 }}>
          <DashPanel id="seat-utilization" title={t('bizDash.seats.title')} loading={!seats} sx={{ height: '100%' }}
            action={<PanelLink to="/organization/settings#members">{t('bizDash.seats.manage')}</PanelLink>}>
            {seats && (
              <Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', mb: 1 }}>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('bizDash.seats.utilization')}</Typography>
                  <Typography sx={{ fontWeight: 700, fontSize: 22 }}>{Math.round(seats.utilizationPercent)}%</Typography>
                </Box>
                <MeterBar motion={motion} value={seats.utilizationPercent} tone={overLimit ? 'error' : seats.utilizationPercent >= 90 ? 'warning' : 'primary'} label={t('bizDash.seats.utilization')} />
                <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 1, mt: 2 }}>
                  {[
                    [t('bizDash.seats.licensed'), seats.licensedSeats],
                    [t('bizDash.seats.used'), seats.activeMemberCount],
                    [t('bizDash.seats.available'), unusedSeats],
                  ].map(([label, value]) => (
                    <Box key={String(label)}>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>{label}</Typography>
                      <Typography sx={{ fontWeight: 600 }}>{value}</Typography>
                    </Box>
                  ))}
                </Box>
              </Box>
            )}
          </DashPanel>
        </Grid>
        <Grid size={{ xs: 12, lg: 4 }}>
          <DashPanel id="subscription-status" title={t('bizDash.subs.title')} loading={!ready} sx={{ height: '100%' }}>
            {applications.length === 0
              ? <PanelEmpty title={t('bizDash.empty.noApps')} action={<PanelLink to="/products">{t('bizDash.empty.exploreCatalog')}</PanelLink>} />
              : <AnimatedDonut motion={motion} data={donutData} centerValue={String(applications.length)} centerLabel={t('bizDash.subs.apps')}
                  selectedKey={statusFilter} onSelect={(d) => setStatusFilter((cur) => (cur === d.key ? null : d.key as StatusFilter))} />}
          </DashPanel>
        </Grid>
      </Grid>

      {/* 12. Your workspace */}
      <Grid container spacing={2} component="section" aria-label={t('bizDash.workspace.label')}>
        <Grid size={{ xs: 12, md: 6 }}>
          <DashPanel id="recently-used" title={t('bizDash.workspace.recent')} loading={me.loading || !ready} error={me.error} onRetry={me.reload} sx={{ height: '100%' }}>
            {recent.length === 0
              ? <PanelEmpty title={t('bizDash.workspace.noRecent')} action={<PanelLink to="/my/products">{t('bizDash.workspace.explore')}</PanelLink>} />
              : <AppList items={recent} motion={motion} secondary={(p) => t('bizDash.workspace.used', { when: ago(p.lastLaunchedAt!) })}
                  isFavorite={isFavorite} onToggleFavorite={toggleFavorite} onOpen={(p) => launch(p.productId)} />}
          </DashPanel>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <DashPanel id="favorites" title={t('bizDash.workspace.favorites')} loading={me.loading || !ready} error={me.error} onRetry={me.reload} sx={{ height: '100%' }}>
            {favorites.length === 0
              ? <PanelEmpty title={t('bizDash.workspace.noFavorites')} action={<PanelLink to="/my/products">{t('bizDash.workspace.explore')}</PanelLink>} />
              : <AppList items={favorites} motion={motion} secondary={(p) => p.category ?? ''}
                  isFavorite={isFavorite} onToggleFavorite={toggleFavorite} onOpen={(p) => launch(p.productId)} />}
          </DashPanel>
        </Grid>
      </Grid>

      {/* 13. Your account */}
      <DashPanel id="your-account" title={t('bizDash.account.title')} loading={!ready || members.loading} error={members.error} onRetry={members.reload}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap', mb: 2.5 }}>
          <Avatar sx={(theme) => ({ width: 48, height: 48, bgcolor: alpha(theme.palette.primary.main, 0.12), color: 'primary.main', fontWeight: 700 })}>
            {(displayName || '?').charAt(0).toUpperCase()}
          </Avatar>
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Typography sx={{ fontWeight: 600, fontSize: 17 }} noWrap>{displayName}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }} noWrap>{auth.user?.email ?? myMember?.email ?? auth.user?.username}</Typography>
          </Box>
          {myMember && <StatusDot tone={myMember.status === 'ACTIVE' ? 'success' : 'warning'} label={t(`bizDash.account.status.${myMember.status}`)} />}
        </Box>
        <Box sx={{ display: 'grid', gap: 2.5, gridTemplateColumns: { xs: '1fr 1fr', md: 'repeat(4, minmax(0, 1fr))' } }}>
          <Fact label={t('bizDash.account.role')} value={myMember ? t(`bizDash.account.roles.${myMember.orgRole}`) : '—'} />
          <Fact label={t('bizDash.account.mfa')} loading={mfa.loading}
            value={mfa.data ? (mfa.data.enabled ? t('bizDash.account.mfaOn') : t('bizDash.account.mfaOff')) : '—'}
            hint={org?.mfaRequired ? t('bizDash.account.mfaRequired') : undefined} />
          <Fact label={t('bizDash.account.identity')} loading={saml.loading || oidc.loading}
            value={saml.data || oidc.data ? (federationOn ? t('bizDash.account.ssoConnected', { count: providers.filter((p) => p.enabled).length }) : t('bizDash.health.notConfigured')) : '—'} />
          <Fact label={t('bizDash.account.signedIn')} loading={sessions.loading}
            value={currentSession?.startedAt ? formatDateTime(currentSession.startedAt) : '—'} />
        </Box>
        <Box sx={{ mt: 2.5 }}>
          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 0.75 }}>{t('bizDash.account.permissions')}</Typography>
          {permissions.loading ? <Skeleton width="50%" /> : permissions.data && permissions.data.organization.length > 0 ? (
            <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap' }}>
              {permissions.data.organization.slice(0, 10).map((p) => <Chip key={p} size="small" variant="outlined" label={permissionLabel(p)} />)}
              {permissions.data.organization.length > 10 && <Chip size="small" label={t('bizDash.account.more', { count: permissions.data.organization.length - 10 })} />}
            </Box>
          ) : <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('bizDash.account.noPermissions')}</Typography>}
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap', mt: 2.5, pt: 2, borderTop: '1px solid', borderColor: 'divider' }}>
          <PanelLink to="/account/preferences">{t('bizDash.account.preferences')}</PanelLink>
          <PanelLink to="/account/security">{t('bizDash.account.security')}</PanelLink>
        </Box>
      </DashPanel>

      {/* 14. Applications */}
      <DashPanel id="applications" title={t('bizDash.table.title')} loading={!ready} skeletonHeight={200}
        action={filtersActive ? <Button size="small" onClick={clearFilters}>{t('bizDash.table.clear')}</Button> : <PanelLink to="/my/products">{t('bizDash.analytics.viewAll')}</PanelLink>}>
        {filtersActive && (
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 1.5, flexWrap: 'wrap' }}>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('bizDash.table.filtered')}</Typography>
            {appFilter !== '' && <Chip size="small" label={applications.find((a) => a.productId === appFilter)?.productName} onDelete={() => setAppFilter('')} />}
            {statusFilter && <Chip size="small" label={statusFilter === 'ACTIVE' ? t('bizDash.subs.active') : t('bizDash.subs.other')} onDelete={() => setStatusFilter(null)} />}
          </Box>
        )}
        <TableContainer sx={{ overflowX: 'auto' }}>
          <Table size="small" aria-label={t('bizDash.table.title')} sx={{ minWidth: 640 }}>
            <TableHead>
              <TableRow>
                {sortHeader('productName', t('bizDash.table.application'))}
                {sortHeader('subscriptionStatus', t('bizDash.table.status'))}
                {sortHeader('assignedMembers', t('bizDash.table.members'), 'right')}
                {sortHeader('totalLaunches', t('bizDash.table.launches'), 'right')}
                {sortHeader('lastUsedAt', t('bizDash.table.lastUsed'), 'right')}
                <TableCell align="right">{t('bizDash.table.action')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {applications.length === 0 && (
                <TableRow><TableCell colSpan={6}><PanelEmpty title={t('bizDash.empty.noApps')} action={<PanelLink to="/products">{t('bizDash.empty.exploreCatalog')}</PanelLink>} /></TableCell></TableRow>
              )}
              {applications.length > 0 && filtered.length === 0 && (
                <TableRow><TableCell colSpan={6} sx={{ color: 'text.secondary' }}>{t('bizDash.table.noMatch')}</TableCell></TableRow>
              )}
              {filtered.map((app) => <AppRow key={app.productId} app={app} formatDate={formatDate}
                onOpen={() => launch(app.productId)} canLaunch={Boolean(myProducts.get(app.productId)?.launchUrl)} />)}
            </TableBody>
          </Table>
        </TableContainer>
      </DashPanel>

      {/* 15–16. Activity */}
      <Grid container spacing={2} component="section" aria-label={t('bizDash.activity.label')}>
        <Grid size={{ xs: 12, lg: 7 }}>
          <DashPanel id="recent-activity" title={t('bizDash.activity.title')} loading={!ready || audit.loading} error={audit.error} onRetry={audit.reload} sx={{ height: '100%' }}>
            {(audit.data?.items ?? []).length === 0 ? <PanelEmpty title={t('bizDash.activity.empty')} /> : (
              <Box component="ol" sx={{ listStyle: 'none', m: 0, p: 0 }}>
                {audit.data!.items.map((entry, i) => (
                  <Box component="li" key={entry.id} sx={{ display: 'flex', gap: 1.5, py: 1.1, '& + &': { borderTop: '1px solid', borderColor: 'divider' }, ...fadeUp(motion, i, 40) }}>
                    <Box aria-hidden sx={{ width: 8, height: 8, borderRadius: '50%', mt: 0.8, flexShrink: 0, bgcolor: entry.outcome === 'FAILURE' ? 'error.main' : 'primary.main' }} />
                    <Box sx={{ flex: 1, minWidth: 0 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {activityLabel(entry.action, t)}
                        {entry.outcome === 'FAILURE' && <Typography component="span" variant="body2" sx={{ color: 'error.main', fontWeight: 600 }}> · {t('bizDash.activity.failed')}</Typography>}
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }} noWrap>{[entry.actorEmail, entry.detail].filter(Boolean).join(' · ') || '—'}</Typography>
                    </Box>
                    <Tooltip title={formatDateTime(entry.timestamp)}>
                      <Typography variant="caption" sx={{ color: 'text.secondary', whiteSpace: 'nowrap' }}>{ago(entry.timestamp)}</Typography>
                    </Tooltip>
                  </Box>
                ))}
              </Box>
            )}
          </DashPanel>
        </Grid>
        <Grid size={{ xs: 12, lg: 5 }}>
          <DashPanel id="attention" title={t('bizDash.attention.title')} loading={!ready || me.loading} sx={{ height: '100%' }}>
            {attention.length === 0 ? (
              <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', py: 2 }}>
                <Box sx={{ color: 'success.main', display: 'inline-flex' }} aria-hidden><CheckCircle2 size={22} /></Box>
                <Box>
                  <Typography sx={{ fontWeight: 600 }}>{t('bizDash.attention.caughtUp')}</Typography>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('bizDash.attention.caughtUpBody')}</Typography>
                </Box>
              </Box>
            ) : (
              <Box component="ul" sx={{ listStyle: 'none', m: 0, p: 0, display: 'flex', flexDirection: 'column', gap: 1 }}>
                {attention.map((a, i) => {
                  const tone = a.severity === 'error' ? 'error' : a.severity === 'warning' ? 'warning' : 'info'
                  const Icon = tone === 'info' ? Info : AlertTriangle
                  const to = ALERT_ROUTES[a.type]
                  return (
                    <Box component="li" key={`${a.type}-${i}`} sx={(theme) => ({
                      display: 'flex', gap: 1.25, p: 1.5, borderRadius: 2, border: '1px solid',
                      borderColor: alpha(theme.palette[tone].main, 0.35), bgcolor: alpha(theme.palette[tone].main, 0.05), ...fadeUp(motion, i, 50),
                    })}>
                      <Box aria-hidden sx={{ color: `${tone}.main`, display: 'inline-flex', mt: 0.25 }}><Icon size={17} /></Box>
                      <Box sx={{ flex: 1, minWidth: 0 }}>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>{t(`bizDash.attention.types.${a.type}`, { defaultValue: t('bizDash.attention.generic') })}</Typography>
                        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{a.message}</Typography>
                        {to && <Box sx={{ mt: 0.5, ml: -0.75 }}><PanelLink to={to}>{t(`bizDash.attention.actions.${a.type}`, { defaultValue: t('bizDash.attention.review') })}</PanelLink></Box>}
                      </Box>
                    </Box>
                  )
                })}
              </Box>
            )}
          </DashPanel>
        </Grid>
      </Grid>

      {/* 17. Billing summary */}
      <DashPanel id="billing-summary" title={t('bizDash.billing.title')} loading={!ready} action={<PanelLink to="/organization/billing">{t('bizDash.billing.view')}</PanelLink>}>
        <Box sx={{ display: 'grid', gap: 2.5, gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, minmax(0, 1fr))' } }}>
          <Fact label={t('bizDash.billing.activeSubs')} value={String(dashboard?.billing.subscriptions.filter((s) => s.status === 'ACTIVE').length ?? 0)} />
          <Fact label={t('bizDash.billing.nextRenewal')} loading={renewals.loading}
            value={nextRenewal ? `${nextRenewal.productName} · ${formatDate(nextRenewal.renewalDate)}` : t('bizDash.billing.noRenewal')}
            hint={nextRenewal ? (nextRenewal.autoRenew ? t('bizDash.billing.autoRenew') : t('bizDash.billing.manualRenew')) : undefined} />
          <Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>{t('bizDash.billing.spent')}</Typography>
            <Typography sx={{ fontWeight: 600 }}>{spend ? spend.text : t('bizDash.billing.noSpend')}</Typography>
            {spend?.trend && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <Box aria-hidden sx={{ display: 'inline-flex', color: spend.trend.trendPercent >= 0 ? 'success.main' : 'error.main' }}>
                  {spend.trend.trendPercent >= 0 ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
                </Box>
                <Typography variant="caption" sx={{ fontWeight: 600, color: spend.trend.trendPercent >= 0 ? 'success.main' : 'error.main' }}>
                  {Math.abs(spend.trend.trendPercent).toFixed(1)}%
                </Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('bizDash.billing.vsLast')}</Typography>
              </Box>
            )}
          </Box>
        </Box>
      </DashPanel>
    </Box>
  )
}

function HealthRow({ label, status, tone }: { label: string; status: string; tone: Tone }) {
  return (
    <Box component="li" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 1, py: 0.75, '& + &': { borderTop: '1px solid', borderColor: 'divider' } }}>
      <Typography variant="body2" noWrap sx={{ minWidth: 0 }}>{label}</Typography>
      <StatusBadge tone={tone} label={status} />
    </Box>
  )
}

function Fact({ label, value, hint, loading }: { label: string; value: string; hint?: string; loading?: boolean }) {
  return (
    <Box sx={{ minWidth: 0 }}>
      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>{label}</Typography>
      {loading ? <Skeleton width="70%" /> : <Typography sx={{ fontWeight: 600, overflowWrap: 'anywhere' }}>{value}</Typography>}
      {hint && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{hint}</Typography>}
    </Box>
  )
}

function AppList({ items, secondary, isFavorite, onToggleFavorite, onOpen, motion }: {
  items: DashboardProduct[]; secondary: (p: DashboardProduct) => string; motion: boolean
  isFavorite: (p: DashboardProduct) => boolean; onToggleFavorite: (p: DashboardProduct) => void; onOpen: (p: DashboardProduct) => void
}) {
  const { t } = useTranslation()
  return (
    <Box component="ul" sx={{ listStyle: 'none', m: 0, p: 0 }}>
      {items.map((p, i) => {
        const fav = isFavorite(p)
        return (
          <Box component="li" key={p.productId} sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.9, '& + &': { borderTop: '1px solid', borderColor: 'divider' }, ...fadeUp(motion, i, 40) }}>
            <IconButton size="small" onClick={() => onToggleFavorite(p)} aria-pressed={fav}
              aria-label={fav ? t('bizDash.workspace.unfavorite', { name: p.productName }) : t('bizDash.workspace.favorite', { name: p.productName })}
              sx={{ color: fav ? 'warning.main' : 'text.disabled' }}>
              <Star size={16} fill={fav ? 'currentColor' : 'none'} />
            </IconButton>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography component={RouterLink} to={`/products/${p.productId}`} variant="body2"
                sx={{ fontWeight: 600, color: 'text.primary', textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }} noWrap>
                {p.productName}
              </Typography>
              {secondary(p) && <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }} noWrap>{secondary(p)}</Typography>}
            </Box>
            <Button size="small" onClick={() => onOpen(p)} endIcon={p.launchUrl ? <ExternalLink size={13} aria-hidden /> : <ArrowRight size={13} aria-hidden />}
              aria-label={p.launchUrl ? t('bizDash.workspace.openLabel', { name: p.productName }) : t('bizDash.workspace.detailsLabel', { name: p.productName })}>
              {p.launchUrl ? t('bizDash.workspace.open') : t('bizDash.workspace.details')}
            </Button>
          </Box>
        )
      })}
    </Box>
  )
}

function AppRow({ app, formatDate, onOpen, canLaunch }: { app: BusinessApplication; formatDate: (iso?: string | null) => string; onOpen: () => void; canLaunch: boolean }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  return (
    <TableRow hover onClick={() => navigate(`/products/${app.productId}`)} sx={{ cursor: 'pointer' }}>
      <TableCell>
        <Typography component={RouterLink} to={`/products/${app.productId}`} onClick={(e) => e.stopPropagation()} variant="body2"
          sx={{ fontWeight: 600, color: 'text.primary', textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}>
          {app.productName}
        </Typography>
        {app.category && <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>{app.category}</Typography>}
      </TableCell>
      <TableCell>
        {app.subscriptionStatus
          ? <StatusBadge tone={SUB_TONE[app.subscriptionStatus] ?? 'neutral'} label={t(`bizDash.subStatus.${app.subscriptionStatus}`)} />
          : <StatusBadge tone="neutral" label={t('bizDash.subStatus.NONE')} />}
      </TableCell>
      <TableCell align="right">{app.assignedMembers}</TableCell>
      <TableCell align="right">{app.totalLaunches}</TableCell>
      <TableCell align="right">{formatDate(app.lastUsedAt)}</TableCell>
      <TableCell align="right" onClick={(e) => e.stopPropagation()}>
        <Button size="small" onClick={onOpen} endIcon={canLaunch ? <ExternalLink size={13} aria-hidden /> : <ArrowRight size={13} aria-hidden />}
          aria-label={canLaunch ? t('bizDash.workspace.openLabel', { name: app.productName }) : t('bizDash.workspace.detailsLabel', { name: app.productName })}>
          {canLaunch ? t('bizDash.workspace.open') : t('bizDash.workspace.details')}
        </Button>
      </TableCell>
    </TableRow>
  )
}
