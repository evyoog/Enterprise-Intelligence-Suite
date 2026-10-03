import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { RenewalRemindersCard } from './RenewalRemindersCard'

const preferences = vi.fn()
const savePreferences = vi.fn()

vi.mock('../../api/renewalsApi', () => ({
  renewalsApi: { preferences: () => preferences(), savePreferences: (b: unknown) => savePreferences(b), myRenewals: vi.fn() },
  adminRenewalsApi: {},
}))

const defaults = {
  enabled: true, daysBefore: null, sendTime: null, effectiveDaysBefore: 7, effectiveSendTime: '09:00',
  effectiveTimeZone: 'Asia/Kolkata', platformDaysBefore: 7, platformSendTime: '09:00', minDays: 1, maxDays: 30,
}

describe('RenewalRemindersCard', () => {
  beforeEach(() => {
    preferences.mockReset().mockResolvedValue(defaults)
    savePreferences.mockReset()
  })

  it('shows the platform defaults and the schedule', async () => {
    renderWithProviders(<RenewalRemindersCard />)
    expect(await screen.findByText('Daily reminder from 7 days before renewal at 09:00 Asia/Kolkata.')).toBeInTheDocument()
    expect(screen.getByText('Leave empty for the platform default: 7 days.')).toBeInTheDocument()
  })

  it('updates the summary as the values change', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RenewalRemindersCard />)
    await user.type(await screen.findByLabelText('Start reminding (days before renewal)'), '10')
    expect(screen.getByRole('status')).toHaveTextContent('Daily reminder from 10 days before renewal at 09:00 Asia/Kolkata.')
  })

  it('saves own days and refuses an out-of-range value', async () => {
    const user = userEvent.setup()
    savePreferences.mockResolvedValue({ ...defaults, daysBefore: 3, effectiveDaysBefore: 3 })
    renderWithProviders(<RenewalRemindersCard />)
    const days = await screen.findByLabelText('Start reminding (days before renewal)')
    await user.type(days, '45')
    expect(screen.getByText('Enter a number from 1 to 30.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save reminder settings' })).toBeDisabled()
    await user.clear(days)
    await user.type(days, '3')
    await user.click(screen.getByRole('button', { name: 'Save reminder settings' }))
    await waitFor(() => expect(savePreferences).toHaveBeenCalledWith({ enabled: true, daysBefore: 3, sendTime: null }))
    expect(await screen.findByText('Reminder settings saved.')).toBeInTheDocument()
  })

  it('explains that turning reminders off does not stop renewal', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RenewalRemindersCard />)
    await user.click(await screen.findByRole('switch', { name: 'Send renewal reminders' }))
    expect(screen.getByText('Your subscriptions still renew; only the emails stop.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<RenewalRemindersCard />)
    await screen.findByText('Daily reminder from 7 days before renewal at 09:00 Asia/Kolkata.')
    expect(await axe(container)).toHaveNoViolations()
  })
})
