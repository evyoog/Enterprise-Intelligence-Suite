import type { ComponentType } from 'react'
import {
  Activity, BookOpen, Cable, ClipboardList, CreditCard, FolderTree, Handshake, KeyRound, Landmark, LayoutDashboard,
  LayoutGrid, Layers, LifeBuoy, NotebookPen, Package, PlugZap, Radio, ScrollText, SearchCheck, ShieldCheck, Star,
  Building2, Store, UserCog, UsersRound, Wrench, FileText, Receipt, Gauge,
} from 'lucide-react'

/**
 * The one navigation config (C80). Every sidebar item is declared here once:
 * its label, icon, route, the permission that shows it and the group it lives
 * in. `buildAppNavigation` filters this at render time; route guards and the
 * backend stay the real enforcement, hiding an item is only UX.
 *
 * Rules check permissions, never role names, so intermediate roles (billing
 * manager, support agent, auditor) get exactly the items their permissions
 * allow. The one exception is `admin`, which mirrors the existing `/admin/*`
 * route guard (RequireAdmin).
 */
export type NavGroup = 'workspace' | 'organization' | 'platform' | 'operations' | 'help' | 'billing'

/** Group order, top to bottom. A group with no visible item is not rendered. */
export const NAV_GROUP_ORDER: NavGroup[] = ['workspace', 'organization', 'platform', 'operations', 'help', 'billing']

export type NavRule =
  /** A platform permission. Needs the platform-admin role (like the /admin guard); while permissions load, an admin sees it. */
  | { platform: string }
  /** A platform permission held in the permission list itself (knowledge contributors need not be admins). */
  | { platformGrant: string[] }
  | { organization: string }
  | { signedIn: true }
  /** Has a customer workspace: not a platform admin without an organization. */
  | { customer: true }
  | { inOrganization: true }
  | { admin: true }
  /** Permissions are still loading or the lookup failed. */
  | { loading: true }
  | { any: NavRule[] }
  | { all: NavRule[] }
  | { not: NavRule }

export interface NavDef {
  id: string
  /** i18n key under appShell.nav */
  labelKey: string
  /** i18n key under appShell.navHint: a short description shown as a tooltip. */
  hintKey?: string
  icon: ComponentType<{ size?: number }>
  /** Existing route. A list picks the first entry whose `when` passes (the last may omit it). */
  to?: string | { when?: NavRule; to: string }[]
  group: NavGroup
  /** Shows the item only when the rule passes. */
  when?: NavRule
  /** Moves the item to another group when a rule passes (first match wins). */
  groupOverrides?: { when: NavRule; group: NavGroup }[]
  /** Extra paths that also mark the item as current. */
  matchPrefixes?: string[]
  /** For items sharing a path: current only when the query string contains one of these. */
  searchAny?: string[]
  /** For items sharing a path: not current when the query string contains one of these. */
  excludeSearch?: string[]
  /** Hides a child when the rule passes (used for reader vs editor children). */
  unless?: NavRule
  children?: NavDef[]
  /** In these groups the children are shown as top-level items, the group label being the parent. */
  flattenIn?: NavGroup[]
}

const platform = (name: string): NavRule => ({ platform: name })
const organization = (name: string): NavRule => ({ organization: name })
const knowledgeStaff: NavRule = { platformGrant: ['KNOWLEDGE_CONTRIBUTE', 'MANAGE_KNOWLEDGE_BASE'] }
const orgAdmin = organization('MANAGE_ORGANIZATION')
/** People who run things: they see Support and Billing under Operations; everyone else sees them as Help / Billing. */
const opsTier: NavRule = { any: [orgAdmin, { admin: true }] }

