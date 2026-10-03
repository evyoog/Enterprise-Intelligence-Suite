import { Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material'
import { useTranslation } from 'react-i18next'

/**
 * C66: confirmation before a destructive or hard-to-undo action (delete,
 * retire). Replaces window.confirm so the prompt is styled, translated and
 * reachable by assistive technology.
 */
export function ConfirmDialog({ open, title, body, confirmLabel, onConfirm, onClose, busy = false, destructive = true }: {
  open: boolean; title: string; body?: string; confirmLabel: string
  onConfirm: () => void; onClose: () => void; busy?: boolean; destructive?: boolean
}) {
  const { t } = useTranslation()
  return (
    <Dialog open={open} onClose={busy ? undefined : onClose} aria-labelledby="confirm-dialog-title" maxWidth="xs" fullWidth>
      <DialogTitle id="confirm-dialog-title">{title}</DialogTitle>
      {body && <DialogContent><DialogContentText>{body}</DialogContentText></DialogContent>}
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" color={destructive ? 'error' : 'primary'} onClick={onConfirm} disabled={busy}
          startIcon={busy ? <CircularProgress size={14} color="inherit" /> : undefined}>
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
