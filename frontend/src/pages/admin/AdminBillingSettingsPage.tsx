import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router-dom'
import {
  Alert, Box, Button, Chip, FormControlLabel, InputAdornment, MenuItem, Paper, Skeleton, Snackbar, Switch, Tab, Tabs,
  TextField, Typography, useTheme,
} from '@mui/material'
import { alpha } from '@mui/material/styles'
import {
  BadgeCheck, Building2, CreditCard, Eye, FileText, Landmark, Mail, MapPin, Palette, PlugZap, ReceiptText, Settings2,
  Smartphone, Wallet, Banknote, ListChecks, MessageSquareText,
} from 'lucide-react'
import {
  adminBillingApi, type BusinessProfile, type GatewayStatus, type OfflineBankDetails, type PaymentMethodSettings,
} from '../../api/billingApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { FieldGrid, SettingsSection, StatusTile } from '../../components/settings/SettingsSection'
import { SaveBar } from '../../components/settings/SaveBar'
import { useSettingsForm } from '../../components/settings/useSettingsForm'
import { RazorpayGatewayPanel } from '../../components/settings/RazorpayGatewayPanel'
import { accentColor } from '../../theming/accents'

type TabKey = 'business' | 'offline' | 'methods' | 'gateway'
const TABS: TabKey[] = ['business', 'offline', 'methods', 'gateway']

const RE = {
  gstin: /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z]$/i,
  pan: /^[A-Z]{5}[0-9]{4}[A-Z]$/i,
  cin: /^[LU][0-9]{5}[A-Z]{2}[0-9]{4}[A-Z]{3}[0-9]{6}$/i,
  email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  phone: /^[+0-9 ()-]{6,30}$/,
  website: /^https?:\/\/\S{3,250}$/,
  prefix: /^[A-Z0-9]{2,10}$/i,
  ifsc: /^[A-Z]{4}0[A-Z0-9]{6}$/i,
  swift: /^[A-Z0-9]{8}([A-Z0-9]{3})?$/i,
  iban: /^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$/i,
  micr: /^[0-9]{9}$/,
  upi: /^[A-Za-z0-9._-]{2,256}@[A-Za-z]{2,64}$/,
  color: /^#[0-9A-Fa-f]{6}$/,
}

const TERMS_PRESETS = [0, 7, 15, 30, 45, 60, 90]

/**
 * "/admin/billing/settings" — MANAGE_BILLING. C55 (REQ-BIL-001.21), extended
 * by C60 (docs/05-ui/screen-requirements/admin-billing-settings.md):
 * Business & invoicing, Offline payments, Payment methods (with the Razorpay
 * Checkout appearance) and the Razorpay gateway credentials view. None of
 * these values are secrets; Razorpay keys stay in config/secrets.env
 * (BR-SEC-001) and are only reported as set / not set here.
 */
