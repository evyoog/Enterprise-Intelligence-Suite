import { Box, Paper, Typography, useTheme } from '@mui/material'
import { alpha } from '@mui/material/styles'
import type { ComponentType, ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { accentColor, BRAND_STRIPE, type AccentKey } from '../../theming/accents'

interface PageHeaderProps {
  title: string
  subtitle?: string
  action?: ReactNode
  /** C60: an icon for the page, shown in an accent-coloured tile. */
  icon?: ComponentType<{ size?: number | string }>
  /** Accent colour of the icon tile (default blue). */
  accent?: AccentKey
  /** A short label above the title, already translated. */
  eyebrow?: string
  /** Or the area of the app, translated from `appShell.area.<area>`. */
  area?: PageArea
}

export type PageArea = 'catalog' | 'settings' | 'workspace' | 'organization' | 'accessControl' | 'administration' | 'content'
  | 'support' | 'operations' | 'partners' | 'billing' | 'compliance' | 'product' | 'account' | 'help' | 'integrations'

/**
 * The shared page header used across the signed-in tool (Phase 23). C60
 * corporate standard: a card with the brand's multi-colour stripe on top,
 * an optional accent icon tile, an eyebrow label, the page title and
 * subtitle, and actions on the right. Light and dark mode come from the
 * theme; nothing here hard-codes a mode.
 */
export function PageHeader({ title, subtitle, action, icon: Icon, accent = 'blue', eyebrow, area }: PageHeaderProps) {
  const theme = useTheme()
  const { t } = useTranslation()
  const label = eyebrow ?? (area ? t(`appShell.area.${area}`) : undefined)
  const color = accentColor(accent, theme.palette.mode)
  return (
    <Paper
      variant="outlined"
      component="header"
      sx={{
        mb: 3,
        position: 'relative',
        overflow: 'hidden',
        borderRadius: 3,
        px: { xs: 2, sm: 3 },
        pt: 3,
        pb: 2.5,
        background: theme.palette.mode === 'dark'
          ? `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.14)}, ${alpha(theme.palette.secondary.main, 0.04)} 60%, transparent)`
          : `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.07)}, ${alpha(theme.palette.secondary.main, 0.03)} 60%, ${theme.palette.background.paper})`,
        '&::before': {
          content: '""', position: 'absolute', top: 0, left: 0, right: 0, height: 4, backgroundImage: BRAND_STRIPE,
        },
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, minWidth: 0 }}>
          {Icon && (
            <Box aria-hidden sx={{
              width: 52, height: 52, borderRadius: 3, flexShrink: 0, display: 'grid', placeItems: 'center',
              color, bgcolor: alpha(color, theme.palette.mode === 'dark' ? 0.18 : 0.1),
              border: `1px solid ${alpha(color, 0.3)}`,
            }}>
              <Icon size={26} />
            </Box>
          )}
          <Box sx={{ minWidth: 0 }}>
            {label && (
              <Typography variant="overline" sx={{ color, display: 'block', lineHeight: 1.6 }}>{label}</Typography>
            )}
            <Typography variant="h4" sx={{ fontSize: { xs: 26, sm: 32 }, mb: subtitle ? 0.5 : 0 }}>{title}</Typography>
            {subtitle && <Typography sx={{ color: 'text.secondary' }}>{subtitle}</Typography>}
          </Box>
        </Box>
        {action}
      </Box>
    </Paper>
  )
}
