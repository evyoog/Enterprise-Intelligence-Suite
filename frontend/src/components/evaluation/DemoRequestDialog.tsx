import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { evaluationApi } from '../../api/evaluationApi'
import type { SupportTicket } from '../../api/supportApi'

/** Backend limit (DemoRequestService): each free-text field is at most 1000 characters. */
export const DEMO_FIELD_MAX = 1000

/**
 * REQ-MKT-005 (C102): "Request a demo" — a short form that opens a support ticket in the category "Demo request".
 * After sending it shows the ticket number and a link to My tickets.
 */
export function DemoRequestDialog({ open, productId, productName, onClose }: {
  open: boolean
  productId: number
  productName: string
  onClose: () => void
}) {
  const { t } = useTranslation()
  const [preferredTime, setPreferredTime] = useState('')
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [ticket, setTicket] = useState<SupportTicket | null>(null)

  const timeTooLong = preferredTime.length > DEMO_FIELD_MAX
  const messageTooLong = message.length > DEMO_FIELD_MAX
  const tooLong = t('evaluation.demo.tooLong', { max: DEMO_FIELD_MAX })

  const submit = async () => {
    if (timeTooLong || messageTooLong) return
    setSending(true)
    setError(null)
    try {
      setTicket(await evaluationApi.requestDemo({
        productId,
        preferredTime: preferredTime.trim() || undefined,
        message: message.trim() || undefined,
      }))
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('evaluation.demo.error'))
    } finally {
      setSending(false)
    }
  }

  // After a sent request the next opening starts with an empty form.
  const close = () => {
    onClose()
    if (ticket) { setTicket(null); setPreferredTime(''); setMessage('') }
  }

  return (
    <Dialog open={open} onClose={sending ? undefined : close} fullWidth maxWidth="sm" aria-labelledby="demo-request-title">
      <DialogTitle id="demo-request-title">{t('evaluation.demo.dialogTitle', { product: productName })}</DialogTitle>
      {ticket ? (
        <>
          <DialogContent>
            <Alert severity="success" role="status">
              <Typography sx={{ fontWeight: 600 }}>{t('evaluation.demo.successTitle')}</Typography>
              {t('evaluation.demo.success', { id: ticket.id })}
            </Alert>
          </DialogContent>
          <DialogActions>
            <Button component={RouterLink} to="/support/tickets" variant="contained">{t('evaluation.demo.viewTickets')}</Button>
            <Button onClick={close}>{t('evaluation.demo.close')}</Button>
          </DialogActions>
        </>
      ) : (
        <>
          <DialogContent>
            <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('evaluation.demo.intro')}</Typography>
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <TextField label={t('evaluation.demo.preferredTime')} fullWidth autoFocus value={preferredTime}
                onChange={(e) => setPreferredTime(e.target.value)} error={timeTooLong}
                helperText={timeTooLong ? tooLong : t('evaluation.demo.preferredTimeHint')} />
              <TextField label={t('evaluation.demo.message')} fullWidth multiline minRows={3} value={message}
                onChange={(e) => setMessage(e.target.value)} error={messageTooLong}
                helperText={messageTooLong ? tooLong : t('evaluation.demo.messageHint')} />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={close} disabled={sending}>{t('evaluation.demo.cancel')}</Button>
            <Button variant="contained" onClick={submit} disabled={sending || timeTooLong || messageTooLong}>
              {sending ? t('evaluation.demo.sending') : t('evaluation.demo.submit')}
            </Button>
          </DialogActions>
        </>
      )}
    </Dialog>
  )
}
