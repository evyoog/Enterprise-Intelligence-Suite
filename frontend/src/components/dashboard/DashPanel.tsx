import type { ReactNode } from 'react'
import { Box, Button, Paper, Skeleton, Typography } from '@mui/material'
import { ArrowRight, RefreshCw } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'

/** "View all →" style link at the top-right of a panel. */
export function PanelLink({ to, children }: { to: string; children: ReactNode }) {
  return (
    <Button component={RouterLink} to={to} size="small" endIcon={<ArrowRight size={14} aria-hidden />} sx={{ whiteSpace: 'nowrap', flexShrink: 0 }}>
      {children}
    </Button>
  )
}

/**
 * C69: one dashboard panel — a white surface with an uppercase title, an
 * optional action at the top-right, and built-in loading (skeleton) and
 * error (message + Retry) states so one failing source never breaks the page.
 */
export function DashPanel({ id, title, action, children, loading, error, onRetry, skeletonHeight = 160, sx }: {
  id: string; title: string; action?: ReactNode; children?: ReactNode
  loading?: boolean; error?: string | null; onRetry?: () => void; skeletonHeight?: number
  sx?: object
}) {
  const { t } = useTranslation()
  return (
    <Paper variant="outlined" component="section" aria-labelledby={`${id}-title`} aria-busy={loading || undefined}
      sx={{ p: { xs: 2, sm: 2.5 }, borderRadius: 3, minWidth: 0, display: 'flex', flexDirection: 'column', ...sx }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, mb: 1.5, minHeight: 30 }}>
        <Typography id={`${id}-title`} component="h2" sx={{ fontSize: 12, fontWeight: 700, letterSpacing: '0.06em', textTransform: 'uppercase', color: 'text.secondary' }}>
          {title}
        </Typography>
        {!loading && !error && action}
      </Box>
      {loading && (
        <Box>
          <Skeleton width="60%" />
          <Skeleton width="40%" />
          <Skeleton variant="rounded" height={skeletonHeight} sx={{ mt: 1 }} />
        </Box>
      )}
      {!loading && error && (
        <Box role="alert" sx={{ py: 3, textAlign: 'center' }}>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1 }}>{t('bizDash.sectionUnavailable', { section: title })}</Typography>
          {onRetry && <Button size="small" startIcon={<RefreshCw size={14} />} onClick={onRetry}>{t('ui.retry')}</Button>}
        </Box>
      )}
      {!loading && !error && <Box sx={{ flex: 1, minWidth: 0 }}>{children}</Box>}
    </Paper>
  )
}

/** A neutral empty state inside a panel. */
export function PanelEmpty({ title, action }: { title: string; action?: ReactNode }) {
  return (
    <Box sx={{ py: 3, textAlign: 'center' }}>
      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{title}</Typography>
      {action && <Box sx={{ mt: 1 }}>{action}</Box>}
    </Box>
  )
}
