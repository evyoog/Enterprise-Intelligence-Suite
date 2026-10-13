import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationApiKeysSection } from './OrganizationApiKeysSection'

const list = vi.fn()
const create = vi.fn()
const revoke = vi.fn()

vi.mock('../../api/apiKeysApi', () => ({
  API_KEY_STATUS_COLOR: { ACTIVE: 'success', REVOKED: 'default', EXPIRED: 'warning' },
  organizationApiKeysApi: { list: () => list(), create: (b: unknown) => create(b), revoke: (id: number) => revoke(id) },
}))

const active = {
  id: 3, name: 'CRM sync', prefix: 'eis_ab12cd34', status: 'ACTIVE' as const, scope: 'READ' as const, personal: false,
  createdByName: 'Asha Rao', createdAt: '2026-10-03T09:00:00Z', expiresAt: null, lastUsedAt: null, requestCount: 0,
}

// TC-INT-035
describe('OrganizationApiKeysSection', () => {
  beforeEach(() => {
    for (const m of [list, create, revoke]) m.mockReset()
    list.mockResolvedValue([active])
  })

  it('lists the organization keys with scope and creator, by prefix only', async () => {
    renderWithProviders(<OrganizationApiKeysSection />)
    expect(await screen.findByText('eis_ab12cd34_…')).toBeInTheDocument()
    expect(screen.getByText('Read-only')).toBeInTheDocument()
    expect(screen.getByText('Asha Rao')).toBeInTheDocument()
  })

  it('creates a read-write key and shows the full key once', async () => {
    const user = userEvent.setup()
    create.mockResolvedValue({ ...active, id: 4, name: 'Reports', scope: 'READ_WRITE', key: 'eis_ab12cd34_' + 'Q'.repeat(40) })
    renderWithProviders(<OrganizationApiKeysSection />)
    await user.click(await screen.findByRole('button', { name: 'Create key' }))
    await user.click(screen.getByRole('button', { name: 'Create' }))
    expect(screen.getByText('Enter a name for the key.')).toBeInTheDocument()
    await user.type(screen.getByLabelText(/Name/), 'Reports')
    await user.click(screen.getByRole('combobox', { name: 'Scope' }))
    await user.click(screen.getByRole('option', { name: 'Read-write' }))
    await user.click(screen.getByRole('button', { name: 'Create' }))
    await waitFor(() => expect(create).toHaveBeenCalledWith({ name: 'Reports', scope: 'READ_WRITE', expiresAt: null }))
    expect(await screen.findByDisplayValue('eis_ab12cd34_' + 'Q'.repeat(40))).toBeInTheDocument()
    expect(screen.getByText("Copy this key now. You won't be able to see it again.")).toBeInTheDocument()
  })

  it('shows the refusal when the organization is at its key limit', async () => {
    const user = userEvent.setup()
    const { ApiError } = await import('../../api/client')
    create.mockRejectedValue(new ApiError(409, 'Your organization already has 10 active API keys.'))
    renderWithProviders(<OrganizationApiKeysSection />)
    await user.click(await screen.findByRole('button', { name: 'Create key' }))
    await user.type(screen.getByLabelText(/Name/), 'Another')
    await user.click(screen.getByRole('button', { name: 'Create' }))
    expect(await screen.findByText('Your organization already has 10 active API keys.')).toBeInTheDocument()
  })

  it('revokes a key after confirmation', async () => {
    const user = userEvent.setup()
    revoke.mockResolvedValue({ ...active, status: 'REVOKED' })
    renderWithProviders(<OrganizationApiKeysSection />)
    await user.click(await screen.findByRole('button', { name: 'Revoke CRM sync' }))
    await user.click(screen.getByRole('button', { name: 'Revoke' }))
    expect(revoke).toHaveBeenCalledWith(3)
    expect(await screen.findByText('API key revoked.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<OrganizationApiKeysSection />)
    await screen.findByText('eis_ab12cd34_…')
    expect(await axe(container)).toHaveNoViolations()
  })
})
