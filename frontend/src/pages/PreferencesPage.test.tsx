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

function renderPage(route = '/account/preferences') {
  return render(
    <MemoryRouter initialEntries={[route]}>
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

  const open = async (user: ReturnType<typeof userEvent.setup>, name: RegExp) => {
    await user.click(screen.getByRole('tab', { name }))
  }

  it('shows a navigation of four sections and only the selected one', async () => {
    renderPage()
    const tabs = screen.getAllByRole('tab')
    expect(tabs.map((t) => t.textContent)).toEqual([
      'AppearanceTheme, motion and accent', 'Language & FormatsLanguage, region and formats',
      'NotificationsEmail and in-app', 'Renewal RemindersBefore subscriptions renew',
    ])
    expect(screen.getByRole('tab', { name: /Appearance/ })).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('tabpanel')).toHaveAccessibleName(/Appearance/)
    expect(screen.getAllByRole('heading', { level: 2 }).map((h) => h.textContent)).toEqual(['Appearance'])
    expect(screen.queryByRole('combobox', { name: /language/i })).not.toBeInTheDocument()

    const user = userEvent.setup()
    await open(user, /Language & Formats/)
    expect(screen.getAllByRole('heading', { level: 2 }).map((h) => h.textContent)).toEqual(['Language & Formats'])
    expect(screen.getByRole('combobox', { name: /language/i })).toBeInTheDocument()
  })

  it('moves between sections with the arrow keys and opens a section from the URL', async () => {
    renderPage('/account/preferences?section=renewals')
    expect(screen.getByRole('tab', { name: /Renewal Reminders/ })).toHaveAttribute('aria-selected', 'true')
    expect(await screen.findByText('Daily reminder from 7 days before renewal at 09:00 Asia/Kolkata.')).toBeInTheDocument()
    const user = userEvent.setup()
    screen.getByRole('tab', { name: /Renewal Reminders/ }).focus()
    await user.keyboard('{ArrowDown}')
    expect(screen.getByRole('tab', { name: /Appearance/ })).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('tab', { name: /Appearance/ })).toHaveFocus()
  })

  it('every control has an accessible, associated label', async () => {
    renderPage()
    expect(screen.getByRole('combobox', { name: /theme/i })).toBeInTheDocument()
    expect(screen.getByRole('switch', { name: /reduce motion/i })).toBeInTheDocument()
    expect(screen.getByRole('radiogroup', { name: /accent color/i })).toBeInTheDocument()
    const user = userEvent.setup()
    await open(user, /Language & Formats/)
    for (const name of [/language/i, /region/i, /time zone/i, /date format/i, /time format/i]) {
      expect(screen.getByRole('combobox', { name })).toBeInTheDocument()
    }
  })

  it('has no detectable a11y violations on any section', async () => {
    const { container } = renderPage()
    expect(await axe(container)).toHaveNoViolations()
    const user = userEvent.setup()
    await open(user, /Language & Formats/)
    expect(await axe(container)).toHaveNoViolations()
    await open(user, /Notifications/)
    await screen.findByRole('switch', { name: 'Email notifications' })
    expect(await axe(container)).toHaveNoViolations()
    await open(user, /Renewal Reminders/)
    await screen.findByText('Daily reminder from 7 days before renewal at 09:00 Asia/Kolkata.')
    expect(await axe(container)).toHaveNoViolations()
  })

  it('choosing dark theme updates the real ThemeModeProvider and persists it', async () => {
    renderPage()
    const user = userEvent.setup()
    await user.click(screen.getByRole('combobox', { name: /theme/i }))
    await user.click(await screen.findByRole('option', { name: 'Dark' }))
    expect(screen.getByRole('combobox', { name: /theme/i })).toHaveTextContent('Dark')
    expect(localStorage.getItem('vyoog-theme-mode')).toBe('dark')
  })

  it('toggling reduced motion flips the html[data-reduced-motion] attribute', async () => {
    renderPage()
    const user = userEvent.setup()
    const toggle = screen.getByRole('switch', { name: /reduce motion/i })
    expect(document.documentElement.getAttribute('data-reduced-motion')).toBe('false')
    await user.click(toggle)
    expect(document.documentElement.getAttribute('data-reduced-motion')).toBe('true')
    await user.click(toggle)
  })

  it('selecting an accent colour applies it to the theme and persists it', async () => {
    renderPage()
    const user = userEvent.setup()
    const group = screen.getByRole('radiogroup', { name: /accent color/i })
    expect(within(group).getByRole('radio', { name: 'Indigo' })).toHaveAttribute('aria-checked', 'true')
    await user.click(within(group).getByRole('radio', { name: 'Green' }))
    expect(within(group).getByRole('radio', { name: 'Green' })).toHaveAttribute('aria-checked', 'true')
    expect(localStorage.getItem('vyoog-accent')).toBe('green')
    await user.keyboard('{ArrowRight}')
    expect(within(group).getByRole('radio', { name: 'Teal' })).toHaveAttribute('aria-checked', 'true')
    await user.keyboard('{ArrowLeft}')
    // The primary button on another section now uses the green accent (#15803D).
    await open(user, /Renewal Reminders/)
    const save = await screen.findByRole('button', { name: 'Save reminder settings' })
    expect(getComputedStyle(save).getPropertyValue('--variant-containedBg').trim()).toBe('#15803D')
  })

  it('saves a date format and time format', async () => {
    renderPage('/account/preferences?section=formats')
    const user = userEvent.setup()
    await user.click(screen.getByRole('combobox', { name: /date format/i }))
    await user.click(await screen.findByRole('option', { name: 'YYYY-MM-DD' }))
    await user.click(screen.getByRole('combobox', { name: /time format/i }))
    await user.click(await screen.findByRole('option', { name: '24-hour' }))
    expect(localStorage.getItem('vyoog-date-format')).toBe('YYYY-MM-DD')
    expect(localStorage.getItem('vyoog-time-format')).toBe('24h')
    expect(screen.queryByRole('combobox', { name: /first day of week/i })).not.toBeInTheDocument()
  })

  it('maps notification switches onto the existing email opt-out categories', async () => {
    renderPage('/account/preferences?section=notifications')
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

  it('offers a section selector for small screens', async () => {
    renderPage()
    const user = userEvent.setup()
    await user.click(screen.getByRole('combobox', { name: 'Section' }))
    await user.click(await screen.findByRole('option', { name: 'Notifications' }))
    expect(screen.getByRole('tab', { name: /Notifications/ })).toHaveAttribute('aria-selected', 'true')
  })
})
