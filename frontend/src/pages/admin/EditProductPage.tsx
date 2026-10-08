import { Package as PHPackage } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Breadcrumbs, Button, Link, Skeleton, Tab, Tabs, Tooltip, Typography } from '@mui/material'
import { Link as RouterLink, useLocation, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { productsApi, type Product } from '../../api/productsApi'
import { ProductForm } from '../../components/admin/ProductForm'
import { useTabParam } from '../../components/layout/useTabParam'
import { ProductContentManager } from '../../components/productcontent/ProductContentManager'
import { PageHeader } from '../../components/layout/PageHeader'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { ErrorState } from '../../components/ui/ErrorState'
import { StatusBadge } from '../../components/ui/StatusBadge'

const STATUS_TONE = { ACTIVE: 'success', INACTIVE: 'warning', RETIRED: 'neutral' } as const

/**
 * "/admin/products/:id/edit" — Edit App (C66). Publish / Retire stay the
 * dedicated lifecycle actions (02.01.01.04/.05, sprint 2026.4.1); Retire now
 * asks for confirmation. `version` is the backend's edit counter, so it is
 * labelled as a revision, not a release version.
 */
export function EditProductPage() {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const location = useLocation()
  const justCreated = (location.state as { created?: boolean } | null)?.created === true
  const [product, setProduct] = useState<Product | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [reload, setReload] = useState(0)
  const [isChangingStatus, setIsChangingStatus] = useState(false)
  const [statusError, setStatusError] = useState<string | null>(null)
  const [confirmRetire, setConfirmRetire] = useState(false)
  // REQ-CAT-004: the application's details form and its product content are two tabs (?tab=content).
  const [tab, setTab] = useTabParam(['details', 'content'] as const, 'details')

  useEffect(() => {
    if (!id) return
    productsApi.get(Number(id))
      .then((p) => { setProduct(p); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('forms.app.loadError')))
  }, [id, reload, t])

  const changeStatus = (action: 'publish' | 'retire') => {
    if (!product) return
    setIsChangingStatus(true)
    setStatusError(null)
    productsApi[action](product.id)
      .then(setProduct)
      .catch((e) => setStatusError(e instanceof ApiError ? e.message : t('forms.app.statusError')))
      .finally(() => { setIsChangingStatus(false); setConfirmRetire(false) })
  }

  const lifecycle = product && (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
      <StatusBadge label={t(`forms.app.status.${product.status}`)} tone={STATUS_TONE[product.status]} />
      <Tooltip title={t('forms.app.revisionHint')}>
        <Typography variant="body2" sx={{ color: 'text.secondary' }} tabIndex={0}>{t('forms.app.revision', { n: product.version })}</Typography>
      </Tooltip>
      {product.status !== 'ACTIVE' && (
        <Button size="small" variant="outlined" disabled={isChangingStatus} onClick={() => changeStatus('publish')}>
          {t('forms.app.publish')}
        </Button>
      )}
      {product.status !== 'RETIRED' && (
        <Button size="small" color="error" disabled={isChangingStatus} onClick={() => setConfirmRetire(true)}>
          {t('forms.app.retire')}
        </Button>
      )}
    </Box>
  )

  return (
    <>
      <Breadcrumbs sx={{ mb: 2 }} aria-label={t('catalog.detail.breadcrumbs')}>
        <Link component={RouterLink} to="/admin/apps" underline="hover" color="inherit">{t('admin.apps.title')}</Link>
        {product && <Typography color="text.secondary">{product.name}</Typography>}
        <Typography color="text.primary">{t('forms.edit')}</Typography>
      </Breadcrumbs>

      <PageHeader icon={PHPackage} eyebrow={t('forms.app.label')} title={t('forms.app.editTitle')}
        subtitle={t('forms.app.editSubtitle')} action={lifecycle || undefined} />

      {justCreated && product && <Alert severity="success" role="status" sx={{ mb: 2 }}>{t('forms.app.created')}</Alert>}
      {statusError && <Alert severity="error" sx={{ mb: 2 }}>{statusError}</Alert>}
      {error && <ErrorState title={t('forms.app.loadError')} message={error} onRetry={() => setReload((n) => n + 1)} />}

      {!error && !product && (
        <Box aria-busy="true" sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {[0, 1, 2].map((i) => <Skeleton key={i} variant="rounded" height={180} />)}
        </Box>
      )}

      {product && (
        <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('productContent.tabs.label')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
          <Tab value="details" label={t('productContent.tabs.details')} />
          <Tab value="content" label={t('productContent.tabs.content')} />
        </Tabs>
      )}

      {product && tab === 'content' && <ProductContentManager productId={product.id} />}

      {product && tab === 'details' && (
        <ProductForm
          key={product.id}
          initialProduct={product}
          onSubmit={(payload) => productsApi.update(product.id, payload).then((updated) => { setProduct(updated); return updated })}
          submitLabel={t('forms.saveChanges')}
          submittingLabel={t('forms.saving')}
          successMessage={t('forms.app.updated')}
          cancelTo="/admin/apps"
        />
      )}

      <ConfirmDialog open={confirmRetire} title={t('forms.app.retireTitle')} body={t('forms.app.retireBody')}
        confirmLabel={t('forms.app.retire')} busy={isChangingStatus}
        onConfirm={() => changeStatus('retire')} onClose={() => setConfirmRetire(false)} />
    </>
  )
}
