import { Box, Button, InputAdornment, TextField, Typography } from '@mui/material'
import { Search } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { SECTIONS, rememberSearch } from '../../components/knowledge/knowledgeUtils'

/** Search box used in the Knowledge Center header and hero. */
export function KnowledgeSearchBox({ initial = '', large = false }: { initial?: string; large?: boolean }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [value, setValue] = useState(initial)
  const submit = (e: FormEvent) => {
    e.preventDefault()
    if (!value.trim()) return
    rememberSearch(value)
    navigate(`/knowledge/search?q=${encodeURIComponent(value.trim())}`)
  }
  return (
    <Box component="form" role="search" onSubmit={submit} sx={{ display: 'flex', gap: 1, width: '100%' }}>
      <TextField fullWidth size={large ? 'medium' : 'small'} value={value} onChange={(e) => setValue(e.target.value)}
        placeholder={t('knowledge.searchPlaceholder')} slotProps={{
          htmlInput: { 'aria-label': t('knowledge.searchLabel') },
          input: { startAdornment: <InputAdornment position="start"><Search size={18} aria-hidden /></InputAdornment> },
        }} />
      <Button type="submit" variant="contained">{t('knowledge.searchButton')}</Button>
    </Box>
  )
}

/**
 * Knowledge Center frame (REQ-KNW-005.1/.2): title, subtitle, search box and
 * section navigation, inside the existing EIS header and sidebar.
 */
export function KnowledgeCenterLayout() {
  const { t } = useTranslation()
  const location = useLocation()
  const home = location.pathname === '/knowledge'
  return (
    <Box>
      {!home && (
        <Box component="header" sx={{ mb: 2, display: 'flex', gap: 2, alignItems: { md: 'center' }, flexDirection: { xs: 'column', md: 'row' } }}>
          <Box sx={{ flex: 1 }}>
            <Typography variant="overline" sx={{ color: 'primary.main' }}>{t('knowledge.title')}</Typography>
            <Typography sx={{ color: 'text.secondary' }}>{t('knowledge.subtitle')}</Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', md: 460 } }}><KnowledgeSearchBox key={location.search} initial={new URLSearchParams(location.search).get('q') ?? ''} /></Box>
        </Box>
      )}
      <Box component="nav" aria-label={t('knowledge.nav.label')} sx={{ display: 'flex', gap: 0.5, overflowX: 'auto', pb: 1, mb: 2, borderBottom: 1, borderColor: 'divider' }}>
        {[{ key: 'home', path: '/knowledge' }, ...SECTIONS].map((s) => (
          <Box key={s.key} component={NavLink} to={s.path} end={s.path === '/knowledge'} sx={{
            px: 1.5, py: 0.75, borderRadius: 2, whiteSpace: 'nowrap', textDecoration: 'none', color: 'text.secondary', fontWeight: 600, fontSize: 14,
            '&.active': { color: 'primary.main', bgcolor: 'action.selected' }, '&:hover': { bgcolor: 'action.hover' },
          }}>
            {t(`knowledge.nav.${s.key}`)}
          </Box>
        ))}
      </Box>
      <Outlet />
    </Box>
  )
}

/** Small section heading with an optional action. */
export function SectionHeading({ id, title, subtitle, action }: { id?: string; title: string; subtitle?: string; action?: React.ReactNode }) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', gap: 2, mb: 1.5, flexWrap: 'wrap' }}>
      <Box>
        <Typography id={id} component="h2" sx={{ fontWeight: 700, fontSize: 20 }}>{title}</Typography>
        {subtitle && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{subtitle}</Typography>}
      </Box>
      {action}
    </Box>
  )
}
