import { Wallet as PHWallet } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControlLabel, Paper, Radio, RadioGroup, Tab, Table, TableBody, TableCell, TableHead,
  TableRow, Tabs, TextField, Typography,
} from '@mui/material'
import {
  myBillingApi, organizationBillingApi,
  type BillingOverview, type Invoice, type PaymentMethod, type PaymentMethodType,
} from '../api/billingApi'
import { ApiError } from '../api/client'
import { PageHeader } from '../components/layout/PageHeader'
import { openRazorpayCheckout } from '../utils/razorpayCheckout'

type BillingScope = typeof myBillingApi

function money(minorUnits: number, currency: string) {
  return `${(minorUnits / 100).toFixed(2)} ${currency}`
}

function statusColor(status: string): 'success' | 'warning' | 'error' | 'default' {
  if (status === 'PAID' || status === 'CAPTURED') return 'success'
  if (status === 'OPEN' || status === 'CREATED') return 'warning'
  if (status === 'FAILED') return 'error'
  return 'default'
}

type TabKey = 'overview' | 'invoices' | 'methods' | 'history' | 'details'

function BillingScreen({ api }: { api: BillingScope }) {
  const { t } = useTranslation()
  const [tab, setTab] = useState<TabKey>('overview')
  const [overview, setOverview] = useState<BillingOverview | null>(null)
  const [reloadToken, setReloadToken] = useState(0)

  useEffect(() => {
    api.overview().then(setOverview).catch(() => {})
  }, [api, reloadToken])

  const reload = () => setReloadToken((n) => n + 1)

  return (
    <>
      <PageHeader icon={PHWallet} accent="blue" title={t('billing.title')} subtitle={t('billing.subtitle')} />
      {overview && !overview.gatewayConfigured && (
        <Alert severity="info" sx={{ mb: 2 }}>{t('billing.gateway.notConfigured')}</Alert>
      )}
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="overview" label={t('billing.tabs.overview')} />
        <Tab value="invoices" label={t('billing.tabs.invoices')} />
        <Tab value="methods" label={t('billing.tabs.methods')} />
        <Tab value="history" label={t('billing.tabs.history')} />
        <Tab value="details" label={t('billing.tabs.details')} />
      </Tabs>

      {tab === 'overview' && <OverviewTab overview={overview} onGoToInvoices={() => setTab('invoices')} onGoToMethods={() => setTab('methods')} />}
      {tab === 'invoices' && <InvoicesTab api={api} />}
      {tab === 'methods' && <PaymentMethodsTab api={api} gatewayConfigured={overview?.gatewayConfigured ?? false} onChanged={reload} />}
      {tab === 'history' && <PaymentHistoryTab api={api} />}
      {tab === 'details' && <BillingDetailsTab api={api} />}
    </>
  )
}

