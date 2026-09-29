import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent,
  DialogTitle, FormControl, InputLabel, MenuItem, Paper, Select, type SelectChangeEvent, Typography,
} from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../api/client'
import { productsApi, type ProductPlan } from '../api/productsApi'
import { myProductsApi, type Subscription, type SubscriptionStatus } from '../api/registrationApi'
import { PageHeader } from '../components/layout/PageHeader'

type Action = 'suspend' | 'reactivate' | 'cancel' | 'renew'

/**
 * "/my/subscriptions" — 07.01 Subscription Lifecycle & Changes, 07.04
 * automatic expiry (sprint 2026.4.3). Deliberately its own page rather than
 * bolted onto MyProductsPage's dashboard cards: that page's data
 * (DashboardProduct) never carries a subscription id, only its status, so
 * it has nothing to call these endpoints with.
 */
export function MySubscriptionsPage() {
  const { t } = useTranslation()
  const [subscriptions, setSubscriptions] = useState<Subscription[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [planDialogFor, setPlanDialogFor] = useState<Subscription | null>(null)

  const load = () => {
    myProductsApi.listSubscriptions()
      .then(setSubscriptions)
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : 'Could not load your subscriptions.'))
  }

  useEffect(load, [])

  const runAction = async (subscription: Subscription, action: Action) => {
    setBusyId(subscription.id)
    setActionError(null)
    try {
      const updated = await (action === 'suspend' ? myProductsApi.suspend(subscription.id)
        : action === 'reactivate' ? myProductsApi.reactivate(subscription.id)
        : action === 'cancel' ? myProductsApi.cancel(subscription.id)
        : myProductsApi.renew(subscription.id))
      setSubscriptions((prev) => prev?.map((s) => (s.id === updated.id ? updated : s)) ?? prev)
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : 'That action could not be completed.')
    } finally {
      setBusyId(null)
    }
  }

  if (subscriptions === null && !loadError) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
    )
  }

  return (
    <Box>
      <PageHeader
        title={t('subscriptions.title')}
        subtitle={t('subscriptions.subtitle')}
        action={<Button component={RouterLink} to="/my/products" size="small" variant="outlined">{t('subscriptions.backToDashboard')}</Button>}
      />

      {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}
      {actionError && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setActionError(null)}>{actionError}</Alert>}

      {subscriptions && subscriptions.length === 0 && (
        <Typography sx={{ color: 'text.secondary' }}>{t('subscriptions.noSubscriptions')}</Typography>
      )}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {subscriptions?.map((subscription) => (
          <SubscriptionRow
            key={subscription.id}
            subscription={subscription}
            busy={busyId === subscription.id}
            onAction={(action) => runAction(subscription, action)}
            onChangePlan={() => setPlanDialogFor(subscription)}
          />
        ))}
      </Box>

      {planDialogFor && (
        <ChangePlanDialog
          subscription={planDialogFor}
          onClose={() => setPlanDialogFor(null)}
          onChanged={(updated) => {
            setSubscriptions((prev) => prev?.map((s) => (s.id === updated.id ? updated : s)) ?? prev)
            setPlanDialogFor(null)
          }}
        />
      )}
    </Box>
  )
}

function statusColor(status: SubscriptionStatus): 'success' | 'warning' | 'default' {
  if (status === 'ACTIVE') return 'success'
  if (status === 'SUSPENDED') return 'warning'
  return 'default'
}

function SubscriptionRow({ subscription, busy, onAction, onChangePlan }: {
  subscription: Subscription
  busy: boolean
  onAction: (action: Action) => void
  onChangePlan: () => void
}) {
  const { t } = useTranslation()

  return (
    <Paper variant="outlined" sx={{ p: 2.5, display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
      <Box>
        <Typography sx={{ fontWeight: 700 }}>{subscription.productName}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
          {t('subscriptions.plan')}: {subscription.planName ?? t('subscriptions.noPlan')}
        </Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
          {subscription.expiresAt
            ? t('subscriptions.expires', { date: new Date(subscription.expiresAt).toLocaleDateString() })
            : t('subscriptions.neverExpires')}
        </Typography>
        <Chip size="small" sx={{ mt: 1 }} color={statusColor(subscription.status)} label={t(`subscriptions.status.${subscription.status}`)} />
      </Box>

      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        {subscription.status === 'ACTIVE' && (
          <>
            <Button size="small" variant="outlined" disabled={busy} onClick={onChangePlan}>{t('subscriptions.actions.changePlan')}</Button>
            <Button size="small" variant="outlined" disabled={busy} onClick={() => onAction('suspend')}>{t('subscriptions.actions.suspend')}</Button>
            <Button size="small" variant="outlined" disabled={busy} onClick={() => onAction('renew')}>{t('subscriptions.actions.renew')}</Button>
            <Button size="small" color="error" variant="outlined" disabled={busy}
              onClick={() => window.confirm(t('subscriptions.confirmCancel', { product: subscription.productName })) && onAction('cancel')}>
              {t('subscriptions.actions.cancel')}
            </Button>
          </>
        )}
        {subscription.status === 'SUSPENDED' && (
          <>
            <Button size="small" variant="outlined" disabled={busy} onClick={() => onAction('reactivate')}>{t('subscriptions.actions.reactivate')}</Button>
            <Button size="small" color="error" variant="outlined" disabled={busy}
              onClick={() => window.confirm(t('subscriptions.confirmCancel', { product: subscription.productName })) && onAction('cancel')}>
              {t('subscriptions.actions.cancel')}
            </Button>
          </>
        )}
        {subscription.status === 'EXPIRED' && (
          <Button size="small" variant="outlined" disabled={busy} onClick={() => onAction('renew')}>{t('subscriptions.actions.renew')}</Button>
        )}
      </Box>
    </Paper>
  )
}

function ChangePlanDialog({ subscription, onClose, onChanged }: {
  subscription: Subscription
  onClose: () => void
  onChanged: (updated: Subscription) => void
}) {
  const { t } = useTranslation()
  const [plans, setPlans] = useState<ProductPlan[] | null>(null)
  const [selectedPlanId, setSelectedPlanId] = useState<number | ''>(subscription.planId ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    productsApi.get(subscription.productId)
      .then((product) => setPlans(product.plans))
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load plans.'))
  }, [subscription.productId])

  const save = async () => {
    setSaving(true)
    setError(null)
    try {
      const updated = await myProductsApi.changePlan(subscription.id, selectedPlanId === '' ? null : selectedPlanId)
      onChanged(updated)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not change plan.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>{t('subscriptions.changePlanDialog.title', { product: subscription.productName })}</DialogTitle>
      <DialogContent>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {plans === null ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}><CircularProgress size={24} /></Box>
        ) : (
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel id="change-plan-select-label">{t('subscriptions.changePlanDialog.planLabel')}</InputLabel>
            <Select
              labelId="change-plan-select-label"
              label={t('subscriptions.changePlanDialog.planLabel')}
              value={selectedPlanId}
              onChange={(e: SelectChangeEvent<number | ''>) => setSelectedPlanId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <MenuItem value="">{t('subscriptions.noPlan')}</MenuItem>
              {plans.map((plan) => (
                <MenuItem key={plan.id} value={plan.id}>{plan.name}</MenuItem>
              ))}
            </Select>
          </FormControl>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{t('common.cancel')}</Button>
        <Button variant="contained" disabled={saving || plans === null} onClick={save}>{t('subscriptions.changePlanDialog.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}
