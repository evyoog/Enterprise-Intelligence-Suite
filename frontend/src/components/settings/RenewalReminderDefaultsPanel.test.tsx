import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { RenewalReminderDefaultsPanel } from './RenewalReminderDefaultsPanel'

const defaults = vi.fn()
const saveDefaults = vi.fn()

vi.mock('../../api/renewalsApi', () => ({
  renewalsApi: {},
  adminRenewalsApi: { defaults: () => defaults(), saveDefaults: (b: unknown) => saveDefaults(b) },
}))

const value = { daysBefore: 7, sendTime: '09:00', timeZone: 'Asia/Kolkata', updatedAt: '2026-10-03T06:00:00Z' }

describe('RenewalReminderDefaultsPanel', () => {
  beforeEach(() => {
    defaults.mockReset().mockResolvedValue(value)
    saveDefaults.mockReset()
  })

  it('previews the schedule and saves new defaults', async () => {
    const user = userEvent.setup()
    const onSaved = vi.fn()
    saveDefaults.mockResolvedValue({ ...value, daysBefore: 3 })
    renderWithProviders(<RenewalReminderDefaultsPanel onSaved={onSaved} />)
    expect(await screen.findByText('7 days before')).toBeInTheDocument()
    const days = screen.getByLabelText(/Days before renewal/)
    await user.clear(days)
    await user.type(days, '3')
    expect(screen.queryByText('7 days before')).not.toBeInTheDocument()
    expect(screen.getByText('1 day before')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Save changes' }))
    await waitFor(() => expect(saveDefaults).toHaveBeenCalledWith({ daysBefore: 3, sendTime: '09:00', timeZone: 'Asia/Kolkata' }))
    expect(onSaved).toHaveBeenCalled()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<RenewalReminderDefaultsPanel onSaved={vi.fn()} />)
    await screen.findByText('7 days before')
    expect(await axe(container)).toHaveNoViolations()
  })
})
