import { Navigate, useLocation } from 'react-router-dom'

/**
 * "/organization/settings" — C80: the page was split into Members (and
 * Groups), Sign-in security (MFA policy) and Privileged access. The old URL,
 * and its #members / #groups / #mfa / #privileged-access anchors, still work
 * by sending people to the card's new page.
 */
const TARGETS: Record<string, string> = {
  '#members': '/organization/members',
  '#groups': '/organization/members?tab=groups',
  '#mfa': '/organization/identity-federation?tab=mfa',
  '#privileged-access': '/organization/privileged-access',
}

export function OrganizationSettingsRedirect() {
  const { hash } = useLocation()
  return <Navigate to={TARGETS[hash] ?? '/organization/members'} replace />
}
