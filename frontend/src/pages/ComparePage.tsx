import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Autocomplete, Box, Button, Container, IconButton, Link, MenuItem, Paper, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Tooltip, Typography,
} from '@mui/material'
import { Check, GitCompare, ShoppingCart, X } from 'lucide-react'
import { Link as RouterLink, useLocation, useSearchParams } from 'react-router-dom'
import { ApiError, resolveAssetUrl } from '../api/client'
import {
  compareApi, formatCompareItems, MAX_COMPARE_ITEMS, MAX_SEATS, MIN_COMPARE_ITEMS, MIN_SEATS, type CompareItem, type CompareRef, type Comparison,
} from '../api/compareApi'
import { productsApi, type Product } from '../api/productsApi'
import { canAddToCart, useAddToCart } from '../components/cart/useAddToCart'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { PageHeader } from '../components/layout/PageHeader'
import { useInAppShell } from '../components/layout/appShellContext'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { StatusBadge } from '../components/ui/StatusBadge'
import { formatMoney } from '../utils/money'
import { formatPlanPrice } from '../utils/productPricing'

const ITEM_PATTERN = /^(\d+)(?::(\d+))?$/

/** `1,2:5` -> [{1}, {2, plan 5}]. Pieces that are not a product or a product:plan pair are ignored. */
function parseItems(raw: string | null): CompareRef[] {
  const refs: CompareRef[] = []
  for (const piece of (raw ?? '').split(',')) {
    const match = ITEM_PATTERN.exec(piece.trim())
    if (!match || Number(match[1]) < 1) continue
    const planId = match[2] ? Number(match[2]) : 0
    refs.push(planId > 0 ? { productId: Number(match[1]), planId } : { productId: Number(match[1]) })
  }
  return refs
}

/** The seats the text holds, or null when it is not a whole number in range. */
function parseSeats(text: string): number | null {
  if (!/^\d+$/.test(text.trim())) return null
  const n = Number(text.trim())
  return n >= MIN_SEATS && n <= MAX_SEATS ? n : null
}

/** The first way of adding a product that is not already chosen: the product on its own, else one of its plans. */
function nextRef(product: Product, items: CompareRef[]): CompareRef | null {
  if (!items.some((i) => i.productId === product.id && i.planId == null)) return { productId: product.id }
  const free = product.plans.find((p) => !items.some((i) => i.productId === product.id && i.planId === p.id))
  return free ? { productId: product.id, planId: free.id } : null
}

const money = (amount: number, currency: string) => formatMoney(Math.round(amount * 100), currency)

type Result = { key: string; data?: Comparison; error?: string }

function initials(name: string) {
  const words = name.trim().split(/\s+/)
  return (words.length >= 2 ? words[0][0] + words[1][0] : name.slice(0, 2)).toUpperCase()
}

const visuallyHidden = {
  position: 'absolute', width: 1, height: 1, p: 0, m: -1, overflow: 'hidden', clip: 'rect(0 0 0 0)', whiteSpace: 'nowrap', border: 0,
} as const

/**
 * "/compare?items=1,2:5&seats=3" — C102, REQ-MKT-005 (TR-2 A): 2 to 4 products, or products with one chosen plan, side by side with a cost
 * estimate of price x seats + tax. The URL is the state, so a comparison can be shared and bookmarked. Public; the tax line needs a signed-in
 * customer with billing details. Each item shows in its own currency; nothing is converted.
 */
