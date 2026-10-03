import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import { DATE_FORMATS, TIME_FORMATS, type DateFormat, type TimeFormat } from './formatOptions'

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
  dateFormat: DateFormat
  setDateFormat: (value: DateFormat) => void
  timeFormat: TimeFormat
  setTimeFormat: (value: TimeFormat) => void
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
const DATE_FORMAT_KEY = 'vyoog-date-format'
const TIME_FORMAT_KEY = 'vyoog-time-format'

function readStoredChoice<T extends string>(key: string, allowed: readonly T[]): T {
  try {
    const stored = localStorage.getItem(key)
    if (stored && (allowed as readonly string[]).includes(stored)) return stored as T
  } catch {
    // localStorage unavailable — fall through to 'auto'.
  }
  return allowed[0]
}

function storeChoice(key: string, value: string) {
  try {
    if (value === 'auto') localStorage.removeItem(key)
    else localStorage.setItem(key, value)
  } catch {
    // Best-effort, same as the other preferences in this file.
  }
}

/** Day, month and year of `date` in `timeZone`, laid out as `format`. */
function formatNumericDate(date: Date, format: Exclude<DateFormat, 'auto'>, timeZone: string): string {
  const parts = new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric', timeZone }).formatToParts(date)
  const get = (type: string) => parts.find((p) => p.type === type)?.value ?? ''
  const [d, m, y] = [get('day'), get('month'), get('year')]
  if (format === 'MM/DD/YYYY') return `${m}/${d}/${y}`
  if (format === 'YYYY-MM-DD') return `${y}-${m}-${d}`
  return `${d}/${m}/${y}`
}

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
  const [dateFormat, setDateFormatState] = useState<DateFormat>(() => readStoredChoice(DATE_FORMAT_KEY, DATE_FORMATS))
  const [timeFormat, setTimeFormatState] = useState<TimeFormat>(() => readStoredChoice(TIME_FORMAT_KEY, TIME_FORMATS))

  const setDateFormat = useCallback((next: DateFormat) => { setDateFormatState(next); storeChoice(DATE_FORMAT_KEY, next) }, [])
  const setTimeFormat = useCallback((next: TimeFormat) => { setTimeFormatState(next); storeChoice(TIME_FORMAT_KEY, next) }, [])

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

  const hour12 = timeFormat === 'auto' ? undefined : timeFormat === '12h'

  const formatDateTime = useCallback((iso: string) => {
    try {
      const date = new Date(iso)
      if (dateFormat === 'auto') {
        return new Intl.DateTimeFormat(region ?? undefined, {
          dateStyle: 'medium', timeStyle: 'short', timeZone, hour12,
        }).format(date)
      }
      const time = new Intl.DateTimeFormat(region ?? undefined, { timeStyle: 'short', timeZone, hour12 }).format(date)
      return `${formatNumericDate(date, dateFormat, timeZone)} ${time}`
    } catch {
      return new Date(iso).toLocaleString()
    }
  }, [timeZone, region, dateFormat, hour12])

  const formatDate = useCallback((iso: string) => {
    try {
      const date = new Date(iso)
      if (dateFormat !== 'auto') return formatNumericDate(date, dateFormat, timeZone)
      return new Intl.DateTimeFormat(region ?? undefined, { dateStyle: 'medium', timeZone }).format(date)
    } catch {
      return new Date(iso).toLocaleDateString()
    }
  }, [timeZone, region, dateFormat])

  const value = useMemo(
    () => ({
      timeZone, setTimeZone, region, setRegion, formatDateTime, formatDate,
      dateFormat, setDateFormat, timeFormat, setTimeFormat,
    }),
    [timeZone, setTimeZone, region, setRegion, formatDateTime, formatDate,
      dateFormat, setDateFormat, timeFormat, setTimeFormat]
  )

  return <LocalePreferenceContext.Provider value={value}>{children}</LocalePreferenceContext.Provider>
}

export function useLocalePreference(): LocalePreferenceContextValue {
  const ctx = useContext(LocalePreferenceContext)
  if (!ctx) throw new Error('useLocalePreference must be used within a LocalePreferenceProvider')
  return ctx
}
