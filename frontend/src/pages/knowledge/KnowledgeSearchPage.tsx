import { Alert, Box, Chip, CircularProgress, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useSearchParams } from 'react-router-dom'
import { knowledgeApi, type ContentType, type SearchResult } from '../../api/knowledgeApi'
import { CardGrid, ProductCard, SummaryCard } from '../../components/knowledge/KnowledgeCards'
import { KnowledgeSupport } from '../../components/knowledge/KnowledgeSupport'
import { EmptyState } from '../../components/ui/EmptyState'
import { SectionHeading } from './KnowledgeCenterLayout'

/** Knowledge search results grouped by type (knowledge-center-search, REQ-KNW-005.10). */
export function KnowledgeSearchPage() {
  const { t } = useTranslation()
  const [params, setParams] = useSearchParams()
  const query = params.get('q') ?? ''
  const type = (params.get('type') as ContentType | null) ?? undefined
  const key = `${type ?? ''}|${query}`
  const [loaded, setLoaded] = useState<{ key: string; result: SearchResult | null } | null>(null)

  useEffect(() => {
    if (!query) return
    knowledgeApi.search(query, type).then((result) => setLoaded({ key, result })).catch(() => setLoaded({ key, result: null }))
  }, [query, type, key])
  const current = loaded?.key === key ? loaded : null
  const result = current?.result ?? null
  const error = !!current && !current.result

  if (!query) return <EmptyState title={t('knowledge.searchLabel')} />
  if (error) return <Alert severity="error">{t('knowledge.common.error')}</Alert>
  if (!result) return <Box sx={{ display: 'grid', placeItems: 'center', py: 6 }}><CircularProgress aria-label={t('knowledge.common.loading')} /></Box>

  const groups = Object.entries(result.byType) as [ContentType, SearchResult['results']][]
  const total = result.results.length + result.products.length + result.modules.length
  const setType = (value?: ContentType) => {
    const next = new URLSearchParams(params)
    if (value) next.set('type', value); else next.delete('type')
    setParams(next)
  }
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Box>
        <Typography component="h1" variant="h5">{t('knowledge.search.resultsFor', { query })}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
          {t('knowledge.search.count', { count: total })}{result.semanticStatus === 'USED' ? ` · ${t('knowledge.search.meaning')}` : ''}
        </Typography>
        {result.didYouMean && (
          <Typography sx={{ mt: 1 }}>{t('knowledge.search.didYouMean')}{' '}
            <RouterLink to={`/knowledge/search?q=${encodeURIComponent(result.didYouMean)}`}>{result.didYouMean}</RouterLink>?
          </Typography>
        )}
      </Box>
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }} role="group" aria-label={t('knowledge.admin.list.type')}>
        <Chip label={t('knowledge.search.all')} color={!type ? 'primary' : 'default'} onClick={() => setType()} />
        {(type ? [[type, []]] as [ContentType, unknown][] : groups).map(([k]) => (
          <Chip key={k} label={t(`knowledge.typePlural.${k}`)} color={type === k ? 'primary' : 'default'} onClick={() => setType(k)} />
        ))}
      </Box>
      {total === 0 && <EmptyState title={t('knowledge.search.noResults', { query })} description={t('knowledge.search.noResultsHint')} />}
      {result.products.length > 0 && (
        <Box component="section" aria-labelledby="ks-products">
          <SectionHeading id="ks-products" title={t('knowledge.search.products')} />
          <CardGrid min={260}>{result.products.map((p) => <ProductCard key={p.id} product={p} />)}</CardGrid>
        </Box>
      )}
      {result.modules.length > 0 && (
        <Box component="section" aria-labelledby="ks-modules">
          <SectionHeading id="ks-modules" title={t('knowledge.search.modules')} />
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {result.modules.map((m) => <Chip key={m.id} clickable component={RouterLink} to={`/knowledge/products/${m.productSlug}#${m.slug}`} label={`${m.productName} · ${m.name}`} />)}
          </Box>
        </Box>
      )}
      {groups.map(([groupType, items]) => (
        <Box component="section" key={groupType} aria-labelledby={`ks-${groupType}`}>
          <SectionHeading id={`ks-${groupType}`} title={`${t(`knowledge.typePlural.${groupType}`)} (${items.length})`} />
          <CardGrid>{items.map((item) => <SummaryCard key={item.id} item={item} />)}</CardGrid>
        </Box>
      ))}
      <KnowledgeSupport query={query} />
    </Box>
  )
}
