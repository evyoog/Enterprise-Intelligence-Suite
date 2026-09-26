import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OidcProvidersSection } from './OidcProvidersSection'

const list = vi.fn()
const create = vi.fn()
const update = vi.fn()
const enable = vi.fn()
const test = vi.fn()

vi.mock('../../api/oidcApi', () => ({
  oidcApi: {
    list: () => list(),
    create: (p: unknown) => create(p),
    update: (id: number, p: unknown) => update(id, p),
    enable: (id: number) => enable(id),
    disable: vi.fn(),
    test: (id: number) => test(id),
    remove: vi.fn(),
  },
}))

const azure = {
  id: 4, name: 'Azure AD', issuerUrl: 'https://login.microsoftonline.com/t/v2.0', clientId: 'eis-client', clientSecretSet: true,
  scopes: 'openid email profile', enabled: false, redirectUri: 'https://eis.example/api/oidc/1/callback',
  createdAt: '2026-09-26T00:00:00Z', updatedAt: '2026-09-26T00:00:00Z',
}

// REQ-IAM-006 (C27) acceptance criteria (UI).
describe('OidcProvidersSection', () => {
  beforeEach(() => {
    for (const m of [list, create, update, enable, test]) m.mockReset()
  })

  it('adds a provider with a secret and shows the redirect URI to register', async () => {
    list.mockResolvedValueOnce([]).mockResolvedValueOnce([azure])
    create.mockResolvedValue(azure)
    renderWithProviders(<OidcProvidersSection />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Add OIDC provider' }))
    const form = screen.getByRole('form', { name: 'Add OIDC provider' })
    await user.type(within(form).getByRole('textbox', { name: /^Name/ }), 'Azure AD')
    await user.type(within(form).getByRole('textbox', { name: /Issuer/ }), azure.issuerUrl)
    await user.type(within(form).getByRole('textbox', { name: /Client ID/ }), 'eis-client')
    const save = within(form).getByRole('button', { name: 'Save' })
    expect(save).toBeDisabled()
    await user.type(within(form).getByLabelText(/Client secret/), 's3cret')
    await user.click(save)

    expect(create).toHaveBeenCalledWith({ name: 'Azure AD', issuerUrl: azure.issuerUrl, clientId: 'eis-client', clientSecret: 's3cret', scopes: 'openid email profile' })
    expect(await screen.findByText(`Redirect URI: ${azure.redirectUri}`)).toBeInTheDocument()
  })

  it('edits without re-entering the secret', async () => {
    list.mockResolvedValue([azure])
    update.mockResolvedValue(azure)
    renderWithProviders(<OidcProvidersSection />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit Azure AD' }))
    const form = screen.getByRole('form', { name: 'Edit OIDC provider' })
    expect(within(form).getByText('Leave blank to keep the current secret.')).toBeInTheDocument()
    await user.click(within(form).getByRole('button', { name: 'Save' }))
    expect(update).toHaveBeenCalledWith(4, expect.objectContaining({ clientSecret: '' }))
  })

  it('enables a provider, tells the page, and shows test results', async () => {
    const onChanged = vi.fn()
    list.mockResolvedValue([azure])
    enable.mockResolvedValue({ ...azure, enabled: true })
    test.mockResolvedValue({ success: false, checks: ['Discovery document read'], errors: ['The discovery document has no JWKS URI'] })
    renderWithProviders(<OidcProvidersSection onChanged={onChanged} />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Enable Azure AD' }))
    expect(enable).toHaveBeenCalledWith(4)
    expect(onChanged).toHaveBeenCalled()
    await user.click(screen.getByRole('button', { name: 'Test Azure AD' }))
    expect(await screen.findByText('The discovery document has no JWKS URI')).toBeInTheDocument()
  })

  it('shows the backend refusal on save', async () => {
    list.mockResolvedValue([])
    create.mockRejectedValue(new ApiError(400, 'The issuer URL must be an absolute https:// URL.'))
    renderWithProviders(<OidcProvidersSection />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Add OIDC provider' }))
    const form = screen.getByRole('form', { name: 'Add OIDC provider' })
    await user.type(within(form).getByRole('textbox', { name: /^Name/ }), 'x')
    await user.type(within(form).getByRole('textbox', { name: /Issuer/ }), 'http://idp')
    await user.type(within(form).getByRole('textbox', { name: /Client ID/ }), 'c')
    await user.type(within(form).getByLabelText(/Client secret/), 's')
    await user.click(within(form).getByRole('button', { name: 'Save' }))
    expect(await screen.findByText('The issuer URL must be an absolute https:// URL.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    list.mockResolvedValue([azure])
    const { container } = renderWithProviders(<OidcProvidersSection />)
    await screen.findByText('Azure AD')
    expect(await axe(container)).toHaveNoViolations()
  })
})
