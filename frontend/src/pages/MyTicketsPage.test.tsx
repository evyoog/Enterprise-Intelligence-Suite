import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { MyTicketsPage } from './MyTicketsPage'

const create = vi.fn()
const myTickets = vi.fn()

vi.mock('../api/supportApi', async () => {
  const actual = await vi.importActual<typeof import('../api/supportApi')>('../api/supportApi')
  return { ...actual, supportApi: { ...actual.supportApi, create: (s: string, d: string) => create(s, d), myTickets: () => myTickets() } }
})

const ticket = {
  id: 1, requestedByCustomerId: 5, requestedByName: 'Jane Customer', subject: "Can't log in",
  description: 'MFA code never arrives.', priority: 'MEDIUM' as const, status: 'OPEN' as const, createdAt: '2027-02-01T00:00:00Z',
}

const slaDates = { firstResponseDueAt: '2027-02-01T08:00:00Z', resolutionDueAt: '2027-02-03T08:00:00Z' }

describe('MyTicketsPage', () => {
  beforeEach(() => {
    create.mockReset()
    myTickets.mockReset()
  })

  it('lists the caller\'s own tickets', async () => {
    myTickets.mockResolvedValue([ticket])
    renderWithProviders(<MyTicketsPage />)
    expect(await screen.findByText("Can't log in")).toBeInTheDocument()
  })

  it('shows the response and resolution targets for an open ticket, without SLA states', async () => {
    myTickets.mockResolvedValue([{ ...ticket, ...slaDates, firstRespondedAt: null }])
    renderWithProviders(<MyTicketsPage />)
    expect(await screen.findByText(/We aim to respond by .*2027.* and resolve by .*2027/)).toBeInTheDocument()
    expect(screen.queryByText(/Breached|At risk|On track/)).not.toBeInTheDocument()
  })

  it('shows only the resolution target once someone has responded', async () => {
    myTickets.mockResolvedValue([{ ...ticket, ...slaDates, status: 'IN_PROGRESS', firstRespondedAt: '2027-02-01T05:00:00Z' }])
    renderWithProviders(<MyTicketsPage />)
    expect(await screen.findByText(/^We aim to resolve by .*2027/)).toBeInTheDocument()
    expect(screen.queryByText(/respond by/)).not.toBeInTheDocument()
  })

  it('shows no targets for resolved or closed tickets or tickets without deadlines', async () => {
    myTickets.mockResolvedValue([
      { ...ticket, id: 2, subject: 'Done', ...slaDates, status: 'RESOLVED' },
      { ...ticket, id: 3, subject: 'Old', status: 'OPEN' },
    ])
    renderWithProviders(<MyTicketsPage />)
    await screen.findByText('Done')
    expect(screen.queryByText(/We aim to/)).not.toBeInTheDocument()
  })

  it('creates a new ticket', async () => {
    myTickets.mockResolvedValueOnce([]).mockResolvedValueOnce([ticket])
    create.mockResolvedValue(ticket)
    renderWithProviders(<MyTicketsPage />)

    const user = userEvent.setup()
    await user.type(await screen.findByLabelText('Subject'), "Can't log in")
    await user.type(screen.getByLabelText('Description'), 'MFA code never arrives.')
    await user.click(screen.getByRole('button', { name: 'Submit ticket' }))
    expect(create).toHaveBeenCalledWith("Can't log in", 'MFA code never arrives.')
  })

  it('shows an empty state', async () => {
    myTickets.mockResolvedValue([])
    renderWithProviders(<MyTicketsPage />)
    expect(await screen.findByText('You have not created any tickets yet.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    myTickets.mockResolvedValue([ticket])
    const { container } = renderWithProviders(<MyTicketsPage />)
    await screen.findByText("Can't log in")
    expect(await axe(container)).toHaveNoViolations()
  })
})
