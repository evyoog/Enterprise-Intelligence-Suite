import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem,
  Paper, Snackbar, Tab, Table, TableBody, TableCell, TableHead, TableRow, Tabs, TextField, Typography,
} from '@mui/material'
import { adminBillingApi, type Invoice, type OfflinePaymentMethod, type Payment } from '../../api/billingApi'
import { ApiError } from '../../api/client'
import { PageHeader } from '../../components/layout/PageHeader'

function money(minorUnits: number, currency: string) {
  return `${(minorUnits / 100).toFixed(2)} ${currency}`
}

function statusColor(status: string): 'success' | 'warning' | 'error' | 'default' {
  if (status === 'PAID' || status === 'CAPTURED') return 'success'
  if (status === 'OPEN' || status === 'CREATED') return 'warning'
  if (status === 'FAILED') return 'error'
  return 'default'
}

type TabKey = 'invoices' | 'payments'

/** "/admin/billing" — MANAGE_BILLING. REQ-BIL-001.9/.12. */
export function AdminBillingPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useState<TabKey>('invoices')

  return (
    <>
      <PageHeader title={t('admin.billing.title')} subtitle={t('admin.billing.subtitle')} />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="invoices" label={t('billing.tabs.invoices')} />
        <Tab value="payments" label={t('billing.tabs.history')} />
      </Tabs>
      {tab === 'invoices' && <InvoicesTab />}
      {tab === 'payments' && <PaymentsTab />}
    </>
  )
}

