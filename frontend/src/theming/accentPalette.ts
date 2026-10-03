/**
 * C67: the user's platform accent. Each accent is a primary palette for light
 * and dark mode. In light mode `main` is the shade buttons use with white
 * text, chosen to reach WCAG AA (4.5:1) — e.g. green is #15803D, not the
 * brighter #22C55E. `swatch` is the colour shown in the picker.
 * Indigo is the C66 default and matches theme.ts exactly.
 */
export const ACCENT_KEYS = ['indigo', 'blue', 'violet', 'purple', 'green', 'teal', 'orange', 'rose', 'red', 'slate'] as const
export type AccentKey = (typeof ACCENT_KEYS)[number]
export const DEFAULT_ACCENT: AccentKey = 'indigo'

interface Shades { main: string; dark: string; light: string }
export interface AccentPalette { swatch: string; light: Shades; dark: Shades }

export const ACCENT_PALETTES: Record<AccentKey, AccentPalette> = {
  indigo: { swatch: '#6366F1', light: { main: '#4F46E5', dark: '#4338CA', light: '#6366F1' }, dark: { main: '#818CF8', dark: '#6366F1', light: '#A5B4FC' } },
  blue: { swatch: '#3B82F6', light: { main: '#2563EB', dark: '#1D4ED8', light: '#3B82F6' }, dark: { main: '#60A5FA', dark: '#3B82F6', light: '#93C5FD' } },
  violet: { swatch: '#8B5CF6', light: { main: '#7C3AED', dark: '#6D28D9', light: '#8B5CF6' }, dark: { main: '#A78BFA', dark: '#8B5CF6', light: '#C4B5FD' } },
  purple: { swatch: '#A855F7', light: { main: '#9333EA', dark: '#7E22CE', light: '#A855F7' }, dark: { main: '#C084FC', dark: '#A855F7', light: '#D8B4FE' } },
  green: { swatch: '#22C55E', light: { main: '#15803D', dark: '#166534', light: '#22C55E' }, dark: { main: '#4ADE80', dark: '#22C55E', light: '#86EFAC' } },
  teal: { swatch: '#14B8A6', light: { main: '#0F766E', dark: '#115E59', light: '#14B8A6' }, dark: { main: '#2DD4BF', dark: '#14B8A6', light: '#5EEAD4' } },
  orange: { swatch: '#F97316', light: { main: '#C2410C', dark: '#9A3412', light: '#F97316' }, dark: { main: '#FB923C', dark: '#F97316', light: '#FDBA74' } },
  rose: { swatch: '#F43F5E', light: { main: '#BE123C', dark: '#9F1239', light: '#F43F5E' }, dark: { main: '#FB7185', dark: '#F43F5E', light: '#FDA4AF' } },
  red: { swatch: '#EF4444', light: { main: '#DC2626', dark: '#B91C1C', light: '#EF4444' }, dark: { main: '#F87171', dark: '#EF4444', light: '#FCA5A5' } },
  slate: { swatch: '#64748B', light: { main: '#475569', dark: '#334155', light: '#64748B' }, dark: { main: '#94A3B8', dark: '#64748B', light: '#CBD5E1' } },
}

export function isAccentKey(value: unknown): value is AccentKey {
  return typeof value === 'string' && (ACCENT_KEYS as readonly string[]).includes(value)
}
