import { Box, Typography, useTheme } from '@mui/material'
import type { ComponentType, ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { accentColor, type AccentKey } from '../../theming/accents'

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
 * The shared page header (Phase 23), C66 design: a small uppercase area
 * label, the page title, a one-line description and the primary action on
 * the right — no banner, no gradient. `icon` and `accent` are accepted for
 * existing callers and shown as a small icon before the label.
 */
export function PageHeader({ title, subtitle, action, icon: Icon, accent = 'blue', eyebrow, area }: PageHeaderProps) {
  const theme = useTheme()
  const { t } = useTranslation()
  const label = eyebrow ?? (area ? t(`appShell.area.${area}`) : undefined)
  const color = accentColor(accent, theme.palette.mode)
  return (
    <Box component="header" sx={{
      mb: 3, display: 'flex', alignItems: { xs: 'flex-start', sm: 'flex-end' }, justifyContent: 'space-between',
      gap: 2, flexWrap: 'wrap',
    }}>
      <Box sx={{ minWidth: 0 }}>
        {label && (
          <Typography variant="overline" sx={{ color: 'primary.main', display: 'flex', alignItems: 'center', gap: 0.75, lineHeight: 1.8 }}>
            {Icon && <Box component="span" aria-hidden sx={{ display: 'inline-flex', color }}><Icon size={14} /></Box>}
            {label}
          </Typography>
        )}
        <Typography variant="h4" sx={{ fontSize: { xs: 24, sm: 28 }, mb: subtitle ? 0.5 : 0 }}>{title}</Typography>
        {subtitle && <Typography sx={{ color: 'text.secondary', maxWidth: 720 }}>{subtitle}</Typography>}
      </Box>
      {action && <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>{action}</Box>}
    </Box>
  )
}
