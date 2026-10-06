import { Alert, Box, Button, Chip, CircularProgress, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { knowledgeApi, type ProductHub } from '../../api/knowledgeApi'
import { CardGrid, SummaryCard } from '../../components/knowledge/KnowledgeCards'
import { isNotFound, loadWithRetry } from '../../components/knowledge/knowledgeUtils'
import { SectionHeading } from './KnowledgeCenterLayout'

/** A product knowledge hub: its modules and their content (REQ-KNW-005.11). */
export function KnowledgeProductHubPage() {
  const { t } = useTranslation()
  const { slug = '' } = useParams()
  const [loaded, setLoaded] = useState<{ slug: string; hub: ProductHub | null; failed: boolean } | null>(null)
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    let active = true
    loadWithRetry(() => knowledgeApi.hub(slug))
      .then((hub) => active && setLoaded({ slug, hub, failed: false }))
      .catch((error) => active && setLoaded({ slug, hub: null, failed: !isNotFound(error) }))
    return () => { active = false }
  }, [slug, attempt])
  const current = loaded?.slug === slug ? loaded : null
  const hub = current?.hub ?? null
  if (current && !current.hub && current.failed) {
    return (
      <Alert severity="error" action={<Button color="inherit" size="small" onClick={() => { setLoaded(null); setAttempt((n) => n + 1) }}>{t('knowledge.item.retry')}</Button>}>
        {t('knowledge.item.loadError')}
      </Alert>
    )
  }
  if (current && !current.hub) return <Alert severity="warning">{t('knowledge.item.notFound')}</Alert>
  if (!hub) return <Box sx={{ display: 'grid', placeItems: 'center', py: 6 }}><CircularProgress aria-label={t('knowledge.common.loading')} /></Box>
  const p = hub.product
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
        <Box aria-hidden sx={{ width: 48, height: 48, borderRadius: 2, display: 'grid', placeItems: 'center', bgcolor: 'primary.main', color: 'primary.contrastText', fontWeight: 800, fontSize: 22 }}>{p.name.charAt(0)}</Box>
        <Box sx={{ flex: 1 }}>
          <Typography component="h1" variant="h4">{p.name}</Typography>
          {p.description && <Typography sx={{ color: 'text.secondary' }}>{p.description}</Typography>}
        </Box>
        <Button component={RouterLink} to="/knowledge">{t('knowledge.hub.back')}</Button>
        {p.catalogProductId && <Button variant="outlined" component={RouterLink} to={`/products/${p.catalogProductId}`}>{t('knowledge.products.view')}</Button>}
      </Box>
      <Box component="nav" aria-label={t('knowledge.search.modules')} sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        {hub.modules.map(({ module }) => <Chip key={module.id} component="a" href={`#${module.slug}`} clickable label={`${module.name} (${module.contentCount})`} />)}
      </Box>
      {hub.general.length > 0 && (
        <Box component="section" aria-labelledby="hub-general">
          <SectionHeading id="hub-general" title={t('knowledge.hub.general')} />
          <CardGrid>{hub.general.map((item) => <SummaryCard key={item.id} item={item} />)}</CardGrid>
        </Box>
      )}
      {hub.modules.map(({ module, items }) => (
        <Box component="section" key={module.id} id={module.slug} aria-labelledby={`hub-${module.slug}`} sx={{ scrollMarginTop: 80 }}>
          <SectionHeading id={`hub-${module.slug}`} title={module.name} />
          {items.length === 0
            ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.hub.noContent')}</Typography>
            : <CardGrid>{items.map((item) => <SummaryCard key={item.id} item={item} />)}</CardGrid>}
        </Box>
      ))}
    </Box>
  )
}
