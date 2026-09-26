import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField,
} from '@mui/material'
import { ApiError } from '../../api/client'

/**
 * C30 (2026-09-26, 06.01.02 Recover MFA): confirm resetting another user's
 * two-factor authentication. With `name` it confirms a known user (the
 * organization admin's members card); without it, it asks for an email
 * (the platform admin's registrations page). Refusals — your own account,
 * nothing to reset, another organization — come from the backend as shown.
 */
export function ResetMfaDialog({ open, name, onConfirm, onClose, onDone }: {
  open: boolean
  /** The user being reset; omit to ask for an email instead. */
  name?: string
  onConfirm: (email?: string) => Promise<unknown>
  onClose: () => void
  onDone: (who: string) => void
}) {
  if (!open) return null
  return <ResetMfaForm key={name ?? 'by-email'} name={name} onConfirm={onConfirm} onClose={onClose} onDone={onDone} />
}

function ResetMfaForm({ name, onConfirm, onClose, onDone }: {
  name?: string
  onConfirm: (email?: string) => Promise<unknown>
  onClose: () => void
  onDone: (who: string) => void
}) {
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [working, setWorking] = useState(false)
  const byEmail = name === undefined

  const confirm = async () => {
    setWorking(true)
    setError(null)
    try {
      await onConfirm(byEmail ? email.trim() : undefined)
      onDone(byEmail ? email.trim() : name!)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('mfaReset.error'))
    } finally {
      setWorking(false)
    }
  }

  return (
    <Dialog open onClose={working ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{byEmail ? t('mfaReset.titleByEmail') : t('mfaReset.title', { name })}</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>{t('mfaReset.body')}</DialogContentText>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {byEmail && (
          <TextField
            fullWidth
            size="small"
            type="email"
            label={t('mfaReset.email')}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoFocus
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={working}>{t('mfaReset.cancel')}</Button>
        <Button variant="contained" color="error" onClick={confirm} disabled={working || (byEmail && !email.trim())}>
          {t('mfaReset.confirm')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
