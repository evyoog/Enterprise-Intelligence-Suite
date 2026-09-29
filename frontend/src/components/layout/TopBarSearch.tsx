import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, ClickAwayListener, InputBase, Paper, Popper, Typography } from '@mui/material'
import { Search } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { globalSearchApi, type GlobalSearchResult, type SearchResultItem } from '../../api/globalSearchApi'

const LINK_FOR: Record<SearchResultItem['type'], (id: number) => string> = {
  PRODUCT: (id) => `/products/${id}`,
  KNOWLEDGE: () => '/knowledge-base',
  TICKET: () => '/support/tickets',
}

/**
 * C44: 01.03 Global Search moved out of the sidebar into the top bar — a
 * command-style search box (matching AWS/Salesforce/Zoho's own top-bar
 * search) instead of a dedicated nav destination. Typing shows a live,
 * grouped preview inline; Enter or "See all results" goes to the full
 * `/search` page (still a real route, just not a permanent nav item — see
 * appNavigation.ts's own comment).
 */
export function TopBarSearch() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [anchorEl, setAnchorEl] = useState<HTMLDivElement | null>(null)
  const [query, setQuery] = useState('')
  const [result, setResult] = useState<GlobalSearchResult | null>(null)
  const [open, setOpen] = useState(false)

  useEffect(() => {
    if (!query.trim()) return
    const handle = setTimeout(() => {
      globalSearchApi.search(query).then(setResult).catch(() => setResult(null))
    }, 250)
    return () => clearTimeout(handle)
  }, [query])

  const goToFullResults = () => {
    setOpen(false)
    navigate(`/search?q=${encodeURIComponent(query)}`)
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') goToFullResults()
    if (e.key === 'Escape') setOpen(false)
  }

  const groups: { title: string; items: SearchResultItem[] }[] = result ? [
    { title: t('search.products'), items: result.products.slice(0, 3) },
    { title: t('search.knowledgeArticles'), items: result.knowledgeArticles.slice(0, 3) },
    { title: t('search.tickets'), items: result.tickets.slice(0, 3) },
  ].filter((g) => g.items.length > 0) : []

  return (
    <ClickAwayListener onClickAway={() => setOpen(false)}>
      <Box ref={setAnchorEl} sx={{ position: 'relative', flex: 1, maxWidth: 480 }}>
        <Box
          sx={{
            display: 'flex', alignItems: 'center', gap: 1, px: 1.5, height: 38, borderRadius: 999,
            bgcolor: 'action.hover', border: '1px solid', borderColor: 'divider',
            '&:focus-within': { borderColor: 'primary.main' },
          }}
        >
          <Search size={16} style={{ opacity: 0.6, flexShrink: 0 }} />
          <InputBase
            fullWidth
            placeholder={t('search.topBarPlaceholder')}
            value={query}
            onChange={(e) => { setQuery(e.target.value); setOpen(true) }}
            onFocus={() => query && setOpen(true)}
            onKeyDown={handleKeyDown}
            inputProps={{ 'aria-label': t('search.title') }}
            sx={{ fontSize: 14 }}
          />
        </Box>

        <Popper
          open={open && query.trim().length > 0}
          anchorEl={anchorEl}
          placement="bottom-start"
          style={{ width: anchorEl?.offsetWidth, zIndex: 1300 }}
        >
          <Paper elevation={4} sx={{ mt: 0.5, maxHeight: 420, overflowY: 'auto' }}>
            {!result && (
              <Typography sx={{ p: 2, fontSize: 13, color: 'text.secondary' }}>{t('search.searching')}</Typography>
            )}
            {result && groups.length === 0 && (
              <Typography sx={{ p: 2, fontSize: 13, color: 'text.secondary' }}>{t('search.noResults')}</Typography>
            )}
            {groups.map((group) => (
              <Box key={group.title} sx={{ py: 0.5 }}>
                <Typography sx={{ px: 2, py: 0.5, fontSize: 11, fontWeight: 700, letterSpacing: '.04em', textTransform: 'uppercase', color: 'text.secondary' }}>
                  {group.title}
                </Typography>
                {group.items.map((item) => (
                  <Box
                    key={`${item.type}-${item.id}`}
                    onClick={() => { setOpen(false); navigate(LINK_FOR[item.type](item.id)) }}
                    sx={{ px: 2, py: 1, cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
                  >
                    <Typography sx={{ fontSize: 14, fontWeight: 600 }}>{item.title}</Typography>
                    {item.snippet && (
                      <Typography noWrap sx={{ fontSize: 12, color: 'text.secondary' }}>{item.snippet}</Typography>
                    )}
                  </Box>
                ))}
              </Box>
            ))}
            {result && (
              <Box
                onClick={goToFullResults}
                sx={{
                  px: 2, py: 1.25, cursor: 'pointer', fontSize: 13, fontWeight: 600, color: 'primary.main',
                  borderTop: '1px solid', borderColor: 'divider', '&:hover': { bgcolor: 'action.hover' },
                }}
              >
                {t('search.seeAllResults', { query })}
              </Box>
            )}
          </Paper>
        </Popper>
      </Box>
    </ClickAwayListener>
  )
}
