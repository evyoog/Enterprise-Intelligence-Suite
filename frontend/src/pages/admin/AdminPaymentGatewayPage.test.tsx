import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import type { Gateway, GatewayDetail } from '../../api/paymentGatewayApi'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminPaymentGatewayPage } from './AdminPaymentGatewayPage'

const list = vi.fn()
const summary = vi.fn()
const detail = vi.fn()
const saveConfiguration = vi.fn()
const test = vi.fn()
const activate = vi.fn()
const deactivate = vi.fn()
const setMethods = vi.fn()
const setDefault = vi.fn()
const templates = vi.fn()
const create = vi.fn()
const updateDefinition = vi.fn()
const remove = vi.fn()

vi.mock('../../api/paymentGatewayApi', () => ({
  paymentGatewayApi: {
    list: () => list(), summary: () => summary(), detail: (c: string) => detail(c),
    saveConfiguration: (c: string, b: unknown) => saveConfiguration(c, b), test: (c: string) => test(c),
    activate: (c: string) => activate(c), deactivate: (c: string) => deactivate(c),
    setMethods: (c: string, m: string[]) => setMethods(c, m), setDefault: (c: string) => setDefault(c),
    templates: () => templates(), create: (b: unknown) => create(b), updateDefinition: (c: string, d: unknown) => updateDefinition(c, d), remove: (c: string) => remove(c),
  },
}))

const base: Gateway = {
  code: 'RAZORPAY', name: 'Razorpay', description: 'Cards, UPI and netbanking.', logo: 'razorpay', implementation: 'IMPLEMENTED', custom: false,
  supportedMethods: ['card', 'upi', 'netbanking', 'wallet'], enabledMethods: ['card', 'upi', 'netbanking', 'wallet'], currencies: ['INR', 'USD'], countries: ['IN'],
  environment: 'TEST', configurationStatus: 'CONFIGURED', connectionStatus: 'CONNECTION_SUCCESSFUL', connectionMessage: 'Razorpay accepted the credentials.',
  testedAt: '2026-10-14T05:00:00Z', activated: true, defaultGateway: true,
  actions: { configure: true, test: true, activate: false, deactivate: true, setDefault: false, edit: false, remove: false },
}
const second: Gateway = {
  ...base, code: 'FAKEPAY', name: 'FakePay', logo: 'fakepay', environment: undefined, configurationStatus: 'NOT_CONFIGURED', connectionStatus: 'NOT_CONFIGURED',
  connectionMessage: undefined, testedAt: undefined, activated: false, defaultGateway: false, supportedMethods: ['card'], enabledMethods: ['card'], currencies: ['INR'],
  actions: { configure: true, test: false, activate: false, deactivate: false, setDefault: false, edit: false, remove: false },
}
const added: Gateway = {
  ...base, code: 'ACME', name: 'Acme Pay', logo: 'acme', implementation: 'CONFIGURATION_ONLY', custom: true, configurationStatus: 'CONFIGURED', connectionStatus: 'NOT_TESTED',
  connectionMessage: 'No payment integration exists for this gateway, so it cannot be tested or activated.', testedAt: undefined, activated: false, defaultGateway: false,
  supportedMethods: ['card'], enabledMethods: ['card'], currencies: ['INR'],
  actions: { configure: true, test: false, activate: false, deactivate: false, setDefault: false, edit: true, remove: true },
}
const sum = { registered: 3, implemented: 2, configured: 1, active: 1, needsConfiguration: 1 }

