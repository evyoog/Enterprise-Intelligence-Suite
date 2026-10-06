import { Alert, Box, Chip, Paper, Skeleton, Typography } from '@mui/material'
import { BookOpen, Download, HelpCircle, PlayCircle, Rocket, Wrench } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { knowledgeApi, type Home, type Item, type Personal } from '../../api/knowledgeApi'
import { useAuth } from '../../auth/AuthProvider'
import { CardGrid, ContentCard, ProductCard, SummaryRow, VideoCard } from '../../components/knowledge/KnowledgeCards'
import { KnowledgeSupport } from '../../components/knowledge/KnowledgeSupport'
import { recentSearches } from '../../components/knowledge/knowledgeUtils'
import { KnowledgeSearchBox, SectionHeading } from './KnowledgeCenterLayout'

const QUICK = [
  { key: 'gettingStarted', icon: Rocket, path: '/knowledge/getting-started' },
  { key: 'guides', icon: BookOpen, path: '/knowledge/guides', count: 'productGuides' },
  { key: 'videos', icon: PlayCircle, path: '/knowledge/videos' },
  { key: 'troubleshooting', icon: Wrench, path: '/knowledge/troubleshooting' },
  { key: 'downloads', icon: Download, path: '/knowledge/downloads' },
  { key: 'faqs', icon: HelpCircle, path: '/knowledge/faqs' },
]

