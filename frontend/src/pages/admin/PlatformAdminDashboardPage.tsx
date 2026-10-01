import { LayoutDashboard as PHLayoutDashboard } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Alert, Box, Button, Chip, CircularProgress, Grid, Paper, Typography, useTheme } from '@mui/material'
import { ArrowDownRight, ArrowUpRight } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { platformDashboardApi, type PlatformDashboard } from '../../api/platformDashboardApi'
import { BarList } from '../../components/charts/BarList'
import { DonutChart, type DonutSegment } from '../../components/charts/DonutChart'
import { PageHeader } from '../../components/layout/PageHeader'

function StatTile({ label, value, sub, trendPercent, action }: {
  label: string; value: string; sub?: string; trendPercent?: number; action?: React.ReactNode
}) {
  const theme = useTheme()
  const trendColor = trendPercent !== undefined && trendPercent >= 0 ? theme.palette.success.main : theme.palette.error.main
  return (
    <Paper variant="outlined" sx={{ p: 2.5, flex: 1, minWidth: 180 }}>
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

function revenueTrend(dashboard: PlatformDashboard) {
  const currencies = new Set([
    ...Object.keys(dashboard.billing.revenueThisPeriodByCurrency),
    ...Object.keys(dashboard.billing.revenueLastPeriodByCurrency),
  ])
  if (currencies.size !== 1) return null
  const currency = [...currencies][0]
  const last = dashboard.billing.revenueLastPeriodByCurrency[currency] ?? 0
  const current = dashboard.billing.revenueThisPeriodByCurrency[currency] ?? 0
  if (last === 0) return null
  return { currency, current, trendPercent: ((current - last) / last) * 100 }
}

/**
 * "/admin/dashboard" (C53) — a platform admin's own overview: the same
 * "only real numbers, an honest note instead of a fabricated one" rule
 * PlatformDashboardService's own javadoc states. Every figure here comes
 * straight from PlatformDashboardDto — nothing is computed or guessed on
 * this page.
 */
export function PlatformAdminDashboardPage() {
  const [dashboard, setDashboard] = useState<PlatformDashboard | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    platformDashboardApi.get()
      .then(setDashboard)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load the platform dashboard.'))
  }, [])

  if (error) {
    return <Alert severity="error">{error}</Alert>
  }
  if (!dashboard) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
  }

  const trend = revenueTrend(dashboard)
  const revenueValue = Object.entries(dashboard.billing.revenueThisPeriodByCurrency)
    .map(([cur, amt]) => `${(amt / 100).toFixed(2)} ${cur}`).join(', ') || '0.00'

  const subscriptionSegments: DonutSegment[] = [
    { label: 'Active', value: dashboard.subscriptions.byStatus.ACTIVE ?? 0, tone: 'success' as const },
    { label: 'Suspended', value: dashboard.subscriptions.byStatus.SUSPENDED ?? 0, tone: 'warning' as const },
    { label: 'Cancelled', value: dashboard.subscriptions.byStatus.CANCELLED ?? 0, tone: 'neutral' as const },
    { label: 'Expired', value: dashboard.subscriptions.byStatus.EXPIRED ?? 0, tone: 'error' as const },
  ].filter((s) => s.value > 0)
  const totalSubscriptions = subscriptionSegments.reduce((sum, s) => sum + s.value, 0)

  return (
    <Box>
      <PageHeader icon={PHLayoutDashboard} accent="violet" area="administration" title="Platform dashboard" subtitle="Platform-wide overview, real time" />

      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mt: 2 }}>
        <StatTile
          label="Organizations"
          value={String(dashboard.organizations.total)}
          sub={`${dashboard.organizations.active} active`}
        />
        <StatTile
          label="Products"
          value={String(dashboard.catalog.totalProducts)}
          sub={`${dashboard.catalog.activeProducts} published · ${dashboard.catalog.totalPlatforms} platforms`}
        />
        <StatTile
          label="Active subscriptions"
          value={String(dashboard.subscriptions.active)}
        />
        <StatTile
          label="Revenue this period"
          value={revenueValue}
          trendPercent={trend?.trendPercent}
          action={<Button component={RouterLink} to="/admin/billing" size="small">View Report</Button>}
        />
      </Box>
      {Object.keys(dashboard.billing.revenueThisPeriodByCurrency).length === 0 && (
        <Alert severity="info" sx={{ mt: 2 }}>{dashboard.billing.note}</Alert>
      )}

      <Grid container spacing={2} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, sm: 7 }}>
          <SectionTitle>Most-launched products</SectionTitle>
          <Paper variant="outlined" sx={{ p: 2.5 }}>
            <BarList
              items={dashboard.topProductsByLaunches.map((p) => ({ label: p.productName, value: p.totalLaunches }))}
            />
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 5 }}>
          <SectionTitle>Subscriptions by status</SectionTitle>
          <Paper variant="outlined" sx={{ p: 2.5 }}>
            {totalSubscriptions > 0
              ? <DonutChart centerValue={String(totalSubscriptions)} centerLabel="subscriptions" segments={subscriptionSegments} />
              : <Typography variant="body2" sx={{ color: 'text.secondary' }}>No subscriptions yet.</Typography>}
          </Paper>
        </Grid>
      </Grid>

      <Grid container spacing={2} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, sm: 6 }}>
          <SectionTitle>Support &amp; reviews</SectionTitle>
          <Paper variant="outlined" sx={{ p: 2.5, display: 'flex', gap: 3 }}>
            <Box>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>Open tickets</Typography>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>{dashboard.openSupportTicketCount}</Typography>
            </Box>
            <Box>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>Pending reviews</Typography>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>{dashboard.pendingReviewCount}</Typography>
            </Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <SectionTitle>Service health</SectionTitle>
          <Paper variant="outlined" sx={{ p: 2.5 }}>
            <Chip
              label={dashboard.serviceHealth.platformStatus}
              color={dashboard.serviceHealth.platformStatus === 'UP' ? 'success' : 'error'}
              sx={{ mb: 1 }}
            />
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{dashboard.serviceHealth.note}</Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  )
}