const detailOf = (g: Gateway, over: Partial<GatewayDetail> = {}): GatewayDetail => ({
  gateway: g, transactionTypes: ['INVOICE_PAYMENT'], environments: ['TEST', 'LIVE'], webhookSupport: true, refundSupport: true, recurringSupport: false,
  fields: [
    { key: 'keyId', label: 'Key ID', secret: false, required: true, webhook: false, help: 'Starts with rzp_test_', configured: true, source: 'STORED', maskedValue: 'rzp_test_••••••7890' },
    { key: 'keySecret', label: 'Key secret', secret: true, required: true, webhook: false, configured: true, source: 'STORED' },
    { key: 'webhookSecret', label: 'Webhook secret', secret: true, required: true, webhook: true, configured: false, source: 'NONE' },
  ],
  setupSteps: ['Create a key.', 'Add the webhook.'],
  webhook: { url: 'https://api.example.com/webhooks/razorpay', supported: true, secretConfigured: false, signatureStatus: 'NOT_SEEN', failureCount: 2, events: ['payment.captured'], reachableFromInternet: true,
    lastFailureAt: '2026-10-14T04:00:00Z', lastFailureReason: 'The webhook signature did not verify.' },
  evidence: { authenticated: true, webhookReceived: false, paymentCaptured: true, refundMade: false },
  configVersion: 3, encryptionAvailable: true, credentialsUnreadable: false, recentEvents: [], history: [{ at: '2026-10-14T03:00:00Z', action: 'PAYMENT_GATEWAY_CONFIGURED', detail: 'Configuration saved', actor: 'admin-sub' }],
  ...over,
})

const tpls = [{
  code: 'STRIPE', name: 'Stripe', description: 'Cards worldwide.', logo: 'stripe', countries: ['US'], currencies: ['USD', 'EUR'], methods: ['card', 'wallet'],
  fields: [{ key: 'publishableKey', label: 'Publishable key', secret: false, required: true }, { key: 'secretKey', label: 'Secret key', secret: true, required: true }],
}]

