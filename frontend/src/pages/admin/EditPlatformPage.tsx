import { Layers as PHLayers } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Breadcrumbs, Link, Skeleton, Typography } from '@mui/material'
import { useParams, Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { platformsApi, type Platform } from '../../api/platformsApi'
import { productsApi } from '../../api/productsApi'
import { PlatformForm } from '../../components/admin/PlatformForm'
import { PageHeader } from '../../components/layout/PageHeader'
import { ErrorState } from '../../components/ui/ErrorState'

/** "/admin/platforms/:id/edit" — Edit Platform (same C66 form as create). */
export function EditPlatformPage() {
  const { id } = useParams<{ id: string }>()
  const { t } = useTranslation()
  const [platform, setPlatform] = useState<Platform | null>(null)
  const [appCount, setAppCount] = useState<number | undefined>(undefined)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    platformsApi.get(Number(id))
      .then(setPlatform)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('forms.platform.loadError')))
    productsApi.listAdmin()
      .then((apps) => setAppCount(apps.filter((a) => a.platforms.some((p) => p.id === Number(id))).length))
      .catch(() => setAppCount(undefined))
  }, [id, t])

  return (
    <>
      <Breadcrumbs aria-label={t('catalog.detail.breadcrumbs')} sx={{ mb: 2 }}>
        <Link component={RouterLink} to="/admin/platforms" underline="hover" color="inherit">{t('appShell.nav.platforms')}</Link>
        <Link component={RouterLink} to={`/admin/platforms/${id}`} underline="hover" color="inherit">{platform?.name ?? '…'}</Link>
        <Typography color="text.primary">{t('forms.edit')}</Typography>
      </Breadcrumbs>
      <PageHeader icon={PHLayers} accent="indigo" eyebrow={t('forms.platform.label')} title={t('forms.platform.editTitle')}
        subtitle={t('forms.platform.editSubtitle')} />
      {error && <ErrorState title={t('forms.platform.loadError')} message={error} />}
      {!error && !platform && <Skeleton variant="rounded" height={420} />}
      {platform && (
        <PlatformForm
          initialPlatform={platform}
          onSubmit={(payload) => platformsApi.update(platform.id, payload).then((p) => { setPlatform(p); return p })}
          submitLabel={t('forms.saveChanges')}
          submittingLabel={t('forms.saving')}
          successMessage={t('forms.saved')}
          cancelTo={`/admin/platforms/${platform.id}`}
          previewAppCount={appCount}
        />
      )}
    </>
  )
}
