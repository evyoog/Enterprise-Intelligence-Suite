import { describe, expect, it } from 'vitest'
import { appHomePath, buildAppNavigation, isNavItemActive } from './appNavigation'

const keys = (sections: ReturnType<typeof buildAppNavigation>) =>
  Object.fromEntries(sections.map((s) => [s.key, s.items.map((i) => i.key)]))

describe('buildAppNavigation', () => {
  it('gives an organization admin the workspace and organization items, and no administration', () => {
    const nav = keys(buildAppNavigation({
      isAdmin: false,
      permissions: { platform: [], organization: ['MANAGE_ORGANIZATION', 'MANAGE_USERS'] },
    }))
    expect(nav.workspace).toEqual(['dashboard', 'myProducts', 'catalog'])
    expect(nav.organization).toEqual(['identityFederation'])
    expect(nav.administration).toBeUndefined()
    expect(nav.account).toEqual(['security', 'preferences'])
  })

  it('gives a plain member or an individual no business dashboard and no organization items', () => {
    for (const organization of [['MANAGE_PRIVILEGED_ACCESS_SELF'], []]) {
      const nav = keys(buildAppNavigation({ isAdmin: false, permissions: { platform: [], organization } }))
      expect(nav.workspace).toEqual(['myProducts', 'catalog'])
      expect(nav.organization).toBeUndefined()
      expect(nav.administration).toBeUndefined()
    }
  })

  it('shows a platform admin only the admin items their platform permissions allow', () => {
    const nav = keys(buildAppNavigation({
      isAdmin: true,
      permissions: { platform: ['MANAGE_REGISTRATIONS', 'VIEW_AUDIT_LOG'], organization: [] },
    }))
    expect(nav.administration).toEqual(['registrations', 'auditLog'])
    expect(nav.workspace).toEqual(['catalog'])
  })

  it('shows a platform admin the full admin menu while permissions are unknown', () => {
    const nav = keys(buildAppNavigation({ isAdmin: true, permissions: null }))
    expect(nav.administration).toEqual([
      'platforms', 'apps', 'registrations', 'privilegedAccess', 'roles', 'permissions', 'auditLog', 'settings',
    ])
  })

  it('never shows administration to a non-admin, whatever the permission list says', () => {
    const nav = keys(buildAppNavigation({ isAdmin: false, permissions: { platform: ['MANAGE_CATALOG'], organization: [] } }))
    expect(nav.administration).toBeUndefined()
  })
})

describe('appHomePath and isNavItemActive', () => {
  it('opens the admin console for a platform admin and the business dashboard otherwise', () => {
    expect(appHomePath(true)).toBe('/admin')
    expect(appHomePath(false)).toBe('/organization/business-dashboard')
  })

  it('matches "/admin" only exactly, and prefixes for nested pages', () => {
    const platforms = buildAppNavigation({ isAdmin: true, permissions: null })
      .find((s) => s.key === 'administration')!.items.find((i) => i.key === 'platforms')!
    expect(isNavItemActive(platforms, '/admin')).toBe(true)
    expect(isNavItemActive(platforms, '/admin/platforms/4')).toBe(true)
    expect(isNavItemActive(platforms, '/admin/roles')).toBe(false)
  })
})
