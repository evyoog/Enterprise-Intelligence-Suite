import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, CircularProgress, Paper, Typography } from '@mui/material'
import { CalendarClock, PlayCircle, Sparkles } from 'lucide-react'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { cartApi, type ContinueAs } from '../../api/cartApi'
import { evaluationApi } from '../../api/evaluationApi'
import { myProductsApi, organizationApi, type Subscription } from '../../api/registrationApi'
import { useAuth } from '../../auth/AuthProvider'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'
import { DemoRequestDialog } from './DemoRequestDialog'

/** Who the free trial would be for, and whether it is still available (C102 / REQ-MKT-005). */
type TrialBlock = 'subscribed' | 'subscribedOrg' | 'memberNotAllowed' | null

/**
 * REQ-MKT-005 (C102 TR-1): the "Try it first" panel on the product page — Start free trial (only when the product
 * sets trial days), Request a demo (opens a support ticket) and Watch tutorials (only when the product has videos).
 * Who the trial is for follows the cart's own rule (`continueAs`): an individual, an organization admin / person with
 * Manage billing (the organization), or an ordinary member (who cannot start one). Signed-out visitors are sent to
 * sign in and come back to this page, as Buy does.
 */
export function TryItFirstPanel({ productId, productName, trialDays, onWatchTutorials }: {
  productId: number
  productName: string
  trialDays?: number | null
  /** Present only when the product has published videos. */
  onWatchTutorials?: () => void
}) {
  const { t } = useTranslation()
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const { formatDate } = useLocalePreference()
  const hasTrial = trialDays != null && trialDays > 0

  const [continueAs, setContinueAs] = useState<ContinueAs | null>(null)
  const [alreadyHas, setAlreadyHas] = useState(false)
  const [starting, setStarting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [started, setStarted] = useState<Subscription | null>(null)
  const [demoOpen, setDemoOpen] = useState(false)

  // Who the trial would be for, and whether that owner already has (or had) a subscription to this product.
  useEffect(() => {
    if (!isAuthenticated || !hasTrial) return
    let alive = true
    ;(async () => {
      const mode = await cartApi.get().then((c) => c.continueAs).catch(() => 'INDIVIDUAL' as ContinueAs)
      if (!alive) return
      setContinueAs(mode)
      if (mode === 'ORGANIZATION_MEMBER') return
      const status = mode === 'ORGANIZATION_BUYER'
        ? await organizationApi.listMyOrgProducts().then((l) => l.find((p) => p.productId === productId)?.orgSubscriptionStatus ?? null).catch(() => null)
        : await myProductsApi.listProducts().then((l) => l.find((p) => p.productId === productId)?.subscriptionStatus ?? null).catch(() => null)
      if (alive) setAlreadyHas(status !== null)
    })()
    return () => { alive = false }
  }, [isAuthenticated, hasTrial, productId])

  const organization = continueAs === 'ORGANIZATION_BUYER'
  const block: TrialBlock = started ? 'subscribed'
    : continueAs === 'ORGANIZATION_MEMBER' ? 'memberNotAllowed'
    : alreadyHas ? (organization ? 'subscribedOrg' : 'subscribed')
    : null

  const goToSignIn = () => navigate(`/login?returnTo=${encodeURIComponent(`/products/${productId}`)}`)

  const startTrial = async () => {
    if (!isAuthenticated) { goToSignIn(); return }
    setStarting(true)
    setError(null)
    try {
      const subscription = await (organization ? evaluationApi.startOrganizationTrial(productId) : evaluationApi.startTrial(productId))
      setStarted(subscription)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('evaluation.trial.error'))
    } finally {
      setStarting(false)
    }
  }

  const blockMessage = block === 'subscribed' && !started ? t('evaluation.trial.alreadySubscribed')
    : block === 'subscribedOrg' ? t('evaluation.trial.alreadySubscribedOrg')
    : block === 'memberNotAllowed' ? t('evaluation.trial.memberNotAllowed')
    : null

  return (
    <Paper component="section" variant="outlined" aria-labelledby="try-it-first-title" sx={{ p: 3, mb: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
        <Sparkles size={18} aria-hidden />
        <Typography id="try-it-first-title" component="h5" variant="h6" sx={{ fontWeight: 700 }}>{t('evaluation.title')}</Typography>
      </Box>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('evaluation.intro', { product: productName })}</Typography>

      {started && (
        <Alert severity="success" role="status" sx={{ mb: 2 }}
          action={(
            <Button component={RouterLink} to="/my/subscriptions" color="inherit" size="small">{t('evaluation.trial.viewSubscriptions')}</Button>
          )}>
          {t('evaluation.trial.started', { product: productName, date: started.expiresAt ? formatDate(started.expiresAt) : '' })}
        </Alert>
      )}
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        {hasTrial && (
          <Box sx={{ maxWidth: 360 }}>
            <Typography variant="body2" sx={{ fontWeight: 600, mb: 1 }}>{t('evaluation.trial.offer', { count: trialDays })}</Typography>
            <Button variant="contained" onClick={startTrial} disabled={block !== null || starting}
              aria-describedby="try-it-first-trial-note"
              startIcon={starting ? <CircularProgress size={16} color="inherit" /> : undefined}>
              {starting ? t('evaluation.trial.starting') : t('evaluation.trial.start')}
            </Button>
            <Typography id="try-it-first-trial-note" variant="caption" sx={{ display: 'block', color: 'text.secondary', mt: 0.75 }}>
              {blockMessage
                ?? (organization ? t('evaluation.trial.forOrganization') : '')}
            </Typography>
          </Box>
        )}
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          <Button variant="outlined" startIcon={<CalendarClock size={16} aria-hidden />}
            onClick={() => (isAuthenticated ? setDemoOpen(true) : goToSignIn())}>
            {t('evaluation.demo.button')}
          </Button>
          {onWatchTutorials && (
            <Button variant="outlined" startIcon={<PlayCircle size={16} aria-hidden />} onClick={onWatchTutorials}>
              {t('evaluation.tutorials.button')}
            </Button>
          )}
        </Box>
      </Box>
      {!isAuthenticated && (
        <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary', mt: 1.5 }}>{t('evaluation.trial.signInNote')}</Typography>
      )}

      <DemoRequestDialog open={demoOpen} productId={productId} productName={productName} onClose={() => setDemoOpen(false)} />
    </Paper>
  )
}
