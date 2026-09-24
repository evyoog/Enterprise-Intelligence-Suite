// Falls back to whatever host the page itself was loaded from (same port
// convention, :8081) rather than a hardcoded "localhost" — that's what makes
// opening the app via a LAN IP (e.g. http://192.168.1.4:5173) automatically
// call that same machine's backend instead of silently trying localhost:8081
// on whatever device's browser is loading the page. Set VITE_API_BASE_URL to
// override this outright (e.g. for a real deployed domain).
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? `http://${window.location.hostname}:8081/api`

// The backend returns uploaded-asset URLs (e.g. product images) as paths
// relative to its own root ("/api/products/images/xxx.jpg"), not full URLs —
// it doesn't reliably know its own externally-visible domain. Resolve them
// against the same API_BASE_URL every other call already uses, the same way
// the frontend and backend can run on different ports/hosts for every other
// endpoint too.
export function resolveAssetUrl(url: string): string {
  if (/^https?:\/\//.test(url)) return url
  const origin = API_BASE_URL.replace(/\/api\/?$/, '')
  return `${origin}${url}`
}

// For the handful of backend endpoints a caller must navigate the browser to
// directly (a real top-level GET, never fetch) rather than call via
// apiRequest — e.g. SAML's own login-init, which issues a real HTTP redirect
// straight to an external identity provider.
export function apiUrl(path: string): string {
  return `${API_BASE_URL}${path}`
}

export class ApiError extends Error {
  status: number
  // Phase 7: the backend's error body sometimes carries extra flags beyond
  // {message} — e.g. {mfaRequired: true} / {organizationMfaRequired: true}
  // (see GlobalExceptionHandler) — kept as the raw parsed body so callers
  // that need those flags don't have to re-fetch/re-parse anything.
  detail: Record<string, unknown> | null

  constructor(status: number, message: string, detail: Record<string, unknown> | null = null) {
    super(message)
    this.status = status
    this.detail = detail
  }
}

// AuthProvider registers a getter here once, at mount, rather than this module
// holding a copied token — a silent refresh updates the token in AuthProvider's
// own ref, and every subsequent apiRequest() call picks that up automatically
// with no re-registration needed.
let tokenProvider: () => string | undefined = () => undefined

export function setTokenProvider(provider: () => string | undefined) {
  tokenProvider = provider
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = tokenProvider()

  // FormData (file uploads) must NOT get a manual Content-Type — the browser
  // sets one itself with the correct multipart boundary, which we have no way
  // to reproduce by hand.
  const isFormData = options.body instanceof FormData

  const response = await fetch(`${API_BASE_URL}${path}`, {
    // Cookies (the auth refresh token) travel on every call, not just auth ones —
    // harmless for endpoints that don't need it, required for the ones that do.
    credentials: 'include',
    ...options,
    headers: {
      ...(options.body && !isFormData ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    const detail = await response.json().catch(() => null)
    // Debug visibility for every failed API call, not just login — the real
    // backend message/detail (e.g. Keycloak's actual rejection reason, when
    // vyoog.auth.debug-login-errors is on server-side) always lands here
    // even where the UI itself only shows a generic message.
    console.error(`[api] ${options.method ?? 'GET'} ${path} -> ${response.status}`, detail ?? '(no body)')
    throw new ApiError(response.status, detail?.message ?? 'Request failed', detail)
  }

  if (response.status === 204) return undefined as T
  return response.json()
}
