import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
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
    expect(await screen.findByText('Not configured')).toBeInTheDocument()
    expect(screen.getAllByText('Missing')).toHaveLength(3)
    expect(screen.getByRole('button', { name: 'Test connection' })).toBeDisabled()
    // C60 / BR-SEC-001: the steps name the secrets-file variables; no input for a key exists.
    expect(screen.getByText('RAZORPAY_KEY_SECRET=')).toBeInTheDocument()
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
  })

  it('masks the key id and lets a configured gateway be tested', async () => {
    gatewayStatus.mockResolvedValue({
      provider: 'Razorpay', configured: true, liveMode: false, maskedKeyId: 'rzp_test_••••••7890',
      keySecretSet: true, webhookSecretSet: true, webhookUrl: 'http://localhost:8081/api/webhooks/razorpay',
    })
    testGateway.mockResolvedValue(undefined)
    const { container } = renderWithProviders(<AdminPaymentGatewayPage />)
    expect(await screen.findByText('rzp_test_••••••7890')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Test connection' }))
    expect(await screen.findByText('Connected.')).toBeInTheDocument()
  })
})
