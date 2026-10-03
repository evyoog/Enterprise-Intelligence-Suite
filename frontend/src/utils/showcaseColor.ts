/**
 * C66 showcase colour system. A platform has one primary colour (default
 * EIS indigo); an app uses its own accent colour, else its first platform's
 * colour, else the default. Cards stay white — the colour is only used for
 * the icon, badge, small accents, hover and the action arrow. Shades are
 * derived here so no screen hard-codes tints.
 */
export const DEFAULT_SHOWCASE_COLOR = '#6366F1'

export const COLOR_PRESETS: { key: string; hex: string }[] = [
  { key: 'indigo', hex: '#6366F1' },
  { key: 'blue', hex: '#3B82F6' },
  { key: 'purple', hex: '#8B5CF6' },
  { key: 'green', hex: '#10B981' },
  { key: 'orange', hex: '#F97316' },
  { key: 'pink', hex: '#EC4899' },
  { key: 'cyan', hex: '#06B6D4' },
]

const HEX = /^#[0-9a-fA-F]{6}$/

export function isHexColor(value: string | null | undefined): value is string {
  return !!value && HEX.test(value)
}

function toRgb(hex: string): [number, number, number] {
  const n = parseInt(hex.slice(1), 16)
  return [(n >> 16) & 255, (n >> 8) & 255, n & 255]
}

function toHex([r, g, b]: [number, number, number]): string {
  return '#' + [r, g, b].map((v) => Math.round(Math.max(0, Math.min(255, v))).toString(16).padStart(2, '0')).join('').toUpperCase()
}

/** Mixes the colour with white (amount 0–1 of white). */
export function tint(hex: string, amount: number): string {
  const [r, g, b] = toRgb(hex)
  return toHex([r + (255 - r) * amount, g + (255 - g) * amount, b + (255 - b) * amount])
}

/** Mixes the colour with black (amount 0–1 of black). */
export function shade(hex: string, amount: number): string {
  const [r, g, b] = toRgb(hex)
  return toHex([r * (1 - amount), g * (1 - amount), b * (1 - amount)])
}

function luminance(hex: string): number {
  const [r, g, b] = toRgb(hex).map((v) => {
    const c = v / 255
    return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4
  })
  return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

export function contrastWithWhite(hex: string): number {
  return 1.05 / (luminance(hex) + 0.05)
}

/** The colour, darkened until text in it reads on white (WCAG AA 4.5:1). */
export function readableOnWhite(hex: string): string {
  let color = hex
  for (let i = 0; i < 20 && contrastWithWhite(color) < 4.5; i++) {
    color = shade(color, 0.08)
  }
  return color
}

/** The derived set every showcase surface uses. */
export function showcasePalette(hex?: string | null) {
  const base = isHexColor(hex) ? hex.toUpperCase() : DEFAULT_SHOWCASE_COLOR
  return {
    base,
    text: readableOnWhite(base),
    soft: tint(base, 0.9),
    border: tint(base, 0.7),
    hover: shade(base, 0.12),
  }
}

/** An app's effective colour: its own, else its first platform's, else the default. */
export function appColor(app: { accentColor?: string | null; platforms?: { primaryColor?: string | null }[] }): string {
  if (isHexColor(app.accentColor)) return app.accentColor
  const fromPlatform = app.platforms?.find((p) => isHexColor(p.primaryColor))?.primaryColor
  return isHexColor(fromPlatform) ? fromPlatform : DEFAULT_SHOWCASE_COLOR
}
