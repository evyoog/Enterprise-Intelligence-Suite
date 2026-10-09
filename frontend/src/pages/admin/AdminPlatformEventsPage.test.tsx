import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminPlatformEventsPage } from './AdminPlatformEventsPage'

const list = vi.fn()
const types = vi.fn()
const get = vi.fn()
const retry = vi.fn()
const toolOverview = vi.fn()
const toolDeliveries = vi.fn()

vi.mock('../../api/eventsApi', () => ({
  adminEventsApi: {
    list: (f: unknown) => list(f),
    types: () => types(),
    get: (id: number) => get(id),
    retry: (id: number) => retry(id),
  },
}))

const failed = {
  id: 7, eventId: 'e-7', eventType: 'SubscriptionCreated', aggregateType: 'Subscription', aggregateId: '42',
  occurredAt: '2026-10-03T09:00:00Z', status: 'FAILED' as const, attempts: 10, nextAttemptAt: null, lastError: 'provisioning: timeout',
}
const delivered = { ...failed, id: 8, eventId: 'e-8', eventType: 'InvoiceGenerated', aggregateType: 'Invoice', aggregateId: '3', status: 'DELIVERED' as const, attempts: 1, lastError: null }

vi.mock('../../api/toolSyncApi', () => ({
  toolSyncApi: { overview: () => toolOverview(), deliveries: (f: unknown) => toolDeliveries(f) },
}))

describe('AdminPlatformEventsPage', () => {
  beforeEach(() => {
    for (const m of [list, types, get, retry]) m.mockReset()
    list.mockResolvedValue({ items: [failed, delivered], totalElements: 2, page: 0, size: 20 })
    types.mockResolvedValue(['InvoiceGenerated', 'SubscriptionCreated'])
  })

  it('lists events with their status', async () => {
    renderWithProviders(<AdminPlatformEventsPage />)
    expect(await screen.findByText('Subscription #42')).toBeInTheDocument()
    expect(screen.getByText('Invoice #3')).toBeInTheDocument()
    expect(screen.getAllByText('Failed').length).toBeGreaterThan(0)
  })

  it('retries a failed event', async () => {
    retry.mockResolvedValue({ ...failed, status: 'PENDING', attempts: 0, deliveredAt: null, payload: '{}', receipts: [] })
    renderWithProviders(<AdminPlatformEventsPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Retry SubscriptionCreated event 7' }))
    expect(retry).toHaveBeenCalledWith(7)
    expect(await screen.findByText('Event queued for another attempt.')).toBeInTheDocument()
  })

  it('filters by status from a tile', async () => {
    renderWithProviders(<AdminPlatformEventsPage />)
    await screen.findByText('Subscription #42')
    await userEvent.setup().click(screen.getByRole('button', { name: /Delivered/ }))
    await waitFor(() => expect(list).toHaveBeenLastCalledWith(expect.objectContaining({ status: 'DELIVERED', page: 0 })))
  })

  it('shows the payload and receipts of an event', async () => {
    get.mockResolvedValue({ ...delivered, deliveredAt: '2026-10-03T09:00:05Z', payload: '{"invoiceId":3}', receipts: [{ handler: 'provisioning', processedAt: '2026-10-03T09:00:05Z' }] })
    renderWithProviders(<AdminPlatformEventsPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'View InvoiceGenerated event 8' }))
    expect(await screen.findByRole('dialog', { name: 'InvoiceGenerated' })).toBeInTheDocument()
    expect(screen.getByText(/"invoiceId": 3/)).toBeInTheDocument()
    expect(screen.getByText(/provisioning —/)).toBeInTheDocument()
  })

  it('has a Tool sync tab beside the events', async () => {
    toolOverview.mockResolvedValue([])
    toolDeliveries.mockResolvedValue({ items: [], totalElements: 0, page: 0, size: 20 })
    renderWithProviders(<AdminPlatformEventsPage />)
    await screen.findByText('Subscription #42')
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Tool sync' }))
    expect(await screen.findByText('No tool is connected yet.')).toBeInTheDocument()
    expect(screen.queryByText('Subscription #42')).not.toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Platform events' }))
    expect(await screen.findByText('Subscription #42')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<AdminPlatformEventsPage />)
    await screen.findByText('Subscription #42')
    expect(await axe(container)).toHaveNoViolations()
  })
})
