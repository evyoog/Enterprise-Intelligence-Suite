import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, Navigate, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import {
  Alert, Badge, Box, Button, Checkbox, Chip, CircularProgress, Collapse, Dialog, DialogActions, DialogContent,
  DialogContentText, DialogTitle, Divider, FormControlLabel, IconButton, Link, Menu, MenuItem, Paper, Skeleton,
  Snackbar, TextField, Tooltip, Typography, useMediaQuery, useTheme,
} from '@mui/material'
import { alpha } from '@mui/material/styles'
import {
  Building2, Check, CheckCircle2, ChevronDown, ChevronLeft, ClipboardCheck, Copy, CreditCard, FileText, HelpCircle,
  Lock, MoreVertical, Package, Plus, Smartphone, Wallet, XCircle,
} from 'lucide-react'
import {
  myBillingApi, organizationBillingApi,
  type CheckoutMethod, type CheckoutSummary, type OfflineInvoiceResult, type Payment, type PaymentMethod,
} from '../api/billingApi'
import { ApiError, resolveAssetUrl } from '../api/client'
import { productsApi, type Product } from '../api/productsApi'
import { myProductsApi } from '../api/registrationApi'
import { PaymentLogo, PaymentLogos } from '../components/payments/PaymentLogos'
import type { PaymentBrand } from '../components/payments/paymentBrands'
import { openRazorpayCheckout } from '../utils/razorpayCheckout'
import { formatMoney } from '../utils/money'

type Step = 'details' | 'payment' | 'complete'
type OptionKey = 'card' | 'upi' | 'netbanking' | 'wallets' | 'offline'
type Result =
  | { kind: 'success'; payment: Payment }
  | { kind: 'pending' }
  | { kind: 'pendingSlow' }
  | { kind: 'failed'; reason: string; cancelled: boolean }
  | { kind: 'offline'; offline: OfflineInvoiceResult }
  | { kind: 'free' }
  | { kind: 'orders'; orderIds: number[] }

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

/** Same image the signed-in tool frame uses (AppShell). */
const EIS_LOGO = 'https://www.vyoog.com/wp-content/uploads/2022/03/evyoog-logonew1.png'

/** Polling for a payment Razorpay hasn't confirmed yet (C55 default:
 * every 5 s for up to 2 minutes). */
const POLL_MS = 5000
const POLL_LIMIT_MS = 120000

const RAZORPAY_METHOD: Record<Exclude<OptionKey, 'offline'>, CheckoutMethod> = {
  card: 'card', upi: 'upi', netbanking: 'netbanking', wallets: 'wallet',
}

function formatDate(iso?: string) {
  return iso ? new Date(iso).toLocaleDateString() : '—'
}

/** "/checkout/:productId" (C48) is kept as a link target and forwarded to
 * the cart (C59), so old links and login returnTo URLs still add the product. */
export function LegacyCheckoutRedirect() {
  const { productId } = useParams<{ productId: string }>()
  return <Navigate to={`/cart?add=${productId}`} replace />
}

