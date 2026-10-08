import { Box, Tab, Tabs } from '@mui/material'
import { useTranslation } from 'react-i18next'
import { useTabParam } from '../../components/layout/useTabParam'
import { CommonSettingsPage } from './settings/CommonSettingsPage'
import { PlatformsListPage } from './PlatformsListPage'

const TABS = ['products', 'configuration'] as const

/**
 * Platform → Products (C80): the products (platforms) and, on the
 * Configuration tab, the platform-wide settings that used to be
 * Settings → Common: languages, currencies, regions and feature flags.
 */
export function ProductsAdminPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useTabParam(TABS, 'products')
  return (
    <>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('appShell.nav.platforms')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="products" label={t('productsTabs.products')} />
        <Tab value="configuration" label={t('productsTabs.configuration')} />
      </Tabs>
      {tab === 'products' ? <PlatformsListPage /> : <Box><CommonSettingsPage embedded /></Box>}
    </>
  )
}
