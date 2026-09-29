import { useTranslation } from 'react-i18next'
import { Chip } from '@mui/material'
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
