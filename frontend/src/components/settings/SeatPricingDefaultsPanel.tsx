import { useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Button, MenuItem, Paper, Skeleton, TextField, Typography } from '@mui/material'
import { Users } from 'lucide-react'
import { adminBillingApi, type SeatPricingDefaults } from '../../api/billingApi'
import { FieldGrid, SettingsSection } from './SettingsSection'
import { SaveBar } from './SaveBar'
import { useSettingsForm } from './useSettingsForm'

type Form = { globalSeatPrice: string; globalSeatPriceCurrency: string }
const CURRENCIES = ['USD', 'EUR', 'GBP', 'INR']

const toForm = (v: SeatPricingDefaults): Form => ({
  globalSeatPrice: v.globalSeatPrice != null ? String(v.globalSeatPrice) : '',
  globalSeatPriceCurrency: v.globalSeatPriceCurrency ?? 'USD',
})

/**
 * C106: Billing settings → Seat pricing. The platform-wide default per-seat
 * price every seat-pricing-enabled product inherits, unless it has its own
 * override set on the product form.
 */
export function SeatPricingDefaultsPanel({ onSaved }: { onSaved: () => void }) {
  const { t } = useTranslation()
  const validate = useCallback((f: Form) => ({
    globalSeatPrice: f.globalSeatPrice !== '' && (Number.isNaN(Number(f.globalSeatPrice)) || Number(f.globalSeatPrice) < 0)
      ? t('admin.billingSettings.seatPricing.invalidPrice') : undefined,
  }), [t])
  const s = useSettingsForm<SeatPricingDefaults, Form>({
    load: adminBillingApi.seatPricingDefaults,
    save: (f) => adminBillingApi.saveSeatPricingDefaults({
      globalSeatPrice: f.globalSeatPrice === '' ? null : Number(f.globalSeatPrice),
      globalSeatPriceCurrency: f.globalSeatPrice === '' ? null : f.globalSeatPriceCurrency,
    }),
    toForm, validate,
    loadErrorText: t('admin.billingSettings.loadError'), saveErrorText: t('checkout.genericError'),
  })

  if (!s.form) {
    return s.loadError
      ? <Alert severity="error" action={<Button onClick={s.retry}>{t('admin.billingSettings.retry')}</Button>}>{s.loadError}</Alert>
      : <Skeleton variant="rounded" height={220} />
  }
  const f = s.form
  const save = () => { void s.submit().then((ok) => { if (ok) onSaved() }) }

  return (
    <>
      <SettingsSection id="seat-pricing-defaults" icon={Users} accent="violet" title={t('admin.billingSettings.seatPricing.title')}
        description={t('admin.billingSettings.seatPricing.description')}>
        <FieldGrid columns={3}>
          <TextField label={t('admin.billingSettings.seatPricing.price')} value={f.globalSeatPrice}
            onChange={(e) => s.set('globalSeatPrice', e.target.value.replace(/[^0-9.]/g, ''))}
            error={Boolean(s.shownError('globalSeatPrice'))} helperText={s.shownError('globalSeatPrice') ?? t('admin.billingSettings.seatPricing.priceHelp')}
            slotProps={{ htmlInput: { inputMode: 'decimal' } }} />
          <TextField select label={t('admin.billingSettings.seatPricing.currency')} value={f.globalSeatPriceCurrency}
            onChange={(e) => s.set('globalSeatPriceCurrency', e.target.value)}>
            {CURRENCIES.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
          </TextField>
        </FieldGrid>
        <Paper variant="outlined" sx={{ p: 2, mt: 2, borderRadius: 2.5 }}>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.billingSettings.seatPricing.note')}</Typography>
        </Paper>
      </SettingsSection>
      <SaveBar dirty={s.dirty} busy={s.busy} updatedAt={s.saved?.updatedAt} error={s.error} onSave={save} onCancel={s.reset} />
    </>
  )
}
