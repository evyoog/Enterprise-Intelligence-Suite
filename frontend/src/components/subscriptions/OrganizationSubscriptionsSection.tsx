import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, IconButton,
  LinearProgress, Link, Paper, TextField, Typography,
} from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { Building2, Minus, Plus, RefreshCw } from 'lucide-react'
import { ApiError } from '../../api/client'
import { organizationApi, type SeatSummary, type Subscription } from '../../api/registrationApi'
import { myPermissionsApi } from '../../api/myPermissionsApi'
import { SettingsSection } from '../settings/SettingsSection'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'
import { formatMoney } from '../../utils/money'
import { isInGrace } from '../../utils/grace'
import { AutoRenewControl } from './AutoRenewControl'
import { GraceNotice, ReadOnlyChip } from './GraceNotice'
import { RenewalRecipientsSection } from './RenewalRecipientsSection'

interface Row {
  subscription: Subscription
  seats: SeatSummary | null
}

/**
 * REQ-SUB-003 (C63, C100 SE-1/SE-2, RR-1, RR-5): the organization's subscriptions with their seats and auto-renew, and
 * who gets the renewal reminders, on My subscriptions. The API lists them for Manage billing or Manage subscriptions
 * (and refuses everyone else, so the section stays hidden); changing seats and choosing reminder recipients need
 * Manage billing (MANAGE_ORGANIZATION_BILLING), so those controls are hidden from the others.
 */
export function OrganizationSubscriptionsSection() {
  const { t } = useTranslation()
  const [rows, setRows] = useState<Row[] | null>(null)
  const [hidden, setHidden] = useState(false)
  const [permissions, setPermissions] = useState<string[] | null>(null)

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
  useEffect(() => {
    let alive = true
    myPermissionsApi.get().then((p) => { if (alive) setPermissions(p.organization) }).catch(() => { if (alive) setPermissions([]) })
    return () => { alive = false }
  }, [])
  const canManageBilling = permissions?.includes('MANAGE_ORGANIZATION_BILLING') ?? false

  if (hidden || rows === null || rows.length === 0) return null

  return (
    <Box sx={{ mt: 4 }}>
      <SettingsSection id="org-subscriptions" icon={Building2} accent="teal" title={t('subscriptions.seats.sectionTitle')}
        description={t('subscriptions.seats.sectionBody')}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {rows.map((row) => (
            <SeatCard key={row.subscription.id} row={row} canChangeSeats={canManageBilling}
              onChanged={(seats) => setRows((prev) => prev?.map((r) => r.subscription.id === row.subscription.id
                ? { subscription: { ...r.subscription, quantity: seats.quantity, pendingQuantity: seats.pendingQuantity ?? null }, seats } : r) ?? prev)}
              onSubscription={(updated) => setRows((prev) => prev?.map((r) => r.subscription.id === updated.id
                ? { ...r, subscription: updated } : r) ?? prev)} />
          ))}
        </Box>
      </SettingsSection>
      {canManageBilling && <RenewalRecipientsSection />}
    </Box>
  )
}

