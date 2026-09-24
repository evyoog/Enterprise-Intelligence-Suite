import { useEffect, useState } from 'react'
import {
  Alert, Box, Button, Chip, CircularProgress, Container, Grid, LinearProgress,
  Paper, Table, TableBody, TableCell, TableHead, TableRow, Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { businessDashboardApi, type BusinessDashboard } from '../api/businessDashboardApi'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { PageHeader } from '../components/layout/PageHeader'
import { useLocalePreference } from '../theming/LocalePreferenceProvider'

function StatTile({ label, value, sub }: { label: string; value: string; sub?: string }) {
  return (
    <Paper variant="outlined" sx={{ p: 2.5, flex: 1, minWidth: 160 }}>
      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{label}</Typography>
      <Typography variant="h4" sx={{ fontWeight: 700, mt: 0.5 }}>{value}</Typography>
      {sub && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{sub}</Typography>}
    </Paper>
  )
}

function SectionTitle({ children }: { children: React.ReactNode }) {
  return <Typography variant="h6" sx={{ fontWeight: 700, mb: 1.5, mt: 4 }}>{children}</Typography>
}

/**
 * "/organization/business-dashboard" — Phase 19: Business, Usage, Billing,
 * Service Health, Support, and Alerts, all in one place. Billing/Service
 * Health/Support sections show an explicit note wherever the backend has no
 * real data source for something (no payment gateway, no per-product
 * monitoring, no ticketing-by-customer API) rather than inventing numbers —
 * see BusinessDashboardService's own javadoc.
 */
export function BusinessDashboardPage() {
  const navigate = useNavigate()
  const [dashboard, setDashboard] = useState<BusinessDashboard | null>(null)
  const [error, setError] = useState<string | null>(null)
  const { formatDate: formatDateInTimeZone } = useLocalePreference()
  const formatDate = (iso?: string) => iso ? formatDateInTimeZone(iso) : '—'

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
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container component="main" id="main-content" maxWidth="lg" sx={{ pt: '112px', pb: 8 }}>
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
            <Paper variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Application</TableCell>
                    <TableCell>Subscription</TableCell>
                    <TableCell align="right">Assigned members</TableCell>
                    <TableCell align="right">Total launches</TableCell>
                    <TableCell align="right">Last used</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {dashboard.applications.length === 0 && (
                    <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>No applications assigned yet.</TableCell></TableRow>
                  )}
                  {dashboard.applications.map((app) => (
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
          </>
        )}
      </Container>
    </Box>
  )
}
