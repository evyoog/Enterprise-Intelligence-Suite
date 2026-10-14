import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { RenewalRecipientsSection } from './RenewalRecipientsSection'

const get = vi.fn()
const save = vi.fn()
const candidates = vi.fn()

vi.mock('../../api/renewalsApi', () => ({
  reminderRecipientsApi: { get: () => get(), save: (b: unknown) => save(b), candidates: () => candidates() },
}))

const person = (customerId: number, name: string, reason: 'ORG_ADMIN' | 'MANAGE_BILLING' | 'ADDED', emailEnabled = true, inAppEnabled = true) =>
  ({ customerId, name, email: `${name.split(' ')[0].toLowerCase()}@acme.test`, reason, emailEnabled, inAppEnabled })
const initial = {
  defaults: [person(1, 'Asha Admin', 'ORG_ADMIN'), person(2, 'Bala Billing', 'MANAGE_BILLING')],
  added: [person(3, 'Chitra Finance', 'ADDED', true, false)],
}

describe('RenewalRecipientsSection', () => {
  beforeEach(() => {
    for (const m of [get, save, candidates]) m.mockReset()
    get.mockResolvedValue(initial)
    // The server already leaves out the default recipients.
    candidates.mockResolvedValue([
      { customerId: 3, name: 'Chitra Finance', email: 'chitra@acme.test' },
      { customerId: 4, name: 'Dev Ops', email: 'dev@acme.test' },
    ])
  })

  it('lists the default recipients read-only with their reasons, and the added people with their channels', async () => {
    renderWithProviders(<RenewalRecipientsSection />)
    expect(await screen.findByText('Asha Admin')).toBeInTheDocument()
    const defaults = within(screen.getByRole('region', { name: 'Always receive reminders' }))
    expect(defaults.getByText('Organization admin')).toBeInTheDocument()
    expect(defaults.getByText('Manage billing')).toBeInTheDocument()
    expect(defaults.queryByRole('checkbox')).not.toBeInTheDocument()
    expect(screen.getByRole('checkbox', { name: 'Email for Chitra Finance' })).toBeChecked()
    expect(screen.getByRole('checkbox', { name: 'In-app for Chitra Finance' })).not.toBeChecked()
    expect(screen.getByRole('button', { name: 'Save recipients' })).toBeDisabled()
  })

  it('adds a member, changes channels, removes someone and saves the payload', async () => {
    const user = userEvent.setup()
    save.mockImplementation((b: { recipients: { customerId: number; email: boolean; inApp: boolean }[] }) => Promise.resolve({
      defaults: initial.defaults,
      added: b.recipients.map((r) => person(r.customerId, r.customerId === 4 ? 'Dev Ops' : 'Chitra Finance', 'ADDED', r.email, r.inApp)),
    }))
    renderWithProviders(<RenewalRecipientsSection />)
    await screen.findByText('Chitra Finance')

    await user.click(screen.getByRole('combobox', { name: 'Add person' }))
    // People already added are not offered again.
    const options = screen.getAllByRole('option').map((o) => o.textContent)
    expect(options).toEqual(['Dev Ops (dev@acme.test)'])
    await user.click(screen.getByRole('option', { name: 'Dev Ops (dev@acme.test)' }))

    await user.click(screen.getByRole('checkbox', { name: 'In-app for Dev Ops' }))
    await user.click(screen.getByRole('checkbox', { name: 'In-app for Chitra Finance' }))
    await user.click(screen.getByRole('button', { name: 'Save recipients' }))

    expect(save).toHaveBeenCalledWith({
      recipients: [{ customerId: 3, email: true, inApp: true }, { customerId: 4, email: true, inApp: false }],
    })
    expect(await screen.findByText('Reminder recipients saved.')).toBeInTheDocument()
  })

  it('removes an added person', async () => {
    const user = userEvent.setup()
    save.mockResolvedValue({ defaults: initial.defaults, added: [] })
    renderWithProviders(<RenewalRecipientsSection />)
    await user.click(await screen.findByRole('button', { name: 'Remove Chitra Finance' }))
    expect(screen.getByText('No one else has been added.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Save recipients' }))
    expect(save).toHaveBeenCalledWith({ recipients: [] })
  })

  it('needs at least one channel per person', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RenewalRecipientsSection />)
    await user.click(await screen.findByRole('checkbox', { name: 'Email for Chitra Finance' }))
    expect(screen.getByText('Choose email, in-app or both for Chitra Finance.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save recipients' })).toBeDisabled()
  })

  it('shows the server message when saving fails', async () => {
    const user = userEvent.setup()
    save.mockRejectedValue(new ApiError(409, 'Only active members can be added.'))
    renderWithProviders(<RenewalRecipientsSection />)
    await user.click(await screen.findByRole('checkbox', { name: 'In-app for Chitra Finance' }))
    await user.click(screen.getByRole('button', { name: 'Save recipients' }))
    expect(await screen.findByText('Only active members can be added.')).toBeInTheDocument()
  })

  it('stays hidden without Manage billing', async () => {
    get.mockRejectedValue(new ApiError(403, 'Forbidden'))
    const { container } = renderWithProviders(<RenewalRecipientsSection />)
    await waitFor(() => expect(get).toHaveBeenCalled())
    await waitFor(() => expect(container).toBeEmptyDOMElement())
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<RenewalRecipientsSection />)
    await screen.findByText('Chitra Finance')
    expect(await axe(container)).toHaveNoViolations()
  })
})
