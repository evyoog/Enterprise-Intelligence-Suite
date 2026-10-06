import type { ComponentType } from 'react'
import {
  Building2, KeyRound, LayoutDashboard, LayoutGrid, Layers, Package, ScrollText, Settings, ShieldCheck,
  SlidersHorizontal, Store, UserCheck, UserCog, UsersRound, Palette, Boxes, Activity, ClipboardList, BookOpen,
  LifeBuoy, Star, Handshake, CreditCard, PlugZap, Landmark, Radio, Cable, Settings2, SearchCheck, Library,
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
  // REQ-KNW-005 Knowledge Center (C71–C77, was 11.01 Knowledge Base): public reads.
  workspace.push({ key: 'knowledgeCenter', labelKey: 'knowledgeCenter', to: '/knowledge', icon: BookOpen })
  // 12.01 Ticket Management (sprint 2027.1.2): any authenticated customer.
  if (hasCustomerWorkspace) {
    workspace.push({ key: 'support', labelKey: 'support', to: '/support/tickets', icon: LifeBuoy })
  }
  // 08 Billing & Payments (sprint 2026.4.3, REQ-BIL-001, C46): an
  // individual customer's own invoices/payment methods.
  if (hasCustomerWorkspace) {
    workspace.push({ key: 'billing', labelKey: 'billing', to: '/billing', icon: CreditCard })
  }
  // 01.03 Global Search (sprint 2027.1.3): C44 moved this into the top bar
  // (AppShell's TopBarSearch) instead of a sidebar destination — search is a
  // utility, not a place you navigate to. "/search" still exists as a
  // deep-link target ("See all results"), just not a permanent nav item.
  // 14.01.01.01 Register provider (sprint 2027.2.1): public — a prospective
  // partner applies before it has any Vyoog identity, same as /register.
  workspace.push({ key: 'becomeAPartner', labelKey: 'becomeAPartner', to: '/partners/apply', icon: Handshake })

  const organizationItems: AppNavItem[] = []
  // C69: members, groups, MFA policy and privileged access (moved off the dashboard).
  if (organization('MANAGE_ORGANIZATION')) {
    organizationItems.push({ key: 'orgSettings', labelKey: 'orgSettings', to: '/organization/settings', icon: Settings2 })
  }
  if (organization('MANAGE_ORGANIZATION')) {
    organizationItems.push({ key: 'identityFederation', labelKey: 'identityFederation', to: '/organization/identity-federation', icon: Building2 })
  }
  // 09 Order & Provisioning Management (sprint 2027.1.1): any organization
  // member may submit an order; MANAGE_ORDERS (ORG_ADMIN) also sees the
  // pending-approvals section on the same page — see OrganizationOrdersPage.
  if (inOrganization) {
    organizationItems.push({ key: 'orders', labelKey: 'orders', to: '/organization/orders', icon: ClipboardList })
  }
  // 08 Billing & Payments (C46), FRD Open question 5: organization admins
  // only, via the existing MANAGE_ORGANIZATION permission.
  if (organization('MANAGE_ORGANIZATION')) {
    organizationItems.push({ key: 'orgBilling', labelKey: 'billing', to: '/organization/billing', icon: CreditCard })
  }

  const admin: AppNavItem[] = []
  // Platform admin dashboard (C53): first item, same position the GoodFood
  // reference image's own sidebar gives "Dashboard" — but the admin INDEX
  // route stays Platforms (C44's own documented landing-page decision);
  // this is a destination a platform admin navigates to, not the default.
  if (platform('VIEW_PLATFORM_DASHBOARD')) {
    admin.push({ key: 'platformDashboard', labelKey: 'platformDashboard', to: '/admin/dashboard', icon: LayoutDashboard })
  }
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
  // REQ-KNW-008: Knowledge Management for platform administrators and the
  // persons given a knowledge permission (contributor or publisher) — not
  // only ADMIN, so it is checked on the permission list itself.
  const knowledgeStaff = permissions === null
    ? isAdmin
    : permissions.platform.includes('KNOWLEDGE_CONTRIBUTE') || permissions.platform.includes('MANAGE_KNOWLEDGE_BASE')
  if (knowledgeStaff) {
    admin.push({ key: 'knowledgeManagement', labelKey: 'knowledgeManagement', to: '/knowledge-management', icon: Library })
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
  // 08 Billing & Payments (sprint 2026.4.3, C46): its own permission —
  // refunding a payment and seeing every customer's invoices is a distinct,
  // sensitive responsibility from every other admin capability above.
  if (platform('MANAGE_BILLING')) {
    admin.push({
      key: 'adminBilling', labelKey: 'billing', to: '/admin/billing', icon: CreditCard,
      matchPrefixes: ['/admin/billing'],
      children: [
        { key: 'adminBillingInvoices', labelKey: 'billingInvoicesPayments', to: '/admin/billing', icon: CreditCard },
        { key: 'adminPaymentGateway', labelKey: 'paymentGateway', to: '/admin/billing/payment-gateway', icon: PlugZap },
        { key: 'adminBillingSettings', labelKey: 'billingSettings', to: '/admin/billing/settings', icon: Landmark },
      ],
    })
  }
  // REQ-INT-001/REQ-INT-002 (C61, C62): platform events and API-key usage.
  if (platform('MANAGE_INTEGRATIONS')) {
    admin.push({
      key: 'adminIntegrations', labelKey: 'integrations', to: '/admin/integrations/events', icon: Cable,
      matchPrefixes: ['/admin/integrations'],
      children: [
        { key: 'adminPlatformEvents', labelKey: 'platformEvents', to: '/admin/integrations/events', icon: Radio },
        { key: 'adminApiKeys', labelKey: 'apiKeys', to: '/admin/integrations/api-keys', icon: KeyRound },
      ],
    })
  }
  // C70: search index status and rebuild, synonyms and search insights.
  if (platform('MANAGE_SEARCH')) {
    admin.push({ key: 'adminSearch', labelKey: 'searchAdmin', to: '/admin/search', icon: SearchCheck })
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

/** Router state that lets a signed-in user see the public website at "/"
 * (C79). Without it, "/" sends a signed-in user into the tool (PublicOnly),
 * which is what sign-in and SSO returns rely on. Browsers keep this state on
 * reload, so a refreshed landing page stays the landing page. */
export const WEBSITE_HOME_STATE = { website: true } as const

export function wantsWebsite(state: unknown) {
  return typeof state === 'object' && state !== null && (state as { website?: unknown }).website === true
}
