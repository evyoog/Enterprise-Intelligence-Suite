import { describe, expect, it } from 'vitest'
import { appHomePath, buildAppNavigation, findNavTrail, isNavBranchActive, isNavItemActive, type AppNavSection } from './appNavigation'
import { NAV_CONFIG, type NavDef } from './navConfig'

/** group -> ["item", "parent>child", ...] */
const tree = (sections: AppNavSection[]) =>
  Object.fromEntries(sections.map((s) => [
    s.key,
    s.items.flatMap((i) => (i.children ? [i.key, ...i.children.map((c) => `${i.key}>${c.key}`)] : [i.key])),
  ]))
const nav = (isAdmin: boolean, platform: string[] | null, organization: string[] = []) =>
  buildAppNavigation({ isAdmin, permissions: platform === null ? null : { platform, organization } })
const allItems = (sections: AppNavSection[]) => sections.flatMap((s) => s.items.flatMap((i) => [i, ...(i.children ?? [])]))
const ORG_ADMIN = ['MANAGE_ORGANIZATION', 'MANAGE_USERS', 'MANAGE_PRODUCT_ACCESS', 'MANAGE_PRIVILEGED_ACCESS', 'MANAGE_ORDERS']

describe('nav config', () => {
  it('has no duplicate ids', () => {
    const ids: string[] = []
    const walk = (defs: NavDef[]) => defs.forEach((d) => { ids.push(d.id); if (d.children) walk(d.children) })
    walk(NAV_CONFIG)
    expect(new Set(ids).size).toBe(ids.length)
  })
})

describe('regular member (C80 §3)', () => {
  const t = tree(nav(false, [], []))

  it('sees the short task-focused menu and no admin module', () => {
    expect(t).toEqual({
      workspace: ['myApplications', 'catalog', 'knowledgeCenter', 'knowledgeCenter>knowledgeArticles', 'knowledgeCenter>knowledgeGuides', 'knowledgeCenter>knowledgeFaqs'],
      help: ['support'],
      billing: ['billingOverview', 'billingInvoices'],
      account: ['security', 'preferences'],
    })
  })

  it('has no Organization, Platform or Operations group, no Service status and no Become-a-partner item', () => {
    expect(Object.keys(t)).not.toContain('organization')
    expect(Object.keys(t)).not.toContain('platform')
    expect(Object.keys(t)).not.toContain('operations')
    expect(JSON.stringify(t)).not.toMatch(/serviceStatus|partners|becomeAPartner|auditLog|integrations|search/i)
  })

  it('an organization member also gets Orders in the workspace, still no Organization group', () => {
    const member = tree(nav(false, [], ['MANAGE_PRIVILEGED_ACCESS_SELF']))
    expect(member.workspace).toContain('orders')
    expect(member.organization).toBeUndefined()
  })
})

describe('organization admin (C80 §2)', () => {
  const sections = nav(false, [], ORG_ADMIN)
  const t = tree(sections)

  it('sees workspace, organization, status, billing and support under their groups', () => {
    expect(t.workspace).toEqual(['dashboard', 'myApplications', 'catalog', 'knowledgeCenter', 'knowledgeCenter>knowledgeArticles', 'knowledgeCenter>knowledgeGuides', 'knowledgeCenter>knowledgeFaqs'])
    expect(t.organization).toEqual(['members', 'privilegedAccess', 'identityFederation', 'orders'])
    expect(t.platform).toEqual(['serviceStatus'])
    expect(t.operations).toEqual(['billing', 'billing>billingOverview', 'billing>billingInvoices', 'support'])
    expect(t.account).toEqual(['security', 'preferences'])
    expect(t.help).toBeUndefined()
    expect(t.billing).toBeUndefined()
  })

  it('points Billing and Privileged access at the organization pages', () => {
    const items = Object.fromEntries(allItems(sections).map((i) => [i.key, i.to]))
    expect(items.billingOverview).toBe('/organization/billing')
    expect(items.billingInvoices).toBe('/organization/billing?tab=invoices')
    expect(items.privilegedAccess).toBe('/organization/privileged-access')
    expect(items.dashboard).toBe('/organization/business-dashboard')
    expect(items.support).toBe('/support/tickets')
  })
})

describe('platform administrator (C80 §2)', () => {
  const sections = nav(true, null)
  const t = tree(sections)

  it('sees every group while permissions are still loading', () => {
    expect(t.workspace).toEqual([
      'dashboard', 'catalog', 'knowledgeCenter', 'knowledgeCenter>knowledgeManageArticles', 'knowledgeCenter>knowledgeCategories',
      'knowledgeCenter>knowledgeDrafts', 'knowledgeCenter>knowledgeManage',
    ])
    expect(t.organization).toEqual(['rolesPermissions', 'registrations', 'privilegedAccess'])
    expect(t.platform).toEqual(['products', 'applications', 'integrations', 'integrations>platformEvents', 'integrations>apiKeys', 'searchAdmin', 'serviceStatus'])
    expect(t.operations).toEqual([
      'billing', 'billing>adminBillingInvoices', 'billing>adminPaymentGateway', 'billing>adminBillingSettings',
      'support', 'reviews', 'partners', 'auditLog',
    ])
    expect(t.account).toEqual(['security', 'preferences'])
  })

  it('has no generic Settings item and no separate Knowledge Management or sidebar Search', () => {
    const keys = allItems(sections).map((i) => i.key)
    expect(keys).not.toContain('settings')
    expect(keys.filter((k) => /knowledgeManagement|^search$/.test(k))).toEqual([])
  })

  it('uses the platform routes', () => {
    const items = Object.fromEntries(allItems(sections).map((i) => [i.key, i.to]))
    expect(items.dashboard).toBe('/admin/dashboard')
    expect(items.support).toBe('/admin/support/tickets')
    expect(items.privilegedAccess).toBe('/admin/privileged-access')
    expect(items.products).toBe('/admin')
    expect(items.applications).toBe('/admin/apps')
  })

  it('never shows administration to a non-admin, whatever the permission list says', () => {
    const t2 = tree(nav(false, ['MANAGE_CATALOG', 'MANAGE_BILLING', 'VIEW_AUDIT_LOG'], []))
    expect(t2.platform).toBeUndefined()
    expect(t2.operations).toBeUndefined()
  })
})

