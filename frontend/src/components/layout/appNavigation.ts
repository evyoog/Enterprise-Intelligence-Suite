import type { ComponentType } from 'react'
import {
  Building2, KeyRound, LayoutDashboard, LayoutGrid, Layers, Package, ScrollText, Settings, ShieldCheck,
  SlidersHorizontal, Store, UserCheck, UserCog, UsersRound, Palette, Boxes, Activity, ClipboardList, BookOpen,
  LifeBuoy, Search, Star, Handshake,
} from 'lucide-react'
import type { MyPermissions } from '../../api/myPermissionsApi'

export interface AppNavItem {
  key: string
  /** i18n key under appShell.nav */
  labelKey: string
  to: string
  icon: ComponentType<{ size?: number }>
  /** Extra paths that also mark this item as current. */
  matchPrefixes?: string[]
  children?: AppNavItem[]
}

export interface AppNavSection {
  key: string
  /** i18n key under appShell.sections */
  labelKey: string
  items: AppNavItem[]
}

export interface NavAccess {
  isAdmin: boolean
  /** null while loading or if the lookup failed. */
  permissions: MyPermissions | null
}

/** Where the signed-in tool opens. BusinessDashboardPage itself sends members
 * without MANAGE_ORGANIZATION and individuals on to /my/products. */
export function appHomePath(isAdmin: boolean) {
  return isAdmin ? '/admin' : '/organization/business-dashboard'
}

/**
 * The signed-in sidebar, filtered to what this user can use. Each item is
 * gated on the same permission its backend endpoints check (SecurityConfig /
 * OrganizationSelfService), so nothing is offered that would only return 403.
 * This is UI filtering only; the backend stays the security boundary.
 *
 * While permissions are unknown, a platform admin sees the full admin menu
 * (the behavior before this change) and organization-only items are hidden.
 */
