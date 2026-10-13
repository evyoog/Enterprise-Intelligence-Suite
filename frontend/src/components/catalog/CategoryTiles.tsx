import { useTranslation } from 'react-i18next'
import { Box, Typography } from '@mui/material'
import { LayoutGrid, Tag } from 'lucide-react'
import type { CategoryFacet } from '../../api/productsApi'

/** C94 (REQ-CAT-003.11): the row of category tiles with product counts (replaces the category chips). One category at a time. */
export function CategoryTiles({ facets, total, selected, onSelect }: {
  facets: CategoryFacet[]; total: number; selected: string | null; onSelect: (category: string | null) => void
}) {
  const { t } = useTranslation()
  const tiles: { key: string; value: string | null; label: string; count: number; all?: boolean }[] = [
    { key: '__all', value: null, label: t('catalog.all'), count: total, all: true },
    ...facets.map((f) => ({ key: f.category, value: f.category, label: f.category, count: f.count })),
  ]
  return (
    <Box role="group" aria-label={t('catalog.filterLabel')} sx={{ display: 'flex', gap: 1.5, overflowX: 'auto', pb: 1, mb: 2, scrollSnapType: 'x proximity' }}>
      {tiles.map((tile) => {
        const active = selected === tile.value
        const Icon = tile.all ? LayoutGrid : Tag
        return (
          <Box key={tile.key} component="button" type="button" aria-pressed={active} onClick={() => onSelect(tile.value)} sx={{
            flex: '0 0 auto', minWidth: 132, display: 'flex', alignItems: 'center', gap: 1.25, p: 1.25, textAlign: 'left', cursor: 'pointer', font: 'inherit',
            borderRadius: 3, border: '1px solid', borderColor: active ? 'primary.main' : 'divider', color: 'text.primary', scrollSnapAlign: 'start',
            bgcolor: active ? 'action.selected' : 'background.paper', transition: 'border-color .15s, box-shadow .15s',
            '&:hover': { borderColor: 'primary.main', boxShadow: '0 4px 14px -8px rgba(99,102,241,.5)' },
            '&:focus-visible': { outline: '2px solid', outlineColor: 'primary.main', outlineOffset: 2 },
          }}>
            <Box sx={{ width: 34, height: 34, borderRadius: 2, display: 'grid', placeItems: 'center', bgcolor: active ? 'primary.main' : 'action.hover', color: active ? 'primary.contrastText' : 'primary.main' }}>
              <Icon size={17} aria-hidden />
            </Box>
            <Box sx={{ minWidth: 0 }}>
              <Typography sx={{ fontWeight: 700, fontSize: 13, lineHeight: 1.2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{tile.label}</Typography>
              <Typography sx={{ color: 'text.secondary', fontSize: 12 }}>{t('catalog.tiles.products', { count: tile.count })}</Typography>
            </Box>
          </Box>
        )
      })}
    </Box>
  )
}