/**
 * "/checkout" — REQ-BIL-001.18/.19/.22, C55, layout redesigned by C59
 * (docs/05-ui/screen-requirements/checkout-payment.md). Breadcrumb steps
 * Cart › Billing details › Payment › Complete inside one branded card.
 * Targets: an OPEN invoice (`?invoiceId=`, from the cart with `&from=cart`
 * or from Billing), a subscription's invoice (`?subscriptionId=`), a product
 * with only a free plan (`?productId=`, the unchanged free-plan flow), or
 * an organization member's submitted orders (`?orders=1,2&step=complete`).
 * `&scope=organization` pays an organization's invoice.
 *
 * BR-BIL-001: no card number, expiry or CVV is ever held in this page's
 * state, sent to EIS, or logged. The card fields are decorative
 * placeholders; Pay opens Razorpay Checkout, where the customer types the
 * card (C55 records why).
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
  const fromCart = params.get('from') === 'cart'
  const orderIds = useMemo(() => (params.get('orders') ?? '').split(',').filter(Boolean).map(Number), [params])
  const step: Step = (params.get('step') as Step) ?? 'details'
  const selfUrl = `/checkout?${params.toString()}`

  const [product, setProduct] = useState<Product | null>(null)
  const [summary, setSummary] = useState<CheckoutSummary | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loading, setLoading] = useState(orderIds.length === 0)
  const [details, setDetails] = useState<Details>(EMPTY_DETAILS)
  const [detailsSaved, setDetailsSaved] = useState(false)
  const [editingDetails, setEditingDetails] = useState(false)
  const [touched, setTouched] = useState<Partial<Record<keyof Details, boolean>>>({})
  const [option, setOption] = useState<OptionKey | null>(null)
  const [savedMethods, setSavedMethods] = useState<PaymentMethod[]>([])
  const [selectedCard, setSelectedCard] = useState<number | 'new' | null>(null)
  const [saveCard, setSaveCard] = useState(false)
  const [consent, setConsent] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<Result | null>(orderIds.length ? { kind: 'orders', orderIds } : null)
  const [toast, setToast] = useState<{ message: string; severity: 'success' | 'error' } | null>(null)
  const [summaryOpen, setSummaryOpen] = useState(false)
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
    if (orderIds.length) return
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
      api.paymentMethods(),
    ]).then(([summaryResult, productResult, detailsResult, methodsResult]) => {
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
      if (methodsResult.status === 'fulfilled') {
        const methods = methodsResult.value ?? []
        setSavedMethods(methods)
        const usable = methods.filter((m) => m.type === 'CARD' && !m.expired)
        setSelectedCard((usable.find((m) => m.isDefault) ?? usable[0])?.id ?? 'new')
      } else {
        setSelectedCard('new')
      }
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
  const shownStep: Step = shownResult && (!result ? settledResult : result.kind === 'orders') ? 'complete' : step

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
      // Free-plan flow (no paid plan, so never through the cart): subscribe now.
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
      const method = RAZORPAY_METHOD[option]
      const paymentMethodId = option === 'card' && typeof selectedCard === 'number' ? selectedCard : undefined
      const order = await api.createPayment(invoice, { method, ...(paymentMethodId ? { paymentMethodId } : {}) })
      const razorpay = await openRazorpayCheckout({
        keyId: order.keyId, orderId: order.providerOrderId, amount: order.amount, currency: order.currency,
        // C60: name, description and colour come from Billing settings when set.
        name: summary.checkoutName || 'eVyoog',
        description: summary.checkoutDescription ? `${summary.checkoutDescription} · ${summary.invoiceNumber ?? ''}` : summary.invoiceNumber ?? '',
        method, themeColor: summary.checkoutThemeColor || undefined,
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

  const removeSavedMethod = async (methodId: number) => {
    try {
      await api.removePaymentMethod(methodId)
      const next = savedMethods.filter((m) => m.id !== methodId)
      setSavedMethods(next)
      if (selectedCard === methodId) {
        const usable = next.filter((m) => m.type === 'CARD' && !m.expired)
        setSelectedCard(usable[0]?.id ?? 'new')
      }
    } catch (e) {
      setToast({ message: e instanceof ApiError ? e.message : t('checkout.genericError'), severity: 'error' })
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
        <Skeleton variant="rounded" height={48} sx={{ maxWidth: 520, mb: 3 }} />
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '62fr 38fr' } }}>
          <Skeleton variant="rounded" height={460} />
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

  const showSummary = shownResult?.kind !== 'orders' && items.length > 0
  const cartSummary = showSummary ? (
    <CartSummary items={items} subtotal={subtotal} total={total} taxLines={summary?.taxLines ?? []} money={money}
      editCart={fromCart && shownStep !== 'complete'} />
  ) : null

  const stepBack = () => {
    if (shownStep === 'payment') goTo('details')
    else if (fromCart) navigate('/cart')
    else navigate(-1)
  }

  return (
    <Box sx={{ pb: isMobile ? 12 : 4 }}>
      <Box sx={{
        position: 'relative', overflow: 'hidden', borderRadius: { xs: 3, md: 5 },
        px: { xs: 1, sm: 3, md: 6 }, py: { xs: 1.5, sm: 4, md: 6 },
        background: `linear-gradient(135deg, ${theme.palette.primary.dark}, ${theme.palette.primary.main} 55%, ${alpha(theme.palette.secondary.main, 0.85)})`,
      }}>
        <DecorativeBackdrop />
        <Paper elevation={6} sx={{ position: 'relative', borderRadius: 4, overflow: 'hidden', maxWidth: 1180, mx: 'auto' }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: showSummary ? '62fr 38fr' : '1fr' } }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', minWidth: 0 }}>
              <Box sx={{ p: { xs: 2, sm: 4 }, pb: 0 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Box component="img" src={EIS_LOGO} alt="" sx={{ height: 34, width: 'auto' }} />
                  <Typography component="h1" variant="h5" sx={{ fontWeight: 800, letterSpacing: 0.3 }}>{t('checkout.brand')}</Typography>
                </Box>
                <Breadcrumb step={shownStep} fromCart={fromCart} onDetails={() => goTo('details')} />
              </Box>

              {isMobile && cartSummary && (
                <Box sx={{ mx: 2, mt: 2 }}>
                  <Button fullWidth variant="outlined" onClick={() => setSummaryOpen((o) => !o)} aria-expanded={summaryOpen}
                    endIcon={<ChevronDown size={16} style={{ transform: summaryOpen ? 'rotate(180deg)' : undefined }} />}
                    sx={{ justifyContent: 'space-between' }}>
                    {t('checkout.summary.toggle', { total: money(total), count: items.length })}
                  </Button>
                  <Collapse in={summaryOpen}><Box sx={{ mt: 1 }}>{cartSummary}</Box></Collapse>
                </Box>
              )}

              <Box sx={{ p: { xs: 2, sm: 4 }, flex: 1 }}>
                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                {shownStep === 'details' && (
                  <BillingDetailsStep
                    details={details} setDetails={setDetails} showForm={showForm} touched={touched}
                    setTouched={setTouched} fieldError={fieldError} busy={busy}
                    onEdit={() => setEditingDetails(true)} onContinue={continueToPayment}
                  />
                )}

                {shownStep === 'payment' && summary && (
                  <PaymentStep
                    summary={summary} details={details} onChangeDetails={() => { setEditingDetails(true); goTo('details') }}
                    option={option} setOption={setOption} savedMethods={savedMethods}
                    selectedCard={selectedCard} setSelectedCard={setSelectedCard} onRemoveMethod={removeSavedMethod}
                    saveCard={saveCard} setSaveCard={setSaveCard} consent={consent} setConsent={setConsent}
                    busy={busy} total={money(total)} onPay={pay}
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
              </Box>

              {shownStep !== 'complete' && (
                <Box component="footer" sx={{
                  px: { xs: 2, sm: 4 }, py: 2, borderTop: '1px solid', borderColor: 'divider',
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2,
                }}>
                  <Button startIcon={<ChevronLeft size={16} aria-hidden />} onClick={stepBack}>{t('checkout.footer.stepBack')}</Button>
                  {shownStep === 'payment' && (
                    // Mirrors the reference's two-level action: the result moves
                    // to Complete by itself; this button never starts a payment.
                    <Button variant="outlined" disabled>{t('checkout.footer.completeOrder')}</Button>
                  )}
                </Box>
              )}
            </Box>

            {!isMobile && cartSummary && (
              <Box sx={{ bgcolor: theme.palette.mode === 'dark' ? alpha(theme.palette.common.white, 0.04) : theme.palette.grey[100], borderLeft: { md: '1px solid' }, borderColor: { md: 'divider' } }}>
                {cartSummary}
              </Box>
            )}
          </Box>
        </Paper>
        <Box sx={{ position: 'relative', maxWidth: 1180, mx: 'auto', mt: 2, display: 'flex', alignItems: 'center', gap: 1, color: 'common.white' }}>
          <Lock size={14} aria-hidden />
          <Typography variant="caption" sx={{ opacity: 0.9 }}>{t('checkout.poweredBy')}</Typography>
          <Box sx={{ bgcolor: 'background.paper', borderRadius: 1, px: 0.5, py: 0.25 }}>
            <PaymentLogo brand="razorpay" height={18} dark={theme.palette.mode === 'dark'} />
          </Box>
        </Box>
      </Box>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        {toast ? <Alert severity={toast.severity} onClose={() => setToast(null)} variant="filled">{toast.message}</Alert> : undefined}
      </Snackbar>
    </Box>
  )
}

/** Decorative only (aria-hidden): soft curves and waves in the brand colour. */
function DecorativeBackdrop() {
  return (
    <Box aria-hidden sx={{ position: 'absolute', inset: 0, pointerEvents: 'none', color: 'common.white' }}>
      <svg width="100%" height="100%" preserveAspectRatio="none" viewBox="0 0 1200 800" focusable="false">
        <circle cx="120" cy="120" r="220" fill="none" stroke="currentColor" strokeOpacity="0.10" strokeWidth="60" />
        <circle cx="1100" cy="700" r="260" fill="none" stroke="currentColor" strokeOpacity="0.08" strokeWidth="70" />
        {[0, 1, 2].map((i) => (
          <path key={i} d={`M0 ${640 + i * 40} q 50 -30 100 0 t 100 0 t 100 0 t 100 0`} fill="none" stroke="currentColor" strokeOpacity="0.25" strokeWidth="6" />
        ))}
        <g fill="currentColor" fillOpacity="0.25">
          {Array.from({ length: 24 }, (_, i) => <circle key={i} cx={1080 + (i % 4) * 28} cy={80 + Math.floor(i / 4) * 28} r="3" />)}
        </g>
      </svg>
    </Box>
  )
}

