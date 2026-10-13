import { Box, Button, Chip, Paper, Switch, Tooltip, Typography } from '@mui/material'
import { Lock, RotateCcw } from 'lucide-react'
import { useId, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import type { AccessSource } from '../../api/accessApi'

/** Where an access item comes from: text, never colour alone. */
export function SourceBadge({ source }: { source: AccessSource }) {
  const { t } = useTranslation()
  return (
    <Chip size="small" variant={source === 'OVERRIDE' ? 'filled' : 'outlined'} color={source === 'OVERRIDE' ? 'primary' : 'default'}
      label={t(`access.source.${source}`)} />
  )
}

interface RowProps {
  name: string
  description?: string
  /** The technical name, shown in a tooltip. */
  code?: string
  checked: boolean
  source: AccessSource
  /** Why the switch cannot be used; shown next to a lock and read with the switch. */
  disabledReason?: string | null
  /** True when the value differs from what is saved (staged). */
  changed?: boolean
  /** Staged to return to the role default. */
  resetPending?: boolean
  detail?: ReactNode
  onChange: (next: boolean) => void
  onReset?: () => void
}

function ToggleBody({ name, description, code, checked, source, disabledReason, changed, resetPending, detail, onChange, onReset }: RowProps) {
  const { t } = useTranslation()
  const reasonId = useId()
  const disabled = Boolean(disabledReason)
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, minHeight: 40, py: 0.75 }}>
      <Box sx={{ flex: 1, minWidth: 0 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
          <Tooltip title={code ?? ''} disableHoverListener={!code} arrow>
            <Typography sx={{ fontWeight: 600 }} component="span" tabIndex={code ? 0 : undefined}>{name}</Typography>
          </Tooltip>
          <SourceBadge source={source} />
          {changed && <Chip size="small" color="warning" variant="outlined" label={t('access.unsaved')} />}
          {resetPending && <Chip size="small" color="info" variant="outlined" label={t('access.willReset')} />}
        </Box>
        {description && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{description}</Typography>}
        {detail}
        {disabledReason && (
          <Typography id={reasonId} variant="caption" sx={{ color: 'text.secondary', display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.25 }}>
            <Lock size={12} aria-hidden /> {disabledReason}
          </Typography>
        )}
      </Box>
      {onReset && source === 'OVERRIDE' && !resetPending && !disabled && (
        <Button size="small" startIcon={<RotateCcw size={14} />} aria-label={t('access.resetItem', { name })} onClick={onReset}>
          {t('access.reset')}
        </Button>
      )}
      <Switch checked={checked} disabled={disabled} onChange={(e) => onChange(e.target.checked)}
        slotProps={{ input: { role: 'switch', 'aria-label': name, 'aria-describedby': disabledReason ? reasonId : undefined } }} />
      <Typography variant="caption" sx={{ width: 24, color: 'text.secondary' }} aria-hidden>{checked ? t('access.on') : t('access.off')}</Typography>
    </Box>
  )
}

/** One feature permission: name, description, switch, where it comes from and, when locked, why. */
export function PermissionToggleRow(props: RowProps) {
  return <ToggleBody {...props} />
}

interface ProductProps extends Omit<RowProps, 'description' | 'code'> {
  plan?: string | null
  seats?: { inUse: number; quantity: number }
}

/** One entitled product: name, plan, seat indicator and switch. */
export function ProductAccessCard({ plan, seats, ...rest }: ProductProps) {
  const { t } = useTranslation()
  const info = [plan, seats && seats.quantity > 0 ? t('access.seats', { inUse: seats.inUse, quantity: seats.quantity }) : null].filter(Boolean).join(' · ')
  return (
    <Paper variant="outlined" sx={{ px: 1.5, mb: 1 }}>
      <ToggleBody {...rest} description={info || undefined} />
    </Paper>
  )
}

/** Sticky footer of the member panel and the role defaults: how many changes are waiting, Cancel and Save. */
export function StickySaveBar({ dirtyCount, busy, onSave, onCancel, saveLabel, secondary }: {
  dirtyCount: number
  busy?: boolean
  onSave: () => void
  onCancel: () => void
  saveLabel?: string
  secondary?: ReactNode
}) {
  const { t } = useTranslation()
  return (
    <Paper elevation={3} sx={{ position: 'sticky', bottom: 0, p: 1.5, display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', zIndex: 1 }}>
      <Typography role="status" sx={{ flex: 1, minWidth: 140 }}>
        {dirtyCount === 0 ? t('access.noChanges') : t('access.changesWaiting', { count: dirtyCount })}
      </Typography>
      {secondary}
      <Button onClick={onCancel} disabled={busy || dirtyCount === 0}>{t('access.cancel')}</Button>
      <Button variant="contained" onClick={onSave} disabled={busy || dirtyCount === 0}>{saveLabel ?? t('access.save')}</Button>
    </Paper>
  )
}