function InvoicesTab() {
  const { t } = useTranslation()
  const [invoices, setInvoices] = useState<Invoice[] | null>(null)
  const [recordTarget, setRecordTarget] = useState<Invoice | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const load = () => adminBillingApi.invoices().then((p) => setInvoices(p.content)).catch(() => setInvoices([]))
  useEffect(() => { load() }, [])

  return (
    <>
      {recordTarget && (
        <RecordOfflinePaymentDialog invoice={recordTarget} onClose={() => setRecordTarget(null)}
          onDone={(inv) => { setRecordTarget(null); setToast(t('admin.billing.offline.recorded', { number: inv.invoiceNumber })); load() }} />
      )}
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{t('billing.invoices.number')}</TableCell>
              <TableCell>{t('admin.billing.owner')}</TableCell>
              <TableCell>{t('billing.invoices.issued')}</TableCell>
              <TableCell>{t('billing.invoices.status')}</TableCell>
              <TableCell>{t('admin.billing.offline.route')}</TableCell>
              <TableCell align="right">{t('billing.invoices.total')}</TableCell>
              <TableCell align="right">{t('billing.invoices.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {invoices === null && <TableRow><TableCell colSpan={7}><CircularProgress size={20} /></TableCell></TableRow>}
            {invoices?.length === 0 && <TableRow><TableCell colSpan={7} sx={{ color: 'text.secondary' }}>{t('billing.invoices.empty')}</TableCell></TableRow>}
            {invoices?.map((inv) => (
              <TableRow key={inv.id}>
                <TableCell>{inv.invoiceNumber}</TableCell>
                <TableCell>{inv.ownerLabel}</TableCell>
                <TableCell>{new Date(inv.issuedAt).toLocaleDateString()}</TableCell>
                <TableCell><Chip size="small" label={inv.status} color={statusColor(inv.status)} /></TableCell>
                <TableCell>{inv.paymentRoute ? t(`admin.billing.routes.${inv.paymentRoute}`) : '—'}</TableCell>
                <TableCell align="right">{money(inv.total, inv.currency)}</TableCell>
                <TableCell align="right">
                  {/* C55: only an OPEN invoice the customer chose to pay by invoice. */}
                  {inv.status === 'OPEN' && inv.paymentRoute === 'OFFLINE' && (
                    <Button size="small" onClick={() => setRecordTarget(inv)}>{t('admin.billing.offline.action')}</Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast ? <Alert severity="success" variant="filled" onClose={() => setToast(null)}>{toast}</Alert> : undefined}
      </Snackbar>
    </>
  )
}

const OFFLINE_METHODS: OfflinePaymentMethod[] = ['BANK_TRANSFER', 'NEFT_RTGS', 'CHEQUE']

function todayIso() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** C55, REQ-BIL-001.20 (admin-billing.md "Record offline payment"). The
 * backend re-checks every rule; these checks only give earlier feedback. */
export function RecordOfflinePaymentDialog({ invoice, onClose, onDone }: {
  invoice: Invoice; onClose: () => void; onDone: (invoice: Invoice) => void
}) {
  const { t } = useTranslation()
  const [amount, setAmount] = useState((invoice.total / 100).toFixed(2))
  const [receivedOn, setReceivedOn] = useState(todayIso())
  const [method, setMethod] = useState<OfflinePaymentMethod | ''>('')
  const [reference, setReference] = useState('')
  const [note, setNote] = useState('')
  const [touched, setTouched] = useState(false)
  const [confirming, setConfirming] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const amountMinor = Math.round(parseFloat(amount || '0') * 100)
  const errors = {
    amount: !amount ? t('admin.billing.offline.required') : amountMinor !== invoice.total ? t('admin.billing.offline.amountMismatch') : null,
    receivedOn: !receivedOn ? t('admin.billing.offline.required') : receivedOn > todayIso() ? t('admin.billing.offline.dateFuture') : null,
    method: !method ? t('admin.billing.offline.required') : null,
    reference: !reference.trim() ? t('admin.billing.offline.required') : reference.length > 100 ? t('admin.billing.offline.tooLong') : null,
    note: note.length > 500 ? t('admin.billing.offline.tooLong') : null,
  }
  const valid = Object.values(errors).every((e) => !e)
  const shown = (key: keyof typeof errors) => (touched ? errors[key] : null)

  const submit = () => {
    if (!method) return
    setBusy(true)
    setError(null)
    adminBillingApi.recordOfflinePayment(invoice.id, {
      amount: amountMinor, receivedOn, method, reference: reference.trim(), note: note.trim() || undefined,
    })
      .then(onDone)
      .catch((e) => { setConfirming(false); setError(e instanceof ApiError ? e.message : t('checkout.genericError')) })
      .finally(() => setBusy(false))
  }

  return (
    <Dialog open onClose={onClose} aria-labelledby="record-offline-title">
      <DialogTitle id="record-offline-title">{t('admin.billing.offline.title')}</DialogTitle>
      <DialogContent sx={{ minWidth: { sm: 400 }, display: 'grid', gap: 2, pt: '8px !important' }}>
        {error && <Alert severity="error">{error}</Alert>}
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{invoice.invoiceNumber} · {invoice.ownerLabel}</Typography>
        {confirming ? (
          <Alert severity="warning">{t('admin.billing.offline.confirm', { number: invoice.invoiceNumber })}</Alert>
        ) : (
          <>
            <TextField label={t('admin.billing.offline.amount')} type="number" value={amount} required
              onChange={(e) => setAmount(e.target.value)} error={Boolean(shown('amount'))}
              helperText={shown('amount') ?? t('admin.billing.offline.amountHelp', { amount: money(invoice.total, invoice.currency) })}
              slotProps={{ htmlInput: { min: 0, step: '0.01' } }} />
            <TextField label={t('admin.billing.offline.date')} type="date" value={receivedOn} required
              onChange={(e) => setReceivedOn(e.target.value)} error={Boolean(shown('receivedOn'))} helperText={shown('receivedOn')}
              slotProps={{ inputLabel: { shrink: true }, htmlInput: { max: todayIso() } }} />
            <TextField select label={t('admin.billing.offline.method')} value={method} required
              onChange={(e) => setMethod(e.target.value as OfflinePaymentMethod)} error={Boolean(shown('method'))} helperText={shown('method')}>
              {OFFLINE_METHODS.map((m) => <MenuItem key={m} value={m}>{t(`admin.billing.offline.methods.${m}`)}</MenuItem>)}
            </TextField>
            <TextField label={t('admin.billing.offline.reference')} value={reference} required
              onChange={(e) => setReference(e.target.value)} error={Boolean(shown('reference'))} helperText={shown('reference')}
              slotProps={{ htmlInput: { maxLength: 100 } }} />
            <TextField label={t('admin.billing.offline.note')} value={note} multiline minRows={2}
              onChange={(e) => setNote(e.target.value)} error={Boolean(shown('note'))} helperText={shown('note')}
              slotProps={{ htmlInput: { maxLength: 500 } }} />
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={confirming ? () => setConfirming(false) : onClose}>{t('common.cancel')}</Button>
        {confirming ? (
          <Button variant="contained" disabled={busy} onClick={submit}>{t('admin.billing.offline.confirmAction')}</Button>
        ) : (
          <Button variant="contained" onClick={() => { setTouched(true); if (valid) setConfirming(true) }}>
            {t('admin.billing.offline.submit')}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  )
}

function PaymentsTab() {
  const { t } = useTranslation()
  const [payments, setPayments] = useState<Payment[] | null>(null)
  const [selected, setSelected] = useState<Payment | null>(null)
  const load = () => adminBillingApi.payments().then((p) => setPayments(p.content)).catch(() => setPayments([]))
  useEffect(() => { load() }, [])

  return (
    <>
      <RefundDialog payment={selected} onClose={() => setSelected(null)} onDone={() => { setSelected(null); load() }} />
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{t('billing.history.date')}</TableCell>
              <TableCell>{t('admin.billing.owner')}</TableCell>
              <TableCell>{t('billing.invoices.number')}</TableCell>
              <TableCell align="right">{t('billing.invoices.total')}</TableCell>
              <TableCell>{t('billing.invoices.status')}</TableCell>
              <TableCell align="right">{t('billing.invoices.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {payments === null && <TableRow><TableCell colSpan={6}><CircularProgress size={20} /></TableCell></TableRow>}
            {payments?.length === 0 && <TableRow><TableCell colSpan={6} sx={{ color: 'text.secondary' }}>{t('billing.history.empty')}</TableCell></TableRow>}
            {payments?.map((p) => (
              <TableRow key={p.id}>
                <TableCell>{new Date(p.createdAt).toLocaleString()}</TableCell>
                <TableCell>{p.ownerLabel}</TableCell>
                <TableCell>{p.invoiceNumber}</TableCell>
                <TableCell align="right">{money(p.amount, p.currency)}</TableCell>
                <TableCell><Chip size="small" label={p.status} color={statusColor(p.status)} /></TableCell>
                <TableCell align="right">
                  {/* C55: offline payments are settled outside Razorpay — no gateway refund or reconcile. */}
                  {p.methodType !== 'OFFLINE' && (p.status === 'CAPTURED' || p.status === 'PARTIALLY_REFUNDED') && (
                    <Button size="small" onClick={() => setSelected(p)}>{t('admin.billing.refund.action')}</Button>
                  )}
                  {p.methodType !== 'OFFLINE' && (
                    <Button size="small" onClick={() => adminBillingApi.reconcile(p.id).then(load)}>{t('admin.billing.reconcile')}</Button>
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

function RefundDialog({ payment, onClose, onDone }: { payment: Payment | null; onClose: () => void; onDone: () => void }) {
  const { t } = useTranslation()
  const [amount, setAmount] = useState('')
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)

  if (!payment) return null
  const refundable = payment.amount - payment.refundedAmount

  const submit = () => {
    const amountMinor = Math.round(parseFloat(amount || '0') * 100)
    adminBillingApi.refund(payment.id, amountMinor, reason)
      .then(onDone)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not process the refund.'))
  }

  return (
    <Dialog open onClose={onClose}>
      <DialogTitle>{t('admin.billing.refund.title')}</DialogTitle>
      <DialogContent sx={{ minWidth: 320, display: 'grid', gap: 2, pt: 1 }}>
        {error && <Alert severity="error">{error}</Alert>}
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
          {t('admin.billing.refund.refundable', { amount: money(refundable, payment.currency) })}
        </Typography>
        <TextField label={t('admin.billing.refund.amount')} type="number" value={amount} onChange={(e) => setAmount(e.target.value)} required />
        <TextField label={t('admin.billing.refund.reason')} value={reason} onChange={(e) => setReason(e.target.value)} required multiline minRows={2} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{t('common.cancel')}</Button>
        <Button variant="contained" color="error" disabled={!amount || !reason} onClick={submit}>
          {t('admin.billing.refund.confirm')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
