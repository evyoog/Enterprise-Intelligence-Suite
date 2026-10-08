import { KeyRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Box } from '@mui/material'
import { PageHeader } from '../components/layout/PageHeader'
import { OrganizationPrivilegedAccessCard } from '../components/organization/OrganizationPrivilegedAccessCard'

/** "/organization/privileged-access" — C80: the organization's privileged-access approvals (REQ-IAM-004), on their own page. */
export function OrganizationPrivilegedAccessPage() {
  const { t } = useTranslation()
  return (
    <Box sx={{ pb: 4 }}>
      <PageHeader icon={KeyRound} area="organization" title={t('orgPrivileged.title')} subtitle={t('orgPrivileged.subtitle')} />
      <OrganizationPrivilegedAccessCard />
    </Box>
  )
}
