import type { ComponentType, ReactNode } from 'react'
import { Box, ButtonBase, Paper, Skeleton, Typography } from '@mui/material'
import { alpha } from '@mui/material/styles'
import { ArrowRight } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { fadeUp, useCountUp } from './motion'

/**
 * C69: one overview KPI. The whole card is a link to the relevant module;
 * the number counts up once when it first loads (motion permitting); the
 * action text appears on hover and focus. `value` undefined = loading.
 */
export function KpiCard({ label, value, format = (n) => String(n), sub, footer, to, actionLabel, icon: Icon, motion, index }: {
  label: string; value: number | undefined; format?: (n: number) => string; sub?: ReactNode; footer?: ReactNode
  to: string; actionLabel: string; icon: ComponentType<{ size?: number | string }>; motion: boolean; index: number
}) {
  const shown = useCountUp(value, motion)
  return (
    <Paper variant="outlined" sx={(theme) => ({
      borderRadius: 3, height: '100%', overflow: 'hidden', ...fadeUp(motion, index, 60),
      transition: 'transform 150ms ease, box-shadow 150ms ease, border-color 150ms ease',
      '&:hover, &:focus-within': {
        borderColor: alpha(theme.palette.primary.main, 0.4),
        ...(motion ? { transform: 'translateY(-2px)', boxShadow: '0 4px 14px rgba(15, 23, 42, 0.08)' } : {}),
      },
      '&:hover .kpi-action, &:focus-within .kpi-action': { opacity: 1 },
    })}>
      <ButtonBase component={RouterLink} to={to} aria-label={`${label}: ${value === undefined ? '' : format(value)}. ${actionLabel}`}
        sx={{ display: 'block', textAlign: 'left', p: 2.25, height: '100%', width: '100%' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
          <Typography sx={{ fontSize: 12, fontWeight: 700, letterSpacing: '0.06em', textTransform: 'uppercase', color: 'text.secondary' }}>{label}</Typography>
          <Box aria-hidden sx={{ color: 'primary.main', display: 'inline-flex' }}><Icon size={17} /></Box>
        </Box>
        {value === undefined
          ? <Skeleton width={64} height={40} />
          : <Typography sx={{ fontSize: 30, fontWeight: 700, lineHeight: 1.2, fontVariantNumeric: 'tabular-nums' }}>{format(shown ?? value)}</Typography>}
        {sub && <Typography variant="body2" component="div" sx={{ color: 'text.secondary', mt: 0.25 }}>{sub}</Typography>}
        {footer && <Box sx={{ mt: 1.25 }}>{footer}</Box>}
        <Box className="kpi-action" aria-hidden sx={{ mt: 1.25, display: 'flex', alignItems: 'center', gap: 0.5, color: 'primary.main', fontSize: 13, fontWeight: 600, opacity: { xs: 1, md: 0.75 }, transition: 'opacity 150ms' }}>
          {actionLabel} <ArrowRight size={13} />
        </Box>
      </ButtonBase>
    </Paper>
  )
}
