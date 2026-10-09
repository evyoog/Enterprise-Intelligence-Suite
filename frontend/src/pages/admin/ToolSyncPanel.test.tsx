import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { ToolSyncPanel } from './ToolSyncPanel'

const overview = vi.fn()
const deliveries = vi.fn()
const retry = vi.fn()
const replay = vi.fn()
const pause = vi.fn()
const resume = vi.fn()
const start = vi.fn()
const reconcile = vi.fn()

vi.mock('../../api/toolSyncApi', () => ({
  toolSyncApi: {
    overview: () => overview(),
    deliveries: (f: unknown) => deliveries(f),
    retry: (id: number) => retry(id),
    replay: (id: number) => replay(id),
    pause: (id: number) => pause(id),
    resume: (id: number) => resume(id),
    start: (o: number, c: number) => start(o, c),
    reconcile: (o: number, c: number, r: boolean) => reconcile(o, c, r),
  },
}))

const tenant = {
  organizationId: 100, organizationName: 'ABC Manufacturing', status: 'READY' as const, schemaVersion: '79', lastSyncedAt: '2026-10-09T05:00:00Z',
  lastError: null, pending: 2, failed: 1, lastDeliveredAt: '2026-10-09T05:00:00Z',
}
const connector = { id: 1, productCode: 'thittam', baseMcpUrl: 'https://pms.example.com/api/mcp', clientId: 'thittam-sync', status: 'ACTIVE' as const, tenants: [tenant] }
const failed = {
  id: 7, eventId: 'e-7', connectorId: 1, productCode: 'thittam', organizationId: 100, eventType: 'OrgNodeUpserted', aggregateType: 'OrgNode', aggregateId: '4812',
  status: 'FAILED' as const, attempts: 8, nextAttemptAt: null, lastError: 'UNREACHABLE: connection refused', sentVersion: null, createdAt: '2026-10-09T05:00:00Z', deliveredAt: null,
}
const delivered = { ...failed, id: 8, aggregateId: '4813', status: 'DELIVERED' as const, attempts: 1, lastError: null, deliveredAt: '2026-10-09T05:00:05Z' }

describe('ToolSyncPanel', () => {
  beforeEach(() => {
    for (const m of [overview, deliveries, retry, replay, pause, resume, start, reconcile]) m.mockReset()
    overview.mockResolvedValue([connector])
    deliveries.mockResolvedValue({ items: [failed, delivered], totalElements: 2, page: 0, size: 20 })
  })

  it('shows each tool with the state of its tenants and the messages sent to it', async () => {
    renderWithProviders(<ToolSyncPanel />)
    expect(await screen.findByText('ABC Manufacturing')).toBeInTheDocument()
    expect(screen.getByText('https://pms.example.com/api/mcp')).toBeInTheDocument()
    expect(screen.getByText('Ready')).toBeInTheDocument()
    expect(screen.getByText('UNREACHABLE: connection refused')).toBeInTheDocument()
    expect(screen.getByText('OrgNode 4812')).toBeInTheDocument()
  })

  it('retries a failed message and replays a delivered one', async () => {
    retry.mockResolvedValue({ message: 'Queued again' })
    replay.mockResolvedValue({ message: 'Replay queued' })
    renderWithProviders(<ToolSyncPanel />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Retry message 7' }))
    expect(retry).toHaveBeenCalledWith(7)
    expect(await screen.findByText('Queued again')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Retry message 8' })).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Replay message 8' }))
    expect(replay).toHaveBeenCalledWith(8)
  })

  it('pauses a tool and starts an organization', async () => {
    pause.mockResolvedValue({ message: 'Paused' })
    start.mockResolvedValue({ message: 'Resync queued: 5 message(s)' })
    renderWithProviders(<ToolSyncPanel />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Pause thittam' }))
    expect(pause).toHaveBeenCalledWith(1)
    await user.click(screen.getByRole('button', { name: 'Start or resync ABC Manufacturing in thittam' }))
    expect(start).toHaveBeenCalledWith(100, 1)
    expect(await screen.findByText('Resync queued: 5 message(s)')).toBeInTheDocument()
  })

  it('offers Resume for a paused tool', async () => {
    overview.mockResolvedValue([{ ...connector, status: 'PAUSED' as const }])
    resume.mockResolvedValue({ message: 'Resumed' })
    renderWithProviders(<ToolSyncPanel />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Resume thittam' }))
    expect(resume).toHaveBeenCalledWith(1)
  })

  it('shows what a reconcile found', async () => {
    reconcile.mockResolvedValue({
      organizationId: 100, productCode: 'thittam', reachable: true, problem: null, inSync: false, resent: 1,
      types: [{ aggregateType: 'OrgNode', platformCount: 5, toolCount: 4, inSync: false, resent: 1, extraInTool: 0 },
        { aggregateType: 'User', platformCount: 3, toolCount: 3, inSync: true, resent: 0, extraInTool: 0 }],
    })
    renderWithProviders(<ToolSyncPanel />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Reconcile ABC Manufacturing with thittam' }))
    const dialog = await screen.findByRole('dialog', { name: 'Reconcile with thittam' })
    expect(reconcile).toHaveBeenCalledWith(100, 1, true)
    expect(within(dialog).getByText('Differences found; 1 message was sent again.')).toBeInTheDocument()
    expect(within(dialog).getByText('OrgNode')).toBeInTheDocument()
  })

  it('says so when the tool cannot be reached for a reconcile', async () => {
    reconcile.mockResolvedValue({ organizationId: 100, productCode: 'thittam', reachable: false, problem: 'The tool could not be reached: refused', inSync: false, resent: 0, types: [] })
    renderWithProviders(<ToolSyncPanel />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Reconcile ABC Manufacturing with thittam' }))
    expect(await screen.findByText('The tool could not be reached: refused')).toBeInTheDocument()
  })

  it('filters the messages by status', async () => {
    renderWithProviders(<ToolSyncPanel />)
    await screen.findByText('ABC Manufacturing')
    await userEvent.setup().click(screen.getByRole('combobox', { name: 'Status' }))
    await userEvent.setup().click(await screen.findByRole('option', { name: 'Failed' }))
    await waitFor(() => expect(deliveries).toHaveBeenLastCalledWith(expect.objectContaining({ status: 'FAILED', page: 0 })))
  })

  it('says when no tool is connected', async () => {
    overview.mockResolvedValue([])
    deliveries.mockResolvedValue({ items: [], totalElements: 0, page: 0, size: 20 })
    renderWithProviders(<ToolSyncPanel />)
    expect(await screen.findByText('No tool is connected yet.')).toBeInTheDocument()
    expect(screen.getByText('No messages match this filter.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<ToolSyncPanel />)
    await screen.findByText('ABC Manufacturing')
    expect(await axe(container)).toHaveNoViolations()
  })
})
