/**
 * Decodes the access token the backend hands back from /auth/login,
 * /auth/session, or /auth/ping (see AuthController) — role-reading here
 * should look at exactly what the backend's own JWT resource-server
 * validation actually sees.
 */
export function decodeJwtPayload(token: string): Record<string, unknown> {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0))
    return JSON.parse(new TextDecoder().decode(bytes)) as Record<string, unknown>
  } catch {
    return {}
  }
}

// Mirrors the backend's KeycloakJwtAuthenticationConverter: roles here are
// Keycloak CLIENT roles ("resource_access.<clientId>.roles"), matching
// vyg-pms, not realm roles. Which client key to read isn't fixed — "aud"
// only lists it with an Audience mapper configured, otherwise only "azp"
// (the client the token was issued to) has it — so both are checked, same
// as the backend.
function extractClientRolesFromClaims(claims: Record<string, unknown>): string[] {
  const resourceAccess = claims.resource_access as Record<string, { roles?: string[] }> | undefined
  if (!resourceAccess) return []

  const clientsToCheck = new Set<string>()
  const audiences = claims.aud
  if (Array.isArray(audiences)) audiences.forEach((a) => clientsToCheck.add(a))
  else if (typeof audiences === 'string') clientsToCheck.add(audiences)
  if (typeof claims.azp === 'string') clientsToCheck.add(claims.azp)

  const roles: string[] = []
  for (const client of clientsToCheck) {
    const clientRoles = resourceAccess[client]?.roles
    if (clientRoles) roles.push(...clientRoles)
  }
  return roles
}

export function extractClientRolesFromToken(accessToken: string): string[] {
  return extractClientRolesFromClaims(decodeJwtPayload(accessToken))
}
