import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, Paper, Rating, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { adminReviewsApi, type ProductReview, type ReviewStatus } from '../../api/reviewsApi'
import { PageHeader } from '../../components/layout/PageHeader'

const STATUS_COLOR: Record<ReviewStatus, 'warning' | 'success' | 'error'> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
}

/** "/admin/reviews" — MANAGE_REVIEWS. */
export function AdminReviewsPage() {
  const { t } = useTranslation()
  const [reviews, setReviews] = useState<ProductReview[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  const load = () => {
    adminReviewsApi.listAll()
      .then(setReviews)
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : 'Could not load reviews.'))
  }

  useEffect(load, [])

  const decide = async (review: ProductReview, approve: boolean) => {
    setBusyId(review.id)
    try {
      const updated = approve ? await adminReviewsApi.approve(review.id) : await adminReviewsApi.reject(review.id)
      setReviews((prev) => prev?.map((r) => (r.id === updated.id ? updated : r)) ?? prev)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Box>
      <PageHeader title={t('reviews.admin.title')} />
      {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {reviews?.map((review) => (
          <Paper key={review.id} variant="outlined" sx={{ p: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Rating value={review.rating} readOnly size="small" />
                <Typography sx={{ fontWeight: 700 }}>{review.productName}</Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>— {review.customerName}</Typography>
              </Box>
              <Chip size="small" color={STATUS_COLOR[review.status]} label={t(`reviews.admin.status.${review.status}`)} />
            </Box>
            {review.comment && <Typography variant="body2" sx={{ mt: 1 }}>{review.comment}</Typography>}
            {review.status === 'PENDING' && (
              <Box sx={{ display: 'flex', gap: 1, mt: 1.5 }}>
                <Button size="small" variant="contained" disabled={busyId === review.id} onClick={() => decide(review, true)}>
                  {t('reviews.admin.approve')}
                </Button>
                <Button size="small" color="error" variant="outlined" disabled={busyId === review.id} onClick={() => decide(review, false)}>
                  {t('reviews.admin.reject')}
                </Button>
              </Box>
            )}
          </Paper>
        ))}
      </Box>
    </Box>
  )
}
