import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react'
import { authApi, ApiError } from '../api/authApi'
import { setTokenProvider } from '../api/client'
import { decodeJwtPayload, extractClientRolesFromToken } from './jwtRoles'

interface AuthUser {
  username: string
  email?: string
  roles: string[]
}

interface AuthState {
  user: AuthUser | null
  isAuthenticated: boolean
  /** Convenience for "ADMIN" in user.roles — this is a UI hint only. The real
   * enforcement lives on the backend (SecurityConfig's hasRole("ADMIN") check);
   * a user could in principle tamper with this in devtools and it wouldn't
   * grant them anything the backend doesn't also allow. */
  isAdmin: boolean
  isBootstrapping: boolean
  error: string | null
  /** Own-branded login: the browser never talks to Keycloak directly — the
   * backend does the real ROPC check against Keycloak (see AuthController).
   * Throws on invalid credentials; callers (AuthModal) show the message.
   * Resolves with the fresh roles so a caller can navigate correctly right
   * away — reading `isAdmin`/`user` off this same render's `auth` object
   * would still see the PRE-login value, since the state update triggered
   * by applyToken() hasn't re-rendered yet. */
  login: (email: string, password: string, totp?: string) => Promise<string[]>
  /** Phase 2 (2026.3.3): the second half of a login the backend parked on a
   * Platform MFA challenge — see `login`'s own doc and AuthModal for where
   * this is actually called. */
  verifyMfaChallenge: (challengeId: string, code: string) => Promise<string[]>
  /** C29: finishes a sign-in by setting up an authenticator; resolves with roles and recovery codes. */
  completeSignInEnrollment: (challengeId: string, code: string) => Promise<{ roles: string[]; recoveryCodes: string[] }>
  logout: () => void
  /** For the Launch button's plain-link case and any other caller that needs
   * the current token synchronously — a getter, not a reactive value, so
   * callers always read the live token without needing to re-render when a
   * background poll refreshes it. */
  getAccessToken: () => string | undefined
}

const AuthContext = createContext<AuthState | null>(null)

// Cross-tab / cross-app session sync: own-branded login means there's no
// shared Keycloak browser cookie to react to (see CLAUDE.md's SSO decisions),
// so instead this polls Vyoog's own backend, which itself checks both this
// app's own session and the shared vyoog_sso bridge against PMS (see
// AuthController.session()/.ping()). This is what makes logging out of PMS
// eventually flip an already-open Vyoog tab to signed-out too, and vice
// versa — bounded by the poll interval, not instant, since there's no push
// mechanism once Keycloak's own UI/cookie is out of the picture.
const SESSION_POLL_MS = 20_000

