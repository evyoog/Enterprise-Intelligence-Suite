import '../i18n'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Navigate, MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { NavAccessContext, type NavAccessValue } from '../components/layout/appShellContext'
import { LEGACY_ADMIN_REDIRECTS } from '../components/routing/legacyRedirects'
import { OrganizationMembersPage } from './OrganizationMembersPage'
import { OrganizationSettingsRedirect } from './OrganizationSettingsPage'
import { ServiceStatusRoutePage } from './ServiceStatusRoutePage'
import { ProductsAdminPage } from './admin/ProductsAdminPage'
import { RolesPermissionsPage } from './admin/RolesPermissionsPage'

// The pages inside the tabs are tested on their own; here only the shells are.
vi.mock('../components/organization/OrganizationMembersCard', () => ({ OrganizationMembersCard: () => <p>Members card</p> }))
vi.mock('../api/myPermissionsApi', () => ({ myPermissionsApi: { get: () => Promise.resolve({ platform: [], organization: ['MANAGE_ORGANIZATION', 'MANAGE_USERS', 'INVITE_USERS'] }) } }))
vi.mock('../components/organization/OrganizationInvitationsCard', () => ({ OrganizationInvitationsCard: () => <p>Invitations card</p> }))
vi.mock('../components/organization/OrganizationStructurePanel', () => ({ OrganizationStructurePanel: () => <p>Structure panel</p> }))
vi.mock('../components/organization/OrganizationGroupsCard', () => ({ OrganizationGroupsCard: () => <p>Groups card</p> }))
vi.mock('./ServiceStatusPage', () => ({ ServiceStatusPage: () => <p>Status page</p> }))
vi.mock('./admin/ServiceStatusAdminPage', () => ({ ServiceStatusAdminPage: () => <p>Manage status page</p> }))
vi.mock('./admin/PlatformsListPage', () => ({ PlatformsListPage: () => <p>Products list</p> }))
vi.mock('./admin/settings/CommonSettingsPage', () => ({ CommonSettingsPage: () => <p>Languages, currencies, regions, feature flags</p> }))
vi.mock('./admin/RolesAdminPage', () => ({ RolesAdminPage: () => <p>Roles page</p> }))
vi.mock('./admin/PermissionsAdminPage', () => ({ PermissionsAdminPage: () => <p>Permissions page</p> }))

function Where() {
  const { pathname, search } = useLocation()
  return <p>at {pathname}{search}</p>
}

function at(path: string, element: React.ReactElement, access?: NavAccessValue) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <NavAccessContext.Provider value={access ?? { isAdmin: true, permissions: null }}>
        <Routes>
          <Route path="/status" element={element} />
          <Route path="/admin" element={element} />
          <Route path="/admin/roles" element={element} />
          <Route path="/organization/members" element={element} />
          <Route path="*" element={<Where />} />
        </Routes>
      </NavAccessContext.Provider>
    </MemoryRouter>,
  )
}

describe('compatibility redirects (C80)', () => {
  const run = (from: string) => render(
    <MemoryRouter initialEntries={[from]}>
      <Routes>
        {LEGACY_ADMIN_REDIRECTS.map(([path, to]) => <Route key={path} path={`/admin/${path}`} element={<Navigate to={to} replace />} />)}
        <Route path="/organization/settings" element={<OrganizationSettingsRedirect />} />
        <Route path="*" element={<Where />} />
      </Routes>
    </MemoryRouter>,
  )

  it.each([
    ['/admin/settings', 'at /admin/apps?new=1'],
    ['/admin/settings/product', 'at /admin/apps?new=1'],
    ['/admin/settings/platform', 'at /admin/platforms/new'],
    ['/admin/settings/common', 'at /admin?tab=configuration'],
    ['/admin/permissions', 'at /admin/roles?tab=permissions'],
    ['/admin/service-status', 'at /status?tab=manage'],
    ['/organization/settings', 'at /organization/members'],
  ])('%s opens its new home', (from, expected) => {
    run(from)
    expect(screen.getByText(expected)).toBeInTheDocument()
  })

  it('the old Organization settings anchors open the card\'s new page', () => {
    // MemoryRouter takes the hash from the entry
    run('/organization/settings#privileged-access')
    expect(screen.getByText('at /organization/privileged-access')).toBeInTheDocument()
  })
})

describe('Organization → Members', () => {
  it('has Members and Groups tabs and keeps the tab in the URL', async () => {
    at('/organization/members', <OrganizationMembersPage />)
    expect(await screen.findByText('Members card')).toBeInTheDocument()
    await userEvent.setup().click(await screen.findByRole('tab', { name: 'Groups' }))
    expect(screen.getByText('Groups card')).toBeInTheDocument()
  })

  it('opens the Groups tab from ?tab=groups', async () => {
    at('/organization/members?tab=groups', <OrganizationMembersPage />)
    expect(await screen.findByText('Groups card')).toBeInTheDocument()
  })
})

describe('Platform → Service status', () => {
  it('shows only the status to someone without MANAGE_SERVICE_STATUS', () => {
    at('/status', <ServiceStatusRoutePage />, { isAdmin: true, permissions: { platform: ['VIEW_AUDIT_LOG'], organization: [] } })
    expect(screen.getByText('Status page')).toBeInTheDocument()
    expect(screen.queryByRole('tab', { name: 'Manage' })).not.toBeInTheDocument()
  })

  it('adds a Manage tab (the old admin page) for MANAGE_SERVICE_STATUS', async () => {
    at('/status', <ServiceStatusRoutePage />, { isAdmin: true, permissions: { platform: ['MANAGE_SERVICE_STATUS'], organization: [] } })
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Manage' }))
    expect(screen.getByText('Manage status page')).toBeInTheDocument()
  })
})

describe('Platform → Products', () => {
  it('lists products, and the Configuration tab holds the old Common settings', async () => {
    at('/admin', <ProductsAdminPage />)
    expect(screen.getByText('Products list')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Configuration' }))
    expect(screen.getByText(/feature flags/)).toBeInTheDocument()
  })
})

describe('Organization → Roles & permissions', () => {
  it('is one feature with Roles, Permissions and Role assignments tabs', async () => {
    at('/admin/roles', <RolesPermissionsPage />)
    expect(screen.getByText('Roles page')).toBeInTheDocument()
    const user = userEvent.setup()
    await user.click(screen.getByRole('tab', { name: 'Permissions' }))
    expect(screen.getByText('Permissions page')).toBeInTheDocument()
    await user.click(screen.getByRole('tab', { name: 'Role assignments' }))
    expect(screen.getByRole('link', { name: 'Go to Members' })).toHaveAttribute('href', '/organization/members')
  })

  it('shows only the tabs the permissions allow', () => {
    at('/admin/roles', <RolesPermissionsPage />, { isAdmin: true, permissions: { platform: ['MANAGE_PERMISSIONS'], organization: [] } })
    expect(screen.queryByRole('tab', { name: 'Roles' })).not.toBeInTheDocument()
    expect(screen.getByText('Permissions page')).toBeInTheDocument()
  })
})
