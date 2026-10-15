import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Button, Chip, Typography } from '@mui/material'
import { Gift } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import type { Subscription } from '../../api/registrationApi'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

const DAY_MS = 24 * 60 * 60 * 1000

/** Whole days left until `expiresAt` (a part of a day counts as a day); 0 when the end has passed. */
function trialDaysLeft(expiresAt: string, now: number = Date.now()): number {
  return Math.max(0, Math.ceil((new Date(expiresAt).getTime() - now) / DAY_MS))
}

/** C102 (REQ-MKT-005): the "Free trial" chip and "Ends {date} (N days left)" of a trial subscription. */
export function TrialChips({ subscription }: { subscription: Subscription }) {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  const [now] = useState(() => Date.now())
  const end = subscription.expiresAt
  let ends: string | null = null
  if (end) {
    const date = formatDate(end)
    const left = trialDaysLeft(end, now)
    ends = subscription.status === 'EXPIRED' || new Date(end).getTime() <= now ? t('evaluation.subscription.endedOn', { date })
      : t('evaluation.subscription.endsOn', { count: left, date })
  }
  return (
    <>
      <Chip size="small" color="secondary" icon={<Gift size={14} />} label={t('evaluation.subscription.chip')} />
      {ends && <Chip size="small" variant="outlined" label={ends} />}
    </>
  )
}

/**
 * C102: a trial cannot be renewed or changed, so this replaces those controls: a note and "Subscribe", which goes to
 * the product page, where the normal cart and checkout turn the trial into a paid subscription.
 */
export function TrialSubscribe({ subscription, showNote = true }: { subscription: Subscription; showNote?: boolean }) {
  const { t } = useTranslation()
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.75, alignItems: 'flex-start' }}>
      {showNote && (
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
          {t('evaluation.subscription.note', { product: subscription.productName })}
        </Typography>
      )}
      <Button component={RouterLink} to={`/products/${subscription.productId}`} size="small" variant="contained"
        aria-label={t('evaluation.subscription.subscribeLabel', { product: subscription.productName })}>
        {t('evaluation.subscription.subscribe')}
      </Button>
    </Box>
  )
}
