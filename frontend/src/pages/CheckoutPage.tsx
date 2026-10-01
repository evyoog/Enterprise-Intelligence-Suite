import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, Navigate, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import {
  Accordion, AccordionDetails, AccordionSummary, Alert, Box, Button, Checkbox, Chip, CircularProgress,
  Divider, FormControlLabel, IconButton, Paper, Skeleton, Snackbar, TextField, Tooltip, Typography,
  useMediaQuery, useTheme,
} from '@mui/material'
import { alpha } from '@mui/material/styles'
import {
  Building2, Check, CheckCircle2, ChevronDown, Copy, CreditCard, FileText, HelpCircle, Lock, Package,
  Smartphone, XCircle,
} from 'lucide-react'
import {
  myBillingApi, organizationBillingApi,
  type CheckoutSummary, type OfflineInvoiceResult, type Payment,
} from '../api/billingApi'
import { ApiError, resolveAssetUrl } from '../api/client'
import { productsApi, type Product } from '../api/productsApi'
import { myProductsApi } from '../api/registrationApi'
import { openRazorpayCheckout } from '../utils/razorpayCheckout'

type Step = 'details' | 'payment' | 'complete'
type OptionKey = 'card' | 'upi' | 'other' | 'offline'
type Result =
  | { kind: 'success'; payment: Payment }
  | { kind: 'pending' }
  | { kind: 'pendingSlow' }
  | { kind: 'failed'; reason: string; cancelled: boolean }
  | { kind: 'offline'; offline: OfflineInvoiceResult }
  | { kind: 'free' }

interface Details {
  billingName: string; billingEmail: string; addressLine1: string; addressLine2: string
  city: string; state: string; postalCode: string; country: string; taxId: string
}

const EMPTY_DETAILS: Details = {
  billingName: '', billingEmail: '', addressLine1: '', addressLine2: '', city: '', state: '',
  postalCode: '', country: '', taxId: '',
}

const REQUIRED: (keyof Details)[] = ['billingName', 'billingEmail', 'addressLine1', 'city', 'state', 'postalCode', 'country']
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/** Polling for a payment Razorpay hasn't confirmed yet (checkout-payment.md:
 * interval Not specified by any source — 5 s for up to 2 minutes). */
const POLL_MS = 5000
const POLL_LIMIT_MS = 120000

function formatMoney(minorUnits: number, currency: string, locale?: string) {
  try {
    return new Intl.NumberFormat(locale, { style: 'currency', currency }).format(minorUnits / 100)
  } catch {
    return `${(minorUnits / 100).toFixed(2)} ${currency}`
  }
}

function formatDate(iso?: string) {
  return iso ? new Date(iso).toLocaleDateString() : '—'
}

/** "/checkout/:productId" (C48) is kept as a link target and forwarded to the
 * C55 checkout, so existing Subscribe buttons and login returnTo URLs work. */
export function LegacyCheckoutRedirect() {
  const { productId } = useParams<{ productId: string }>()
  return <Navigate to={`/checkout?productId=${productId}`} replace />
}

/**
 * "/checkout" — C55, REQ-BIL-001.18/.19 ([checkout-payment.md](docs/05-ui/screen-requirements/checkout-payment.md)).
 * One screen with three steps for every payment: a new paid subscription
 * (`?productId=`, subscribes on "Continue to payment"), a subscription's
 * invoice (`?subscriptionId=`) or an OPEN invoice (`?invoiceId=`), in the
 * individual scope or, with `&scope=organization`, the organization's.
 *
 * BR-BIL-001: no card number, expiry or CVV is ever held in this page's
 * state, sent to EIS, or logged. The card panel shows decorative
 * placeholders only; Pay opens Razorpay Checkout, where the customer types
 * the card (C55 records why — Razorpay offers no embeddable card fields we
 * could confirm).
 */
