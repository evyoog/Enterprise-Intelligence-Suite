import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Grid, LinearProgress,
  Paper, Table, TableBody, TableCell, TableHead, TableRow, TableSortLabel, Typography, useTheme,
} from '@mui/material'
import { ArrowDownRight, ArrowUpRight } from 'lucide-react'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { businessDashboardApi, type BusinessDashboard } from '../api/businessDashboardApi'
import { BarList } from '../components/charts/BarList'
import { DonutChart } from '../components/charts/DonutChart'
import { PageHeader } from '../components/layout/PageHeader'
import { OrganizationMfaPolicyCard } from '../components/organization/OrganizationMfaPolicyCard'
import { OrganizationGroupsCard } from '../components/organization/OrganizationGroupsCard'
import { OrganizationMembersCard } from '../components/organization/OrganizationMembersCard'
import { OrganizationPrivilegedAccessCard } from '../components/organization/OrganizationPrivilegedAccessCard'
import { useLocalePreference } from '../theming/LocalePreferenceProvider'

function StatTile({ label, value, sub, trendPercent, action }: {
  label: string; value: string; sub?: string; trendPercent?: number; action?: React.ReactNode
}) {
  const theme = useTheme()
  const trendColor = trendPercent !== undefined && trendPercent >= 0 ? theme.palette.success.main : theme.palette.error.main
  return (
    <Paper variant="outlined" sx={{ p: 2.5, flex: 1, minWidth: 160 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{label}</Typography>
        {action}
      </Box>
      <Typography variant="h4" sx={{ fontWeight: 700, mt: 0.5 }}>{value}</Typography>
      {trendPercent !== undefined && (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.5 }}>
          {trendPercent >= 0 ? <ArrowUpRight size={14} color={trendColor} /> : <ArrowDownRight size={14} color={trendColor} />}
          <Typography variant="caption" sx={{ color: trendPercent >= 0 ? 'success.main' : 'error.main', fontWeight: 600 }}>
            {Math.abs(trendPercent).toFixed(1)}%
          </Typography>
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>vs last period</Typography>
        </Box>
      )}
      {sub && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{sub}</Typography>}
    </Paper>
  )
}

function SectionTitle({ children }: { children: React.ReactNode }) {
  return <Typography variant="h6" sx={{ fontWeight: 700, mb: 1.5, mt: 4 }}>{children}</Typography>
}

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

/**
 * "/organization/business-dashboard" — Phase 19: Business, Usage, Billing,
 * Service Health, Support, and Alerts, all in one place. Billing/Service
 * Health/Support sections show an explicit note wherever the backend has no
 * real data source for something (no payment gateway, no per-product
 * monitoring, no ticketing-by-customer API) rather than inventing numbers —
 * see BusinessDashboardService's own javadoc.
 */
type SortKey = 'productName' | 'subscriptionStatus' | 'assignedMembers' | 'totalLaunches' | 'lastUsedAt'

