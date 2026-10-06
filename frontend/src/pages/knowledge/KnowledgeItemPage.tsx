import { Alert, Box, Breadcrumbs, Button, Chip, CircularProgress, Divider, Paper, Typography } from '@mui/material'
import { Bookmark, BookmarkCheck, ExternalLink } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { knowledgeApi, type GlossaryTerm, type Item } from '../../api/knowledgeApi'
import { useAuth } from '../../auth/AuthProvider'
import { CardGrid, SummaryCard } from '../../components/knowledge/KnowledgeCards'
import { KnowledgeBlocksView } from '../../components/knowledge/KnowledgeBlocksView'
import { KnowledgeFeedback } from '../../components/knowledge/KnowledgeFeedback'
import { KnowledgeSupport } from '../../components/knowledge/KnowledgeSupport'
import { KnowledgeVideoPlayer } from '../../components/knowledge/KnowledgeVideoPlayer'
import { formatDate, isEisRoute } from '../../components/knowledge/knowledgeUtils'

/** Type-specific fields shown above the blocks (error codes, release notes, FAQs, glossary). */
function TypeFieldsView({ item }: { item: Item }) {
  const { t } = useTranslation()
  const f = item.typeFields ?? {}
  const text = (k: string) => (typeof f[k] === 'string' ? (f[k] as string) : null)
  const list = (k: string) => (Array.isArray(f[k]) ? (f[k] as string[]) : typeof f[k] === 'string' && f[k] ? (f[k] as string).split('\n') : [])
  const row = (label: string, value: string | null) => value ? (
    <Box sx={{ mb: 1.5 }}><Typography variant="overline">{label}</Typography><Typography sx={{ whiteSpace: 'pre-wrap' }}>{value}</Typography></Box>
  ) : null
  if (item.contentType === 'ERROR_CODE' || item.contentType === 'TROUBLESHOOTING') {
    return (
      <Paper variant="outlined" sx={{ p: 2 }}>
        {text('code') && <Chip label={text('code')} color="error" variant="outlined" sx={{ mb: 1, fontFamily: 'monospace' }} />}
        {row(t('knowledge.item.errorCode.error'), text('error') ?? text('problem'))}
        {row(t('knowledge.item.errorCode.cause'), text('cause'))}
        {row(t('knowledge.item.errorCode.solution'), text('solution'))}
        {row(t('knowledge.item.errorCode.permission'), text('permission'))}
      </Paper>
    )
  }
  if (item.contentType === 'RELEASE_NOTE') {
    const sections = [['newFeatures', 'newFeatures'], ['improvements', 'improvements'], ['bugFixes', 'bugFixes'], ['deprecated', 'deprecated']] as const
    return (
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap', mb: 1 }}>
          {row(t('knowledge.item.release.version'), text('version'))}
          {row(t('knowledge.item.release.date'), text('releaseDate'))}
        </Box>
        {sections.map(([k, label]) => list(k).length > 0 && (
          <Box key={k} sx={{ mb: 1.5 }}>
            <Typography variant="overline">{t(`knowledge.item.release.${label}`)}</Typography>
            <Box component="ul" sx={{ m: 0, pl: 3 }}>{list(k).map((x, i) => <li key={i}>{x}</li>)}</Box>
          </Box>
        ))}
        {row(t('knowledge.item.release.notices'), text('notices'))}
      </Paper>
    )
  }
  if (item.contentType === 'FAQ' && (text('question') || text('answer'))) {
    return <Paper variant="outlined" sx={{ p: 2 }}>{row(t('knowledge.item.faq.question'), text('question'))}{row(t('knowledge.item.faq.answer'), text('answer'))}</Paper>
  }
  if (item.contentType === 'GLOSSARY_TERM' && text('definition')) {
    return <Paper variant="outlined" sx={{ p: 2 }}><Typography sx={{ whiteSpace: 'pre-wrap' }}>{text('definition')}</Typography></Paper>
  }
  return null
}

