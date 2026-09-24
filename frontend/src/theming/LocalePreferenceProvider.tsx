import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'

// A short, real list of IANA time zones spanning Vyoog's actual customer
// regions today (India-headquartered, per Organization.country data seen in
// registration) plus common international ones — not every IANA zone (400+),
// which would be unusable as a plain dropdown.
export const SUPPORTED_TIMEZONES = [
  { code: 'Asia/Kolkata', label: 'India (IST)' },
  { code: 'America/New_York', label: 'US Eastern (ET)' },
  { code: 'America/Los_Angeles', label: 'US Pacific (PT)' },
  { code: 'Europe/London', label: 'United Kingdom (GMT/BST)' },
  { code: 'Europe/Berlin', label: 'Central Europe (CET)' },
  { code: 'Asia/Singapore', label: 'Singapore (SGT)' },
  { code: 'Australia/Sydney', label: 'Australia Eastern (AET)' },
] as const

// Phase 6 (2026.3.3): "region" here means an Intl FORMATTING convention
// (date order, number grouping) — genuinely distinct from a time zone (which
// clock) and from the UI language (which strings are shown). Previously
// conflated into one list literally named SUPPORTED_REGIONS that actually
// held time zones (see this file's own git history / CustomerPreference's
// backend javadoc for the fix). Same short, real-customer-footprint scope as
// the time zone list above, not an exhaustive locale catalog.
export const SUPPORTED_REGIONS = [
  { code: 'en-IN', label: 'India (DD/MM/YYYY)' },
  { code: 'en-US', label: 'United States (MM/DD/YYYY)' },
  { code: 'en-GB', label: 'United Kingdom (DD/MM/YYYY)' },
  { code: 'de-DE', label: 'Central Europe (DD.MM.YYYY)' },
  { code: 'en-SG', label: 'Singapore (DD/MM/YYYY)' },
  { code: 'en-AU', label: 'Australia (DD/MM/YYYY)' },
] as const

interface LocalePreferenceContextValue {
  timeZone: string
  setTimeZone: (timeZone: string) => void
  region: string | null
  setRegion: (region: string | null) => void
  formatDateTime: (iso: string) => string
  formatDate: (iso: string) => string
}

const LocalePreferenceContext = createContext<LocalePreferenceContextValue | null>(null)

const STORAGE_KEY = 'vyoog-timezone'
const REGION_STORAGE_KEY = 'vyoog-region'

function readStoredTimeZone(): string {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) return stored
  } catch {
    // localStorage unavailable — fall through to the browser's own zone.
  }
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone
  } catch {
    return 'UTC'
  }
}

/** Null means "no explicit choice" — Intl calls pass `undefined` as the
 * locale, which means "use the browser's own formatting convention," same
 * as before this preference existed at all. */
function readStoredRegion(): string | null {
  try {
    return localStorage.getItem(REGION_STORAGE_KEY)
  } catch {
    return null
  }
}

/**
 * Phase 21 ("multi-region"), extended Phase 6 (2026.3.3): a real, working
 * time-zone AND region preference — distinct from Organization's postal
 * address fields (country/state/city), which describe where a business is
 * registered, not what clock or date-formatting convention its members want.
 * Time zone (which clock) and region (which formatting convention, e.g.
 * DD/MM vs MM/DD) are two separate axes — see this file's own
 * SUPPORTED_REGIONS/SUPPORTED_TIMEZONES comments for why they used to be
 * wrongly conflated into one list. Deliberately scoped to what's genuinely
 * achievable client-side: persisted display preferences used for date/time
 * formatting everywhere a timestamp is shown. There is no backend
 * region-aware routing or data-residency behind this (no such infrastructure
 * exists in this system) — these are display preferences, not a claim about
 * where data is stored or processed.
 */
export function LocalePreferenceProvider({ children }: { children: ReactNode }) {
  const [timeZone, setTimeZoneState] = useState<string>(readStoredTimeZone)
  const [region, setRegionState] = useState<string | null>(readStoredRegion)

  const setTimeZone = useCallback((next: string) => {
    setTimeZoneState(next)
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      // Best-effort — a failed persist just means it resets to the browser default next visit.
    }
  }, [])

  const setRegion = useCallback((next: string | null) => {
    setRegionState(next)
    try {
      if (next) localStorage.setItem(REGION_STORAGE_KEY, next)
      else localStorage.removeItem(REGION_STORAGE_KEY)
    } catch {
      // Best-effort — see setTimeZone's own comment.
    }
  }, [])

  const formatDateTime = useCallback((iso: string) => {
    try {
      return new Intl.DateTimeFormat(region ?? undefined, {
        dateStyle: 'medium', timeStyle: 'short', timeZone,
      }).format(new Date(iso))
    } catch {
      return new Date(iso).toLocaleString()
    }
  }, [timeZone, region])

  const formatDate = useCallback((iso: string) => {
    try {
      return new Intl.DateTimeFormat(region ?? undefined, { dateStyle: 'medium', timeZone }).format(new Date(iso))
    } catch {
      return new Date(iso).toLocaleDateString()
    }
  }, [timeZone, region])

  const value = useMemo(
    () => ({ timeZone, setTimeZone, region, setRegion, formatDateTime, formatDate }),
    [timeZone, setTimeZone, region, setRegion, formatDateTime, formatDate]
  )

  return <LocalePreferenceContext.Provider value={value}>{children}</LocalePreferenceContext.Provider>
}

export function useLocalePreference(): LocalePreferenceContextValue {
  const ctx = useContext(LocalePreferenceContext)
  if (!ctx) throw new Error('useLocalePreference must be used within a LocalePreferenceProvider')
  return ctx
}