function Breadcrumb({ step, fromCart, onDetails }: { step: Step; fromCart: boolean; onDetails: () => void }) {
  const { t } = useTranslation()
  const steps: (Step | 'cart')[] = fromCart ? ['cart', 'details', 'payment', 'complete'] : ['details', 'payment', 'complete']
  const activeIndex = steps.indexOf(step)
  return (
    <Box component="nav" aria-label={t('checkout.stepperLabel')} sx={{ mt: 1 }}>
      <Box component="ol" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 0.75 }}>
        {steps.map((s, i) => {
          const active = i === activeIndex
          // Earlier steps are links back, except once the order is complete.
          const linkable = i < activeIndex && step !== 'complete'
          const label = t(`checkout.steps.${s}`)
          return (
            <Box component="li" key={s} aria-current={active ? 'step' : undefined} sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
              {i > 0 && <Box component="span" aria-hidden sx={{ color: 'text.disabled' }}>›</Box>}
              {linkable && s === 'cart' && <Link component={RouterLink} to="/cart" variant="body2" underline="hover">{label}</Link>}
              {linkable && s === 'details' && <Link component="button" type="button" variant="body2" underline="hover" onClick={onDetails}>{label}</Link>}
              {!linkable && (
                <Typography variant="body2" sx={{ fontWeight: active ? 700 : 400, color: active ? 'text.primary' : 'text.secondary' }}>{label}</Typography>
              )}
            </Box>
          )
        })}
      </Box>
    </Box>
  )
}

