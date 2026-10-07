import { productContentApi, type PublicContent } from '../api/productContentApi'
import { ProductResources } from '../components/productcontent/ProductResources'
import { hasPublishedContent } from '../components/productcontent/productContentUtils'
import { Package as PHPackage } from 'lucide-react'
import { useBuy } from '../components/cart/useBuy'
import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Breadcrumbs, Button, Chip, CircularProgress, Fade, LinearProgress, Link as MuiLink, Paper, Rating, Tab, Tabs, TextField, Typography,
} from '@mui/material'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { Boxes, CheckCircle2, ClipboardCheck, Rocket, Search, Star } from 'lucide-react'
import { ApiError, resolveAssetUrl } from '../api/client'
import { FeatureTag } from '../components/ui/FeatureTag'
import { SupportCTA } from '../components/ui/SupportCTA'
import { appColor, showcasePalette } from '../utils/showcaseColor'
import { productsApi, type Product, type ProductPlan } from '../api/productsApi'
import { reviewsApi, type ProductRatingSummary } from '../api/reviewsApi'
import { useAuth } from '../auth/AuthProvider'
import { PageHeader } from '../components/layout/PageHeader'

const FLOW_STEPS = [
  { icon: Search, titleKey: 'discover', descKey: 'discoverDesc' },
  { icon: ClipboardCheck, titleKey: 'subscribe', descKey: 'subscribeDesc' },
  { icon: CheckCircle2, titleKey: 'access', descKey: 'accessDesc' },
  { icon: Rocket, titleKey: 'launch', descKey: 'launchDesc' },
] as const

const BILLING_SUFFIX: Record<ProductPlan['billingPeriod'], string> = { MONTHLY: '/mo', YEARLY: '/yr', ONE_TIME: '' }

type TabKey = 'overview' | 'pricing' | 'resources' | 'reviews'

/** "/products/:id" — public product detail: description, pricing, the
 * platform's own subscribe → access → launch flow, and ratings/reviews
 * (03.04.01, sprint 2027.1.3). C49 reorganized this into three tabs
 * (Overview / Pricing / Reviews) with a rating-distribution breakdown that
 * doubles as a filter — real interactivity built from data this page
 * already has, not decoration. C45 added the Subscribe action and the flow
 * showcase — every product suite gets the same page shape and the same real
 * subscribe/order paths, not a per-product fabricated workflow (which this
 * codebase has no source for beyond the suite's own name). */
