import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, CircularProgress, Paper, Rating, TextField, Typography } from '@mui/material'
import { useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { productsApi, type Product } from '../api/productsApi'
import { reviewsApi, type ProductRatingSummary } from '../api/reviewsApi'
import { useAuth } from '../auth/AuthProvider'
import { PageHeader } from '../components/layout/PageHeader'

/** "/products/:id" — public product detail, built for 03.04.01 Reviews &
 * Ratings (sprint 2027.1.3) since no customer-facing detail page existed
 * before this sprint. */
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

  return (
    <Box>
      <PageHeader title={product.name} subtitle={product.category} />
      {product.description && <Typography sx={{ mb: 3 }}>{product.description}</Typography>}

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
