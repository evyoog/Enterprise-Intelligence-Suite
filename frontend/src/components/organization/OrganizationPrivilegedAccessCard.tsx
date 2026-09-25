import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, Paper, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import type { PrivilegedAccessRequest } from '../../api/platformPrivilegedAccessApi'
import { organizationPrivilegedAccessApi } from '../../api/privilegedAccessApi'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

type Decision = 'approve' | 'reject' | 'revoke'

/**
 * REQ-IAM-004 (sprint 2026.3.3, 06.03.01 Approve / Revoke access): pending
 * ORGANIZATION-scope requests of the caller's own organization, reviewed the
 * same way AdminPrivilegedAccessPage reviews PLATFORM-scope ones. Needs standing
 * MANAGE_PRIVILEGED_ACCESS (enforced server-side); hidden on a 403/404. The
 * backend refuses self-approval and already-decided requests — its message is
 * shown as returned.
 */
export function OrganizationPrivilegedAccessCard() {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  const [requests, setRequests] = useState<PrivilegedAccessRequest[] | null>(null)
  const [hidden, setHidden] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notes, setNotes] = useState<Record<number, string>>({})
  const [busyId, setBusyId] = useState<number | null>(null)

  const load = useCallback(() => {
    organizationPrivilegedAccessApi.listPending()
      .then(setRequests)
      .catch((e) => {
        if (e instanceof ApiError && (e.status === 403 || e.status === 404)) setHidden(true)
        else setError(e instanceof ApiError ? e.message : t('privilegedAccess.loadError'))
      })
  }, [t])

  useEffect(load, [load])

  const decide = async (id: number, decision: Decision) => {
    setBusyId(id)
    setError(null)
    try {
      await organizationPrivilegedAccessApi[decision](id, notes[id] || undefined)
      load()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('privilegedAccess.actionError'))
    } finally {
      setBusyId(null)
    }
  }

  if (hidden) return null

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }} component="section" aria-labelledby="org-pam-title">
      <Typography id="org-pam-title" variant="subtitle1" component="h3" sx={{ fontWeight: 700, mb: 1 }}>
        {t('orgSettings.approvalsTitle')}
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      {requests?.length === 0 && (
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('orgSettings.noPending')}</Typography>
      )}
      {requests?.map((request) => (
        <Box key={request.id} sx={{ borderTop: 1, borderColor: 'divider', pt: 1.5, mt: 1.5 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1 }}>
            <Box>
              <Typography sx={{ fontWeight: 700 }}>{request.permissionName}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                {t('privilegedAccess.requestedOn', { date: formatDate(request.requestedAt), minutes: request.requestedDurationMinutes })}
              </Typography>
            </Box>
            <Chip size="small" label={request.effectiveStatus} />
          </Box>
          <Typography variant="body2" sx={{ my: 1 }}>{request.justification}</Typography>
          <TextField
            size="small"
            fullWidth
            label={t('privilegedAccess.note')}
            value={notes[request.id] ?? ''}
            onChange={(e) => setNotes((prev) => ({ ...prev, [request.id]: e.target.value }))}
            sx={{ mb: 1 }}
          />
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button size="small" variant="contained" disabled={busyId === request.id} onClick={() => decide(request.id, 'approve')}>
              {t('privilegedAccess.approve')}
            </Button>
            <Button size="small" variant="outlined" color="error" disabled={busyId === request.id} onClick={() => decide(request.id, 'reject')}>
              {t('privilegedAccess.reject')}
            </Button>
            <Button size="small" variant="text" disabled={busyId === request.id} onClick={() => decide(request.id, 'revoke')}>
              {t('privilegedAccess.revoke')}
            </Button>
          </Box>
        </Box>
      ))}
    </Paper>
  )
}