export function CheckoutPage() {
  const { t, i18n } = useTranslation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'))
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()

  const isOrganization = params.get('scope') === 'organization'
  const api = isOrganization ? organizationBillingApi : myBillingApi
  const productId = params.get('productId') ? Number(params.get('productId')) : null
  const step: Step = (params.get('step') as Step) ?? 'details'
  const selfUrl = `/checkout?${params.toString()}`

  const [product, setProduct] = useState<Product | null>(null)
  const [summary, setSummary] = useState<CheckoutSummary | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [details, setDetails] = useState<Details>(EMPTY_DETAILS)
  const [detailsSaved, setDetailsSaved] = useState(false)
  const [editingDetails, setEditingDetails] = useState(false)
  const [touched, setTouched] = useState<Partial<Record<keyof Details, boolean>>>({})
  const [option, setOption] = useState<OptionKey | null>(null)
  const [saveCard, setSaveCard] = useState(false)
  const [consent, setConsent] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<Result | null>(null)
  const [toast, setToast] = useState<{ message: string; severity: 'success' | 'error' } | null>(null)
  const resultHeading = useRef<HTMLHeadingElement>(null)
  const busyRef = useRef(false)

  const goTo = useCallback((next: Step, extra?: Record<string, string>) => {
    const p = new URLSearchParams(params)
    p.set('step', next)
    if (extra) Object.entries(extra).forEach(([k, v]) => p.set(k, v))
    if (extra?.subscriptionId) p.delete('productId')
    setParams(p)
  }, [params, setParams])

  const redirectToLogin = useCallback(() => {
    navigate(`/login?returnTo=${encodeURIComponent(selfUrl)}`, { replace: true })
  }, [navigate, selfUrl])

  const handleError = useCallback((e: unknown, fallback: string) => {
    if (e instanceof ApiError && e.status === 401) {
      redirectToLogin()
      return true
    }
    setError(e instanceof ApiError ? e.message : fallback)
    return false
  }, [redirectToLogin])

  // Loads once per checkout target. Billing details 404 = nothing saved yet.
  const invoiceId = params.get('invoiceId')
  const subscriptionId = params.get('subscriptionId')
  useEffect(() => {
    let active = true
    const target = invoiceId
      ? api.checkout({ invoiceId: Number(invoiceId) })
      : subscriptionId
        ? api.checkout({ subscriptionId: Number(subscriptionId) })
        : null
    Promise.allSettled([
      target ?? Promise.resolve(null),
      productId && !target ? productsApi.get(productId) : Promise.resolve(null),
      api.getDetails(),
    ]).then(([summaryResult, productResult, detailsResult]) => {
      if (!active) return
      const failed = [summaryResult, productResult].find((r) => r.status === 'rejected') as PromiseRejectedResult | undefined
      if (failed) {
        if (failed.reason instanceof ApiError && failed.reason.status === 401) {
          redirectToLogin()
          return
        }
        setLoadError(t('checkout.loadError'))
      } else if (!target && !productId) {
        setLoadError(t('checkout.loadError'))
      }
      if (summaryResult.status === 'fulfilled') setSummary(summaryResult.value)
      if (productResult.status === 'fulfilled') setProduct(productResult.value)
      if (detailsResult.status === 'fulfilled') {
        const d = detailsResult.value
        setDetails({ ...EMPTY_DETAILS, ...d, addressLine2: d.addressLine2 ?? '', taxId: d.taxId ?? '' })
        setDetailsSaved(true)
      } else if (detailsResult.reason instanceof ApiError && detailsResult.reason.status === 401) {
        redirectToLogin()
        return
      }
      setLoading(false)
    })
    return () => { active = false }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- reload only when the checkout target changes
  }, [invoiceId, subscriptionId, productId, isOrganization])

  // A paid invoice reached directly shows its result instead of a second payment (FRD BR-4).
  const settledResult = useMemo<Result | null>(() => {
    if (!summary?.invoiceStatus || summary.invoiceStatus === 'OPEN') return null
    return summary.invoiceStatus === 'PAID'
      ? { kind: 'success', payment: { amount: summary.total, currency: summary.currency } as Payment }
      : { kind: 'failed', reason: summary.invoiceStatus, cancelled: false }
  }, [summary])
  const shownResult = result ?? settledResult
  const shownStep: Step = !result && settledResult ? 'complete' : step

  useEffect(() => {
    if (shownStep === 'complete') resultHeading.current?.focus()
  }, [shownStep, shownResult])

  const fieldError = (field: keyof Details): string | null => {
    const value = details[field].trim()
    if (REQUIRED.includes(field) && !value) return t('checkout.details.required')
    if (field === 'billingEmail' && value && !EMAIL.test(value)) return t('checkout.details.invalidEmail')
    if ((field === 'billingName' || field === 'addressLine1' || field === 'addressLine2') && value.length > 200) return t('checkout.details.tooLong')
    return null
  }
  const detailsValid = REQUIRED.every((f) => !fieldError(f)) && !fieldError('addressLine2')
  const showForm = !detailsSaved || editingDetails

  const continueToPayment = async () => {
    if (busyRef.current) return
    setTouched(Object.fromEntries(REQUIRED.map((f) => [f, true])))
    if (!detailsValid) return
    busyRef.current = true
    setBusy(true)
    setError(null)
    try {
      if (showForm) {
        await api.saveDetails({
          ...details,
          addressLine2: details.addressLine2 || undefined,
          taxId: details.taxId || undefined,
        })
        setDetailsSaved(true)
        setEditingDetails(false)
      }
      if (productId && !summary) {
        const subscription = await myProductsApi.subscribe(productId)
        const next = await api.checkout({ subscriptionId: subscription.id })
        setSummary(next)
        if (!next.invoiceId) {
          setResult({ kind: 'free' })
          goTo('complete', { subscriptionId: String(subscription.id) })
          return
        }
        goTo('payment', { subscriptionId: String(subscription.id) })
        return
      }
      goTo('payment')
    } catch (e) {
      handleError(e, t('checkout.genericError'))
    } finally {
      busyRef.current = false
      setBusy(false)
    }
  }

  const pollUntilSettled = useCallback(async (invoice: number) => {
    const started = Date.now()
    while (Date.now() - started < POLL_LIMIT_MS) {
      await new Promise((r) => setTimeout(r, POLL_MS))
      try {
        const detail = await api.invoiceDetail(invoice)
        if (detail.status === 'PAID') {
          const paid = detail.payments?.find((p) => p.status === 'CAPTURED')
          setResult({ kind: 'success', payment: paid ?? ({ amount: detail.total, currency: detail.currency } as Payment) })
          return
        }
      } catch {
        // keep polling — a transient failure isn't a payment result
      }
    }
    setResult({ kind: 'pendingSlow' })
  }, [api])

  const pay = async () => {
    if (busyRef.current || !option || !consent || !summary?.invoiceId) return
    busyRef.current = true
    setBusy(true)
    setError(null)
    const invoice = summary.invoiceId
    try {
      if (option === 'offline') {
        const offline = await api.payByInvoice(invoice)
        setResult({ kind: 'offline', offline })
        setToast({ message: t('checkout.toast.invoiceGenerated'), severity: 'success' })
        goTo('complete')
        return
      }
      const order = await api.createPayment(invoice)
      const razorpay = await openRazorpayCheckout({
        keyId: order.keyId, orderId: order.providerOrderId, amount: order.amount, currency: order.currency,
        name: 'eVyoog', description: summary.invoiceNumber ?? '',
        method: option === 'card' ? 'card' : option === 'upi' ? 'upi' : undefined,
      })
      const payment = await api.confirmPayment(order.paymentId, razorpay)
      goTo('complete')
      if (payment.status === 'CAPTURED') {
        setResult({ kind: 'success', payment })
        setToast({ message: t('checkout.toast.paid'), severity: 'success' })
      } else if (payment.status === 'FAILED') {
        setResult({ kind: 'failed', reason: payment.failureReason ?? t('checkout.genericError'), cancelled: false })
        setToast({ message: t('checkout.toast.failed'), severity: 'error' })
      } else {
        setResult({ kind: 'pending' })
        void pollUntilSettled(invoice)
      }
      void saveCard // consent only: the card itself is saved by Razorpay, never by EIS
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) {
        redirectToLogin()
        return
      }
      const cancelled = e instanceof Error && e.message === 'cancelled'
      setResult({ kind: 'failed', cancelled, reason: cancelled ? t('checkout.result.cancelled') : (e instanceof ApiError ? e.message : t('checkout.genericError')) })
      setToast({ message: t('checkout.toast.failed'), severity: 'error' })
      goTo('complete')
    } finally {
      busyRef.current = false
      setBusy(false)
    }
  }

  const items = useMemo(() => {
    if (summary) return summary.items
    if (!product) return []
    const plan = product.plans.find((p) => p.billingPeriod === 'MONTHLY') ?? product.plans[0]
    const price = plan ? plan.price : product.price
    return [{
      productId: product.id, productName: product.name, imageUrl: product.imageUrl, planName: plan?.name,
      billingPeriod: plan?.billingPeriod, amount: Math.round(price * 100),
    }]
  }, [summary, product])
  const currency = summary?.currency ?? product?.plans[0]?.currency ?? 'USD'
  const subtotal = summary?.subtotal ?? items.reduce((s, i) => s + i.amount, 0)
  const total = summary?.total ?? subtotal
  const money = (v: number) => formatMoney(v, currency, i18n.language)

  if (loading) {
    return (
      <Box>
        <Skeleton variant="rounded" height={48} sx={{ maxWidth: 520, mx: 'auto', mb: 3 }} />
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '3fr 2fr' } }}>
          <Skeleton variant="rounded" height={420} />
          <Skeleton variant="rounded" height={320} />
        </Box>
      </Box>
    )
  }
  if (loadError) {
    return (
      <Alert severity="error" action={<Button component={RouterLink} to={isOrganization ? '/organization/billing' : '/billing'}>{t('checkout.actions.viewBilling')}</Button>}>
        {loadError}
      </Alert>
    )
  }

  const orderSummary = (
    <OrderSummary items={items} subtotal={subtotal} total={total} taxLines={summary?.taxLines ?? []} money={money} />
  )

  return (
    <Box sx={{ pb: isMobile ? 10 : 4 }}>
      <Typography variant="h5" component="h1" sx={{ fontWeight: 700, mb: 2, textAlign: 'center' }}>{t('checkout.title')}</Typography>
      <CheckoutStepper step={shownStep} />

      {isMobile && (
        <Accordion disableGutters variant="outlined" sx={{ mb: 2, borderRadius: 2, '&:before': { display: 'none' } }}>
          <AccordionSummary expandIcon={<ChevronDown size={18} />}>
            <Typography sx={{ fontWeight: 600 }}>{t('checkout.summary.toggle', { total: money(total) })}</Typography>
          </AccordionSummary>
          <AccordionDetails>{orderSummary}</AccordionDetails>
        </Accordion>
      )}

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '3fr 2fr' }, alignItems: 'start' }}>
        <Paper variant="outlined" sx={{ p: 3, borderRadius: 3 }}>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          {shownStep === 'details' && (
            <BillingDetailsStep
              details={details} setDetails={setDetails} showForm={showForm} touched={touched}
              setTouched={setTouched} fieldError={fieldError} busy={busy}
              onEdit={() => setEditingDetails(true)} onContinue={continueToPayment}
              onBack={() => navigate(-1)}
            />
          )}

          {shownStep === 'payment' && summary && (
            <PaymentStep
              summary={summary} option={option} setOption={setOption} saveCard={saveCard} setSaveCard={setSaveCard}
              consent={consent} setConsent={setConsent} busy={busy} total={money(total)} onPay={pay}
              onBack={() => goTo('details')}
            />
          )}
          {shownStep === 'payment' && !summary && (
            <Alert severity="info" action={<Button onClick={() => goTo('details')}>{t('checkout.details.back')}</Button>}>
              {t('checkout.details.title')}
            </Alert>
          )}

          {shownStep === 'complete' && shownResult && (
            <ResultStep
              result={shownResult} headingRef={resultHeading} money={money} summary={summary} isOrganization={isOrganization}
              api={api} onTryAgain={() => { setResult(null); goTo('payment') }}
              onChooseAnother={() => { setResult(null); setOption(null); goTo('payment') }}
            />
          )}
        </Paper>

        {!isMobile && orderSummary}
      </Box>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        {toast ? <Alert severity={toast.severity} onClose={() => setToast(null)} variant="filled">{toast.message}</Alert> : undefined}
      </Snackbar>
    </Box>
  )
}

