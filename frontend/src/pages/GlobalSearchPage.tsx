import { BookOpen, LifeBuoy, Package, Search as PHSearch, X } from 'lucide-react'
import { useCallback, useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, IconButton, InputAdornment, Link, Paper, Skeleton, TextField, Typography } from '@mui/material'
import { Link as RouterLink, useSearchParams } from 'react-router-dom'
import {
  globalSearchApi, rankedResults, SEARCH_LINK_FOR,
  type GlobalSearchResult, type SearchResultItem, type SearchResultType,
} from '../api/globalSearchApi'
import { searchHistoryApi } from '../api/searchHistoryApi'
import { useAuth } from '../auth/AuthProvider'
import { PageHeader } from '../components/layout/PageHeader'
import { HighlightedText } from '../components/search/HighlightedText'
import { MatchLabel } from '../components/search/MatchLabel'

const TYPE_ICON: Record<SearchResultType, typeof Package> = { PRODUCT: Package, KNOWLEDGE: BookOpen, TICKET: LifeBuoy }
const FILTERS: (SearchResultType | 'ALL')[] = ['ALL', 'PRODUCT', 'KNOWLEDGE', 'TICKET']

/**
 * "/search" — 01.03.01 Unified Search: keyword search (REQ-PRT-002) and
 * semantic search (REQ-PRT-003), C70. Ranked results with a match label and
 * highlighted words, type filters, "Did you mean", help when nothing matches
 * and (signed in) recent searches with Clear history. Typing searches as you
 * go; a search the user runs (Enter, or arriving from the top bar) is saved
 * in their history and counted in search insights.
 */
export function GlobalSearchPage() {
  const { t } = useTranslation()
  const auth = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const [query, setQuery] = useState(searchParams.get('q') ?? '')
  const [type, setType] = useState<SearchResultType | 'ALL'>(() => {
    const fromUrl = searchParams.get('type')?.toUpperCase()
    return FILTERS.includes(fromUrl as SearchResultType) ? (fromUrl as SearchResultType) : 'ALL'
  })
  const [result, setResult] = useState<GlobalSearchResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [failed, setFailed] = useState(false)
  const [history, setHistory] = useState<string[] | null>(null)
  const requestId = useRef(0)
  const initialRun = useRef(true)

  const run = useCallback((q: string, filter: SearchResultType | 'ALL', tracked: boolean) => {
    const id = ++requestId.current
    setLoading(true)
    setSearchParams(() => {
      const next = new URLSearchParams()
      if (q) next.set('q', q)
      if (filter !== 'ALL') next.set('type', filter.toLowerCase())
      return next
    }, { replace: true })
    globalSearchApi.search(q, { type: filter === 'ALL' ? undefined : filter, track: tracked && !!q.trim() })
      .then((r) => { if (id === requestId.current) { setResult(r); setFailed(false) } })
      .catch(() => { if (id === requestId.current) { setResult(null); setFailed(true) } })
      .finally(() => { if (id === requestId.current) setLoading(false) })
    if (tracked && q.trim() && auth.isAuthenticated) {
      searchHistoryApi.record(q.trim()).then(() => setHistory(null)).catch(() => undefined)
    }
  }, [auth.isAuthenticated, setSearchParams])

  // Search as you type (not saved); the first run (from the URL) counts as a search the user ran.
  useEffect(() => {
    const tracked = initialRun.current
    initialRun.current = false
    const handle = setTimeout(() => run(query, type, tracked), tracked ? 0 : 300)
    return () => clearTimeout(handle)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, type])

  useEffect(() => {
    if (!auth.isAuthenticated || history !== null) return
    searchHistoryApi.list().then((entries) => setHistory(entries.map((e) => e.query))).catch(() => setHistory([]))
  }, [auth.isAuthenticated, history])

  const clearHistory = () => {
    searchHistoryApi.clear().then(() => setHistory([])).catch(() => undefined)
  }

  const results = result ? rankedResults(result) : []
  const trimmed = query.trim()
  const visibleFilters = FILTERS.filter((f) => f !== 'TICKET' || auth.isAuthenticated)

  return (
    <Box>
      <PageHeader icon={PHSearch} accent="blue" title={t('search.title')} subtitle={t('search.subtitle')} />
      <Box component="form" role="search" onSubmit={(e) => { e.preventDefault(); run(query, type, true) }} sx={{ mb: 2 }}>
        <TextField
          fullWidth
          autoFocus
          placeholder={t('search.placeholder')}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          slotProps={{
            htmlInput: { 'aria-label': t('search.title') },
            input: {
              startAdornment: <InputAdornment position="start"><PHSearch size={18} aria-hidden /></InputAdornment>,
              endAdornment: query ? (
                <InputAdornment position="end">
                  <IconButton size="small" aria-label={t('search.clearQuery')} onClick={() => setQuery('')}><X size={16} /></IconButton>
                </InputAdornment>
              ) : undefined,
            },
          }}
        />
      </Box>

      <Box role="group" aria-label={t('search.filterLabel')} sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
        {visibleFilters.map((f) => (
          <Chip
            key={f}
            label={f === 'ALL' ? t('search.filterAll') : t(`search.filter.${f}`)}
            color={type === f ? 'primary' : 'default'}
            variant={type === f ? 'filled' : 'outlined'}
            onClick={() => setType(f)}
            aria-pressed={type === f}
          />
        ))}
      </Box>

      {!trimmed && auth.isAuthenticated && history && history.length > 0 && (
        <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
            <Typography component="h2" sx={{ fontWeight: 700, fontSize: 15 }}>{t('search.recentSearches')}</Typography>
            <Button size="small" onClick={clearHistory}>{t('search.clearHistory')}</Button>
          </Box>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {history.map((h) => <Chip key={h} label={h} onClick={() => { setQuery(h); run(h, type, true) }} />)}
          </Box>
        </Paper>
      )}

      {result?.didYouMean && (
        <Typography sx={{ mb: 2 }}>
          {t('search.didYouMean')}{' '}
          <Link component="button" type="button" onClick={() => { setQuery(result.didYouMean!); run(result.didYouMean!, type, true) }} sx={{ fontWeight: 700, fontSize: 'inherit', verticalAlign: 'baseline' }}>
            {result.didYouMean}
          </Link>
          ?
        </Typography>
      )}

      {result?.semanticStatus === 'UNAVAILABLE' && trimmed && (
        <Alert severity="info" sx={{ mb: 2 }}>{t('search.semanticUnavailable')}</Alert>
      )}

      {failed && <Alert severity="error" sx={{ mb: 2 }}>{t('search.loadError')}</Alert>}

      {loading && !result && (
        <Box sx={{ display: 'grid', gap: 1 }}>{[0, 1, 2].map((i) => <Skeleton key={i} variant="rounded" height={72} />)}</Box>
      )}

      {result && trimmed && results.length > 0 && (
        <Typography sx={{ color: 'text.secondary', fontSize: 13, mb: 1 }} aria-live="polite">
          {t('search.resultCount', { count: results.length })}
          {result.semanticStatus === 'USED' ? ` · ${t('search.semanticUsed')}` : ''}
        </Typography>
      )}

      {result && results.length === 0 && !failed && (
        trimmed ? <NoResults query={trimmed} signedIn={auth.isAuthenticated} /> : (
          <Typography sx={{ color: 'text.secondary' }}>{t('search.noResults')}</Typography>
        )
      )}

      <Box component="ol" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1 }} aria-label={t('search.resultsLabel')}>
        {results.map((item) => <ResultRow key={`${item.type}-${item.id}`} item={item} />)}
      </Box>
    </Box>
  )
}

