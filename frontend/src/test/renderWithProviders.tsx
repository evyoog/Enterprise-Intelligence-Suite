import type { ReactElement } from 'react'
import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { LocalePreferenceProvider } from '../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../theming/ThemeModeProvider'

/** The provider stack every page and card renders inside (router, theme, locale). */
export function renderWithProviders(ui: ReactElement, opts: { route?: string } = {}) {
  return render(
    <MemoryRouter initialEntries={opts.route ? [opts.route] : undefined}>
      <ThemeModeProvider>
        <LocalePreferenceProvider>{ui}</LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}
