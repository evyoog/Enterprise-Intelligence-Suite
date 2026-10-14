import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, Paper, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material'
import { Server } from 'lucide-react'
import { ApiError } from '../api/client'
import { organizationServiceInstancesApi, type ServiceInstance } from '../api/serviceInstancesApi'
import { PageHeader } from '../components/layout/PageHeader'
import { InstanceHealthChip, InstanceStatusChip } from '../components/status/ServiceStatusChip'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { useLocalePreference } from '../theming/LocalePreferenceProvider'

/**
 * "/organization/service-instances" — REQ-SRM-001 (decision C101). Read-only: the service instances of the
 * signed-in organization (one per active subscription) and their health. The backend returns only this organization's.
 */
export function OrganizationServiceInstancesPage() {
  const { t } = useTranslation()
  const { formatDateTime } = useLocalePreference()
  const [instances, setInstances] = useState<ServiceInstance[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    organizationServiceInstancesApi.list().then((rows) => { setInstances(rows); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('serviceInstances.org.loadError')))
  }, [t])
  useEffect(() => { load() }, [load])

  return (
    <>
      <PageHeader icon={Server} area="organization" title={t('serviceInstances.org.title')} subtitle={t('serviceInstances.org.subtitle')} />

      {error && <ErrorState title={t('serviceInstances.org.loadError')} message={error} onRetry={load} />}
      {!error && instances === null && <Skeleton variant="rounded" height={160} />}
      {instances?.length === 0 && <EmptyState icon={Server} title={t('serviceInstances.org.empty.title')} description={t('serviceInstances.org.empty.body')} />}
      {instances && instances.length > 0 && (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small" aria-label={t('serviceInstances.org.title')}>
              <TableHead>
                <TableRow>
                  <TableCell>{t('serviceInstances.columns.product')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.status')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.health')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.lastReport')}</TableCell>
                  <TableCell>{t('serviceInstances.org.accessUrl')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {instances.map((i) => (
                  <TableRow key={i.id}>
                    <TableCell sx={{ fontWeight: 600 }}>{i.productName}</TableCell>
                    <TableCell><InstanceStatusChip status={i.status} /></TableCell>
                    <TableCell>
                      <InstanceHealthChip health={i.health} stale={i.stale} />
                      {i.healthMessage && <Typography variant="caption" component="div" sx={{ color: 'text.secondary' }}>{i.healthMessage}</Typography>}
                    </TableCell>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{i.lastReportedAt ? formatDateTime(i.lastReportedAt) : t('serviceInstances.never')}</TableCell>
                    <TableCell>
                      {i.accessUrl
                        ? <Link href={i.accessUrl} target="_blank" rel="noopener noreferrer">{i.accessUrl}</Link>
                        : '—'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}
    </>
  )
}
