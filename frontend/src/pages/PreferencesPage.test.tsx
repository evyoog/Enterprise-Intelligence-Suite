import '../i18n'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
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

const getNotificationPrefs = vi.fn()
const updateNotificationPrefs = vi.fn()
vi.mock('../api/notificationsApi', () => ({
  notificationsApi: {
    getPreferences: () => getNotificationPrefs(),
    updatePreferences: (p: unknown) => updateNotificationPrefs(p),
  },
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
  beforeEach(() => {
    localStorage.clear()
    getNotificationPrefs.mockReset().mockResolvedValue({ emailDisabledCategories: [] })
    updateNotificationPrefs.mockReset().mockImplementation((p) => Promise.resolve(p))
  })

  it('shows the four sections in order', async () => {
    renderPage()
    await screen.findByRole('switch', { name: 'Email notifications' })
    expect(screen.getAllByRole('heading', { level: 2 }).map((h) => h.textContent))
      .toEqual(['Appearance', 'Language & Formats', 'Notifications', 'Renewal Reminders'])
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderPage()
    await screen.findByRole('switch', { name: 'Email notifications' })
    await screen.findByText('Daily reminder from 7 days before renewal at 09:00 Asia/Kolkata.')
    const results = await axe(container)
    expect(results).toHaveNoViolations()
  })

  it('every grouped control has an accessible, associated label', () => {
    renderPage()
    expect(screen.getByRole('combobox', { name: /theme/i })).toBeInTheDocument()
    expect(screen.getByRole('radiogroup', { name: /accent color/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /date format/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /time format/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /first day of week/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /language/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /region/i })).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /time zone/i })).toBeInTheDocument()
    expect(screen.getByRole('switch', { name: /reduce motion/i })).toBeInTheDocument()
  })

  it('choosing dark theme updates the real ThemeModeProvider and persists it', async () => {
    renderPage()
    const user = userEvent.setup()
    await user.click(screen.getByRole('combobox', { name: /theme/i }))
    await user.click(await screen.findByRole('option', { name: 'Dark' }))
    expect(screen.getByRole('combobox', { name: /theme/i })).toHaveTextContent('Dark')
    expect(localStorage.getItem('vyoog-theme-mode')).toBe('dark')
  })

  it('selecting an accent colour applies it to the theme and persists it', async () => {
    renderPage()
    const user = userEvent.setup()
    const group = screen.getByRole('radiogroup', { name: /accent color/i })
    expect(within(group).getByRole('radio', { name: 'Indigo' })).toHaveAttribute('aria-checked', 'true')
    await user.click(within(group).getByRole('radio', { name: 'Green' }))
    expect(within(group).getByRole('radio', { name: 'Green' })).toHaveAttribute('aria-checked', 'true')
    expect(localStorage.getItem('vyoog-accent')).toBe('green')
    // The primary button now uses the green accent (#15803D).
    expect(getComputedStyle(screen.getByRole('button', { name: 'Save reminder settings' })).getPropertyValue('--variant-containedBg').trim())
      .toBe('#15803D')
    // Arrow keys move the selection.
    await user.keyboard('{ArrowRight}')
    expect(within(group).getByRole('radio', { name: 'Teal' })).toHaveAttribute('aria-checked', 'true')
  })

  it('saves a date format, time format and first day of week', async () => {
    renderPage()
    const user = userEvent.setup()
    await user.click(screen.getByRole('combobox', { name: /date format/i }))
    await user.click(await screen.findByRole('option', { name: 'YYYY-MM-DD' }))
    await user.click(screen.getByRole('combobox', { name: /time format/i }))
    await user.click(await screen.findByRole('option', { name: '24-hour' }))
    await user.click(screen.getByRole('combobox', { name: /first day of week/i }))
    await user.click(await screen.findByRole('option', { name: 'Monday' }))
    expect(localStorage.getItem('vyoog-date-format')).toBe('YYYY-MM-DD')
    expect(localStorage.getItem('vyoog-time-format')).toBe('24h')
    expect(localStorage.getItem('vyoog-week-start')).toBe('monday')
  })

  it('maps notification switches onto the existing email opt-out categories', async () => {
    renderPage()
    const user = userEvent.setup()
    expect(screen.getByText('Always on')).toBeInTheDocument()
    await user.click(await screen.findByRole('switch', { name: 'Billing' }))
    await waitFor(() => expect(updateNotificationPrefs).toHaveBeenLastCalledWith({ emailDisabledCategories: ['BILLING', 'SUBSCRIPTION'] }))
    await user.click(screen.getByRole('switch', { name: 'Security' }))
    expect(await screen.findByText(/Security emails are off/)).toBeInTheDocument()
    await user.click(screen.getByRole('switch', { name: 'Email notifications' }))
    await waitFor(() => expect(updateNotificationPrefs).toHaveBeenLastCalledWith({
      emailDisabledCategories: ['ORDER', 'BILLING', 'SUBSCRIPTION', 'SYSTEM', 'ORGANIZATION', 'SECURITY', 'PRIVILEGED_ACCESS'],
    }))
    expect(screen.getByRole('switch', { name: 'Billing' })).toBeDisabled()
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
