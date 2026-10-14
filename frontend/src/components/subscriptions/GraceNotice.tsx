import { useTranslation } from 'react-i18next'
import { Alert, Button, Chip } from '@mui/material'
import { Lock } from 'lucide-react'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

/** C100 RR-1 B: "Read-only until {date}" while a subscription is in its grace period. */
export function ReadOnlyChip({ graceEndsAt, i18nKey = 'subscriptions.grace.chip' }: { graceEndsAt: string; i18nKey?: string }) {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  return <Chip size="small" color="warning" icon={<Lock size={14} />} label={t(i18nKey, { date: formatDate(graceEndsAt) })} />
}

/** The term has ended and auto-renew is off: read-only until the grace period ends. `onRenew` is shown when the screen can renew. */
export function GraceNotice({ product, graceEndsAt, onRenew, busy = false }: {
  product: string
  graceEndsAt: string
  onRenew?: () => void
  busy?: boolean
}) {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  return (
    <Alert severity="warning" sx={{ mt: 1.5 }}
      action={onRenew && <Button color="inherit" size="small" disabled={busy} onClick={onRenew}>{t('subscriptions.grace.renewNow')}</Button>}>
      {t('subscriptions.grace.notice', { product, date: formatDate(graceEndsAt) })}
    </Alert>
  )
}
