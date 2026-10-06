import { Box, Checkbox, FormControlLabel, MenuItem, TextField } from '@mui/material'
import { useTranslation } from 'react-i18next'
import type { Audience, ContentInput, Taxonomy } from '../../api/knowledgeApi'

const AUDIENCES: Audience[] = ['PUBLIC', 'CUSTOMER', 'ORGANIZATION', 'ADMIN']
const ids = (v: string) => v.split(',').map((x) => Number(x.trim())).filter((x) => Number.isInteger(x) && x > 0)
const toLocal = (iso?: string | null) => (iso ? iso.slice(0, 16) : '')
const fromLocal = (v: string) => (v ? new Date(v).toISOString() : null)

/** The metadata every content type shares (REQ-KNW-002.1): taxonomy, audience, dates, relations. */
export function ContentMetadataFields({ value, onChange, taxonomy, disabled, required = false }: {
  value: ContentInput; onChange: (patch: Partial<ContentInput>) => void; taxonomy: Taxonomy; disabled?: boolean; required?: boolean
}) {
  const { t } = useTranslation()
  const modules = taxonomy.products.find((p) => p.id === value.productId)?.modules ?? []
  const categories = taxonomy.categories.filter((c) => !c.scope || c.scope === value.contentType)
  const grid = { display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' } }
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Box sx={grid}>
        <TextField select size="small" required={required} disabled={disabled} label={t('knowledge.admin.editor.product')} value={value.productId ?? ''}
          onChange={(e) => onChange({ productId: e.target.value ? Number(e.target.value) : null, moduleId: null })}>
          <MenuItem value="">{t('knowledge.admin.editor.none')}</MenuItem>
          {taxonomy.products.filter((p) => p.active).map((p) => <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>)}
        </TextField>
        <TextField select size="small" required={required} disabled={disabled || !value.productId} label={t('knowledge.admin.editor.module')} value={value.moduleId ?? ''}
          onChange={(e) => onChange({ moduleId: e.target.value ? Number(e.target.value) : null })}>
          <MenuItem value="">{t('knowledge.admin.editor.none')}</MenuItem>
          {modules.filter((m) => m.active).map((m) => <MenuItem key={m.id} value={m.id}>{m.name}</MenuItem>)}
        </TextField>
        <TextField select size="small" required={required} disabled={disabled} label={t('knowledge.admin.editor.category')} value={value.categoryId ?? ''}
          onChange={(e) => onChange({ categoryId: e.target.value ? Number(e.target.value) : null })}>
          <MenuItem value="">{t('knowledge.admin.editor.none')}</MenuItem>
          {categories.filter((c) => c.active).map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
        </TextField>
        <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.feature')} value={value.feature ?? ''} onChange={(e) => onChange({ feature: e.target.value })} />
        <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.tags')} value={(value.tags ?? []).join(', ')}
          onChange={(e) => onChange({ tags: e.target.value.split(',').map((x) => x.trim()).filter(Boolean) })} />
        <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.keywords')} value={value.keywords ?? ''} onChange={(e) => onChange({ keywords: e.target.value })} />
      </Box>
      <Box sx={grid}>
        <TextField select size="small" disabled={disabled} label={t('knowledge.admin.editor.audience')} value={value.audience ?? 'PUBLIC'}
          onChange={(e) => onChange({ audience: e.target.value as Audience })}>
          {AUDIENCES.map((a) => <MenuItem key={a} value={a}>{t(`knowledge.admin.audience.${a}`)}</MenuItem>)}
        </TextField>
        {value.audience === 'ORGANIZATION' && (
          <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.organizations')} value={(value.audienceOrganizationIds ?? []).join(', ')}
            onChange={(e) => onChange({ audienceOrganizationIds: ids(e.target.value) })} />
        )}
        <FormControlLabel control={<Checkbox disabled={disabled || !value.productId} checked={!!value.requireProductAccess} onChange={(e) => onChange({ requireProductAccess: e.target.checked })} />}
          label={t('knowledge.admin.editor.requireProductAccess')} />
        <FormControlLabel control={<Checkbox disabled={disabled} checked={!!value.featured} onChange={(e) => onChange({ featured: e.target.checked })} />} label={t('knowledge.admin.editor.featured')} />
      </Box>
      <Box sx={grid}>
        <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.productVersion')} value={value.productVersion ?? ''} onChange={(e) => onChange({ productVersion: e.target.value })} />
        <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.documentationVersion')} value={value.documentationVersion ?? ''} onChange={(e) => onChange({ documentationVersion: e.target.value })} />
        <TextField size="small" type="datetime-local" disabled={disabled} label={t('knowledge.admin.editor.effectiveAt')} slotProps={{ inputLabel: { shrink: true } }}
          value={toLocal(value.effectiveAt)} onChange={(e) => onChange({ effectiveAt: fromLocal(e.target.value) })} />
        <TextField size="small" type="datetime-local" disabled={disabled} label={t('knowledge.admin.editor.reviewAt')} slotProps={{ inputLabel: { shrink: true } }}
          value={toLocal(value.reviewAt)} onChange={(e) => onChange({ reviewAt: fromLocal(e.target.value) })} />
        <TextField size="small" type="datetime-local" disabled={disabled} label={t('knowledge.admin.editor.expiresAt')} slotProps={{ inputLabel: { shrink: true } }}
          value={toLocal(value.expiresAt)} onChange={(e) => onChange({ expiresAt: fromLocal(e.target.value) })} />
        <TextField select size="small" disabled={disabled} label={t('knowledge.admin.editor.difficulty')} value={value.difficulty ?? ''} onChange={(e) => onChange({ difficulty: e.target.value || null })}>
          <MenuItem value="">{t('knowledge.admin.editor.none')}</MenuItem>
          {['BEGINNER', 'INTERMEDIATE', 'ADVANCED'].map((d) => <MenuItem key={d} value={d}>{t(`knowledge.difficulty.${d}`)}</MenuItem>)}
        </TextField>
      </Box>
      <Box sx={grid}>
        <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.directRoute')} value={value.directActionRoute ?? ''} onChange={(e) => onChange({ directActionRoute: e.target.value })} />
        <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.directLabel')} value={value.directActionLabel ?? ''} onChange={(e) => onChange({ directActionLabel: e.target.value })} />
        <TextField size="small" disabled={disabled} label={t('knowledge.admin.editor.related')} value={(value.relatedContentIds ?? []).join(', ')}
          onChange={(e) => onChange({ relatedContentIds: ids(e.target.value) })} />
      </Box>
    </Box>
  )
}
