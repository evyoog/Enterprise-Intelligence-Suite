import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import type { AdminIncident } from '../../api/serviceStatusApi'
import type { SupportTicket } from '../../api/supportApi'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminIncidentsPage } from './AdminIncidentsPage'

const incidents = vi.fn()
const getPage = vi.fn()
const createIncident = vi.fn()
const updateIncident = vi.fn()
const linkTicket = vi.fn()
const unlinkTicket = vi.fn()
const listTickets = vi.fn()

vi.mock('../../api/serviceStatusApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/serviceStatusApi')>()
  return {
    ...actual,
    adminServiceStatusApi: {
      get: () => getPage(),
      incidents: () => incidents(),
      createIncident: (p: unknown) => createIncident(p),
      updateIncident: (id: number, p: unknown) => updateIncident(id, p),
      linkTicket: (id: number, t: number) => linkTicket(id, t),
      unlinkTicket: (id: number, t: number) => unlinkTicket(id, t),
    },
  }
})
vi.mock('../../api/supportApi', () => ({ adminSupportApi: { listAll: () => listTickets() } }))

const base: AdminIncident = {
  id: 1, productId: 1, productName: 'Thiran.ai', title: 'Slow exports', message: 'Up to 10 minutes.', startedAt: '2026-10-01T08:00:00Z', endedAt: null, open: true,
  severity: 'MAJOR', phase: 'INVESTIGATING', internalNotes: 'Looks like the queue', assignedToCustomerId: 5, assignedToName: 'Asha Rao', resolutionSummary: null,
  closedAt: null, problemId: null, problemTitle: null, tickets: [],
}
const linked: AdminIncident = { ...base, id: 2, title: 'Login errors', productId: 2, productName: 'Valam.ai', severity: 'CRITICAL', phase: 'LOGGED', assignedToCustomerId: null, assignedToName: null,
  problemId: 9, problemTitle: 'Auth cache', tickets: [{ id: 41, subject: 'Cannot sign in', status: 'OPEN', priority: 'HIGH' }] }
const closed: AdminIncident = { ...base, id: 3, title: 'Old outage', phase: 'CLOSED', endedAt: '2026-09-02T10:00:00Z', closedAt: '2026-09-03T10:00:00Z', open: false, resolutionSummary: 'Fixed' }
const tickets = [
  { id: 41, subject: 'Cannot sign in' }, { id: 42, subject: 'Export is slow' },
] as SupportTicket[]

