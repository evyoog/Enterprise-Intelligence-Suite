import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import type { PrivilegedAccessRequest } from '../../api/platformPrivilegedAccessApi'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

/**
 * REQ-IAM-004.7 (06.03.01 Revoke access): approved, unexpired grants in one
 * scope, each with an early Revoke. Used by the organization admin's
 * approvals card and the platform admin's privileged-access page; the caller
 * passes the scope's own list/revoke calls. The backend re-checks the
 * approver's authority and refuses anything no longer active.
 */
export function ActiveGrantsList({ load, revoke, refreshKey = 0, headingId }: {
  load: () => Promise<PrivilegedAccessRequest[]>
  revoke: (id: number, note?: string) => Promise<unknown>
  /** Changing it reloads the list (e.g. after an approval elsewhere on the page). */
  refreshKey?: number
  headingId: string
}) {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  const [grants, setGrants] = useState<PrivilegedAccessRequest[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notes, setNotes] = useState<Record<number, string>>({})
  const [busyId, setBusyId] = useState<number | null>(null)

  const reload = useCallback(() => {
    load()
      .then(setGrants)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('privilegedAccess.loadError')))
  }, [load, t])

  useEffect(() => { reload() }, [reload, refreshKey])

  const onRevoke = async (id: number) => {
    setBusyId(id)
    setError(null)
    try {
      await revoke(id, notes[id] || undefined)
      reload()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('privilegedAccess.actionError'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Box component="section" aria-labelledby={headingId}>
      <Typography id={headingId} variant="subtitle2" component="h4" sx={{ fontWeight: 700, mb: 1 }}>
        {t('privilegedAccess.activeTitle')}
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      {grants?.length === 0 && (
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('privilegedAccess.noActive')}</Typography>
      )}
      {grants?.map((grant) => {
        const who = grant.requesterEmail ?? t('privilegedAccess.unknownRequester')
        return (
          <Box key={grant.id} sx={{ borderTop: 1, borderColor: 'divider', pt: 1.5, mt: 1.5 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1, flexWrap: 'wrap' }}>
              <Box>
                <Typography sx={{ fontWeight: 700 }}>{grant.permissionName}</Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                  {t('privilegedAccess.requestedBy', { email: who })}
                  {grant.expiresAt && <> · {t('privilegedAccess.expiresOn', { date: formatDate(grant.expiresAt) })}</>}
                </Typography>
              </Box>
              <Chip size="small" color="success" label={grant.effectiveStatus} />
            </Box>
            <Typography variant="body2" sx={{ my: 1 }}>{grant.justification}</Typography>
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
              <TextField
                size="small"
                label={t('privilegedAccess.note')}
                value={notes[grant.id] ?? ''}
                onChange={(e) => setNotes((prev) => ({ ...prev, [grant.id]: e.target.value }))}
                sx={{ flex: 1, minWidth: 200 }}
              />
              <Button
                size="small"
                variant="outlined"
                color="error"
                disabled={busyId === grant.id}
                aria-label={t('privilegedAccess.revokeGrant', { permission: grant.permissionName, email: who })}
                onClick={() => onRevoke(grant.id)}
              >
                {t('privilegedAccess.revoke')}
              </Button>
            </Box>
          </Box>
        )
      })}
    </Box>
  )
}
