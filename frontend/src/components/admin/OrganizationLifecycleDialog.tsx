import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from '@mui/material'
import {
  adminRegistrationApi,
  type OrganizationAdmin,
  type OrganizationLifecycleAction,
  type OrganizationLifecycleResult,
} from '../../api/adminRegistrationApi'
import { ApiError } from '../../api/client'

export interface LifecycleTarget {
  organization: OrganizationAdmin
  action: OrganizationLifecycleAction
}

/**
 * REQ-TEN-001 (05.01.01.03-05): confirm a suspend / activate / close, with an
 * optional reason that is written to the audit log. Refusals (e.g. a closed
 * organization cannot be suspended, an admin cannot suspend their own
 * organization) come from the backend and are shown as returned.
 */
export function OrganizationLifecycleDialog({ target, onClose, onDone }: {
  target: LifecycleTarget | null
  onClose: () => void
  onDone: (result: OrganizationLifecycleResult, action: OrganizationLifecycleAction) => void
}) {
  if (!target) return null
  // Keyed so the reason and error start empty for every action.
  return <ConfirmLifecycle key={`${target.organization.id}-${target.action}`} target={target} onClose={onClose} onDone={onDone} />
}

function ConfirmLifecycle({ target, onClose, onDone }: {
  target: LifecycleTarget
  onClose: () => void
  onDone: (result: OrganizationLifecycleResult, action: OrganizationLifecycleAction) => void
}) {
  const { t } = useTranslation()
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [working, setWorking] = useState(false)
  const { organization, action } = target

  const confirm = async () => {
    setWorking(true)
    setError(null)
    try {
      onDone(await adminRegistrationApi.changeOrganizationLifecycle(organization.id, action, reason), action)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('adminOrgLifecycle.actionError'))
    } finally {
      setWorking(false)
    }
  }

  return (
    <Dialog open onClose={working ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{t(`adminOrgLifecycle.${action}.title`, { name: organization.name })}</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>{t(`adminOrgLifecycle.${action}.body`)}</DialogContentText>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <TextField
          fullWidth
          size="small"
          label={t('adminOrgLifecycle.reason')}
          helperText={t('adminOrgLifecycle.reasonHint')}
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          slotProps={{ htmlInput: { maxLength: 500 } }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={working}>{t('adminOrgLifecycle.cancel')}</Button>
        <Button
          variant="contained"
          color={action === 'activate' ? 'primary' : 'error'}
          onClick={confirm}
          disabled={working}
        >
          {t(`adminOrgLifecycle.${action}.confirm`)}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