function CheckoutStepper({ step }: { step: Step }) {
  const { t } = useTranslation()
  const steps: Step[] = ['details', 'payment', 'complete']
  const activeIndex = steps.indexOf(step)
  return (
    <Box component="ol" aria-label={t('checkout.stepperLabel')} sx={{
      listStyle: 'none', p: 0, m: 0, mb: 3, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 1,
    }}>
      {steps.map((s, i) => {
        const done = i < activeIndex
        const active = i === activeIndex
        return (
          <Box component="li" key={s} aria-current={active ? 'step' : undefined} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {i > 0 && <Box aria-hidden sx={{ width: { xs: 20, sm: 56 }, height: 2, bgcolor: done || active ? 'primary.main' : 'divider', borderRadius: 1 }} />}
            <Box sx={{
              width: 28, height: 28, borderRadius: '50%', display: 'grid', placeItems: 'center', fontSize: 13, fontWeight: 700,
              bgcolor: active || done ? 'primary.main' : 'action.disabledBackground',
              color: active || done ? 'primary.contrastText' : 'text.secondary',
            }}>
              {done ? <Check size={15} aria-hidden /> : i + 1}
            </Box>
            <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? 'text.primary' : 'text.secondary', display: { xs: active ? 'block' : 'none', sm: 'block' } }}>
              {t(`checkout.steps.${s}`)}
            </Typography>
          </Box>
        )
      })}
    </Box>
  )
}

