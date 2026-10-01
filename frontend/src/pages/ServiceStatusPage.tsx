import { Activity as PHActivity } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, CircularProgress, Paper, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import { ApiError } from '../api/client'
import { serviceStatusApi, type ServiceStatusPage as StatusPage } from '../api/serviceStatusApi'
import { PageHeader } from '../components/layout/PageHeader'
import { ServiceStatusChip } from '../components/status/ServiceStatusChip'
import { useLocalePreference } from '../theming/LocalePreferenceProvider'

/**
 * "/status" — REQ-PRT-001.3 (decisions C20, C26). Every signed-in customer
 * sees every product's status; incident details only for products their
 * organization or account has purchased (the backend already filters them).
 * Posted manually by the platform team until Health Monitoring and Incident
 * Management replace it (C20).
 */
export function ServiceStatusPage() {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  const [page, setPage] = useState<StatusPage | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    serviceStatusApi.get()
      .then(setPage)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('serviceStatus.loadError')))
  }, [t])

  return (
    <Box sx={{ pb: 4 }}>
      <PageHeader icon={PHActivity} accent="emerald" title={t('serviceStatus.title')} subtitle={t('serviceStatus.subtitle')} />
      {error && <Alert severity="error">{error}</Alert>}
      {!error && page === null && <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>}
      {page && !page.enabled && <Alert severity="info">{t('serviceStatus.disabled')}</Alert>}

      {page?.enabled && (
        <>
          <Paper variant="outlined" sx={{ mb: 3, overflowX: 'auto' }}>
            <Table size="small" aria-label={t('serviceStatus.productsLabel')}>
              <TableHead>
                <TableRow>
                  <TableCell>{t('serviceStatus.product')}</TableCell>
                  <TableCell>{t('serviceStatus.status')}</TableCell>
                  <TableCell>{t('serviceStatus.details')}</TableCell>
                  <TableCell>{t('serviceStatus.updated')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {page.products.length === 0 && (
                  <TableRow><TableCell colSpan={4} sx={{ color: 'text.secondary' }}>{t('serviceStatus.noProducts')}</TableCell></TableRow>
                )}
                {page.products.map((p) => (
                  <TableRow key={p.productId}>
                    <TableCell sx={{ fontWeight: 600 }}>{p.productName}</TableCell>
                    <TableCell><ServiceStatusChip status={p.status} /></TableCell>
                    <TableCell sx={{ color: 'text.secondary' }}>
                      {p.note}
                      {p.purchased && p.openIncidents > 0 && (
                        <>{p.note ? ' · ' : ''}{t('serviceStatus.openIncidents', { count: p.openIncidents })}</>
                      )}
                      {!p.purchased && p.openIncidents > 0 && (
                        <>{p.note ? ' · ' : ''}{t('serviceStatus.notPurchased')}</>
                      )}
                    </TableCell>
                    <TableCell sx={{ color: 'text.secondary', whiteSpace: 'nowrap' }}>{p.updatedAt ? formatDate(p.updatedAt) : '—'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>

          <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('serviceStatus.incidentsTitle')}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('serviceStatus.incidentsHint')}</Typography>
          {page.incidents.length === 0 && <Typography sx={{ color: 'text.secondary' }}>{t('serviceStatus.noIncidents')}</Typography>}
          <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {page.incidents.map((i) => (
              <Paper component="li" key={i.id} variant="outlined" sx={{ p: 2, borderLeft: 4, borderLeftColor: i.open ? 'warning.main' : 'success.main' }}>
                <Typography sx={{ fontWeight: 700 }}>{i.productName} — {i.title}</Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary', mb: 0.5 }}>
                  {i.open
                    ? t('serviceStatus.openSince', { date: formatDate(i.startedAt) })
                    : t('serviceStatus.resolvedRange', { start: formatDate(i.startedAt), end: formatDate(i.endedAt!) })}
                </Typography>
                <Typography variant="body2">{i.message}</Typography>
              </Paper>
            ))}
          </Box>
        </>
      )}
    </Box>
  )
}
