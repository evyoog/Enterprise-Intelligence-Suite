import { useCallback, useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useNavigate, useSearchParams } from 'react-router-dom'
import {
  Alert, Box, Button, Chip, CircularProgress, Collapse, Dialog, DialogActions, DialogContent, DialogContentText,
  DialogTitle, Divider, MenuItem, Paper, Skeleton, Snackbar, TextField, Typography, useMediaQuery, useTheme,
} from '@mui/material'
import { alpha } from '@mui/material/styles'
import { Lock, Package, ShoppingCart, Trash2 } from 'lucide-react'
import { cartApi, type Cart, type CartIssue, type CartItem } from '../api/cartApi'
import { ApiError, resolveAssetUrl } from '../api/client'
import { productsApi } from '../api/productsApi'
import { useCart } from '../components/cart/CartContext'
import { PaymentLogos } from '../components/payments/PaymentLogos'
import { formatMoney } from '../utils/money'

interface Toast { message: string; severity: 'success' | 'error' | 'info'; undo?: () => void }

/** The period a new subscription on this plan covers, starting today. */
function term(billingPeriod: string | undefined, locale: string) {
  if (billingPeriod !== 'MONTHLY' && billingPeriod !== 'YEARLY') return null
  const start = new Date()
  const end = new Date(start)
  if (billingPeriod === 'MONTHLY') end.setMonth(end.getMonth() + 1)
  else end.setFullYear(end.getFullYear() + 1)
  end.setDate(end.getDate() - 1)
  const fmt = (d: Date) => d.toLocaleDateString(locale, { day: 'numeric', month: 'short', year: 'numeric' })
  return `${fmt(start)} – ${fmt(end)}`
}

/**
 * "/cart" — C59, REQ-MKT-003 (docs/05-ui/screen-requirements/cart.md).
 * Every Buy on a paid plan lands here with `?add=<productId>[&plan=<planId>]`;
 * the item is added once and the parameter removed. A product with no paid
 * plan keeps its current free-plan behaviour (the subscribe checkout).
 */
