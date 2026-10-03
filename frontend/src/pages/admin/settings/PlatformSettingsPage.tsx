import { Layers as PHLayers } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../../../components/layout/PageHeader'
import { platformsApi } from '../../../api/platformsApi'
import { PlatformForm } from '../../../components/admin/PlatformForm'

/** "/admin/settings/platform" — Create Platform (C66 form with live preview). */
export function PlatformSettingsPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  return (
    <>
      <PageHeader icon={PHLayers} accent="indigo" eyebrow={t('forms.platform.label')} title={t('forms.platform.createTitle')}
        subtitle={t('forms.platform.createSubtitle')} />
      <PlatformForm
        onSubmit={(payload) => platformsApi.create(payload).then((created) => { navigate(`/admin/platforms/${created.id}`); return created })}
        submitLabel={t('forms.platform.create')}
        submittingLabel={t('forms.saving')}
        successMessage={t('forms.saved')}
        cancelTo="/admin/platforms"
      />
    </>
  )
}
