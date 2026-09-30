import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminPaymentGatewayPage } from './AdminPaymentGatewayPage'

const gatewayStatus = vi.fn()
const testGateway = vi.fn()

vi.mock('../../api/billingApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/billingApi')>('../../api/billingApi')
  return { ...actual, adminBillingApi: { ...actual.adminBillingApi, gatewayStatus: () => gatewayStatus(), testGateway: () => testGateway() } }
})

describe('AdminPaymentGatewayPage', () => {
  beforeEach(() => {
    gatewayStatus.mockReset()
    testGateway.mockReset()
  })

  it('shows Not configured guidance and never a secret value when not configured', async () => {
    gatewayStatus.mockResolvedValue({
      provider: 'Razorpay', configured: false, liveMode: false, maskedKeyId: undefined,
      keySecretSet: false, webhookSecretSet: false, webhookUrl: 'http://localhost:8081/api/webhooks/razorpay',
    })
    renderWithProviders(<AdminPaymentGatewayPage />)
    expect(await screen.findByText('Add the Razorpay credentials to the common secrets file, then restart the backend.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Test connection' })).toBeDisabled()
  })

  it('masks the key id and lets a configured gateway be tested', async () => {
    gatewayStatus.mockResolvedValue({
      provider: 'Razorpay', configured: true, liveMode: false, maskedKeyId: 'rzp_test_••••••7890',
      keySecretSet: true, webhookSecretSet: true, webhookUrl: 'http://localhost:8081/api/webhooks/razorpay',
    })
    testGateway.mockResolvedValue(undefined)
    renderWithProviders(<AdminPaymentGatewayPage />)
    expect(await screen.findByText('rzp_test_••••••7890')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Test connection' }))
    expect(await screen.findByText('Connected.')).toBeInTheDocument()
  })
})
