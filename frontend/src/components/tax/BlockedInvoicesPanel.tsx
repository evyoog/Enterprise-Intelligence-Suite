import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, CircularProgress, Paper, Skeleton, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import { taxApi, type BlockedInvoice } from '../../api/taxApi'
import { formatMoney } from '../../utils/money'
import { EmptyState } from '../ui/EmptyState'
import { errorText } from './taxFormat'

/** REQ-BIL-002.42: Draft invoices (tax could not be determined) with the reason, and Retry tax. */
export function BlockedInvoicesPanel({ onChanged, onToast }: { onChanged: () => void; onToast: (message: string) => void }) {
  const { t } = useTranslation()
  const [rows, setRows] = useState<BlockedInvoice[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [note, setNote] = useState<string | null>(null)

  const load = useCallback(() => {
    return taxApi.blockedInvoices().then((r) => { setRows(r); setError(null) }).catch((e) => setError(errorText(e, t('admin.tax.loadError'))))
  }, [t])
  useEffect(() => { void load() }, [load])

  const retry = async (invoiceId: number) => {
    setBusyId(invoiceId)
    setNote(null)
    try {
      const invoice = await taxApi.retryBlockedInvoice(invoiceId)
      if (invoice.status === 'DRAFT') {
        setNote(t('admin.tax.blocked.stillBlocked', { reason: invoice.taxBlockReason ?? invoice.taxBlockCode ?? '' }))
      } else {
        onToast(t('admin.tax.blocked.issued', { number: invoice.invoiceNumber }))
      }
      void load()
      onChanged()
    } catch (e) {
      setError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
      <Typography component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.blocked.title')}</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('admin.tax.blocked.description')}</Typography>
      {note && <Alert severity="warning" sx={{ mb: 2 }} onClose={() => setNote(null)}>{note}</Alert>}
      {error && <Alert severity="error" role="alert" action={<Button onClick={() => void load()}>{t('admin.tax.retry')}</Button>} sx={{ mb: 2 }}>{error}</Alert>}
      {!error && rows === null && <Skeleton variant="rounded" height={100} />}
      {rows && rows.length === 0 && <EmptyState title={t('admin.tax.blocked.title')} description={t('admin.tax.blocked.empty')} />}
      {rows && rows.length > 0 && (
        <Table size="small" aria-label={t('admin.tax.blocked.title')}>
          <TableHead>
            <TableRow>{(['invoice', 'owner', 'amount', 'reason', 'engine', 'actions'] as const).map((c) => <TableCell key={c}>{t(`admin.tax.blocked.columns.${c}`)}</TableCell>)}</TableRow>
          </TableHead>
          <TableBody>
            {rows.map((r) => (
              <TableRow key={r.invoiceId}>
                <TableCell>{t('admin.tax.blocked.draftNumber', { id: r.invoiceId })}</TableCell>
                <TableCell>{r.ownerLabel ?? '—'}</TableCell>
                <TableCell>{formatMoney(r.subtotal, r.currency)}</TableCell>
                <TableCell>
                  <Box sx={{ fontWeight: 600 }}>{r.blockCode ? t(`admin.tax.blocked.codes.${r.blockCode}`, { defaultValue: r.blockCode }) : '—'}</Box>
                  {r.blockReason && <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', maxWidth: 420 }}>{r.blockReason}</Typography>}
                </TableCell>
                <TableCell>{r.taxEngine ? t(r.taxEngine === 'EXTERNAL' ? 'admin.tax.engine.external' : 'admin.tax.engine.internal') : '—'}</TableCell>
                <TableCell>
                  <Button size="small" variant="outlined" onClick={() => void retry(r.invoiceId)} disabled={busyId === r.invoiceId}
                    startIcon={busyId === r.invoiceId ? <CircularProgress size={14} /> : undefined}>
                    {t(busyId === r.invoiceId ? 'admin.tax.blocked.retrying' : 'admin.tax.blocked.retry')}
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Paper>
  )
}