export function AdminBillingSettingsPage() {
  const { t } = useTranslation()
  const [params, setParams] = useSearchParams()
  const tab: TabKey = TABS.includes(params.get('tab') as TabKey) ? (params.get('tab') as TabKey) : 'business'
  const setTab = (next: TabKey) => setParams({ tab: next }, { replace: true })

  // Overview tiles read the same endpoints the tabs edit.
  const [overview, setOverview] = useState<{ business?: BusinessProfile; offline?: OfflineBankDetails; methods?: PaymentMethodSettings; gateway?: GatewayStatus }>({})
  const refreshOverview = useCallback(() => {
    adminBillingApi.businessProfile().then((business) => setOverview((o) => ({ ...o, business }))).catch(() => {})
    adminBillingApi.offlineBankDetails().then((offline) => setOverview((o) => ({ ...o, offline }))).catch(() => {})
    adminBillingApi.paymentMethodSettings().then((methods) => setOverview((o) => ({ ...o, methods }))).catch(() => {})
    adminBillingApi.gatewayStatus().then((gateway) => setOverview((o) => ({ ...o, gateway }))).catch(() => {})
  }, [])
  useEffect(() => { refreshOverview() }, [refreshOverview])
  const [toast, setToast] = useState<string | null>(null)
  const onSaved = () => { setToast(t('admin.billingSettings.saved')); refreshOverview() }

  const methodsOn = overview.methods
    ? [overview.methods.cardEnabled, overview.methods.upiEnabled, overview.methods.netbankingEnabled, overview.methods.walletEnabled, overview.methods.payByInvoiceEnabled].filter(Boolean).length
    : null
  const businessReady = Boolean(overview.business?.legalName && overview.business?.addressLine1)
  const offlineReady = Boolean(overview.offline?.accountName && overview.offline?.accountNumber)

  return (
    <>
      <PageHeader icon={Settings2} accent="violet" eyebrow={t('admin.billingSettings.eyebrow')}
        title={t('admin.billingSettings.title')} subtitle={t('admin.billingSettings.subtitle')} />

      <Box sx={{ display: 'grid', gap: 1.5, mb: 3, gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 1fr))', lg: 'repeat(4, minmax(0, 1fr))' } }}>
        <StatusTile icon={Building2} accent="blue" label={t('admin.billingSettings.tabs.business')} selected={tab === 'business'} onClick={() => setTab('business')}
          value={overview.business?.legalName ?? t('admin.billingSettings.overview.notSet')}
          status={businessReady ? t('admin.billingSettings.overview.complete') : t('admin.billingSettings.overview.incomplete')}
          statusColor={businessReady ? 'success' : 'warning'} />
        <StatusTile icon={Landmark} accent="teal" label={t('admin.billingSettings.tabs.offline')} selected={tab === 'offline'} onClick={() => setTab('offline')}
          value={overview.offline?.bankName ?? t('admin.billingSettings.overview.notSet')}
          status={offlineReady ? t('admin.billingSettings.overview.ready') : t('admin.billingSettings.overview.incomplete')}
          statusColor={offlineReady ? 'success' : 'warning'} />
        <StatusTile icon={ListChecks} accent="violet" label={t('admin.billingSettings.tabs.methods')} selected={tab === 'methods'} onClick={() => setTab('methods')}
          value={methodsOn == null ? '—' : t('admin.billingSettings.overview.methodsOn', { count: methodsOn, total: 5 })}
          status={methodsOn ? t('admin.billingSettings.overview.ready') : undefined} statusColor="success" />
        <StatusTile icon={PlugZap} accent="amber" label={t('admin.billingSettings.tabs.gateway')} selected={tab === 'gateway'} onClick={() => setTab('gateway')}
          value={overview.gateway ? (overview.gateway.liveMode ? t('admin.paymentGateway.live') : t('admin.paymentGateway.test')) : '—'}
          status={overview.gateway ? (overview.gateway.configured ? t('admin.paymentGateway.configured') : t('admin.paymentGateway.notConfigured')) : undefined}
          statusColor={overview.gateway?.configured ? 'success' : 'error'} />
      </Box>

      <Paper variant="outlined" sx={{ borderRadius: 3, mb: 3 }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" allowScrollButtonsMobile aria-label={t('admin.billingSettings.tabsLabel')} sx={{ px: 1 }}>
          <Tab value="business" icon={<Building2 size={16} aria-hidden />} iconPosition="start" label={t('admin.billingSettings.tabs.business')} id="tab-business" aria-controls="panel-business" />
          <Tab value="offline" icon={<Landmark size={16} aria-hidden />} iconPosition="start" label={t('admin.billingSettings.tabs.offline')} id="tab-offline" aria-controls="panel-offline" />
          <Tab value="methods" icon={<ListChecks size={16} aria-hidden />} iconPosition="start" label={t('admin.billingSettings.tabs.methods')} id="tab-methods" aria-controls="panel-methods" />
          <Tab value="gateway" icon={<PlugZap size={16} aria-hidden />} iconPosition="start" label={t('admin.billingSettings.tabs.gateway')} id="tab-gateway" aria-controls="panel-gateway" />
        </Tabs>
      </Paper>

      <Box role="tabpanel" id={`panel-${tab}`} aria-labelledby={`tab-${tab}`}>
        {tab === 'business' && <BusinessTab onSaved={onSaved} />}
        {tab === 'offline' && <OfflineTab onSaved={onSaved} />}
        {tab === 'methods' && <MethodsTab onSaved={onSaved} gatewayConfigured={overview.gateway?.configured ?? true} />}
        {tab === 'gateway' && <RazorpayGatewayPanel onStatus={(gateway) => setOverview((o) => ({ ...o, gateway }))} />}
      </Box>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast ? <Alert severity="success" variant="filled" onClose={() => setToast(null)}>{toast}</Alert> : undefined}
      </Snackbar>
    </>
  )
}

function Loading({ error, onRetry }: { error: string | null; onRetry: () => void }) {
  const { t } = useTranslation()
  if (error) return <Alert severity="error" action={<Button onClick={onRetry}>{t('admin.billingSettings.retry')}</Button>}>{error}</Alert>
  return <Box sx={{ display: 'grid', gap: 2 }}><Skeleton variant="rounded" height={220} /><Skeleton variant="rounded" height={180} /></Box>
}

/** Two columns on large screens: the form, and a sticky live preview. */
function WithPreview({ children, preview }: { children: React.ReactNode; preview: React.ReactNode }) {
  return (
    <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 2fr) minmax(280px, 1fr)' }, alignItems: 'start' }}>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, minWidth: 0 }}>{children}</Box>
      <Box sx={{ position: { lg: 'sticky' }, top: { lg: 88 } }}>{preview}</Box>
    </Box>
  )
}

