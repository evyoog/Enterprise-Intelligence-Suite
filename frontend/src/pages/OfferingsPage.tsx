import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, Paper, Skeleton, Typography } from '@mui/material'
import { Boxes } from 'lucide-react'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { offeringsApi, type Offering } from '../api/offeringsApi'
import { PageHeader } from '../components/layout/PageHeader'
import { EmptyState } from '../components/ui/EmptyState'

/** "/offerings" — REQ-CAT-005: the published offerings. Prices stay on each product (C85, OF-2 is not decided). */
export function OfferingsPage() {
  const { t } = useTranslation()
  const [offerings, setOfferings] = useState<Offering[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    offeringsApi.list().then(setOfferings).catch((e) => setError(e instanceof ApiError ? e.message : t('offerings.public.loadFailed')))
  }, [t])

  return (
    <Box>
      <PageHeader icon={Boxes} accent="violet" area="catalog" title={t('offerings.public.title')} subtitle={t('offerings.public.subtitle')} />
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {!offerings && !error && <Skeleton variant="rounded" height={120} />}
      {offerings && offerings.length === 0 && <EmptyState icon={Boxes} title={t('offerings.public.empty')} />}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' }, gap: 2 }}>
        {offerings?.map((o) => (
          <Paper key={o.id} variant="outlined" sx={{ p: 2.5, borderRadius: 3 }}>
            <Typography variant="h6" component="h2" sx={{ fontWeight: 700 }}>
              <RouterLink to={`/offerings/${o.id}`} style={{ color: 'inherit', textDecoration: 'none' }}>{o.name}</RouterLink>
            </Typography>
            {o.description && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>{o.description}</Typography>}
            <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap', mt: 1.5 }}>
              {o.products.map((p) => <Chip key={p.id} size="small" label={p.name} />)}
            </Box>
            <Button component={RouterLink} to={`/offerings/${o.id}`} size="small" sx={{ mt: 1.5 }}>{t('offerings.public.view')}</Button>
          </Paper>
        ))}
      </Box>
    </Box>
  )
}

/** "/offerings/:id" — one offering and the products in it; each product is bought from its own page. */
export function OfferingDetailPage() {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const [offering, setOffering] = useState<Offering | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    offeringsApi.get(Number(id)).then(setOffering).catch((e) => setError(e instanceof ApiError ? e.message : t('offerings.public.loadFailed')))
  }, [id, t])

  if (error) return <Alert severity="error">{error}</Alert>
  if (!offering) return <Skeleton variant="rounded" height={160} />

  return (
    <Box>
      <PageHeader icon={Boxes} accent="violet" area="catalog" title={offering.name} subtitle={offering.description} />
      <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('offerings.public.included')}</Typography>
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' }, gap: 2 }}>
        {offering.products.map((p) => (
          <Paper key={p.id} variant="outlined" sx={{ p: 2, borderRadius: 3 }}>
            <Typography sx={{ fontWeight: 600 }}>{p.name}</Typography>
            <Button component={RouterLink} to={`/products/${p.id}`} size="small" sx={{ mt: 1 }}>{t('offerings.public.viewProduct')}</Button>
          </Paper>
        ))}
      </Box>
      <Typography variant="body2" sx={{ color: 'text.secondary', mt: 2 }}>{t('offerings.public.priceNote')}</Typography>
    </Box>
  )
}
