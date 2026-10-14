import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminSlaSettingsPage } from './AdminSlaSettingsPage'

const get = vi.fn()
const updatePolicies = vi.fn()
const updateSettings = vi.fn()

vi.mock('../../api/slaApi', () => ({
  adminSlaApi: {
    get: () => get(),
    updatePolicies: (p: unknown) => updatePolicies(p),
    updateSettings: (s: unknown) => updateSettings(s),
  },
}))

const config = {
  policies: [
    { priority: 'LOW', firstResponseMinutes: 480, resolutionMinutes: 2400, aroundTheClock: false },
    { priority: 'MEDIUM', firstResponseMinutes: 240, resolutionMinutes: 1440, aroundTheClock: false },
    { priority: 'HIGH', firstResponseMinutes: 240, resolutionMinutes: 480, aroundTheClock: false },
    { priority: 'URGENT', firstResponseMinutes: 60, resolutionMinutes: 480, aroundTheClock: true },
  ],
  settings: {
    businessDays: ['MON', 'TUE', 'WED', 'THU', 'FRI'], businessStart: '09:00', businessEnd: '18:00',
    timeZone: 'Asia/Kolkata', warnAtPercent: 80, alertEmails: ['support-leads@evyoog.com'],
  },
}

describe('AdminSlaSettingsPage', () => {
  beforeEach(() => {
    for (const m of [get, updatePolicies, updateSettings]) m.mockReset()
    get.mockResolvedValue(config)
  })

  it('shows the targets with human-readable durations and the note', async () => {
    renderWithProviders(<AdminSlaSettingsPage />)
    expect(await screen.findByRole('spinbutton', { name: 'Urgent first response, minutes' })).toHaveValue(60)
    expect(screen.getByRole('spinbutton', { name: 'High resolution, minutes' })).toHaveValue(480)
    expect(screen.getByText('New targets apply to tickets created or re-prioritised from now on.')).toBeInTheDocument()
    expect(screen.getAllByText('1 day (8 business h)').length).toBeGreaterThan(0)
    expect(screen.getByRole('checkbox', { name: 'Urgent around the clock' })).toBeChecked()
    expect(screen.getByRole('checkbox', { name: 'Low around the clock' })).not.toBeChecked()
  })

  it('saves the policies exactly as the backend expects', async () => {
    updatePolicies.mockResolvedValue(config)
    renderWithProviders(<AdminSlaSettingsPage />)
    const user = userEvent.setup()
    const save = await screen.findByRole('button', { name: 'Save targets' })
    expect(save).toBeDisabled()

    const input = screen.getByRole('spinbutton', { name: 'Medium first response, minutes' })
    await user.clear(input)
    await user.type(input, '120')
    await user.click(screen.getByRole('checkbox', { name: 'Low around the clock' }))
    await user.click(save)

    expect(updatePolicies).toHaveBeenCalledWith([
      { priority: 'LOW', firstResponseMinutes: 480, resolutionMinutes: 2400, aroundTheClock: true },
      { priority: 'MEDIUM', firstResponseMinutes: 120, resolutionMinutes: 1440, aroundTheClock: false },
      { priority: 'HIGH', firstResponseMinutes: 240, resolutionMinutes: 480, aroundTheClock: false },
      { priority: 'URGENT', firstResponseMinutes: 60, resolutionMinutes: 480, aroundTheClock: true },
    ])
    expect(await screen.findByText('SLA targets saved.')).toBeInTheDocument()
  })

  it('blocks a first response later than the resolution and a value below 1', async () => {
    renderWithProviders(<AdminSlaSettingsPage />)
    const user = userEvent.setup()
    const save = await screen.findByRole('button', { name: 'Save targets' })

    const first = screen.getByRole('spinbutton', { name: 'High first response, minutes' })
    await user.clear(first)
    await user.type(first, '600')
    expect(screen.getByText('First response cannot be later than resolution.')).toBeInTheDocument()
    expect(save).toBeDisabled()

    await user.clear(first)
    await user.type(first, '0')
    expect(screen.getByText('Enter a whole number of minutes, 1 or more.')).toBeInTheDocument()
    expect(save).toBeDisabled()
    expect(updatePolicies).not.toHaveBeenCalled()
  })

  it('shows the backend error when saving targets fails', async () => {
    updatePolicies.mockRejectedValue(new ApiError(400, 'Urgent must be around the clock.'))
    renderWithProviders(<AdminSlaSettingsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('checkbox', { name: 'Low around the clock' }))
    await user.click(screen.getByRole('button', { name: 'Save targets' }))
    expect(await screen.findByText('Urgent must be around the clock.')).toBeInTheDocument()
  })

  it('saves business hours, warn percent and alert emails', async () => {
    updateSettings.mockResolvedValue(config)
    renderWithProviders(<AdminSlaSettingsPage />)
    const user = userEvent.setup()
    const save = await screen.findByRole('button', { name: 'Save business hours' })
    expect(save).toBeDisabled()

    await user.click(screen.getByRole('button', { name: 'Saturday' }))
    const warn = screen.getByRole('spinbutton', { name: 'Warn at' })
    await user.clear(warn)
    await user.type(warn, '75')
    await user.type(screen.getByRole('combobox', { name: 'Alert email addresses' }), 'oncall@evyoog.com{Enter}')
    await user.click(save)

    expect(updateSettings).toHaveBeenCalledWith({
      businessDays: ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'], businessStart: '09:00', businessEnd: '18:00',
      timeZone: 'Asia/Kolkata', warnAtPercent: 75, alertEmails: ['support-leads@evyoog.com', 'oncall@evyoog.com'],
    })
    expect(await screen.findByText('Business hours and alerts saved.')).toBeInTheDocument()
  })

  it('rejects a malformed email and an out-of-range warn percent', async () => {
    renderWithProviders(<AdminSlaSettingsPage />)
    const user = userEvent.setup()
    await user.type(await screen.findByRole('combobox', { name: 'Alert email addresses' }), 'not-an-email{Enter}')
    expect(screen.getByText('"not-an-email" is not a valid email address.')).toBeInTheDocument()
    expect(screen.queryByText('not-an-email')).not.toBeInTheDocument()

    const warn = screen.getByRole('spinbutton', { name: 'Warn at' })
    await user.clear(warn)
    await user.type(warn, '100')
    expect(screen.getByText('Enter a whole number from 1 to 99.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save business hours' })).toBeDisabled()
  })

  it('requires at least one business day', async () => {
    renderWithProviders(<AdminSlaSettingsPage />)
    const user = userEvent.setup()
    for (const day of ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday']) {
      await user.click(await screen.findByRole('button', { name: day }))
    }
    expect(screen.getByText('Pick at least one business day.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save business hours' })).toBeDisabled()
  })

  it('shows an error with retry when the configuration cannot be loaded', async () => {
    get.mockRejectedValueOnce(new ApiError(500, 'Boom')).mockResolvedValueOnce(config)
    renderWithProviders(<AdminSlaSettingsPage />)
    await user_retry()
    expect(await screen.findByRole('spinbutton', { name: 'Urgent first response, minutes' })).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<AdminSlaSettingsPage />)
    await screen.findByRole('spinbutton', { name: 'Urgent first response, minutes' })
    await waitFor(async () => expect(await axe(container)).toHaveNoViolations())
  })
})

async function user_retry() {
  expect(await screen.findByText(/Could not load the SLA settings/)).toBeInTheDocument()
  await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
}