export function CartPage() {
  const { t, i18n } = useTranslation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'))
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const { setCount } = useCart()

  const [cart, setCart] = useState<Cart | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [issues, setIssues] = useState<CartIssue[]>([])
  const [busy, setBusy] = useState(false)
  const [toast, setToast] = useState<Toast | null>(null)
  const [confirmClear, setConfirmClear] = useState(false)
  const [summaryOpen, setSummaryOpen] = useState(false)
  const addHandled = useRef<string | null>(null)
  const itemRefs = useRef<Record<number, HTMLLIElement | null>>({})

  const apply = useCallback((next: Cart) => {
    setCart(next)
    setCount(next.itemCount)
  }, [setCount])

  const money = (v: number, currency = cart?.currency ?? 'USD') => formatMoney(v, currency, i18n.language)
  const errorText = (e: unknown) => (e instanceof ApiError ? e.message : t('cart.error'))

  const load = useCallback(() => {
    cartApi.get().then(apply).catch((e) => setLoadError(e instanceof ApiError ? e.message : t('cart.error')))
  }, [apply, t])

  // Buy → ?add=<productId>[&plan=<planId>]: added once, then the URL is cleaned.
  const addParam = params.get('add')
  const planParam = params.get('plan')
  useEffect(() => {
    if (!addParam) {
      load()
      return
    }
    const key = `${addParam}:${planParam ?? ''}`
    if (addHandled.current === key) return
    addHandled.current = key
    const productId = Number(addParam)
    productsApi.get(productId).then(async (product) => {
      const paid = product.plans.filter((p) => p.price > 0)
      const plan = paid.find((p) => String(p.id) === planParam) ?? paid.find((p) => p.billingPeriod === 'MONTHLY') ?? paid[0]
      if (!plan) {
        // No paid plan: the free-plan behaviour is unchanged (REQ-MKT-003.1).
        navigate(`/checkout?productId=${productId}`, { replace: true })
        return
      }
      const next = await cartApi.add(productId, plan.id)
      apply(next)
      setToast({ message: t('cart.added', { product: product.name, plan: plan.name }), severity: 'success' })
      setParams({}, { replace: true })
    }).catch((e) => {
      setToast({ message: errorText(e), severity: 'error' })
      setParams({}, { replace: true })
      load()
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps -- runs once per Buy parameter
  }, [addParam, planParam])

  const issuesFor = (itemId: number) => issues.filter((i) => i.itemId === itemId)

  const remove = async (item: CartItem) => {
    try {
      const next = await cartApi.remove(item.id)
      apply(next)
      setIssues((all) => all.filter((i) => i.itemId !== item.id))
      setToast({
        message: t('cart.removed', { product: item.productName }), severity: 'info',
        undo: () => {
          cartApi.add(item.productId, item.planId).then(apply).catch((e) => setToast({ message: errorText(e), severity: 'error' }))
          setToast(null)
        },
      })
    } catch (e) {
      setToast({ message: errorText(e), severity: 'error' })
    }
  }

  // Optimistic plan change with rollback (cart.md "Change plan").
  const changePlan = async (item: CartItem, planId: number) => {
    if (!cart) return
    const previous = cart
    const plan = item.plans.find((p) => p.id === planId)
    if (plan) {
      const items = cart.items.map((i) => i.id === item.id
        ? { ...i, planId, planName: plan.name, billingPeriod: plan.billingPeriod, unitPrice: plan.price, unitPriceAtAdd: plan.price, amount: plan.price }
        : i)
      const subtotal = items.reduce((s, i) => s + i.amount, 0)
      setCart({ ...cart, items, subtotal, total: subtotal })
    }
    try {
      apply(await cartApi.changePlan(item.id, planId))
      setIssues((all) => all.filter((i) => i.itemId !== item.id))
    } catch (e) {
      setCart(previous)
      setToast({ message: errorText(e), severity: 'error' })
    }
  }

  const confirmPrice = async (item: CartItem) => {
    try {
      apply(await cartApi.confirmPrice(item.id))
      setIssues((all) => all.filter((i) => !(i.itemId === item.id && i.code === 'PRICE_CHANGED')))
    } catch (e) {
      setToast({ message: errorText(e), severity: 'error' })
    }
  }

  const clear = async () => {
    setConfirmClear(false)
    try {
      await cartApi.clear()
      setIssues([])
      apply({ ...(cart as Cart), items: [], itemCount: 0, subtotal: 0, total: 0 })
    } catch (e) {
      setToast({ message: errorText(e), severity: 'error' })
    }
  }

  const showIssues = (found: CartIssue[]) => {
    setIssues(found)
    const first = found[0]
    if (first) {
      const el = itemRefs.current[first.itemId]
      el?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
      el?.querySelector<HTMLElement>('[role="alert"]')?.focus()
    }
  }

  const proceed = async () => {
    if (busy) return
    setBusy(true)
    try {
      const validation = await cartApi.validate()
      if (!validation.valid) {
        showIssues(validation.issues)
        return
      }
      const result = await cartApi.checkout()
      setCount(0)
      if (result.kind === 'INVOICE') navigate(`/checkout?invoiceId=${result.invoiceId}&from=cart`)
      else navigate(`/checkout?orders=${result.orderIds.join(',')}&step=complete`)
    } catch (e) {
      const detailIssues = e instanceof ApiError ? (e.detail?.issues as CartIssue[] | undefined) : undefined
      if (detailIssues?.length) showIssues(detailIssues)
      else setToast({ message: errorText(e), severity: 'error' })
    } finally {
      setBusy(false)
    }
  }

  const issueText = (issue: CartIssue, item: CartItem) => {
    switch (issue.code) {
      case 'NOT_AVAILABLE': return issue.reason === 'PLAN' ? t('cart.issue.planUnavailable') : t('cart.issue.productUnavailable')
      case 'PRICE_CHANGED': return t('cart.issue.priceChanged', { old: money(issue.oldPrice ?? 0, item.currency), new: money(issue.newPrice ?? 0, item.currency) })
      case 'ALREADY_SUBSCRIBED': return t('cart.issue.alreadySubscribed', { product: item.productName })
      case 'MISSING_DEPENDENCY': return t('cart.issue.missingDependency', { product: item.productName, required: issue.requiredProductName })
      case 'CURRENCY_MISMATCH': return t('cart.issue.currencyMismatch')
      case 'NOT_ELIGIBLE': return t('cart.issue.notEligible', { product: item.productName })
    }
  }

  if (loadError) {
    return <Alert severity="error" action={<Button onClick={() => { setLoadError(null); load() }}>{t('cart.retry')}</Button>}>{loadError}</Alert>
  }
  if (!cart) {
    return (
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '65fr 35fr' } }}>
        <Box sx={{ display: 'grid', gap: 1.5 }}>{[0, 1].map((i) => <Skeleton key={i} variant="rounded" height={132} />)}</Box>
        <Skeleton variant="rounded" height={280} />
      </Box>
    )
  }

  const organization = cart.continueAs === 'ORGANIZATION_MEMBER'
  const primaryLabel = organization ? t('cart.submitForApproval') : t('cart.proceed')

  if (cart.items.length === 0) {
    return (
      <>
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Box sx={{ display: 'inline-grid', placeItems: 'center', width: 88, height: 88, borderRadius: '50%', bgcolor: alpha(theme.palette.primary.main, 0.1), color: 'primary.main', mb: 2 }}>
            <ShoppingCart size={40} aria-hidden />
          </Box>
          <Typography component="h1" variant="h5" sx={{ fontWeight: 700 }}>{t('cart.empty.title')}</Typography>
          <Typography sx={{ color: 'text.secondary', mt: 0.5, mb: 3 }}>{t('cart.empty.body')}</Typography>
          <Button variant="contained" component={RouterLink} to="/products">{t('cart.empty.browse')}</Button>
        </Box>
        <CartToast toast={toast} onClose={() => setToast(null)} />
      </>
    )
  }

  const summary = (
    <Paper variant="outlined" component="section" aria-labelledby="cart-summary-title"
      sx={{ p: 3, borderRadius: 3, position: { md: 'sticky' }, top: { md: 88 } }}>
      <Typography id="cart-summary-title" component="h2" variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
        {t('cart.summary.title')} <Box component="span" sx={{ color: 'text.secondary', fontWeight: 500 }}>({t('cart.summary.items', { count: cart.itemCount })})</Box>
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Row label={t('cart.summary.subtotal')} value={money(cart.subtotal)} />
        {cart.taxLines.length > 0
          ? cart.taxLines.map((tl) => <Row key={tl.name} label={tl.ratePercent != null ? `${tl.name} (${tl.ratePercent} %)` : tl.name} value={money(tl.amount)} />)
          : <Typography variant="body2" sx={{ color: 'text.secondary' }}>{cart.taxCalculatedAtPayment ? t('cart.summary.taxAtPayment') : t('cart.summary.noTax')}</Typography>}
        <Divider sx={{ my: 1 }} />
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
          <Typography sx={{ fontWeight: 700 }}>{t('cart.summary.total')}</Typography>
          <Typography variant="h5" component="p" sx={{ fontWeight: 800, fontVariantNumeric: 'tabular-nums' }}>{money(cart.total)}</Typography>
        </Box>
      </Box>
      {!isMobile && (
        <Button variant="contained" size="large" fullWidth sx={{ mt: 2.5, py: 1.4 }} disabled={busy} onClick={proceed}
          startIcon={busy ? <CircularProgress size={16} color="inherit" /> : undefined}>
          {primaryLabel}
        </Button>
      )}
      {organization && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1 }}>{t('cart.approvalHelp')}</Typography>}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2, color: 'text.secondary' }}>
        <Lock size={14} aria-hidden />
        <Typography variant="caption">{t('cart.summary.trust')}</Typography>
      </Box>
      <Box sx={{ mt: 1 }}>
        <PaymentLogos brands={['visa', 'mastercard', 'rupay', 'amex', 'upi']} height={18} dark={theme.palette.mode === 'dark'} label={t('cart.summary.acceptedMethods')} />
      </Box>
    </Paper>
  )

  return (
    <Box sx={{ pb: isMobile ? 12 : 0 }}>
      <Typography component="h1" variant="h5" sx={{ fontWeight: 700, mb: 2 }}>
        {t('cart.title')} <Box component="span" sx={{ color: 'text.secondary', fontWeight: 500 }}>({cart.itemCount})</Box>
      </Typography>
      <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', md: '65fr 35fr' }, alignItems: 'start' }}>
        <Box>
          <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {cart.items.map((item) => {
              const itemIssues = issuesFor(item.id)
              const period = term(item.billingPeriod, i18n.language)
              return (
                <Paper component="li" key={item.id} variant="outlined" ref={(el: HTMLLIElement | null) => { itemRefs.current[item.id] = el }}
                  sx={{ p: 2, borderRadius: 2.5, borderColor: itemIssues.length ? 'warning.main' : 'divider' }}>
                  <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
                    <Box sx={{ width: 64, height: 64, borderRadius: 2, flexShrink: 0, overflow: 'hidden', display: 'grid', placeItems: 'center', bgcolor: alpha(theme.palette.primary.main, 0.08), color: 'primary.main' }}>
                      {item.imageUrl
                        ? <Box component="img" src={resolveAssetUrl(item.imageUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        : <Package size={28} aria-hidden />}
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 0 }}>
                      <Typography component="h2" sx={{ fontWeight: 700, fontSize: 16 }}>{item.productName}</Typography>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{item.planName}</Typography>
                      <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap', mt: 0.75 }}>
                        {item.billingPeriod && <Chip size="small" label={t(`checkout.period.${item.billingPeriod}`, { defaultValue: item.billingPeriod })} />}
                        {period && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{period}</Typography>}
                      </Box>
                      {item.plans.length > 1 && (
                        <TextField select size="small" label={t('cart.changePlan')} value={item.planId}
                          onChange={(e) => changePlan(item, Number(e.target.value))}
                          sx={{ mt: 1.5, minWidth: 220 }}>
                          {item.plans.map((p) => (
                            <MenuItem key={p.id} value={p.id}>{p.name} · {money(p.price, p.currency)}</MenuItem>
                          ))}
                        </TextField>
                      )}
                    </Box>
                    <Box sx={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1, alignSelf: 'stretch' }}>
                      <Typography sx={{ fontWeight: 800, fontSize: 20, fontVariantNumeric: 'tabular-nums' }}>{money(item.amount, item.currency)}</Typography>
                      <Box sx={{ flex: 1 }} />
                      <Button size="small" color="inherit" startIcon={<Trash2 size={15} aria-hidden />} onClick={() => remove(item)}
                        aria-label={t('cart.removeItem', { product: item.productName })}>
                        {t('cart.remove')}
                      </Button>
                    </Box>
                  </Box>
                  {itemIssues.map((issue, n) => (
                    <Alert key={n} role="alert" tabIndex={-1} severity={issue.code === 'PRICE_CHANGED' ? 'warning' : 'error'} sx={{ mt: 1.5 }}
                      action={
                        issue.code === 'PRICE_CHANGED'
                          ? <Button color="inherit" size="small" onClick={() => confirmPrice(item)}>{t('cart.confirmPrice')}</Button>
                          : issue.code === 'MISSING_DEPENDENCY' && issue.requiredProductId
                            ? <Button color="inherit" size="small" component={RouterLink} to={`/products/${issue.requiredProductId}`}>{t('cart.addRequired', { required: issue.requiredProductName })}</Button>
                            : <Button color="inherit" size="small" onClick={() => remove(item)}>{t('cart.remove')}</Button>
                      }>
                      {issueText(issue, item)}
                    </Alert>
                  ))}
                </Paper>
              )
            })}
          </Box>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
            <Button component={RouterLink} to="/products">{t('cart.continueBrowsing')}</Button>
            <Button color="inherit" onClick={() => setConfirmClear(true)}>{t('cart.clear')}</Button>
          </Box>
        </Box>

        {!isMobile && summary}
      </Box>

      {isMobile && (
        <Paper elevation={8} square sx={{ position: 'fixed', left: 0, right: 0, bottom: 0, zIndex: 10, p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
          <Collapse in={summaryOpen}><Box sx={{ mb: 2, maxHeight: '50vh', overflow: 'auto' }}>{summary}</Box></Collapse>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ flex: 1 }}>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('cart.summary.total')}</Typography>
              <Typography sx={{ fontWeight: 800 }}>{money(cart.total)}</Typography>
              <Button size="small" sx={{ p: 0, minWidth: 0 }} onClick={() => setSummaryOpen((o) => !o)} aria-expanded={summaryOpen}>
                {t('cart.viewSummary')}
              </Button>
            </Box>
            <Button variant="contained" disabled={busy} onClick={proceed}>{primaryLabel}</Button>
          </Box>
        </Paper>
      )}

      <Dialog open={confirmClear} onClose={() => setConfirmClear(false)} aria-labelledby="clear-cart-title">
        <DialogTitle id="clear-cart-title">{t('cart.clearConfirm.title')}</DialogTitle>
        <DialogContent><DialogContentText>{t('cart.clearConfirm.body')}</DialogContentText></DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmClear(false)}>{t('common.cancel')}</Button>
          <Button color="error" variant="contained" onClick={clear}>{t('cart.clear')}</Button>
        </DialogActions>
      </Dialog>
      <CartToast toast={toast} onClose={() => setToast(null)} />
    </Box>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{label}</Typography>
      <Typography variant="body2" sx={{ fontVariantNumeric: 'tabular-nums' }}>{value}</Typography>
    </Box>
  )
}

/** Undo toast stays ~5 s (cart.md); MUI pauses the timer while hovered or focused. */
function CartToast({ toast, onClose }: { toast: Toast | null; onClose: () => void }) {
  const { t } = useTranslation()
  return (
    <Snackbar open={Boolean(toast)} autoHideDuration={toast?.undo ? 5000 : 4000} onClose={(_, reason) => { if (reason !== 'clickaway') onClose() }}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
      {toast ? (
        <Alert severity={toast.severity} variant="filled" onClose={onClose}
          action={toast.undo ? <Button color="inherit" size="small" onClick={toast.undo}>{t('cart.undo')}</Button> : undefined}>
          {toast.message}
        </Alert>
      ) : undefined}
    </Snackbar>
  )
}
