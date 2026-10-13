import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Button, Checkbox, FormControlLabel, FormGroup, Paper, Typography } from '@mui/material'
import { ChevronDown, ChevronUp } from 'lucide-react'
import { activeFilterCount, type CatalogFilters, type FacetCount, type PricingModel } from '../../utils/catalogFilters'

const FEATURES_SHOWN = 6

interface Facets {
  platforms: FacetCount<number>[]
  pricingModels: FacetCount<PricingModel>[]
  features: FacetCount<string>[]
}

function toggled<T>(list: T[], value: T): T[] {
  return list.includes(value) ? list.filter((v) => v !== value) : [...list, value]
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  const [open, setOpen] = useState(true)
  const id = `filter-${title.replace(/\W+/g, '-').toLowerCase()}`
  return (
    <Box component="fieldset" sx={{ border: 0, m: 0, p: 0, minWidth: 0, borderTop: '1px solid', borderColor: 'divider', py: 1.5 }}>
      <Box component="legend" sx={{ float: 'left', width: '100%', p: 0 }}>
        <Box component="button" type="button" aria-expanded={open} aria-controls={id} onClick={() => setOpen((o) => !o)} sx={{
          display: 'flex', width: '100%', alignItems: 'center', justifyContent: 'space-between', background: 'none', border: 0, p: 0, cursor: 'pointer',
          font: 'inherit', fontWeight: 700, fontSize: 14, color: 'text.primary',
        }}>
          {title}{open ? <ChevronUp size={16} aria-hidden /> : <ChevronDown size={16} aria-hidden />}
        </Box>
      </Box>
      <Box id={id} hidden={!open} sx={{ clear: 'both', pt: 0.5 }}>{children}</Box>
    </Box>
  )
}

/** C94 (REQ-CAT-003.12): the filter panel beside the product grid: platform, pricing model, features, saved. Counts come from the loaded products. */
export function CatalogFilterPanel({ facets, filters, onChange, canFilterSaved }: {
  facets: Facets; filters: CatalogFilters; onChange: (next: CatalogFilters) => void; canFilterSaved: boolean
}) {
  const { t } = useTranslation()
  const [allFeatures, setAllFeatures] = useState(false)
  const features = allFeatures ? facets.features : facets.features.slice(0, FEATURES_SHOWN)
  const count = activeFilterCount(filters)
  const option = (key: string, label: string, n: number, checked: boolean, onToggle: () => void) => (
    <FormControlLabel key={key} sx={{ mr: 0, '& .MuiFormControlLabel-label': { fontSize: 13, display: 'flex', width: '100%', justifyContent: 'space-between', gap: 1 } }}
      control={<Checkbox size="small" checked={checked} onChange={onToggle} />}
      label={<><span>{label}</span><Typography component="span" sx={{ color: 'text.secondary', fontSize: 12 }}>{n}</Typography></>} />
  )
  return (
    <Paper variant="outlined" component="aside" aria-label={t('catalog.filters.title')} sx={{ p: 2, borderRadius: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
        <Typography component="h2" sx={{ fontWeight: 800, fontSize: 16 }}>{t('catalog.filters.title')}</Typography>
        <Button size="small" disabled={count === 0} onClick={() => onChange({ platformIds: [], pricingModels: [], features: [], savedOnly: false })}>
          {t('catalog.filters.clearAll')}
        </Button>
      </Box>
      {canFilterSaved && (
        <FormControlLabel sx={{ mb: 1 }} control={<Checkbox size="small" checked={filters.savedOnly} onChange={() => onChange({ ...filters, savedOnly: !filters.savedOnly })} />}
          label={<Typography sx={{ fontSize: 13 }}>{t('catalog.filters.savedOnly')}</Typography>} />
      )}
      {facets.platforms.length > 0 && (
        <Section title={t('catalog.filters.platform')}>
          <FormGroup>{facets.platforms.map((p) => option(`p${p.value}`, p.label ?? '', p.count, filters.platformIds.includes(p.value),
            () => onChange({ ...filters, platformIds: toggled(filters.platformIds, p.value) })))}</FormGroup>
        </Section>
      )}
      {facets.pricingModels.length > 0 && (
        <Section title={t('catalog.filters.pricingModel')}>
          <FormGroup>{facets.pricingModels.map((m) => option(m.value, t(`catalog.pricingModel.${m.value}`), m.count, filters.pricingModels.includes(m.value),
            () => onChange({ ...filters, pricingModels: toggled(filters.pricingModels, m.value) })))}</FormGroup>
        </Section>
      )}
      {facets.features.length > 0 && (
        <Section title={t('catalog.filters.features')}>
          <FormGroup>{features.map((f) => option(`f${f.value}`, f.value, f.count, filters.features.includes(f.value),
            () => onChange({ ...filters, features: toggled(filters.features, f.value) })))}</FormGroup>
          {facets.features.length > FEATURES_SHOWN && (
            <Button size="small" onClick={() => setAllFeatures((v) => !v)} sx={{ mt: 0.5 }}>
              {allFeatures ? t('catalog.filters.showLess') : t('catalog.filters.showMore')}
            </Button>
          )}
        </Section>
      )}
    </Paper>
  )
}
