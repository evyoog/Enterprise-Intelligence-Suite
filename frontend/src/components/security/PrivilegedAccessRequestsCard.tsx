import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, MenuItem, Paper, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import type { PrivilegedAccessRequest } from '../../api/platformPrivilegedAccessApi'
import { myPrivilegedAccessApi, type RequestablePermission } from '../../api/privilegedAccessApi'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

/**
 * REQ-IAM-004 (sprint 2026.3.3, 06.03.01 Request elevated access, Revoke access):
 * request temporary access and see / withdraw your own requests. The permission
 * dropdown comes from the backend (decision C24) and all validation — duration
 * 1–480 minutes, no MANAGE_PRIVILEGED_ACCESS, organization membership — stays on
 * the backend; its messages are shown as returned.
 */
export function PrivilegedAccessRequestsCard() {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  const [requestable, setRequestable] = useState<RequestablePermission[] | null>(null)
  const [requests, setRequests] = useState<PrivilegedAccessRequest[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [permissionName, setPermissionName] = useState('')
  const [justification, setJustification] = useState('')
  const [durationMinutes, setDurationMinutes] = useState('60')
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)

  const loadRequests = useCallback(() => {
    myPrivilegedAccessApi.listMine()
      .then(setRequests)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('privilegedAccess.loadError')))
  }, [t])

  useEffect(() => {
    myPrivilegedAccessApi.listRequestable()
      .then(setRequestable)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('privilegedAccess.loadError')))
    loadRequests()
  }, [loadRequests, t])

  const submit = async () => {
    setFormError(null)
    setSubmitting(true)
    try {
      await myPrivilegedAccessApi.request({ permissionName, justification, durationMinutes: Number(durationMinutes) })
      setPermissionName('')
      setJustification('')
      loadRequests()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('privilegedAccess.actionError'))
    } finally {
      setSubmitting(false)
    }
  }

  const withdraw = async (id: number) => {
    setBusyId(id)
    setError(null)
    try {
      await myPrivilegedAccessApi.withdraw(id)
      loadRequests()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('privilegedAccess.actionError'))
    } finally {
      setBusyId(null)
    }
  }

  // The backend is the final check; this only avoids offering Withdraw where it would certainly fail.
  const canWithdraw = (r: PrivilegedAccessRequest) => r.effectiveStatus === 'PENDING' || r.effectiveStatus === 'APPROVED'

  return (
    <Paper variant="outlined" sx={{ p: 3, mt: 3 }} component="section" aria-labelledby="pam-request-title">
      <Typography id="pam-request-title" variant="h6" component="h2" sx={{ fontWeight: 700 }}>
        {t('privilegedAccess.title')}
      </Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('privilegedAccess.subtitle')}</Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {requestable?.length === 0 && (
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('privilegedAccess.noRequestable')}</Typography>
      )}

      {requestable && requestable.length > 0 && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mb: 3 }}>
          {formError && <Alert severity="error">{formError}</Alert>}
          <TextField select required label={t('privilegedAccess.permission')} value={permissionName} onChange={(e) => setPermissionName(e.target.value)}>
            {requestable.map((p) => (
              <MenuItem key={p.permissionName} value={p.permissionName}>
                {p.permissionName} ({t(`privilegedAccess.scope.${p.scope}`)})
              </MenuItem>
            ))}
          </TextField>
          <TextField required multiline minRows={2} label={t('privilegedAccess.justification')} value={justification} onChange={(e) => setJustification(e.target.value)} />
          <TextField
            required type="number" label={t('privilegedAccess.duration')} helperText={t('privilegedAccess.durationHint')}
            value={durationMinutes} onChange={(e) => setDurationMinutes(e.target.value)}
            slotProps={{ htmlInput: { min: 1, max: 480 } }}
          />
          <Box>
            <Button variant="contained" disabled={submitting || !permissionName || !justification || !durationMinutes} onClick={submit}>
              {submitting ? t('privilegedAccess.submitting') : t('privilegedAccess.submit')}
            </Button>
          </Box>
        </Box>
      )}

      <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 700, mb: 1 }}>{t('privilegedAccess.myRequests')}</Typography>
      {requests?.length === 0 && (
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('privilegedAccess.noRequests')}</Typography>
      )}
      {requests?.map((request) => (
        <Box key={request.id} sx={{ borderTop: 1, borderColor: 'divider', py: 1.5, display: 'flex', justifyContent: 'space-between', gap: 2, alignItems: 'flex-start' }}>
          <Box>
            <Typography sx={{ fontWeight: 700 }}>{request.permissionName}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>
              {t('privilegedAccess.requestedOn', { date: formatDate(request.requestedAt), minutes: request.requestedDurationMinutes })}
              {request.expiresAt && ` · ${t('privilegedAccess.expiresOn', { date: formatDate(request.expiresAt) })}`}
            </Typography>
            {request.decisionNote && (
              <Typography variant="body2">{t('privilegedAccess.decisionNote', { note: request.decisionNote })}</Typography>
            )}
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
            <Chip size="small" label={request.effectiveStatus} />
            {canWithdraw(request) && (
              <Button size="small" variant="outlined" disabled={busyId === request.id} onClick={() => withdraw(request.id)}>
                {t('privilegedAccess.withdraw')}
              </Button>
            )}
          </Box>
        </Box>
      ))}
    </Paper>
  )
}
