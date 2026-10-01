// In dev, calls go to the page's own origin ("/api") and the Vite dev server
// proxies them to the backend (see vite.config.ts). That works unchanged on
// localhost, a LAN IP, and GitHub Codespaces — where the page is https and
// ports are separate subdomains, so "http://<host>:8081" is both blocked as
// mixed content and the wrong host.
// Production builds fall back to the page's host on the :8081 convention.
// Set VITE_API_BASE_URL to override either (e.g. for a real deployed domain).
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  (import.meta.env.DEV ? '/api' : `http://${window.location.hostname}:8081/api`)

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
  // A controller returning null (e.g. "no billing details yet") answers 200
  // with an empty body, which response.json() rejects.
  const text = await response.text()
  return (text ? JSON.parse(text) : null) as T
}

/** For the handful of endpoints that return a file (Billing & Payments'
 * invoice/receipt documents, REQ-BIL-001.11) rather than JSON — triggers a
 * real browser download using the filename the backend's
 * Content-Disposition header names, same as clicking a plain download link
 * but with the Authorization header apiRequest already attaches. */
export async function apiDownload(path: string): Promise<void> {
  const token = tokenProvider()
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!response.ok) {
    const detail = await response.json().catch(() => null)
    throw new ApiError(response.status, detail?.message ?? 'Download failed', detail)
  }
  const disposition = response.headers.get('Content-Disposition') ?? ''
  const filename = /filename="?([^"]+)"?/.exec(disposition)?.[1] ?? 'document.txt'
  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}