function SeatCard({ row, canChangeSeats, onChanged, onSubscription }: {
  row: Row
  canChangeSeats: boolean
  onChanged: (seats: SeatSummary) => void
  onSubscription: (subscription: Subscription) => void
}) {
  const { t, i18n } = useTranslation()
  const { formatDate } = useLocalePreference()
  const { subscription, seats } = row
  const [value, setValue] = useState(String(seats?.quantity ?? subscription.quantity ?? 1))
  const [confirming, setConfirming] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<{ text: string; invoice: boolean } | null>(null)
  const [autoRenewBusy, setAutoRenewBusy] = useState(false)

  // C100 RR-1 B: the term ended with auto-renew off, so the subscription is read-only until the grace period ends.
  const graceEndsAt = subscription.graceEndsAt ?? null
  const inGrace = graceEndsAt !== null && isInGrace(graceEndsAt)

  const quantity = Number(value)
  const valid = Number.isInteger(quantity) && seats !== null && quantity >= seats.minimum && quantity <= seats.maximum
  const changed = seats !== null && quantity !== seats.quantity
  const percent = seats && seats.quantity > 0 ? Math.min(100, Math.round((seats.inUse / seats.quantity) * 100)) : 0
  const headingId = `seat-card-${subscription.id}`
  const renewsAt = seats?.renewsAt ?? subscription.expiresAt ?? null
  const pending = seats?.pendingQuantity ?? null

  // C100 SE-1 B: an increase is charged now for the remaining days of the term (the server's invoice is authoritative).
  const added = seats && valid && quantity > seats.quantity ? quantity - seats.quantity : 0
  const estimate = seats && added > 0 && (seats.unitAmount ?? 0) > 0 && (seats.termDays ?? 0) > 0
    ? Math.round((added * (seats.unitAmount ?? 0) * (seats.remainingDays ?? 0)) / (seats.termDays ?? 1))
    : null
  const estimateText = seats && estimate !== null
    ? t('subscriptions.seats.estimate', {
      days: seats.remainingDays, term: seats.termDays, amount: formatMoney(estimate, seats.currency ?? 'INR', i18n.language),
    })
    : null
  const decreasing = seats !== null && valid && quantity < seats.quantity

  const describe = (s: SeatSummary): { text: string; invoice: boolean } => {
    const date = s.renewsAt ? formatDate(s.renewsAt) : null
    switch (s.effect) {
      case 'AT_RENEWAL':
        return { text: date ? t('subscriptions.seats.result.AT_RENEWAL', { quantity: s.pendingQuantity ?? quantity, date })
          : t('subscriptions.seats.result.AT_RENEWAL_NO_DATE', { quantity: s.pendingQuantity ?? quantity }), invoice: false }
      case 'WITHDRAWN': return { text: t('subscriptions.seats.result.WITHDRAWN', { quantity: s.quantity }), invoice: false }
      case 'UNCHANGED': return { text: t('subscriptions.seats.result.UNCHANGED'), invoice: false }
      default:
        return s.invoiceId != null
          ? { text: t('subscriptions.seats.result.NOW_INVOICE', { quantity: s.quantity }), invoice: true }
          : { text: t('subscriptions.seats.result.NOW', { quantity: s.quantity }), invoice: false }
    }
  }

  const change = (target: number) => {
    setBusy(true)
    setError(null)
    setResult(null)
    organizationApi.changeSeats(subscription.id, target)
      .then((s) => { setConfirming(false); setValue(String(s.quantity)); setResult(describe(s)); onChanged(s) })
      .catch((e) => { setConfirming(false); setError(e instanceof ApiError ? e.message : t('subscriptions.seats.error')) })
      .finally(() => setBusy(false))
  }
  const save = () => change(quantity)
  // Asking for the current quantity again withdraws a scheduled decrease.
  const withdraw = () => { if (seats) change(seats.quantity) }

  const setAutoRenew = (autoRenew: boolean) => {
    setAutoRenewBusy(true)
    setError(null)
    organizationApi.setAutoRenew(subscription.id, autoRenew)
      .then(onSubscription)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('subscriptions.autoRenew.error')))
      .finally(() => setAutoRenewBusy(false))
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
          {subscription.expiresAt && !inGrace && (
            <Chip size="small" variant="outlined" label={t('subscriptions.renewal.renewsOn', { date: formatDate(subscription.expiresAt) })} />
          )}
          {inGrace && graceEndsAt && <ReadOnlyChip graceEndsAt={graceEndsAt} />}
        </Box>
      </Box>

      {inGrace && graceEndsAt && <GraceNotice product={subscription.productName} graceEndsAt={graceEndsAt} />}
      {subscription.status === 'ACTIVE' && subscription.expiresAt && (
        <Box sx={{ my: 1.5 }}>
          <AutoRenewControl id={`org-auto-renew-${subscription.id}`} product={subscription.productName}
            checked={subscription.autoRenew ?? false} busy={autoRenewBusy} onChange={setAutoRenew} />
        </Box>
      )}

      {seats ? (
        <>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {t('subscriptions.seats.usage', { inUse: seats.inUse, quantity: seats.quantity })}
          </Typography>
          <LinearProgress variant="determinate" value={percent} color={percent >= 90 ? 'warning' : 'primary'}
            aria-label={t('subscriptions.seats.usageLabel', { product: subscription.productName })}
            sx={{ my: 1, height: 8, borderRadius: 4 }} />
          {pending !== null && (
            <Alert severity="info" sx={{ mt: 1 }}
              action={canChangeSeats && !inGrace ? (
                <Button color="inherit" size="small" disabled={busy} onClick={withdraw}>{t('subscriptions.seats.withdraw')}</Button>
              ) : undefined}>
              {renewsAt
                ? t('subscriptions.seats.pending', { quantity: pending, date: formatDate(renewsAt) })
                : t('subscriptions.seats.pendingNoDate', { quantity: pending })}
            </Alert>
          )}
          {!canChangeSeats ? (
            <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1.5 }}>{t('subscriptions.seats.viewOnly')}</Typography>
          ) : inGrace ? (
            <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1.5 }}>{t('subscriptions.seats.readOnly')}</Typography>
          ) : (
          <>
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
          {estimateText && (
            <Box aria-live="polite" sx={{ mt: 1 }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>{estimateText}</Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('subscriptions.seats.estimateNote', { added })}</Typography>
            </Box>
          )}
          {decreasing && (
            <Typography aria-live="polite" variant="body2" sx={{ color: 'text.secondary', mt: 1 }}>
              {renewsAt ? t('subscriptions.seats.decreaseNote', { date: formatDate(renewsAt) }) : t('subscriptions.seats.decreaseNoteNoDate')}
            </Typography>
          )}
          </>
          )}
        </>
      ) : (
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('subscriptions.seats.notChangeable')}</Typography>
      )}
      {error && <Alert severity="error" sx={{ mt: 1.5 }} onClose={() => setError(null)}>{error}</Alert>}
      {result && (
        <Alert severity="success" sx={{ mt: 1.5 }} onClose={() => setResult(null)}
          action={result.invoice ? (
            <Link component={RouterLink} to="/organization/billing?tab=invoices" color="inherit" underline="always" sx={{ alignSelf: 'center', mr: 1 }}>
              {t('subscriptions.seats.viewInvoices')}
            </Link>
          ) : undefined}>
          {result.text}
        </Alert>
      )}

      <Dialog open={confirming} onClose={() => setConfirming(false)} aria-labelledby={`${headingId}-confirm`}>
        <DialogTitle id={`${headingId}-confirm`}>
          {t('subscriptions.seats.confirmTitle', { from: seats?.quantity ?? 0, to: quantity })}
        </DialogTitle>
        <DialogContent>
          {decreasing ? (
            <DialogContentText>{t('subscriptions.seats.confirmDecreaseBody', { quantity: seats?.quantity ?? 0 })}</DialogContentText>
          ) : (
            <>
              <DialogContentText>{t('subscriptions.seats.confirmIncreaseBody')}</DialogContentText>
              {estimateText && <DialogContentText sx={{ fontWeight: 600, mt: 1 }}>{estimateText}</DialogContentText>}
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirming(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" disabled={busy} onClick={save}>{t('subscriptions.seats.confirm')}</Button>
        </DialogActions>
      </Dialog>
    </Paper>
  )
}
