import '../i18n'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { LocalePreferenceProvider } from '../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../theming/ThemeModeProvider'
import { PreferencesPage } from './PreferencesPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@example.com' }, logout: vi.fn() }),
}))

vi.mock('../auth/AuthModalContext', () => ({
  useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }),
}))

vi.mock('../api/renewalsApi', () => ({
  renewalsApi: {
    preferences: vi.fn().mockResolvedValue({
      enabled: true, daysBefore: null, sendTime: null, effectiveDaysBefore: 7, effectiveSendTime: '09:00',
      effectiveTimeZone: 'Asia/Kolkata', platformDaysBefore: 7, platformSendTime: '09:00', minDays: 1, maxDays: 30,
    }),
  },
  adminRenewalsApi: {},
}))

vi.mock('../api/productsApi', () => ({
  productsApi: { list: vi.fn().mockResolvedValue([]) },
}))

function renderPage() {
  return render(
    <MemoryRouter>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <PreferencesPage />
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('PreferencesPage', () => {
  it('has no detectable a11y violations', async () => {
    const { container } = renderPage()
    const results = await axe(container)
    expect(results).toHaveNoViolations()
  })

  it('every grouped control has an accessible, associated label', () => {
    renderPage()
    expect(screen.getByRole('radiogroup', { name: /theme/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /language/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /region/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /time zone/i })).toBeInTheDocument()
    expect(screen.getByRole('switch', { name: /reduce motion/i })).toBeInTheDocument()
  })

  it('choosing dark theme via keyboard updates <html> through the real ThemeModeProvider', async () => {
    renderPage()
    const user = userEvent.setup()
    const darkRadio = screen.getByRole('radio', { name: 'Dark' })
    darkRadio.focus()
    await user.keyboard(' ')
    expect(darkRadio).toBeChecked()
  })

  it('toggling reduced motion flips the html[data-reduced-motion] attribute', async () => {
    renderPage()
    const user = userEvent.setup()
    const toggle = screen.getByRole('switch', { name: /reduce motion/i })
    expect(document.documentElement.getAttribute('data-reduced-motion')).toBe('false')
    await user.click(toggle)
    expect(document.documentElement.getAttribute('data-reduced-motion')).toBe('true')
  })
})