export function buildAppNavigation({ isAdmin, permissions }: NavAccess): AppNavSection[] {
  const platform = (name: string) => isAdmin && (permissions === null || permissions.platform.includes(name))
  const organization = (name: string) => permissions !== null && permissions.organization.includes(name)
  const inOrganization = permissions !== null && permissions.organization.length > 0
  // A platform admin normally has no customer account of their own.
  const hasCustomerWorkspace = !isAdmin || inOrganization

  const workspace: AppNavItem[] = []
  if (hasCustomerWorkspace && (permissions === null ? !isAdmin : organization('MANAGE_ORGANIZATION'))) {
    workspace.push({ key: 'dashboard', labelKey: 'dashboard', to: '/organization/business-dashboard', icon: LayoutDashboard })
  }
  if (hasCustomerWorkspace) {
    workspace.push({ key: 'myProducts', labelKey: 'myProducts', to: '/my/products', icon: Package })
  }
  workspace.push({ key: 'catalog', labelKey: 'catalog', to: '/products', icon: Store })
  // REQ-PRT-001 (C26): every signed-in user sees the status page.
  workspace.push({ key: 'serviceStatus', labelKey: 'serviceStatus', to: '/status', icon: Activity })
  // 11.01 Knowledge Base (sprint 2027.1.1): public reads, same as the catalog above.
  workspace.push({ key: 'knowledgeBase', labelKey: 'knowledgeBase', to: '/knowledge-base', icon: BookOpen })
  // 12.01 Ticket Management (sprint 2027.1.2): any authenticated customer.
  if (hasCustomerWorkspace) {
    workspace.push({ key: 'support', labelKey: 'support', to: '/support/tickets', icon: LifeBuoy })
  }
  // 01.03 Global Search (sprint 2027.1.3): public, same as the catalog above.
  workspace.push({ key: 'search', labelKey: 'search', to: '/search', icon: Search })
  // 14.01.01.01 Register provider (sprint 2027.2.1): public — a prospective
  // partner applies before it has any Vyoog identity, same as /register.
  workspace.push({ key: 'becomeAPartner', labelKey: 'becomeAPartner', to: '/partners/apply', icon: Handshake })

  const organizationItems: AppNavItem[] = []
  if (organization('MANAGE_ORGANIZATION')) {
    organizationItems.push({ key: 'identityFederation', labelKey: 'identityFederation', to: '/organization/identity-federation', icon: Building2 })
  }
  // 09 Order & Provisioning Management (sprint 2027.1.1): any organization
  // member may submit an order; MANAGE_ORDERS (ORG_ADMIN) also sees the
  // pending-approvals section on the same page — see OrganizationOrdersPage.
  if (inOrganization) {
    organizationItems.push({ key: 'orders', labelKey: 'orders', to: '/organization/orders', icon: ClipboardList })
  }

  const admin: AppNavItem[] = []
  if (platform('MANAGE_CATALOG')) {
    admin.push(
      { key: 'platforms', labelKey: 'platforms', to: '/admin', icon: Layers, matchPrefixes: ['/admin/platforms'] },
      { key: 'apps', labelKey: 'apps', to: '/admin/apps', icon: LayoutGrid, matchPrefixes: ['/admin/products'] },
    )
  }
  if (platform('MANAGE_REGISTRATIONS')) admin.push({ key: 'registrations', labelKey: 'registrations', to: '/admin/registrations', icon: UserCheck })
  if (platform('MANAGE_PRIVILEGED_ACCESS')) admin.push({ key: 'privilegedAccess', labelKey: 'privilegedAccess', to: '/admin/privileged-access', icon: KeyRound })
  if (platform('MANAGE_ROLES')) admin.push({ key: 'roles', labelKey: 'roles', to: '/admin/roles', icon: UsersRound })
  if (platform('MANAGE_PERMISSIONS')) admin.push({ key: 'permissions', labelKey: 'permissions', to: '/admin/permissions', icon: ShieldCheck })
  if (platform('VIEW_AUDIT_LOG')) admin.push({ key: 'auditLog', labelKey: 'auditLog', to: '/admin/audit-log', icon: ScrollText })
  if (platform('MANAGE_SERVICE_STATUS')) {
    admin.push({ key: 'adminServiceStatus', labelKey: 'serviceStatus', to: '/admin/service-status', icon: Activity })
  }
  if (platform('MANAGE_KNOWLEDGE_BASE')) {
    admin.push({ key: 'adminKnowledgeBase', labelKey: 'knowledgeBase', to: '/admin/knowledge-base', icon: BookOpen })
  }
  if (platform('MANAGE_SUPPORT_TICKETS')) {
    admin.push({ key: 'adminSupportTickets', labelKey: 'support', to: '/admin/support/tickets', icon: LifeBuoy })
  }
  if (platform('MANAGE_REVIEWS')) {
    admin.push({ key: 'adminReviews', labelKey: 'reviews', to: '/admin/reviews', icon: Star })
  }
  if (platform('MANAGE_PARTNERS')) {
    admin.push({ key: 'adminPartners', labelKey: 'partners', to: '/admin/partners', icon: Handshake })
  }
  if (platform('MANAGE_CATALOG')) {
    admin.push({
      key: 'settings', labelKey: 'settings', to: '/admin/settings/product', icon: Settings, matchPrefixes: ['/admin/settings'],
      children: [
        { key: 'settingsApp', labelKey: 'settingsApp', to: '/admin/settings/product', icon: Boxes },
        { key: 'settingsPlatform', labelKey: 'settingsPlatform', to: '/admin/settings/platform', icon: Layers },
        { key: 'settingsCommon', labelKey: 'settingsCommon', to: '/admin/settings/common', icon: SlidersHorizontal },
      ],
    })
  }

  const account: AppNavItem[] = [
    { key: 'security', labelKey: 'security', to: '/account/security', icon: UserCog },
    { key: 'preferences', labelKey: 'preferences', to: '/account/preferences', icon: Palette },
  ]

  return [
    { key: 'workspace', labelKey: 'workspace', items: workspace },
    { key: 'organization', labelKey: 'organization', items: organizationItems },
    { key: 'administration', labelKey: 'administration', items: admin },
    { key: 'account', labelKey: 'account', items: account },
  ].filter((section) => section.items.length > 0)
}

/** Whether `item` is the current page. "/admin" only matches exactly, so it
 * does not light up for every admin page. */
export function isNavItemActive(item: AppNavItem, pathname: string) {
  if (pathname === item.to) return true
  if (item.to !== '/admin' && pathname.startsWith(item.to + '/')) return true
  return (item.matchPrefixes ?? []).some((prefix) => pathname.startsWith(prefix))
}
