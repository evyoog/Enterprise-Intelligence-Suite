import { useTranslation } from 'react-i18next'
import { Chip } from '@mui/material'
import type { InstanceHealth, InstanceStatus } from '../../api/serviceInstancesApi'
import type { ServiceStatusValue } from '../../api/serviceStatusApi'

const COLOR: Record<ServiceStatusValue, 'success' | 'warning' | 'error' | 'info'> = {
  OPERATIONAL: 'success',
  DEGRADED: 'warning',
  PARTIAL_OUTAGE: 'warning',
  MAJOR_OUTAGE: 'error',
  MAINTENANCE: 'info',
}

/** REQ-PRT-001: one status value, colour-coded, text from i18n. */
export function ServiceStatusChip({ status }: { status: ServiceStatusValue }) {
  const { t } = useTranslation()
  return <Chip size="small" color={COLOR[status]} label={t(`serviceStatus.values.${status}`)} />
}

const INSTANCE_STATUS_COLOR: Record<InstanceStatus, 'success' | 'warning' | 'error' | 'info' | 'default'> = {
  PROVISIONING: 'info',
  ACTIVE: 'success',
  SUSPENDED: 'warning',
  FAILED: 'error',
  DEPROVISIONED: 'default',
}

const INSTANCE_HEALTH_COLOR: Record<InstanceHealth, 'success' | 'warning' | 'error' | 'default'> = {
  HEALTHY: 'success',
  DEGRADED: 'warning',
  DOWN: 'error',
  UNKNOWN: 'default',
}

/** REQ-SRM-001 (C101): the lifecycle status of a service instance. */
export function InstanceStatusChip({ status }: { status: InstanceStatus }) {
  const { t } = useTranslation()
  return <Chip size="small" color={INSTANCE_STATUS_COLOR[status]} variant={status === 'DEPROVISIONED' ? 'outlined' : 'filled'}
    label={t(`serviceInstances.status.${status}`)} />
}

/** REQ-SRM-001 (C101): the health of a service instance; "No recent report" when the last report is stale. */
export function InstanceHealthChip({ health, stale }: { health: InstanceHealth; stale: boolean }) {
  const { t } = useTranslation()
  if (stale) return <Chip size="small" variant="outlined" label={t('serviceInstances.noRecentReport')} />
  return <Chip size="small" color={INSTANCE_HEALTH_COLOR[health]} label={t(`serviceInstances.health.${health}`)} />
}