export function ProductDetailPage() {
  const { t } = useTranslation()
  const auth = useAuth()
  const { id } = useParams<{ id: string }>()
  const productId = Number(id)

  const [product, setProduct] = useState<Product | null>(null)
  const [ratings, setRatings] = useState<ProductRatingSummary | null>(null)
  const [myRating, setMyRating] = useState<number | null>(null)
  const [myComment, setMyComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [justSubmitted, setJustSubmitted] = useState(false)

  const [tab, setTab] = useState<TabKey>('overview')
  // REQ-CAT-004: published product content (datasheets, documentation, images, videos, case studies).
  const [content, setContent] = useState<PublicContent | null>(null)
  const [starFilter, setStarFilter] = useState<number | null>(null)

  const loadRatings = () => reviewsApi.getRatings(productId).then(setRatings).catch(() => {})

  useEffect(() => {
    productsApi.get(productId).then(setProduct).catch(() => {})
    productContentApi.get(productId).then(setContent).catch(() => setContent(null))
    loadRatings()
    if (auth.isAuthenticated) {
      reviewsApi.getMine(productId)
        .then((mine) => { setMyRating(mine.rating); setMyComment(mine.comment ?? '') })
        .catch(() => {})
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productId])

  // C59: Buy adds the plan to the cart and opens it (useBuy).
  const buy = useBuy()
  const handleSubscribe = (planId?: number) => buy(productId, planId)

  const submit = async () => {
    if (!myRating) return
    setSubmitting(true)
    setSubmitError(null)
    try {
      await reviewsApi.submit(productId, myRating, myComment || undefined)
      setJustSubmitted(true)
      loadRatings()
    } catch (e) {
      setSubmitError(e instanceof ApiError ? e.message : 'Could not submit your review.')
    } finally {
      setSubmitting(false)
    }
  }

  // Real data, computed client-side: how many of THIS product's approved
  // reviews landed on each star value — the bars below double as a filter
  // (click one to narrow the list, click again to clear it) rather than
  // being purely decorative.
  const distribution = useMemo(() => {
    const counts = [0, 0, 0, 0, 0]
    for (const review of ratings?.reviews ?? []) {
      const bucket = Math.min(5, Math.max(1, Math.round(review.rating)))
      counts[bucket - 1] += 1
    }
    return counts
  }, [ratings])

  const visibleReviews = useMemo(() => {
    const all = ratings?.reviews ?? []
    return starFilter === null ? all : all.filter((r) => Math.round(r.rating) === starFilter)
  }, [ratings, starFilter])

  if (!product) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
  }

  // C66: the app's showcase colour (own, else its platform's, else the default).
  const palette = showcasePalette(appColor(product))
  const accent = { bg: palette.soft, fg: palette.text }
  const plan = product.plans.find((p) => p.billingPeriod === 'MONTHLY') ?? product.plans[0]
  const price = plan ? plan.price : product.price

  return (
    <Box sx={{ maxWidth: 1120 }}>
      <Breadcrumbs aria-label={t('catalog.detail.breadcrumbs')} sx={{ mb: 2 }}>
        <MuiLink component={RouterLink} to="/products" underline="hover" color="inherit">{t('catalog.title')}</MuiLink>
        {product.platforms[0] && (
          <MuiLink component={RouterLink} to={`/catalog/platforms/${product.platforms[0].id}`} underline="hover" color="inherit">{product.platforms[0].name}</MuiLink>
        )}
        <Typography color="text.primary">{product.name}</Typography>
      </Breadcrumbs>
      <PageHeader icon={PHPackage} accent="blue" eyebrow={t('catalog.app.type')} title={product.name} subtitle={product.category} />

      <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 2.5, flexWrap: 'wrap' }}>
          <Box
            sx={{
              width: 56, height: 56, borderRadius: 2.5, flexShrink: 0,
              display: 'grid', placeItems: 'center', bgcolor: accent.bg, color: accent.fg,
              transition: 'transform 0.2s ease',
              '&:hover': { transform: 'scale(1.08) rotate(-2deg)' },
            }}
          >
            {product.imageUrl
              ? <Box component="img" src={resolveAssetUrl(product.imageUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 2.5 }} />
              : <Boxes size={28} />}
          </Box>
          <Box sx={{ flex: 1, minWidth: 220 }}>
            {product.description && <Typography sx={{ mb: 1 }}>{product.description}</Typography>}
            {/* C66: feature tags and resources configured on the app. */}
            {(product.featureTags ?? []).length > 0 && (
              <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap', mb: 1.5 }} aria-label={t('catalog.detail.features')} role="list">
                {(product.featureTags ?? []).map((f) => <Box key={f} role="listitem" component="span"><FeatureTag label={f} /></Box>)}
              </Box>
            )}
            {(product.documentationUrl || product.supportUrl) && (
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 1.5 }}>
                {product.documentationUrl && (
                  <MuiLink href={product.documentationUrl} target="_blank" rel="noopener">{t('catalog.app.documentation')}</MuiLink>
                )}
                {product.supportUrl && (
                  <MuiLink href={product.supportUrl} target="_blank" rel="noopener">{t('catalog.app.supportSite')}</MuiLink>
                )}
              </Box>
            )}
            {ratings && ratings.reviewCount > 0 && (
              <Box
                role="button"
                tabIndex={0}
                onClick={() => setTab('reviews')}
                onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') setTab('reviews') }}
                sx={{
                  display: 'inline-flex', alignItems: 'center', gap: 0.75, mb: 1.5, cursor: 'pointer',
                  color: 'text.secondary', '&:hover': { color: accent.fg },
                }}
              >
                <Rating value={ratings.averageRating ?? 0} precision={0.1} readOnly size="small" />
                <Typography variant="body2">
                  {t('reviews.averageOf', { average: ratings.averageRating?.toFixed(1), count: ratings.reviewCount, plural: ratings.reviewCount === 1 ? '' : 's' })}
                </Typography>
              </Box>
            )}
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              ${price.toFixed(2)}
              <Typography component="span" variant="body2" sx={{ color: 'text.secondary', ml: 0.5 }}>
                {plan ? t(`productDetail.billing.${plan.billingPeriod}`) : ''}
              </Typography>
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, alignItems: 'flex-start', justifyContent: 'center' }}>
            <Button variant="contained" size="large" onClick={() => handleSubscribe(plan?.id)}>
              {t('productDetail.subscribe')}
            </Button>
            {auth.isAuthenticated && (
              <Typography variant="caption">
                <RouterLink to="/organization/orders">{t('productDetail.requestForOrg')}</RouterLink>
              </Typography>
            )}
          </Box>
        </Box>
      </Paper>

      <Tabs
        value={tab}
        onChange={(_, value) => setTab(value)}
        sx={{ mb: 3, borderBottom: '1px solid', borderColor: 'divider' }}
      >
        <Tab value="overview" label={t('productDetail.tabs.overview')} />
        <Tab value="pricing" label={t('productDetail.tabs.pricing')} />
        {hasPublishedContent(content) && <Tab value="resources" label={t('productContent.public.tab')} />}
        <Tab value="reviews" label={t('productDetail.tabs.reviews', { count: ratings?.reviewCount ?? 0 })} />
      </Tabs>

      {tab === 'overview' && (
        <Fade in timeout={250}>
          <Box>
            {plan?.includedFeatures && (
              <Box sx={{ mb: 4 }}>
                <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{t('productDetail.included')}</Typography>
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 1 }}>
                  {plan.includedFeatures.split(',').map((feature) => (
                    <Box key={feature} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CheckCircle2 size={16} color={accent.fg} />
                      <Typography variant="body2">{feature.trim()}</Typography>
                    </Box>
                  ))}
                </Box>
              </Box>
            )}

            <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{t('productDetail.flowTitle')}</Typography>
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(4, 1fr)' }, gap: 1.5 }}>
              {FLOW_STEPS.map((step, index) => {
                const StepIcon = step.icon
                return (
                  <Paper
                    key={step.titleKey}
                    variant="outlined"
                    sx={{
                      p: 2, position: 'relative', transition: 'transform 0.15s ease, box-shadow 0.15s ease',
                      '&:hover': { transform: 'translateY(-2px)', boxShadow: 2 },
                    }}
                  >
                    <Typography variant="caption" sx={{ color: 'text.disabled', fontWeight: 700 }}>
                      {String(index + 1).padStart(2, '0')}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, mb: 0.5 }}>
                      <StepIcon size={18} color={accent.fg} />
                      <Typography sx={{ fontWeight: 700, fontSize: 14 }}>{t(`productDetail.flow.${step.titleKey}`)}</Typography>
                    </Box>
                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                      {t(`productDetail.flow.${step.descKey}`)}
                    </Typography>
                  </Paper>
                )
              })}
            </Box>

            {product.launchUrl && (
              <Box sx={{ mt: 3 }}>
                <Chip label={product.launchUrl} component="a" href={product.launchUrl} target="_blank" rel="noopener" clickable />
              </Box>
            )}
          </Box>
        </Fade>
      )}

      {tab === 'pricing' && (
        <Fade in timeout={250}>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(auto-fit, minmax(200px, 1fr))' }, gap: 2 }}>
            {(product.plans.length > 0 ? product.plans : []).map((p) => (
              <Paper
                key={p.id}
                variant="outlined"
                sx={{
                  p: 2.5, transition: 'transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease',
                  '&:hover': { transform: 'translateY(-3px)', boxShadow: 3, borderColor: accent.fg },
                }}
              >
                <Typography sx={{ fontWeight: 700, mb: 0.5 }}>{p.name}</Typography>
                <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                  ${p.price.toFixed(2)}
                  <Typography component="span" variant="body2" sx={{ color: 'text.secondary' }}>{BILLING_SUFFIX[p.billingPeriod]}</Typography>
                </Typography>
                {p.includedFeatures && (
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, mb: 1.5 }}>
                    {p.includedFeatures.split(',').map((feature) => (
                      <Box key={feature} sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                        <CheckCircle2 size={14} color={accent.fg} />
                        <Typography variant="caption">{feature.trim()}</Typography>
                      </Box>
                    ))}
                  </Box>
                )}
                <Button variant="outlined" size="small" fullWidth onClick={() => handleSubscribe(p.id)}>
                  {t('productDetail.subscribe')}
                </Button>
              </Paper>
            ))}
            {product.plans.length === 0 && (
              <Paper variant="outlined" sx={{ p: 2.5 }}>
                <Typography variant="h5" sx={{ fontWeight: 700, mb: 1.5 }}>${product.price.toFixed(2)}</Typography>
                <Button variant="contained" onClick={() => handleSubscribe()}>{t('productDetail.subscribe')}</Button>
              </Paper>
            )}
          </Box>
        </Fade>
      )}

      {tab === 'resources' && content && <ProductResources productId={productId} content={content} />}

      {tab === 'reviews' && (
        <Fade in timeout={250}>
          <Box>
            {ratings && ratings.reviewCount > 0 ? (
              <Box sx={{ display: 'flex', gap: 4, flexWrap: 'wrap', mb: 3 }}>
                <Box sx={{ textAlign: 'center', minWidth: 120 }}>
                  <Typography variant="h3" sx={{ fontWeight: 700, lineHeight: 1 }}>{ratings.averageRating?.toFixed(1)}</Typography>
                  <Rating value={ratings.averageRating ?? 0} precision={0.1} readOnly size="small" sx={{ my: 0.5 }} />
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    {t('reviews.averageOf', { average: ratings.averageRating?.toFixed(1), count: ratings.reviewCount, plural: ratings.reviewCount === 1 ? '' : 's' })}
                  </Typography>
                </Box>
                <Box sx={{ flex: 1, minWidth: 220, display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                  {[5, 4, 3, 2, 1].map((star) => {
                    const count = distribution[star - 1]
                    const pct = ratings.reviewCount > 0 ? (count / ratings.reviewCount) * 100 : 0
                    const active = starFilter === star
                    return (
                      <Box
                        key={star}
                        role="button"
                        tabIndex={count > 0 ? 0 : -1}
                        aria-pressed={active}
                        aria-label={t('reviews.filterByStars', { count: star })}
                        onClick={() => count > 0 && setStarFilter(active ? null : star)}
                        onKeyDown={(e) => { if ((e.key === 'Enter' || e.key === ' ') && count > 0) { e.preventDefault(); setStarFilter(active ? null : star) } }}
                        sx={{
                          display: 'flex', alignItems: 'center', gap: 1, cursor: count > 0 ? 'pointer' : 'default',
                          opacity: starFilter !== null && !active ? 0.5 : 1, transition: 'opacity 0.15s ease',
                          borderRadius: 1, px: 0.5, '&:hover': count > 0 ? { bgcolor: 'action.hover' } : undefined,
                        }}
                      >
                        <Typography variant="caption" sx={{ width: 34, display: 'flex', alignItems: 'center', gap: 0.25 }}>
                          {star} <Star size={11} fill="currentColor" />
                        </Typography>
                        <LinearProgress
                          variant="determinate"
                          value={pct}
                          sx={{
                            flex: 1, height: 6, borderRadius: 3, bgcolor: 'action.hover',
                            '& .MuiLinearProgress-bar': { bgcolor: accent.fg, borderRadius: 3 },
                          }}
                        />
                        <Typography variant="caption" sx={{ width: 24, textAlign: 'right', color: 'text.secondary' }}>{count}</Typography>
                      </Box>
                    )
                  })}
                  {starFilter !== null && (
                    <Button size="small" sx={{ alignSelf: 'flex-start', mt: 0.5 }} onClick={() => setStarFilter(null)}>
                      {t('reviews.clearFilter')}
                    </Button>
                  )}
                </Box>
              </Box>
            ) : (
              <Typography sx={{ color: 'text.secondary', mb: 2 }}>{t('reviews.noReviews')}</Typography>
            )}

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, mb: 3 }}>
              {visibleReviews.length === 0 && starFilter !== null && (
                <Typography sx={{ color: 'text.secondary' }}>{t('reviews.noneAtThisRating')}</Typography>
              )}
              {visibleReviews.map((review) => (
                <Paper key={review.id} variant="outlined" sx={{ p: 1.5 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Rating value={review.rating} readOnly size="small" />
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>{review.customerName}</Typography>
                  </Box>
                  {review.comment && <Typography variant="body2" sx={{ mt: 0.5 }}>{review.comment}</Typography>}
                </Paper>
              ))}
            </Box>

            {auth.isAuthenticated && (
              <Paper variant="outlined" sx={{ p: 2.5 }}>
                <Typography sx={{ fontWeight: 700, mb: 1.5 }}>{t('reviews.yourReview.title')}</Typography>
                {submitError && <Alert severity="error" sx={{ mb: 2 }}>{submitError}</Alert>}
                {justSubmitted && <Alert severity="info" sx={{ mb: 2 }}>{t('reviews.yourReview.pendingNotice')}</Alert>}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  <Typography component="label" htmlFor="review-rating">{t('reviews.yourReview.rating')}</Typography>
                  <Rating id="review-rating" value={myRating} onChange={(_, value) => setMyRating(value)} />
                </Box>
                <TextField
                  fullWidth
                  multiline
                  minRows={2}
                  sx={{ mb: 2 }}
                  label={t('reviews.yourReview.comment')}
                  value={myComment}
                  onChange={(e) => setMyComment(e.target.value)}
                />
                <Button variant="contained" disabled={submitting || !myRating} onClick={submit}>
                  {t('reviews.yourReview.submit')}
                </Button>
              </Paper>
            )}
          </Box>
        </Fade>
      )}
      <Box sx={{ mt: 4 }}>
        <SupportCTA title={t('catalog.detail.helpTitle')} description={t('catalog.detail.helpBody')} />
      </Box>
    </Box>
  )
}
