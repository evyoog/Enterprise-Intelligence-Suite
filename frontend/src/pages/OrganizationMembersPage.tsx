import { useEffect, useState } from 'react'
import { UsersRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Box, Tab, Tabs } from '@mui/material'
import { myPermissionsApi } from '../api/myPermissionsApi'
import { PageHeader } from '../components/layout/PageHeader'
import { useTabParam } from '../components/layout/useTabParam'
import { OrganizationGroupsCard } from '../components/organization/OrganizationGroupsCard'
import { OrganizationInvitationsCard } from '../components/organization/OrganizationInvitationsCard'
import { OrganizationMembersCard } from '../components/organization/OrganizationMembersCard'
import { OrganizationStructurePanel } from '../components/organization/OrganizationStructurePanel'

const TABS = ['members', 'groups', 'invitations', 'structure'] as const
type Tab = (typeof TABS)[number]
/** The permission each tab needs; the backend enforces it, the tab is only hidden here. */
const NEEDS: Record<Tab, string> = {
  members: 'MANAGE_USERS', groups: 'MANAGE_USERS', invitations: 'INVITE_USERS', structure: 'MANAGE_ORGANIZATION',
}

/**
 * "/organization/members" — C80: members and their roles (REQ-IAM-002), groups (REQ-TEN-003), the
 * invitations (REQ-TEN-008, C84) and the organization structure (REQ-TEN-006, C83), as tabs. Each tab is
 * shown to people who hold its permission, so a member allowed only to invite sees just Invitations.
 */
export function OrganizationMembersPage() {
  const { t } = useTranslation()
  const [permissions, setPermissions] = useState<string[] | null>(null)
  useEffect(() => {
    let alive = true
    myPermissionsApi.get().then((p) => { if (alive) setPermissions(p.organization) }).catch(() => { if (alive) setPermissions([]) })
    return () => { alive = false }
  }, [])
  const allowed = permissions === null ? TABS : TABS.filter((k) => permissions.includes(NEEDS[k]))
  const [wanted, setTab] = useTabParam(TABS, 'members')
  const tab: Tab = allowed.includes(wanted) ? wanted : (allowed[0] ?? 'members')
  return (
    <Box sx={{ pb: 4 }}>
      <PageHeader icon={UsersRound} area="organization" title={t('orgMembers.title')} subtitle={t('orgMembers.subtitle')} />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('orgMembers.title')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        {allowed.map((k) => <Tab key={k} value={k} label={t(`orgMembers.tabs.${k}`)} />)}
      </Tabs>
      {tab === 'members' && <OrganizationMembersCard />}
      {tab === 'groups' && <OrganizationGroupsCard />}
      {tab === 'invitations' && <OrganizationInvitationsCard canInviteAdmin={permissions?.includes('MANAGE_USERS') ?? false} />}
      {tab === 'structure' && <OrganizationStructurePanel />}
    </Box>
  )
}
