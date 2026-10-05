import { useCallback, useEffect, useId, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, ClickAwayListener, InputBase, Paper, Popper, Typography } from '@mui/material'
import { BookOpen, Clock, LifeBuoy, Package, Search } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { globalSearchApi, SEARCH_LINK_FOR, type SearchResultType, type SearchSuggestion } from '../../api/globalSearchApi'
import { searchHistoryApi } from '../../api/searchHistoryApi'
import { HighlightedText } from '../search/HighlightedText'

const TYPE_ICON: Record<SearchResultType, typeof Package> = { PRODUCT: Package, KNOWLEDGE: BookOpen, TICKET: LifeBuoy }

type Option =
  | { kind: 'suggestion'; suggestion: SearchSuggestion }
  | { kind: 'recent'; query: string }
  | { kind: 'all'; query: string }

const isMac = typeof navigator !== 'undefined' && /Mac|iPhone|iPad/.test(navigator.platform)

/** Ranges of `needle` (case- and accent-insensitive) in `text`, for highlighting. */
function matchRanges(text: string, needle: string) {
  const fold = (s: string) => s.normalize('NFD').replace(/\p{M}+/gu, '').toLowerCase()
  const folded = fold(text)
  const target = fold(needle.trim())
  if (!target || folded.length !== text.length) return []
  const index = folded.indexOf(target)
  return index < 0 ? [] : [{ start: index, length: target.length }]
}

/**
 * C44/C70: the top-bar search. Typing shows suggestions (records whose title
 * matches, from /search/suggest); with an empty box it shows recent searches.
 * Arrow keys move through the list, Enter opens the highlighted item or the
 * full results page, Escape closes. Ctrl+K (⌘K on Mac) focuses the box from
 * anywhere in the app.
 */
