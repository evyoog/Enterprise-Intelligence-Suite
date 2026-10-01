import { useTranslation } from 'react-i18next'
import { Button } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { PlugZap } from 'lucide-react'
import { PageHeader } from '../../components/layout/PageHeader'
import { RazorpayGatewayPanel } from '../../components/settings/RazorpayGatewayPanel'

/** "/admin/billing/payment-gateway" — MANAGE_BILLING, read-only
 * (REQ-BIL-001.13/.14, BR-SEC-001). Never shows or edits a secret. C60: the
 * same panel as Billing settings → Razorpay gateway. */
export function AdminPaymentGatewayPage() {
  const { t } = useTranslation()
  return (
    <>
      <PageHeader icon={PlugZap} accent="amber" eyebrow={t('admin.billingSettings.eyebrow')}
        title={t('admin.paymentGateway.title')} subtitle={t('admin.paymentGateway.subtitle')}
        action={<Button component={RouterLink} to="/admin/billing/settings?tab=methods" variant="outlined">{t('admin.billingSettings.tabs.methods')}</Button>} />
      <RazorpayGatewayPanel />
    </>
  )
}
