import { apiRequest } from './client'

export interface SessionInfo {
  id: string
  ipAddress?: string
  startedAt?: string
  lastAccessAt?: string
  clients: string[]
  current: boolean
}

// Self-service Keycloak session management — always the caller's own
// sessions (see SessionController on the backend: ownership is enforced
// server-side, never trust a client-supplied session id).
export const sessionsApi = {
  list: () => apiRequest<SessionInfo[]>('/me/sessions'),
  revoke: (sessionId: string) => apiRequest<undefined>(`/me/sessions/${sessionId}`, { method: 'DELETE' }),
}