function PreviewCard({ title, children }: { title: string; children: React.ReactNode }) {
  const theme = useTheme()
  const color = accentColor('indigo', theme.palette.mode)
  return (
    <Paper variant="outlined" component="aside" aria-label={title} sx={{ borderRadius: 3, overflow: 'hidden' }}>
      <Box sx={{ px: 2, py: 1.25, display: 'flex', alignItems: 'center', gap: 1, color, bgcolor: alpha(color, theme.palette.mode === 'dark' ? 0.16 : 0.07), borderBottom: '1px solid', borderColor: 'divider' }}>
        <Eye size={16} aria-hidden />
        <Typography variant="subtitle2" component="h2">{title}</Typography>
      </Box>
      <Box sx={{ p: 2 }}>{children}</Box>
    </Paper>
  )
}

// ---------------------------------------------------------------- Business

type BusinessForm = {
  legalName: string; tradeName: string; gstin: string; pan: string; cin: string
  addressLine1: string; addressLine2: string; city: string; state: string; postalCode: string; country: string
  email: string; phone: string; website: string; invoicePrefix: string; paymentTermsDays: string; invoiceFooterNote: string
}

const businessToForm = (b: BusinessProfile): BusinessForm => ({
  legalName: b.legalName ?? '', tradeName: b.tradeName ?? '', gstin: b.gstin ?? '', pan: b.pan ?? '', cin: b.cin ?? '',
  addressLine1: b.addressLine1 ?? '', addressLine2: b.addressLine2 ?? '', city: b.city ?? '', state: b.state ?? '',
  postalCode: b.postalCode ?? '', country: b.country ?? '', email: b.email ?? '', phone: b.phone ?? '', website: b.website ?? '',
  invoicePrefix: b.invoicePrefix ?? 'INV', paymentTermsDays: String(b.paymentTermsDays ?? 0), invoiceFooterNote: b.invoiceFooterNote ?? '',
})

