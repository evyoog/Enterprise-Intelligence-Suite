import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Chip, Paper, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { adminPartnersApi, type Provider, type ProviderStatus } from '../../api/partnersApi'
import { PageHeader } from '../../components/layout/PageHeader'

const STATUS_COLOR: Record<ProviderStatus, 'default' | 'info' | 'success' | 'error'> = {
  REGISTERED: 'default',
  VERIFIED: 'info',
  APPROVED: 'info',
  ACTIVE: 'success',
  REJECTED: 'error',
}

/** "/admin/partners" — 14.01 Provider Onboarding (sprint 2027.2.1), MANAGE_PARTNERS. */
export function AdminPartnersPage() {
  const { t } = useTranslation()
  const [providers, setProviders] = useState<Provider[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)

  useEffect(() => {
    adminPartnersApi.listAll()
      .then(setProviders)
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : 'Could not load providers.'))
  }, [])

  return (
    <Box>
      <PageHeader title={t('partners.admin.title')} />
      {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {providers?.length === 0 && <Typography sx={{ color: 'text.secondary' }}>{t('partners.admin.none')}</Typography>}
        {providers?.map((provider) => (
          <Paper
            key={provider.id}
            component={RouterLink}
            to={`/admin/partners/${provider.id}`}
            variant="outlined"
            sx={{ p: 2, display: 'block', textDecoration: 'none', color: 'inherit' }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
              <Box>
                <Typography sx={{ fontWeight: 700 }}>{provider.name}</Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>{provider.contactName} — {provider.contactEmail}</Typography>
              </Box>
              <Chip size="small" color={STATUS_COLOR[provider.status]} label={t(`partners.admin.status.${provider.status}`)} />
            </Box>
          </Paper>
        ))}
      </Box>
    </Box>
  )
}