export function BusinessDashboardPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [dashboard, setDashboard] = useState<BusinessDashboard | null>(null)
  const [error, setError] = useState<string | null>(null)
  const { formatDate: formatDateInTimeZone } = useLocalePreference()
  const formatDate = (iso?: string) => iso ? formatDateInTimeZone(iso) : '—'

  // C54: click-to-filter on the donut/bar charts, sortable table — all
  // client-side over the applications list the page already has.
  const [subscriptionFilter, setSubscriptionFilter] = useState<'ACTIVE' | 'OTHER' | null>(null)
  const [productFilter, setProductFilter] = useState<string | null>(null)
  const [sortKey, setSortKey] = useState<SortKey>('productName')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc')

  const toggleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortKey(key)
      setSortDir('asc')
    }
  }

  const filteredApplications = useMemo(() => {
    if (!dashboard) return []
    let rows = dashboard.applications
    if (subscriptionFilter) {
      rows = rows.filter((a) => (subscriptionFilter === 'ACTIVE' ? a.subscriptionStatus === 'ACTIVE' : a.subscriptionStatus !== 'ACTIVE'))
    }
    if (productFilter) {
      rows = rows.filter((a) => a.productName === productFilter)
    }
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
  }, [dashboard, subscriptionFilter, productFilter, sortKey, sortDir])

  const clearFilters = () => { setSubscriptionFilter(null); setProductFilter(null) }

  useEffect(() => {
    businessDashboardApi.get()
      .then(setDashboard)
      .catch((e) => {
        // This dashboard is ORG_ADMIN-only (MANAGE_ORGANIZATION, enforced
        // server-side in BusinessDashboardService) — landing here as a
        // regular org member (403 — a member without MANAGE_ORGANIZATION) or
        // as an individual with no organization at all (404 — see
        // OrganizationSelfService#resolveMembership's own "not a member of
        // an organization" case) isn't an error to show, just the wrong
        // dashboard for this account, so send them to their own personal one
        // instead of stalling on a permission error. This is also what makes
        // it safe for the navbar's own workspace link (Phase 7) to always
        // point here regardless of which of the three shapes an account is —
        // this page sorts it out rather than the navbar needing to know.
        if (e instanceof ApiError && (e.status === 403 || e.status === 404)) {
          navigate('/my/products', { replace: true })
          return
        }
        setError(e instanceof ApiError ? e.message : 'Could not load the business dashboard.')
      })
  }, [navigate])

  return (
    <Box>
      {/* C54: full width — the app shell's main content area already has
          no cap of its own; this page no longer adds one. */}
      <Box sx={{ pb: 4 }}>
        {error && <Alert severity="error">{error}</Alert>}

        {!error && !dashboard && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
        )}

        {dashboard && (
          <>
            <PageHeader
              title={dashboard.organization.name}
              subtitle="Business dashboard"
              action={<Button variant="outlined" onClick={() => navigate('/organization/identity-federation')}>Identity Federation</Button>}
            />

            {dashboard.alerts.length > 0 && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mt: 2 }}>
                {dashboard.alerts.map((alert, i) => (
                  <Alert key={i} severity={alert.severity as 'info' | 'warning' | 'error'}>{alert.message}</Alert>
                ))}
              </Box>
            )}

            <SectionTitle>Seat usage</SectionTitle>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 1.5 }}>
              <StatTile label="Licensed seats" value={String(dashboard.seatUsage.licensedSeats)} />
              <StatTile label="Active members" value={String(dashboard.seatUsage.activeMemberCount)} />
              <StatTile label="Utilization" value={`${dashboard.seatUsage.utilizationPercent.toFixed(0)}%`} />
            </Box>
            <LinearProgress
              variant="determinate"
              value={Math.min(100, dashboard.seatUsage.utilizationPercent)}
              sx={{ height: 8, borderRadius: 999 }}
            />

            <SectionTitle>Applications &amp; usage</SectionTitle>
            <Grid container spacing={2} sx={{ mb: 2 }}>
              <Grid size={{ xs: 12, sm: 7 }}>
                <Paper variant="outlined" sx={{ p: 2.5, height: '100%' }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 2 }}>Launches by application</Typography>
                  <BarList
                    items={dashboard.applications
                      .map((a) => ({ label: a.productName, value: a.totalLaunches }))
                      .sort((a, b) => b.value - a.value)}
                    selectedLabel={productFilter}
                    onItemClick={(item) => setProductFilter((current) => (current === item.label ? null : item.label))}
                  />
                </Paper>
              </Grid>
              <Grid size={{ xs: 12, sm: 5 }}>
                <Paper variant="outlined" sx={{ p: 2.5, height: '100%' }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 2 }}>Applications by subscription</Typography>
                  <DonutChart
                    centerValue={String(dashboard.applications.length)}
                    centerLabel="applications"
                    selectedLabel={subscriptionFilter === 'ACTIVE' ? 'Active subscription' : subscriptionFilter === 'OTHER' ? 'Not subscribed / inactive' : null}
                    onSegmentClick={(segment) => {
                      const value = segment.label === 'Active subscription' ? 'ACTIVE' : 'OTHER'
                      setSubscriptionFilter((current) => (current === value ? null : value))
                    }}
                    segments={[
                      {
                        label: 'Active subscription',
                        value: dashboard.applications.filter((a) => a.subscriptionStatus === 'ACTIVE').length,
                        tone: 'success',
                      },
                      {
                        label: 'Not subscribed / inactive',
                        value: dashboard.applications.filter((a) => a.subscriptionStatus !== 'ACTIVE').length,
                        tone: 'neutral',
                      },
                    ]}
                  />
                </Paper>
              </Grid>
            </Grid>
            {(subscriptionFilter || productFilter) && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>Filtered:</Typography>
                {subscriptionFilter && (
                  <Chip size="small" label={subscriptionFilter === 'ACTIVE' ? 'Active subscription' : 'Not subscribed / inactive'} onDelete={() => setSubscriptionFilter(null)} />
                )}
                {productFilter && <Chip size="small" label={productFilter} onDelete={() => setProductFilter(null)} />}
                <Button size="small" onClick={clearFilters}>Clear filter</Button>
              </Box>
            )}
            <Paper variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sortDirection={sortKey === 'productName' ? sortDir : false}>
                      <TableSortLabel active={sortKey === 'productName'} direction={sortKey === 'productName' ? sortDir : 'asc'} onClick={() => toggleSort('productName')}>
                        Application
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sortDirection={sortKey === 'subscriptionStatus' ? sortDir : false}>
                      <TableSortLabel active={sortKey === 'subscriptionStatus'} direction={sortKey === 'subscriptionStatus' ? sortDir : 'asc'} onClick={() => toggleSort('subscriptionStatus')}>
                        Subscription
                      </TableSortLabel>
                    </TableCell>
                    <TableCell align="right" sortDirection={sortKey === 'assignedMembers' ? sortDir : false}>
                      <TableSortLabel active={sortKey === 'assignedMembers'} direction={sortKey === 'assignedMembers' ? sortDir : 'asc'} onClick={() => toggleSort('assignedMembers')}>
                        Assigned members
                      </TableSortLabel>
                    </TableCell>
                    <TableCell align="right" sortDirection={sortKey === 'totalLaunches' ? sortDir : false}>
                      <TableSortLabel active={sortKey === 'totalLaunches'} direction={sortKey === 'totalLaunches' ? sortDir : 'asc'} onClick={() => toggleSort('totalLaunches')}>
                        Total launches
                      </TableSortLabel>
                    </TableCell>
                    <TableCell align="right" sortDirection={sortKey === 'lastUsedAt' ? sortDir : false}>
                      <TableSortLabel active={sortKey === 'lastUsedAt'} direction={sortKey === 'lastUsedAt' ? sortDir : 'asc'} onClick={() => toggleSort('lastUsedAt')}>
                        Last used
                      </TableSortLabel>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {dashboard.applications.length === 0 && (
                    <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>No applications assigned yet.</TableCell></TableRow>
                  )}
                  {dashboard.applications.length > 0 && filteredApplications.length === 0 && (
                    <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>No applications match this filter.</TableCell></TableRow>
                  )}
                  {filteredApplications.map((app) => (
                    <TableRow key={app.productId}>
                      <TableCell>{app.productName}</TableCell>
                      <TableCell>
                        {app.subscriptionStatus
                          ? <Chip size="small" label={app.subscriptionStatus} color={app.subscriptionStatus === 'ACTIVE' ? 'success' : 'default'} />
                          : <Chip size="small" variant="outlined" label="Not subscribed" />}
                      </TableCell>
                      <TableCell align="right">{app.assignedMembers}</TableCell>
                      <TableCell align="right">{app.totalLaunches}</TableCell>
                      <TableCell align="right">{formatDate(app.lastUsedAt)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>

            <SectionTitle>Billing</SectionTitle>
            <Alert severity="info" sx={{ mb: 1.5 }}>{dashboard.billing.note}</Alert>
            {Object.keys(dashboard.billing.spentThisPeriodByCurrency).length > 0 && (() => {
              const trend = spendTrend(dashboard.billing.spentThisPeriodByCurrency, dashboard.billing.spentLastPeriodByCurrency)
              return (
                <Box sx={{ display: 'flex', gap: 2, mb: 1.5, flexWrap: 'wrap' }}>
                  <StatTile
                    label="Spent this period"
                    value={Object.entries(dashboard.billing.spentThisPeriodByCurrency)
                      .map(([cur, amt]) => `${(amt / 100).toFixed(2)} ${cur}`).join(', ')}
                    trendPercent={trend?.trendPercent}
                    action={<Button component={RouterLink} to="/billing" size="small">View Report</Button>}
                  />
                </Box>
              )
            })()}
            <Paper variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Product</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Started</TableCell>
                    <TableCell>Expires</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {dashboard.billing.subscriptions.length === 0 && (
                    <TableRow><TableCell colSpan={4} sx={{ color: 'text.secondary' }}>No subscriptions yet.</TableCell></TableRow>
                  )}
                  {dashboard.billing.subscriptions.map((sub) => (
                    <TableRow key={sub.id}>
                      <TableCell>{sub.productName}</TableCell>
                      <TableCell><Chip size="small" label={sub.status} /></TableCell>
                      <TableCell>{formatDate(sub.startedAt)}</TableCell>
                      <TableCell>{formatDate(sub.expiresAt)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>

            <Grid container spacing={2} sx={{ mt: 0.5 }}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <SectionTitle>Service health</SectionTitle>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Chip
                    label={dashboard.serviceHealth.platformStatus}
                    color={dashboard.serviceHealth.platformStatus === 'UP' ? 'success' : 'error'}
                    sx={{ mb: 1 }}
                  />
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>{dashboard.serviceHealth.note}</Typography>
                  {/* REQ-PRT-001 (C26): per-product status and incidents. */}
                  <Button component={RouterLink} to="/status" size="small" sx={{ mt: 1, px: 0 }}>
                    {t('serviceStatus.link')}
                  </Button>
                </Paper>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <SectionTitle>Support</SectionTitle>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Chip
                    label={dashboard.support.available ? 'Available' : 'Not available'}
                    color={dashboard.support.available ? 'success' : 'default'}
                    sx={{ mb: 1 }}
                  />
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>{dashboard.support.note}</Typography>
                </Paper>
              </Grid>
            </Grid>

            {/* Sprint 2026.3.3: REQ-IAM-001 (MFA policy), REQ-IAM-002 (member roles),
                REQ-IAM-004 (organization privileged-access approvals). Each card
                loads its own data and hides itself if the backend refuses. */}
            <SectionTitle>{t('orgSettings.title')}</SectionTitle>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <OrganizationMfaPolicyCard />
              <OrganizationMembersCard />
              <OrganizationGroupsCard />
              <OrganizationPrivilegedAccessCard />
            </Box>
          </>
        )}
      </Box>
    </Box>
  )
}
