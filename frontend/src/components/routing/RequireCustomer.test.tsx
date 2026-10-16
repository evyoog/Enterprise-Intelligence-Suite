import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { resetCustomerWorkspaceCache } from '../../auth/useCustomerWorkspace'
import { renderWithProviders } from '../../test/renderWithProviders'
import { RequireCustomer } from './RequireCustomer'

const auth = { isAuthenticated: true, isAdmin: false, user: { username: 'sub-1' } }
vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => auth }))

const permissions = vi.hoisted(() => ({ get: vi.fn() }))
vi.mock('../../api/myPermissionsApi', () => ({ myPermissionsApi: permissions }))

describe('RequireCustomer (the platform admin has no customer account)', () => {
  beforeEach(() => {
    resetCustomerWorkspaceCache()
    permissions.get.mockReset()
    Object.assign(auth, { isAuthenticated: true, isAdmin: false, user: { username: 'sub-1' } })
  })

  it('shows the page at once to a customer, without asking for permissions', () => {
    renderWithProviders(<RequireCustomer><p>My cart</p></RequireCustomer>)
    expect(screen.getByText('My cart')).toBeInTheDocument()
    expect(permissions.get).not.toHaveBeenCalled()
  })

  it('explains the situation to a platform admin who is in no organization, instead of failing with the raw error', async () => {
    Object.assign(auth, { isAdmin: true, user: { username: 'apps@vyoog.com' } })
    permissions.get.mockResolvedValue({ platform: ['MANAGE_CATALOG'], organization: [] })
    renderWithProviders(<RequireCustomer><p>My cart</p></RequireCustomer>)
    expect(await screen.findByText('This page is for customer accounts')).toBeInTheDocument()
    expect(screen.queryByText('My cart')).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Go to the admin area' }).getAttribute('href')).toBe('/admin')
  })

  it('still shows the page to an admin who is also a member of an organization', async () => {
    Object.assign(auth, { isAdmin: true, user: { username: 'admin-member' } })
    permissions.get.mockResolvedValue({ platform: [], organization: ['MANAGE_ORGANIZATION'] })
    renderWithProviders(<RequireCustomer><p>My cart</p></RequireCustomer>)
    await waitFor(() => expect(screen.getByText('My cart')).toBeInTheDocument())
  })

  it('shows the page when the permission lookup fails, as before this guard existed', async () => {
    Object.assign(auth, { isAdmin: true, user: { username: 'admin-unknown' } })
    permissions.get.mockRejectedValue(new Error('down'))
    renderWithProviders(<RequireCustomer><p>My cart</p></RequireCustomer>)
    await waitFor(() => expect(screen.getByText('My cart')).toBeInTheDocument())
  })
})