function BusinessTab({ onSaved }: { onSaved: () => void }) {
  const { t } = useTranslation()
  const req = t('admin.billingSettings.required')
  const validate = useCallback((f: BusinessForm) => ({
    legalName: !f.legalName.trim() ? req : f.legalName.length > 200 ? t('admin.billingSettings.tooLong', { max: 200 }) : undefined,
    gstin: f.gstin && !RE.gstin.test(f.gstin) ? t('admin.billingSettings.v.gstin') : undefined,
    pan: f.pan && !RE.pan.test(f.pan) ? t('admin.billingSettings.v.pan') : undefined,
    cin: f.cin && !RE.cin.test(f.cin) ? t('admin.billingSettings.v.cin') : undefined,
    addressLine1: !f.addressLine1.trim() ? req : undefined,
    city: !f.city.trim() ? req : undefined,
    state: !f.state.trim() ? req : undefined,
    postalCode: !f.postalCode.trim() ? req : undefined,
    country: !f.country.trim() ? req : undefined,
    email: !f.email.trim() ? req : !RE.email.test(f.email) ? t('admin.billingSettings.v.email') : undefined,
    phone: f.phone && !RE.phone.test(f.phone) ? t('admin.billingSettings.v.phone') : undefined,
    website: f.website && !RE.website.test(f.website) ? t('admin.billingSettings.v.website') : undefined,
    invoicePrefix: !RE.prefix.test(f.invoicePrefix) ? t('admin.billingSettings.v.prefix') : undefined,
    paymentTermsDays: !/^\d{1,3}$/.test(f.paymentTermsDays) || Number(f.paymentTermsDays) > 365 ? t('admin.billingSettings.v.terms') : undefined,
    invoiceFooterNote: f.invoiceFooterNote.length > 500 ? t('admin.billingSettings.tooLong', { max: 500 }) : undefined,
  }), [t, req])
  const s = useSettingsForm<BusinessProfile, BusinessForm>({
    load: adminBillingApi.businessProfile,
    save: (f) => adminBillingApi.saveBusinessProfile({ ...f, gstin: f.gstin.toUpperCase(), pan: f.pan.toUpperCase(), cin: f.cin.toUpperCase(), invoicePrefix: f.invoicePrefix.toUpperCase(), paymentTermsDays: Number(f.paymentTermsDays) }),
    toForm: businessToForm, validate,
    loadErrorText: t('admin.billingSettings.loadError'), saveErrorText: t('checkout.genericError'),
  })
  if (!s.form) return <Loading error={s.loadError} onRetry={s.retry} />
  const f = s.form

  const field = (key: keyof BusinessForm, opts: { required?: boolean; max?: number; full?: boolean; upper?: boolean; placeholder?: string; type?: string; helper?: string; multiline?: boolean } = {}) => (
    <TextField
      label={t(`admin.billingSettings.f.${key}`)} value={f[key]} required={opts.required} type={opts.type}
      onChange={(e) => s.set(key, opts.upper ? e.target.value.toUpperCase() : e.target.value)}
      error={Boolean(s.shownError(key))} helperText={s.shownError(key) ?? opts.helper ?? ' '}
      placeholder={opts.placeholder} fullWidth multiline={opts.multiline} minRows={opts.multiline ? 3 : undefined}
      sx={opts.full ? { gridColumn: '1 / -1' } : undefined}
      slotProps={{ htmlInput: { maxLength: opts.max } }}
    />
  )
  const year = new Date().getFullYear()
  const terms = Number(f.paymentTermsDays) || 0

  return (
    <>
      <WithPreview preview={(
        <PreviewCard title={t('admin.billingSettings.preview.invoiceTitle')}>
          <Typography sx={{ fontWeight: 800 }}>{f.legalName || t('admin.billingSettings.preview.legalName')}</Typography>
          {f.tradeName && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{f.tradeName}</Typography>}
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
            {[f.addressLine1, f.addressLine2, f.city, f.state, f.postalCode, f.country].filter(Boolean).join(', ') || t('admin.billingSettings.preview.address')}
          </Typography>
          <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mt: 1 }}>
            {f.gstin && <Chip size="small" color="primary" variant="outlined" label={`GSTIN ${f.gstin}`} />}
            {f.pan && <Chip size="small" color="secondary" variant="outlined" label={`PAN ${f.pan}`} />}
            {f.cin && <Chip size="small" variant="outlined" label={`CIN ${f.cin}`} />}
          </Box>
          <Box sx={{ mt: 2, p: 1.5, borderRadius: 2, bgcolor: 'action.hover', display: 'grid', gridTemplateColumns: 'auto 1fr', columnGap: 2, rowGap: 0.5 }}>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('admin.billingSettings.preview.number')}</Typography>
            <Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 700 }}>{(f.invoicePrefix || 'INV').toUpperCase()}-{year}-000123</Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('admin.billingSettings.preview.due')}</Typography>
            <Typography variant="caption" sx={{ fontWeight: 600 }}>{terms === 0 ? t('admin.billingSettings.preview.onReceipt') : t('admin.billingSettings.preview.netDays', { days: terms })}</Typography>
          </Box>
          {f.invoiceFooterNote && <Typography variant="caption" sx={{ display: 'block', mt: 1.5, color: 'text.secondary', fontStyle: 'italic' }}>{f.invoiceFooterNote}</Typography>}
        </PreviewCard>
      )}>
        <SettingsSection id="legal" icon={BadgeCheck} accent="blue" title={t('admin.billingSettings.s.legal')} description={t('admin.billingSettings.s.legalBody')}>
          <FieldGrid>
            {field('legalName', { required: true, max: 200, full: true })}
            {field('tradeName', { max: 200 })}
            {field('gstin', { upper: true, max: 15, placeholder: '29ABCDE1234F1Z5' })}
            {field('pan', { upper: true, max: 10, placeholder: 'ABCDE1234F' })}
            {field('cin', { upper: true, max: 21, placeholder: 'U72900KA2020PTC123456' })}
          </FieldGrid>
        </SettingsSection>
        <SettingsSection id="address" icon={MapPin} accent="indigo" title={t('admin.billingSettings.s.address')} description={t('admin.billingSettings.s.addressBody')}>
          <FieldGrid>
            {field('addressLine1', { required: true, max: 200, full: true })}
            {field('addressLine2', { max: 200, full: true })}
            {field('city', { required: true, max: 100 })}
            {field('state', { required: true, max: 100 })}
            {field('postalCode', { required: true, max: 20 })}
            {field('country', { required: true, max: 100 })}
          </FieldGrid>
        </SettingsSection>
        <SettingsSection id="contact" icon={Mail} accent="cyan" title={t('admin.billingSettings.s.contact')} description={t('admin.billingSettings.s.contactBody')}>
          <FieldGrid>
            {field('email', { required: true, max: 255, type: 'email' })}
            {field('phone', { max: 30, placeholder: '+91 80 1234 5678' })}
            {field('website', { max: 255, full: true, placeholder: 'https://' })}
          </FieldGrid>
        </SettingsSection>
        <SettingsSection id="invoicing" icon={ReceiptText} accent="amber" title={t('admin.billingSettings.s.invoicing')} description={t('admin.billingSettings.s.invoicingBody')}>
          <FieldGrid>
            {field('invoicePrefix', { required: true, upper: true, max: 10, helper: t('admin.billingSettings.h.prefix') })}
            <TextField
              label={t('admin.billingSettings.f.paymentTermsDays')} value={f.paymentTermsDays} required
              onChange={(e) => s.set('paymentTermsDays', e.target.value.replace(/\D/g, '').slice(0, 3))}
              error={Boolean(s.shownError('paymentTermsDays'))} helperText={s.shownError('paymentTermsDays') ?? t('admin.billingSettings.h.terms')}
              slotProps={{ input: { endAdornment: <InputAdornment position="end">{t('admin.billingSettings.days')}</InputAdornment> }, htmlInput: { inputMode: 'numeric' } }}
            />
            <Box sx={{ gridColumn: '1 / -1', display: 'flex', gap: 1, flexWrap: 'wrap' }} role="group" aria-label={t('admin.billingSettings.h.termsPresets')}>
              {TERMS_PRESETS.map((d) => (
                <Chip key={d} label={d === 0 ? t('admin.billingSettings.preview.onReceipt') : t('admin.billingSettings.preview.netDays', { days: d })}
                  color={terms === d ? 'primary' : 'default'} variant={terms === d ? 'filled' : 'outlined'}
                  onClick={() => s.set('paymentTermsDays', String(d))} aria-pressed={terms === d} />
              ))}
            </Box>
            {field('invoiceFooterNote', { max: 500, full: true, multiline: true, helper: t('admin.billingSettings.h.footer') })}
          </FieldGrid>
        </SettingsSection>
      </WithPreview>
      <SaveBar dirty={s.dirty} busy={s.busy} error={s.error} updatedAt={s.saved?.updatedAt}
        onCancel={s.reset} onSave={async () => { if (await s.submit()) onSaved() }} />
    </>
  )
}

