import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Breadcrumbs, Container, Grid, Link, Paper, Skeleton, Typography } from '@mui/material'
import { Layers } from 'lucide-react'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { ApiError, resolveAssetUrl } from '../api/client'
import { catalogApi, type CatalogPlatform } from '../api/catalogApi'
import { useBuy } from '../components/cart/useBuy'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { useInAppShell } from '../components/layout/appShellContext'
import { ProductTile } from '../components/products/ProductTile'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { FeatureTag } from '../components/ui/FeatureTag'
import { StatusBadge } from '../components/ui/StatusBadge'
import { SupportCTA } from '../components/ui/SupportCTA'
import { showcasePalette } from '../utils/showcaseColor'

/**
 * "/catalog/platforms/:id" — a product family's details (C66): overview,
 * its applications, the features its apps list, and product information —
 * all from the public catalog API. Version history is not shown: the
 * platform keeps no release data (only apps keep an edit counter).
 */
export function PlatformDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { t } = useTranslation()
  const inShell = useInAppShell()
  const handleSubscribeClick = useBuy()
  const [platform, setPlatform] = useState<CatalogPlatform | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [reload, setReload] = useState(0)

  useEffect(() => {
    if (!id) return
    catalogApi.getPlatform(Number(id))
      .then((p) => { setPlatform(p); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('catalog.detail.loadError')))
  }, [id, reload, t])

  const palette = showcasePalette(platform?.primaryColor)
  const apps = platform?.apps ?? []

  const body = (
    <>
      <Breadcrumbs aria-label={t('catalog.detail.breadcrumbs')} sx={{ mb: 2 }}>
        <Link component={RouterLink} to="/products" underline="hover" color="inherit">{t('catalog.title')}</Link>
        <Typography color="text.primary">{platform?.name ?? '…'}</Typography>
      </Breadcrumbs>

      {error && <ErrorState title={t('catalog.detail.loadErrorTitle')} message={error} onRetry={() => setReload((n) => n + 1)} />}
      {!error && !platform && <Skeleton variant="rounded" height={320} />}

      {platform && (
        <>
          <Box component="header" sx={{ display: 'flex', gap: 2, alignItems: 'flex-start', mb: 3, flexWrap: 'wrap' }}>
            <Box sx={{ width: 64, height: 64, borderRadius: 3, display: 'grid', placeItems: 'center', overflow: 'hidden', bgcolor: palette.soft, color: palette.text, flexShrink: 0 }}>
              {platform.imageUrl
                ? <Box component="img" src={resolveAssetUrl(platform.imageUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                : <Layers size={30} aria-hidden />}
            </Box>
            <Box sx={{ minWidth: 0, flex: 1 }}>
              <Typography variant="overline" sx={{ color: 'primary.main' }}>{t('catalog.detail.label')}</Typography>
              <Typography variant="h4" sx={{ fontSize: { xs: 24, sm: 30 } }}>{platform.name}</Typography>
              <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', mt: 1, flexWrap: 'wrap' }}>
                <StatusBadge label={t('catalog.status.available')} tone="success" />
                <Box component="span" sx={{ px: 1, py: 0.25, borderRadius: 1.5, fontSize: 12, fontWeight: 600, color: palette.text, bgcolor: palette.soft }}>
                  {t('catalog.platformType')}
                </Box>
              </Box>
            </Box>
          </Box>

          <Grid container spacing={3}>
            <Grid size={{ xs: 12, lg: 8 }} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Paper variant="outlined" component="section" aria-labelledby="platform-overview" sx={{ p: 3, borderRadius: 3 }}>
                <Typography id="platform-overview" component="h2" variant="h6" sx={{ mb: 1 }}>{t('catalog.detail.overview')}</Typography>
                <Typography sx={{ color: platform.description ? 'text.primary' : 'text.secondary', whiteSpace: 'pre-line' }}>
                  {platform.description || t('catalog.detail.noDescription')}
                </Typography>
              </Paper>

              <Box component="section" aria-labelledby="platform-apps">
                <Typography id="platform-apps" component="h2" variant="h6" sx={{ mb: 1.5 }}>
                  {t('catalog.detail.applications')} <Typography component="span" sx={{ color: 'text.secondary', fontWeight: 500 }}>({apps.length})</Typography>
                </Typography>
                {apps.length === 0
                  ? <EmptyState title={t('catalog.detail.noAppsTitle')} description={t('catalog.detail.noAppsBody')} />
                  : (
                    <Grid container spacing={2.5}>
                      {apps.map((app) => (
                        <Grid key={app.id} size={{ xs: 12, sm: 6 }}>
                          <ProductTile product={app} onSubscribe={() => handleSubscribeClick(app.id)} />
                        </Grid>
                      ))}
                    </Grid>
                  )}
              </Box>

              {platform.featureTags.length > 0 && (
                <Paper variant="outlined" component="section" aria-labelledby="platform-features" sx={{ p: 3, borderRadius: 3 }}>
                  <Typography id="platform-features" component="h2" variant="h6" sx={{ mb: 1.5 }}>{t('catalog.detail.features')}</Typography>
                  <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap' }}>
                    {platform.featureTags.map((f) => <FeatureTag key={f} label={f} />)}
                  </Box>
                </Paper>
              )}
            </Grid>

            <Grid size={{ xs: 12, lg: 4 }}>
              <Paper variant="outlined" component="section" aria-labelledby="platform-info" sx={{ p: 3, borderRadius: 3 }}>
                <Typography id="platform-info" component="h2" variant="h6" sx={{ mb: 1.5 }}>{t('catalog.detail.information')}</Typography>
                <Box component="dl" sx={{ m: 0, display: 'grid', gridTemplateColumns: 'auto 1fr', columnGap: 2, rowGap: 1.25,
                  '& dt': { color: 'text.secondary' }, '& dd': { m: 0, fontWeight: 600, textAlign: 'right' } }}>
                  <dt>{t('catalog.detail.type')}</dt><dd>{t('catalog.platformType')}</dd>
                  <dt>{t('catalog.detail.status')}</dt><dd>{t('catalog.status.available')}</dd>
                  <dt>{t('catalog.detail.appsCount')}</dt><dd>{platform.appCount}</dd>
                  <dt>{t('catalog.detail.categories')}</dt><dd>{platform.categories.length > 0 ? platform.categories.join(', ') : '—'}</dd>
                </Box>
              </Paper>
            </Grid>
          </Grid>

          <Box sx={{ mt: 4 }}>
            <SupportCTA title={t('catalog.detail.helpTitle')} description={t('catalog.detail.helpBody')} />
          </Box>
        </>
      )}
    </>
  )

  if (inShell) return body
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container component="main" id="main-content" maxWidth="xl" sx={{ pt: '112px', pb: 8 }}>{body}</Container>
    </Box>
  )
}
