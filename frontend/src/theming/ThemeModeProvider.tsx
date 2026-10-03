import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { ThemeProvider } from '@mui/material'
import { getTheme } from '../theme'
import { DEFAULT_ACCENT, isAccentKey, type AccentKey } from './accentPalette'

export type ThemeMode = 'light' | 'dark' | 'system'

interface ThemeModeContextValue {
  mode: ThemeMode
  resolvedMode: 'light' | 'dark'
  setMode: (mode: ThemeMode) => void
  // Phase 6 (2026.3.3): a manual override on top of the OS-level
  // prefers-reduced-motion media query — see tiles.css's own comment on the
  // two rules this drives together (this app's own animations always honor
  // both, independently).
  reducedMotion: boolean
  setReducedMotion: (value: boolean) => void
  // C67: the user's platform accent (primary colour). Stored in this browser
  // — the account preference API has no field for it.
  accent: AccentKey
  setAccent: (value: AccentKey) => void
}

const ThemeModeContext = createContext<ThemeModeContextValue | null>(null)

const STORAGE_KEY = 'vyoog-theme-mode'
const REDUCED_MOTION_STORAGE_KEY = 'vyoog-reduced-motion'
const ACCENT_STORAGE_KEY = 'vyoog-accent'

function systemPrefersDark(): boolean {
  return typeof window !== 'undefined' && window.matchMedia('(prefers-color-scheme: dark)').matches
}

function readStoredMode(): ThemeMode {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'light' || stored === 'dark' || stored === 'system') return stored
  } catch {
    // localStorage unavailable (private browsing, etc.) — fall through to default.
  }
  return 'system'
}

function readStoredAccent(): AccentKey {
  try {
    const stored = localStorage.getItem(ACCENT_STORAGE_KEY)
    if (isAccentKey(stored)) return stored
  } catch {
    // localStorage unavailable — fall through to the default accent.
  }
  return DEFAULT_ACCENT
}

function readStoredReducedMotion(): boolean {
  try {
    return localStorage.getItem(REDUCED_MOTION_STORAGE_KEY) === 'true'
  } catch {
    return false
  }
}

/**
 * Phase 21: theme.ts used to be a single hardcoded light palette with a
 * comment saying so "by design" — this is the real mode-switching layer on
 * top of it. "system" (the default) follows the OS preference live, via a
 * matchMedia listener, without the user ever having picked light or dark
 * explicitly; picking one explicitly persists it in localStorage and stops
 * following the OS.
 */
export function ThemeModeProvider({ children }: { children: ReactNode }) {
  const [mode, setModeState] = useState<ThemeMode>(readStoredMode)
  const [systemDark, setSystemDark] = useState(systemPrefersDark)
  const [reducedMotion, setReducedMotionState] = useState<boolean>(readStoredReducedMotion)
  const [accent, setAccentState] = useState<AccentKey>(readStoredAccent)

  useEffect(() => {
    const media = window.matchMedia('(prefers-color-scheme: dark)')
    const listener = (e: MediaQueryListEvent) => setSystemDark(e.matches)
    media.addEventListener('change', listener)
    return () => media.removeEventListener('change', listener)
  }, [])

  useEffect(() => {
    document.documentElement.setAttribute('data-reduced-motion', String(reducedMotion))
  }, [reducedMotion])

  const setMode = useCallback((next: ThemeMode) => {
    setModeState(next)
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      // Best-effort — a failed persist just means it resets to "system" next visit.
    }
  }, [])

  const setReducedMotion = useCallback((next: boolean) => {
    setReducedMotionState(next)
    try {
      localStorage.setItem(REDUCED_MOTION_STORAGE_KEY, String(next))
    } catch {
      // Best-effort — see setMode's own comment.
    }
  }, [])

  const setAccent = useCallback((next: AccentKey) => {
    setAccentState(next)
    try {
      localStorage.setItem(ACCENT_STORAGE_KEY, next)
    } catch {
      // Best-effort — see setMode's own comment.
    }
  }, [])

  const resolvedMode: 'light' | 'dark' = mode === 'system' ? (systemDark ? 'dark' : 'light') : mode

  const theme = useMemo(() => getTheme(resolvedMode, accent), [resolvedMode, accent])
  const contextValue = useMemo(
    () => ({ mode, resolvedMode, setMode, reducedMotion, setReducedMotion, accent, setAccent }),
    [mode, resolvedMode, setMode, reducedMotion, setReducedMotion, accent, setAccent]
  )

  return (
    <ThemeModeContext.Provider value={contextValue}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </ThemeModeContext.Provider>
  )
}

export function useThemeMode(): ThemeModeContextValue {
  const ctx = useContext(ThemeModeContext)
  if (!ctx) throw new Error('useThemeMode must be used within a ThemeModeProvider')
  return ctx
}