/** Knowledge Center home (knowledge-center-home screen, REQ-KNW-005.3–.9). */
export function KnowledgeHomePage() {
  const { t } = useTranslation()
  const auth = useAuth()
  const [home, setHome] = useState<Home | null>(null)
  const [error, setError] = useState(false)
  const [personal, setPersonal] = useState<Personal | null>(null)
  const [updates, setUpdates] = useState<Item[]>([])

  useEffect(() => {
    knowledgeApi.home().then(setHome).catch(() => setError(true))
    knowledgeApi.releaseNotes().then((n) => setUpdates(n.slice(0, 3))).catch(() => setUpdates([]))
  }, [])
  useEffect(() => {
    if (auth.isAuthenticated) knowledgeApi.personal().then(setPersonal).catch(() => setPersonal(null))
  }, [auth.isAuthenticated])

  const recent = recentSearches()
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Hero */}
      <Box component="section" aria-labelledby="kc-hero-title" sx={{
        borderRadius: 4, p: { xs: 3, md: 5 }, color: '#fff', position: 'relative', overflow: 'hidden',
        background: 'radial-gradient(circle at 85% 20%, rgba(99,102,241,0.45), transparent 45%), #0E1726',
        display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', md: '3fr 2fr' }, alignItems: 'center',
      }}>
        <Box>
          <Typography variant="overline" sx={{ color: '#C7C9FB' }}>{t('knowledge.title')}</Typography>
          <Typography id="kc-hero-title" component="h1" sx={{ fontSize: { xs: 30, md: 42 }, fontWeight: 800, lineHeight: 1.1, mb: 1 }}>{t('knowledge.hero.title')}</Typography>
          <Typography sx={{ color: '#CBD2DF', mb: 3, maxWidth: 560 }}>{t('knowledge.hero.subtitle')}</Typography>
          <Box sx={{ bgcolor: '#fff', borderRadius: 2, p: 1 }}><KnowledgeSearchBox large /></Box>
          {home && home.popularSearches.length > 0 && (
            <Box sx={{ display: 'flex', gap: 1, mt: 2, flexWrap: 'wrap', alignItems: 'center' }}>
              <Typography variant="body2" sx={{ color: '#CBD2DF' }}>{t('knowledge.hero.popular')}</Typography>
              {home.popularSearches.map((s) => (
                <Chip key={s} component={RouterLink} to={`/knowledge/search?q=${encodeURIComponent(s)}`} clickable label={s} size="small"
                  sx={{ color: '#fff', borderColor: 'rgba(255,255,255,0.35)' }} variant="outlined" />
              ))}
            </Box>
          )}
        </Box>
        <Box aria-hidden sx={{ display: { xs: 'none', md: 'block' }, position: 'relative', height: 220,
          '@keyframes kcFloat': { '0%, 100%': { transform: 'translateY(0)' }, '50%': { transform: 'translateY(-6px)' } } }}>
          <Box sx={{ position: 'absolute', inset: '0 20px 40px 0', borderRadius: 3, bgcolor: '#F4F6FA', boxShadow: '0 20px 40px rgba(0,0,0,0.35)', p: 2,
            animation: 'kcFloat 6s ease-in-out infinite', '@media (prefers-reduced-motion: reduce)': { animation: 'none' } }}>
            {[70, 45, 85, 60].map((w, i) => <Box key={i} sx={{ height: 10, width: `${w}%`, bgcolor: i === 0 ? '#4338CA' : '#E3E7EF', borderRadius: 1, mb: 1.5 }} />)}
          </Box>
          <Box component={RouterLink} to="/knowledge/videos" aria-label={t('knowledge.hero.videoLabel')}
            sx={{ position: 'absolute', right: 0, bottom: 0, width: 180, height: 104, borderRadius: 2, bgcolor: '#0E1A3A', border: '2px solid #3B30C4', display: 'grid', placeItems: 'center', color: '#fff' }}>
            <PlayCircle size={40} />
          </Box>
        </Box>
      </Box>

      {error && <Alert severity="error">{t('knowledge.common.error')}</Alert>}

      {/* Quick access */}
      <Box component="section" aria-labelledby="kc-quick">
        <SectionHeading id="kc-quick" title={t('knowledge.quick.title')} subtitle={t('knowledge.quick.subtitle')} />
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr 1fr', md: 'repeat(3, 1fr)', lg: 'repeat(6, 1fr)' } }}>
          {QUICK.map(({ key, icon: Icon, path, count }) => (
            <Paper key={key} component={RouterLink} to={path} variant="outlined" sx={{
              p: 2, textDecoration: 'none', color: 'inherit', display: 'flex', flexDirection: 'column', gap: 1,
              transition: 'transform 160ms ease, box-shadow 160ms ease', '&:hover, &:focus-visible': { transform: 'translateY(-2px)', boxShadow: '0 8px 24px rgba(15,23,42,0.08)' },
              '@media (prefers-reduced-motion: reduce)': { transition: 'none', '&:hover, &:focus-visible': { transform: 'none' } },
            }}>
              <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'grid', placeItems: 'center', bgcolor: 'action.selected', color: 'primary.main' }}><Icon size={18} /></Box>
              <Typography component="h3" sx={{ fontWeight: 700, fontSize: 15 }}>{t(`knowledge.nav.${key}`)}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t(`knowledge.quick.${key}`)}</Typography>
              {home && <Typography variant="caption" sx={{ color: 'text.secondary', mt: 'auto' }}>{t('knowledge.quick.count', { count: home.sectionCounts[count ?? key] ?? 0 })}</Typography>}
            </Paper>
          ))}
        </Box>
      </Box>

      <Box sx={{ display: 'grid', gap: 4, gridTemplateColumns: { xs: '1fr', lg: '1fr 320px' } }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, minWidth: 0 }}>
          <Box component="section" aria-labelledby="kc-recommended">
            <SectionHeading id="kc-recommended" title={t('knowledge.recommended.title')}
              subtitle={home?.personalized ? t('knowledge.recommended.personal') : t('knowledge.recommended.popular')} />
            {!home ? <Skeleton variant="rounded" height={140} /> : (
              <CardGrid>{home.recommended.map((item) => item.contentType === 'VIDEO' ? <VideoCard key={item.id} item={item} /> : <ContentCard key={item.id} item={item} />)}</CardGrid>
            )}
          </Box>
          <Box component="section" aria-labelledby="kc-products">
            <SectionHeading id="kc-products" title={t('knowledge.products.title')} />
            <CardGrid min={260}>{home?.products.map((p) => <ProductCard key={p.id} product={p} />)}</CardGrid>
          </Box>
          {home && home.popularGuides.length > 0 && (
            <Box component="section" aria-labelledby="kc-popular">
              <SectionHeading id="kc-popular" title={t('knowledge.popularGuides.title')} />
              <CardGrid>{home.popularGuides.map((item) => <ContentCard key={item.id} item={item} />)}</CardGrid>
            </Box>
          )}
          {home && home.featuredVideos.length > 0 && (
            <Box component="section" aria-labelledby="kc-videos">
              <SectionHeading id="kc-videos" title={t('knowledge.featuredVideos.title')} />
              <CardGrid min={220}>{home.featuredVideos.map((item) => <VideoCard key={item.id} item={item} />)}</CardGrid>
            </Box>
          )}
        </Box>
        <Box component="aside" sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {auth.isAuthenticated && (
            <Paper variant="outlined" component="section" aria-labelledby="kc-personal" sx={{ p: 2 }}>
              <Typography id="kc-personal" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.personal.title')}</Typography>
              {personal && personal.continueLearning.length > 0 && (
                <>
                  <Typography variant="overline">{t('knowledge.personal.continue')}</Typography>
                  {personal.continueLearning.map((p) => <SummaryRow key={p.item.id} item={p.item} extra={t('knowledge.personal.percent', { percent: p.percent })} />)}
                </>
              )}
              {personal && personal.recentlyViewed.length > 0 && (
                <>
                  <Typography variant="overline">{t('knowledge.personal.recent')}</Typography>
                  {personal.recentlyViewed.slice(0, 5).map((s) => <SummaryRow key={s.id} item={s} />)}
                </>
              )}
              {personal && personal.bookmarks.length > 0 && (
                <>
                  <Typography variant="overline">{t('knowledge.personal.bookmarks')}</Typography>
                  {personal.bookmarks.slice(0, 5).map((s) => <SummaryRow key={s.id} item={s} />)}
                </>
              )}
              {recent.length > 0 && (
                <>
                  <Typography variant="overline">{t('knowledge.personal.recentSearches')}</Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {recent.map((s) => <Chip key={s} size="small" clickable component={RouterLink} to={`/knowledge/search?q=${encodeURIComponent(s)}`} label={s} />)}
                  </Box>
                </>
              )}
              {personal && !personal.continueLearning.length && !personal.recentlyViewed.length && !personal.bookmarks.length && recent.length === 0 && (
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.personal.empty')}</Typography>
              )}
            </Paper>
          )}
          {updates.length > 0 && (
            <Paper variant="outlined" component="section" aria-labelledby="kc-updates" sx={{ p: 2 }}>
              <Typography id="kc-updates" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.nav.releaseNotes')}</Typography>
              {updates.map((u) => (
                <Box key={u.id} component={RouterLink} to={`/knowledge/content/${u.slug ?? u.id}`} sx={{ display: 'block', py: 1, textDecoration: 'none', color: 'inherit' }}>
                  <Typography sx={{ fontWeight: 600, fontSize: 14 }}>{u.title}</Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary' }}>{u.productName}</Typography>
                </Box>
              ))}
            </Paper>
          )}
          {home && home.popularSearches.length > 0 && (
            <Paper variant="outlined" component="section" aria-labelledby="kc-searched" sx={{ p: 2 }}>
              <Typography id="kc-searched" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.analytics.mostSearched')}</Typography>
              <Box component="ol" sx={{ m: 0, pl: 2.5 }}>
                {home.popularSearches.slice(0, 5).map((s) => (
                  <li key={s}><Box component={RouterLink} to={`/knowledge/search?q=${encodeURIComponent(s)}`} sx={{ color: 'inherit' }}>{s}</Box></li>
                ))}
              </Box>
            </Paper>
          )}
        </Box>
      </Box>
      <KnowledgeSupport />
    </Box>
  )
}
