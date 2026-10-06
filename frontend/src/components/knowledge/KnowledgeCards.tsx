import { Box, Chip, Paper, Typography } from '@mui/material'
import { BookOpen, Clock, Eye, FileText, PlayCircle } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import type { ProductCard as ProductCardData, Summary } from '../../api/knowledgeApi'
import { formatDate, formatDuration, itemPath } from './knowledgeUtils'

const hover = {
  transition: 'transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease',
  '&:hover, &:focus-visible': { transform: 'translateY(-2px)', boxShadow: '0 8px 24px rgba(15, 23, 42, 0.08)', borderColor: 'primary.light' },
  '@media (prefers-reduced-motion: reduce)': { transition: 'none', '&:hover, &:focus-visible': { transform: 'none' } },
}

/** A content card (article, guide, document, FAQ…). */
export function ContentCard({ item }: { item: Summary }) {
  const { t, i18n } = useTranslation()
  return (
    <Paper component={RouterLink} to={itemPath(item)} variant="outlined" sx={{
      p: 2, display: 'flex', flexDirection: 'column', gap: 1, textDecoration: 'none', color: 'inherit', height: '100%', ...hover,
    }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
        <Chip size="small" label={t(`knowledge.type.${item.contentType}`)} sx={{ fontWeight: 600 }} />
        {item.productName && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{item.productName}{item.moduleName ? ` · ${item.moduleName}` : ''}</Typography>}
        {item.deprecated && <Chip size="small" color="warning" variant="outlined" label={t('knowledge.card.deprecated')} />}
      </Box>
      <Typography component="h3" sx={{ fontWeight: 700, fontSize: 15, lineHeight: 1.35 }}>{item.title}</Typography>
      {item.shortDescription && (
        <Typography variant="body2" sx={{ color: 'text.secondary', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {item.shortDescription}
        </Typography>
      )}
      <Box sx={{ mt: 'auto', display: 'flex', gap: 1.5, flexWrap: 'wrap', color: 'text.secondary', fontSize: 12 }}>
        <Box component="span" sx={{ display: 'inline-flex', gap: 0.5, alignItems: 'center' }}><Clock size={13} aria-hidden />{t('knowledge.card.minutes', { count: item.readingMinutes })}</Box>
        <Box component="span" sx={{ display: 'inline-flex', gap: 0.5, alignItems: 'center' }}><Eye size={13} aria-hidden />{t('knowledge.card.views', { count: item.views })}</Box>
        <Box component="span">{t('knowledge.card.updated', { date: formatDate(item.updatedAt, i18n.language) })}</Box>
      </Box>
    </Paper>
  )
}

/** A video card with thumbnail, play button, duration, difficulty and source indicator. */
export function VideoCard({ item }: { item: Summary }) {
  const { t } = useTranslation()
  const duration = formatDuration(item.durationSeconds)
  return (
    <Paper component={RouterLink} to={itemPath(item)} variant="outlined" aria-label={t('knowledge.card.play', { title: item.title })}
      sx={{ overflow: 'hidden', textDecoration: 'none', color: 'inherit', display: 'flex', flexDirection: 'column', height: '100%', ...hover }}>
      <Box sx={{ position: 'relative', aspectRatio: '16 / 9', bgcolor: '#0E1A3A', backgroundImage: item.thumbnailUrl ? `url("${item.thumbnailUrl}")` : 'none', backgroundSize: 'cover', backgroundPosition: 'center' }}>
        <Box aria-hidden sx={{ position: 'absolute', inset: 0, display: 'grid', placeItems: 'center', color: '#fff', bgcolor: 'rgba(14, 23, 38, 0.25)' }}>
          <PlayCircle size={44} />
        </Box>
        {duration && <Box sx={{ position: 'absolute', right: 8, bottom: 8, px: 0.75, borderRadius: 1, bgcolor: 'rgba(14, 23, 38, 0.85)', color: '#fff', fontSize: 12, fontWeight: 600 }}>{duration}</Box>}
        {item.videoSource && <Box sx={{ position: 'absolute', left: 8, top: 8, px: 0.75, borderRadius: 1, bgcolor: 'rgba(255,255,255,0.92)', color: '#2D3748', fontSize: 11, fontWeight: 700 }}>{t(`knowledge.source.${item.videoSource}`)}</Box>}
      </Box>
      <Box sx={{ p: 1.5, display: 'flex', flexDirection: 'column', gap: 0.5 }}>
        <Typography component="h3" sx={{ fontWeight: 700, fontSize: 14, lineHeight: 1.35 }}>{item.title}</Typography>
        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
          {[item.productName, item.difficulty ? t(`knowledge.difficulty.${item.difficulty}`) : null, t('knowledge.card.views', { count: item.views })].filter(Boolean).join(' · ')}
        </Typography>
      </Box>
    </Paper>
  )
}

export function SummaryCard({ item }: { item: Summary }) {
  return item.contentType === 'VIDEO' ? <VideoCard item={item} /> : <ContentCard item={item} />
}

/** A compact row for lists in side panels. */
export function SummaryRow({ item, extra }: { item: Summary; extra?: string }) {
  const { t } = useTranslation()
  const Icon = item.contentType === 'VIDEO' ? PlayCircle : item.contentType === 'DOCUMENT' || item.contentType === 'TEMPLATE' ? FileText : BookOpen
  return (
    <Box component={RouterLink} to={itemPath(item)} sx={{
      display: 'flex', gap: 1.25, alignItems: 'flex-start', py: 1, px: 1, borderRadius: 2, textDecoration: 'none', color: 'inherit',
      '&:hover, &:focus-visible': { bgcolor: 'action.hover' },
    }}>
      <Box aria-hidden sx={{ mt: 0.25, color: 'primary.main', display: 'flex' }}><Icon size={16} /></Box>
      <Box sx={{ minWidth: 0 }}>
        <Typography sx={{ fontWeight: 600, fontSize: 14, lineHeight: 1.35 }}>{item.title}</Typography>
        <Typography variant="caption" sx={{ color: 'text.secondary' }}>{extra ?? t(`knowledge.type.${item.contentType}`)}</Typography>
      </Box>
    </Box>
  )
}

/** A product card (Valam.ai, Varthan.ai…) with modules and content count. */
export function ProductCard({ product }: { product: ProductCardData }) {
  const { t } = useTranslation()
  return (
    <Paper variant="outlined" sx={{ p: 2.25, display: 'flex', flexDirection: 'column', gap: 1.25, height: '100%' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25 }}>
        <Box aria-hidden sx={{ width: 40, height: 40, borderRadius: 2, display: 'grid', placeItems: 'center', bgcolor: 'primary.main', color: 'primary.contrastText', fontWeight: 800 }}>
          {product.name.charAt(0)}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <Typography component="h3" sx={{ fontWeight: 700 }}>{product.name}</Typography>
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>
            {t('knowledge.products.modules', { count: product.modules.length })} · {t('knowledge.products.items', { count: product.contentCount })}
          </Typography>
        </Box>
        {product.hasAccess && <Chip size="small" color="success" variant="outlined" label={t('knowledge.products.access')} sx={{ ml: 'auto' }} />}
      </Box>
      {product.description && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{product.description}</Typography>}
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
        {product.modules.slice(0, 6).map((m) => <Chip key={m.id} size="small" variant="outlined" label={m.name} />)}
        {product.modules.length > 6 && <Chip size="small" variant="outlined" label={`+${product.modules.length - 6}`} />}
      </Box>
      <Box sx={{ mt: 'auto', display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <Typography component={RouterLink} to={`/knowledge/products/${product.slug}`} sx={{ fontWeight: 600, color: 'primary.main', fontSize: 14 }}>
          {t('knowledge.products.explore')}
        </Typography>
        {product.catalogProductId && (
          <Typography component={RouterLink} to={`/products/${product.catalogProductId}`} sx={{ fontWeight: 600, color: 'text.secondary', fontSize: 14 }}>
            {t('knowledge.products.view')}
          </Typography>
        )}
      </Box>
    </Paper>
  )
}

/** Responsive card grid. */
export function CardGrid({ children, min = 240 }: { children: React.ReactNode; min?: number }) {
  return <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: `repeat(auto-fill, minmax(${min}px, 1fr))` } }}>{children}</Box>
}
