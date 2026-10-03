import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, IconButton,
  LinearProgress, Paper, Snackbar, TextField, Typography,
} from '@mui/material'
import { Building2, Minus, Plus, RefreshCw } from 'lucide-react'
import { ApiError } from '../../api/client'
import { organizationApi, type SeatSummary, type Subscription } from '../../api/registrationApi'
import { SettingsSection } from '../settings/SettingsSection'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

interface Row {
  subscription: Subscription
  seats: SeatSummary | null
}

/**
 * REQ-SUB-003 (C63): the organization's subscriptions with their seats, on
 * My subscriptions. Shown only to members with MANAGE_ORGANIZATION (the API
 * refuses everyone else, and the section then stays hidden).
 */
export function OrganizationSubscriptionsSection() {
  const { t } = useTranslation()
  const [rows, setRows] = useState<Row[] | null>(null)
  const [hidden, setHidden] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  const load = useCallback(() => organizationApi.listOrganizationSubscriptions()
    .then((subscriptions) => Promise.all(subscriptions.map((subscription) => (
      subscription.status === 'ACTIVE' || subscription.status === 'SUSPENDED'
        ? organizationApi.seats(subscription.id).catch(() => null)
        : Promise.resolve(null)
    ).then((seats) => ({ subscription, seats })))))
    .then(setRows)
    .catch((e) => {
      if (e instanceof ApiError && (e.status === 403 || e.status === 404)) setHidden(true)
      else setRows([])
    }), [])
  useEffect(() => { void load() }, [load])

  if (hidden || rows === null || rows.length === 0) return null

  return (
    <Box sx={{ mt: 4 }}>
      <SettingsSection id="org-subscriptions" icon={Building2} accent="teal" title={t('subscriptions.seats.sectionTitle')}
        description={t('subscriptions.seats.sectionBody')}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {rows.map((row) => (
            <SeatCard key={row.subscription.id} row={row} onChanged={(seats) => {
              setRows((prev) => prev?.map((r) => r.subscription.id === row.subscription.id
                ? { subscription: { ...r.subscription, quantity: seats.quantity }, seats } : r) ?? prev)
              setToast(t('subscriptions.seats.updated'))
            }} />
          ))}
        </Box>
      </SettingsSection>
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </Box>
  )
}

function SeatCard({ row, onChanged }: { row: Row; onChanged: (seats: SeatSummary) => void }) {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  const { subscription, seats } = row
  const [value, setValue] = useState(String(seats?.quantity ?? subscription.quantity ?? 1))
  const [confirming, setConfirming] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const quantity = Number(value)
  const valid = Number.isInteger(quantity) && seats !== null && quantity >= seats.minimum && quantity <= seats.maximum
  const changed = seats !== null && quantity !== seats.quantity
  const percent = seats && seats.quantity > 0 ? Math.min(100, Math.round((seats.inUse / seats.quantity) * 100)) : 0
  const headingId = `seat-card-${subscription.id}`

  const save = () => {
    setBusy(true)
    setError(null)
    organizationApi.changeSeats(subscription.id, quantity)
      .then((s) => { setConfirming(false); setValue(String(s.quantity)); onChanged(s) })
      .catch((e) => { setConfirming(false); setError(e instanceof ApiError ? e.message : t('subscriptions.seats.error')) })
      .finally(() => setBusy(false))
  }

  return (
    <Paper variant="outlined" component="article" aria-labelledby={headingId} sx={{ p: 2.5, borderRadius: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap', mb: 1.5 }}>
        <Box>
          <Typography id={headingId} component="h3" sx={{ fontWeight: 700 }}>{subscription.productName}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            {t('subscriptions.plan')}: {subscription.planName ?? t('subscriptions.noPlan')}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'flex-start' }}>
          <Chip size="small" color={subscription.status === 'ACTIVE' ? 'success' : subscription.status === 'SUSPENDED' ? 'warning' : 'default'}
            label={t(`subscriptions.status.${subscription.status}`)} />
          {subscription.autoRenew && subscription.expiresAt && (
            <Chip size="small" variant="outlined" color="info" icon={<RefreshCw size={14} />}
              label={t('subscriptions.renewal.autoRenewOn')} />
          )}
          {subscription.expiresAt && (
            <Chip size="small" variant="outlined" label={t('subscriptions.renewal.renewsOn', { date: formatDate(subscription.expiresAt) })} />
          )}
        </Box>
      </Box>

      {seats ? (
        <>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {t('subscriptions.seats.usage', { inUse: seats.inUse, quantity: seats.quantity })}
          </Typography>
          <LinearProgress variant="determinate" value={percent} color={percent >= 90 ? 'warning' : 'primary'}
            aria-label={t('subscriptions.seats.usageLabel', { product: subscription.productName })}
            sx={{ my: 1, height: 8, borderRadius: 4 }} />
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', mt: 1.5 }}>
            <IconButton aria-label={t('subscriptions.seats.remove')} disabled={busy || quantity <= seats.minimum}
              onClick={() => setValue(String(Math.max(seats.minimum, quantity - 1)))}><Minus size={16} /></IconButton>
            <TextField size="small" label={t('subscriptions.seats.label')} value={value} sx={{ width: 120 }}
              onChange={(e) => setValue(e.target.value.replace(/[^0-9]/g, ''))}
              error={value !== '' && !valid}
              slotProps={{ htmlInput: { inputMode: 'numeric', min: seats.minimum, max: seats.maximum } }} />
            <IconButton aria-label={t('subscriptions.seats.add')} disabled={busy || quantity >= seats.maximum}
              onClick={() => setValue(String(Math.min(seats.maximum, (Number.isInteger(quantity) ? quantity : 0) + 1)))}><Plus size={16} /></IconButton>
            <Button variant="contained" disabled={!valid || !changed || busy} onClick={() => setConfirming(true)}>
              {t('subscriptions.seats.save')}
            </Button>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              {t('subscriptions.seats.minimum', { minimum: seats.minimum })}
            </Typography>
          </Box>
        </>
      ) : (
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('subscriptions.seats.notChangeable')}</Typography>
      )}
      {error && <Alert severity="error" sx={{ mt: 1.5 }} onClose={() => setError(null)}>{error}</Alert>}

      <Dialog open={confirming} onClose={() => setConfirming(false)} aria-labelledby={`${headingId}-confirm`}>
        <DialogTitle id={`${headingId}-confirm`}>
          {t('subscriptions.seats.confirmTitle', { from: seats?.quantity ?? 0, to: quantity })}
        </DialogTitle>
        <DialogContent><DialogContentText>{t('subscriptions.seats.confirmBody')}</DialogContentText></DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirming(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" disabled={busy} onClick={save}>{t('subscriptions.seats.confirm')}</Button>
        </DialogActions>
      </Dialog>
    </Paper>
  )
}
