import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Checkbox, Chip, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, FormGroup, FormLabel,
  IconButton, MenuItem, Paper, Radio, RadioGroup, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Tabs, TextField, Tooltip, Typography,
} from '@mui/material'
import { Boxes, Pencil, Plus, Trash2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  adminOfferingsApi, type Offering, type OfferingStatus, type ProductAudience, type ProductRule,
} from '../../api/offeringsApi'
import { productsApi, type Product } from '../../api/productsApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { EmptyState } from '../../components/ui/EmptyState'
import { StatusBadge } from '../../components/ui/StatusBadge'

const STATUS_TONE = { DRAFT: 'warning', ACTIVE: 'success', RETIRED: 'neutral' } as const
const STATUSES: OfferingStatus[] = ['DRAFT', 'ACTIVE', 'RETIRED']
const AUDIENCES: ProductAudience[] = ['BOTH', 'INDIVIDUAL', 'ORGANIZATION']

function errorText(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}

/** A checkbox per product — a catalog has tens of products, so a plain list beats a search box. */
function ProductChecklist({ label, products, selected, onChange }: {
  label: string; products: Product[]; selected: number[]; onChange: (ids: number[]) => void
}) {
  const toggle = (id: number) => onChange(selected.includes(id) ? selected.filter((x) => x !== id) : [...selected, id])
  return (
    <Box component="fieldset" sx={{ border: 'none', p: 0, m: 0 }}>
      <FormLabel component="legend" sx={{ mb: 0.5 }}>{label}</FormLabel>
      <FormGroup sx={{ maxHeight: 240, overflowY: 'auto', flexWrap: 'nowrap' }}>
        {products.map((p) => (
          <FormControlLabel
            key={p.id}
            control={<Checkbox size="small" checked={selected.includes(p.id)} onChange={() => toggle(p.id)} />}
            label={p.status === 'ACTIVE' ? p.name : `${p.name} (${p.status.toLowerCase()})`}
          />
        ))}
      </FormGroup>
    </Box>
  )
}

