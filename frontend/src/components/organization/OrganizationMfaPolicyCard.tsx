import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, FormControlLabel, Paper, Switch, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { organizationApi } from '../../api/registrationApi'

/**
 * REQ-IAM-001 (sprint 2026.3.3, 06.02.02): the organization MFA policy toggle.
 * The current value comes from GET /organization/me; changing it needs
 * MANAGE_ORGANIZATION, which the backend enforces. Hidden on a 403/404, the same
 * way BusinessDashboardPage treats an account without organization management.
 */
export function OrganizationMfaPolicyCard() {
  const { t } = useTranslation()
  const [mfaRequired, setMfaRequired] = useState<boolean | null>(null)
  const [hidden, setHidden] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    organizationApi.getMyOrganization()
      .then((org) => setMfaRequired(org.mfaRequired))
      .catch((e) => {
        if (e instanceof ApiError && (e.status === 403 || e.status === 404)) setHidden(true)
        else setError(e instanceof ApiError ? e.message : t('orgSettings.loadError'))
      })
  }, [t])

  const toggle = async (next: boolean) => {
    setSaving(true)
    setError(null)
    try {
      const org = await organizationApi.updateMfaPolicy(next)
      setMfaRequired(org.mfaRequired)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('orgSettings.mfaSaveError'))
    } finally {
      setSaving(false)
    }
  }

  if (hidden) return null

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }} component="section" aria-labelledby="org-mfa-title">
      <Typography id="org-mfa-title" variant="subtitle1" component="h3" sx={{ fontWeight: 700, mb: 1 }}>
        {t('orgSettings.mfaTitle')}
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      {mfaRequired !== null && (
        <>
          <FormControlLabel
            control={<Switch checked={mfaRequired} disabled={saving} onChange={(e) => toggle(e.target.checked)} />}
            label={t('orgSettings.mfaRequire')}
          />
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
            {mfaRequired ? t('orgSettings.mfaRequired') : t('orgSettings.mfaNotRequired')} {t('orgSettings.mfaHint')}
          </Typography>
        </>
      )}
    </Paper>
  )
}
