import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material'
import { taxApi, type CustomerTypeCondition, type TaxClass, type TaxPreview, type TaxReference } from '../../api/taxApi'
import { formatMoney } from '../../utils/money'
import { errorText, trimRate } from './taxFormat'

const CURRENCIES = ['INR', 'USD', 'EUR', 'GBP']

/** REQ-BIL-002.21: the internal rules applied to a made-up transaction, to check a rule before it is relied on. Creates nothing. */
export function TaxPreviewPanel({ reference, classes }: { reference: TaxReference; classes: TaxClass[] }) {
  const { t } = useTranslation()
  const [amount, setAmount] = useState('1000.00')
  const [currency, setCurrency] = useState('INR')
  const [country, setCountry] = useState(reference.supplierCountryCode ?? 'IN')
  const [state, setState] = useState('')
  const [taxClass, setTaxClass] = useState('')
  const [customerType, setCustomerType] = useState<CustomerTypeCondition>('INDIVIDUAL')
  const [taxId, setTaxId] = useState('')
  const [busy, setBusy] = useState(false)
  const [result, setResult] = useState<TaxPreview | null>(null)
  const [error, setError] = useState<string | null>(null)

  const states = reference.states[country] ?? []
  const valid = Number(amount) > 0 && country !== ''

  const run = async () => {
    setBusy(true)
    setError(null)
    try {
      setResult(await taxApi.preview({
        amount: Math.round(Number(amount) * 100), currency, country, state: state || undefined, productClassCode: taxClass || undefined,
        customerType, taxId: taxId.trim() || undefined,
      }))
    } catch (e) {
      setError(errorText(e, t('admin.tax.preview.error')))
      setResult(null)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Paper variant="outlined" component="section" aria-labelledby="tax-preview-title" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
      <Typography id="tax-preview-title" component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.preview.title')}</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('admin.tax.preview.description')}</Typography>
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, minmax(0, 1fr))' } }}>
        <TextField label={t('admin.tax.preview.amount')} value={amount} onChange={(e) => setAmount(e.target.value)} slotProps={{ htmlInput: { inputMode: 'decimal' } }} />
        <TextField select label={t('admin.tax.preview.currency')} value={currency} onChange={(e) => setCurrency(e.target.value)}>
          {CURRENCIES.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
        </TextField>
        <TextField select label={t('admin.tax.preview.country')} value={country} onChange={(e) => { setCountry(e.target.value); setState('') }}>
          {reference.countries.map((c) => <MenuItem key={c.code} value={c.code}>{c.name}</MenuItem>)}
        </TextField>
        {states.length > 0 ? (
          <TextField select label={t('admin.tax.preview.state')} value={state} onChange={(e) => setState(e.target.value)}>
            <MenuItem value="">—</MenuItem>
            {states.map((s) => <MenuItem key={s.code} value={s.code}>{s.name}</MenuItem>)}
          </TextField>
        ) : (
          <TextField label={t('admin.tax.preview.state')} value={state} onChange={(e) => setState(e.target.value)} />
        )}
        <TextField select label={t('admin.tax.preview.taxClass')} value={taxClass} onChange={(e) => setTaxClass(e.target.value)}>
          <MenuItem value="">—</MenuItem>
          {classes.map((c) => <MenuItem key={c.code} value={c.code}>{c.code}</MenuItem>)}
        </TextField>
        <TextField select label={t('admin.tax.preview.customerType')} value={customerType} onChange={(e) => setCustomerType(e.target.value as CustomerTypeCondition)}>
          {(['INDIVIDUAL', 'BUSINESS'] as const).map((c) => <MenuItem key={c} value={c}>{t(`admin.tax.customerType.${c}`)}</MenuItem>)}
        </TextField>
        <TextField label={t('admin.tax.preview.taxId')} value={taxId} onChange={(e) => setTaxId(e.target.value)} />
      </Box>
      <Button variant="contained" sx={{ mt: 2 }} onClick={() => void run()} disabled={busy || !valid}>{t('admin.tax.preview.run')}</Button>

      {error && <Alert severity="error" role="alert" sx={{ mt: 2 }}>{error}</Alert>}
      {result?.status === 'BLOCKED' && <Alert severity="warning" sx={{ mt: 2 }}>{t('admin.tax.preview.blocked', { message: result.blockMessage })}</Alert>}
      {result?.status === 'CALCULATED' && (
        <Box sx={{ mt: 2 }} data-testid="tax-preview-result">
          {result.placeOfSupply && <Typography variant="body2" sx={{ mb: 1 }}>{t('admin.tax.preview.placeOfSupply', { place: result.placeOfSupply })}</Typography>}
          <Table size="small" aria-label={t('admin.tax.preview.title')}>
            <TableHead><TableRow><TableCell>{t('admin.tax.ruleForm.componentName')}</TableCell><TableCell align="right">%</TableCell><TableCell align="right">{t('admin.tax.preview.totalTax')}</TableCell></TableRow></TableHead>
            <TableBody>
              {result.components.map((c, i) => (
                <TableRow key={`${c.code}-${i}`}>
                  <TableCell>{c.code === c.treatment ? t(`admin.tax.treatment.${c.treatment}`) : `${c.code} — ${c.name}`}</TableCell>
                  <TableCell align="right">{c.rate != null ? trimRate(c.rate) : '—'}</TableCell>
                  <TableCell align="right">{formatMoney(c.taxAmount, currency)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <Typography sx={{ mt: 1, fontWeight: 700 }}>{t('admin.tax.preview.totalTax')}: {formatMoney(result.totalTax, currency)} · {t('admin.tax.preview.total')}: {formatMoney(result.total, currency)}</Typography>
        </Box>
      )}
    </Paper>
  )
}