export const NAV_CONFIG: NavDef[] = [
  // ---------------------------------------------------------------- WORKSPACE
  {
    id: 'dashboard', labelKey: 'dashboard', icon: LayoutDashboard, group: 'workspace',
    to: [
      { when: platform('VIEW_PLATFORM_DASHBOARD'), to: '/admin/dashboard' },
      { to: '/organization/business-dashboard' },
    ],
    // Members have no dashboard page; they open on My applications.
    when: {
      any: [
        platform('VIEW_PLATFORM_DASHBOARD'),
        { all: [{ customer: true }, { any: [orgAdmin, { all: [{ loading: true }, { not: { admin: true } }] }] }] },
      ],
    },
  },
  { id: 'myApplications', labelKey: 'myApplications', icon: Package, group: 'workspace', to: '/my/products', when: { customer: true } },
  { id: 'catalog', labelKey: 'catalog', hintKey: 'catalog', icon: Store, group: 'workspace', to: '/products', when: { signedIn: true } },
  {
    id: 'knowledgeCenter', labelKey: 'knowledgeCenter', icon: BookOpen, group: 'workspace', to: '/knowledge',
    matchPrefixes: ['/knowledge', '/knowledge-management'],
    children: [
      // Readers
      { id: 'knowledgeArticles', labelKey: 'knowledgeArticles', icon: FileText, group: 'workspace', to: '/knowledge/getting-started', unless: knowledgeStaff },
      { id: 'knowledgeGuides', labelKey: 'knowledgeGuides', icon: BookOpen, group: 'workspace', to: '/knowledge/guides', unless: knowledgeStaff },
      { id: 'knowledgeFaqs', labelKey: 'knowledgeFaqs', icon: LifeBuoy, group: 'workspace', to: '/knowledge/faqs', unless: knowledgeStaff },
      // Authors and publishers
      { id: 'knowledgeManageArticles', labelKey: 'knowledgeArticles', icon: FileText, group: 'workspace', to: '/knowledge-management/content', when: knowledgeStaff, excludeSearch: ['status=DRAFT'] },
      { id: 'knowledgeCategories', labelKey: 'knowledgeCategories', icon: FolderTree, group: 'workspace', to: '/knowledge-management/taxonomy', when: { platformGrant: ['MANAGE_KNOWLEDGE_BASE'] } },
      { id: 'knowledgeDrafts', labelKey: 'knowledgeDrafts', icon: NotebookPen, group: 'workspace', to: '/knowledge-management/content?status=DRAFT', when: knowledgeStaff, searchAny: ['status=DRAFT'] },
      { id: 'knowledgeManage', labelKey: 'knowledgeManage', icon: Wrench, group: 'workspace', to: '/knowledge-management', when: knowledgeStaff },
    ],
  },

  // ------------------------------------------------------------- ORGANIZATION
  { id: 'members', labelKey: 'peopleStructure', icon: UsersRound, group: 'organization', to: '/organization/members',
    // Administrators, and members allowed only to send invitations (REQ-TEN-008).
    when: { any: [orgAdmin, organization('INVITE_USERS')] } },
  {
    id: 'rolesPermissions', labelKey: 'rolesPermissions', icon: ShieldCheck, group: 'organization', to: '/admin/roles',
    matchPrefixes: ['/admin/permissions'],
    when: { any: [platform('MANAGE_ROLES'), platform('MANAGE_PERMISSIONS')] },
  },
  {
    id: 'registrations', labelKey: 'organizationsDirectory', icon: Building2, group: 'organization', to: '/admin/organizations',
    matchPrefixes: ['/admin/organizations'], when: platform('MANAGE_REGISTRATIONS'),
  },
  {
    id: 'privilegedAccess', labelKey: 'privilegedAccess', icon: KeyRound, group: 'organization',
    to: [
      { when: platform('MANAGE_PRIVILEGED_ACCESS'), to: '/admin/privileged-access' },
      { to: '/organization/privileged-access' },
    ],
    when: { any: [platform('MANAGE_PRIVILEGED_ACCESS'), organization('MANAGE_PRIVILEGED_ACCESS')] },
  },
  { id: 'identityFederation', labelKey: 'orgSecurity', icon: UserCog, group: 'organization', to: '/organization/identity-federation', when: orgAdmin },
  // Any organization member may submit an order; MANAGE_ORDERS also approves, so it is an Organization item for them.
  {
    id: 'orders', labelKey: 'orders', icon: ClipboardList, group: 'workspace', to: '/organization/orders', when: { inOrganization: true },
    groupOverrides: [{ when: organization('MANAGE_ORDERS'), group: 'organization' }],
  },

  // ----------------------------------------------------------------- PLATFORM
  {
    id: 'products', labelKey: 'platforms', hintKey: 'platforms', icon: Layers, group: 'platform', to: '/admin',
    matchPrefixes: ['/admin/platforms'], when: platform('MANAGE_CATALOG'),
  },
  {
    id: 'applications', labelKey: 'apps', icon: LayoutGrid, group: 'platform', to: '/admin/apps',
    matchPrefixes: ['/admin/products'], when: platform('MANAGE_CATALOG'),
  },
  {
    id: 'integrations', labelKey: 'integrations', icon: Cable, group: 'platform', matchPrefixes: ['/admin/integrations'],
    when: platform('MANAGE_INTEGRATIONS'),
    children: [
      { id: 'platformEvents', labelKey: 'platformEvents', icon: Radio, group: 'platform', to: '/admin/integrations/events' },
      { id: 'apiKeys', labelKey: 'apiKeys', icon: KeyRound, group: 'platform', to: '/admin/integrations/api-keys' },
    ],
  },
  { id: 'searchAdmin', labelKey: 'searchAdmin', icon: SearchCheck, group: 'platform', to: '/admin/search', when: platform('MANAGE_SEARCH') },
  // One status entry for everyone who runs things. Managing it (incidents, component status) is a tab on the page.
  { id: 'serviceStatus', labelKey: 'serviceStatus', icon: Activity, group: 'platform', to: '/status', when: opsTier },

  // --------------------------------------------------------------- OPERATIONS
  {
    id: 'billing', labelKey: 'billing', icon: CreditCard, group: 'billing', matchPrefixes: ['/billing', '/organization/billing', '/admin/billing'],
    groupOverrides: [{ when: { any: [platform('MANAGE_BILLING'), orgAdmin] }, group: 'operations' }],
    when: { any: [{ customer: true }, platform('MANAGE_BILLING')] },
    flattenIn: ['billing'],
    children: [
      {
        id: 'billingOverview', labelKey: 'billingOverview', icon: Gauge, group: 'billing',
        to: [{ when: orgAdmin, to: '/organization/billing' }, { to: '/billing' }],
        when: { all: [{ customer: true }, { not: platform('MANAGE_BILLING') }] },
        excludeSearch: ['tab=invoices', 'tab=methods', 'tab=history'],
      },
      {
        id: 'billingInvoices', labelKey: 'billingInvoicesPayments', icon: Receipt, group: 'billing',
        to: [{ when: orgAdmin, to: '/organization/billing?tab=invoices' }, { to: '/billing?tab=invoices' }],
        when: { all: [{ customer: true }, { not: platform('MANAGE_BILLING') }] },
        searchAny: ['tab=invoices', 'tab=methods', 'tab=history'],
      },
      { id: 'adminBillingInvoices', labelKey: 'billingInvoicesPayments', icon: Receipt, group: 'operations', to: '/admin/billing', when: platform('MANAGE_BILLING') },
      { id: 'adminPaymentGateway', labelKey: 'paymentGateway', icon: PlugZap, group: 'operations', to: '/admin/billing/payment-gateway', when: platform('MANAGE_BILLING') },
      { id: 'adminBillingSettings', labelKey: 'billingSettings', icon: Landmark, group: 'operations', to: '/admin/billing/settings', when: platform('MANAGE_BILLING') },
    ],
  },
  {
    id: 'support', labelKey: 'support', icon: LifeBuoy, group: 'help',
    to: [
      { when: platform('MANAGE_SUPPORT_TICKETS'), to: '/admin/support/tickets' },
      { to: '/support/tickets' },
    ],
    matchPrefixes: ['/support', '/admin/support'],
    when: { any: [platform('MANAGE_SUPPORT_TICKETS'), { customer: true }] },
    groupOverrides: [{ when: { any: [platform('MANAGE_SUPPORT_TICKETS'), orgAdmin] }, group: 'operations' }],
  },
  { id: 'reviews', labelKey: 'reviews', icon: Star, group: 'operations', to: '/admin/reviews', when: platform('MANAGE_REVIEWS') },
  { id: 'partners', labelKey: 'partners', icon: Handshake, group: 'operations', to: '/admin/partners', when: platform('MANAGE_PARTNERS') },
  { id: 'auditLog', labelKey: 'auditLog', icon: ScrollText, group: 'operations', to: '/admin/audit-log', when: platform('VIEW_AUDIT_LOG') },

]


