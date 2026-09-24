import { useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { preferenceApi } from '../api/preferenceApi'
import { useAuth } from '../auth/AuthProvider'
import { useLocalePreference } from './LocalePreferenceProvider'
import { useThemeMode } from './ThemeModeProvider'

/**
 * Phase 6 (2026.3.3): keeps the account-level {@code /me/preferences} row in
 * step with the client-side theme/locale/i18n state that used to be
 * localStorage-only. Renders nothing — purely an effect. Two directions:
 *
 * <ol>
 *   <li><b>Hydrate</b> once per login: pull this account's own saved
 *   preferences and apply them on top of whatever localStorage/browser
 *   defaults were already showing, so switching accounts on the same
 *   browser doesn't leak the previous account's choices, and a fresh device
 *   picks up an existing account's real preferences immediately.</li>
 *   <li><b>Persist</b> any later change (from the navbar dropdown or the
 *   Preferences page) back to the account.</li>
 * </ol>
 *
 * {@code readyToPersist} is the one thing that makes this safe rather than
 * lossy: the persist effect is a no-op until the hydrate GET has actually
 * resolved (success OR failure) — without it, the persist effect's own
 * initial run (which fires synchronously on mount, before the async GET
 * response arrives) would PUT the stale, pre-hydration localStorage values
 * and overwrite whatever was really saved.
 *
 * Signed-out visitors are untouched — every provider already has a working
 * localStorage/browser-default fallback from before this feature existed,
 * and this component simply never calls the backend for them.
 */
export function PreferenceSync() {
  const auth = useAuth()
  const { i18n } = useTranslation()
  const { mode, setMode, reducedMotion, setReducedMotion } = useThemeMode()
  const { timeZone, setTimeZone, region, setRegion } = useLocalePreference()

  const hydratedFor = useRef<string | null>(null)
  const readyToPersist = useRef(false)

  useEffect(() => {
    if (!auth.isAuthenticated) {
      hydratedFor.current = null
      readyToPersist.current = false
      return
    }
    const sub = auth.user?.username ?? 'unknown'
    if (hydratedFor.current === sub) return
    hydratedFor.current = sub
    readyToPersist.current = false

    preferenceApi.get()
      .then((pref) => {
        if (pref.themeMode === 'light' || pref.themeMode === 'dark' || pref.themeMode === 'system') {
          setMode(pref.themeMode)
        }
        setReducedMotion(Boolean(pref.reducedMotion))
        if (pref.timeZone) setTimeZone(pref.timeZone)
        setRegion(pref.region ?? null)
        if (pref.language) i18n.changeLanguage(pref.language)
      })
      .catch(() => {
        // No saved preferences yet, or a transient failure — the
        // localStorage/browser defaults already showing are a fine fallback.
      })
      .finally(() => {
        readyToPersist.current = true
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [auth.isAuthenticated, auth.user?.username])

  useEffect(() => {
    if (!auth.isAuthenticated || !readyToPersist.current) return
    preferenceApi.update({ language: i18n.language, region, timeZone, themeMode: mode, reducedMotion }).catch(() => {
      // Best-effort — a failed save just means this device falls back to
      // localStorage next time, same as before this feature existed.
    })
  }, [auth.isAuthenticated, mode, reducedMotion, timeZone, region, i18n.language])

  return null
}
