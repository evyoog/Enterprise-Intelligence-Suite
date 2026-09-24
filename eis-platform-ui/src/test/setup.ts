import '@testing-library/jest-dom/vitest'
import { afterEach, expect } from 'vitest'
import { toHaveNoViolations } from 'jest-axe'

expect.extend(toHaveNoViolations)

afterEach(() => {
  localStorage.clear()
  document.documentElement.removeAttribute('data-reduced-motion')
})

// jsdom doesn't implement matchMedia — ThemeModeProvider/every
// prefers-color-scheme check needs a stand-in, same shape as a real browser
// with no explicit OS dark-mode preference.
if (!window.matchMedia) {
  window.matchMedia = (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }) as unknown as MediaQueryList
}
