import { Package as PHPackage } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../../../components/layout/PageHeader'
import { productsApi } from '../../../api/productsApi'
import { ProductForm } from '../../../components/admin/ProductForm'

/** "/admin/settings/product" — Create App (C66 sectioned form with live preview). */
export function ProductSettingsPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  return (
    <>
      <PageHeader icon={PHPackage} eyebrow={t('forms.app.label')} title={t('forms.app.createTitle')} subtitle={t('forms.app.createSubtitle')} />
      <ProductForm
        onSubmit={(payload) => productsApi.create(payload).then((created) => {
          navigate(`/admin/products/${created.id}/edit`, { state: { created: true } })
          return created
        })}
        submitLabel={t('forms.app.create')}
        submittingLabel={t('forms.app.creating')}
        successMessage={t('forms.app.created')}
        cancelTo="/admin/apps"
      />
    </>
  )
}
