import { describe, expect, it } from 'vitest'
import { appColor, contrastWithWhite, DEFAULT_SHOWCASE_COLOR, readableOnWhite, showcasePalette, tint } from './showcaseColor'

describe('showcase colour system', () => {
  it('derives a light tint and a readable text colour', () => {
    expect(tint('#000000', 0.5)).toBe('#808080')
    expect(contrastWithWhite(readableOnWhite('#06B6D4'))).toBeGreaterThanOrEqual(4.5)
    expect(showcasePalette(null).base).toBe(DEFAULT_SHOWCASE_COLOR)
    expect(showcasePalette('not-a-colour').base).toBe(DEFAULT_SHOWCASE_COLOR)
  })

  it('lets an app inherit its platform colour unless it has its own', () => {
    expect(appColor({ accentColor: null, platforms: [{ primaryColor: '#10B981' }] })).toBe('#10B981')
    expect(appColor({ accentColor: '#EC4899', platforms: [{ primaryColor: '#10B981' }] })).toBe('#EC4899')
    expect(appColor({ platforms: [] })).toBe(DEFAULT_SHOWCASE_COLOR)
  })
})
