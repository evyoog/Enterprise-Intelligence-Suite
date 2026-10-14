import { useTranslation } from 'react-i18next'
import { Button } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { PlugZap } from 'lucide-react'
import { PageHeader } from '../../components/layout/PageHeader'
import { PaymentGatewayManagement } from '../../components/payments/PaymentGatewayManagement'

/** "/admin/billing/payment-gateway" — MANAGE_BILLING. C98 / REQ-BIL-003: provider-independent gateway management (configure, test, activate,
 * default, webhooks). Credentials are write-only; nothing here shows a secret. */
export function AdminPaymentGatewayPage() {
  const { t } = useTranslation()
  return (
    <>
      <PageHeader icon={PlugZap} accent="amber" eyebrow={t('admin.billingSettings.eyebrow')}
        title={t('admin.paymentGateway.title')} subtitle={t('admin.paymentGateway.subtitle')}
        action={<Button component={RouterLink} to="/admin/billing/settings?tab=methods" variant="outlined">{t('admin.billingSettings.tabs.methods')}</Button>} />
      <PaymentGatewayManagement />
    </>
  )
}
