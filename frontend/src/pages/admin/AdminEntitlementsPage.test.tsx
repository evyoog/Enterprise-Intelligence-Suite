import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminEntitlementsPage } from './AdminEntitlementsPage'

const adminCustomer = vi.fn()
const adminOrganization = vi.fn()

vi.mock('../../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/registrationApi')>('../../api/registrationApi')
  return { ...actual, entitlementsApi: { ...actual.entitlementsApi, adminCustomer: (id: number) => adminCustomer(id), adminOrganization: (id: number) => adminOrganization(id) } }
})

const entitlement = {
  subscriptionId: 1, productId: 10, productName: 'Valam.ai', ownerType: 'ORGANIZATION' as const, planName: 'Team', status: 'ACTIVE',
  expiresAt: '2026-11-02T10:00:00Z', includedFeatures: ['Dashboards'], usageLimit: 500, quantity: 5,
}

describe('AdminEntitlementsPage (C97 EN-3)', () => {
  beforeEach(() => {
    adminCustomer.mockReset()
    adminOrganization.mockReset()
  })

  it('looks up a customer read-only and has no axe violations', async () => {
    adminCustomer.mockResolvedValue([{ ...entitlement, ownerType: 'INDIVIDUAL' }])
    const { container } = renderWithProviders(<AdminEntitlementsPage />)
    const user = userEvent.setup()
    expect(screen.getByRole('button', { name: 'Look up' })).toBeDisabled()

    await user.type(screen.getByRole('textbox', { name: 'ID' }), '42')
    await user.click(screen.getByRole('button', { name: 'Look up' }))

    await waitFor(() => expect(adminCustomer).toHaveBeenCalledWith(42))
    expect(await screen.findByRole('heading', { name: 'Valam.ai' })).toBeInTheDocument()
    expect(screen.getByText('Dashboards')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /grant|revoke/i })).not.toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('looks up an organization, says when nothing is active, and says when the ID is unknown', async () => {
    adminOrganization.mockResolvedValueOnce([]).mockRejectedValueOnce(new ApiError(404, 'Organization not found'))
    renderWithProviders(<AdminEntitlementsPage />)
    const user = userEvent.setup()
    await user.click(screen.getByRole('combobox', { name: 'Look up' }))
    await user.click(await screen.findByRole('option', { name: 'Organization' }))
    await user.type(screen.getByRole('textbox', { name: 'ID' }), '7')
    await user.click(screen.getByRole('button', { name: 'Look up' }))
    expect(await screen.findByText('Nothing is active for this customer or organization.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Look up' }))
    expect(await screen.findByText('No customer or organization has that ID.')).toBeInTheDocument()
  })

  it('refuses a non-numeric ID', async () => {
    renderWithProviders(<AdminEntitlementsPage />)
    await userEvent.setup().type(screen.getByRole('textbox', { name: 'ID' }), 'abc')
    expect(screen.getByText('Enter a number')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Look up' })).toBeDisabled()
  })
})