describe('AdminIncidentsPage (C101 IC-1)', () => {
  beforeEach(() => {
    for (const m of [incidents, getPage, createIncident, updateIncident, linkTicket, unlinkTicket, listTickets]) m.mockReset()
    incidents.mockResolvedValue([base, linked, closed])
    getPage.mockResolvedValue({ enabled: true, incidents: [], products: [
      { productId: 1, productName: 'Thiran.ai', status: 'OPERATIONAL', purchased: true, openIncidents: 1 },
      { productId: 2, productName: 'Valam.ai', status: 'OPERATIONAL', purchased: true, openIncidents: 1 },
    ] })
    listTickets.mockResolvedValue(tickets)
  })

  it('lists the open incidents with severity, phase, assignee, tickets and problem, hides closed by default, and has no accessibility violations', async () => {
    const { container } = renderWithProviders(<AdminIncidentsPage />)
    const row = (await screen.findByText('Slow exports')).closest('tr')!
    expect(within(row).getByText('Thiran.ai')).toBeInTheDocument()
    expect(within(row).getByText('Major')).toBeInTheDocument()
    expect(within(row).getByText('Investigating')).toBeInTheDocument()
    expect(within(row).getByText('Ongoing')).toBeInTheDocument()
    expect(within(row).getByText('Asha Rao')).toBeInTheDocument()
    const linkedRow = screen.getByText('Login errors').closest('tr')!
    expect(within(linkedRow).getByText('Critical')).toBeInTheDocument()
    expect(within(linkedRow).getByText('Unassigned')).toBeInTheDocument()
    expect(within(linkedRow).getByText('1')).toBeInTheDocument()
    expect(within(linkedRow).getByRole('link', { name: 'Auth cache' })).toHaveAttribute('href', '/status?tab=problems')
    expect(screen.queryByText('Old outage')).not.toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()

    const user = userEvent.setup()
    await user.click(screen.getByRole('combobox', { name: 'Phase' }))
    await user.click(await screen.findByRole('option', { name: 'All phases' }))
    expect(await screen.findByText('Old outage')).toBeInTheDocument()
  })

  it('offers a retry when the incidents cannot be loaded', async () => {
    incidents.mockRejectedValueOnce(new ApiError(500, 'Boom'))
    renderWithProviders(<AdminIncidentsPage />)
    expect(await screen.findByText(/Boom/)).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByText('Slow exports')).toBeInTheDocument()
  })

  it('shows an empty state with a New incident action', async () => {
    incidents.mockResolvedValue([])
    renderWithProviders(<AdminIncidentsPage />)
    expect(await screen.findByText('No incidents')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: 'New incident' })).toHaveLength(2)
  })

  it('creates an incident from the public and internal groups', async () => {
    createIncident.mockResolvedValue({ ...base, id: 9, title: 'DB failover' })
    renderWithProviders(<AdminIncidentsPage />)
    const user = userEvent.setup()
    await user.click((await screen.findAllByRole('button', { name: 'New incident' }))[0])
    const dialog = await screen.findByRole('dialog', { name: 'New incident' })
    expect(within(dialog).getByRole('heading', { name: 'Shown on the status page' })).toBeInTheDocument()
    expect(within(dialog).getByRole('heading', { name: 'Internal only — never shown to customers' })).toBeInTheDocument()
    expect(await axe(dialog)).toHaveNoViolations()
    // a new incident can be logged, investigating or resolved (never closed)
    await user.click(within(dialog).getByRole('combobox', { name: 'Phase' }))
    expect((await screen.findAllByRole('option')).map((o) => o.textContent)).toEqual(['Logged', 'Investigating', 'Resolved'])
    await user.keyboard('{Escape}')

    await user.click(within(dialog).getByRole('button', { name: 'Create incident' }))
    expect(within(dialog).getByText('Choose a product.')).toBeInTheDocument()
    expect(within(dialog).getByText('Enter a title.')).toBeInTheDocument()
    expect(createIncident).not.toHaveBeenCalled()

    await user.click(within(dialog).getByRole('combobox', { name: /^Product/ }))
    await user.click(await screen.findByRole('option', { name: 'Valam.ai' }))
    await user.type(within(dialog).getByRole('textbox', { name: /^Title/ }), 'DB failover')
    await user.type(within(dialog).getByRole('textbox', { name: /^Message/ }), 'Investigating a failover.')
    await user.click(within(dialog).getByRole('combobox', { name: 'Severity' }))
    await user.click(await screen.findByRole('option', { name: 'Critical' }))
    await user.type(within(dialog).getByRole('textbox', { name: 'Internal notes' }), 'Replica lag')
    await user.type(within(dialog).getByRole('textbox', { name: /Assignee/ }), '5')
    await user.click(within(dialog).getByRole('button', { name: 'Create incident' }))
    await waitFor(() => expect(createIncident).toHaveBeenCalledTimes(1))
    expect(createIncident).toHaveBeenCalledWith(expect.objectContaining({
      productId: 2, title: 'DB failover', message: 'Investigating a failover.', severity: 'CRITICAL', phase: 'LOGGED', endedAt: null,
      internalNotes: 'Replica lag', assignedToCustomerId: 5,
    }))
    expect(await screen.findByText('Incident "DB failover" created')).toBeInTheDocument()
    expect(incidents).toHaveBeenCalledTimes(2)
  }, 20000)

  it('only offers the allowed next phases and requires a resolution summary before RESOLVED', async () => {
    updateIncident.mockResolvedValue({ ...base, phase: 'RESOLVED' })
    renderWithProviders(<AdminIncidentsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit incident Slow exports' }))
    const dialog = await screen.findByRole('dialog', { name: 'Edit incident' })
    await user.click(within(dialog).getByRole('combobox', { name: 'Phase' }))
    // investigating: stay, or resolve. Not back to logged, and not straight to closed
    expect((await screen.findAllByRole('option')).map((o) => o.textContent)).toEqual(['Investigating', 'Resolved'])
    await user.click(screen.getByRole('option', { name: 'Resolved' }))

    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(within(dialog).getByText('Write a resolution summary before marking the incident resolved.')).toBeInTheDocument()
    expect(updateIncident).not.toHaveBeenCalled()

    await user.type(within(dialog).getByRole('textbox', { name: /Resolution summary/ }), 'Restarted the queue')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(updateIncident).toHaveBeenCalledTimes(1))
    expect(updateIncident).toHaveBeenCalledWith(1, expect.objectContaining({
      productId: 1, title: 'Slow exports', startedAt: '2026-10-01T08:00:00Z', endedAt: null, phase: 'RESOLVED',
      resolutionSummary: 'Restarted the queue', assignedToCustomerId: 5,
    }))
    expect(await screen.findByText('Incident "Slow exports" saved')).toBeInTheDocument()
  }, 15000)

  it('shows what the server refuses and keeps the dialog open', async () => {
    updateIncident.mockRejectedValue(new ApiError(400, 'The end time must be after the start time.'))
    renderWithProviders(<AdminIncidentsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit incident Slow exports' }))
    const dialog = await screen.findByRole('dialog', { name: 'Edit incident' })
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(await within(dialog).findByText('The end time must be after the start time.')).toBeInTheDocument()
  })

  it('offers reopening a resolved incident and sends no end time', async () => {
    incidents.mockResolvedValue([{ ...base, phase: 'RESOLVED', endedAt: '2026-10-01T09:00:00Z', open: false, resolutionSummary: 'Done' }])
    updateIncident.mockResolvedValue(base)
    renderWithProviders(<AdminIncidentsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit incident Slow exports' }))
    const dialog = await screen.findByRole('dialog', { name: 'Edit incident' })
    await user.click(within(dialog).getByRole('combobox', { name: 'Phase' }))
    expect((await screen.findAllByRole('option')).map((o) => o.textContent)).toEqual(['Resolved', 'Investigating (reopen)', 'Closed'])
    await user.click(screen.getByRole('option', { name: 'Investigating (reopen)' }))
    expect(within(dialog).queryByLabelText('Ended')).not.toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(updateIncident).toHaveBeenCalledWith(1, expect.objectContaining({ phase: 'INVESTIGATING', endedAt: null })))
  })

  it('shows a closed incident read-only', async () => {
    renderWithProviders(<AdminIncidentsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('combobox', { name: 'Phase' }))
    await user.click(await screen.findByRole('option', { name: 'Closed' }))
    await user.click(await screen.findByRole('button', { name: 'View incident Old outage' }))
    const dialog = await screen.findByRole('dialog', { name: 'Incident (closed)' })
    expect(within(dialog).getByText(/is closed .* and cannot be changed/)).toBeInTheDocument()
    expect(within(dialog).getByRole('textbox', { name: /^Title/ })).toBeDisabled()
    expect(within(dialog).getByRole('textbox', { name: /Resolution summary/ })).toBeDisabled()
    expect(within(dialog).queryByRole('button', { name: 'Save' })).not.toBeInTheDocument()
    expect(within(dialog).queryByRole('button', { name: 'Link ticket' })).not.toBeInTheDocument()
    expect(listTickets).not.toHaveBeenCalled()
    expect(await axe(dialog)).toHaveNoViolations()
  })

  it('links a picked support ticket and unlinks a linked one', async () => {
    linkTicket.mockResolvedValue({ ...base, tickets: [{ id: 42, subject: 'Export is slow', status: 'OPEN', priority: 'LOW' }] })
    unlinkTicket.mockResolvedValue({ ...base, tickets: [] })
    renderWithProviders(<AdminIncidentsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit incident Slow exports' }))
    const dialog = await screen.findByRole('dialog', { name: 'Edit incident' })
    await waitFor(() => expect(listTickets).toHaveBeenCalled())
    await user.click(await within(dialog).findByRole('combobox', { name: 'Add a support ticket' }))
    await user.click(await screen.findByRole('option', { name: '#42 Export is slow' }))
    await user.click(within(dialog).getByRole('button', { name: 'Link ticket' }))
    expect(linkTicket).toHaveBeenCalledWith(1, 42)
    expect(await within(dialog).findByText('#42 Export is slow')).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Unlink ticket 42' }))
    expect(unlinkTicket).toHaveBeenCalledWith(1, 42)
    expect(await within(dialog).findByText('No tickets linked yet.')).toBeInTheDocument()
  })

  it('lets an administrator without ticket access type a ticket id', async () => {
    listTickets.mockRejectedValue(new ApiError(403, 'Forbidden'))
    linkTicket.mockResolvedValue({ ...base, tickets: [{ id: 77, subject: 'Typed', status: 'OPEN', priority: 'LOW' }] })
    renderWithProviders(<AdminIncidentsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit incident Slow exports' }))
    const dialog = await screen.findByRole('dialog', { name: 'Edit incident' })
    await user.type(await within(dialog).findByRole('textbox', { name: 'Support ticket ID' }), '77')
    await user.click(within(dialog).getByRole('button', { name: 'Link ticket' }))
    expect(linkTicket).toHaveBeenCalledWith(1, 77)
  })
})
