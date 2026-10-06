import type { ComponentType } from 'react'
import type { MyPermissions } from '../../api/myPermissionsApi'
import { NAV_CONFIG, NAV_GROUP_ORDER, type NavDef, type NavGroup, type NavRule } from './navConfig'

export interface AppNavItem {
  key: string
  /** i18n key under appShell.nav */
  labelKey: string
  /** i18n key under appShell.navHint (tooltip text). */
  hintKey?: string
  /** Absent for a group header that only expands. */
  to?: string
  icon: ComponentType<{ size?: number }>
  /** Extra paths that also mark this item as current. */
  matchPrefixes?: string[]
  searchAny?: string[]
  excludeSearch?: string[]
  children?: AppNavItem[]
}

export interface AppNavSection {
  key: NavGroup
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

/** Whether a rule passes for this user. */
export function can(rule: NavRule, { isAdmin, permissions }: NavAccess): boolean {
  const inOrganization = permissions !== null && permissions.organization.length > 0
  if ('platform' in rule) return isAdmin && (permissions === null || permissions.platform.includes(rule.platform))
  if ('platformGrant' in rule) {
    return permissions === null ? isAdmin : rule.platformGrant.some((name) => permissions.platform.includes(name))
  }
  if ('organization' in rule) return permissions !== null && permissions.organization.includes(rule.organization)
  if ('signedIn' in rule) return true
  // A platform admin normally has no customer account of their own.
  if ('customer' in rule) return !isAdmin || inOrganization
  if ('inOrganization' in rule) return inOrganization
  if ('admin' in rule) return isAdmin
  if ('loading' in rule) return permissions === null
  if ('any' in rule) return rule.any.some((r) => can(r, { isAdmin, permissions }))
  if ('all' in rule) return rule.all.every((r) => can(r, { isAdmin, permissions }))
  return !can(rule.not, { isAdmin, permissions })
}

function resolve(def: NavDef, access: NavAccess, group: NavGroup): AppNavItem | null {
  if (def.when && !can(def.when, access)) return null
  const to = typeof def.to === 'string'
    ? def.to
    : def.to?.find((v) => !v.when || can(v.when, access))?.to
  const children = def.children
    ?.filter((c) => !(c.unless && can(c.unless, access)))
    .map((c) => resolve(c, access, group))
    .filter((c): c is AppNavItem => c !== null)
  // A parent whose children are all hidden, and that has no page of its own, is not shown.
  if (def.children && (children?.length ?? 0) === 0 && !to) return null
  return {
    key: def.id, labelKey: def.labelKey, hintKey: def.hintKey, to, icon: def.icon,
    matchPrefixes: def.matchPrefixes, searchAny: def.searchAny, excludeSearch: def.excludeSearch,
    children: children && children.length > 0 ? children : undefined,
  }
}

/**
 * The signed-in sidebar, filtered to what this user can use, from the single
 * declarative NAV_CONFIG. Each rule matches the permission the page's backend
 * endpoints check, so nothing is offered that would only return 403. This is UI
 * filtering only; route guards and the backend stay the security boundary.
 *
 * While permissions are unknown, a platform admin sees the full admin menu
 * (the behavior before this change) and organization-only items are hidden.
 */
export function buildAppNavigation(access: NavAccess): AppNavSection[] {
  const byGroup = new Map<NavGroup, AppNavItem[]>()
  for (const def of NAV_CONFIG) {
    const group = def.groupOverrides?.find((o) => can(o.when, access))?.group ?? def.group
    const item = resolve(def, access, group)
    if (!item) continue
    // In a "flattenIn" group the group label is the parent: its children become the items.
    const items = def.flattenIn?.includes(group) && item.children ? item.children : [item]
    byGroup.set(group, [...(byGroup.get(group) ?? []), ...items])
  }
  return NAV_GROUP_ORDER
    .filter((group) => (byGroup.get(group)?.length ?? 0) > 0)
    .map((group) => ({ key: group, labelKey: group, items: byGroup.get(group)! }))
}

function pathOf(to: string) {
  return to.split('?')[0]
}

/** Whether `item` itself is the current page ("/admin" matches exactly, so it
 * does not light up for every admin page). `search` is the location's query string. */
export function isNavItemActive(item: AppNavItem, pathname: string, search = '') {
  if (item.children && !item.to) return false
  if (item.searchAny && !item.searchAny.some((q) => search.includes(q))) return false
  if (item.excludeSearch?.some((q) => search.includes(q))) return false
  const to = item.to ? pathOf(item.to) : undefined
  if (to && pathname === to) return true
  if (to && to !== '/admin' && pathname.startsWith(to + '/')) return true
  return (item.matchPrefixes ?? []).some((prefix) => pathname === prefix || pathname.startsWith(prefix + '/'))
}

/** Whether the item or one of its children is current (the group to expand). */
export function isNavBranchActive(item: AppNavItem, pathname: string, search = ''): boolean {
  return isNavItemActive(item, pathname, search) || (item.children?.some((c) => isNavBranchActive(c, pathname, search)) ?? false)
}

/** Section, parent and item for the current page: the breadcrumb trail. */
export function findNavTrail(sections: AppNavSection[], pathname: string, search = '') {
  let best: { section: AppNavSection; parent?: AppNavItem; item: AppNavItem; depth: number } | null = null
  for (const section of sections) {
    for (const item of section.items) {
      for (const child of item.children ?? []) {
        if (isNavItemActive(child, pathname, search)) return { section, parent: item, item: child }
      }
      if (isNavItemActive(item, pathname, search)) {
        const depth = item.to ? pathOf(item.to).length : 0
        if (!best || depth > best.depth) best = { section, item, depth }
      }
    }
  }
  return best ? { section: best.section, parent: best.parent, item: best.item } : null
}

/** Router state that lets a signed-in user see the public website at "/"
 * (C79). Without it, "/" sends a signed-in user into the tool (PublicOnly),
 * which is what sign-in and SSO returns rely on. Browsers keep this state on
 * reload, so a refreshed landing page stays the landing page. */
export const WEBSITE_HOME_STATE = { website: true } as const

export function wantsWebsite(state: unknown) {
  return typeof state === 'object' && state !== null && (state as { website?: unknown }).website === true
}
