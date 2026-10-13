import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Paper, Skeleton, Table, TableBody,
  TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { Plus } from 'lucide-react'
import { taxApi, type ProductTaxClass, type TaxClass } from '../../api/taxApi'
import { EmptyState } from '../ui/EmptyState'
import { errorText } from './taxFormat'

/** REQ-BIL-002.19: tax classes (for example a SAC code) and the class of each product. */
export function TaxClassesPanel({ onToast }: { onToast: (message: string) => void }) {
  const { t } = useTranslation()
  const [classes, setClasses] = useState<TaxClass[] | null>(null)
  const [products, setProducts] = useState<ProductTaxClass[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [editing, setEditing] = useState<TaxClass | 'new' | null>(null)
  const [savingProduct, setSavingProduct] = useState<number | null>(null)

  const load = useCallback(() => {
    return Promise.all([taxApi.classes(), taxApi.products()])
      .then(([c, p]) => { setClasses(c); setProducts(p); setError(null) })
      .catch((e) => setError(errorText(e, t('admin.tax.loadError'))))
  }, [t])
  useEffect(() => { void load() }, [load])

  const assign = async (product: ProductTaxClass, code: string) => {
    setSavingProduct(product.productId)
    try {
      const updated = await taxApi.assignClass(product.productId, code || null)
      setProducts((rows) => rows && rows.map((p) => (p.productId === updated.productId ? updated : p)))
      void taxApi.classes().then(setClasses)
      onToast(t('admin.tax.saved'))
    } catch (e) {
      setError(errorText(e, t('admin.tax.loadError')))
    } finally {
      setSavingProduct(null)
    }
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start', flexWrap: 'wrap', mb: 2 }}>
          <Box sx={{ flex: 1, minWidth: 260 }}>
            <Typography component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.classes.title')}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.tax.classes.description')}</Typography>
          </Box>
          <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setEditing('new')}>{t('admin.tax.classes.new')}</Button>
        </Box>
        {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('admin.tax.retry')}</Button>}>{error}</Alert>}
        {!error && classes === null && <Skeleton variant="rounded" height={120} />}
        {classes && classes.length === 0 && <EmptyState title={t('admin.tax.classes.title')} description={t('admin.tax.classes.empty')} />}
        {classes && classes.length > 0 && (
          <Table size="small" aria-label={t('admin.tax.classes.title')}>
            <TableHead>
              <TableRow>
                <TableCell>{t('admin.tax.classes.code')}</TableCell><TableCell>{t('admin.tax.classes.name')}</TableCell>
                <TableCell>{t('admin.tax.classes.sac')}</TableCell><TableCell>{t('admin.tax.classes.enabled')}</TableCell>
                <TableCell>{t('admin.tax.classes.productsColumn')}</TableCell><TableCell />
              </TableRow>
            </TableHead>
            <TableBody>
              {classes.map((c) => (
                <TableRow key={c.id}>
                  <TableCell sx={{ fontWeight: 600 }}>{c.code}</TableCell>
                  <TableCell>{c.name}</TableCell>
                  <TableCell>{c.hsnSacCode ?? '—'}</TableCell>
                  <TableCell>{c.enabled ? t('forms.active') : t('forms.inactive')}</TableCell>
                  <TableCell>{t('admin.tax.classes.products', { count: c.productCount })}</TableCell>
                  <TableCell align="right"><Button size="small" onClick={() => setEditing(c)}>{t('admin.tax.classes.edit')}</Button></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Paper>

      <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
        <Typography component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.classes.productsTitle')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('admin.tax.classes.productsDescription')}</Typography>
        {products === null ? <Skeleton variant="rounded" height={120} /> : (
          <Table size="small" aria-label={t('admin.tax.classes.productsTitle')}>
            <TableHead><TableRow><TableCell>{t('admin.tax.classes.product')}</TableCell><TableCell>{t('admin.tax.ruleForm.taxClass')}</TableCell></TableRow></TableHead>
            <TableBody>
              {products.map((p) => (
                <TableRow key={p.productId}>
                  <TableCell>{p.name}</TableCell>
                  <TableCell sx={{ minWidth: 220 }}>
                    <TextField select size="small" fullWidth value={p.taxClassCode ?? ''} disabled={savingProduct === p.productId}
                      onChange={(e) => void assign(p, e.target.value)} slotProps={{ htmlInput: { 'aria-label': t('admin.tax.classes.classFor', { name: p.name }) } }}>
                      <MenuItem value="">{t('admin.tax.classes.none')}</MenuItem>
                      {(classes ?? []).filter((c) => c.enabled || c.code === p.taxClassCode).map((c) => <MenuItem key={c.code} value={c.code}>{c.code} — {c.name}</MenuItem>)}
                    </TextField>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Paper>

      {editing !== null && (
        <ClassDialog taxClass={editing === 'new' ? null : editing} onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); onToast(t('admin.tax.saved')); void load() }} />
      )}
    </Box>
  )
}

function ClassDialog({ taxClass, onClose, onSaved }: { taxClass: TaxClass | null; onClose: () => void; onSaved: () => void }) {
  const { t } = useTranslation()
  const [code, setCode] = useState(taxClass?.code ?? '')
  const [name, setName] = useState(taxClass?.name ?? '')
  const [sac, setSac] = useState(taxClass?.hsnSacCode ?? '')
  const [description, setDescription] = useState(taxClass?.description ?? '')
  const [enabled, setEnabled] = useState(taxClass?.enabled ?? true)
  const [submitted, setSubmitted] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const codeError = !/^[A-Za-z0-9_-]{1,40}$/.test(code.trim()) ? t('admin.tax.classes.errors.code') : undefined
  const nameError = !name.trim() ? t('admin.tax.classes.errors.name') : undefined

  const save = async () => {
    setSubmitted(true)
    if (codeError || nameError) return
    setBusy(true)
    setError(null)
    const body = { code: code.trim(), name: name.trim(), hsnSacCode: sac.trim() || undefined, description: description.trim() || undefined, enabled }
    try {
      if (taxClass) await taxApi.updateClass(taxClass.id, body); else await taxApi.createClass(body)
      onSaved()
    } catch (e) {
      setError(errorText(e, t('admin.tax.loadError')))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="tax-class-title" maxWidth="xs" fullWidth>
      <DialogTitle id="tax-class-title">{t(taxClass ? 'admin.tax.classes.edit' : 'admin.tax.classes.new')}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        <TextField label={t('admin.tax.classes.code')} value={code} onChange={(e) => setCode(e.target.value)} disabled={taxClass !== null} required
          error={submitted && Boolean(codeError)} helperText={submitted && codeError ? codeError : taxClass ? t('admin.tax.classes.codeLocked') : undefined} />
        <TextField label={t('admin.tax.classes.name')} value={name} onChange={(e) => setName(e.target.value)} required error={submitted && Boolean(nameError)} helperText={submitted ? nameError : undefined} />
        <TextField label={t('admin.tax.classes.sac')} value={sac} onChange={(e) => setSac(e.target.value)} />
        <TextField label={t('admin.tax.classes.descriptionField')} value={description} onChange={(e) => setDescription(e.target.value)} multiline minRows={2} />
        <FormControlLabel control={<Checkbox checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />} label={t('admin.tax.classes.enabled')} />
        {error && <Alert severity="error" role="alert">{error}</Alert>}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" onClick={() => void save()} disabled={busy}>{t('admin.tax.classes.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}
