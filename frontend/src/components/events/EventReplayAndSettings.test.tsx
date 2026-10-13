import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { EventReplayDialog } from './EventReplayDialog'
import { EventSettingsDialog } from './EventSettingsDialog'

const receivers = vi.fn()
const replay = vi.fn()
const settings = vi.fn()
const updateSettings = vi.fn()

vi.mock('../../api/eventsApi', () => ({
  adminEventsApi: {
    receivers: () => receivers(),
    replay: (b: unknown) => replay(b),
    settings: () => settings(),
    updateSettings: (b: unknown) => updateSettings(b),
  },
}))

// REQ-INT-002 (C91): replay to one receiver (EV-1 B), configurable retention (EV-3) and failure alerts (EV-4).
describe('EventReplayDialog', () => {
  beforeEach(() => {
    ;[receivers, replay, settings, updateSettings].forEach((m) => m.mockReset())
    receivers.mockResolvedValue(['provisioning', 'webhook-dispatcher'])
  })

  async function fill(user: ReturnType<typeof userEvent.setup>) {
    await user.click(await screen.findByRole('combobox', { name: 'Receiver' }))
    await user.click(await screen.findByRole('option', { name: 'provisioning' }))
  }

  it('previews the size of a replay and then sends it to the chosen receiver', async () => {
    const user = userEvent.setup()
    replay.mockResolvedValueOnce({ preview: true, receiver: 'provisioning', matched: 12, replayed: 0, failed: 0, limit: 1000, failures: [] })
    replay.mockResolvedValueOnce({ preview: false, receiver: 'provisioning', matched: 12, replayed: 11, failed: 1, limit: 1000,
      failures: [{ eventId: 'e-9', eventType: 'SubscriptionCreated', error: 'product still down' }] })
    const { baseElement } = renderWithProviders(<EventReplayDialog open eventTypes={['SubscriptionCreated']} onClose={() => {}} />)
    const dialog = await screen.findByRole('dialog', { name: 'Replay events' })
    expect(within(dialog).getByRole('button', { name: 'Replay now' })).toBeDisabled() // nothing previewed yet

    await fill(user)
    await user.click(within(dialog).getByRole('button', { name: 'Preview' }))
    expect(await within(dialog).findByText('12 events would be sent to provisioning.')).toBeInTheDocument()
    expect(replay).toHaveBeenLastCalledWith(expect.objectContaining({ receiver: 'provisioning', preview: true, eventType: undefined }))
    expect(await axe(baseElement)).toHaveNoViolations()

    await user.click(within(dialog).getByRole('button', { name: 'Replay now' }))
    expect(await within(dialog).findByText('Replay to provisioning: 11 sent, 1 failed.')).toBeInTheDocument()
    expect(within(dialog).getByText('product still down')).toBeInTheDocument()
    expect(replay).toHaveBeenLastCalledWith(expect.objectContaining({ receiver: 'provisioning', preview: false }))
    expect(within(dialog).getByRole('button', { name: 'Replay now' })).toBeDisabled() // a replay is not repeated by accident
  })

  it('does not allow a replay that is too large and shows the backend message', async () => {
    const user = userEvent.setup()
    replay.mockResolvedValueOnce({ preview: true, receiver: 'provisioning', matched: 5000, replayed: 0, failed: 0, limit: 1000, failures: [] })
    renderWithProviders(<EventReplayDialog open eventTypes={[]} onClose={() => {}} />)
    const dialog = await screen.findByRole('dialog', { name: 'Replay events' })
    await fill(user)
    await user.click(within(dialog).getByRole('button', { name: 'Preview' }))
    expect(await within(dialog).findByText(/at most 1000/)).toBeInTheDocument()
    expect(within(dialog).getByRole('button', { name: 'Replay now' })).toBeDisabled()

    replay.mockRejectedValueOnce(new ApiError(404, 'Receiver not found'))
    await user.click(within(dialog).getByRole('button', { name: 'Preview' }))
    expect(await within(dialog).findByText('Receiver not found')).toBeInTheDocument()
  })

  it('refuses a range that ends before it starts', async () => {
    const user = userEvent.setup()
    renderWithProviders(<EventReplayDialog open eventTypes={[]} onClose={() => {}} />)
    const dialog = await screen.findByRole('dialog', { name: 'Replay events' })
    await fill(user)
    const to = within(dialog).getByLabelText('To')
    await user.clear(to)
    await user.type(to, '2020-01-01')
    expect(await within(dialog).findByText('The last day must not be before the first day.')).toBeInTheDocument()
    expect(within(dialog).getByRole('button', { name: 'Preview' })).toBeDisabled()
  })
})

describe('EventSettingsDialog', () => {
  beforeEach(() => {
    ;[settings, updateSettings].forEach((m) => m.mockReset())
    settings.mockResolvedValue({ retentionDays: 30, alertRecipients: ['ops@vyoog.com'], alertInApp: true, alertEmail: true, updatedAt: '2026-10-12T00:00:00Z' })
  })

  it('shows the defaults and saves a new retention period and recipients', async () => {
    const user = userEvent.setup()
    updateSettings.mockResolvedValue({ retentionDays: 90, alertRecipients: ['ops@vyoog.com', 'dev@vyoog.com'], alertInApp: true, alertEmail: false, updatedAt: '2026-10-12T00:00:00Z' })
    const { baseElement } = renderWithProviders(<EventSettingsDialog open onClose={() => {}} />)
    const dialog = await screen.findByRole('dialog', { name: 'Event platform settings' })
    const days = await within(dialog).findByLabelText('Keep delivered events for (days)')
    await waitFor(() => expect(days).toHaveValue(30))
    expect(within(dialog).getByLabelText('Email addresses to alert')).toHaveValue('ops@vyoog.com')
    expect(await axe(baseElement)).toHaveNoViolations()

    await user.clear(days)
    await user.type(days, '90')
    await user.type(within(dialog).getByLabelText('Email addresses to alert'), '{enter}dev@vyoog.com')
    await user.click(within(dialog).getByRole('switch', { name: 'Notify by email' }))
    await user.click(within(dialog).getByRole('button', { name: 'Save settings' }))

    await waitFor(() => expect(updateSettings).toHaveBeenCalledWith({
      retentionDays: 90, alertRecipients: ['ops@vyoog.com', 'dev@vyoog.com'], alertInApp: true, alertEmail: false,
    }))
    expect(await within(dialog).findByText('Settings saved.')).toBeInTheDocument()
  })

  it('does not save an invalid retention period and shows a backend refusal', async () => {
    const user = userEvent.setup()
    updateSettings.mockRejectedValue(new ApiError(400, '"nope" is not a valid email address.'))
    renderWithProviders(<EventSettingsDialog open onClose={() => {}} />)
    const dialog = await screen.findByRole('dialog', { name: 'Event platform settings' })
    const days = await within(dialog).findByLabelText('Keep delivered events for (days)')
    await user.clear(days)
    await user.type(days, '0')
    expect(within(dialog).getByRole('button', { name: 'Save settings' })).toBeDisabled()
    expect(within(dialog).getByText('Enter a whole number of days from 1 to 3650.')).toBeInTheDocument()

    await user.clear(days)
    await user.type(days, '30')
    await user.click(within(dialog).getByRole('button', { name: 'Save settings' }))
    expect(await within(dialog).findByText('"nope" is not a valid email address.')).toBeInTheDocument()
  })
})
