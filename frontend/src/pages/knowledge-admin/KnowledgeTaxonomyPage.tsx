import { Alert, Box, Button, Checkbox, FormControlLabel, MenuItem, Paper, TextField, Typography } from '@mui/material'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { CONTENT_TYPES, knowledgeAdminApi, type ContentType } from '../../api/knowledgeApi'
import { useKnowledgeAdmin } from './knowledgeAdminContext'

/** Taxonomy as data (C74, knowledge-admin-taxonomy): products, modules, categories. Publishers only. */
export function KnowledgeTaxonomyPage() {
  const { t } = useTranslation()
  const { taxonomy, reloadTaxonomy, me } = useKnowledgeAdmin()
  const [productId, setProductId] = useState<number | null>(taxonomy.products[0]?.id ?? null)
  const [newProduct, setNewProduct] = useState('')
  const [newModule, setNewModule] = useState('')
  const [newCategory, setNewCategory] = useState('')
  const [scope, setScope] = useState<ContentType | ''>('')
  const [error, setError] = useState<string | null>(null)
  const run = (p: Promise<unknown>) => p.then(reloadTaxonomy).catch((e: Error) => setError(e.message))
  if (!me.publisher) return <Alert severity="warning">{t('knowledge.admin.noAccess')}</Alert>
  const product = taxonomy.products.find((p) => p.id === productId)
  return (
    <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr 1fr' }, alignItems: 'start' }}>
      {error && <Alert severity="error" onClose={() => setError(null)} sx={{ gridColumn: '1 / -1' }}>{error}</Alert>}
      <Paper variant="outlined" component="section" aria-labelledby="kt-products" sx={{ p: 2 }}>
        <Typography id="kt-products" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.taxonomy.products')}</Typography>
        {taxonomy.products.map((p) => (
          <Box key={p.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.5 }}>
            <Button size="small" variant={p.id === productId ? 'contained' : 'text'} onClick={() => setProductId(p.id)} sx={{ flex: 1, justifyContent: 'flex-start' }}>
              {p.name}
            </Button>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.taxonomy.contentCount', { count: p.contentCount })}</Typography>
            <FormControlLabel control={<Checkbox size="small" checked={p.active} onChange={(e) => run(knowledgeAdminApi.saveProduct(p.id, { ...p, active: e.target.checked }))} />} label={t('knowledge.admin.taxonomy.active')} />
          </Box>
        ))}
        <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
          <TextField size="small" label={t('knowledge.admin.taxonomy.name')} value={newProduct} onChange={(e) => setNewProduct(e.target.value)} sx={{ flex: 1 }} />
          <Button variant="outlined" disabled={!newProduct.trim()} onClick={() => { run(knowledgeAdminApi.saveProduct(null, { name: newProduct, displayOrder: taxonomy.products.length })); setNewProduct('') }}>{t('knowledge.admin.taxonomy.add')}</Button>
        </Box>
      </Paper>
      <Paper variant="outlined" component="section" aria-labelledby="kt-modules" sx={{ p: 2 }}>
        <Typography id="kt-modules" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.taxonomy.modules')}{product ? ` — ${product.name}` : ''}</Typography>
        {product?.modules.map((m) => (
          <Box key={m.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.25 }}>
            <Typography sx={{ flex: 1 }}>{m.name}</Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.taxonomy.contentCount', { count: m.contentCount })}</Typography>
            <FormControlLabel control={<Checkbox size="small" checked={m.active} onChange={(e) => run(knowledgeAdminApi.saveModule(m.id, { ...m, active: e.target.checked }))} />} label={t('knowledge.admin.taxonomy.active')} />
            <Button size="small" color="error" disabled={m.contentCount > 0} title={m.contentCount > 0 ? t('knowledge.admin.taxonomy.inUse') : undefined}
              onClick={() => run(knowledgeAdminApi.deleteTaxonomy('modules', m.id))}>{t('knowledge.admin.taxonomy.delete')}</Button>
          </Box>
        ))}
        {product && (
          <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
            <TextField size="small" label={t('knowledge.admin.taxonomy.name')} value={newModule} onChange={(e) => setNewModule(e.target.value)} sx={{ flex: 1 }} />
            <Button variant="outlined" disabled={!newModule.trim()} onClick={() => { run(knowledgeAdminApi.saveModule(null, { productId: product.id, name: newModule, displayOrder: product.modules.length })); setNewModule('') }}>{t('knowledge.admin.taxonomy.add')}</Button>
          </Box>
        )}
      </Paper>
      <Paper variant="outlined" component="section" aria-labelledby="kt-categories" sx={{ p: 2 }}>
        <Typography id="kt-categories" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.taxonomy.categories')}</Typography>
        {taxonomy.categories.map((c) => (
          <Box key={c.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.25 }}>
            <Typography sx={{ flex: 1 }}>{c.name}</Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>{c.scope ? t(`knowledge.type.${c.scope}`) : t('knowledge.admin.taxonomy.anyType')}</Typography>
            <FormControlLabel control={<Checkbox size="small" checked={c.active} onChange={(e) => run(knowledgeAdminApi.saveCategory(c.id, { ...c, active: e.target.checked }))} />} label={t('knowledge.admin.taxonomy.active')} />
          </Box>
        ))}
        <Box sx={{ display: 'flex', gap: 1, mt: 1, flexWrap: 'wrap' }}>
          <TextField size="small" label={t('knowledge.admin.taxonomy.name')} value={newCategory} onChange={(e) => setNewCategory(e.target.value)} sx={{ flex: 1, minWidth: 140 }} />
          <TextField select size="small" label={t('knowledge.admin.taxonomy.scope')} value={scope} onChange={(e) => setScope(e.target.value as ContentType | '')} sx={{ minWidth: 150 }}>
            <MenuItem value="">{t('knowledge.admin.taxonomy.anyType')}</MenuItem>
            {CONTENT_TYPES.map((ct) => <MenuItem key={ct} value={ct}>{t(`knowledge.type.${ct}`)}</MenuItem>)}
          </TextField>
          <Button variant="outlined" disabled={!newCategory.trim()} onClick={() => { run(knowledgeAdminApi.saveCategory(null, { name: newCategory, scope: scope || null })); setNewCategory('') }}>{t('knowledge.admin.taxonomy.add')}</Button>
        </Box>
      </Paper>
    </Box>
  )
}
