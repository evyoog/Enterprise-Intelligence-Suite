import { Tab, Tabs } from '@mui/material'
import { useTranslation } from 'react-i18next'
import { useNavAccess } from '../components/layout/appShellContext'
import { useTabParam } from '../components/layout/useTabParam'
import { AdminIncidentsPage } from './admin/AdminIncidentsPage'
import { AdminProblemsPage } from './admin/AdminProblemsPage'
import { AdminServiceInstancesPage } from './admin/AdminServiceInstancesPage'
import { ServiceStatusAdminPage } from './admin/ServiceStatusAdminPage'
import { ServiceStatusPage } from './ServiceStatusPage'

const TABS = ['status', 'manage', 'instances', 'incidents', 'problems'] as const

/**
 * Platform → Service status (C80), the one status entry. Everyone sees the
 * status; people with MANAGE_SERVICE_STATUS also get a Manage tab (what used
 * to be "/admin/service-status") to edit component status, and (C101) the Instances, Incidents and Problems tabs.
 */
export function ServiceStatusRoutePage() {
  const { t } = useTranslation()
  const { isAdmin, permissions } = useNavAccess()
  const canManage = isAdmin && (permissions === null || permissions.platform.includes('MANAGE_SERVICE_STATUS'))
  const [tab, setTab] = useTabParam(TABS, 'status')
  if (!canManage) return <ServiceStatusPage />
  return (
    <>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('appShell.nav.serviceStatus')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="status" label={t('serviceTabs.status')} />
        <Tab value="manage" label={t('serviceTabs.manage')} />
        <Tab value="instances" label={t('serviceTabs.instances')} />
        <Tab value="incidents" label={t('serviceTabs.incidents')} />
        <Tab value="problems" label={t('serviceTabs.problems')} />
      </Tabs>
      {tab === 'status' && <ServiceStatusPage />}
      {tab === 'manage' && <ServiceStatusAdminPage />}
      {tab === 'instances' && <AdminServiceInstancesPage />}
      {tab === 'incidents' && <AdminIncidentsPage />}
      {tab === 'problems' && <AdminProblemsPage />}
    </>
  )
}
