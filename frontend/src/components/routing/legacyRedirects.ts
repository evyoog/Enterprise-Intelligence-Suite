/**
 * C80: routes that moved when the sidebar was reorganized. The old URLs keep
 * working (bookmarks, emails, links from other apps) by redirecting to where
 * the feature lives now. Paths under "/admin" are relative to it.
 */
export const LEGACY_ADMIN_REDIRECTS: [path: string, to: string][] = [
  ['settings', '/admin/apps?new=1'],
  ['settings/product', '/admin/apps?new=1'],
  ['settings/platform', '/admin/platforms/new'],
  ['settings/common', '/admin?tab=configuration'],
  ['permissions', '/admin/roles?tab=permissions'],
  ['service-status', '/status?tab=manage'],
  ['knowledge-base', '/knowledge-management/content?type=ARTICLE'],
]