function OrderSummary({ items, subtotal, total, taxLines, money }: {
  items: { productName: string; imageUrl?: string; planName?: string; billingPeriod?: string; periodStart?: string; periodEnd?: string; amount: number; quantity?: number }[]
  subtotal: number; total: number; taxLines: { name: string; ratePercent?: number; amount: number }[]; money: (v: number) => string
}) {
  const { t } = useTranslation()
  const theme = useTheme()
  return (
    <Paper variant="outlined" component="aside" aria-label={t('checkout.summary.title')} sx={{
      p: 3, borderRadius: 3,
      background: `linear-gradient(160deg, ${alpha(theme.palette.primary.main, 0.08)}, ${alpha(theme.palette.secondary.main, 0.06)})`,
    }}>
      <Typography component="h2" variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
        {t('checkout.summary.title')} <Box component="span" sx={{ color: 'text.secondary', fontWeight: 500 }}>({items.length})</Box>
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, mb: 2 }}>
        {items.map((item, i) => (
          <Paper key={i} variant="outlined" sx={{ p: 1.5, display: 'flex', gap: 1.5, alignItems: 'center', borderRadius: 2 }}>
            <Box sx={{ width: 44, height: 44, borderRadius: 1.5, flexShrink: 0, overflow: 'hidden', display: 'grid', placeItems: 'center', bgcolor: alpha(theme.palette.primary.main, 0.1), color: 'primary.main' }}>
              {item.imageUrl ? <Box component="img" src={resolveAssetUrl(item.imageUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <Package size={20} aria-hidden />}
            </Box>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography sx={{ fontWeight: 600 }} noWrap>{item.productName}</Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                {[item.planName, item.billingPeriod ? t(`checkout.period.${item.billingPeriod}`, { defaultValue: item.billingPeriod }) : null].filter(Boolean).join(' · ')}
              </Typography>
              {(item.periodStart || item.periodEnd) && (
                <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                  {formatDate(item.periodStart)} – {formatDate(item.periodEnd)}
                </Typography>
              )}
              {item.quantity != null && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('checkout.summary.qty', { qty: item.quantity })}</Typography>}
            </Box>
            <Typography sx={{ fontWeight: 600, fontVariantNumeric: 'tabular-nums' }}>{money(item.amount)}</Typography>
          </Paper>
        ))}
      </Box>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.75 }}>
        <SummaryRow label={t('checkout.summary.subtotal')} value={money(subtotal)} />
        {taxLines.length === 0
          ? <SummaryRow label={t('checkout.summary.noTax')} value="" />
          : taxLines.map((line) => (
            <SummaryRow key={line.name} label={line.ratePercent != null ? `${line.name} (${line.ratePercent} %)` : line.name} value={money(line.amount)} />
          ))}
        <Divider sx={{ my: 1 }} />
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
          <Typography sx={{ fontWeight: 700 }}>{t('checkout.summary.total')}</Typography>
          <Typography variant="h5" component="p" sx={{ fontWeight: 800, fontVariantNumeric: 'tabular-nums' }}>{money(total)}</Typography>
        </Box>
      </Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2, color: 'text.secondary' }}>
        <Lock size={14} aria-hidden />
        <Typography variant="caption">{t('checkout.summary.trust')}</Typography>
      </Box>
    </Paper>
  )
}

