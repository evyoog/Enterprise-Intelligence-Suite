import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Checkbox, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, FormGroup, IconButton,
  MenuItem, Paper, Skeleton, Switch, TextField, Typography,
} from '@mui/material'
import { Plus, Trash2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  paymentGatewayApi, type GatewayDefinition, type GatewayFieldDefinition, type GatewayTemplate,
} from '../../api/paymentGatewayApi'

const METHODS = ['card', 'upi', 'netbanking', 'wallet', 'bank_transfer', 'paypal']

interface Row extends GatewayFieldDefinition { value: string; existing: boolean; keyTouched: boolean }

const slugCode = (name: string) => {
  const code = name.toUpperCase().replace(/[^A-Z0-9]+/g, '_').replace(/^_+|_+$/g, '').slice(0, 30)
  return /^[A-Z]/.test(code) || code === '' ? code : `G_${code}`.slice(0, 30)
}

const camelKey = (label: string) => {
  const words = label.split(/[^A-Za-z0-9]+/).filter(Boolean)
  const key = words.map((w, i) => (i === 0 ? (w === w.toUpperCase() ? w.toLowerCase() : w.charAt(0).toLowerCase() + w.slice(1)) : w.charAt(0).toUpperCase() + w.slice(1))).join('')
  return (/^[A-Za-z]/.test(key) || key === '' ? key : `f${key}`).slice(0, 40)
}

const list = (text: string) => text.split(/[\s,;]+/).map((v) => v.trim()).filter(Boolean)

const blankRow = (): Row => ({ key: '', label: '', secret: true, required: true, value: '', existing: false, keyTouched: false })

/**
 * C99 (REQ-BIL-003.16): the Add payment gateway form (and, for a gateway already added, its definition). A platform administrator names a gateway,
 * lists the credentials it needs and may enter their values; the values are sent once, stored encrypted and never shown again. A gateway added
 * here has no payment integration behind it, so it only stores its configuration. "Start from" pre-fills a well-known provider's fields; every
 * value can be changed.
 */
