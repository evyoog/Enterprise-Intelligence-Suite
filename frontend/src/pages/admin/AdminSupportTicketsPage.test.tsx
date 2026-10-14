import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminSupportTicketsPage } from './AdminSupportTicketsPage'

const listAll = vi.fn()
const escalate = vi.fn()
const resolve = vi.fn()
const close = vi.fn()

vi.mock('../../api/supportApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/supportApi')>('../../api/supportApi')
  return {
    ...actual,
    adminSupportApi: {
      ...actual.adminSupportApi,
      listAll: () => listAll(),
      escalate: (id: number) => escalate(id),
      resolve: (id: number, note?: string) => resolve(id, note),
      close: (id: number) => close(id),
    },
  }
})

const openTicket = {
  id: 1, requestedByCustomerId: 5, requestedByName: 'Jane Customer', subject: "Can't log in",
  description: 'MFA code never arrives.', priority: 'MEDIUM' as const, status: 'OPEN' as const, createdAt: '2027-02-01T00:00:00Z',
}
const resolvedTicket = { ...openTicket, id: 2, status: 'RESOLVED' as const }
const sla = { firstRespondedAt: null, firstResponseDueAt: '2027-02-01T08:00:00Z', resolutionDueAt: '2027-02-03T08:00:00Z' }
const warningTicket = { ...openTicket, id: 3, subject: 'Slow dashboard', ...sla, firstResponseState: 'WARNING' as const, resolutionState: 'ON_TRACK' as const }
const breachedTicket = { ...openTicket, id: 4, subject: 'Payments down', status: 'ESCALATED' as const, ...sla, firstResponseState: 'BREACHED' as const, resolutionState: 'ON_TRACK' as const }

describe('AdminSupportTicketsPage', () => {
  beforeEach(() => {
    for (const m of [listAll, escalate, resolve, close]) m.mockReset()
  })

  it('escalates a ticket', async () => {
    listAll.mockResolvedValue([openTicket])
    escalate.mockResolvedValue({ ...openTicket, status: 'ESCALATED', priority: 'URGENT' })
    renderWithProviders(<AdminSupportTicketsPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Escalate' }))
    expect(escalate).toHaveBeenCalledWith(1)
  })

  it('resolves a ticket', async () => {
    listAll.mockResolvedValue([openTicket])
    resolve.mockResolvedValue({ ...openTicket, status: 'RESOLVED' })
    renderWithProviders(<AdminSupportTicketsPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Resolve' }))
    expect(resolve).toHaveBeenCalledWith(1, undefined)
  })

  it('closes a resolved ticket', async () => {
    listAll.mockResolvedValue([resolvedTicket])
    close.mockResolvedValue({ ...resolvedTicket, status: 'CLOSED' })
    renderWithProviders(<AdminSupportTicketsPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Close' }))
    expect(close).toHaveBeenCalledWith(2)
  })

  it('shows both SLA chips with due times, and flags a breached ticket', async () => {
    listAll.mockResolvedValue([openTicket, breachedTicket])
    renderWithProviders(<AdminSupportTicketsPage />)

    expect(await screen.findByText('First response: Breached')).toBeInTheDocument()
    expect(screen.getByText('Resolution: On track')).toBeInTheDocument()
    expect(screen.getAllByText(/^Due /)).toHaveLength(2)
    expect(screen.getAllByText('SLA breached')).toHaveLength(1)
  })

  it('filters by SLA state', async () => {
    listAll.mockResolvedValue([openTicket, warningTicket, breachedTicket])
    renderWithProviders(<AdminSupportTicketsPage />)
    const user = userEvent.setup()
    expect(await screen.findByText('Slow dashboard')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'At risk (warning)' }))
    expect(screen.getByText('Slow dashboard')).toBeInTheDocument()
    expect(screen.queryByText('Payments down')).not.toBeInTheDocument()
    expect(screen.queryByText("Can't log in")).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Breached' }))
    expect(screen.getByText('Payments down')).toBeInTheDocument()
    expect(screen.queryByText('Slow dashboard')).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'All' }))
    expect(screen.getAllByText(/Can't log in|Slow dashboard|Payments down/)).toHaveLength(3)
  })

  it('shows an empty state when nothing matches the SLA filter', async () => {
    listAll.mockResolvedValue([openTicket])
    renderWithProviders(<AdminSupportTicketsPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Breached' }))
    expect(screen.getByText('No ticket has breached its SLA.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    listAll.mockResolvedValue([openTicket, resolvedTicket, warningTicket, breachedTicket])
    const { container } = renderWithProviders(<AdminSupportTicketsPage />)
    await screen.findAllByText("Can't log in")
    expect(await axe(container)).toHaveNoViolations()
  })
})
