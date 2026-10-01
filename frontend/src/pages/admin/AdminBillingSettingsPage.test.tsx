import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminBillingSettingsPage } from './AdminBillingSettingsPage'

const businessProfile = vi.fn()
const saveBusinessProfile = vi.fn()
const offlineBankDetails = vi.fn()
const saveOfflineBankDetails = vi.fn()
const paymentMethodSettings = vi.fn()
const savePaymentMethodSettings = vi.fn()
const gatewayStatus = vi.fn()

vi.mock('../../api/billingApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/billingApi')>('../../api/billingApi')
  return {
    ...actual,
    adminBillingApi: {
      ...actual.adminBillingApi,
      businessProfile: () => businessProfile(),
      saveBusinessProfile: (b: unknown) => saveBusinessProfile(b),
      offlineBankDetails: () => offlineBankDetails(),
      saveOfflineBankDetails: (b: unknown) => saveOfflineBankDetails(b),
      paymentMethodSettings: () => paymentMethodSettings(),
      savePaymentMethodSettings: (b: unknown) => savePaymentMethodSettings(b),
      gatewayStatus: () => gatewayStatus(),
      testGateway: () => Promise.resolve(),
    },
  }
})

const methods = { cardEnabled: true, upiEnabled: true, netbankingEnabled: true, walletEnabled: true, payByInvoiceEnabled: true }

function renderAt(tab?: string) {
  return renderWithProviders(<AdminBillingSettingsPage />, { route: tab ? `/admin/billing/settings?tab=${tab}` : '/admin/billing/settings' })
}