export function ComparePage() {
  const { t } = useTranslation()
  const inShell = useInAppShell()
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const { addToCart, busyId, notice } = useAddToCart()

  const itemsRaw = searchParams.get('items')
  const seatsRaw = searchParams.get('seats')
  const items = useMemo(() => parseItems(itemsRaw), [itemsRaw])
  const itemsText = formatCompareItems(items)
  const seats = seatsRaw === null ? 1 : parseSeats(seatsRaw)
  const [seatsDraft, setSeatsDraft] = useState<string | null>(null)
  const seatsText = seatsDraft ?? seatsRaw ?? '1'
  const seatsInvalid = parseSeats(seatsText) === null

  const [products, setProducts] = useState<Product[]>([])
  const [productsLoading, setProductsLoading] = useState(true)
  const [pickerText, setPickerText] = useState('')
  const [reload, setReload] = useState(0)
  const [result, setResult] = useState<Result | null>(null)

  useEffect(() => {
    let cancelled = false
    productsApi.list()
      .then((list) => { if (!cancelled) setProducts(list) })
      .catch(() => { /* the picker stays empty; the comparison itself still works */ })
      .finally(() => { if (!cancelled) setProductsLoading(false) })
    return () => { cancelled = true }
  }, [])

  const canCompare = items.length >= MIN_COMPARE_ITEMS && seats !== null
  const key = canCompare ? `${itemsText}|${seats}|${reload}` : null
  useEffect(() => {
    if (key === null || seats === null) return
    let cancelled = false
    compareApi.compare(items, seats)
      .then((data) => { if (!cancelled) setResult({ key, data }) })
      .catch((e) => { if (!cancelled) setResult({ key, error: e instanceof ApiError ? e.message : t('compare.error.generic') }) })
    return () => { cancelled = true }
  }, [key, items, seats, t])
  const current = key !== null && result?.key === key ? result : null
  const loading = key !== null && current === null

  const productById = useMemo(() => {
    const map = new Map<number, Product>()
    for (const c of current?.data?.items ?? []) map.set(c.product.id, c.product)
    for (const p of products) map.set(p.id, p)
    return map
  }, [products, current])

  const update = (next: CompareRef[], replace = false) => {
    const params = new URLSearchParams(searchParams)
    if (next.length > 0) params.set('items', formatCompareItems(next)); else params.delete('items')
    setSearchParams(params, { replace })
  }
  const setPlan = (index: number, planId: number | null) =>
    update(items.map((item, i) => (i === index ? (planId == null ? { productId: item.productId } : { productId: item.productId, planId }) : item)))
  const removeAt = (index: number) => update(items.filter((_, i) => i !== index))
  const changeSeats = (text: string) => {
    setSeatsDraft(text)
    const n = parseSeats(text)
    if (n === null) return
    const params = new URLSearchParams(searchParams)
    if (n === 1) params.delete('seats'); else params.set('seats', String(n))
    setSearchParams(params, { replace: true })
  }

  const atLimit = items.length >= MAX_COMPARE_ITEMS
  const nameOf = (ref: CompareRef) => productById.get(ref.productId)?.name ?? t('compare.chooser.unknownProduct', { id: ref.productId })
  const signInPath = `/login?returnTo=${encodeURIComponent(location.pathname + location.search)}`

  const statusText = loading ? t('compare.loading') : current?.data ? t('compare.live', { count: current.data.items.length, seats: current.data.seats }) : ''

  const chooser = (
    <Box component="section" aria-labelledby="compare-chooser" sx={{ mb: 3 }}>
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-start', mb: 2 }}>
        <Autocomplete
          sx={{ flex: '1 1 260px', minWidth: 0, maxWidth: 420 }}
          options={products}
          value={null}
          inputValue={pickerText}
          onInputChange={(_, v, reason) => { if (reason !== 'reset') setPickerText(v) }}
          onChange={(_, product) => {
            setPickerText('')
            const ref = product ? nextRef(product, items) : null
            if (ref) update([...items, ref])
          }}
          getOptionLabel={(p) => p.name}
          getOptionDisabled={(p) => nextRef(p, items) === null}
          disabled={atLimit}
          loading={productsLoading}
          blurOnSelect
          clearOnBlur
          noOptionsText={t('compare.add.noOptions')}
          renderInput={(params) => (
            <TextField {...params} size="small" label={t('compare.add.label')} placeholder={t('compare.add.placeholder')}
              helperText={atLimit ? t('compare.add.limit') : undefined} />
          )}
        />
        <TextField
          size="small" type="number" label={t('compare.seats.label')} value={seatsText}
          onChange={(e) => changeSeats(e.target.value)} onBlur={() => setSeatsDraft(null)}
          error={seatsInvalid} helperText={seatsInvalid ? t('compare.seats.error') : t('compare.seats.help')}
          slotProps={{ htmlInput: { min: MIN_SEATS, max: MAX_SEATS, step: 1, inputMode: 'numeric' } }}
          sx={{ width: 200 }}
        />
      </Box>
      {items.length > 0 && (
        <>
          <Typography id="compare-chooser" component="h2" variant="subtitle2" sx={{ mb: 1, color: 'text.secondary' }}>{t('compare.chooser.heading')}</Typography>
          <Box component="ul" sx={{ listStyle: 'none', m: 0, p: 0, display: 'grid', gap: 1.5, gridTemplateColumns: 'repeat(auto-fill, minmax(min(100%, 240px), 1fr))' }}>
            {items.map((ref, index) => {
              const product = productById.get(ref.productId)
              const name = nameOf(ref)
              return (
                <Paper component="li" key={`${itemsText}-${index}`} variant="outlined" sx={{ p: 1.5, borderRadius: 3, display: 'flex', flexDirection: 'column', gap: 1, minWidth: 0 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography sx={{ fontWeight: 700, flex: 1, minWidth: 0, overflowWrap: 'anywhere' }}>{name}</Typography>
                    <IconButton size="small" aria-label={t('compare.chooser.remove', { name })} onClick={() => removeAt(index)}>
                      <X size={16} aria-hidden />
                    </IconButton>
                  </Box>
                  <TextField select size="small" fullWidth label={t('compare.chooser.plan', { name })}
                    value={ref.planId ?? ''} disabled={!product || product.plans.length === 0}
                    onChange={(e) => setPlan(index, e.target.value === '' ? null : Number(e.target.value))}>
                    <MenuItem value="">{t('compare.chooser.entryPlan')}</MenuItem>
                    {(product?.plans ?? []).map((plan) => (
                      <MenuItem key={plan.id} value={plan.id}>{plan.name} — {formatPlanPrice(plan.price, plan.currency, plan.billingPeriod)}</MenuItem>
                    ))}
                  </TextField>
                </Paper>
              )
            })}
          </Box>
        </>
      )}
    </Box>
  )

  const taxCell = (c: CompareItem) => {
    switch (c.tax.status) {
      case 'ESTIMATED':
        return c.tax.amount == null ? t('compare.table.none') : money(c.tax.amount, c.currency)
      case 'NOT_SIGNED_IN':
        return <Link component={RouterLink} to={signInPath}>{t('compare.tax.notSignedIn')}</Link>
      case 'NEEDS_BILLING_DETAILS':
        return <Link component={RouterLink} to="/billing">{t('compare.tax.needsBilling')}</Link>
      default:
        return t('compare.tax.atCheckout')
    }
  }

  const totalCell = (c: CompareItem) => (c.tax.status === 'ESTIMATED' && c.tax.total != null
    ? <Typography component="span" sx={{ fontWeight: 700 }}>{money(c.tax.total, c.currency)}</Typography>
    : (
      <>
        <Typography component="span" sx={{ fontWeight: 700 }}>{money(c.subtotal, c.currency)}</Typography>
        <Typography component="span" variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>{t('compare.table.beforeTax')}</Typography>
      </>
    ))

  const renderTable = (data: Comparison) => {
    const cols = data.items
    const features: string[] = []
    const seen = new Set<string>()
    for (const c of cols) for (const f of c.includedFeatures) if (!seen.has(f.toLowerCase())) { seen.add(f.toLowerCase()); features.push(f) }
    const has = (c: CompareItem, f: string) => c.includedFeatures.some((x) => x.toLowerCase() === f.toLowerCase())

    const rowHead = { position: 'sticky', left: 0, zIndex: 1, bgcolor: 'background.paper', width: { xs: 112, sm: 168 }, minWidth: { xs: 112, sm: 168 }, fontWeight: 600, verticalAlign: 'top' } as const
    const cellSx = { minWidth: 200, verticalAlign: 'top' } as const
    const row = (label: string, render: (c: CompareItem) => ReactNode) => (
      <TableRow key={label}>
        <TableCell component="th" scope="row" sx={rowHead}>{label}</TableCell>
        {cols.map((c, i) => <TableCell key={i} sx={cellSx}>{render(c)}</TableCell>)}
      </TableRow>
    )

    return (
      <TableContainer component={Paper} variant="outlined" role="region" tabIndex={0} aria-label={t('compare.table.caption')} sx={{ borderRadius: 3, overflowX: 'auto' }}>
        <Table sx={{ minWidth: 'max-content' }}>
          <Box component="caption" sx={visuallyHidden}>{t('compare.table.caption')}</Box>
          <TableHead>
            <TableRow>
              <TableCell component="th" scope="col" sx={{ ...rowHead, zIndex: 2 }}><Box component="span" sx={visuallyHidden}>{t('compare.table.feature')}</Box></TableCell>
              {cols.map((c, i) => (
                <TableCell key={i} component="th" scope="col" sx={cellSx}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25 }}>
                    <Box sx={{ width: 40, height: 40, flexShrink: 0, borderRadius: 2, overflow: 'hidden', display: 'grid', placeItems: 'center', bgcolor: 'action.hover', color: 'text.secondary', fontWeight: 800, fontSize: 14 }}>
                      {c.product.imageUrl
                        ? <Box component="img" src={resolveAssetUrl(c.product.imageUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        : initials(c.product.name)}
                    </Box>
                    <Link component={RouterLink} to={`/products/${c.product.id}`} color="inherit" underline="hover" sx={{ fontWeight: 700, fontSize: 16, overflowWrap: 'anywhere' }}>
                      {c.product.name}
                    </Link>
                  </Box>
                  {c.trialDays != null && c.trialDays > 0 && (
                    <Box sx={{ mt: 0.75 }}><StatusBadge tone="success" label={t('compare.trial', { count: c.trialDays })} /></Box>
                  )}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {row(t('compare.table.category'), (c) => c.product.category || t('compare.table.none'))}
            {row(t('compare.table.plan'), (c) => c.plan?.name ?? t('compare.table.noPlan'))}
            {row(t('compare.table.billingPeriod'), (c) => t(`compare.period.${c.billingPeriod ?? 'ONE_TIME'}`))}
            {row(t('compare.table.unitPrice'), (c) => money(c.unitPrice, c.currency))}
            {row(t('compare.table.seats'), (c) => c.seats)}
            {row(t('compare.table.subtotal'), (c) => money(c.subtotal, c.currency))}
            {row(t('compare.table.tax'), taxCell)}
            {row(t('compare.table.total'), totalCell)}
            <TableRow>
              <TableCell component="th" scope="colgroup" colSpan={cols.length + 1} sx={{ bgcolor: 'action.hover', fontWeight: 700 }}>{t('compare.table.features')}</TableCell>
            </TableRow>
            {features.length === 0 && (
              <TableRow><TableCell colSpan={cols.length + 1} sx={{ color: 'text.secondary' }}>{t('compare.features.none')}</TableCell></TableRow>
            )}
            {features.map((f) => (
              <TableRow key={f}>
                  <TableCell component="th" scope="row" sx={{ ...rowHead, fontWeight: 500, overflowWrap: 'anywhere' }}>{f}</TableCell>
                  {cols.map((c, i) => (
                    <TableCell key={i} sx={cellSx}>
                      {has(c, f)
                        ? <><Check size={18} aria-hidden /><Box component="span" sx={visuallyHidden}>{t('compare.features.included')}</Box></>
                        : <><Box component="span" aria-hidden sx={{ color: 'text.disabled' }}>{t('compare.table.none')}</Box><Box component="span" sx={visuallyHidden}>{t('compare.features.notIncluded')}</Box></>}
                    </TableCell>
                  ))}
              </TableRow>
            ))}
            <TableRow>
              <TableCell component="th" scope="row" sx={rowHead}>{t('compare.table.actions')}</TableCell>
              {cols.map((c, i) => {
                const unavailable = !c.plan || !canAddToCart({ plans: [c.plan] })
                return (
                  <TableCell key={i} sx={cellSx}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, alignItems: 'stretch' }}>
                      <Button size="small" variant="outlined" component={RouterLink} to={`/products/${c.product.id}`}
                        aria-label={t('compare.actions.viewLabel', { name: c.product.name })}>
                        {t('compare.actions.view')}
                      </Button>
                      <Tooltip title={unavailable ? t('cart.noPaidPlan') : ''}>
                        <Box component="span" sx={{ display: 'flex' }}>
                          <Button size="small" variant="contained" fullWidth disabled={unavailable || busyId === c.product.id}
                            startIcon={<ShoppingCart size={14} aria-hidden />}
                            aria-label={t('compare.actions.addLabel', { name: c.product.name })}
                            onClick={() => { if (c.plan) addToCart({ ...c.product, plans: [c.plan] }) }}>
                            {t('cart.addToCart')}
                          </Button>
                        </Box>
                      </Tooltip>
                    </Box>
                  </TableCell>
                )
              })}
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>
    )
  }

  const skeleton = (
    <Box aria-busy="true" sx={{ display: 'grid', gap: 1 }}>
      {[0, 1, 2, 3, 4, 5].map((i) => <Skeleton key={i} variant="rounded" height={44} />)}
    </Box>
  )

  const content = (
    <>
      <PageHeader icon={GitCompare} title={t('compare.title')} subtitle={t('compare.subtitle')} />
      <Box role="status" aria-live="polite" sx={visuallyHidden}>{statusText}</Box>
      {chooser}
      {items.length < MIN_COMPARE_ITEMS && (
        <EmptyState icon={GitCompare} title={t('compare.empty.title')} description={t('compare.empty.body')}
          action={<Button component={RouterLink} to="/products" variant="outlined">{t('compare.browseCatalog')}</Button>} />
      )}
      {loading && skeleton}
      {current?.error && <ErrorState title={t('compare.error.title')} message={current.error} onRetry={() => setReload((n) => n + 1)} />}
      {current?.data && (
        <>
          {renderTable(current.data)}
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 2 }}>{t('compare.note')}</Typography>
        </>
      )}
      {notice}
    </>
  )

  if (inShell) return content
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container component="main" id="main-content" maxWidth="xl" sx={{ pt: '112px', pb: 8 }}>
        {content}
      </Container>
    </Box>
  )
}
