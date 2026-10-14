import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { TaxReference, TaxRule, TaxStatus, ValidationReport } from '../../api/taxApi'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminTaxConfigurationPage } from './AdminTaxConfigurationPage'

const api = vi.hoisted(() => ({
  status: vi.fn(), reference: vi.fn(), setMode: vi.fn(), saveProvider: vi.fn(), validateProvider: vi.fn(), saveApprovalPolicy: vi.fn(),
  rules: vi.fn(), createRule: vi.fn(), updateRule: vi.fn(), deleteRule: vi.fn(), submitRule: vi.fn(), approveRule: vi.fn(), rejectRule: vi.fn(),
  activateRule: vi.fn(), deactivateRule: vi.fn(), newRuleVersion: vi.fn(), preview: vi.fn(),
  classes: vi.fn(), createClass: vi.fn(), updateClass: vi.fn(), products: vi.fn(), assignClass: vi.fn(),
  registrations: vi.fn(), createRegistration: vi.fn(), updateRegistration: vi.fn(), deleteRegistration: vi.fn(),
  exemptions: vi.fn(), createExemption: vi.fn(), approveExemption: vi.fn(), revokeExemption: vi.fn(),
  blockedInvoices: vi.fn(), retryBlockedInvoice: vi.fn(),
}))
vi.mock('../../api/taxApi', () => ({ taxApi: api }))

const status = (over: Partial<TaxStatus> = {}): TaxStatus => ({
  mode: 'INTERNAL', modeLabel: 'EIS internal tax engine', externalEngineEnabled: false, status: 'READY',
  messages: [{ level: 'INFO', code: 'OK', text: '1 approved rule(s) in force.' }], configVersion: 3, allowSelfApproval: false,
  internal: { rulesInForce: 1, pendingApproval: 0, draftRules: 0, countriesCovered: ['IN'] },
  external: {
    providerCode: 'GENERIC_REST', providerName: 'Generic REST tax provider (contract v1)',
    providers: [{ code: 'GENERIC_REST', name: 'Generic REST tax provider (contract v1)' }], configured: false,
    credentials: { TAX_PROVIDER_BASE_URL: false, TAX_PROVIDER_API_KEY: false, TAX_PROVIDER_API_SECRET: false },
    environment: 'SANDBOX', companyCode: 'EVYOOG', timeoutSeconds: 10, maxAttempts: 3, validationStatus: 'NOT_VALIDATED',
    countries: [], currencies: [], transactionTypes: [],
  },
  invoicesWaiting: 0, ...over,
})

const reference: TaxReference = {
  countries: [{ code: 'IN', name: 'India' }, { code: 'US', name: 'United States' }],
  states: { IN: [{ code: 'TN', name: 'Tamil Nadu', gstCode: '33' }, { code: 'KA', name: 'Karnataka', gstCode: '29' }], US: [] },
  taxTypes: ['GST', 'VAT', 'SALES_TAX', 'OTHER'], treatments: ['TAXABLE', 'EXEMPT', 'ZERO_RATED', 'NIL_RATED', 'REVERSE_CHARGE', 'OUT_OF_SCOPE'],
  customerTypes: ['ANY', 'INDIVIDUAL', 'BUSINESS'], supplyTypes: ['ANY', 'INTRA_STATE', 'INTER_STATE', 'CROSS_BORDER'],
  taxIdConditions: ['ANY', 'PROVIDED', 'NOT_PROVIDED'], supplierCountryCode: 'IN', supplierStateCode: 'TN',
}

const rule = (over: Partial<TaxRule> = {}): TaxRule => ({
  id: 1, ruleKey: 'k1', version: 1, latestVersion: true, name: 'GST within state', countryCode: 'IN', countryName: 'India', jurisdiction: 'India',
  taxType: 'GST', customerType: 'ANY', supplyType: 'INTRA_STATE', taxIdCondition: 'ANY', treatment: 'TAXABLE', effectiveFrom: '2026-04-01',
  approvalStatus: 'APPROVED', active: true, inForce: true, createdAt: '2026-04-01T00:00:00Z', totalRate: 18,
  components: [{ code: 'CGST', name: 'Central GST', rate: 9, compound: false }, { code: 'SGST', name: 'State GST', rate: 9, compound: false }], ...over,
})

