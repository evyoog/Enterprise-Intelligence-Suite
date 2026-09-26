import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { renderWithProviders } from '../test/renderWithProviders'
import { OrganizationSamlProvidersPage } from './OrganizationSamlProvidersPage'

const list = vi.fn()
const update = vi.fn()

vi.mock('../api/samlApi', () => ({
  samlApi: {
    list: () => list(),
    update: (id: number, p: unknown) => update(id, p),
    create: vi.fn(), enable: vi.fn(), disable: vi.fn(), test: vi.fn(), remove: vi.fn(),
  },
}))
vi.mock('../api/oidcApi', () => ({ oidcApi: { list: vi.fn().mockResolvedValue([]) } }))
vi.mock('../api/registrationApi', () => ({
  organizationApi: { getMyOrganization: vi.fn().mockResolvedValue({ id: 1 }) },
}))
vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@example.com' }, logout: vi.fn() }),
}))
vi.mock('../auth/AuthModalContext', () => ({
  useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }),
}))
vi.mock('../api/productsApi', () => ({
  productsApi: { list: vi.fn().mockResolvedValue([]) },
}))

const provider = {
  id: 3, name: 'Okta', entityId: 'https://idp.example/entity', ssoUrl: 'https://idp.example/sso',
  certificatePem: '-----BEGIN CERTIFICATE-----\nMIIB\n-----END CERTIFICATE-----', certificateFingerprint: null,
  certificateExpiresAt: null, certificateExpired: false, enabled: true, createdAt: '2026-09-01T00:00:00Z', updatedAt: '2026-09-01T00:00:00Z',
}

// REQ-IAM-005 acceptance criteria AC-1, AC-2, AC-3 (UI), AC-7.
describe('OrganizationSamlProvidersPage — edit provider', () => {
  beforeEach(() => {
    list.mockReset()
    update.mockReset()
    list.mockResolvedValue([provider])
  })

  it('changes only the name, leaving the connection details untouched', async () => {
    update.mockResolvedValue({ ...provider, name: 'Okta EU' })
    renderWithProviders(<OrganizationSamlProvidersPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit Okta' }))
    const nameField = screen.getByRole('textbox', { name: /Name/ })
    expect(nameField).toHaveValue('Okta')
    await user.clear(nameField)
    await user.type(nameField, 'Okta EU')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(update).toHaveBeenCalledWith(3, { name: 'Okta EU' })
  })

  it('sends new manual details, starting from the current values', async () => {
    update.mockResolvedValue(provider)
    renderWithProviders(<OrganizationSamlProvidersPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit Okta' }))
    await user.click(screen.getByRole('button', { name: 'Enter details manually' }))
    const ssoUrl = screen.getByRole('textbox', { name: /SSO URL/ })
    expect(ssoUrl).toHaveValue('https://idp.example/sso')
    await user.clear(ssoUrl)
    await user.type(ssoUrl, 'https://idp.example/sso2')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(update).toHaveBeenCalledWith(3, {
      name: 'Okta', entityId: 'https://idp.example/entity', ssoUrl: 'https://idp.example/sso2', certificatePem: provider.certificatePem,
    })
  })

  it('keeps the form open and shows the backend message when the update is refused', async () => {
    update.mockRejectedValue(new ApiError(400, 'ssoUrl is not a valid absolute URL'))
    renderWithProviders(<OrganizationSamlProvidersPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit Okta' }))
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByText('ssoUrl is not a valid absolute URL')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save changes' })).toBeInTheDocument()
  })

  it('has no detectable a11y violations with the edit form open', async () => {
    const { container } = renderWithProviders(<OrganizationSamlProvidersPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Edit Okta' }))
    expect(await axe(container)).toHaveNoViolations()
  })
})