/** Article / video / any content page (knowledge-center-article, -video-player; REQ-KNW-005.12). */
export function KnowledgeItemView({ item, glossary }: { item: Item; glossary: GlossaryTerm[] }) {
  const { t, i18n } = useTranslation()
  const auth = useAuth()
  const [bookmarked, setBookmarked] = useState(item.bookmarked)
  const toggleBookmark = () => {
    const call = bookmarked ? knowledgeApi.unbookmark(item.id) : knowledgeApi.bookmark(item.id)
    call.then(() => setBookmarked(!bookmarked)).catch(() => undefined)
  }
  return (
    <Box component="article" sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, maxWidth: 1100 }}>
      {item.preview && <Alert severity="info">{t('knowledge.item.previewBanner')}</Alert>}
      <Breadcrumbs aria-label={t('knowledge.item.breadcrumb')}>
        <Box component={RouterLink} to="/knowledge" sx={{ color: 'text.secondary' }}>{t('knowledge.title')}</Box>
        {item.productSlug && <Box component={RouterLink} to={`/knowledge/products/${item.productSlug}`} sx={{ color: 'text.secondary' }}>{item.productName}</Box>}
        {item.moduleName && <Typography sx={{ color: 'text.secondary' }}>{item.moduleName}</Typography>}
      </Breadcrumbs>
      {item.deprecated && <Alert severity="warning">{t('knowledge.item.deprecatedBanner')}</Alert>}
      <Box>
        <Chip size="small" label={t(`knowledge.type.${item.contentType}`)} sx={{ mb: 1, fontWeight: 600 }} />
        <Typography component="h1" variant="h4">{item.title}</Typography>
        {item.shortDescription && <Typography sx={{ color: 'text.secondary', mt: 0.5 }}>{item.shortDescription}</Typography>}
        <Box sx={{ display: 'flex', gap: 2, mt: 1, flexWrap: 'wrap', alignItems: 'center', color: 'text.secondary', fontSize: 13 }}>
          <span>{t('knowledge.item.version', { version: item.versionLabel })}</span>
          {item.publishedAt && <span>{t('knowledge.item.updated', { date: formatDate(item.publishedAt, i18n.language) })}</span>}
          <span>{t('knowledge.item.readingTime', { count: item.readingMinutes })}</span>
          {item.difficulty && <span>{t(`knowledge.difficulty.${item.difficulty}`)}</span>}
          {auth.isAuthenticated && !item.preview && (
            <Button size="small" onClick={toggleBookmark} startIcon={bookmarked ? <BookmarkCheck size={16} /> : <Bookmark size={16} />} aria-pressed={bookmarked}>
              {bookmarked ? t('knowledge.item.bookmarked') : t('knowledge.item.bookmark')}
            </Button>
          )}
        </Box>
      </Box>
      {item.video && <KnowledgeVideoPlayer contentId={item.id} title={item.title} video={item.video} staffPreview={item.preview} />}
      <TypeFieldsView item={item} />
      <KnowledgeBlocksView blocks={item.blocks} glossary={glossary} preview={item.preview} />
      {isEisRoute(item.directActionRoute) && (
        <Box><Button variant="contained" component={RouterLink} to={item.directActionRoute} endIcon={<ExternalLink size={16} />}>
          {item.directActionLabel ?? t('knowledge.item.directAction')}
        </Button></Box>
      )}
      {item.tags.length > 0 && <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>{item.tags.map((tag) => <Chip key={tag} size="small" variant="outlined" label={tag} />)}</Box>}
      {!item.preview && <KnowledgeFeedback contentId={item.id} />}
      {item.related.length > 0 && (
        <Box component="section" aria-labelledby="kc-related">
          <Typography id="kc-related" component="h2" sx={{ fontWeight: 700, fontSize: 18, mb: 1.5 }}>{t('knowledge.item.related')}</Typography>
          <CardGrid>{item.related.map((r) => <SummaryCard key={r.id} item={r} />)}</CardGrid>
        </Box>
      )}
      <Divider />
      {!item.preview && <KnowledgeSupport contentId={item.id} title={item.title} product={item.productName} module={item.moduleName}
        errorCode={typeof item.typeFields?.code === 'string' ? item.typeFields.code : undefined} />}
    </Box>
  )
}

export function KnowledgeItemPage() {
  const { t } = useTranslation()
  const { idOrSlug = '' } = useParams()
  const [loaded, setLoaded] = useState<{ key: string; item: Item | null } | null>(null)
  const [glossary, setGlossary] = useState<GlossaryTerm[]>([])
  useEffect(() => {
    knowledgeApi.get(idOrSlug).then((item) => setLoaded({ key: idOrSlug, item })).catch(() => setLoaded({ key: idOrSlug, item: null }))
  }, [idOrSlug])
  const current = loaded?.key === idOrSlug ? loaded : null
  const item = current?.item ?? null
  const missing = !!current && !current.item
  useEffect(() => { knowledgeApi.glossary().then(setGlossary).catch(() => setGlossary([])) }, [])
  if (missing) return <Alert severity="warning">{t('knowledge.item.notFound')}</Alert>
  if (!item) return <Box sx={{ display: 'grid', placeItems: 'center', py: 6 }}><CircularProgress aria-label={t('knowledge.common.loading')} /></Box>
  return <KnowledgeItemView key={item.id} item={item} glossary={glossary.filter((g) => g.id !== item.id)} />
}
