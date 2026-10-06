import { Alert, Box, Button, Tab, Tabs } from '@mui/material'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { useNavAccess } from '../../components/layout/appShellContext'
import { useTabParam } from '../../components/layout/useTabParam'
import { PermissionsAdminPage } from './PermissionsAdminPage'
import { RolesAdminPage } from './RolesAdminPage'

/**
 * Organization → Roles & permissions (C80): roles, permissions and where roles
 * are assigned, as one feature with three tabs. A tab shows only when its
 * permission is held (MANAGE_ROLES, MANAGE_PERMISSIONS); the backend still
 * decides every call.
 */
export function RolesPermissionsPage() {
  const { t } = useTranslation()
  const { permissions } = useNavAccess()
  const holds = (name: string) => permissions === null || permissions.platform.includes(name)
  const tabs = [
    ...(holds('MANAGE_ROLES') ? (['roles'] as const) : []),
    ...(holds('MANAGE_PERMISSIONS') ? (['permissions'] as const) : []),
    'assignments' as const,
  ]
  const [tab, setTab] = useTabParam(tabs, tabs[0])
  return (
    <>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('appShell.nav.rolesPermissions')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        {tabs.map((k) => <Tab key={k} value={k} label={t(`rolesTabs.${k}`)} />)}
      </Tabs>
      {tab === 'roles' && <RolesAdminPage />}
      {tab === 'permissions' && <PermissionsAdminPage />}
      {tab === 'assignments' && (
        <Box sx={{ maxWidth: 720 }}>
          <Alert severity="info" sx={{ mb: 2 }}>{t('rolesTabs.assignmentsHelp')}</Alert>
          <Button component={RouterLink} to="/organization/members" variant="outlined">{t('rolesTabs.goToMembers')}</Button>
        </Box>
      )}
    </>
  )
}
