import { Layers as PHLayers, Pencil, Plus } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Button, Grid, Skeleton } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { platformsApi, type Platform } from '../../api/platformsApi'
import { productsApi, type Product } from '../../api/productsApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { EmptyState } from '../../components/ui/EmptyState'
import { ErrorState } from '../../components/ui/ErrorState'
import { ShowcaseCard } from '../../components/ui/ShowcaseCard'

/**
 * "/admin/platforms" — every platform (product family), each a doorway into
 * its own scoped dashboard (PlatformDashboardPage). C66: the same
 * ShowcaseCard as the catalog, with the platform's colour, its real app count
 * (computed from the admin product list) and its catalog visibility.
 */
export function PlatformsListPage() {
  const { t } = useTranslation()
  const [platforms, setPlatforms] = useState<Platform[] | null>(null)
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [reload, setReload] = useState(0)

  useEffect(() => {
    Promise.all([platformsApi.list(), productsApi.listAdmin()])
      .then(([platformResult, productResult]) => {
        setPlatforms([...platformResult].sort((a, b) =>
          (a.displayOrder ?? 0) - (b.displayOrder ?? 0) || a.name.localeCompare(b.name)))
        setProducts(productResult)
        setError(null)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.platforms.loadError')))
  }, [reload, t])

  const appCountFor = (platformId: number) =>
    products?.filter((p) => p.platforms.some((pl) => pl.id === platformId)).length ?? 0

  const statusFor = (platform: Platform) => {
    if (platform.status === 'INACTIVE') return { label: t('forms.inactive'), tone: 'warning' as const }
    if (platform.showInCatalog === false) return { label: t('forms.preview.hidden'), tone: 'neutral' as const }
    return { label: t('admin.platforms.inCatalog'), tone: 'success' as const }
  }

  const addButton = (
    <Button component={RouterLink} to="/admin/settings/platform" variant="contained" startIcon={<Plus size={16} />}>
      {t('catalog.addProduct')}
    </Button>
  )

  return (
    <>
      <PageHeader icon={PHLayers} area="catalog" title={t('appShell.nav.platforms')} subtitle={t('admin.platforms.subtitle')} action={addButton} />

      {error && <ErrorState title={t('admin.platforms.loadErrorTitle')} message={error} onRetry={() => setReload((n) => n + 1)} />}

      {!error && platforms === null && (
        <Grid container spacing={2.5}>
          {[0, 1, 2].map((i) => <Grid key={i} size={{ xs: 12, sm: 6, lg: 4, xl: 3 }}><Skeleton variant="rounded" height={220} /></Grid>)}
        </Grid>
      )}

      {platforms !== null && platforms.length === 0 && (
        <EmptyState title={t('admin.platforms.emptyTitle')} description={t('admin.platforms.emptyBody')} action={addButton} />
      )}

      {platforms !== null && platforms.length > 0 && (
        <Grid container spacing={2.5}>
          {platforms.map((platform) => (
            <Grid key={platform.id} size={{ xs: 12, sm: 6, lg: 4, xl: 3 }}>
              <ShowcaseCard
                name={platform.name}
                typeLabel={t('catalog.platformType')}
                logoUrl={platform.imageUrl}
                color={platform.primaryColor}
                meta={products ? t('catalog.appCount', { count: appCountFor(platform.id) }) : undefined}
                description={platform.description}
                status={statusFor(platform)}
                actionLabel={t('admin.platforms.explore')}
                to={`/admin/platforms/${platform.id}`}
                extra={(
                  <Box sx={{ mt: 1.5 }}>
                    <Button size="small" component={RouterLink} to={`/admin/platforms/${platform.id}/edit`}
                      startIcon={<Pencil size={14} />} aria-label={t('admin.platforms.editLabel', { name: platform.name })}>
                      {t('forms.edit')}
                    </Button>
                  </Box>
                )}
              />
            </Grid>
          ))}
        </Grid>
      )}
    </>
  )
}
