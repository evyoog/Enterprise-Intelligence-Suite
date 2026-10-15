import { useTranslation } from 'react-i18next'
import { Box, Button, IconButton, Paper, Typography } from '@mui/material'
import { GitCompare, X } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { MIN_COMPARE_ITEMS } from '../../api/compareApi'
import { useCompare } from './CompareContext'

const TRAY_HEIGHT = 72

/**
 * C102 (REQ-MKT-005): the floating tray at the bottom of the catalog once a card is ticked "Compare":
 * the count, the chosen products (named when `names` knows them), Compare now and Clear.
 */
export function CompareTray({ names }: { names?: ReadonlyMap<number, string> }) {
  const { t } = useTranslation()
  const { ids, remove, clear } = useCompare()
  if (ids.length === 0) return null
  const ready = ids.length >= MIN_COMPARE_ITEMS
  return (
    <>
      {/* Keeps the end of the page clear of the fixed tray. */}
      <Box aria-hidden sx={{ height: TRAY_HEIGHT }} />
      <Paper component="section" role="region" aria-label={t('compare.tray.label')} elevation={8}
        sx={{
          position: 'fixed', left: 0, right: 0, bottom: 0, zIndex: (theme) => theme.zIndex.drawer + 1, px: { xs: 2, md: 3 }, py: 1.25,
          display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap', borderRadius: 0, borderTop: 1, borderColor: 'divider',
          minHeight: TRAY_HEIGHT,
        }}>
        <Typography component="span" aria-live="polite" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.75, fontWeight: 700 }}>
          <GitCompare size={18} aria-hidden />
          {t('compare.tray.count', { count: ids.length })}
        </Typography>
        <Box component="ul" sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap', flex: 1, minWidth: 0, listStyle: 'none', m: 0, p: 0 }}>
          {ids.map((id) => {
            const name = names?.get(id) ?? t('compare.chooser.unknownProduct', { id })
            return (
              <Box component="li" key={id} sx={{
                display: 'inline-flex', alignItems: 'center', gap: 0.25, pl: 1.25, pr: 0.25, borderRadius: 4, border: 1, borderColor: 'divider', bgcolor: 'action.hover', maxWidth: 200,
              }}>
                <Typography component="span" noWrap variant="body2" sx={{ fontWeight: 500 }}>{name}</Typography>
                <IconButton size="small" aria-label={t('compare.tray.remove', { name })} onClick={() => remove(id)}>
                  <X size={14} aria-hidden />
                </IconButton>
              </Box>
            )
          })}
        </Box>
        {!ready && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('compare.tray.needMore')}</Typography>}
        <Button variant="text" onClick={clear}>{t('compare.tray.clear')}</Button>
        {ready
          ? <Button variant="contained" component={RouterLink} to={`/compare?items=${ids.join(',')}`}>{t('compare.tray.now')}</Button>
          : <Button variant="contained" disabled>{t('compare.tray.now')}</Button>}
      </Paper>
    </>
  )
}
