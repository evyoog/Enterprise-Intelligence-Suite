import type { ReactNode } from 'react'
import { Box, Card, Typography } from '@mui/material'
import { ArrowRight, Layers } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { resolveAssetUrl } from '../../api/client'
import { showcasePalette } from '../../utils/showcaseColor'
import { FeatureTag } from './FeatureTag'
import { StatusBadge, type StatusTone } from './StatusBadge'

export interface ShowcaseCardProps {
  name: string
  /** Badge at the top right, e.g. "Platform" or the app's category. */
  typeLabel?: string
  logoUrl?: string | null
  color?: string | null
  /** One line under the name, e.g. "3 apps" — only real values. */
  meta?: string
  description?: string | null
  features?: string[]
  status?: { label: string; tone: StatusTone }
  /** The action at the bottom right. Without `to`/`onAction` it is shown as text (preview). */
  actionLabel?: string
  to?: string
  onAction?: () => void
  /** Extra content under the description (e.g. price, launch buttons). */
  extra?: ReactNode
  headingLevel?: 'h2' | 'h3'
  maxFeatures?: number
}

/**
 * C66: the one card used for platforms and apps in the catalog, in the
 * admin lists and in the live Showcase Preview, so what the admin previews
 * is what customers see. White card; the showcase colour only tints the
 * icon, the type badge, the hover border and the action arrow.
 */
export function ShowcaseCard({
  name, typeLabel, logoUrl, color, meta, description, features = [], status, actionLabel, to, onAction, extra,
  headingLevel = 'h3', maxFeatures = 3,
}: ShowcaseCardProps) {
  const palette = showcasePalette(color)
  const shown = features.slice(0, maxFeatures)
  const more = features.length - shown.length
  const action = actionLabel && (
    <Box component="span" className="showcase-action" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, fontWeight: 600, fontSize: 14, color: palette.text }}>
      {actionLabel}<Box component="span" className="showcase-arrow" sx={{ display: 'inline-flex', transition: 'transform .15s' }}><ArrowRight size={15} aria-hidden /></Box>
    </Box>
  )
  return (
    <Card sx={{
      height: '100%', display: 'flex', flexDirection: 'column', p: 2.5, position: 'relative', overflow: 'hidden',
      transition: 'border-color .15s, box-shadow .15s, transform .15s',
      '&::before': { content: '""', position: 'absolute', inset: '0 0 auto 0', height: 3, bgcolor: palette.base },
      '&:hover': { borderColor: palette.border, boxShadow: '0 6px 20px -12px rgba(15,23,42,.25)', transform: 'translateY(-1px)' },
      '&:hover .showcase-arrow': { transform: 'translateX(3px)' },
      '@media (prefers-reduced-motion: reduce)': { transition: 'none', '&:hover': { transform: 'none' } },
    }}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 1, mb: 1.5 }}>
        <Box sx={{ width: 44, height: 44, borderRadius: 2.5, flexShrink: 0, display: 'grid', placeItems: 'center', overflow: 'hidden', bgcolor: palette.soft, color: palette.text }}>
          {logoUrl
            ? <Box component="img" src={resolveAssetUrl(logoUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            : <Layers size={22} aria-hidden />}
        </Box>
        {typeLabel && (
          <Box component="span" sx={{ px: 1, py: 0.25, borderRadius: 1.5, fontSize: 12, fontWeight: 600, color: palette.text, bgcolor: palette.soft }}>
            {typeLabel}
          </Box>
        )}
      </Box>
      <Typography component={headingLevel} sx={{ fontWeight: 700, fontSize: 17, lineHeight: 1.3, overflowWrap: 'anywhere' }}>{name}</Typography>
      {meta && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.25 }}>{meta}</Typography>}
      {description && (
        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1.25, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {description}
        </Typography>
      )}
      {shown.length > 0 && (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75, mt: 1.5 }}>
          {shown.map((f) => <FeatureTag key={f} label={f} />)}
          {more > 0 && <FeatureTag label={`+${more}`} />}
        </Box>
      )}
      {extra && <Box sx={{ position: 'relative', zIndex: 1 }}>{extra}</Box>}
      <Box sx={{ flexGrow: 1 }} />
      {(status || action) && (
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, mt: 2, pt: 1.5, borderTop: '1px solid', borderColor: 'divider' }}>
          {status ? <StatusBadge label={status.label} tone={status.tone} /> : <span />}
          {action && to && (
            <Box component={RouterLink} to={to} aria-label={`${actionLabel}: ${name}`} sx={{ textDecoration: 'none', borderRadius: 1, '&::after': { content: '""', position: 'absolute', inset: 0 } }}>
              {action}
            </Box>
          )}
          {action && !to && onAction && (
            <Box component="button" type="button" onClick={onAction} aria-label={`${actionLabel}: ${name}`} sx={{ background: 'none', border: 0, p: 0, cursor: 'pointer', font: 'inherit' }}>
              {action}
            </Box>
          )}
          {action && !to && !onAction && action}
        </Box>
      )}
    </Card>
  )
}
