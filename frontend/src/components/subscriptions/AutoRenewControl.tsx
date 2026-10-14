import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, FormControlLabel, Switch, Typography,
} from '@mui/material'

/**
 * C100 RR-1 B: the auto-renew switch of one subscription. Turning it off asks first, because the term end then starts
 * a 7-day read-only grace period and the subscription expires after it; turning it on is immediate (during the grace
 * period it renews at once).
 */
export function AutoRenewControl({ id, product, checked, busy = false, disabled = false, onChange }: {
  id: string
  product: string
  checked: boolean
  busy?: boolean
  disabled?: boolean
  onChange: (autoRenew: boolean) => void
}) {
  const { t } = useTranslation()
  const [confirming, setConfirming] = useState(false)
  const hintId = `${id}-hint`
  return (
    <Box>
      <FormControlLabel
        control={(
          <Switch size="small" checked={checked} disabled={busy || disabled}
            onChange={(_, next) => (next ? onChange(true) : setConfirming(true))}
            slotProps={{ input: { 'aria-describedby': hintId } }} />
        )}
        label={<Typography variant="body2" sx={{ fontWeight: 600 }}>{t('subscriptions.autoRenew.label')}</Typography>} />
      <Typography id={hintId} variant="caption" sx={{ display: 'block', color: 'text.secondary', ml: 0.5 }}>
        {checked ? t('subscriptions.autoRenew.onHint') : t('subscriptions.autoRenew.offHint')}
      </Typography>
      <Dialog open={confirming} onClose={() => setConfirming(false)} aria-labelledby={`${id}-off-title`}>
        <DialogTitle id={`${id}-off-title`}>{t('subscriptions.autoRenew.confirmOffTitle', { product })}</DialogTitle>
        <DialogContent><DialogContentText>{t('subscriptions.autoRenew.confirmOffBody', { product })}</DialogContentText></DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirming(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" color="warning" onClick={() => { setConfirming(false); onChange(false) }}>
            {t('subscriptions.autoRenew.confirmOff')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
