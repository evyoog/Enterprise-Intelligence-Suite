import { Search as PHSearch } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Chip, Paper, TextField, Typography } from '@mui/material'
import { Link as RouterLink, useSearchParams } from 'react-router-dom'
import { globalSearchApi, type GlobalSearchResult, type SearchResultItem } from '../api/globalSearchApi'
import { PageHeader } from '../components/layout/PageHeader'

const LINK_FOR: Record<SearchResultItem['type'], (id: number) => string> = {
  PRODUCT: (id) => `/products/${id}`,
  KNOWLEDGE: () => '/knowledge-base',
  TICKET: () => '/support/tickets',
}

/** "/search" — 01.03.01 Unified Search (sprint 2027.1.3): keyword search
 * across products, published knowledge articles, and (signed in) the
 * caller's own tickets. Semantic search is not built — see
 * globalSearchApi's own doc. */
export function GlobalSearchPage() {
  const { t } = useTranslation()
  const [searchParams, setSearchParams] = useSearchParams()
  const [query, setQuery] = useState(searchParams.get('q') ?? '')
  const [result, setResult] = useState<GlobalSearchResult | null>(null)

  useEffect(() => {
    const handle = setTimeout(() => {
      setSearchParams(query ? { q: query } : {}, { replace: true })
      globalSearchApi.search(query).then(setResult).catch(() => setResult({ products: [], knowledgeArticles: [], tickets: [] }))
    }, 250)
    return () => clearTimeout(handle)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  const isEmpty = result !== null
    && result.products.length === 0 && result.knowledgeArticles.length === 0 && result.tickets.length === 0

  return (
    <Box>
      <PageHeader icon={PHSearch} accent="blue" title={t('search.title')} />
      <TextField
        fullWidth
        sx={{ mb: 3 }}
        placeholder={t('search.placeholder')}
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      {isEmpty && <Typography sx={{ color: 'text.secondary' }}>{t('search.noResults')}</Typography>}

      <ResultSection title={t('search.products')} items={result?.products ?? []} />
      <ResultSection title={t('search.knowledgeArticles')} items={result?.knowledgeArticles ?? []} />
      <ResultSection title={t('search.tickets')} items={result?.tickets ?? []} />
    </Box>
  )
}

function ResultSection({ title, items }: { title: string; items: SearchResultItem[] }) {
  if (items.length === 0) return null
  return (
    <Box sx={{ mb: 3 }}>
      <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{title}</Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {items.map((item) => (
          <Paper key={`${item.type}-${item.id}`} variant="outlined" sx={{ p: 1.5 }}>
            <Typography component={RouterLink} to={LINK_FOR[item.type](item.id)} sx={{ fontWeight: 700, textDecoration: 'none', color: 'inherit' }}>
              {item.title}
            </Typography>
            {item.snippet && (
              <Typography variant="body2" sx={{ color: 'text.secondary' }} noWrap>
                {item.snippet}
              </Typography>
            )}
            <Chip size="small" sx={{ mt: 0.5 }} variant="outlined" label={item.type} />
          </Paper>
        ))}
      </Box>
    </Box>
  )
}
