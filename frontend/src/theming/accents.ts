/**
 * C60: the corporate multi-colour accent set. Every accent has a light-mode
 * shade (dark enough for icons and text on white, WCAG AA for large text and
 * graphics) and a dark-mode shade (light enough on the dark surfaces). The
 * brand palette in theme.ts (blue / violet / cyan, C45) stays the primary
 * and secondary colours; accents only colour icons, section markers and
 * status tiles, so screens stay calm while being easy to scan.
 */
export type AccentKey = 'blue' | 'violet' | 'teal' | 'amber' | 'rose' | 'emerald' | 'cyan' | 'indigo' | 'orange' | 'pink'

export const ACCENTS: Record<AccentKey, { light: string; dark: string }> = {
  blue: { light: '#4c63ff', dark: '#8b99ff' },
  violet: { light: '#733dff', dark: '#b197ff' },
  teal: { light: '#0f766e', dark: '#2dd4bf' },
  amber: { light: '#b45309', dark: '#fbbf24' },
  rose: { light: '#e11d48', dark: '#fb7185' },
  emerald: { light: '#047857', dark: '#34d399' },
  cyan: { light: '#0e7490', dark: '#22d3ee' },
  indigo: { light: '#4338ca', dark: '#a5b4fc' },
  orange: { light: '#c2410c', dark: '#fb923c' },
  pink: { light: '#be185d', dark: '#f472b6' },
}

export const ACCENT_CYCLE: AccentKey[] = ['blue', 'violet', 'teal', 'amber', 'rose', 'emerald', 'cyan', 'indigo', 'orange', 'pink']

export function accentColor(key: AccentKey, mode: 'light' | 'dark') {
  return ACCENTS[key][mode]
}

/** A stable accent for any string (a nav key, a section name). */
export function accentFor(id: string): AccentKey {
  let hash = 0
  for (let i = 0; i < id.length; i++) hash = (hash * 31 + id.charCodeAt(i)) >>> 0
  return ACCENT_CYCLE[hash % ACCENT_CYCLE.length]
}

/** The brand gradient stripe used on headers and section markers. */
export const BRAND_STRIPE = 'linear-gradient(90deg, #4c63ff 0%, #733dff 30%, #42d8ff 55%, #0f766e 75%, #f59e0b 100%)'
