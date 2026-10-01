import type { ComponentType, ReactNode } from 'react'
import { Box, Paper, Typography, useTheme } from '@mui/material'
import { alpha } from '@mui/material/styles'
import { accentColor, type AccentKey } from '../../theming/accents'

/** C60: one titled block of a settings screen, marked with its accent colour. */
export function SettingsSection({ id, title, description, icon: Icon, accent, children, action }: {
  id?: string; title: string; description?: string; icon: ComponentType<{ size?: number | string }>; accent: AccentKey
  children: ReactNode; action?: ReactNode
}) {
  const theme = useTheme()
  const color = accentColor(accent, theme.palette.mode)
  const headingId = id ? `${id}-title` : undefined
  return (
    <Paper variant="outlined" component="section" aria-labelledby={headingId}
      sx={{ position: 'relative', overflow: 'hidden', p: { xs: 2, sm: 3 }, pl: { xs: 2.5, sm: 3.5 }, borderRadius: 3 }}>
      <Box aria-hidden sx={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: 4, bgcolor: color }} />
      <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5, mb: 2.5 }}>
        <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, flexShrink: 0, display: 'grid', placeItems: 'center', color, bgcolor: alpha(color, theme.palette.mode === 'dark' ? 0.18 : 0.1) }}>
          <Icon size={18} />
        </Box>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography id={headingId} component="h2" variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.3 }}>{title}</Typography>
          {description && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.25 }}>{description}</Typography>}
        </Box>
        {action}
      </Box>
      {children}
    </Paper>
  )
}

/** A responsive field grid: one column on phones, two (or `columns`) above. */
export function FieldGrid({ children, columns = 2 }: { children: ReactNode; columns?: number }) {
  return (
    <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: `repeat(${columns}, minmax(0, 1fr))` } }}>
      {children}
    </Box>
  )
}

/** C60: a coloured summary tile; a button when `onClick` is given. */
export function StatusTile({ icon: Icon, accent, label, value, status, statusColor = 'default', onClick, selected }: {
  icon: ComponentType<{ size?: number | string }>; accent: AccentKey; label: string; value: string; status?: string
  statusColor?: 'default' | 'success' | 'warning' | 'error' | 'info'; onClick?: () => void; selected?: boolean
}) {
  const theme = useTheme()
  const color = accentColor(accent, theme.palette.mode)
  const statusPalette = statusColor === 'default' ? theme.palette.text.secondary : theme.palette[statusColor].main
  return (
    <Paper
      variant="outlined"
      component={onClick ? 'button' : 'div'}
      type={onClick ? 'button' : undefined}
      onClick={onClick}
      aria-pressed={onClick ? Boolean(selected) : undefined}
      sx={{
        position: 'relative', overflow: 'hidden', textAlign: 'left', width: '100%', p: 2, borderRadius: 3, font: 'inherit', color: 'inherit',
        cursor: onClick ? 'pointer' : 'default', display: 'flex', gap: 1.5, alignItems: 'center',
        borderColor: selected ? color : 'divider',
        background: `linear-gradient(135deg, ${alpha(color, theme.palette.mode === 'dark' ? 0.16 : 0.08)}, transparent 70%)`,
        transition: 'border-color .15s, transform .15s, box-shadow .15s',
        '&:hover': onClick ? { borderColor: color, transform: 'translateY(-1px)', boxShadow: `0 8px 20px -14px ${color}` } : undefined,
        '&:focus-visible': { outline: `2px solid ${color}`, outlineOffset: 2 },
      }}
    >
      <Box aria-hidden sx={{ width: 44, height: 44, borderRadius: 2.5, flexShrink: 0, display: 'grid', placeItems: 'center', color: theme.palette.mode === 'dark' ? '#0b1220' : '#fff', bgcolor: color }}>
        <Icon size={22} />
      </Box>
      <Box sx={{ minWidth: 0 }}>
        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block' }}>{label}</Typography>
        <Typography sx={{ fontWeight: 700, lineHeight: 1.3 }} noWrap>{value}</Typography>
        {status && (
          <Typography variant="caption" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, fontWeight: 600, color: statusPalette }}>
            <Box component="span" aria-hidden sx={{ width: 7, height: 7, borderRadius: '50%', bgcolor: statusPalette }} />{status}
          </Typography>
        )}
      </Box>
    </Paper>
  )
}
