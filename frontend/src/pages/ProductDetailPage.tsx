import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, CircularProgress, Paper, Rating, TextField, Typography } from '@mui/material'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { Boxes, CheckCircle2, ClipboardCheck, Rocket, Search } from 'lucide-react'
import { ApiError } from '../api/client'
import { productsApi, type Product } from '../api/productsApi'
import { reviewsApi, type ProductRatingSummary } from '../api/reviewsApi'
import { myProductsApi } from '../api/registrationApi'
import { useAuth } from '../auth/AuthProvider'
import { useAuthModal } from '../auth/AuthModalContext'
import { PageHeader } from '../components/layout/PageHeader'
import { accentFor } from '../utils/accentColor'

const FLOW_STEPS = [
  { icon: Search, titleKey: 'discover', descKey: 'discoverDesc' },
  { icon: ClipboardCheck, titleKey: 'subscribe', descKey: 'subscribeDesc' },
  { icon: CheckCircle2, titleKey: 'access', descKey: 'accessDesc' },
  { icon: Rocket, titleKey: 'launch', descKey: 'launchDesc' },
] as const

/** "/products/:id" — public product detail: description, pricing, the
 * platform's own subscribe → access → launch flow, and ratings/reviews
 * (03.04.01, sprint 2027.1.3). C45 added the Subscribe action and the flow
 * showcase — every product suite gets the same page shape and the same real
 * subscribe/order paths, not a per-product fabricated workflow (which this
 * codebase has no source for beyond the suite's own name). */
export function ProductDetailPage() {
  const { t } = useTranslation()
  const auth = useAuth()
  const authModal = useAuthModal()
  const { id } = useParams<{ id: string }>()
  const productId = Number(id)

  const [product, setProduct] = useState<Product | null>(null)
  const [ratings, setRatings] = useState<ProductRatingSummary | null>(null)
  const [myRating, setMyRating] = useState<number | null>(null)
  const [myComment, setMyComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [justSubmitted, setJustSubmitted] = useState(false)

  const [subscribing, setSubscribing] = useState(false)
  const [subscribed, setSubscribed] = useState(false)
  const [subscribeError, setSubscribeError] = useState<string | null>(null)

  const loadRatings = () => reviewsApi.getRatings(productId).then(setRatings).catch(() => {})

  useEffect(() => {
    productsApi.get(productId).then(setProduct).catch(() => {})
    loadRatings()
    if (auth.isAuthenticated) {
      reviewsApi.getMine(productId)
        .then((mine) => { setMyRating(mine.rating); setMyComment(mine.comment ?? '') })
        .catch(() => {})
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productId])

  const handleSubscribe = () => {
    if (!auth.isAuthenticated) { authModal.openRegister(); return }
    setSubscribing(true)
    setSubscribeError(null)
    myProductsApi.subscribe(productId)
      .then(() => setSubscribed(true))
      .catch((e) => setSubscribeError(e instanceof ApiError ? e.message : t('productDetail.subscribeError')))
      .finally(() => setSubscribing(false))
  }

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

  if (!product) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
  }

  const accent = accentFor(product.name)
  const plan = product.plans.find((p) => p.billingPeriod === 'MONTHLY') ?? product.plans[0]
  const price = plan ? plan.price : product.price

  return (
    <Box sx={{ maxWidth: 880 }}>
      <PageHeader title={product.name} subtitle={product.category} />

      <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 2.5, flexWrap: 'wrap' }}>
          <Box
            sx={{
              width: 56, height: 56, borderRadius: 2.5, flexShrink: 0,
              display: 'grid', placeItems: 'center', bgcolor: accent.bg, color: accent.fg,
            }}
          >
            <Boxes size={28} />
          </Box>
          <Box sx={{ flex: 1, minWidth: 220 }}>
            {product.description && <Typography sx={{ mb: 1.5 }}>{product.description}</Typography>}
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              ${price.toFixed(2)}
              <Typography component="span" variant="body2" sx={{ color: 'text.secondary', ml: 0.5 }}>
                {plan ? t(`productDetail.billing.${plan.billingPeriod}`) : ''}
              </Typography>
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, alignItems: 'flex-start', justifyContent: 'center' }}>
            {subscribeError && <Alert severity="error" sx={{ mb: 0.5 }}>{subscribeError}</Alert>}
            {subscribed ? (
              <Alert severity="success" sx={{ mb: 0.5 }}>
                {t('productDetail.subscribed')}{' '}
                <RouterLink to="/my/products">{t('productDetail.viewInMyProducts')}</RouterLink>
              </Alert>
            ) : (
              <Button
                variant="contained"
                size="large"
                disabled={subscribing}
                onClick={handleSubscribe}
                startIcon={subscribing ? <CircularProgress size={16} color="inherit" /> : undefined}
              >
                {t('productDetail.subscribe')}
              </Button>
            )}
            {auth.isAuthenticated && (
              <Typography variant="caption">
                <RouterLink to="/organization/orders">{t('productDetail.requestForOrg')}</RouterLink>
              </Typography>
            )}
          </Box>
        </Box>
      </Paper>

      <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{t('productDetail.flowTitle')}</Typography>
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(4, 1fr)' }, gap: 1.5, mb: 4 }}>
        {FLOW_STEPS.map((step, index) => {
          const StepIcon = step.icon
          return (
            <Paper key={step.titleKey} variant="outlined" sx={{ p: 2, position: 'relative' }}>
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

      <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1 }}>{t('reviews.title')}</Typography>
      {ratings && ratings.reviewCount > 0 ? (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <Rating value={ratings.averageRating ?? 0} precision={0.1} readOnly />
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            {t('reviews.averageOf', { average: ratings.averageRating?.toFixed(1), count: ratings.reviewCount, plural: ratings.reviewCount === 1 ? '' : 's' })}
          </Typography>
        </Box>
      ) : (
        <Typography sx={{ color: 'text.secondary', mb: 2 }}>{t('reviews.noReviews')}</Typography>
      )}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, mb: 3 }}>
        {ratings?.reviews.map((review) => (
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

      {product.launchUrl && (
        <Box sx={{ mt: 3 }}>
          <Chip label={product.launchUrl} component="a" href={product.launchUrl} target="_blank" rel="noopener" clickable />
        </Box>
      )}
    </Box>
  )
}