function ResultRow({ item }: { item: SearchResultItem }) {
  const { t } = useTranslation()
  const Icon = TYPE_ICON[item.type]
  return (
    <Paper component="li" variant="outlined" sx={{ p: 1.75, display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
      <Box sx={{ mt: 0.25, color: 'text.secondary', flexShrink: 0 }}><Icon size={18} aria-hidden /></Box>
      <Box sx={{ minWidth: 0, flex: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
          <Typography
            component={RouterLink}
            to={SEARCH_LINK_FOR[item.type](item.id)}
            sx={{ fontWeight: 700, textDecoration: 'none', color: 'inherit', '&:hover': { textDecoration: 'underline' }, '& mark': { bgcolor: 'warning.light', color: 'inherit', borderRadius: 0.5, px: 0.25 } }}
          >
            <HighlightedText text={item.title} highlights={item.titleHighlights} />
          </Typography>
          <MatchLabel matchType={item.matchType} />
        </Box>
        {item.snippet && (
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5, '& mark': { bgcolor: 'warning.light', color: 'text.primary', borderRadius: 0.5, px: 0.25 } }}>
            <HighlightedText text={item.snippet} highlights={item.snippetHighlights} />
          </Typography>
        )}
        <Typography sx={{ fontSize: 12, color: 'text.secondary', mt: 0.5 }}>
          {t(`search.typeLabel.${item.type}`)}{item.reference ? ` · ${item.reference}` : ''}
        </Typography>
      </Box>
    </Paper>
  )
}

function NoResults({ query, signedIn }: { query: string; signedIn: boolean }) {
  const { t } = useTranslation()
  return (
    <Paper variant="outlined" sx={{ p: 2.5 }}>
      <Typography component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('search.noResultsFor', { query })}</Typography>
      <Box component="ul" sx={{ m: 0, pl: 2.5, color: 'text.secondary', '& li': { mb: 0.5 } }}>
        <li>{t('search.tipSpelling')}</li>
        <li>{t('search.tipFewerWords')}</li>
        <li>{t('search.tipQuotes')}</li>
        <li>{t('search.tipId')}</li>
      </Box>
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 2 }}>
        <Button component={RouterLink} to="/products" variant="outlined" size="small">{t('search.browseCatalog')}</Button>
        <Button component={RouterLink} to="/knowledge-base" variant="outlined" size="small">{t('search.browseKnowledge')}</Button>
        {signedIn && <Button component={RouterLink} to="/support/tickets" variant="outlined" size="small">{t('search.contactSupport')}</Button>}
      </Box>
    </Paper>
  )
}
