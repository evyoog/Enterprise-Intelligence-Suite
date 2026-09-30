import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  Alert, Box, Button, CircularProgress, Paper, Step, StepLabel, Stepper, TextField, Typography,
} from '@mui/material'
import { CheckCircle2 } from 'lucide-react'
import { myBillingApi, type Invoice } from '../api/billingApi'
import { ApiError } from '../api/client'
import { productsApi, type Product } from '../api/productsApi'
import { myProductsApi } from '../api/registrationApi'
import { PageHeader } from '../components/layout/PageHeader'
import { accentFor } from '../utils/accentColor'
import { openRazorpayCheckout } from '../utils/razorpayCheckout'

function money(minorUnits: number, currency: string) {
  return `${(minorUnits / 100).toFixed(2)} ${currency}`
}

type Step_ = 'loading' | 'billing' | 'processing' | 'payment' | 'paid' | 'free' | 'gateway-unavailable' | 'error'

/** "/checkout/:productId" (C48) — what a signed-in customer sees right
 * after clicking Subscribe: this platform's own real purchase flow (confirm
 * billing details -> pay through Razorpay Checkout), not a fabricated
 * multi-step wizard. There is no separate "create order" step for an
 * individual purchase — {@code POST /me/subscriptions} both activates the
 * subscription and generates its invoice in one call (SubscriptionService's
 * own doc), so this page's job is: make sure billing details exist, call
 * that endpoint, then collect payment for whatever invoice it produced (a
 * $0 plan produces none — see InvoiceService#generateForSubscription — so
 * that case skips straight to a plain confirmation). Card/UPI entry itself
 * always happens inside Razorpay's own secure window (BR-BIL-001) — never
 * on this page. */