function OfferingDialog({ offering, products, onClose, onSaved }: {
  offering: Offering | 'new'; products: Product[]; onClose: () => void; onSaved: () => void
}) {
  const { t } = useTranslation()
  const existing = offering === 'new' ? null : offering
  const [name, setName] = useState(existing?.name ?? '')
  const [description, setDescription] = useState(existing?.description ?? '')
  const [status, setStatus] = useState<OfferingStatus>(existing?.status ?? 'DRAFT')
  const [productIds, setProductIds] = useState<number[]>(existing?.products.map((p) => p.id) ?? [])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const save = async () => {
    setBusy(true)
    setError(null)
    try {
      const input = { name: name.trim(), description: description.trim() || undefined, status, productIds }
      if (existing) await adminOfferingsApi.update(existing.id, input)
      else await adminOfferingsApi.create(input)
      onSaved()
    } catch (e) {
      setError(errorText(e, t('offerings.admin.saveFailed')))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="sm" aria-labelledby="offering-dialog-title">
      <DialogTitle id="offering-dialog-title">{existing ? t('offerings.admin.editTitle') : t('offerings.admin.newTitle')}</DialogTitle>
      <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '8px !important' }}>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField label={t('offerings.admin.name')} value={name} onChange={(e) => setName(e.target.value)} required
          slotProps={{ htmlInput: { maxLength: 150 } }} />
        <TextField label={t('offerings.admin.description')} value={description} onChange={(e) => setDescription(e.target.value)}
          multiline minRows={2} slotProps={{ htmlInput: { maxLength: 1000 } }} />
        <TextField select label={t('offerings.admin.status')} value={status} onChange={(e) => setStatus(e.target.value as OfferingStatus)}
          helperText={t('offerings.admin.statusHelp')}>
          {STATUSES.map((s) => <MenuItem key={s} value={s}>{t(`offerings.status.${s}`)}</MenuItem>)}
        </TextField>
        <ProductChecklist label={t('offerings.admin.products')} products={products} selected={productIds} onChange={setProductIds} />
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" onClick={save} disabled={busy || name.trim() === '' || productIds.length === 0}>
          {t('offerings.admin.save')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

function RuleDialog({ rule, products, onClose, onSaved }: {
  rule: ProductRule; products: Product[]; onClose: () => void; onSaved: () => void
}) {
  const { t } = useTranslation()
  const [audience, setAudience] = useState<ProductAudience>(rule.audience)
  const [worksWith, setWorksWith] = useState<number[]>(rule.worksWithProductIds)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const save = async () => {
    setBusy(true)
    setError(null)
    try {
      await adminOfferingsApi.setRules(rule.productId, { audience, worksWithProductIds: worksWith })
      onSaved()
    } catch (e) {
      setError(errorText(e, t('offerings.admin.saveFailed')))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="sm" aria-labelledby="rule-dialog-title">
      <DialogTitle id="rule-dialog-title">{t('offerings.rules.editTitle', { product: rule.productName })}</DialogTitle>
      <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '8px !important' }}>
        {error && <Alert severity="error">{error}</Alert>}
        <Box component="fieldset" sx={{ border: 'none', p: 0, m: 0 }}>
          <FormLabel component="legend">{t('offerings.rules.audience')}</FormLabel>
          <RadioGroup value={audience} onChange={(e) => setAudience(e.target.value as ProductAudience)}>
            {AUDIENCES.map((a) => <FormControlLabel key={a} value={a} control={<Radio size="small" />} label={t(`offerings.audience.${a}`)} />)}
          </RadioGroup>
        </Box>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('offerings.rules.requiredHint')}</Typography>
        <ProductChecklist label={t('offerings.rules.worksWith')} products={products.filter((p) => p.id !== rule.productId)}
          selected={worksWith} onChange={setWorksWith} />
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" onClick={save} disabled={busy}>{t('offerings.admin.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}

/** "/admin/offerings" — REQ-CAT-005 (MANAGE_CATALOG): offerings, and the audience / "works with" rule of each product. */
export function AdminOfferingsPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useState<'offerings' | 'rules'>('offerings')
  const [offerings, setOfferings] = useState<Offering[] | null>(null)
  const [rules, setRules] = useState<ProductRule[] | null>(null)
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState<string | null>(null)
  const [version, setVersion] = useState(0)
  const [editing, setEditing] = useState<Offering | 'new' | null>(null)
  const [editingRule, setEditingRule] = useState<ProductRule | null>(null)
  const [pendingDelete, setPendingDelete] = useState<Offering | null>(null)
  const [deleting, setDeleting] = useState(false)

  const refresh = useCallback(() => setVersion((v) => v + 1), [])

  useEffect(() => {
    let cancelled = false
    Promise.all([adminOfferingsApi.list(), adminOfferingsApi.rules(), productsApi.listAdmin()])
      .then(([o, r, p]) => {
        if (cancelled) return
        setOfferings(o)
        setRules(r)
        setProducts(p)
        setError(null)
      })
      .catch((e) => { if (!cancelled) setError(errorText(e, t('offerings.admin.loadFailed'))) })
    return () => { cancelled = true }
  }, [version, t])

  const nameOf = (id: number) => products.find((p) => p.id === id)?.name ?? `#${id}`

  const confirmDelete = async () => {
    if (!pendingDelete) return
    setDeleting(true)
    try {
      await adminOfferingsApi.remove(pendingDelete.id)
      setPendingDelete(null)
      refresh()
    } catch (e) {
      setError(errorText(e, t('offerings.admin.deleteFailed')))
      setPendingDelete(null)
    } finally {
      setDeleting(false)
    }
  }

  return (
    <Box>
      <PageHeader icon={Boxes} accent="violet" area="catalog" title={t('offerings.admin.title')} subtitle={t('offerings.admin.subtitle')}
        action={tab === 'offerings'
          ? <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setEditing('new')}>{t('offerings.admin.new')}</Button>
          : undefined} />
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Alert severity="info" sx={{ mb: 2 }}>{t('offerings.admin.priceNote')}</Alert>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="offerings" label={t('offerings.admin.tabs.offerings')} />
        <Tab value="rules" label={t('offerings.admin.tabs.rules')} />
      </Tabs>

      {tab === 'offerings' && offerings && (offerings.length === 0
        ? <EmptyState icon={Boxes} title={t('offerings.admin.empty')} description={t('offerings.admin.emptyHint')} />
        : (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small" aria-label={t('offerings.admin.tabs.offerings')}>
              <TableHead>
                <TableRow>
                  <TableCell>{t('offerings.admin.name')}</TableCell>
                  <TableCell>{t('offerings.admin.status')}</TableCell>
                  <TableCell>{t('offerings.admin.products')}</TableCell>
                  <TableCell align="right">{t('offerings.admin.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {offerings.map((o) => (
                  <TableRow key={o.id} hover>
                    <TableCell sx={{ fontWeight: 600 }}>{o.name}</TableCell>
                    <TableCell><StatusBadge label={t(`offerings.status.${o.status}`)} tone={STATUS_TONE[o.status]} /></TableCell>
                    <TableCell>{o.products.map((p) => p.name).join(', ')}</TableCell>
                    <TableCell align="right">
                      <Tooltip title={t('offerings.admin.edit')}>
                        <IconButton size="small" aria-label={t('offerings.admin.editNamed', { name: o.name })} onClick={() => setEditing(o)}><Pencil size={16} /></IconButton>
                      </Tooltip>
                      {o.status === 'DRAFT' && (
                        <Tooltip title={t('offerings.admin.delete')}>
                          <IconButton size="small" aria-label={t('offerings.admin.deleteNamed', { name: o.name })} onClick={() => setPendingDelete(o)}><Trash2 size={16} /></IconButton>
                        </Tooltip>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        ))}

      {tab === 'rules' && rules && (
        <>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('offerings.rules.intro')}</Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small" aria-label={t('offerings.admin.tabs.rules')}>
              <TableHead>
                <TableRow>
                  <TableCell>{t('offerings.rules.product')}</TableCell>
                  <TableCell>{t('offerings.rules.audience')}</TableCell>
                  <TableCell>{t('offerings.rules.worksWith')}</TableCell>
                  <TableCell align="right">{t('offerings.admin.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rules.map((r) => (
                  <TableRow key={r.productId} hover>
                    <TableCell sx={{ fontWeight: 600 }}>{r.productName}</TableCell>
                    <TableCell>{t(`offerings.audience.${r.audience}`)}</TableCell>
                    <TableCell>
                      {r.worksWithProductIds.length === 0
                        ? '—'
                        : <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>{r.worksWithProductIds.map((id) => <Chip key={id} size="small" label={nameOf(id)} />)}</Box>}
                    </TableCell>
                    <TableCell align="right">
                      <Tooltip title={t('offerings.admin.edit')}>
                        <IconButton size="small" aria-label={t('offerings.rules.editNamed', { name: r.productName })} onClick={() => setEditingRule(r)}><Pencil size={16} /></IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      {editing && (
        <OfferingDialog offering={editing} products={products} onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); refresh() }} />
      )}
      {editingRule && (
        <RuleDialog rule={editingRule} products={products} onClose={() => setEditingRule(null)}
          onSaved={() => { setEditingRule(null); refresh() }} />
      )}
      <ConfirmDialog open={pendingDelete !== null} title={t('offerings.admin.deleteTitle', { name: pendingDelete?.name ?? '' })}
        body={t('offerings.admin.deleteBody')} confirmLabel={t('offerings.admin.delete')} busy={deleting}
        onConfirm={confirmDelete} onClose={() => setPendingDelete(null)} />
    </Box>
  )
}