const failedReport: ValidationReport = {
  passed: false, validatedAt: '2026-10-13T10:00:00Z', countries: [], currencies: [], transactionTypes: [],
  checks: [
    { code: 'PROVIDER_SELECTED', passed: true, label: 'A tax provider is selected', detail: 'Generic' },
    { code: 'CREDENTIALS', passed: false, label: 'Credentials are present on the backend', detail: 'Set TAX_PROVIDER_BASE_URL in the backend environment.' },
  ],
}

function setup(over: Partial<TaxStatus> = {}) {
  api.status.mockResolvedValue(status(over))
  api.reference.mockResolvedValue(reference)
  api.rules.mockResolvedValue([rule()])
  api.classes.mockResolvedValue([])
  api.products.mockResolvedValue([])
  api.registrations.mockResolvedValue([])
  api.exemptions.mockResolvedValue([])
  api.blockedInvoices.mockResolvedValue([])
}

describe('AdminTaxConfigurationPage', () => {
  beforeEach(() => {
    Object.values(api).forEach((m) => m.mockReset())
    setup()
  })

  it('shows the External Tax Engine switch, the active mode and its status', async () => {
    const { container } = renderWithProviders(<AdminTaxConfigurationPage />)
    expect(await screen.findByRole('heading', { name: 'External Tax Engine' })).toBeInTheDocument()
    expect(screen.getByText('Enable or disable external tax calculation for billing transactions.')).toBeInTheDocument()
    expect(screen.getByTestId('tax-active-mode')).toHaveTextContent('EIS internal tax engine')
    expect(screen.getByTestId('tax-engine-status')).toHaveTextContent('Ready')
    expect(screen.getByRole('switch', { name: 'External Tax Engine' })).not.toBeChecked()
    expect(screen.getByText(/Configuration version 3/)).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('shows the external mode when the switch is on', async () => {
    setup({ mode: 'EXTERNAL', externalEngineEnabled: true, status: 'READY', modeChangedAt: '2026-10-13T10:00:00Z', modeChangedBy: 'admin@evyoog.com' })
    renderWithProviders(<AdminTaxConfigurationPage />)
    expect(await screen.findByTestId('tax-active-mode')).toHaveTextContent('External tax provider')
    expect(screen.getByRole('switch', { name: 'External Tax Engine' })).toBeChecked()
    expect(screen.getByText(/admin@evyoog.com/)).toBeInTheDocument()
  })

  it('says clearly when invoices are blocked', async () => {
    setup({ status: 'BLOCKING', messages: [{ level: 'ERROR', code: 'NO_RULE_IN_FORCE', text: 'No approved tax rule is in force. New invoices will wait as Draft until a rule is approved.' }], invoicesWaiting: 2 })
    renderWithProviders(<AdminTaxConfigurationPage />)
    expect(await screen.findByTestId('tax-engine-status')).toHaveTextContent('Blocking invoices')
    expect(screen.getByText(/No approved tax rule is in force/)).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: /Draft invoices/ })).toBeInTheDocument()
  })

  it('switches on after a confirmation and reports the change', async () => {
    api.setMode.mockResolvedValue({ changed: true, status: status({ mode: 'EXTERNAL', externalEngineEnabled: true }) })
    renderWithProviders(<AdminTaxConfigurationPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('switch', { name: 'External Tax Engine' }))
    const dialog = await screen.findByRole('dialog', { name: 'Switch to the external tax provider?' })
    expect(within(dialog).getByText(/does not use its own rules instead/)).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Switch' }))
    expect(api.setMode).toHaveBeenCalledWith(true)
    expect(await screen.findByText('The external tax provider is now used for new invoices.')).toBeInTheDocument()
    expect(screen.getByTestId('tax-active-mode')).toHaveTextContent('External tax provider')
  })

  it('does nothing when the confirmation is cancelled', async () => {
    renderWithProviders(<AdminTaxConfigurationPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('switch', { name: 'External Tax Engine' }))
    await user.click(await screen.findByRole('button', { name: 'Cancel' }))
    expect(api.setMode).not.toHaveBeenCalled()
    expect(screen.getByTestId('tax-active-mode')).toHaveTextContent('EIS internal tax engine')
  })

  it('lists every failed check when the external engine cannot be switched on and keeps the mode', async () => {
    api.setMode.mockResolvedValue({ changed: false, status: status(), report: failedReport })
    renderWithProviders(<AdminTaxConfigurationPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('switch', { name: 'External Tax Engine' }))
    await user.click(await screen.findByRole('button', { name: 'Switch' }))
    const dialog = await screen.findByRole('dialog', { name: 'The external engine was not switched on' })
    expect(within(dialog).getByText('Credentials are present on the backend')).toBeInTheDocument()
    expect(within(dialog).getByText('Set TAX_PROVIDER_BASE_URL in the backend environment.')).toBeInTheDocument()
    expect(within(dialog).getByText(/Nothing was changed/)).toBeInTheDocument()
    await userEvent.setup().click(within(dialog).getByRole('button', { name: 'Close' }))
    expect(await screen.findByRole('switch', { name: 'External Tax Engine' })).not.toBeChecked()
    expect(screen.getByTestId('tax-active-mode')).toHaveTextContent('EIS internal tax engine')
  })

  it('switches back off with its own confirmation', async () => {
    setup({ mode: 'EXTERNAL', externalEngineEnabled: true })
    api.setMode.mockResolvedValue({ changed: true, status: status() })
    renderWithProviders(<AdminTaxConfigurationPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('switch', { name: 'External Tax Engine' }))
    expect(await screen.findByRole('dialog', { name: 'Switch to the EIS internal tax engine?' })).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Switch' }))
    expect(api.setMode).toHaveBeenCalledWith(false)
    expect(await screen.findByText('The EIS internal engine is now used for new invoices.')).toBeInTheDocument()
  })

  it('lists the rules with their components, dates, status and life cycle actions', async () => {
    api.rules.mockResolvedValue([
      rule(),
      rule({ id: 2, name: 'GST between states', supplyType: 'INTER_STATE', approvalStatus: 'PENDING_APPROVAL', active: false, inForce: false, components: [{ code: 'IGST', name: 'Integrated GST', rate: 18, compound: false }] }),
      rule({ id: 3, name: 'Draft rule', approvalStatus: 'DRAFT', active: false, inForce: false }),
      rule({ id: 4, name: 'Rejected rule', approvalStatus: 'REJECTED', active: false, inForce: false, rejectionReason: 'Rate is out of date' }),
    ])
    renderWithProviders(<AdminTaxConfigurationPage />)
    expect((await screen.findAllByText('CGST 9% + SGST 9%')).length).toBeGreaterThan(0)
    expect(screen.getByText('IGST 18%')).toBeInTheDocument()
    expect(screen.getByText('Pending approval')).toBeInTheDocument()
    expect(screen.getByText('Rejected: Rate is out of date')).toBeInTheDocument()
    expect(screen.getByText('In force today')).toBeInTheDocument()
    const rows = screen.getAllByRole('row')
    const approved = rows.find((r) => within(r).queryByText('GST within state'))!
    expect(within(approved).getByRole('button', { name: 'Deactivate' })).toBeInTheDocument()
    expect(within(approved).getByRole('button', { name: 'New version' })).toBeInTheDocument()
    expect(within(approved).queryByRole('button', { name: 'Edit' })).not.toBeInTheDocument()
    const pending = rows.find((r) => within(r).queryByText('GST between states'))!
    expect(within(pending).getByRole('button', { name: 'Approve' })).toBeInTheDocument()
    expect(within(pending).getByRole('button', { name: 'Reject' })).toBeInTheDocument()
    const draft = rows.find((r) => within(r).queryByText('Draft rule'))!
    expect(within(draft).getByRole('button', { name: 'Edit' })).toBeInTheDocument()
    expect(within(draft).getByRole('button', { name: 'Submit for approval' })).toBeInTheDocument()
  })

  it('approves, submits and deactivates through the API and refreshes the status', async () => {
    api.rules.mockResolvedValue([rule({ id: 2, approvalStatus: 'PENDING_APPROVAL', active: false, inForce: false })])
    api.approveRule.mockResolvedValue(rule())
    renderWithProviders(<AdminTaxConfigurationPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Approve' }))
    expect(api.approveRule).toHaveBeenCalledWith(2)
    expect(await screen.findByText('Approved and active.')).toBeInTheDocument()
    expect(api.status.mock.calls.length).toBeGreaterThan(1)
  })

  it('shows the server message when an approval is refused (the same administrator)', async () => {
    api.rules.mockResolvedValue([rule({ id: 2, approvalStatus: 'PENDING_APPROVAL', active: false, inForce: false })])
    api.approveRule.mockRejectedValue(new Error('A different administrator must approve this rule: you edited or submitted it.'))
    renderWithProviders(<AdminTaxConfigurationPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Approve' }))
    expect(await screen.findByText(/A different administrator must approve this rule/)).toBeInTheDocument()
  })

  it('rejects a rule with a reason', async () => {
    api.rules.mockResolvedValue([rule({ id: 2, approvalStatus: 'PENDING_APPROVAL', active: false, inForce: false })])
    api.rejectRule.mockResolvedValue(rule({ approvalStatus: 'REJECTED' }))
    renderWithProviders(<AdminTaxConfigurationPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Reject' }))
    const dialog = await screen.findByRole('dialog', { name: 'Reject this rule' })
    expect(within(dialog).getByRole('button', { name: 'Reject rule' })).toBeDisabled()
    await user.type(within(dialog).getByRole('textbox', { name: /Reason/ }), 'Wrong rate')
    await user.click(within(dialog).getByRole('button', { name: 'Reject rule' }))
    expect(api.rejectRule).toHaveBeenCalledWith(2, 'Wrong rate')
  })

  it('lets a single administrator approve only when the policy is switched on', async () => {
    api.saveApprovalPolicy.mockResolvedValue(status({ allowSelfApproval: true }))
    renderWithProviders(<AdminTaxConfigurationPage />)
    const toggle = await screen.findByRole('switch', { name: 'Allow the same administrator to approve' })
    expect(toggle).not.toBeChecked()
    await userEvent.setup().click(toggle)
    expect(api.saveApprovalPolicy).toHaveBeenCalledWith(true)
  })

  it('creates a rule with CGST and SGST components and sends no hard-coded rate of its own', async () => {
    api.rules.mockResolvedValue([])
    api.createRule.mockResolvedValue(rule())
    renderWithProviders(<AdminTaxConfigurationPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'New rule' }))
    const dialog = await screen.findByRole('dialog', { name: 'New tax rule' })
    await user.type(within(dialog).getByRole('textbox', { name: /Rule name/ }), 'GST within state')
    const group = within(dialog).getByRole('group', { name: /Tax components 1/ })
    await user.type(within(group).getByRole('textbox', { name: /Code/ }), 'CGST')
    await user.type(within(group).getByRole('textbox', { name: /Name/ }), 'Central GST')
    expect(within(group).getByRole('textbox', { name: /Rate/ })).toHaveValue('')   // no default rate is pre-filled
    await user.type(within(group).getByRole('textbox', { name: /Rate/ }), '9')
    await user.click(within(dialog).getByRole('button', { name: 'Add component' }))
    const second = within(dialog).getByRole('group', { name: /Tax components 2/ })
    await user.type(within(second).getByRole('textbox', { name: /Code/ }), 'SGST')
    await user.type(within(second).getByRole('textbox', { name: /Name/ }), 'State GST')
    await user.type(within(second).getByRole('textbox', { name: /Rate/ }), '9')
    expect(within(dialog).getByText('Total rate: 18%')).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Save as draft' }))
    await waitFor(() => expect(api.createRule).toHaveBeenCalledTimes(1))
    const body = api.createRule.mock.calls[0][0]
    expect(body.name).toBe('GST within state')
    expect(body.countryCode).toBe('IN')
    expect(body.treatment).toBe('TAXABLE')
    expect(body.components).toEqual([
      { code: 'CGST', name: 'Central GST', rate: 9, compound: false },
      { code: 'SGST', name: 'State GST', rate: 9, compound: false },
    ])
  })

  it('validates the rule form before calling the API', async () => {
    api.rules.mockResolvedValue([])
    renderWithProviders(<AdminTaxConfigurationPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'New rule' }))
    const dialog = await screen.findByRole('dialog', { name: 'New tax rule' })
    await user.click(within(dialog).getByRole('button', { name: 'Save as draft' }))
    expect(await within(dialog).findByText('Enter a rule name.')).toBeInTheDocument()
    expect(within(dialog).getByText('Each component needs a code (letters and digits).')).toBeInTheDocument()
    expect(within(dialog).getByText('A rate is 0 to 100 with up to 4 decimals.')).toBeInTheDocument()
    expect(api.createRule).not.toHaveBeenCalled()
    const group = within(dialog).getByRole('group', { name: /Tax components 1/ })
    await user.type(within(group).getByRole('textbox', { name: /Rate/ }), '180')
    expect(within(dialog).getByText('A rate is 0 to 100 with up to 4 decimals.')).toBeInTheDocument()
  })

  it('previews a calculation and shows each component', async () => {
    api.preview.mockResolvedValue({
      status: 'CALCULATED', placeOfSupply: 'Tamil Nadu, India', taxableAmount: 100000, totalTax: 18000, total: 118000,
      components: [{ code: 'CGST', name: 'Central GST', treatment: 'TAXABLE', rate: 9, taxableAmount: 100000, taxAmount: 9000 }, { code: 'SGST', name: 'State GST', treatment: 'TAXABLE', rate: 9, taxableAmount: 100000, taxAmount: 9000 }],
    })
    renderWithProviders(<AdminTaxConfigurationPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Calculate' }))
    const result = await screen.findByTestId('tax-preview-result')
    expect(api.preview.mock.calls[0][0]).toMatchObject({ amount: 100000, currency: 'INR', country: 'IN' })
    expect(within(result).getByText('CGST — Central GST')).toBeInTheDocument()
    expect(within(result).getByText('Place of supply: Tamil Nadu, India')).toBeInTheDocument()
  })

  it('shows why a preview is blocked instead of a zero', async () => {
    api.preview.mockResolvedValue({ status: 'BLOCKED', blockCode: 'NO_MATCHING_RULE', blockMessage: 'No approved, active tax rule matches.', taxableAmount: 100000, totalTax: 0, total: 100000, components: [] })
    renderWithProviders(<AdminTaxConfigurationPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Calculate' }))
    expect(await screen.findByText('Blocked: No approved, active tax rule matches.')).toBeInTheDocument()
    expect(screen.queryByTestId('tax-preview-result')).not.toBeInTheDocument()
  })

  it('shows the provider credentials only as set or not set and has no field for a key or secret', async () => {
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=provider' })
    expect(await screen.findByRole('heading', { name: 'External tax provider' })).toBeInTheDocument()
    expect(screen.getByText('TAX_PROVIDER_API_KEY: Not set')).toBeInTheDocument()
    expect(screen.getByText('TAX_PROVIDER_BASE_URL: Not set')).toBeInTheDocument()
    const fields = screen.getAllByRole('textbox').map((f) => f.getAttribute('type') ?? 'text')
    expect(fields).not.toContain('password')
    expect(screen.queryByLabelText(/api key/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/secret/i)).not.toBeInTheDocument()
    expect(screen.getByText('Not validated')).toBeInTheDocument()
  })

  it('saves provider settings and tests the connection, listing the capabilities', async () => {
    api.saveProvider.mockResolvedValue(status())
    api.validateProvider.mockResolvedValue({ passed: true, validatedAt: '2026-10-13T10:00:00Z', countries: ['IN'], currencies: ['INR'], transactionTypes: ['SALE'], checks: [{ code: 'CONNECTION', passed: true, label: 'The provider answers and accepts the credentials', detail: 'Provider: Example.' }] })
    api.status.mockResolvedValueOnce(status()).mockResolvedValue(status({ external: { ...status().external, configured: true, validationStatus: 'PASSED', validatedAt: '2026-10-13T10:00:00Z', countries: ['IN'], currencies: ['INR'], transactionTypes: ['SALE'] } }))
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=provider' })
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Save settings' }))
    expect(api.saveProvider).toHaveBeenCalledWith({ providerCode: 'GENERIC_REST', companyCode: 'EVYOOG', environment: 'SANDBOX', timeoutSeconds: 10, maxAttempts: 3 })
    await user.click(screen.getByRole('button', { name: 'Test connection and validate capabilities' }))
    expect(await screen.findByText('The provider answers and accepts the credentials')).toBeInTheDocument()
    expect(await screen.findByText('Passed')).toBeInTheDocument()
    expect(screen.getAllByText('IN').length).toBeGreaterThan(0)
  })

  it('lists Draft invoices with their reason and retries tax', async () => {
    api.blockedInvoices.mockResolvedValue([{ invoiceId: 42, ownerLabel: 'Acme Pvt Ltd', currency: 'INR', subtotal: 100000, createdAt: '2026-10-13T10:00:00Z', blockCode: 'NO_MATCHING_RULE', blockReason: 'No approved tax rule matches: country IN.', taxEngine: 'INTERNAL' }])
    api.retryBlockedInvoice.mockResolvedValue({ id: 42, invoiceNumber: 'EIS/26-27/000001', status: 'OPEN' })
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=blocked' })
    expect(await screen.findByText('Draft #42')).toBeInTheDocument()
    expect(screen.getByText('No matching rule')).toBeInTheDocument()
    expect(screen.getByText('No approved tax rule matches: country IN.')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry tax' }))
    expect(api.retryBlockedInvoice).toHaveBeenCalledWith(42)
    expect(await screen.findByText('Invoice EIS/26-27/000001 was issued.')).toBeInTheDocument()
  })

  it('says when a retried invoice is still blocked', async () => {
    api.blockedInvoices.mockResolvedValue([{ invoiceId: 42, currency: 'INR', subtotal: 100000, createdAt: '2026-10-13T10:00:00Z', blockCode: 'NO_MATCHING_RULE' }])
    api.retryBlockedInvoice.mockResolvedValue({ id: 42, status: 'DRAFT', taxBlockCode: 'NO_MATCHING_RULE', taxBlockReason: 'Still no rule.' })
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=blocked' })
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Retry tax' }))
    expect(await screen.findByText('Still blocked: Still no rule.')).toBeInTheDocument()
  })

  it('shows an empty state when nothing is waiting', async () => {
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=blocked' })
    expect(await screen.findByText('No invoices are waiting for tax.')).toBeInTheDocument()
  })

  it('assigns a tax class to a product', async () => {
    api.classes.mockResolvedValue([{ id: 1, code: 'SAAS', name: 'Software as a service', hsnSacCode: '998439', enabled: true, productCount: 0 }])
    api.products.mockResolvedValue([{ productId: 7, name: 'Valam.ai', status: 'ACTIVE' }])
    api.assignClass.mockResolvedValue({ productId: 7, name: 'Valam.ai', status: 'ACTIVE', taxClassCode: 'SAAS' })
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=classification' })
    const user = userEvent.setup()
    const select = await screen.findByRole('combobox', { name: 'Tax class of Valam.ai' })
    await user.click(select)
    await user.click(await screen.findByRole('option', { name: 'SAAS — Software as a service' }))
    expect(api.assignClass).toHaveBeenCalledWith(7, 'SAAS')
  })

  it('creates a tax class', async () => {
    api.createClass.mockResolvedValue({ id: 1, code: 'SAAS', name: 'SaaS', enabled: true, productCount: 0 })
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=classification' })
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'New class' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Save class' }))
    expect(await within(dialog).findByText('Enter a code (letters, digits, - and _).')).toBeInTheDocument()
    await user.type(within(dialog).getByRole('textbox', { name: /Code/ }), 'SAAS')
    await user.type(within(dialog).getByRole('textbox', { name: /^Name/ }), 'SaaS')
    await user.type(within(dialog).getByRole('textbox', { name: /SAC/ }), '998439')
    await user.click(within(dialog).getByRole('button', { name: 'Save class' }))
    expect(api.createClass).toHaveBeenCalledWith({ code: 'SAAS', name: 'SaaS', hsnSacCode: '998439', description: undefined, enabled: true })
  })

  it('lists exemptions and approves a pending one', async () => {
    api.exemptions.mockResolvedValue([{ id: 5, ownerType: 'ORGANIZATION', ownerId: 9, ownerLabel: 'SEZ Co', countryCode: 'IN', countryName: 'India', reason: 'SEZ unit', certificateNumber: 'SEZ-1', effectiveFrom: '2026-04-01', status: 'PENDING_APPROVAL', inForce: false, createdAt: '2026-04-01T00:00:00Z' }])
    api.approveExemption.mockResolvedValue({})
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=exemptions' })
    expect(await screen.findByText('SEZ Co')).toBeInTheDocument()
    expect(screen.getByText('SEZ-1')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Approve' }))
    expect(api.approveExemption).toHaveBeenCalledWith(5)
  })

  it('lists registrations and marks the business profile GSTIN as read-only', async () => {
    api.registrations.mockResolvedValue([
      { countryCode: 'IN', countryName: 'India', taxType: 'GST', registrationNumber: '33ABCDE1234F1Z5', enabled: true, fromBusinessProfile: true },
      { id: 3, countryCode: 'DE', countryName: 'Germany', taxType: 'VAT', registrationNumber: 'DE123456789', enabled: true, fromBusinessProfile: false },
    ])
    renderWithProviders(<AdminTaxConfigurationPage />, { route: '/admin/billing/tax?tab=registrations' })
    expect(await screen.findByText('33ABCDE1234F1Z5')).toBeInTheDocument()
    expect(screen.getByText('From Billing settings')).toBeInTheDocument()
    const rows = screen.getAllByRole('row')
    expect(within(rows.find((r) => within(r).queryByText('33ABCDE1234F1Z5'))!).queryByRole('button', { name: 'Delete' })).not.toBeInTheDocument()
    expect(within(rows.find((r) => within(r).queryByText('DE123456789'))!).getByRole('button', { name: 'Delete' })).toBeInTheDocument()
  })

  it('shows an error with a retry when the configuration cannot be loaded', async () => {
    api.status.mockRejectedValueOnce(new Error('boom'))
    renderWithProviders(<AdminTaxConfigurationPage />)
    expect(await screen.findByText('boom')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Try again' }))
    expect(await screen.findByRole('heading', { name: 'External Tax Engine' })).toBeInTheDocument()
  })
})