export function CheckoutPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { productId } = useParams<{ productId: string }>()
  const id = Number(productId)

  const [product, setProduct] = useState<Product | null>(null)
  const [step, setStep] = useState<Step_>('loading')
  const [error, setError] = useState<string | null>(null)
  const [invoice, setInvoice] = useState<Invoice | null>(null)
  const [paying, setPaying] = useState(false)

  const [details, setDetails] = useState({
    billingName: '', billingEmail: '', addressLine1: '', addressLine2: '', city: '', state: '',
    postalCode: '', country: '', taxId: '',
  })
  const [hasExistingDetails, setHasExistingDetails] = useState(false)

  useEffect(() => {
    Promise.all([productsApi.get(id), myBillingApi.getDetails()])
      .then(([p, d]) => {
        setProduct(p)
        if (d) {
          setDetails({ ...d, addressLine2: d.addressLine2 ?? '', taxId: d.taxId ?? '' })
          setHasExistingDetails(true)
        }
        setStep('billing')
      })
      .catch((e) => {
        // A session that expired (or died in another tab — AuthProvider's
        // own periodic/focus session check) surfaces here as a 401 on
        // myBillingApi.getDetails(), which needs a real session — never
        // show "Could not load this product" for that; it isn't the
        // product, it's the visitor no longer being signed in. Same
        // returnTo pattern the Subscribe button itself uses, so signing
        // back in lands right back on this checkout.
        if (e instanceof ApiError && e.status === 401) {
          navigate(`/login?returnTo=${encodeURIComponent(`/checkout/${id}`)}`, { replace: true })
          return
        }
        setStep('error')
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const set = (field: keyof typeof details) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setDetails((d) => ({ ...d, [field]: e.target.value }))

  const confirmAndSubscribe = async () => {
    setStep('processing')
    setError(null)
    try {
      await myBillingApi.saveDetails(details)
      await myProductsApi.subscribe(id)
      // Decision C47: an invoice's list row doesn't carry back which product
      // it bills, only its own detail (lines) does — but the one this
      // subscribe call just generated is, by construction, the newest OPEN
      // invoice this customer has, since invoices are ordered issuedAt desc.
      const recent = await myBillingApi.invoices(0)
      const openInvoice = recent.content.find((inv) => inv.status === 'OPEN') ?? null
      if (!openInvoice) {
        setStep('free')
        return
      }
      const overview = await myBillingApi.overview()
      setInvoice(openInvoice)
      setStep(overview.gatewayConfigured ? 'payment' : 'gateway-unavailable')
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) {
        navigate(`/login?returnTo=${encodeURIComponent(`/checkout/${id}`)}`, { replace: true })
        return
      }
      setError(e instanceof ApiError ? e.message : 'Could not start your subscription.')
      setStep('billing')
    }
  }

  const pay = async () => {
    if (!invoice || !product) return
    setPaying(true)
    setError(null)
    try {
      const order = await myBillingApi.createPayment(invoice.id)
      const result = await openRazorpayCheckout({
        keyId: order.keyId, orderId: order.providerOrderId, amount: order.amount, currency: order.currency,
        name: 'eVyoog', description: product.name,
      })
      await myBillingApi.confirmPayment(order.paymentId, result)
      setStep('paid')
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) {
        navigate(`/login?returnTo=${encodeURIComponent(`/checkout/${id}`)}`, { replace: true })
        return
      }
      if (e instanceof Error && e.message === 'cancelled') {
        setError(t('checkout.paymentCancelled'))
      } else {
        setError(e instanceof ApiError ? e.message : t('checkout.paymentFailed'))
      }
    } finally {
      setPaying(false)
    }
  }

  if (step === 'loading') {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
  }
  if (step === 'error' || !product) {
    return <Alert severity="error">{t('checkout.loadError')}</Alert>
  }

  const accent = accentFor(product.name)
  const plan = product.plans.find((p) => p.billingPeriod === 'MONTHLY') ?? product.plans[0]
  const price = plan ? plan.price : product.price

  const activeStepIndex = step === 'billing' || step === 'processing' ? 0 : step === 'payment' ? 1 : 2

  return (
    <Box sx={{ maxWidth: 640 }}>
      <PageHeader title={t('checkout.title')} subtitle={product.name} />

      <Stepper activeStep={activeStepIndex} sx={{ mb: 3 }}>
        <Step><StepLabel>{t('checkout.steps.details')}</StepLabel></Step>
        <Step><StepLabel>{t('checkout.steps.payment')}</StepLabel></Step>
        <Step><StepLabel>{t('checkout.steps.done')}</StepLabel></Step>
      </Stepper>

      <Paper variant="outlined" sx={{ p: 2.5, mb: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
        <Box sx={{ width: 44, height: 44, borderRadius: 2, display: 'grid', placeItems: 'center', bgcolor: accent.bg, color: accent.fg, flexShrink: 0 }}>
          {product.name.charAt(0)}
        </Box>
        <Box sx={{ flex: 1 }}>
          <Typography sx={{ fontWeight: 700 }}>{product.name}</Typography>
          {plan && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{plan.name}</Typography>}
        </Box>
        <Typography variant="h6" sx={{ fontWeight: 700 }}>
          {price > 0 ? `$${price.toFixed(2)}` : t('checkout.free')}
        </Typography>
      </Paper>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {(step === 'billing' || step === 'processing') && (
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Typography sx={{ fontWeight: 700, mb: 2 }}>{t('checkout.billingDetails')}</Typography>
          <Box sx={{ display: 'grid', gap: 2 }}>
            <TextField label={t('billing.details.name')} value={details.billingName} onChange={set('billingName')} required />
            <TextField label={t('billing.details.email')} type="email" value={details.billingEmail} onChange={set('billingEmail')} required />
            <TextField label={t('billing.details.address1')} value={details.addressLine1} onChange={set('addressLine1')} required />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField label={t('billing.details.city')} value={details.city} onChange={set('city')} required fullWidth />
              <TextField label={t('billing.details.state')} value={details.state} onChange={set('state')} required fullWidth />
            </Box>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField label={t('billing.details.postalCode')} value={details.postalCode} onChange={set('postalCode')} required fullWidth />
              <TextField label={t('billing.details.country')} value={details.country} onChange={set('country')} required fullWidth />
            </Box>
            <Button
              variant="contained" size="large" sx={{ mt: 1 }}
              disabled={step === 'processing' || !details.billingName || !details.billingEmail || !details.addressLine1 || !details.city || !details.state || !details.postalCode || !details.country}
              onClick={confirmAndSubscribe}
              startIcon={step === 'processing' ? <CircularProgress size={16} color="inherit" /> : undefined}
            >
              {hasExistingDetails ? t('checkout.continueToPayment') : t('checkout.saveAndContinue')}
            </Button>
          </Box>
        </Paper>
      )}

      {step === 'payment' && invoice && (
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Typography sx={{ fontWeight: 700, mb: 1 }}>{t('checkout.payTitle')}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{invoice.invoiceNumber}</Typography>
          <Typography variant="h5" sx={{ fontWeight: 700, mb: 3 }}>{money(invoice.total, invoice.currency)}</Typography>
          <Button variant="contained" size="large" disabled={paying} onClick={pay}
            startIcon={paying ? <CircularProgress size={16} color="inherit" /> : undefined}>
            {t('checkout.payNow', { amount: money(invoice.total, invoice.currency) })}
          </Button>
        </Paper>
      )}

      {step === 'gateway-unavailable' && (
        <Alert severity="info">
          {t('checkout.gatewayUnavailable')}{' '}
          <Link to="/billing">{t('checkout.viewInBilling')}</Link>
        </Alert>
      )}

      {(step === 'paid' || step === 'free') && (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <CheckCircle2 size={40} color={accent.fg} style={{ marginBottom: 12 }} />
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>
            {step === 'paid' ? t('checkout.paidTitle') : t('checkout.freeTitle')}
          </Typography>
          <Typography sx={{ color: 'text.secondary', mb: 3 }}>{product.name}</Typography>
          <Button variant="contained" onClick={() => navigate('/my/products')}>{t('checkout.goToMyProducts')}</Button>
        </Paper>
      )}
    </Box>
  )
}