function userFromAccessToken(accessToken: string): AuthUser {
  const claims = decodeJwtPayload(accessToken)
  return {
    username: (claims.preferred_username as string | undefined) ?? (claims.sub as string) ?? 'you',
    email: claims.email as string | undefined,
    roles: extractClientRolesFromToken(accessToken),
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [accessToken, setAccessToken] = useState<string | undefined>(undefined)
  const [isBootstrapping, setIsBootstrapping] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const hasSessionRef = useRef(false)
  const checkInFlightRef = useRef<Promise<void> | null>(null)
  // C79: bumped on every sign-in or sign-out, so a session check that started
  // before one cannot undo it when its answer arrives late (for example
  // putting the old session back right after Sign out).
  const sessionGenRef = useRef(0)
  // C79: the server-side logout call; session checks wait for it, so the
  // still-valid session cookie is never read back in the meantime.
  const logoutPendingRef = useRef<Promise<void> | null>(null)

  const applyToken = useCallback((token: string | undefined) => {
    sessionGenRef.current += 1
    hasSessionRef.current = !!token
    // Registered synchronously here, not in a useEffect keyed on accessToken —
    // on the render where isBootstrapping flips to false (routes mounting for
    // the first time) and accessToken is set in the same batch, React fires
    // newly-mounted children's effects (their first authenticated fetch)
    // BEFORE this component's own updated effects, since effects run
    // child-before-parent within a commit. A useEffect-based registration
    // would lose that race — the first request after login/reload would go
    // out with no Authorization header at all. Calling it here instead runs
    // it synchronously as part of handling the resolved session/login promise,
    // before React even schedules that render.
    setTokenProvider(() => token)
    setAccessToken(token)
    setUser(token ? userFromAccessToken(token) : null)
  }, [])

  const checkSession = useCallback(async () => {
    // Reentrancy guard: the mount effect below fires this once immediately
    // AND registers it as the 'focus' handler AND on an interval — if two of
    // those land close enough together (found live in vyg-ticket-ui, which
    // reloads the page right after login: the reload itself can trigger a
    // near-simultaneous 'focus' event), two concurrent calls would both read
    // the same pre-rotation refresh cookie. Keycloak's refresh-token rotation
    // invalidates it for whichever call loses that race, and since a failed
    // call never updates the stored refresh token, every later poll keeps
    // retrying the same now-dead token forever. An overlapping call AWAITS
    // the same in-flight promise rather than returning immediately — Phase 2
    // (2026.3.3) found live (via a real browser test of a freshly-mounted
    // page's own effect firing an authenticated call right after reload)
    // that returning immediately let React StrictMode's double-invoked mount
    // effect's OWN `.finally(() => setIsBootstrapping(false))` fire on the
    // SECOND, instantly-resolved call — flipping isBootstrapping to false,
    // and routes mounting, BEFORE the real first call's network request (and
    // therefore the real access token) had actually arrived. Every
    // newly-mounted authenticated page's own on-mount fetch would then race
    // ahead of the token being set and 401 once, spuriously, on first load.
    if (checkInFlightRef.current) {
      await checkInFlightRef.current
      return
    }
    const promise = (async () => {
      if (logoutPendingRef.current) await logoutPendingRef.current
      const generation = sessionGenRef.current
      try {
        const result = await authApi.session()
        if (generation !== sessionGenRef.current) return
        applyToken(result.accessToken)
      } catch {
        if (generation !== sessionGenRef.current) return
        // A 401 here means genuinely signed out everywhere (own session dead
        // AND no usable cross-app bridge) — only act if we previously thought
        // we were logged in, so an already-signed-out tab doesn't loop.
        if (hasSessionRef.current) applyToken(undefined)
      }
    })()
    checkInFlightRef.current = promise
    try {
      await promise
    } finally {
      checkInFlightRef.current = null
    }
  }, [applyToken])

  // Runs once on mount (own session, else the cross-app vyoog_sso bridge —
  // see AuthController.session()'s own doc) before anything renders based on
  // auth state, then keeps polling so an already-open tab notices a logout
  // that happened in PMS.
  useEffect(() => {
    checkSession().finally(() => setIsBootstrapping(false))
    const interval = setInterval(checkSession, SESSION_POLL_MS)
    window.addEventListener('focus', checkSession)
    return () => {
      clearInterval(interval)
      window.removeEventListener('focus', checkSession)
    }
  }, [checkSession])

  // Phase 7: totp is undefined on a normal login attempt; AuthModal passes
  // one only when resubmitting after a prior attempt came back with the
  // mfaRequired flag on its ApiError.detail (see GlobalExceptionHandler on
  // the backend for exactly which two flags — mfaRequired vs.
  // organizationMfaRequired — this rejection can carry, and what each means).
  const login = useCallback(async (email: string, password: string, totp?: string) => {
    setError(null)
    try {
      const result = await authApi.login(email, password, totp)
      applyToken(result.accessToken)
      return extractClientRolesFromToken(result.accessToken)
    } catch (e) {
      // apiRequest() already logged the raw response — this adds the
      // auth-specific framing (which email, whether it got as far as an
      // MFA/org-MFA flag) in one place, so "did this reach Keycloak, and
      // what did it actually say" is answerable from the console alone.
      console.error('[auth] login failed for', email, e instanceof ApiError
        ? { status: e.status, message: e.message, detail: e.detail }
        : e)
      setError(e instanceof ApiError ? e.message : 'Could not sign in. Please try again.')
      throw e
    }
  }, [applyToken])

  // Phase 2 (2026.3.3): completes a login the backend parked on a Platform
  // MFA challenge (see ApiError.detail.platformMfaRequired/mfaChallengeId on
  // the login() call above) — same resolve/throw shape as login() itself,
  // since this is the second half of the same sign-in.
  const verifyMfaChallenge = useCallback(async (challengeId: string, code: string) => {
    setError(null)
    try {
      const result = await authApi.verifyMfaChallenge(challengeId, code)
      applyToken(result.accessToken)
      return extractClientRolesFromToken(result.accessToken)
    } catch (e) {
      console.error('[auth] platform MFA verification failed', e instanceof ApiError
        ? { status: e.status, message: e.message, detail: e.detail }
        : e)
      setError(e instanceof ApiError ? e.message : 'Could not verify your code. Please try again.')
      throw e
    }
  }, [applyToken])

  const completeSignInEnrollment = useCallback(async (challengeId: string, code: string) => {
    setError(null)
    try {
      const result = await authApi.completeSignInEnrollment(challengeId, code)
      applyToken(result.accessToken)
      return { roles: extractClientRolesFromToken(result.accessToken), recoveryCodes: result.recoveryCodes }
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not verify your code. Please try again.')
      throw e
    }
  }, [applyToken])

  const logout = useCallback(() => {
    setError(null)
    // Real logout: ends the actual Keycloak session server-side (not just
    // this tab's local copy) — this is what makes PMS's own session die too,
    // if it's currently open. Fire-and-forget: local state clears regardless
    // of whether this network call succeeds.
    const pending = authApi.logout().catch(() => {}).finally(() => {
      if (logoutPendingRef.current === pending) logoutPendingRef.current = null
    })
    logoutPendingRef.current = pending
    applyToken(undefined)
  }, [applyToken])

  const isAdmin = user?.roles.includes('ADMIN') ?? false
  const getAccessToken = useCallback(() => accessToken, [accessToken])

  return (
    <AuthContext.Provider
      value={{
        user, isAuthenticated: !!user, isAdmin, isBootstrapping, error,
        login, verifyMfaChallenge, completeSignInEnrollment, logout, getAccessToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth() called outside <AuthProvider>')
  return ctx
}