// ---------------------------------------------------------------- Offline

type OfflineForm = {
  accountName: string; bankName: string; branchName: string; accountNumber: string; accountType: string
  ifsc: string; micr: string; swiftBic: string; iban: string; upiId: string
  chequePayableTo: string; chequeAddress: string; instructions: string
  bankTransferEnabled: boolean; neftRtgsEnabled: boolean; chequeEnabled: boolean
}

const offlineToForm = (d: OfflineBankDetails): OfflineForm => ({
  accountName: d.accountName ?? '', bankName: d.bankName ?? '', branchName: d.branchName ?? '', accountNumber: d.accountNumber ?? '',
  accountType: d.accountType ?? '', ifsc: d.ifsc ?? '', micr: d.micr ?? '', swiftBic: d.swiftBic ?? '', iban: d.iban ?? '',
  upiId: d.upiId ?? '', chequePayableTo: d.chequePayableTo ?? '', chequeAddress: d.chequeAddress ?? '', instructions: d.instructions ?? '',
  bankTransferEnabled: d.bankTransferEnabled ?? true, neftRtgsEnabled: d.neftRtgsEnabled ?? true, chequeEnabled: d.chequeEnabled ?? true,
})

function OfflineTab({ onSaved }: { onSaved: () => void }) {
  const { t } = useTranslation()
  const req = t('admin.billingSettings.required')
  const validate = useCallback((f: OfflineForm) => ({
    accountName: !f.accountName.trim() ? req : undefined,
    bankName: !f.bankName.trim() ? req : undefined,
    accountNumber: !f.accountNumber.trim() ? req : f.accountNumber.length > 34 ? t('admin.billingSettings.tooLong', { max: 34 }) : undefined,
    ifsc: f.ifsc && !RE.ifsc.test(f.ifsc) ? t('admin.billingSettings.ifscLength') : undefined,
    swiftBic: f.swiftBic && !RE.swift.test(f.swiftBic) ? t('admin.billingSettings.swiftLength') : undefined,
    iban: f.iban && !RE.iban.test(f.iban) ? t('admin.billingSettings.v.iban') : undefined,
    micr: f.micr && !RE.micr.test(f.micr) ? t('admin.billingSettings.v.micr') : undefined,
    upiId: f.upiId && !RE.upi.test(f.upiId) ? t('admin.billingSettings.v.upi') : undefined,
    instructions: f.instructions.length > 1000 ? t('admin.billingSettings.tooLong', { max: 1000 }) : undefined,
    bankTransferEnabled: !f.bankTransferEnabled && !f.neftRtgsEnabled && !f.chequeEnabled ? t('admin.billingSettings.v.oneOffline') : undefined,
  }), [t, req])
  const s = useSettingsForm<OfflineBankDetails, OfflineForm>({
    load: adminBillingApi.offlineBankDetails,
    save: (f) => adminBillingApi.saveOfflineBankDetails({
      ...f, accountType: (f.accountType || undefined) as OfflineBankDetails['accountType'],
      ifsc: f.ifsc.toUpperCase(), swiftBic: f.swiftBic.toUpperCase(), iban: f.iban.toUpperCase(),
    }),
    toForm: offlineToForm, validate,
    loadErrorText: t('admin.billingSettings.loadError'), saveErrorText: t('checkout.genericError'),
  })
  if (!s.form) return <Loading error={s.loadError} onRetry={s.retry} />
  const f = s.form

  const field = (key: keyof OfflineForm, opts: { required?: boolean; max?: number; full?: boolean; upper?: boolean; placeholder?: string; helper?: string; multiline?: boolean } = {}) => (
    <TextField
      label={t(`admin.billingSettings.f.${key}`)} value={f[key] as string} required={opts.required}
      onChange={(e) => s.set(key, opts.upper ? e.target.value.toUpperCase() : e.target.value)}
      error={Boolean(s.shownError(key))} helperText={s.shownError(key) ?? opts.helper ?? ' '}
      placeholder={opts.placeholder} fullWidth multiline={opts.multiline} minRows={opts.multiline ? 3 : undefined}
      sx={opts.full ? { gridColumn: '1 / -1' } : undefined}
      slotProps={{ htmlInput: { maxLength: opts.max } }}
    />
  )
  const toggle = (key: 'bankTransferEnabled' | 'neftRtgsEnabled' | 'chequeEnabled') => (
    <FormControlLabel control={<Switch checked={f[key]} onChange={(e) => s.set(key, e.target.checked)} />} label={t(`admin.billingSettings.f.${key}`)} />
  )

  return (
    <>
      {!s.saved?.accountNumber && <Alert severity="info" sx={{ mb: 3 }}>{t('admin.billingSettings.empty')}</Alert>}
      <WithPreview preview={(
        <PreviewCard title={t('admin.billingSettings.preview.customerTitle')}>
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('checkout.result.offline.bankTitle')}</Typography>
          <Box sx={{ mt: 1, display: 'grid', gridTemplateColumns: 'auto 1fr', columnGap: 2, rowGap: 0.5 }}>
            {([
              ['accountName', f.accountName], ['bankName', [f.bankName, f.branchName].filter(Boolean).join(', ')],
              ['accountNumber', f.accountNumber ? `${f.accountNumber}${f.accountType ? ` (${t(`admin.billingSettings.accountTypes.${f.accountType}`)})` : ''}` : ''],
              ['ifsc', f.ifsc], ['swiftBic', f.swiftBic], ['iban', f.iban], ['upiId', f.upiId],
            ] as const).filter(([, v]) => v).map(([k, v]) => (
              <Box key={k} sx={{ display: 'contents' }}>
                <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t(`admin.billingSettings.f.${k}`)}</Typography>
                <Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 600, wordBreak: 'break-all' }}>{v}</Typography>
              </Box>
            ))}
          </Box>
          {!f.accountName && !f.accountNumber && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.offline.noBank')}</Typography>}
          <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mt: 1.5 }}>
            {f.bankTransferEnabled && <Chip size="small" color="success" variant="outlined" label={t('admin.billing.offline.methods.BANK_TRANSFER')} />}
            {f.neftRtgsEnabled && <Chip size="small" color="success" variant="outlined" label={t('admin.billing.offline.methods.NEFT_RTGS')} />}
            {f.chequeEnabled && <Chip size="small" color="success" variant="outlined" label={t('admin.billing.offline.methods.CHEQUE')} />}
          </Box>
          {f.chequeEnabled && f.chequePayableTo && (
            <Typography variant="caption" sx={{ display: 'block', mt: 1 }}>{t('admin.billingSettings.preview.cheque', { name: f.chequePayableTo })}</Typography>
          )}
          {f.instructions && <Typography variant="caption" sx={{ display: 'block', mt: 1, color: 'text.secondary', whiteSpace: 'pre-line' }}>{f.instructions}</Typography>}
        </PreviewCard>
      )}>
        <SettingsSection id="bank" icon={Landmark} accent="teal" title={t('admin.billingSettings.s.bank')} description={t('admin.billingSettings.s.bankBody')}>
          <FieldGrid>
            {field('accountName', { required: true, max: 200, full: true })}
            {field('bankName', { required: true, max: 200 })}
            {field('branchName', { max: 200 })}
            {field('accountNumber', { required: true, max: 34 })}
            <TextField select label={t('admin.billingSettings.f.accountType')} value={f.accountType} onChange={(e) => s.set('accountType', e.target.value)} helperText=" ">
              <MenuItem value="">{t('admin.billingSettings.accountTypes.none')}</MenuItem>
              <MenuItem value="CURRENT">{t('admin.billingSettings.accountTypes.CURRENT')}</MenuItem>
              <MenuItem value="SAVINGS">{t('admin.billingSettings.accountTypes.SAVINGS')}</MenuItem>
            </TextField>
            {field('ifsc', { upper: true, max: 11, placeholder: 'HDFC0001234' })}
            {field('micr', { max: 9, placeholder: '560240002' })}
          </FieldGrid>
        </SettingsSection>
        <SettingsSection id="international" icon={Banknote} accent="emerald" title={t('admin.billingSettings.s.international')} description={t('admin.billingSettings.s.internationalBody')}>
          <FieldGrid>
            {field('swiftBic', { upper: true, max: 11, placeholder: 'HDFCINBB' })}
            {field('iban', { upper: true, max: 34 })}
          </FieldGrid>
        </SettingsSection>
        <SettingsSection id="upi" icon={Smartphone} accent="violet" title={t('admin.billingSettings.s.upi')} description={t('admin.billingSettings.s.upiBody')}>
          <FieldGrid>{field('upiId', { max: 100, placeholder: 'company@bank' })}</FieldGrid>
        </SettingsSection>
        <SettingsSection id="cheque" icon={FileText} accent="orange" title={t('admin.billingSettings.s.cheque')} description={t('admin.billingSettings.s.chequeBody')}>
          <FieldGrid>
            {field('chequePayableTo', { max: 200, full: true })}
            {field('chequeAddress', { max: 500, full: true, multiline: true })}
          </FieldGrid>
        </SettingsSection>
        <SettingsSection id="accepted" icon={ListChecks} accent="rose" title={t('admin.billingSettings.s.accepted')} description={t('admin.billingSettings.s.acceptedBody')}>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            {toggle('bankTransferEnabled')}
            {toggle('neftRtgsEnabled')}
            {toggle('chequeEnabled')}
          </Box>
          {s.shownError('bankTransferEnabled') && <Alert severity="error" sx={{ mt: 1 }}>{s.shownError('bankTransferEnabled')}</Alert>}
        </SettingsSection>
        <SettingsSection id="instructions" icon={MessageSquareText} accent="pink" title={t('admin.billingSettings.s.instructions')} description={t('admin.billingSettings.s.instructionsBody')}>
          {field('instructions', { max: 1000, full: true, multiline: true })}
        </SettingsSection>
      </WithPreview>
      <SaveBar dirty={s.dirty} busy={s.busy} error={s.error} updatedAt={s.saved?.updatedAt}
        onCancel={s.reset} onSave={async () => { if (await s.submit()) onSaved() }} />
    </>
  )
}

