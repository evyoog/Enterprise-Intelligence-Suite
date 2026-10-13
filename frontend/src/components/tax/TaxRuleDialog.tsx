import { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Checkbox, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, IconButton,
  MenuItem, TextField, Typography,
} from '@mui/material'
import { Plus, Trash2 } from 'lucide-react'
import {
  taxApi, type CustomerTypeCondition, type SaveTaxRuleInput, type SupplyType, type TaxClass, type TaxIdCondition, type TaxReference,
  type TaxRule, type TaxTreatment, type TaxType,
} from '../../api/taxApi'
import { errorText, trimRate } from './taxFormat'

interface ComponentRow { code: string; name: string; rate: string; compound: boolean }

const EMPTY_COMPONENT: ComponentRow = { code: '', name: '', rate: '', compound: false }
const RATE_PATTERN = /^\d{1,3}(\.\d{1,4})?$/

function today() {
  return new Date().toISOString().slice(0, 10)
}

/** REQ-BIL-002.10/.11: create or edit a Draft (or Rejected) rule, with its tax components. The server validates again. */
export function TaxRuleDialog({ rule, reference, classes, onClose, onSaved }: {
  rule: TaxRule | null
  reference: TaxReference
  classes: TaxClass[]
  onClose: () => void
  onSaved: (rule: TaxRule) => void
}) {
  const { t } = useTranslation()
  const [name, setName] = useState(rule?.name ?? '')
  const [country, setCountry] = useState(rule?.countryCode ?? reference.supplierCountryCode ?? 'IN')
  const [state, setState] = useState(rule?.stateCode ?? '')
  const [jurisdiction, setJurisdiction] = useState(rule?.jurisdiction ?? '')
  const [taxType, setTaxType] = useState<TaxType>(rule?.taxType ?? 'GST')
  const [taxClass, setTaxClass] = useState(rule?.productClassCode ?? '')
  const [customerType, setCustomerType] = useState<CustomerTypeCondition>(rule?.customerType ?? 'ANY')
  const [supplyType, setSupplyType] = useState<SupplyType>(rule?.supplyType ?? 'ANY')
  const [taxIdCondition, setTaxIdCondition] = useState<TaxIdCondition>(rule?.taxIdCondition ?? 'ANY')
  const [treatment, setTreatment] = useState<TaxTreatment>(rule?.treatment ?? 'TAXABLE')
  const [from, setFrom] = useState(rule?.effectiveFrom ?? today())
  const [to, setTo] = useState(rule?.effectiveTo ?? '')
  const [notes, setNotes] = useState(rule?.notes ?? '')
  const [components, setComponents] = useState<ComponentRow[]>(
    rule?.components.length ? rule.components.map((c) => ({ code: c.code, name: c.name, rate: trimRate(c.rate), compound: c.compound })) : [{ ...EMPTY_COMPONENT }])
  const [submitted, setSubmitted] = useState(false)
  const [busy, setBusy] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  const states = reference.states[country] ?? []

  const taxable = treatment === 'TAXABLE'
  const errors = useMemo(() => {
    const e: Record<string, string> = {}
    if (!name.trim()) e.name = t('admin.tax.ruleForm.errors.name')
    if (!country) e.country = t('admin.tax.ruleForm.errors.country')
    if (!from) e.from = t('admin.tax.ruleForm.errors.from')
    if (from && to && to < from) e.to = t('admin.tax.ruleForm.errors.to')
    if (taxable) {
      if (components.length === 0) e.components = t('admin.tax.ruleForm.errors.components')
      const codes = components.map((c) => c.code.trim().toUpperCase())
      if (codes.some((c) => !/^[A-Z0-9_]{1,20}$/.test(c))) e.code = t('admin.tax.ruleForm.errors.code')
      else if (new Set(codes).size !== codes.length) e.code = t('admin.tax.ruleForm.errors.duplicate')
      if (components.some((c) => !c.name.trim())) e.componentName = t('admin.tax.ruleForm.errors.componentName')
      if (components.some((c) => !RATE_PATTERN.test(c.rate.trim()) || Number(c.rate) > 100)) e.rate = t('admin.tax.ruleForm.errors.rate')
    }
    return e
  }, [name, country, from, to, taxable, components, t])

  const totalRate = taxable && !errors.rate ? components.reduce((sum, c) => sum + Number(c.rate || 0), 0) : null

  const setComponent = (index: number, patch: Partial<ComponentRow>) =>
    setComponents((rows) => rows.map((r, i) => (i === index ? { ...r, ...patch } : r)))

  const save = async () => {
    setSubmitted(true)
    if (Object.keys(errors).length > 0) return
    const body: SaveTaxRuleInput = {
      name: name.trim(), countryCode: country, stateCode: state || undefined, jurisdiction: jurisdiction.trim() || undefined, taxType,
      productClassCode: taxClass || undefined, customerType, supplyType, taxIdCondition, treatment, effectiveFrom: from,
      effectiveTo: to || undefined, notes: notes.trim() || undefined,
      components: taxable ? components.map((c) => ({ code: c.code.trim().toUpperCase(), name: c.name.trim(), rate: Number(c.rate), compound: c.compound })) : [],
    }
    setBusy(true)
    setServerError(null)
    try {
      onSaved(rule ? await taxApi.updateRule(rule.id, body) : await taxApi.createRule(body))
    } catch (e) {
      setServerError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setBusy(false)
    }
  }

  const show = (key: string) => (submitted ? errors[key] : undefined)

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="tax-rule-title" maxWidth="md" fullWidth>
      <DialogTitle id="tax-rule-title">{t(rule ? 'admin.tax.ruleForm.editTitle' : 'admin.tax.ruleForm.createTitle')}</DialogTitle>
      <DialogContent dividers>
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 1fr))' }, pt: 0.5 }}>
          <TextField label={t('admin.tax.ruleForm.name')} value={name} onChange={(e) => setName(e.target.value)} required
            error={Boolean(show('name'))} helperText={show('name')} sx={{ gridColumn: { sm: '1 / -1' } }} />
          <TextField select label={t('admin.tax.ruleForm.country')} value={country} onChange={(e) => { setCountry(e.target.value); setState('') }} required
            error={Boolean(show('country'))} helperText={show('country')}>
            {reference.countries.map((c) => <MenuItem key={c.code} value={c.code}>{c.name}</MenuItem>)}
          </TextField>
          {states.length > 0 ? (
            <TextField select label={t('admin.tax.ruleForm.state')} value={state} onChange={(e) => setState(e.target.value)} helperText={t('admin.tax.ruleForm.stateHelp')}>
              <MenuItem value="">{t('admin.tax.rules.anywhere')}</MenuItem>
              {states.map((s) => <MenuItem key={s.code} value={s.code}>{s.name}{s.gstCode ? ` (${s.gstCode})` : ''}</MenuItem>)}
            </TextField>
          ) : (
            <TextField label={t('admin.tax.ruleForm.state')} value={state} onChange={(e) => setState(e.target.value)} helperText={t('admin.tax.ruleForm.stateHelp')} />
          )}
          <TextField label={t('admin.tax.ruleForm.jurisdiction')} value={jurisdiction} onChange={(e) => setJurisdiction(e.target.value)} />
          <TextField select label={t('admin.tax.ruleForm.taxType')} value={taxType} onChange={(e) => setTaxType(e.target.value as TaxType)}>
            {reference.taxTypes.map((v) => <MenuItem key={v} value={v}>{t(`admin.tax.taxType.${v}`)}</MenuItem>)}
          </TextField>
          <TextField select label={t('admin.tax.ruleForm.taxClass')} value={taxClass} onChange={(e) => setTaxClass(e.target.value)}>
            <MenuItem value="">{t('admin.tax.ruleForm.anyClass')}</MenuItem>
            {classes.filter((c) => c.enabled || c.code === taxClass).map((c) => <MenuItem key={c.code} value={c.code}>{c.code} — {c.name}</MenuItem>)}
          </TextField>
          <TextField select label={t('admin.tax.ruleForm.treatment')} value={treatment} onChange={(e) => setTreatment(e.target.value as TaxTreatment)}>
            {reference.treatments.map((v) => <MenuItem key={v} value={v}>{t(`admin.tax.treatment.${v}`)}</MenuItem>)}
          </TextField>
          <TextField select label={t('admin.tax.ruleForm.supplyType')} value={supplyType} onChange={(e) => setSupplyType(e.target.value as SupplyType)}>
            {reference.supplyTypes.map((v) => <MenuItem key={v} value={v}>{t(`admin.tax.supplyType.${v}`)}</MenuItem>)}
          </TextField>
          <TextField select label={t('admin.tax.ruleForm.customerType')} value={customerType} onChange={(e) => setCustomerType(e.target.value as CustomerTypeCondition)}>
            {reference.customerTypes.map((v) => <MenuItem key={v} value={v}>{t(`admin.tax.customerType.${v}`)}</MenuItem>)}
          </TextField>
          <TextField select label={t('admin.tax.ruleForm.taxIdCondition')} value={taxIdCondition} onChange={(e) => setTaxIdCondition(e.target.value as TaxIdCondition)}>
            {reference.taxIdConditions.map((v) => <MenuItem key={v} value={v}>{t(`admin.tax.taxIdCondition.${v}`)}</MenuItem>)}
          </TextField>
          <TextField type="date" label={t('admin.tax.ruleForm.effectiveFrom')} value={from} onChange={(e) => setFrom(e.target.value)} required
            slotProps={{ inputLabel: { shrink: true } }} error={Boolean(show('from'))} helperText={show('from')} />
          <TextField type="date" label={t('admin.tax.ruleForm.effectiveTo')} value={to} onChange={(e) => setTo(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }} error={Boolean(show('to'))} helperText={show('to')} />
          <TextField label={t('admin.tax.ruleForm.notes')} value={notes} onChange={(e) => setNotes(e.target.value)} multiline minRows={2} sx={{ gridColumn: { sm: '1 / -1' } }} />
        </Box>

        <Box sx={{ mt: 3 }}>
          <Typography component="h3" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.ruleForm.components')}</Typography>
          {taxable ? (
            <>
              <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('admin.tax.ruleForm.componentsHelp')}</Typography>
              {components.map((c, i) => (
                <Box key={i} role="group" aria-label={`${t('admin.tax.ruleForm.components')} ${i + 1}`}
                  sx={{ display: 'grid', gap: 1.5, gridTemplateColumns: { xs: '1fr 1fr', md: '110px 1fr 120px auto auto' }, alignItems: 'center', mb: 1.5 }}>
                  <TextField size="small" label={t('admin.tax.ruleForm.componentCode')} value={c.code} onChange={(e) => setComponent(i, { code: e.target.value })} />
                  <TextField size="small" label={t('admin.tax.ruleForm.componentName')} value={c.name} onChange={(e) => setComponent(i, { name: e.target.value })} />
                  <TextField size="small" label={t('admin.tax.ruleForm.componentRate')} value={c.rate} onChange={(e) => setComponent(i, { rate: e.target.value })}
                    slotProps={{ htmlInput: { inputMode: 'decimal' } }} />
                  <FormControlLabel control={<Checkbox size="small" checked={c.compound} onChange={(e) => setComponent(i, { compound: e.target.checked })} />}
                    label={<Typography variant="caption">{t('admin.tax.ruleForm.compound')}</Typography>} />
                  <IconButton aria-label={t('admin.tax.ruleForm.removeComponent', { n: i + 1 })} onClick={() => setComponents((rows) => rows.filter((_, k) => k !== i))}
                    disabled={components.length === 1}><Trash2 size={16} aria-hidden /></IconButton>
                </Box>
              ))}
              {(['components', 'code', 'componentName', 'rate'] as const).map((k) => show(k) && <Alert key={k} severity="error" sx={{ mb: 1 }} role="alert">{show(k)}</Alert>)}
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
                <Button size="small" startIcon={<Plus size={14} />} onClick={() => setComponents((rows) => [...rows, { ...EMPTY_COMPONENT }])}>{t('admin.tax.ruleForm.addComponent')}</Button>
                {totalRate !== null && <Typography variant="body2" sx={{ fontWeight: 600 }}>{t('admin.tax.ruleForm.totalRate', { rate: trimRate(Number(totalRate.toFixed(4))) })}</Typography>}
              </Box>
            </>
          ) : (
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.tax.ruleForm.noComponents', { treatment: t(`admin.tax.treatment.${treatment}`).toLowerCase() })}</Typography>
          )}
        </Box>
        {serverError && <Alert severity="error" role="alert" sx={{ mt: 2 }}>{serverError}</Alert>}
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" onClick={() => void save()} disabled={busy} startIcon={busy ? <CircularProgress size={14} color="inherit" /> : undefined}>
          {t('admin.tax.ruleForm.save')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