describe('AdminPaymentGatewayPage (C98, C99, REQ-BIL-003)', () => {
  beforeEach(() => {
    for (const m of [list, summary, detail, saveConfiguration, test, activate, deactivate, setMethods, setDefault, templates, create, updateDefinition, remove]) m.mockReset()
    list.mockResolvedValue([base, second, added])
    templates.mockResolvedValue(tpls)
    summary.mockResolvedValue(sum)
    detail.mockImplementation(async (code: string) => detailOf(code === 'FAKEPAY' ? second : code === 'ACME' ? added : base))
  })

  it('shows the overview from the backend and each provider with only the actions valid for its state, with no axe violations', async () => {
    const { container } = renderWithProviders(<AdminPaymentGatewayPage />)

    expect(await screen.findByRole('heading', { name: 'Payment Gateways' })).toBeInTheDocument()
    expect(screen.getByText('Configure and manage payment providers used to collect subscription payments across EIS.')).toBeInTheDocument()
    const stats = screen.getByText('Registered gateways').closest('div')!.parentElement!
    expect(within(stats).getByText('3')).toBeInTheDocument()
    expect(within(stats).getByText('Need configuration')).toBeInTheDocument()

    const razorpay = screen.getByRole('listitem', { name: 'Razorpay' })
    expect(within(razorpay).getByText('Connection successful')).toBeInTheDocument()
    expect(within(razorpay).getByText('Active')).toBeInTheDocument()
    expect(within(razorpay).getByText('Default')).toBeInTheDocument()
    expect(within(razorpay).getByText('Test')).toBeInTheDocument()
    expect(within(razorpay).getByRole('button', { name: 'Deactivate Razorpay' })).toBeInTheDocument()
    expect(within(razorpay).queryByRole('button', { name: 'Activate Razorpay' })).not.toBeInTheDocument()
    expect(within(razorpay).queryByRole('button', { name: /Make Razorpay the default/ })).not.toBeInTheDocument()

    const fake = screen.getByRole('listitem', { name: 'FakePay' })
    expect(within(fake).getByText('Not configured')).toBeInTheDocument()
    expect(within(fake).getByRole('button', { name: 'Configure FakePay' })).toBeInTheDocument()
    expect(within(fake).queryByRole('button', { name: /Test the FakePay/ })).not.toBeInTheDocument()
    expect(within(fake).queryByRole('button', { name: /Activate/ })).not.toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('labels a gateway added by an administrator as having no payment integration and offers only what works', async () => {
    renderWithProviders(<AdminPaymentGatewayPage />)
    const acme = await screen.findByRole('listitem', { name: 'Acme Pay' })

    expect(within(acme).getByText('No payment integration')).toBeInTheDocument()
    expect(within(acme).getByText('Configured')).toBeInTheDocument()
    expect(within(acme).getByText(/EIS has no payment integration for this gateway/)).toBeInTheDocument()
    expect(within(acme).getByRole('button', { name: 'Configure Acme Pay' })).toBeInTheDocument()
    expect(within(acme).getByRole('button', { name: 'Edit Acme Pay' })).toBeInTheDocument()
    expect(within(acme).getByRole('button', { name: 'Remove Acme Pay' })).toBeInTheDocument()
    expect(within(acme).queryByRole('button', { name: /Test the|Activate|default/ })).not.toBeInTheDocument()
    expect(screen.queryByText(/Planned integrations/)).not.toBeInTheDocument()
    await userEvent.setup().click(within(acme).getByRole('button', { name: 'View details of Acme Pay' }))
    const dialog = await screen.findByRole('dialog', { name: /Acme Pay/ })
    expect(within(dialog).getByText(/Acme Pay was added by an administrator/)).toBeInTheDocument()
    expect(within(dialog).queryByRole('tab', { name: 'Webhook' })).not.toBeInTheDocument()
    expect(within(dialog).queryByText('Verified by real activity')).not.toBeInTheDocument()
  })

  it('adds a gateway from the form: pre-fills from a template, hides secrets, sends the definition and values once', async () => {
    create.mockResolvedValue(detailOf(added))
    const { container } = renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await screen.findByRole('listitem', { name: 'Razorpay' })

    await user.click(screen.getByRole('button', { name: 'Add payment gateway' }))
    const dialog = await screen.findByRole('dialog', { name: 'Add payment gateway' })
    expect(within(dialog).getByText(/stored encrypted in the database and are never shown again/)).toBeInTheDocument()
    expect(within(dialog).getByText(/no payment integration for a gateway added here/i)).toBeInTheDocument()
    expect(within(dialog).getByRole('button', { name: 'Add gateway' })).toBeDisabled()
    expect(await axe(container)).toHaveNoViolations()

    await user.click(within(dialog).getByRole('combobox', { name: 'Start from' }))
    await user.click(await screen.findByRole('option', { name: 'Stripe' }))
    expect(within(dialog).getByLabelText('Gateway name', { exact: false })).toHaveValue('Stripe')
    expect(within(dialog).getByLabelText('Gateway code', { exact: false })).toHaveValue('STRIPE')
    expect(within(dialog).getByLabelText('Currencies', { exact: false })).toHaveValue('USD, EUR')
    const groups = within(dialog).getAllByRole('group', { name: /Credential \d/ })
    expect(groups).toHaveLength(2)
    const secretValue = within(groups[1]).getByLabelText('Value') as HTMLInputElement
    expect(secretValue.type).toBe('password')

    await user.type(within(groups[0]).getByLabelText('Value'), 'pk_test_123')
    await user.type(secretValue, 'sk_test_secret')
    await user.click(within(dialog).getByRole('button', { name: 'Add gateway' }))

    await waitFor(() => expect(create).toHaveBeenCalledTimes(1))
    expect(create).toHaveBeenCalledWith({
      definition: {
        code: 'STRIPE', name: 'Stripe', description: 'Cards worldwide.', logo: 'stripe', countries: ['US'], currencies: ['USD', 'EUR'], methods: ['card', 'wallet'],
        fields: [{ key: 'publishableKey', label: 'Publishable key', secret: false, required: true }, { key: 'secretKey', label: 'Secret key', secret: true, required: true }],
      },
      environment: 'TEST',
      values: { publishableKey: 'pk_test_123', secretKey: 'sk_test_secret' },
    })
    expect(await screen.findByText('Stripe was added.')).toBeInTheDocument()
    await waitFor(() => expect(list.mock.calls.length).toBeGreaterThan(1))
  })

  it('adds a gateway from scratch: derives the code and credential names and keeps the Add button off until the form is valid', async () => {
    create.mockResolvedValue(detailOf(added))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Add payment gateway' }))
    const dialog = await screen.findByRole('dialog', { name: 'Add payment gateway' })
    const add = within(dialog).getByRole('button', { name: 'Add gateway' })

    await user.type(within(dialog).getByLabelText('Gateway name', { exact: false }), 'My Local Bank')
    expect(within(dialog).getByLabelText('Gateway code', { exact: false })).toHaveValue('MY_LOCAL_BANK')
    expect(add).toBeDisabled()
    expect(within(dialog).getByText('Every credential needs a label and a name (letters and digits, starting with a letter).')).toBeInTheDocument()

    await user.type(within(dialog).getByLabelText('Label', { exact: false }), 'API secret key')
    expect(within(dialog).getByLabelText(/^Name/)).toHaveValue('apiSecretKey')
    expect(add).toBeEnabled()
    await user.click(within(dialog).getByRole('button', { name: 'Add credential' }))
    expect(within(dialog).getAllByRole('group', { name: /Credential \d/ })).toHaveLength(2)
    await user.click(within(dialog).getByRole('button', { name: 'Remove credential 2' }))
    await user.click(add)

    await waitFor(() => expect(create).toHaveBeenCalledWith({
      definition: expect.objectContaining({ code: 'MY_LOCAL_BANK', name: 'My Local Bank', currencies: ['INR'], methods: ['card'],
        fields: [{ key: 'apiSecretKey', label: 'API secret key', secret: true, required: true }] }),
      environment: 'TEST',
      values: {},
    }))
  })

  it('shows the server message when adding is refused', async () => {
    create.mockRejectedValue(new ApiError(409, 'A payment gateway with the code ACME already exists.'))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Add payment gateway' }))
    const dialog = await screen.findByRole('dialog', { name: 'Add payment gateway' })
    await user.type(within(dialog).getByLabelText('Gateway name', { exact: false }), 'Acme')
    await user.type(within(dialog).getByLabelText('Label', { exact: false }), 'Key')
    await user.click(within(dialog).getByRole('button', { name: 'Add gateway' }))

    expect(await within(dialog).findByText('A payment gateway with the code ACME already exists.')).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Add payment gateway' })).toBeInTheDocument()
  })

  it('edits the definition of an added gateway without asking for credential values, and removes it after confirmation', async () => {
    updateDefinition.mockResolvedValue(detailOf(added))
    remove.mockResolvedValue(undefined)
    detail.mockImplementation(async () => detailOf(added, { fields: [{ key: 'apiKey', label: 'API key', secret: true, required: true, webhook: false, configured: true, source: 'STORED' }] }))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await user.click(within(await screen.findByRole('listitem', { name: 'Acme Pay' })).getByRole('button', { name: 'Edit Acme Pay' }))
    const dialog = await screen.findByRole('dialog', { name: 'Edit payment gateway' })

    await waitFor(() => expect(within(dialog).getByLabelText('Gateway name', { exact: false })).toHaveValue('Acme Pay'))
    expect(within(dialog).getByLabelText('Gateway code', { exact: false })).toBeDisabled()
    expect(within(dialog).queryByLabelText('Value')).not.toBeInTheDocument()
    expect(within(dialog).getByLabelText(/^Name/)).toBeDisabled()
    await user.clear(within(dialog).getByLabelText('Gateway name', { exact: false }))
    await user.type(within(dialog).getByLabelText('Gateway name', { exact: false }), 'Acme Payments')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(updateDefinition).toHaveBeenCalledWith('ACME', expect.objectContaining({ code: 'ACME', name: 'Acme Payments',
      fields: [{ key: 'apiKey', label: 'API key', secret: true, required: true }] })))
    expect(await screen.findByText('Acme Payments was updated.')).toBeInTheDocument()

    await user.click(within(screen.getByRole('listitem', { name: 'Acme Pay' })).getByRole('button', { name: 'Remove Acme Pay' }))
    const confirm = await screen.findByRole('dialog', { name: 'Remove Acme Pay?' })
    expect(within(confirm).getByText(/stored credentials are deleted/)).toBeInTheDocument()
    await user.click(within(confirm).getByRole('button', { name: 'Remove' }))
    await waitFor(() => expect(remove).toHaveBeenCalledWith('ACME'))
    expect(await screen.findByText('Acme Pay was removed.')).toBeInTheDocument()
  })

  it('shows the real outcome of a connection test and reloads the list', async () => {
    test.mockResolvedValue({ status: 'AUTHENTICATION_FAILED', message: 'Razorpay refused the credentials (HTTP 401).', successful: false })
    renderWithProviders(<AdminPaymentGatewayPage />)
    const razorpay = await screen.findByRole('listitem', { name: 'Razorpay' })

    await userEvent.setup().click(within(razorpay).getByRole('button', { name: 'Test the Razorpay connection' }))

    expect(await within(razorpay).findByText(/Authentication failed — Razorpay refused the credentials/)).toBeInTheDocument()
    expect(test).toHaveBeenCalledWith('RAZORPAY')
    await waitFor(() => expect(list.mock.calls.length).toBeGreaterThan(1))
  })

  it('activates a configured gateway', async () => {
    const ready: Gateway = { ...second, configurationStatus: 'CONFIGURED', connectionStatus: 'NOT_TESTED', actions: { ...second.actions, test: true, activate: true } }
    list.mockResolvedValue([base, ready])
    activate.mockResolvedValue(detailOf(ready))
    renderWithProviders(<AdminPaymentGatewayPage />)

    await userEvent.setup().click(within(await screen.findByRole('listitem', { name: 'FakePay' })).getByRole('button', { name: 'Activate FakePay' }))

    await waitFor(() => expect(activate).toHaveBeenCalledWith('FAKEPAY'))
    expect(await screen.findByText('FakePay is active.')).toBeInTheDocument()
  })

  it('makes another active gateway the default and deactivates one', async () => {
    const other: Gateway = { ...second, configurationStatus: 'CONFIGURED', connectionStatus: 'CONNECTION_SUCCESSFUL', activated: true, defaultGateway: true,
      actions: { configure: true, test: true, activate: false, deactivate: true, setDefault: false, edit: false, remove: false } }
    list.mockResolvedValue([{ ...base, defaultGateway: false, actions: { ...base.actions, setDefault: true } }, other])
    setDefault.mockResolvedValue([])
    deactivate.mockResolvedValue(detailOf(base))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()

    await user.click(await screen.findByRole('button', { name: 'Make Razorpay the default gateway' }))
    await waitFor(() => expect(setDefault).toHaveBeenCalledWith('RAZORPAY'))
    expect(await screen.findByText('Razorpay is now the default for new payments.')).toBeInTheDocument()

    await user.click(within(screen.getByRole('listitem', { name: 'FakePay' })).getByRole('button', { name: 'Deactivate FakePay' }))
    await waitFor(() => expect(deactivate).toHaveBeenCalledWith('FAKEPAY'))
  })

  it('shows the server message when activation is refused', async () => {
    const ready: Gateway = { ...second, configurationStatus: 'CONFIGURED', connectionStatus: 'NOT_TESTED', actions: { ...second.actions, test: true, activate: true } }
    list.mockResolvedValue([ready])
    activate.mockRejectedValue(new ApiError(409, 'FakePay was not activated: FakePay said AUTHENTICATION_FAILED'))
    renderWithProviders(<AdminPaymentGatewayPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Activate FakePay' }))

    expect(await screen.findByText(/FakePay was not activated/)).toBeInTheDocument()
  })

  it('builds the configuration form from the provider fields, keeps secrets write-only and sends only what was typed', async () => {
    saveConfiguration.mockResolvedValue(detailOf(base))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await user.click(within(await screen.findByRole('listitem', { name: 'Razorpay' })).getByRole('button', { name: 'Configure Razorpay' }))

    const dialog = await screen.findByRole('dialog', { name: 'Configure Razorpay' })
    expect(within(dialog).getByText(/This gateway is active, so the new credentials are tested first/)).toBeInTheDocument()
    expect(within(dialog).getByText(/Current: rzp_test_••••••7890/)).toBeInTheDocument()
    const secret = within(dialog).getByLabelText('Key secret', { exact: false }) as HTMLInputElement
    expect(secret.type).toBe('password')
    expect(secret.value).toBe('')
    expect(within(dialog).getByRole('button', { name: 'Save' })).toBeDisabled()

    await user.type(within(dialog).getByLabelText('Webhook secret', { exact: false }), 'whsec_new')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))

    await waitFor(() => expect(saveConfiguration).toHaveBeenCalledWith('RAZORPAY', { environment: 'TEST', fields: { webhookSecret: 'whsec_new' } }))
    expect(await screen.findByText('Razorpay configuration saved.')).toBeInTheDocument()
  })

  it('shows a refused save and says when the server cannot store credentials', async () => {
    saveConfiguration.mockRejectedValue(new ApiError(409, 'Not saved: AUTHENTICATION_FAILED: nope. The previous configuration is unchanged.'))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await user.click(within(await screen.findByRole('listitem', { name: 'Razorpay' })).getByRole('button', { name: 'Configure Razorpay' }))
    const dialog = await screen.findByRole('dialog', { name: 'Configure Razorpay' })
    await user.type(within(dialog).getByLabelText('Key secret', { exact: false }), 'typo')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(await within(dialog).findByText(/The previous configuration is unchanged/)).toBeInTheDocument()
  })

  it('refuses to save when the server has no encryption key', async () => {
    detail.mockResolvedValue(detailOf(second, { encryptionAvailable: false }))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await user.click(within(await screen.findByRole('listitem', { name: 'FakePay' })).getByRole('button', { name: 'Configure FakePay' }))
    const dialog = await screen.findByRole('dialog', { name: 'Configure FakePay' })

    expect(within(dialog).getByText(/server has no encryption key/)).toBeInTheDocument()
    await user.type(within(dialog).getByLabelText('Key ID', { exact: false }), 'abc')
    expect(within(dialog).getByRole('button', { name: 'Save' })).toBeDisabled()
  })

  it('shows capabilities, separate evidence, the webhook set-up and recent failures in the details', async () => {
    setMethods.mockImplementation(async (_c: string, methods: string[]) => detailOf({ ...base, enabledMethods: methods }))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await user.click(within(await screen.findByRole('listitem', { name: 'Razorpay' })).getByRole('button', { name: 'View details of Razorpay' }))
    const dialog = await screen.findByRole('dialog', { name: /Razorpay/ })

    const evidenceOf = (label: string) => within(dialog).getByText(new RegExp(label)).closest('li')
    expect(evidenceOf('Credentials accepted')).toHaveTextContent('Yes')
    expect(evidenceOf('Signed webhook received')).toHaveTextContent('Not yet')
    expect(evidenceOf('Payment captured')).toHaveTextContent('Yes')
    expect(evidenceOf('Refund made')).toHaveTextContent('Not yet')

    await user.click(within(dialog).getByRole('switch', { name: 'Wallets' }))
    await waitFor(() => expect(setMethods).toHaveBeenCalledWith('RAZORPAY', ['card', 'upi', 'netbanking']))

    await user.click(within(dialog).getByRole('tab', { name: 'Webhook' }))
    expect(within(dialog).getByText('https://api.example.com/webhooks/razorpay')).toBeInTheDocument()
    expect(within(dialog).getByText('Webhook secret missing')).toBeInTheDocument()
    expect(within(dialog).getByText('2 rejected webhook(s)')).toBeInTheDocument()
    expect(within(dialog).getByText(/The webhook signature did not verify/)).toBeInTheDocument()
    expect(within(dialog).getByText('Create a key.')).toBeInTheDocument()

    await user.click(within(dialog).getByRole('tab', { name: 'Activity' }))
    expect(within(dialog).getByText(/Configuration saved · admin-sub/)).toBeInTheDocument()
  })

  it('warns when the webhook URL points at the local machine', async () => {
    detail.mockResolvedValue(detailOf(base, { webhook: { ...detailOf(base).webhook, url: 'http://localhost:8081/api/webhooks/razorpay', reachableFromInternet: false } }))
    renderWithProviders(<AdminPaymentGatewayPage />)
    const user = userEvent.setup()
    await user.click(within(await screen.findByRole('listitem', { name: 'Razorpay' })).getByRole('button', { name: 'View details of Razorpay' }))
    await user.click(await screen.findByRole('tab', { name: 'Webhook' }))

    expect(screen.getByText(/points at this machine/)).toBeInTheDocument()
  })

  it('shows a load error with Retry', async () => {
    list.mockRejectedValueOnce(new ApiError(500, 'boom'))
    renderWithProviders(<AdminPaymentGatewayPage />)
    expect(await screen.findByText('boom')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByRole('listitem', { name: 'Razorpay' })).toBeInTheDocument()
  })
})