export function TopBarSearch({ showRecent = true }: { showRecent?: boolean }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const listId = useId()
  const inputRef = useRef<HTMLInputElement | null>(null)
  const [anchorEl, setAnchorEl] = useState<HTMLDivElement | null>(null)
  const [query, setQuery] = useState('')
  const [suggestions, setSuggestions] = useState<SearchSuggestion[] | null>(null)
  const [recent, setRecent] = useState<string[]>([])
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(-1)

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault()
        inputRef.current?.focus()
        inputRef.current?.select()
        setOpen(true)
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  const trimmed = query.trim()
  useEffect(() => {
    if (trimmed.length < 2) return
    let cancelled = false
    const handle = setTimeout(() => {
      globalSearchApi.suggest(trimmed)
        .then((s) => { if (!cancelled) setSuggestions(s) })
        .catch(() => { if (!cancelled) setSuggestions([]) })
    }, 150)
    return () => { cancelled = true; clearTimeout(handle) }
  }, [trimmed])

  const loadRecent = useCallback(() => {
    if (!showRecent) return
    searchHistoryApi.list().then((entries) => setRecent(entries.map((e) => e.query).slice(0, 5))).catch(() => setRecent([]))
  }, [showRecent])

  const options: Option[] = useMemo(() => {
    if (!trimmed) return recent.map((q) => ({ kind: 'recent' as const, query: q }))
    const list: Option[] = trimmed.length >= 2 && suggestions
      ? suggestions.map((s) => ({ kind: 'suggestion' as const, suggestion: s }))
      : []
    list.push({ kind: 'all', query: trimmed })
    return list
  }, [trimmed, suggestions, recent])

  const goToResults = (q: string) => {
    setOpen(false)
    setActive(-1)
    if (q.trim()) navigate(`/search?q=${encodeURIComponent(q.trim())}`)
  }

  const choose = (option: Option) => {
    if (option.kind === 'suggestion') {
      setOpen(false)
      navigate(SEARCH_LINK_FOR[option.suggestion.type](option.suggestion.id))
    } else {
      setQuery(option.query)
      goToResults(option.query)
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      setOpen(true)
      setActive((i) => (options.length === 0 ? -1 : (i + 1) % options.length))
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      setActive((i) => (options.length === 0 ? -1 : (i <= 0 ? options.length - 1 : i - 1)))
    } else if (e.key === 'Enter') {
      e.preventDefault()
      if (active >= 0 && options[active]) choose(options[active])
      else goToResults(query)
    } else if (e.key === 'Escape') {
      setOpen(false)
      setActive(-1)
    }
  }

  const showList = open && options.length > 0
  const optionId = (index: number) => `${listId}-option-${index}`

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
          <Search size={16} style={{ opacity: 0.6, flexShrink: 0 }} aria-hidden />
          <InputBase
            fullWidth
            inputRef={inputRef}
            placeholder={t('search.topBarPlaceholder')}
            value={query}
            onChange={(e) => { setQuery(e.target.value); setOpen(true); setActive(-1) }}
            onFocus={() => { setOpen(true); if (!query) loadRecent() }}
            onKeyDown={handleKeyDown}
            inputProps={{
              'aria-label': t('search.title'),
              role: 'combobox',
              'aria-expanded': showList,
              'aria-controls': listId,
              'aria-autocomplete': 'list',
              'aria-activedescendant': active >= 0 ? optionId(active) : undefined,
            }}
            sx={{ fontSize: 14 }}
          />
          <Box
            component="kbd"
            aria-label={t('search.shortcut', { keys: isMac ? '⌘K' : 'Ctrl+K' })}
            sx={{
              flexShrink: 0, px: 0.75, py: 0.125, borderRadius: 1, border: '1px solid', borderColor: 'divider',
              fontFamily: 'inherit', fontSize: 11, color: 'text.secondary', bgcolor: 'background.paper',
            }}
          >
            {isMac ? '⌘K' : 'Ctrl K'}
          </Box>
        </Box>

        <Popper open={showList} anchorEl={anchorEl} placement="bottom-start" style={{ width: anchorEl?.offsetWidth, zIndex: 1300 }}>
          <Paper elevation={4} sx={{ mt: 0.5, maxHeight: 420, overflowY: 'auto', py: 0.5 }}>
            {!trimmed && (
              <Typography sx={{ px: 2, py: 0.5, fontSize: 11, fontWeight: 700, letterSpacing: '.04em', textTransform: 'uppercase', color: 'text.secondary' }}>
                {t('search.recentSearches')}
              </Typography>
            )}
            {trimmed.length >= 2 && suggestions === null && (
              <Typography sx={{ px: 2, py: 1, fontSize: 13, color: 'text.secondary' }}>{t('search.searching')}</Typography>
            )}
            <Box component="ul" role="listbox" id={listId} aria-label={t('search.suggestions')} sx={{ listStyle: 'none', m: 0, p: 0 }}>
              {options.map((option, index) => {
                const selected = index === active
                const common = {
                  id: optionId(index),
                  role: 'option',
                  'aria-selected': selected,
                  onMouseEnter: () => setActive(index),
                  onMouseDown: (e: React.MouseEvent) => e.preventDefault(),
                  onClick: () => choose(option),
                }
                const rowSx = {
                  display: 'flex', alignItems: 'center', gap: 1.25, px: 2, py: 1, cursor: 'pointer', fontSize: 14,
                  bgcolor: selected ? 'action.selected' : 'transparent',
                }
                if (option.kind === 'suggestion') {
                  const Icon = TYPE_ICON[option.suggestion.type]
                  return (
                    <Box component="li" key={`s-${option.suggestion.type}-${option.suggestion.id}`} {...common} sx={rowSx}>
                      <Icon size={15} style={{ opacity: 0.6, flexShrink: 0 }} aria-hidden />
                      <Box sx={{ flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontWeight: 600 }}>
                        <HighlightedText text={option.suggestion.title} highlights={matchRanges(option.suggestion.title, trimmed)} />
                      </Box>
                      <Typography component="span" sx={{ fontSize: 11.5, color: 'text.secondary', flexShrink: 0 }}>
                        {t(`search.typeLabel.${option.suggestion.type}`)}
                      </Typography>
                    </Box>
                  )
                }
                if (option.kind === 'recent') {
                  return (
                    <Box component="li" key={`r-${option.query}`} {...common} sx={rowSx}>
                      <Clock size={15} style={{ opacity: 0.6, flexShrink: 0 }} aria-hidden />
                      <span>{option.query}</span>
                    </Box>
                  )
                }
                return (
                  <Box
                    component="li"
                    key="all"
                    {...common}
                    sx={{ ...rowSx, fontSize: 13, fontWeight: 600, color: 'primary.main', borderTop: options.length > 1 ? '1px solid' : 'none', borderColor: 'divider' }}
                  >
                    <Search size={15} style={{ flexShrink: 0 }} aria-hidden />
                    {t('search.seeAllResults', { query: option.query })}
                  </Box>
                )
              })}
            </Box>
          </Paper>
        </Popper>
      </Box>
    </ClickAwayListener>
  )
}