function CartSummary({ items, subtotal, total, taxLines, money, editCart }: {
  items: { productName: string; imageUrl?: string; planName?: string; billingPeriod?: string; amount: number; quantity?: number }[]
  subtotal: number; total: number; taxLines: { name: string; ratePercent?: number; amount: number }[]; money: (v: number) => string
  editCart: boolean
}) {
  const { t } = useTranslation()
  const theme = useTheme()
  return (
    <Box component="aside" aria-labelledby="checkout-summary-title" sx={{ p: { xs: 2, sm: 4 } }}>
      <Typography id="checkout-summary-title" component="h2" variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>
        {t('checkout.summary.title')} <Box component="span" sx={{ color: 'text.secondary', fontWeight: 500 }}>({items.length})</Box>
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        <SummaryRow label={t('checkout.summary.subtotal')} value={money(subtotal)} />
        {taxLines.length === 0
          ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.summary.noTax')}</Typography>
          : taxLines.map((line) => (
            <SummaryRow key={line.name} label={line.ratePercent != null ? `${line.name} (${line.ratePercent} %)` : line.name} value={money(line.amount)} />
          ))}
        <Divider sx={{ my: 1 }} />
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
          <Typography sx={{ fontWeight: 600 }}>{t('checkout.summary.total')}</Typography>
          <Typography variant="h5" component="p" sx={{ fontWeight: 800, color: 'primary.main', fontVariantNumeric: 'tabular-nums' }}>{money(total)}</Typography>
        </Box>
      </Box>
      <Divider sx={{ my: 2.5 }} />
      <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 2 }}>
        {items.map((item, i) => (
          <Box component="li" key={i} sx={{ display: 'flex', gap: 1.5, alignItems: 'center' }}>
            <Badge badgeContent={item.quantity ?? 1} color="secondary" overlap="rectangular"
              anchorOrigin={{ vertical: 'top', horizontal: 'left' }} slotProps={{ badge: { 'aria-hidden': true } as Record<string, unknown> }}>
              <Box sx={{ width: 48, height: 48, borderRadius: 2, flexShrink: 0, overflow: 'hidden', display: 'grid', placeItems: 'center', bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider', color: 'primary.main' }}>
                {item.imageUrl ? <Box component="img" src={resolveAssetUrl(item.imageUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <Package size={22} aria-hidden />}
              </Box>
            </Badge>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography sx={{ fontWeight: 600 }} noWrap>{item.productName}</Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                {[item.planName, item.billingPeriod ? t(`checkout.period.${item.billingPeriod}`, { defaultValue: item.billingPeriod }) : null].filter(Boolean).join(' · ')}
              </Typography>
            </Box>
            <Typography sx={{ fontWeight: 600, fontVariantNumeric: 'tabular-nums' }}>{money(item.amount)}</Typography>
          </Box>
        ))}
      </Box>
      {editCart && (
        <Button component={RouterLink} to="/cart" size="small" sx={{ mt: 2 }}>{t('checkout.summary.editCart')}</Button>
      )}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 3, color: 'text.secondary' }}>
        <Lock size={14} aria-hidden />
        <Typography variant="caption">{t('checkout.summary.trust')}</Typography>
      </Box>
      <Box sx={{ mt: 1 }}>
        <PaymentLogos brands={['visa', 'mastercard', 'rupay', 'amex', 'upi']} height={18} dark={theme.palette.mode === 'dark'} label={t('cart.summary.acceptedMethods')} />
      </Box>
    </Box>
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

function BillingDetailsStep({ details, setDetails, showForm, touched, setTouched, fieldError, busy, onEdit, onContinue }: {
  details: Details; setDetails: (fn: (d: Details) => Details) => void; showForm: boolean
  touched: Partial<Record<keyof Details, boolean>>; setTouched: (fn: (t: Partial<Record<keyof Details, boolean>>) => Partial<Record<keyof Details, boolean>>) => void
  fieldError: (f: keyof Details) => string | null; busy: boolean
  onEdit: () => void; onContinue: () => void
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
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{addressLine(details)}</Typography>
            {details.taxId && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.details.taxId')}: {details.taxId}</Typography>}
          </Box>
          <Button size="small" onClick={onEdit}>{t('checkout.details.edit')}</Button>
        </Paper>
      )}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 3 }}>
        <Button variant="contained" size="large" disabled={busy} onClick={onContinue}
          startIcon={busy ? <CircularProgress size={16} color="inherit" /> : undefined}>
          {t('checkout.details.continue')}
        </Button>
      </Box>
    </Box>
  )
}

