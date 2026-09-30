import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle,
  Paper, Tab, Table, TableBody, TableCell, TableHead, TableRow, Tabs, TextField, Typography,
} from '@mui/material'
import { adminBillingApi, type Invoice, type Payment } from '../../api/billingApi'
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
  useEffect(() => { adminBillingApi.invoices().then((p) => setInvoices(p.content)).catch(() => setInvoices([])) }, [])

  return (
    <Paper variant="outlined">
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>{t('billing.invoices.number')}</TableCell>
            <TableCell>{t('admin.billing.owner')}</TableCell>
            <TableCell>{t('billing.invoices.issued')}</TableCell>
            <TableCell>{t('billing.invoices.status')}</TableCell>
            <TableCell align="right">{t('billing.invoices.total')}</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {invoices === null && <TableRow><TableCell colSpan={5}><CircularProgress size={20} /></TableCell></TableRow>}
          {invoices?.length === 0 && <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>{t('billing.invoices.empty')}</TableCell></TableRow>}
          {invoices?.map((inv) => (
            <TableRow key={inv.id}>
              <TableCell>{inv.invoiceNumber}</TableCell>
              <TableCell>{inv.ownerLabel}</TableCell>
              <TableCell>{new Date(inv.issuedAt).toLocaleDateString()}</TableCell>
              <TableCell><Chip size="small" label={inv.status} color={statusColor(inv.status)} /></TableCell>
              <TableCell align="right">{money(inv.total, inv.currency)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
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
                  {(p.status === 'CAPTURED' || p.status === 'PARTIALLY_REFUNDED') && (
                    <Button size="small" onClick={() => setSelected(p)}>{t('admin.billing.refund.action')}</Button>
                  )}
                  <Button size="small" onClick={() => adminBillingApi.reconcile(p.id).then(load)}>{t('admin.billing.reconcile')}</Button>
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