// ---------------------------------------------------------------- Payment methods

type MethodsForm = {
  cardEnabled: boolean; upiEnabled: boolean; netbankingEnabled: boolean; walletEnabled: boolean; payByInvoiceEnabled: boolean
  checkoutDisplayName: string; checkoutDescription: string; checkoutThemeColor: string
}

const methodsToForm = (m: PaymentMethodSettings): MethodsForm => ({
  cardEnabled: m.cardEnabled, upiEnabled: m.upiEnabled, netbankingEnabled: m.netbankingEnabled, walletEnabled: m.walletEnabled,
  payByInvoiceEnabled: m.payByInvoiceEnabled, checkoutDisplayName: m.checkoutDisplayName ?? '',
  checkoutDescription: m.checkoutDescription ?? '', checkoutThemeColor: m.checkoutThemeColor ?? '',
})

const METHOD_ROWS = [
  { key: 'cardEnabled', icon: CreditCard, accent: 'blue' },
  { key: 'upiEnabled', icon: Smartphone, accent: 'violet' },
  { key: 'netbankingEnabled', icon: Building2, accent: 'teal' },
  { key: 'walletEnabled', icon: Wallet, accent: 'amber' },
] as const

function MethodsTab({ onSaved, gatewayConfigured }: { onSaved: () => void; gatewayConfigured: boolean }) {
  const { t } = useTranslation()
  const theme = useTheme()
  const validate = useCallback((f: MethodsForm) => ({
    cardEnabled: !f.cardEnabled && !f.upiEnabled && !f.netbankingEnabled && !f.walletEnabled && !f.payByInvoiceEnabled ? t('admin.billingSettings.v.oneMethod') : undefined,
    checkoutDisplayName: f.checkoutDisplayName.length > 100 ? t('admin.billingSettings.tooLong', { max: 100 }) : undefined,
    checkoutDescription: f.checkoutDescription.length > 255 ? t('admin.billingSettings.tooLong', { max: 255 }) : undefined,
    checkoutThemeColor: f.checkoutThemeColor && !RE.color.test(f.checkoutThemeColor) ? t('admin.billingSettings.v.color') : undefined,
  }), [t])
  const s = useSettingsForm<PaymentMethodSettings, MethodsForm>({
    load: adminBillingApi.paymentMethodSettings,
    save: (f) => adminBillingApi.savePaymentMethodSettings(f),
    toForm: methodsToForm, validate,
    loadErrorText: t('admin.billingSettings.loadError'), saveErrorText: t('checkout.genericError'),
  })
  if (!s.form) return <Loading error={s.loadError} onRetry={s.retry} />
  const f = s.form
  const previewColor = RE.color.test(f.checkoutThemeColor) ? f.checkoutThemeColor : theme.palette.primary.main

  const methodCard = (key: keyof MethodsForm, Icon: typeof CreditCard, accent: Parameters<typeof accentColor>[0], online: boolean) => {
    const color = accentColor(accent, theme.palette.mode)
    const on = f[key] as boolean
    return (
      <Paper key={key} variant="outlined" sx={{ p: 2, borderRadius: 2.5, display: 'flex', alignItems: 'center', gap: 1.5, borderColor: on ? color : 'divider', bgcolor: on ? alpha(color, theme.palette.mode === 'dark' ? 0.12 : 0.05) : 'background.paper' }}>
        <Box aria-hidden sx={{ width: 40, height: 40, borderRadius: 2, display: 'grid', placeItems: 'center', color, bgcolor: alpha(color, 0.14) }}><Icon size={20} /></Box>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography sx={{ fontWeight: 700 }}>{t(`admin.billingSettings.f.${key}`)}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t(`admin.billingSettings.h.${key}`)}</Typography>
          {online && !gatewayConfigured && <Typography variant="caption" sx={{ color: 'warning.main' }}>{t('admin.billingSettings.h.needsGateway')}</Typography>}
        </Box>
        <Switch checked={on} onChange={(e) => s.set(key, e.target.checked)} slotProps={{ input: { 'aria-label': t(`admin.billingSettings.f.${key}`) } }} />
      </Paper>
    )
  }

  return (
    <>
      <WithPreview preview={(
        <PreviewCard title={t('admin.billingSettings.preview.checkoutTitle')}>
          <Paper variant="outlined" sx={{ borderRadius: 2, overflow: 'hidden' }}>
            <Box sx={{ bgcolor: previewColor, color: '#fff', px: 2, py: 1.5 }}>
              <Typography sx={{ fontWeight: 800 }}>{f.checkoutDisplayName || 'eVyoog'}</Typography>
              <Typography variant="caption" sx={{ opacity: 0.9 }}>{f.checkoutDescription || t('admin.billingSettings.preview.checkoutDescription')}</Typography>
            </Box>
            <Box sx={{ p: 1.5, display: 'flex', flexDirection: 'column', gap: 0.75 }}>
              {METHOD_ROWS.filter((m) => f[m.key]).map((m) => (
                <Box key={m.key} sx={{ display: 'flex', alignItems: 'center', gap: 1, fontSize: 13 }}>
                  <m.icon size={14} aria-hidden /> {t(`admin.billingSettings.f.${m.key}`)}
                </Box>
              ))}
              {f.payByInvoiceEnabled && <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, fontSize: 13 }}><FileText size={14} aria-hidden /> {t('admin.billingSettings.f.payByInvoiceEnabled')}</Box>}
            </Box>
          </Paper>
          <Typography variant="caption" sx={{ display: 'block', mt: 1, color: 'text.secondary' }}>{t('admin.billingSettings.preview.checkoutNote')}</Typography>
        </PreviewCard>
      )}>
        <SettingsSection id="online" icon={CreditCard} accent="violet" title={t('admin.billingSettings.s.online')} description={t('admin.billingSettings.s.onlineBody')}>
          <Box sx={{ display: 'grid', gap: 1.5, gridTemplateColumns: { xs: '1fr', md: 'repeat(2, minmax(0, 1fr))' } }}>
            {METHOD_ROWS.map((m) => methodCard(m.key, m.icon, m.accent, true))}
          </Box>
        </SettingsSection>
        <SettingsSection id="offline-method" icon={FileText} accent="teal" title={t('admin.billingSettings.s.offlineMethod')} description={t('admin.billingSettings.s.offlineMethodBody')}>
          {methodCard('payByInvoiceEnabled', FileText, 'teal', false)}
          {s.shownError('cardEnabled') && <Alert severity="error" sx={{ mt: 1.5 }}>{s.shownError('cardEnabled')}</Alert>}
        </SettingsSection>
        <SettingsSection id="appearance" icon={Palette} accent="amber" title={t('admin.billingSettings.s.appearance')} description={t('admin.billingSettings.s.appearanceBody')}>
          <FieldGrid>
            <TextField label={t('admin.billingSettings.f.checkoutDisplayName')} value={f.checkoutDisplayName} placeholder="eVyoog"
              onChange={(e) => s.set('checkoutDisplayName', e.target.value)} error={Boolean(s.shownError('checkoutDisplayName'))}
              helperText={s.shownError('checkoutDisplayName') ?? ' '} slotProps={{ htmlInput: { maxLength: 100 } }} />
            <TextField label={t('admin.billingSettings.f.checkoutThemeColor')} value={f.checkoutThemeColor} placeholder="#4C63FF"
              onChange={(e) => s.set('checkoutThemeColor', e.target.value)} error={Boolean(s.shownError('checkoutThemeColor'))}
              helperText={s.shownError('checkoutThemeColor') ?? t('admin.billingSettings.h.color')}
              slotProps={{ htmlInput: { maxLength: 7 }, input: { startAdornment: (
                <InputAdornment position="start">
                  <Box component="input" type="color" aria-label={t('admin.billingSettings.h.colorPicker')}
                    value={RE.color.test(f.checkoutThemeColor) ? f.checkoutThemeColor : '#4c63ff'}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => s.set('checkoutThemeColor', e.target.value.toUpperCase())}
                    sx={{ width: 28, height: 28, p: 0, border: 'none', background: 'none', cursor: 'pointer' }} />
                </InputAdornment>
              ) } }} />
            <TextField label={t('admin.billingSettings.f.checkoutDescription')} value={f.checkoutDescription} sx={{ gridColumn: '1 / -1' }}
              onChange={(e) => s.set('checkoutDescription', e.target.value)} error={Boolean(s.shownError('checkoutDescription'))}
              helperText={s.shownError('checkoutDescription') ?? t('admin.billingSettings.h.description')} slotProps={{ htmlInput: { maxLength: 255 } }} />
          </FieldGrid>
        </SettingsSection>
      </WithPreview>
      <SaveBar dirty={s.dirty} busy={s.busy} error={s.error} updatedAt={s.saved?.updatedAt}
        onCancel={s.reset} onSave={async () => { if (await s.submit()) onSaved() }} />
    </>
  )
}