function addressLine(details: Details) {
  return [details.addressLine1, details.addressLine2, details.city, details.state, details.postalCode, details.country].filter(Boolean).join(', ')
}

const OPTIONS: { key: OptionKey; icon: typeof CreditCard; brands?: PaymentBrand[] }[] = [
  { key: 'card', icon: CreditCard, brands: ['visa', 'mastercard', 'rupay', 'amex'] },
  { key: 'upi', icon: Smartphone, brands: ['upi', 'gpay', 'phonepe', 'paytm', 'bhim'] },
  { key: 'netbanking', icon: Building2 },
  // Generic icon until the wallet list is decided (REQ-BIL-001 Open question 19).
  { key: 'wallets', icon: Wallet },
  { key: 'offline', icon: FileText },
]

function PaymentStep({
  summary, details, onChangeDetails, option, setOption, savedMethods, selectedCard, setSelectedCard, onRemoveMethod,
  saveCard, setSaveCard, consent, setConsent, busy, total, onPay,
}: {
  summary: CheckoutSummary; details: Details; onChangeDetails: () => void
  option: OptionKey | null; setOption: (o: OptionKey) => void
  savedMethods: PaymentMethod[]; selectedCard: number | 'new' | null; setSelectedCard: (c: number | 'new') => void
  onRemoveMethod: (id: number) => void
  saveCard: boolean; setSaveCard: (v: boolean) => void; consent: boolean; setConsent: (v: boolean) => void
  busy: boolean; total: string; onPay: () => void
}) {
  const { t } = useTranslation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'))
  const groupRef = useRef<HTMLDivElement>(null)

  // C60: tiles a billing administrator switched off are not shown at all.
  const offered = (key: OptionKey) => key === 'offline'
    ? summary.payByInvoiceAllowed
    : !summary.enabledMethods || summary.enabledMethods.includes(RAZORPAY_METHOD[key])
  const available = OPTIONS.filter((o) => offered(o.key))
  const enabled = (key: OptionKey) => key === 'offline' ? summary.payByInvoiceAllowed : summary.gatewayConfigured
  const enabledKeys = available.filter((o) => enabled(o.key)).map((o) => o.key)
  const focusKey = option && enabledKeys.includes(option) ? option : enabledKeys[0]

  const onKeyDown = (e: React.KeyboardEvent) => {
    if (!['ArrowDown', 'ArrowRight', 'ArrowUp', 'ArrowLeft'].includes(e.key) || enabledKeys.length === 0) return
    if ((e.target as HTMLElement).getAttribute('role') !== 'radio' || !(e.target as HTMLElement).dataset.option) return
    e.preventDefault()
    const current = enabledKeys.indexOf(option ?? enabledKeys[0])
    const delta = e.key === 'ArrowDown' || e.key === 'ArrowRight' ? 1 : -1
    const next = enabledKeys[(current + delta + enabledKeys.length) % enabledKeys.length]
    setOption(next)
    groupRef.current?.querySelector<HTMLElement>(`[data-option="${next}"]`)?.focus()
  }

  const methodReady = option === 'card' ? selectedCard != null : Boolean(option)
  const canPay = Boolean(option && enabled(option) && methodReady && consent && !busy)
  const payLabel = option === 'offline' ? t('checkout.generateInvoice') : t('checkout.pay', { amount: total })

  return (
    <Box>
      {/* Billing-details summary with Change links (reference layout). */}
      <Paper variant="outlined" sx={{ borderRadius: 2, mb: 3 }}>
        {[
          { label: t('checkout.review.contact'), value: details.billingEmail },
          { label: t('checkout.review.address'), value: addressLine(details) + (details.taxId ? ` · ${t('checkout.details.taxId')}: ${details.taxId}` : '') },
        ].map((row, i) => (
          <Box key={row.label} sx={{ display: 'flex', alignItems: 'center', gap: 2, px: 2, py: 1.5, borderTop: i ? '1px solid' : 'none', borderColor: 'divider' }}>
            <Typography variant="body2" sx={{ color: 'text.secondary', width: 110, flexShrink: 0 }}>{row.label}</Typography>
            <Typography variant="body2" sx={{ flex: 1, minWidth: 0, color: 'primary.main' }}>{row.value}</Typography>
            <Button size="small" onClick={onChangeDetails} aria-label={t('checkout.review.changeLabel', { field: row.label })}>{t('checkout.review.change')}</Button>
          </Box>
        ))}
      </Paper>

      {/* Payment header with the Amount due badge (display only — no partial payments). */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
        <Box sx={{ flex: 1 }}>
          <Typography component="h2" variant="h6" sx={{ fontWeight: 700 }} id="payment-options-label">{t('checkout.payment.title')}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.payment.subtitle')}</Typography>
        </Box>
        <Typography variant="body2" sx={{ color: 'text.secondary', display: { xs: 'none', sm: 'block' } }} aria-hidden>{t('checkout.payment.amountDue')}</Typography>
        <Box role="img" aria-label={t('checkout.payment.amountDueLabel', { amount: total })}
          sx={{ position: 'relative', width: 84, height: 84, flexShrink: 0, display: 'grid', placeItems: 'center' }}>
          <CircularProgress variant="determinate" value={100} size={84} thickness={2.5} color="success" sx={{ position: 'absolute', inset: 0 }} aria-hidden />
          <Typography aria-hidden sx={{ fontWeight: 800, fontSize: total.length > 9 ? 12 : 14, color: 'success.main', textAlign: 'center', px: 1, lineHeight: 1.1 }}>{total}</Typography>
        </Box>
      </Box>

      {!summary.gatewayConfigured && (
        <Alert severity="info" id="gateway-off-hint" sx={{ mb: 2 }}>{t('billing.gateway.notConfigured')}</Alert>
      )}
      {enabledKeys.length === 0 && <Alert severity="warning" sx={{ mb: 2 }}>{t('checkout.payment.noOptions')}</Alert>}

      {/* Method tiles: a radio group; the selected tile is filled with the primary colour. */}
      <Box ref={groupRef} role="radiogroup" aria-labelledby="payment-options-label" onKeyDown={onKeyDown}
        sx={{ display: 'grid', gap: 1.5, gridTemplateColumns: { xs: 'repeat(2, 1fr)', sm: `repeat(${available.length}, 1fr)` } }}>
        {available.map(({ key, icon: Icon, brands }) => {
          const selected = option === key
          const disabled = !enabled(key)
          const tile = (
            <Box
              role="radio" data-option={key} aria-checked={selected} aria-disabled={disabled || undefined}
              aria-describedby={disabled && key !== 'offline' ? 'gateway-off-hint' : undefined}
              tabIndex={key === focusKey ? 0 : -1}
              onClick={() => !disabled && setOption(key)}
              onKeyDown={(e) => { if (!disabled && (e.key === ' ' || e.key === 'Enter')) { e.preventDefault(); setOption(key) } }}
              sx={{
                position: 'relative', height: '100%', minHeight: 96, p: 1.5, borderRadius: 2, border: '1px solid',
                display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 0.75, textAlign: 'center',
                cursor: disabled ? 'not-allowed' : 'pointer', opacity: disabled ? 0.5 : 1, transition: 'background-color .15s, border-color .15s',
                borderColor: selected ? 'primary.main' : 'divider',
                bgcolor: selected ? 'primary.main' : 'background.paper',
                color: selected ? 'primary.contrastText' : 'text.primary',
                '&:hover': disabled || selected ? undefined : { borderColor: 'primary.main', bgcolor: alpha(theme.palette.primary.main, 0.04) },
                '&:focus-visible': { outline: `2px solid ${theme.palette.primary.main}`, outlineOffset: 2 },
              }}
            >
              {selected && <Box aria-hidden sx={{ position: 'absolute', top: 6, right: 6 }}><Check size={14} /></Box>}
              <Icon size={24} aria-hidden />
              <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.2 }}>{t(`checkout.option.${key}.title`)}</Typography>
              {brands && !selected && (
                <Box aria-hidden sx={{ display: { xs: 'none', md: 'flex' } }}>
                  <PaymentLogos brands={brands.slice(0, 2)} height={16} />
                </Box>
              )}
            </Box>
          )
          return disabled && key !== 'offline'
            ? <Tooltip key={key} title={t('billing.gateway.notConfigured')}><span>{tile}</span></Tooltip>
            : <Box key={key}>{tile}</Box>
        })}
      </Box>

      {option && (
        <Paper variant="outlined" id={`option-panel-${option}`} role="region" aria-label={t(`checkout.option.${option}.title`)}
          sx={{ mt: 2, p: 2, borderRadius: 2 }}>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t(`checkout.option.${option}.description`)}</Typography>
          {option === 'card' && (
            <>
              <PaymentLogos brands={['visa', 'mastercard', 'rupay', 'amex']} height={18} dark={theme.palette.mode === 'dark'} label={t('checkout.card.accepted')} />
              <SavedCards methods={savedMethods.filter((m) => m.type === 'CARD')} selected={selectedCard} onSelect={setSelectedCard} onRemove={onRemoveMethod} />
              {selectedCard === 'new' && <CardPanel saveCard={saveCard} setSaveCard={setSaveCard} />}
            </>
          )}
          {option === 'upi' && (
            <>
              <PaymentLogos brands={['upi', 'gpay', 'phonepe', 'paytm', 'bhim']} height={18} dark={theme.palette.mode === 'dark'} label={t('checkout.upi.apps')} />
              {savedMethods.filter((m) => m.type === 'UPI' && m.upiMasked).length > 0 && (
                <Box sx={{ mt: 1.5 }}>
                  <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('checkout.upi.saved')}</Typography>
                  {savedMethods.filter((m) => m.type === 'UPI' && m.upiMasked).map((m) => (
                    <Typography key={m.id} variant="body2" sx={{ fontFamily: 'monospace' }}>{m.upiMasked}</Typography>
                  ))}
                </Box>
              )}
              <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1.5 }}>{t('checkout.upi.note')}</Typography>
            </>
          )}
          {option === 'netbanking' && <Typography variant="body2">{t('checkout.netbanking.note')}</Typography>}
          {option === 'wallets' && <Typography variant="body2">{t('checkout.wallets.note')}</Typography>}
          {option === 'offline' && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
              <Typography variant="body2">{t('checkout.offline.terms')}</Typography>
              {summary.billingEmail && (
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('checkout.offline.emailTo', { email: summary.billingEmail })}</Typography>
              )}
            </Box>
          )}
        </Paper>
      )}

      <FormControlLabel
        sx={{ mt: 2, alignItems: 'flex-start' }}
        control={<Checkbox checked={consent} onChange={(e) => setConsent(e.target.checked)} sx={{ pt: 0.25 }} />}
        label={<Typography variant="body2">{t('checkout.consent')}</Typography>}
      />

      <Box sx={isMobile ? {
        position: 'fixed', left: 0, right: 0, bottom: 0, zIndex: 10, p: 2,
        bgcolor: 'background.paper', borderTop: '1px solid', borderColor: 'divider',
      } : { mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
        <Button variant="contained" size="large" fullWidth={isMobile} disabled={!canPay} onClick={onPay}
          startIcon={busy ? <CircularProgress size={16} color="inherit" /> : <Lock size={16} aria-hidden />}
          sx={{ py: 1.25, px: 4, fontSize: 16 }}>
          {payLabel}
        </Button>
      </Box>
    </Box>
  )
}

