import type { TFunction } from 'i18next'
import { useTranslation } from 'react-i18next'
import { Box, Chip, Typography } from '@mui/material'
import type { SlaState } from '../../api/supportApi'
import { formatDateTime } from '../tax/taxFormat'

type ChipStyle = { color: 'default' | 'success' | 'warning' | 'error'; variant: 'filled' | 'outlined' }

const STATE_STYLE: Record<SlaState, ChipStyle> = {
  ON_TRACK: { color: 'default', variant: 'filled' },
  WARNING: { color: 'warning', variant: 'filled' },
  BREACHED: { color: 'error', variant: 'filled' },
  MET: { color: 'success', variant: 'outlined' },
  MET_LATE: { color: 'warning', variant: 'outlined' },
}

/** Minutes in a business day: the product rule is "1 day = 8 business hours". */
export const BUSINESS_DAY_MINUTES = 480
const CLOCK_DAY_MINUTES = 1440

/**
 * A target as text: "30 min", "1 h", "4 h", "1 day (8 business h)",
 * "3 days (24 business h)". Around-the-clock targets count real hours, so a
 * day is 24 h there. Anything that is not a whole number of hours or days
 * falls back to "2 h 30 min".
 */
export function formatSlaDuration(minutes: number, aroundTheClock: boolean, t: TFunction): string {
  if (!Number.isFinite(minutes) || minutes < 1) return ''
  const dayMinutes = aroundTheClock ? CLOCK_DAY_MINUTES : BUSINESS_DAY_MINUTES
  // Under a business day a plain hour count reads best ("4 h"); a whole day or more is shown in days.
  if (minutes >= dayMinutes && minutes % dayMinutes === 0) {
    const days = minutes / dayMinutes
    return t(aroundTheClock ? 'sla.duration.days' : 'sla.duration.businessDays', { count: days, hours: minutes / 60 })
  }
  if (minutes < 60) return t('sla.duration.minutes', { count: minutes })
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m === 0 ? t('sla.duration.hours', { count: h }) : t('sla.duration.hoursMinutes', { hours: h, minutes: m })
}

/**
 * C101: one SLA state as a chip (On track / Due soon / Breached / Met / Met
 * late) with an optional caption ("First response") and the due time
 * underneath. Renders only the due time when there is a deadline but no
 * state (the requester's own view), and nothing for tickets without SLA data.
 */
export function SlaChip({ state, dueAt, caption, showDue = true }: {
  state?: SlaState | null; dueAt?: string | null; caption?: string; showDue?: boolean
}) {
  const { t, i18n } = useTranslation()
  const due = showDue && dueAt ? formatDateTime(dueAt, i18n.language) : ''
  if (!state && !due) return null
  const stateLabel = state ? t(`sla.state.${state}`) : ''
  return (
    <Box component="span" sx={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-start', gap: 0.25 }}>
      {state && (
        <Chip
          size="small"
          color={STATE_STYLE[state].color}
          variant={STATE_STYLE[state].variant}
          label={caption ? `${caption}: ${stateLabel}` : stateLabel}
        />
      )}
      {due && (
        <Typography component="span" variant="caption" sx={{ color: 'text.secondary' }}>
          {caption && !state ? `${caption}: ` : ''}{t('sla.due', { date: due })}
        </Typography>
      )}
    </Box>
  )
}
