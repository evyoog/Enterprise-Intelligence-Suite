import { Tab, Tabs } from '@mui/material'
import { useTranslation } from 'react-i18next'
import { useTabParam } from '../../components/layout/useTabParam'
import { AdminSlaSettingsPage } from './AdminSlaSettingsPage'
import { AdminSupportTicketsPage } from './AdminSupportTicketsPage'

const TABS = ['tickets', 'sla'] as const

/** "/admin/support/tickets" — MANAGE_SUPPORT_TICKETS: the tickets list and (C101) the SLA targets, business hours and alert list. */
export function AdminSupportRoutePage() {
  const { t } = useTranslation()
  const [tab, setTab] = useTabParam(TABS, 'tickets')
  return (
    <>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('appShell.nav.support')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="tickets" label={t('supportTabs.tickets')} />
        <Tab value="sla" label={t('supportTabs.sla')} />
      </Tabs>
      {tab === 'tickets' ? <AdminSupportTicketsPage /> : <AdminSlaSettingsPage />}
    </>
  )
}