/** REQ-BIL-001.22: saved cards (display fields from Razorpay only) and
 * "+ Use another card". An expired card cannot be selected. */
function SavedCards({ methods, selected, onSelect, onRemove }: {
  methods: PaymentMethod[]; selected: number | 'new' | null; onSelect: (c: number | 'new') => void; onRemove: (id: number) => void
}) {
  const { t } = useTranslation()
  const [menu, setMenu] = useState<{ anchor: HTMLElement; method: PaymentMethod } | null>(null)
  const [confirm, setConfirm] = useState<PaymentMethod | null>(null)
  if (methods.length === 0) return null
  const brandOf = (network?: string): PaymentBrand | null => {
    const n = (network ?? '').toLowerCase()
    if (n.includes('visa')) return 'visa'
    if (n.includes('master')) return 'mastercard'
    if (n.includes('rupay')) return 'rupay'
    if (n.includes('amex') || n.includes('american')) return 'amex'
    return null
  }
  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 1 }} id="saved-cards-label">{t('checkout.savedCards.title')}</Typography>
      <Box role="radiogroup" aria-labelledby="saved-cards-label" sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {methods.map((m) => {
          const brand = brandOf(m.network)
          const isSelected = selected === m.id
          const expiry = m.expiryMonth && m.expiryYear ? `${String(m.expiryMonth).padStart(2, '0')}/${String(m.expiryYear).slice(-2)}` : null
          return (
            <Paper key={m.id} variant="outlined" sx={{ display: 'flex', alignItems: 'center', gap: 1.5, px: 1.5, py: 1, borderRadius: 2, borderColor: isSelected ? 'primary.main' : 'divider', opacity: m.expired ? 0.6 : 1 }}>
              <Box role="radio" aria-checked={isSelected} aria-disabled={m.expired || undefined} tabIndex={m.expired ? -1 : 0}
                aria-label={t('checkout.savedCards.cardLabel', { network: m.network ?? t('checkout.option.card.title'), last4: m.last4 ?? '' })}
                onClick={() => !m.expired && onSelect(m.id)}
                onKeyDown={(e) => { if (!m.expired && (e.key === ' ' || e.key === 'Enter')) { e.preventDefault(); onSelect(m.id) } }}
                sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flex: 1, cursor: m.expired ? 'not-allowed' : 'pointer', borderRadius: 1, '&:focus-visible': { outline: '2px solid', outlineColor: 'primary.main' } }}>
                <Box aria-hidden sx={{ width: 16, height: 16, borderRadius: '50%', border: '2px solid', borderColor: isSelected ? 'primary.main' : 'text.disabled', display: 'grid', placeItems: 'center' }}>
                  {isSelected && <Box sx={{ width: 7, height: 7, borderRadius: '50%', bgcolor: 'primary.main' }} />}
                </Box>
                {brand ? <PaymentLogo brand={brand} height={18} decorative /> : <CreditCard size={18} aria-hidden />}
                <Typography variant="body2" sx={{ fontFamily: 'monospace' }} aria-hidden>•••• •••• •••• {m.last4}</Typography>
                {expiry && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('checkout.savedCards.expires', { date: expiry })}</Typography>}
                {m.expired && <Chip size="small" color="warning" label={t('checkout.savedCards.expired')} />}
                {m.isDefault && !m.expired && <Chip size="small" variant="outlined" label={t('checkout.savedCards.default')} />}
              </Box>
              <IconButton size="small" aria-label={t('checkout.savedCards.menu', { last4: m.last4 ?? '' })} aria-haspopup="menu"
                onClick={(e) => setMenu({ anchor: e.currentTarget, method: m })}>
                <MoreVertical size={16} />
              </IconButton>
            </Paper>
          )
        })}
        <Button startIcon={<Plus size={16} aria-hidden />} onClick={() => onSelect('new')} sx={{ alignSelf: 'flex-start' }}
          aria-pressed={selected === 'new'}>
          {t('checkout.savedCards.useAnother')}
        </Button>
      </Box>
      <Menu anchorEl={menu?.anchor} open={Boolean(menu)} onClose={() => setMenu(null)}>
        <MenuItem onClick={() => { setConfirm(menu!.method); setMenu(null) }}>{t('checkout.savedCards.remove')}</MenuItem>
      </Menu>
      <Dialog open={Boolean(confirm)} onClose={() => setConfirm(null)} aria-labelledby="remove-card-title">
        <DialogTitle id="remove-card-title">{t('checkout.savedCards.removeConfirm.title')}</DialogTitle>
        <DialogContent><DialogContentText>{t('checkout.savedCards.removeConfirm.body', { last4: confirm?.last4 ?? '' })}</DialogContentText></DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirm(null)}>{t('common.cancel')}</Button>
          <Button color="error" variant="contained" onClick={() => { onRemove(confirm!.id); setConfirm(null) }}>{t('checkout.savedCards.remove')}</Button>
        </DialogActions>
      </Dialog>
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
    <Box sx={{ mt: 2 }}>
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

      {result.kind === 'orders' && (
        <>
          <Box sx={{ color: 'primary.main' }}><ClipboardCheck size={56} aria-hidden /></Box>
          {heading(t('checkout.result.orderSubmitted.title'))}
          <Typography sx={{ color: 'text.secondary', mt: 1 }}>
            {t('checkout.result.orderSubmitted.reference', { refs: result.orderIds.map((id) => `#${id}`).join(', ') })}
          </Typography>
          <Typography sx={{ color: 'text.secondary', mb: 2 }}>{t('checkout.result.orderSubmitted.body')}</Typography>
          <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center', flexWrap: 'wrap' }}>
            <Button variant="contained" component={RouterLink} to="/organization/orders">{t('checkout.result.orderSubmitted.viewOrders')}</Button>
            <Button component={RouterLink} to="/products">{t('checkout.result.orderSubmitted.browse')}</Button>
          </Box>
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