function OverviewTab({ overview, onGoToInvoices, onGoToMethods }: {
  overview: BillingOverview | null
  onGoToInvoices: () => void
  onGoToMethods: () => void
}) {
  const { t } = useTranslation()
  if (!overview) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress size={28} /></Box>

  const dueEntries = Object.entries(overview.amountDueByCurrency)
  const spentEntries = Object.entries(overview.spentThisPeriodByCurrency)

  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 2 }}>
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('billing.overview.amountDue')}</Typography>
        <Typography variant="h6" component="p" sx={{ fontWeight: 700 }}>
          {dueEntries.length === 0 ? '0.00' : dueEntries.map(([cur, amt]) => money(amt, cur)).join(', ')}
        </Typography>
        {dueEntries.length > 0 && <Button size="small" sx={{ mt: 1 }} onClick={onGoToInvoices}>{t('billing.overview.viewInvoices')}</Button>}
      </Paper>
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('billing.overview.spentThisPeriod')}</Typography>
        <Typography variant="h6" component="p" sx={{ fontWeight: 700 }}>
          {spentEntries.length === 0 ? '0.00' : spentEntries.map(([cur, amt]) => money(amt, cur)).join(', ')}
        </Typography>
      </Paper>
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('billing.overview.defaultMethod')}</Typography>
        {overview.defaultPaymentMethod ? (
          <Typography sx={{ fontWeight: 600 }}>
            {overview.defaultPaymentMethod.type} •••• {overview.defaultPaymentMethod.last4}
          </Typography>
        ) : (
          <>
            <Typography sx={{ color: 'text.secondary' }}>{t('billing.overview.noneSaved')}</Typography>
            <Button size="small" sx={{ mt: 1 }} onClick={onGoToMethods}>{t('billing.overview.addMethod')}</Button>
          </>
        )}
      </Paper>
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('billing.overview.nextInvoice')}</Typography>
        <Typography sx={{ fontWeight: 600 }}>{overview.nextInvoiceDate ?? t('billing.overview.noneUpcoming')}</Typography>
      </Paper>

      <Box sx={{ gridColumn: '1 / -1' }}>
        <Typography sx={{ fontWeight: 700, mb: 1 }}>{t('billing.overview.recentInvoices')}</Typography>
        <Paper variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>{t('billing.invoices.number')}</TableCell>
                <TableCell>{t('billing.invoices.status')}</TableCell>
                <TableCell align="right">{t('billing.invoices.total')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {overview.recentInvoices.length === 0 && (
                <TableRow><TableCell colSpan={3} sx={{ color: 'text.secondary' }}>{t('billing.invoices.empty')}</TableCell></TableRow>
              )}
              {overview.recentInvoices.map((inv) => (
                <TableRow key={inv.id}>
                  <TableCell>{inv.invoiceNumber}</TableCell>
                  <TableCell><Chip size="small" label={inv.status} color={statusColor(inv.status)} /></TableCell>
                  <TableCell align="right">{money(inv.total, inv.currency)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      </Box>
    </Box>
  )
}

function InvoicesTab({ api }: { api: BillingScope }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [invoices, setInvoices] = useState<Invoice[] | null>(null)
  // C55: Pay opens the checkout screen, which offers every payment option
  // (online options are disabled there when the gateway is not configured).
  const payInvoice = (inv: Invoice) => navigate(
    `/checkout?invoiceId=${inv.id}${api === organizationBillingApi ? '&scope=organization' : ''}`)

  const load = () => api.invoices().then((p) => setInvoices(p.content)).catch(() => setInvoices([]))
  // eslint-disable-next-line react-hooks/exhaustive-deps -- load is redefined each render from api; api is the real dependency
  useEffect(() => { load() }, [api])

  return (
    <>
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{t('billing.invoices.number')}</TableCell>
              <TableCell>{t('billing.invoices.issued')}</TableCell>
              <TableCell>{t('billing.invoices.status')}</TableCell>
              <TableCell align="right">{t('billing.invoices.total')}</TableCell>
              <TableCell align="right">{t('billing.invoices.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {invoices === null && (
              <TableRow><TableCell colSpan={5}><CircularProgress size={20} /></TableCell></TableRow>
            )}
            {invoices?.length === 0 && (
              <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>{t('billing.invoices.empty')}</TableCell></TableRow>
            )}
            {invoices?.map((inv) => (
              <TableRow key={inv.id}>
                <TableCell>{inv.invoiceNumber}</TableCell>
                <TableCell>{new Date(inv.issuedAt).toLocaleDateString()}</TableCell>
                <TableCell><Chip size="small" label={inv.status} color={statusColor(inv.status)} /></TableCell>
                <TableCell align="right">{money(inv.total, inv.currency)}</TableCell>
                <TableCell align="right">
                  {inv.status === 'OPEN' && (
                    <Button size="small" onClick={() => payInvoice(inv)}>{t('billing.invoices.pay')}</Button>
                  )}
                  <Button size="small" onClick={() => api.downloadDocument(inv.id, 'invoice')}>{t('billing.invoices.downloadInvoice')}</Button>
                  {(inv.status === 'PAID' || inv.status === 'PARTIALLY_REFUNDED' || inv.status === 'REFUNDED') && (
                    <Button size="small" onClick={() => api.downloadDocument(inv.id, 'receipt')}>{t('billing.invoices.downloadReceipt')}</Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </>
  )
}

function PaymentMethodsTab({ api, gatewayConfigured, onChanged }: { api: BillingScope; gatewayConfigured: boolean; onChanged: () => void }) {
  const { t } = useTranslation()
  const [methods, setMethods] = useState<PaymentMethod[] | null>(null)
  const [addOpen, setAddOpen] = useState(false)

  const load = () => api.paymentMethods().then(setMethods).catch(() => setMethods([]))
  // eslint-disable-next-line react-hooks/exhaustive-deps -- load is redefined each render from api; api is the real dependency
  useEffect(() => { load() }, [api])

  return (
    <>
      <AddPaymentMethodDialog api={api} open={addOpen} onClose={() => setAddOpen(false)} onSaved={() => { setAddOpen(false); load(); onChanged() }} />
      <Button variant="contained" size="small" disabled={!gatewayConfigured} sx={{ mb: 2 }} onClick={() => setAddOpen(true)}>
        {t('billing.methods.add')}
      </Button>
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{t('billing.methods.type')}</TableCell>
              <TableCell>{t('billing.methods.details')}</TableCell>
              <TableCell>{t('billing.methods.status')}</TableCell>
              <TableCell align="right">{t('billing.invoices.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {methods?.length === 0 && (
              <TableRow><TableCell colSpan={4} sx={{ color: 'text.secondary' }}>{t('billing.methods.empty')}</TableCell></TableRow>
            )}
            {methods?.map((m) => (
              <TableRow key={m.id}>
                <TableCell>{m.type}</TableCell>
                <TableCell>{m.type === 'CARD' ? `${m.network ?? ''} •••• ${m.last4 ?? ''}` : m.upiMasked}</TableCell>
                <TableCell>
                  {m.isDefault && <Chip size="small" label={t('billing.methods.default')} color="primary" sx={{ mr: 0.5 }} />}
                  {m.expired && <Chip size="small" label={t('billing.methods.expired')} color="error" />}
                </TableCell>
                <TableCell align="right">
                  {!m.isDefault && !m.expired && (
                    <Button size="small" onClick={() => api.setDefaultPaymentMethod(m.id).then(() => { load(); onChanged() })}>
                      {t('billing.methods.setDefault')}
                    </Button>
                  )}
                  <Button size="small" color="error" onClick={() => api.removePaymentMethod(m.id).then(() => { load(); onChanged() })}>
                    {t('billing.methods.remove')}
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </>
  )
}

function AddPaymentMethodDialog({ api, open, onClose, onSaved }: {
  api: BillingScope; open: boolean; onClose: () => void; onSaved: () => void
}) {
  const { t } = useTranslation()
  const [type, setType] = useState<PaymentMethodType>('CARD')
  const [consent, setConsent] = useState(false)
  const [makeDefault, setMakeDefault] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const canContinue = type === 'UPI' || consent

  const start = async () => {
    setBusy(true)
    setError(null)
    try {
      const setup = await api.setupPaymentMethod(type, consent, makeDefault)
      const result = await openRazorpayCheckout({
        keyId: setup.keyId, orderId: setup.providerOrderId, amount: setup.amount, currency: setup.currency,
        name: 'eVyoog', description: 'Save payment method',
      })
      await api.confirmPaymentMethodSetup({ ...result, type, consent, makeDefault })
      onSaved()
    } catch (e) {
      if (e instanceof Error && e.message === 'cancelled') {
        setError(t('billing.addMethod.cancelled'))
      } else {
        setError(e instanceof ApiError ? e.message : t('billing.addMethod.failed'))
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{t('billing.addMethod.title')}</DialogTitle>
      <DialogContent sx={{ minWidth: 320 }}>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <RadioGroup value={type} onChange={(e) => setType(e.target.value as PaymentMethodType)}>
          <FormControlLabel value="CARD" control={<Radio />} label={t('billing.addMethod.card')} />
          <FormControlLabel value="UPI" control={<Radio />} label={t('billing.addMethod.upi')} />
        </RadioGroup>
        {type === 'CARD' && (
          <FormControlLabel
            control={<Radio checked={consent} onClick={() => setConsent(!consent)} />}
            label={t('billing.addMethod.consent')}
          />
        )}
        <FormControlLabel
          control={<Radio checked={makeDefault} onClick={() => setMakeDefault(!makeDefault)} />}
          label={t('billing.addMethod.makeDefault')}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{t('common.cancel')}</Button>
        <Button variant="contained" disabled={!canContinue || busy} onClick={start}>{t('billing.addMethod.continue')}</Button>
      </DialogActions>
    </Dialog>
  )
}

function PaymentHistoryTab({ api }: { api: BillingScope }) {
  const { t } = useTranslation()
  const [payments, setPayments] = useState<import('../api/billingApi').Payment[] | null>(null)
  useEffect(() => { api.payments().then((p) => setPayments(p.content)).catch(() => setPayments([])) }, [api])

  return (
    <Paper variant="outlined">
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>{t('billing.history.date')}</TableCell>
            <TableCell>{t('billing.invoices.number')}</TableCell>
            <TableCell align="right">{t('billing.invoices.total')}</TableCell>
            <TableCell>{t('billing.methods.type')}</TableCell>
            <TableCell>{t('billing.invoices.status')}</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {payments?.length === 0 && (
            <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>{t('billing.history.empty')}</TableCell></TableRow>
          )}
          {payments?.map((p) => (
            <TableRow key={p.id}>
              <TableCell>{new Date(p.createdAt).toLocaleString()}</TableCell>
              <TableCell>{p.invoiceNumber}</TableCell>
              <TableCell align="right">{money(p.amount, p.currency)}</TableCell>
              <TableCell>{p.methodType ? `${p.methodType} •••• ${p.methodLast4 ?? ''}` : '—'}</TableCell>
              <TableCell><Chip size="small" label={p.status} color={statusColor(p.status)} /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  )
}

function BillingDetailsTab({ api }: { api: BillingScope }) {
  const { t } = useTranslation()
  const [form, setForm] = useState({
    billingName: '', billingEmail: '', addressLine1: '', addressLine2: '', city: '', state: '',
    postalCode: '', country: '', taxId: '',
  })
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.getDetails().then((d) => {
      if (d) setForm({ ...d, addressLine2: d.addressLine2 ?? '', taxId: d.taxId ?? '' })
    }).catch(() => {})
  }, [api])

  const set = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((f) => ({ ...f, [field]: e.target.value }))
    setSaved(false)
  }

  const save = () => {
    setError(null)
    api.saveDetails(form).then(() => setSaved(true)).catch((e) => setError(e instanceof ApiError ? e.message : 'Could not save.'))
  }

  return (
    <Paper variant="outlined" sx={{ p: 3, maxWidth: 560 }}>
      {saved && <Alert severity="success" sx={{ mb: 2 }}>{t('billing.details.saved')}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Box sx={{ display: 'grid', gap: 2 }}>
        <TextField label={t('billing.details.name')} value={form.billingName} onChange={set('billingName')} required />
        <TextField label={t('billing.details.email')} value={form.billingEmail} onChange={set('billingEmail')} required type="email" />
        <TextField label={t('billing.details.address1')} value={form.addressLine1} onChange={set('addressLine1')} required />
        <TextField label={t('billing.details.address2')} value={form.addressLine2} onChange={set('addressLine2')} />
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField label={t('billing.details.city')} value={form.city} onChange={set('city')} required fullWidth />
          <TextField label={t('billing.details.state')} value={form.state} onChange={set('state')} required fullWidth />
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField label={t('billing.details.postalCode')} value={form.postalCode} onChange={set('postalCode')} required fullWidth />
          <TextField label={t('billing.details.country')} value={form.country} onChange={set('country')} required fullWidth />
        </Box>
        <TextField label={t('billing.details.taxId')} value={form.taxId} onChange={set('taxId')} />
        <Box>
          <Button variant="contained" onClick={save}>{t('common.save')}</Button>
        </Box>
      </Box>
    </Paper>
  )
}

/** "/billing" — an individual customer's own billing (REQ-BIL-001). */
export function BillingPage() {
  return <BillingScreen api={myBillingApi} />
}

/** "/organization/billing" — the organization's billing (REQ-BIL-001,
 * FRD Open question 5: organization admins only, MANAGE_ORGANIZATION). */
export function OrganizationBillingPage() {
  return <BillingScreen api={organizationBillingApi} />
}