describe('AdminBillingSettingsPage (C55, C60)', () => {
  beforeEach(() => {
    for (const m of [businessProfile, saveBusinessProfile, offlineBankDetails, saveOfflineBankDetails, paymentMethodSettings, savePaymentMethodSettings, gatewayStatus]) m.mockReset()
    businessProfile.mockResolvedValue({ invoicePrefix: 'INV', paymentTermsDays: 0 })
    offlineBankDetails.mockResolvedValue({ bankTransferEnabled: true, neftRtgsEnabled: true, chequeEnabled: true })
    paymentMethodSettings.mockResolvedValue(methods)
    gatewayStatus.mockResolvedValue({ provider: 'Razorpay', configured: false, liveMode: false, keySecretSet: false, webhookSecretSet: false, webhookUrl: 'http://localhost:8081/api/webhooks/razorpay' })
  })

  it('shows the four overview tiles and the business tab, with no axe violations', async () => {
    const { container } = renderAt()
    expect(await screen.findByRole('textbox', { name: /Legal name/ })).toBeInTheDocument()
    for (const name of ['Business & invoicing', 'Offline payments', 'Payment methods', 'Razorpay gateway']) {
      expect(screen.getByRole('tab', { name })).toBeInTheDocument()
    }
    expect(await screen.findByText('5 of 5 on')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save changes' })).toBeDisabled()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('validates and saves the business profile, with the invoice preview following the form', async () => {
    saveBusinessProfile.mockImplementation(async (b) => ({ ...b, updatedAt: '2026-10-01T00:00:00Z' }))
    renderAt()
    const user = userEvent.setup()
    await user.type(await screen.findByRole('textbox', { name: /Legal name/ }), 'eVyoog Technologies')
    await user.type(screen.getByRole('textbox', { name: /GSTIN/ }), '12345')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))
    expect(screen.getByText(/GSTIN must be 15 characters/)).toBeInTheDocument()
    expect(saveBusinessProfile).not.toHaveBeenCalled()

    await user.clear(screen.getByRole('textbox', { name: /GSTIN/ }))
    await user.type(screen.getByRole('textbox', { name: /GSTIN/ }), '29abcde1234f1z5')
    await user.type(screen.getByRole('textbox', { name: /Address line 1/ }), '12 MG Road')
    await user.type(screen.getByRole('textbox', { name: /^City/ }), 'Bengaluru')
    await user.type(screen.getByRole('textbox', { name: /State/ }), 'Karnataka')
    await user.type(screen.getByRole('textbox', { name: /Postal code/ }), '560001')
    await user.type(screen.getByRole('textbox', { name: /^Country/ }), 'India')
    await user.type(screen.getByRole('textbox', { name: /Billing email/ }), 'billing@evyoog.example')
    const prefix = screen.getByRole('textbox', { name: /Invoice number prefix/ })
    await user.clear(prefix)
    await user.type(prefix, 'evy')
    await user.click(screen.getByRole('button', { name: 'Net 30' }))

    const preview = screen.getByRole('complementary', { name: 'Invoice preview' })
    expect(within(preview).getByText('eVyoog Technologies')).toBeInTheDocument()
    expect(within(preview).getByText(/^EVY-\d{4}-000123$/)).toBeInTheDocument()
    expect(within(preview).getByText('Net 30')).toBeInTheDocument()
    expect(within(preview).getByText('GSTIN 29ABCDE1234F1Z5')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Save changes' }))
    await waitFor(() => expect(saveBusinessProfile).toHaveBeenCalledWith(expect.objectContaining({
      legalName: 'eVyoog Technologies', gstin: '29ABCDE1234F1Z5', invoicePrefix: 'EVY', paymentTermsDays: 30,
    })))
    expect(await screen.findByText('Settings saved.')).toBeInTheDocument()
  }, 30000)

  it('saves the full offline details and needs at least one accepted method', async () => {
    saveOfflineBankDetails.mockImplementation(async (b) => ({ ...b, updatedAt: '2026-10-01T00:00:00Z' }))
    const { container } = renderAt('offline')
    const user = userEvent.setup()
    await user.type(await screen.findByRole('textbox', { name: /Account holder name/ }), 'eVyoog Technologies')
    await user.type(screen.getByRole('textbox', { name: /Bank name/ }), 'HDFC Bank')
    await user.type(screen.getByRole('textbox', { name: /Branch/ }), 'MG Road')
    await user.type(screen.getByRole('textbox', { name: /Account number/ }), '50200012345678')
    await user.type(screen.getByRole('textbox', { name: /IFSC/ }), 'hdfc0001234')
    await user.type(screen.getByRole('textbox', { name: /UPI ID/ }), 'evyoog@hdfcbank')
    await user.type(screen.getByRole('textbox', { name: /Cheques payable to/ }), 'eVyoog Technologies')
    expect(within(screen.getByRole('complementary', { name: 'What customers see' })).getByText('HDFC0001234')).toBeInTheDocument()

    for (const name of ['Bank transfer', 'NEFT / RTGS', 'Cheque']) await user.click(screen.getByRole('switch', { name }))
    await user.click(screen.getByRole('button', { name: 'Save changes' }))
    expect(screen.getByText('Accept at least one offline payment method.')).toBeInTheDocument()
    expect(saveOfflineBankDetails).not.toHaveBeenCalled()

    await user.click(screen.getByRole('switch', { name: 'Bank transfer' }))
    await user.click(screen.getByRole('button', { name: 'Save changes' }))
    await waitFor(() => expect(saveOfflineBankDetails).toHaveBeenCalledWith(expect.objectContaining({
      branchName: 'MG Road', ifsc: 'HDFC0001234', upiId: 'evyoog@hdfcbank', bankTransferEnabled: true, neftRtgsEnabled: false, chequeEnabled: false,
    })))
    expect(await axe(container)).toHaveNoViolations()
  }, 30000)

  it('switches payment methods and checkout appearance, refusing all methods off', async () => {
    savePaymentMethodSettings.mockImplementation(async (b) => ({ ...b, updatedAt: '2026-10-01T00:00:00Z' }))
    renderAt('methods')
    const user = userEvent.setup()
    await user.click(await screen.findByRole('switch', { name: 'Wallets' }))
    await user.type(screen.getByRole('textbox', { name: /Name shown in Razorpay Checkout/ }), 'eVyoog Store')
    await user.type(screen.getByRole('textbox', { name: /Checkout colour/ }), '#0F766E')
    const preview = screen.getByRole('complementary', { name: 'Checkout preview' })
    expect(within(preview).getByText('eVyoog Store')).toBeInTheDocument()
    expect(within(preview).queryByText('Wallets')).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Save changes' }))
    await waitFor(() => expect(savePaymentMethodSettings).toHaveBeenCalledWith(expect.objectContaining({
      walletEnabled: false, cardEnabled: true, checkoutDisplayName: 'eVyoog Store', checkoutThemeColor: '#0F766E',
    })))

    for (const name of ['Cards', 'UPI', 'Netbanking', 'Pay by invoice']) await user.click(screen.getByRole('switch', { name }))
    await user.click(screen.getByRole('button', { name: 'Save changes' }))
    expect(screen.getByText('Keep at least one payment method on, or customers cannot pay.')).toBeInTheDocument()
  }, 30000)

  it('shows the Razorpay credential status without any key input (BR-SEC-001)', async () => {
    renderAt('gateway')
    expect(await screen.findByText('RAZORPAY_KEY_ID=')).toBeInTheDocument()
    expect(screen.getAllByText('Missing')).toHaveLength(3)
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Test connection' })).toBeDisabled()
  })

  it('shows a load error with Retry', async () => {
    businessProfile.mockRejectedValueOnce(new Error('boom'))
    renderAt()
    expect(await screen.findByText('Could not load the billing settings.')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByRole('textbox', { name: /Legal name/ })).toBeInTheDocument()
  })
})