function SummaryRow({ label, value }: { label: string; value: string }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{label}</Typography>
      <Typography variant="body2" sx={{ fontVariantNumeric: 'tabular-nums' }}>{value}</Typography>
    </Box>
  )
}

function BillingDetailsStep({ details, setDetails, showForm, touched, setTouched, fieldError, busy, onEdit, onContinue, onBack }: {
  details: Details; setDetails: (fn: (d: Details) => Details) => void; showForm: boolean
  touched: Partial<Record<keyof Details, boolean>>; setTouched: (fn: (t: Partial<Record<keyof Details, boolean>>) => Partial<Record<keyof Details, boolean>>) => void
  fieldError: (f: keyof Details) => string | null; busy: boolean
  onEdit: () => void; onContinue: () => void; onBack: () => void
}) {
  const { t } = useTranslation()
  const field = (key: keyof Details, labelKey: string, opts: { required?: boolean; type?: string; half?: boolean } = {}) => {
    const err = touched[key] ? fieldError(key) : null
    return (
      <TextField
        label={t(`checkout.details.${labelKey}`)} value={details[key]} required={opts.required} type={opts.type}
        onChange={(e) => setDetails((d) => ({ ...d, [key]: e.target.value }))}
        onBlur={() => setTouched((tt) => ({ ...tt, [key]: true }))}
        error={Boolean(err)} helperText={err ?? ' '} fullWidth size="small"
        sx={{ gridColumn: opts.half ? undefined : '1 / -1' }}
      />
    )
  }
  return (
    <Box>
      <Typography component="h2" variant="h6" sx={{ fontWeight: 700, mb: 2 }}>{t('checkout.details.title')}</Typography>
      {showForm ? (
        <Box sx={{ display: 'grid', gap: 1, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
          {field('billingName', 'name', { required: true })}
          {field('billingEmail', 'email', { required: true, type: 'email' })}
          {field('addressLine1', 'address1', { required: true })}
          {field('addressLine2', 'address2')}
          {field('city', 'city', { required: true, half: true })}
          {field('state', 'state', { required: true, half: true })}
          {field('postalCode', 'postalCode', { required: true, half: true })}
          {field('country', 'country', { required: true, half: true })}
          {field('taxId', 'taxId')}
        </Box>
      ) : (
        <Paper variant="outlined" sx={{ p: 2, borderRadius: 2, display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Box>
            <Typography sx={{ fontWeight: 600 }}>{details.billingName}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{details.billingEmail}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>
              {[details.addressLine1, details.addressLine2, details.city, details.state, details.postalCode, details.country].filter(Boolean).join(', ')}
            </Typography>
            {details.taxId && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.details.taxId')}: {details.taxId}</Typography>}
          </Box>
          <Button size="small" onClick={onEdit}>{t('checkout.details.edit')}</Button>
        </Paper>
      )}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, mt: 3 }}>
        <Button onClick={onBack}>{t('checkout.details.back')}</Button>
        <Button variant="contained" size="large" disabled={busy} onClick={onContinue}
          startIcon={busy ? <CircularProgress size={16} color="inherit" /> : undefined}>
          {t('checkout.details.continue')}
        </Button>
      </Box>
    </Box>
  )
}

const OPTIONS: { key: OptionKey; icon: typeof CreditCard; marks?: string[] }[] = [
  // Text labels, not logos: no licensed acceptance-mark assets in this repo (checkout-payment.md).
  { key: 'card', icon: CreditCard, marks: ['Visa', 'Mastercard', 'RuPay', 'Amex'] },
  { key: 'upi', icon: Smartphone, marks: ['UPI'] },
  { key: 'other', icon: Building2 },
  { key: 'offline', icon: FileText },
]

function PaymentStep({ summary, option, setOption, saveCard, setSaveCard, consent, setConsent, busy, total, onPay, onBack }: {
  summary: CheckoutSummary; option: OptionKey | null; setOption: (o: OptionKey) => void
  saveCard: boolean; setSaveCard: (v: boolean) => void; consent: boolean; setConsent: (v: boolean) => void
  busy: boolean; total: string; onPay: () => void; onBack: () => void
}) {
  const { t } = useTranslation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'))
  const groupRef = useRef<HTMLDivElement>(null)

  const available = OPTIONS.filter((o) => o.key !== 'offline' || summary.payByInvoiceAllowed)
  const enabled = (key: OptionKey) => key === 'offline' ? summary.payByInvoiceAllowed : summary.gatewayConfigured
  const enabledKeys = available.filter((o) => enabled(o.key)).map((o) => o.key)
  const focusKey = option && enabledKeys.includes(option) ? option : enabledKeys[0]

  const onKeyDown = (e: React.KeyboardEvent) => {
    if (!['ArrowDown', 'ArrowRight', 'ArrowUp', 'ArrowLeft'].includes(e.key) || enabledKeys.length === 0) return
    e.preventDefault()
    const current = enabledKeys.indexOf(option ?? enabledKeys[0])
    const delta = e.key === 'ArrowDown' || e.key === 'ArrowRight' ? 1 : -1
    const next = enabledKeys[(current + delta + enabledKeys.length) % enabledKeys.length]
    setOption(next)
    groupRef.current?.querySelector<HTMLElement>(`[data-option="${next}"]`)?.focus()
  }

  const canPay = Boolean(option && enabled(option) && consent && !busy)
  const payLabel = option === 'offline' ? t('checkout.generateInvoice') : t('checkout.pay', { amount: total })

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <Typography component="h2" variant="h6" sx={{ fontWeight: 700 }} id="payment-options-label">{t('checkout.payment.title')}</Typography>
        <Lock size={16} aria-hidden />
      </Box>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('checkout.payment.subtitle')}</Typography>

      {!summary.gatewayConfigured && (
        <Alert severity="info" sx={{ mb: 2 }}>{t('billing.gateway.notConfigured')}</Alert>
      )}
      {enabledKeys.length === 0 && <Alert severity="warning" sx={{ mb: 2 }}>{t('checkout.payment.noOptions')}</Alert>}

      <Box ref={groupRef} role="radiogroup" aria-labelledby="payment-options-label" onKeyDown={onKeyDown}
        sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {available.map(({ key, icon: Icon, marks }) => {
          const selected = option === key
          const disabled = !enabled(key)
          return (
            <Box key={key} sx={{
              border: '1px solid', borderRadius: 2, transition: 'border-color .15s, background-color .15s',
              borderColor: selected ? 'primary.main' : 'divider',
              bgcolor: selected ? alpha(theme.palette.primary.main, 0.05) : 'background.paper',
              opacity: disabled ? 0.55 : 1,
            }}>
              <Box
                role="radio" data-option={key} aria-checked={selected} aria-disabled={disabled || undefined}
                aria-controls={selected ? `option-panel-${key}` : undefined}
                tabIndex={disabled ? -1 : key === focusKey ? 0 : -1}
                onClick={() => !disabled && setOption(key)}
                onKeyDown={(e) => { if (!disabled && (e.key === ' ' || e.key === 'Enter')) { e.preventDefault(); setOption(key) } }}
                sx={{
                  display: 'flex', alignItems: 'center', gap: 1.5, p: 2, cursor: disabled ? 'not-allowed' : 'pointer', borderRadius: 2,
                  '&:focus-visible': { outline: `2px solid ${theme.palette.primary.main}`, outlineOffset: 2 },
                }}
              >
                <Box aria-hidden sx={{
                  width: 18, height: 18, borderRadius: '50%', border: '2px solid', flexShrink: 0,
                  borderColor: selected ? 'primary.main' : 'text.disabled', display: 'grid', placeItems: 'center',
                }}>
                  {selected && <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'primary.main' }} />}
                </Box>
                <Box sx={{ flex: 1, minWidth: 0 }}>
                  <Typography sx={{ fontWeight: 600 }}>{t(`checkout.option.${key}.title`)}</Typography>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t(`checkout.option.${key}.description`)}</Typography>
                </Box>
                <Box sx={{ display: { xs: 'none', sm: 'flex' }, gap: 0.5, alignItems: 'center', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                  {marks?.map((m) => <Chip key={m} label={m} size="small" variant="outlined" sx={{ fontSize: 11, height: 22 }} />)}
                  {!marks && <Icon size={20} aria-hidden />}
                </Box>
              </Box>
              {selected && (
                <Box id={`option-panel-${key}`} role="region" aria-label={t(`checkout.option.${key}.title`)} sx={{ px: 2, pb: 2 }}>
                  {key === 'card' && <CardPanel saveCard={saveCard} setSaveCard={setSaveCard} />}
                  {key === 'upi' && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.upi.note')}</Typography>}
                  {key === 'other' && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.other.note')}</Typography>}
                  {key === 'offline' && (
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.offline.terms')}</Typography>
                      {summary.billingEmail && (
                        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.offline.emailTo', { email: summary.billingEmail })}</Typography>
                      )}
                    </Box>
                  )}
                </Box>
              )}
            </Box>
          )
        })}
      </Box>

      <Box sx={isMobile ? {
        position: 'fixed', left: 0, right: 0, bottom: 0, zIndex: 10, p: 2,
        bgcolor: 'background.paper', borderTop: '1px solid', borderColor: 'divider',
      } : { mt: 3 }}>
        <Button variant="contained" size="large" fullWidth disabled={!canPay} onClick={onPay}
          startIcon={busy ? <CircularProgress size={16} color="inherit" /> : <Lock size={16} aria-hidden />}
          sx={{ py: 1.5, fontSize: 16 }}>
          {payLabel}
        </Button>
      </Box>
      <FormControlLabel
        sx={{ mt: 1.5, alignItems: 'flex-start' }}
        control={<Checkbox checked={consent} onChange={(e) => setConsent(e.target.checked)} sx={{ pt: 0.25 }} />}
        label={<Typography variant="body2">{t('checkout.consent')}</Typography>}
      />
      <Box sx={{ mt: 1 }}>
        <Button onClick={onBack}>{t('checkout.details.back')}</Button>
      </Box>
    </Box>
  )
}

/** BR-BIL-001: decorative only. These are not inputs — nothing here can
 * receive or hold a card number, expiry or CVV. */
function CardPanel({ saveCard, setSaveCard }: { saveCard: boolean; setSaveCard: (v: boolean) => void }) {
  const { t } = useTranslation()
  const placeholder = (label: string, value: string, extra?: React.ReactNode) => (
    <Box sx={{ flex: 1, minWidth: 0 }}>
      <Typography variant="caption" sx={{ color: 'text.secondary', display: 'flex', alignItems: 'center', gap: 0.5 }}>{label}{extra}</Typography>
      <Box aria-hidden sx={{
        mt: 0.5, px: 1.5, py: 1.1, border: '1px dashed', borderColor: 'divider', borderRadius: 1.5,
        color: 'text.disabled', fontFamily: 'monospace', bgcolor: 'action.hover', userSelect: 'none',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        {value}
      </Box>
    </Box>
  )
  return (
    <Box>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('checkout.card.lead')}</Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {placeholder(t('checkout.card.number'), '1234 1234 1234 1234')}
        <Box sx={{ display: 'flex', gap: 1.5, flexDirection: { xs: 'column', sm: 'row' } }}>
          {placeholder(t('checkout.card.name'), '—')}
          {placeholder(t('checkout.card.expiry'), 'MM / YY')}
          {placeholder(t('checkout.card.cvv'), '•••', (
            <Tooltip title={t('checkout.card.cvvHelp')}>
              <Box component="span" role="img" tabIndex={0} aria-label={t('checkout.card.cvvHelp')} sx={{ display: 'inline-flex' }}><HelpCircle size={13} /></Box>
            </Tooltip>
          ))}
        </Box>
      </Box>
      <Alert icon={<Lock size={16} />} severity="info" sx={{ mt: 1.5 }}>{t('checkout.card.secureNote')}</Alert>
      <FormControlLabel
        sx={{ mt: 1 }}
        control={<Checkbox checked={saveCard} onChange={(e) => setSaveCard(e.target.checked)} />}
        label={<Typography variant="body2">{t('checkout.card.save')}</Typography>}
      />
    </Box>
  )
}

function ResultStep({ result, headingRef, money, summary, isOrganization, api, onTryAgain, onChooseAnother }: {
  result: Result; headingRef: React.RefObject<HTMLHeadingElement | null>; money: (v: number) => string
  summary: CheckoutSummary | null; isOrganization: boolean; api: typeof myBillingApi
  onTryAgain: () => void; onChooseAnother: () => void
}) {
  const { t } = useTranslation()
  const [copied, setCopied] = useState<string | null>(null)
  const billingPath = isOrganization ? '/organization/billing' : '/billing'
  const invoiceId = result.kind === 'offline' ? result.offline.invoiceId : summary?.invoiceId
  const heading = (text: string) => (
    <Typography ref={headingRef} tabIndex={-1} component="h2" variant="h5" sx={{ fontWeight: 700, outline: 'none', mt: 1.5 }}>{text}</Typography>
  )
  const copy = (label: string, value: string) => {
    void navigator.clipboard?.writeText(value)
    setCopied(label)
  }

  return (
    <Box aria-live="polite" sx={{ textAlign: 'center', py: 2 }}>
      {result.kind === 'success' && (
        <>
          <Box sx={{ color: 'success.main' }}><CheckCircle2 size={56} aria-hidden /></Box>
          {heading(t('checkout.result.success.title'))}
          <Box sx={{ my: 2, display: 'inline-grid', gridTemplateColumns: 'auto auto', gap: 0.5, columnGap: 3, textAlign: 'left' }}>
            {summary?.invoiceNumber && <><Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.invoiceNumber')}</Typography><Typography variant="body2">{summary.invoiceNumber}</Typography></>}
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.amountPaid')}</Typography>
            <Typography variant="body2">{money(result.payment.amount ?? summary?.total ?? 0)}</Typography>
            {result.payment.methodType && <>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.method')}</Typography>
              <Typography variant="body2">{[result.payment.methodNetwork ?? result.payment.methodType, result.payment.methodLast4 ? `•••• ${result.payment.methodLast4}` : null].filter(Boolean).join(' ')}</Typography>
            </>}
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.date')}</Typography>
            <Typography variant="body2">{formatDate(result.payment.capturedAt ?? new Date().toISOString())}</Typography>
          </Box>
          <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center', flexWrap: 'wrap' }}>
            {invoiceId && <Button variant="outlined" onClick={() => api.downloadDocument(invoiceId, 'receipt')}>{t('checkout.actions.downloadReceipt')}</Button>}
            {invoiceId && <Button variant="outlined" onClick={() => api.downloadDocument(invoiceId, 'invoice')}>{t('checkout.actions.downloadInvoice')}</Button>}
            <Button variant="contained" component={RouterLink} to="/my/products">{t('checkout.actions.goToMyProducts')}</Button>
            <Button component={RouterLink} to={billingPath}>{t('checkout.actions.viewBilling')}</Button>
          </Box>
        </>
      )}

      {(result.kind === 'pending' || result.kind === 'pendingSlow') && (
        <>
          {result.kind === 'pending' ? <CircularProgress size={48} /> : <Box sx={{ color: 'warning.main' }}><Lock size={48} aria-hidden /></Box>}
          {heading(t('checkout.result.pending.title'))}
          {result.kind === 'pendingSlow' && <Typography sx={{ color: 'text.secondary', mt: 1 }}>{t('checkout.result.pending.slow')}</Typography>}
          <Button component={RouterLink} to={billingPath} sx={{ mt: 2 }}>{t('checkout.actions.viewBilling')}</Button>
        </>
      )}

      {result.kind === 'failed' && (
        <>
          <Box sx={{ color: 'error.main' }}><XCircle size={56} aria-hidden /></Box>
          {heading(t('checkout.result.failed.title'))}
          <Typography sx={{ mt: 1 }}>{result.reason}</Typography>
          <Typography sx={{ color: 'text.secondary', mb: 2 }}>{t('checkout.result.stillOpen')}</Typography>
          <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center' }}>
            <Button variant="contained" onClick={onTryAgain}>{t('checkout.result.tryAgain')}</Button>
            <Button variant="outlined" onClick={onChooseAnother}>{t('checkout.result.chooseAnother')}</Button>
          </Box>
        </>
      )}

      {result.kind === 'free' && (
        <>
          <Box sx={{ color: 'success.main' }}><CheckCircle2 size={56} aria-hidden /></Box>
          {heading(t('checkout.result.free.title'))}
          <Button variant="contained" component={RouterLink} to="/my/products" sx={{ mt: 2 }}>{t('checkout.actions.goToMyProducts')}</Button>
        </>
      )}

      {result.kind === 'offline' && (
        <Box sx={{ textAlign: 'left' }}>
          <Box sx={{ textAlign: 'center' }}>
            <Box sx={{ color: 'primary.main' }}><FileText size={52} aria-hidden /></Box>
            {heading(t('checkout.result.offline.title'))}
          </Box>
          <Box sx={{ my: 2, display: 'grid', gridTemplateColumns: 'auto 1fr', gap: 0.5, columnGap: 3 }}>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.invoiceNumber')}</Typography>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>{result.offline.invoiceNumber}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.total')}</Typography>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>{money(result.offline.total)}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.dueDate')}</Typography>
            <Typography variant="body2">{formatDate(result.offline.dueAt)}</Typography>
          </Box>
          <Paper variant="outlined" sx={{ p: 2, borderRadius: 2, mb: 2 }}>
            <Typography sx={{ fontWeight: 700, mb: 1 }}>{t('checkout.result.offline.bankTitle')}</Typography>
            {result.offline.bankDetails.accountName ? (
              ([
                ['accountName', result.offline.bankDetails.accountName],
                ['bankName', result.offline.bankDetails.bankName],
                ['accountNumber', result.offline.bankDetails.accountNumber],
                ['ifsc', result.offline.bankDetails.ifsc],
                ['swift', result.offline.bankDetails.swiftBic],
              ] as const).filter(([, v]) => v).map(([key, value]) => (
                <Box key={key} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
                  <Typography variant="body2" sx={{ color: 'text.secondary', minWidth: 120 }}>{t(`checkout.bank.${key}`)}</Typography>
                  <Typography variant="body2" sx={{ flex: 1, fontFamily: 'monospace' }}>{value}</Typography>
                  <Tooltip title={copied === key ? t('checkout.bank.copied') : t('checkout.bank.copy')}>
                    <IconButton size="small" aria-label={t('checkout.bank.copyField', { field: t(`checkout.bank.${key}`) })} onClick={() => copy(key, value!)}>
                      <Copy size={14} />
                    </IconButton>
                  </Tooltip>
                </Box>
              ))
            ) : (
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.result.offline.noBank')}</Typography>
            )}
          </Paper>
          <Alert severity="info" sx={{ mb: 1 }}>{t('checkout.result.offline.quote', { number: result.offline.invoiceNumber })}</Alert>
          {result.offline.billingEmail && (
            <Typography variant="body2" sx={{ mb: 0.5 }}>{t('checkout.result.offline.emailed', { email: result.offline.billingEmail })}</Typography>
          )}
          {result.offline.subscriptionStatus && (
            <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('checkout.result.offline.subscriptionStatus', { status: result.offline.subscriptionStatus })}</Typography>
          )}
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <Button variant="contained" onClick={() => api.downloadDocument(result.offline.invoiceId, 'invoice')}>{t('checkout.actions.downloadInvoice')}</Button>
            <Button component={RouterLink} to={billingPath}>{t('checkout.actions.viewBilling')}</Button>
          </Box>
        </Box>
      )}
    </Box>
  )
}