export function GatewayFormDialog({ code, onClose, onSaved }: { code?: string; onClose: () => void; onSaved: (message: string) => void }) {
  const { t } = useTranslation()
  const editing = code !== undefined
  const [ready, setReady] = useState(editing ? false : true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [templates, setTemplates] = useState<GatewayTemplate[]>([])
  const [template, setTemplate] = useState('')
  const [name, setName] = useState('')
  const [codeValue, setCodeValue] = useState(code ?? '')
  const [codeTouched, setCodeTouched] = useState(false)
  const [description, setDescription] = useState('')
  const [logo, setLogo] = useState<string | undefined>(undefined)
  const [environment, setEnvironment] = useState('TEST')
  const [currencies, setCurrencies] = useState('INR')
  const [countries, setCountries] = useState('')
  const [methods, setMethods] = useState<string[]>(['card'])
  const [rows, setRows] = useState<Row[]>([blankRow()])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (editing) {
      paymentGatewayApi.detail(code)
        .then((d) => {
          setName(d.gateway.name)
          setDescription(d.gateway.description)
          setLogo(d.gateway.logo)
          setCurrencies(d.gateway.currencies.join(', '))
          setCountries(d.gateway.countries.join(', '))
          setMethods(d.gateway.supportedMethods)
          setRows(d.fields.map((f) => ({ key: f.key, label: f.label, secret: f.secret, required: f.required, value: '', existing: true, keyTouched: true })))
          setReady(true)
        })
        .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('admin.paymentGateway.loadError')))
    } else {
      paymentGatewayApi.templates().then(setTemplates).catch(() => setTemplates([]))
    }
  }, [code, editing, t])

  const pickTemplate = (value: string) => {
    setTemplate(value)
    const tpl = templates.find((x) => x.code === value)
    if (!tpl) return
    setName(tpl.name)
    setCodeValue(tpl.code)
    setCodeTouched(true)
    setDescription(tpl.description)
    setLogo(tpl.logo)
    setCurrencies(tpl.currencies.join(', '))
    setCountries(tpl.countries.join(', '))
    setMethods(tpl.methods)
    setRows(tpl.fields.map((f) => ({ ...f, value: '', existing: false, keyTouched: true })))
  }

  const changeName = (value: string) => {
    setName(value)
    if (!editing && !codeTouched) setCodeValue(slugCode(value))
  }

  const updateRow = (index: number, patch: Partial<Row>) => setRows((all) => all.map((r, i) => (i === index ? { ...r, ...patch } : r)))
  const changeLabel = (index: number, label: string) => setRows((all) => all.map((r, i) => (i === index
    ? { ...r, label, key: r.keyTouched || r.existing ? r.key : camelKey(label) } : r)))

  const problem = (() => {
    if (name.trim() === '') return t('admin.paymentGateway.form.errors.name')
    if (!editing && !/^[A-Z][A-Z0-9_]{1,29}$/.test(codeValue)) return t('admin.paymentGateway.form.errors.code')
    if (list(currencies).length === 0) return t('admin.paymentGateway.form.errors.currencies')
    if (methods.length === 0) return t('admin.paymentGateway.form.errors.methods')
    if (rows.length === 0 || rows.some((r) => r.label.trim() === '' || !/^[A-Za-z][A-Za-z0-9]{0,39}$/.test(r.key))) return t('admin.paymentGateway.form.errors.fields')
    if (new Set(rows.map((r) => r.key.toLowerCase())).size !== rows.length) return t('admin.paymentGateway.form.errors.duplicate')
    if (!rows.some((r) => r.required)) return t('admin.paymentGateway.form.errors.required')
    return null
  })()

  const save = async () => {
    if (problem || busy) return
    setBusy(true)
    setError(null)
    const definition: GatewayDefinition = {
      code: codeValue.trim(), name: name.trim(), description: description.trim(), logo,
      countries: list(countries), currencies: list(currencies), methods,
      fields: rows.map(({ key, label, secret, required }) => ({ key, label: label.trim(), secret, required })),
    }
    try {
      if (editing) {
        await paymentGatewayApi.updateDefinition(code, definition)
        onSaved(t('admin.paymentGateway.form.updated', { name: definition.name }))
      } else {
        const values = Object.fromEntries(rows.filter((r) => r.value.trim() !== '').map((r) => [r.key, r.value.trim()]))
        await paymentGatewayApi.create({ definition, environment, values })
        onSaved(t('admin.paymentGateway.form.added', { name: definition.name }))
      }
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('admin.paymentGateway.saveFailed'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="md" aria-labelledby="gw-form-title">
      <DialogTitle id="gw-form-title">{editing ? t('admin.paymentGateway.form.editTitle') : t('admin.paymentGateway.form.addTitle')}</DialogTitle>
      <DialogContent>
        {loadError && <Alert severity="error">{loadError}</Alert>}
        {!ready && !loadError && <Skeleton variant="rounded" height={260} />}
        {ready && (
          <Box component="form" noValidate onSubmit={(e) => { e.preventDefault(); void save() }} sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <Alert severity="info">{t('admin.paymentGateway.form.intro')}</Alert>

            {!editing && templates.length > 0 && (
              <TextField select size="small" label={t('admin.paymentGateway.form.template')} value={template} onChange={(e) => pickTemplate(e.target.value)}
                helperText={t('admin.paymentGateway.form.templateHelp')}>
                <MenuItem value="">{t('admin.paymentGateway.form.templateNone')}</MenuItem>
                {templates.map((x) => <MenuItem key={x.code} value={x.code}>{x.name}</MenuItem>)}
              </TextField>
            )}

            <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
              <TextField size="small" required label={t('admin.paymentGateway.form.name')} value={name} onChange={(e) => changeName(e.target.value)}
                slotProps={{ htmlInput: { maxLength: 100 } }} />
              <TextField size="small" required label={t('admin.paymentGateway.form.code')} value={codeValue} disabled={editing}
                onChange={(e) => { setCodeTouched(true); setCodeValue(e.target.value.toUpperCase()) }} slotProps={{ htmlInput: { maxLength: 30 } }}
                helperText={t('admin.paymentGateway.form.codeHelp')} />
            </Box>
            <TextField size="small" label={t('admin.paymentGateway.form.description')} value={description} onChange={(e) => setDescription(e.target.value)}
              slotProps={{ htmlInput: { maxLength: 300 } }} />
            <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: editing ? '1fr 1fr' : '1fr 1fr 1fr' } }}>
              {!editing && (
                <TextField select size="small" label={t('admin.paymentGateway.environment')} value={environment} onChange={(e) => setEnvironment(e.target.value)}>
                  {['TEST', 'LIVE'].map((env) => <MenuItem key={env} value={env}>{t(`admin.paymentGateway.env.${env}`)}</MenuItem>)}
                </TextField>
              )}
              <TextField size="small" required label={t('admin.paymentGateway.form.currencies')} value={currencies} onChange={(e) => setCurrencies(e.target.value)}
                helperText={t('admin.paymentGateway.form.currenciesHelp')} />
              <TextField size="small" label={t('admin.paymentGateway.form.countries')} value={countries} onChange={(e) => setCountries(e.target.value)}
                helperText={t('admin.paymentGateway.form.countriesHelp')} />
            </Box>

            <Box component="fieldset" sx={{ border: 0, p: 0, m: 0 }}>
              <Typography component="legend" variant="subtitle2" sx={{ fontWeight: 700, mb: 0.5 }}>{t('admin.paymentGateway.paymentMethods')}</Typography>
              <FormGroup row>
                {METHODS.map((m) => (
                  <FormControlLabel key={m} label={t(`admin.paymentGateway.methods.${m}`)}
                    control={<Checkbox size="small" checked={methods.includes(m)}
                      onChange={(e) => setMethods((all) => (e.target.checked ? [...all, m] : all.filter((x) => x !== m)))} />} />
                ))}
              </FormGroup>
            </Box>

            <Box component="fieldset" sx={{ border: 0, p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              <Typography component="legend" variant="subtitle2" sx={{ fontWeight: 700 }}>{t('admin.paymentGateway.form.credentials')}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                {editing ? t('admin.paymentGateway.form.credentialsHelpEdit') : t('admin.paymentGateway.form.credentialsHelp')}
              </Typography>
              {rows.map((r, i) => (
                <Paper key={i} variant="outlined" sx={{ p: 1.5, borderRadius: 2 }} role="group" aria-label={t('admin.paymentGateway.form.credentialN', { n: i + 1 })}>
                  <Box sx={{ display: 'grid', gap: 1.5, gridTemplateColumns: { xs: '1fr', sm: editing ? '1fr 1fr' : '1fr 1fr 1.2fr' }, alignItems: 'start' }}>
                    <TextField size="small" required label={t('admin.paymentGateway.form.credentialLabel')} value={r.label} onChange={(e) => changeLabel(i, e.target.value)}
                      slotProps={{ htmlInput: { maxLength: 60 } }} />
                    <TextField size="small" required label={t('admin.paymentGateway.form.credentialKey')} value={r.key} disabled={r.existing}
                      onChange={(e) => updateRow(i, { key: e.target.value, keyTouched: true })} slotProps={{ htmlInput: { maxLength: 40 } }}
                      helperText={t('admin.paymentGateway.form.credentialKeyHelp')} />
                    {!editing && (
                      <TextField size="small" label={t('admin.paymentGateway.form.credentialValue')} value={r.value} onChange={(e) => updateRow(i, { value: e.target.value })}
                        type={r.secret ? 'password' : 'text'} autoComplete="off" slotProps={{ htmlInput: { maxLength: 500, spellCheck: false } }}
                        helperText={t('admin.paymentGateway.form.credentialValueHelp')} />
                    )}
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap', mt: 0.5 }}>
                    <FormControlLabel label={t('admin.paymentGateway.form.secret')} control={<Switch size="small" checked={r.secret} onChange={(e) => updateRow(i, { secret: e.target.checked })} />} />
                    <FormControlLabel label={t('admin.paymentGateway.form.requiredField')} control={<Switch size="small" checked={r.required} onChange={(e) => updateRow(i, { required: e.target.checked })} />} />
                    <Box sx={{ flex: 1 }} />
                    <IconButton size="small" disabled={rows.length === 1} onClick={() => setRows((all) => all.filter((_, x) => x !== i))}
                      aria-label={t('admin.paymentGateway.form.removeCredential', { n: i + 1 })}><Trash2 size={16} /></IconButton>
                  </Box>
                </Paper>
              ))}
              <Box><Button size="small" startIcon={<Plus size={16} />} disabled={rows.length >= 12} onClick={() => setRows((all) => [...all, blankRow()])}>{t('admin.paymentGateway.form.addCredential')}</Button></Box>
            </Box>

            <Alert severity="warning">{t('admin.paymentGateway.form.noIntegration')}</Alert>
            {problem && <Typography variant="caption" role="status" sx={{ color: 'text.secondary' }}>{problem}</Typography>}
            {error && <Alert severity="error" role="alert">{error}</Alert>}
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={busy}>{t('admin.paymentGateway.cancel')}</Button>
        <Button variant="contained" disabled={!ready || busy || problem !== null} onClick={() => void save()}
          startIcon={busy ? <CircularProgress size={16} color="inherit" /> : undefined}>
          {editing ? t('admin.paymentGateway.save') : t('admin.paymentGateway.form.add')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