describe('intermediate roles get exactly what their permissions allow', () => {
  it('billing manager', () => {
    const t = tree(nav(true, ['MANAGE_BILLING']))
    expect(t.workspace).toEqual(['catalog', 'knowledgeCenter', 'knowledgeCenter>knowledgeArticles', 'knowledgeCenter>knowledgeGuides', 'knowledgeCenter>knowledgeFaqs'])
    expect(t.platform).toEqual(['serviceStatus'])
    expect(t.operations).toEqual(['billing', 'billing>adminBillingInvoices', 'billing>adminPaymentGateway', 'billing>adminBillingSettings'])
    expect(t.organization).toBeUndefined()
  })

  it('support agent', () => {
    const t = tree(nav(true, ['MANAGE_SUPPORT_TICKETS']))
    expect(t.operations).toEqual(['support'])
    expect(t.help).toBeUndefined()
  })

  it('auditor', () => {
    expect(tree(nav(true, ['VIEW_AUDIT_LOG'])).operations).toEqual(['auditLog'])
  })

  it('knowledge contributor (not an administrator) gets the author children, but not Categories', () => {
    const t = tree(nav(false, ['KNOWLEDGE_CONTRIBUTE'], []))
    expect(t.workspace).toEqual([
      'myApplications', 'catalog', 'knowledgeCenter', 'knowledgeCenter>knowledgeManageArticles', 'knowledgeCenter>knowledgeDrafts', 'knowledgeCenter>knowledgeManage',
    ])
  })

  it('knowledge publisher also gets Categories', () => {
    const t = tree(nav(false, ['MANAGE_KNOWLEDGE_BASE'], []))
    expect(t.workspace).toContain('knowledgeCenter>knowledgeCategories')
  })

  it('roles and permissions is one item for either permission', () => {
    expect(tree(nav(true, ['MANAGE_ROLES'])).organization).toEqual(['rolesPermissions'])
    expect(tree(nav(true, ['MANAGE_PERMISSIONS'])).organization).toEqual(['rolesPermissions'])
  })

  it('Service status shows only once for everyone, with no empty groups', () => {
    for (const sections of [nav(false, [], []), nav(false, [], ORG_ADMIN), nav(true, null), nav(true, ['VIEW_AUDIT_LOG'])]) {
      expect(allItems(sections).filter((i) => i.key === 'serviceStatus').length).toBeLessThanOrEqual(1)
      expect(sections.every((s) => s.items.length > 0)).toBe(true)
    }
  })

  it('Support appears exactly once for every kind of user', () => {
    for (const sections of [nav(false, [], []), nav(false, [], ORG_ADMIN), nav(true, null), nav(true, ['MANAGE_SUPPORT_TICKETS'])]) {
      expect(allItems(sections).filter((i) => i.key === 'support').length).toBe(1)
    }
  })
})

describe('appHomePath and isNavItemActive', () => {
  const find = (sections: AppNavSection[], key: string) => allItems(sections).find((i) => i.key === key)!

  it('opens the admin console for a platform admin and the business dashboard otherwise', () => {
    expect(appHomePath(true)).toBe('/admin')
    expect(appHomePath(false)).toBe('/organization/business-dashboard')
  })

  it('matches "/admin" only exactly, and prefixes for nested pages', () => {
    const products = find(nav(true, null), 'products')
    expect(isNavItemActive(products, '/admin')).toBe(true)
    expect(isNavItemActive(products, '/admin/platforms/4')).toBe(true)
    expect(isNavItemActive(products, '/admin/roles')).toBe(false)
  })

  it('tells items on the same path apart by query string (Knowledge Drafts, Billing)', () => {
    const k = nav(true, null)
    const drafts = find(k, 'knowledgeDrafts')
    const articles = find(k, 'knowledgeManageArticles')
    expect(isNavItemActive(drafts, '/knowledge-management/content', '?status=DRAFT')).toBe(true)
    expect(isNavItemActive(articles, '/knowledge-management/content', '?status=DRAFT')).toBe(false)
    expect(isNavItemActive(articles, '/knowledge-management/content', '')).toBe(true)
    const b = nav(false, [], [])
    expect(isNavItemActive(find(b, 'billingOverview'), '/billing', '')).toBe(true)
    expect(isNavItemActive(find(b, 'billingOverview'), '/billing', '?tab=invoices')).toBe(false)
    expect(isNavItemActive(find(b, 'billingInvoices'), '/billing', '?tab=invoices')).toBe(true)
  })

  it('finds the group to expand and the breadcrumb trail for a deep link', () => {
    const sections = nav(true, null)
    const integrations = find(sections, 'integrations')
    expect(isNavBranchActive(integrations, '/admin/integrations/api-keys')).toBe(true)
    expect(isNavBranchActive(integrations, '/admin/roles')).toBe(false)
    const trail = findNavTrail(sections, '/admin/integrations/api-keys')!
    expect([trail.section.key, trail.parent?.key, trail.item.key]).toEqual(['platform', 'integrations', 'apiKeys'])
    expect(findNavTrail(sections, '/admin/audit-log')!.item.key).toBe('auditLog')
    expect(findNavTrail(sections, '/admin/platforms/7/edit')!.item.key).toBe('products')
  })
})
