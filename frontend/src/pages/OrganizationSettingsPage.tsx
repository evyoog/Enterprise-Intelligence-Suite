import { Settings2 } from 'lucide-react'
import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Box } from '@mui/material'
import { useLocation } from 'react-router-dom'
import { PageHeader } from '../components/layout/PageHeader'
import { OrganizationMfaPolicyCard } from '../components/organization/OrganizationMfaPolicyCard'
import { OrganizationGroupsCard } from '../components/organization/OrganizationGroupsCard'
import { OrganizationMembersCard } from '../components/organization/OrganizationMembersCard'
import { OrganizationPrivilegedAccessCard } from '../components/organization/OrganizationPrivilegedAccessCard'

/**
 * "/organization/settings" — C69: the organization administration cards
 * that used to sit at the bottom of the business dashboard (REQ-IAM-001 MFA
 * policy, REQ-IAM-002 members and roles, REQ-TEN-003 groups, REQ-IAM-004
 * privileged-access approvals), unchanged, on their own page. Each card
 * loads its own data and hides itself if the backend refuses. `#members`,
 * `#groups`, `#mfa` and `#privileged-access` jump to a card.
 */
export function OrganizationSettingsPage() {
  const { t } = useTranslation()
  const { hash } = useLocation()

  useEffect(() => {
    if (!hash) return
    const timer = setTimeout(() => document.getElementById(hash.slice(1))?.scrollIntoView({ block: 'start' }), 300)
    return () => clearTimeout(timer)
  }, [hash])

  return (
    <Box sx={{ pb: 4 }}>
      <PageHeader icon={Settings2} area="organization" title={t('orgSettings.title')} subtitle={t('orgSettings.pageSubtitle')} />
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
        <Box id="members" sx={{ scrollMarginTop: 88 }}><OrganizationMembersCard /></Box>
        <Box id="groups" sx={{ scrollMarginTop: 88 }}><OrganizationGroupsCard /></Box>
        <Box id="mfa" sx={{ scrollMarginTop: 88 }}><OrganizationMfaPolicyCard /></Box>
        <Box id="privileged-access" sx={{ scrollMarginTop: 88 }}><OrganizationPrivilegedAccessCard /></Box>
      </Box>
    </Box>
  )
}
