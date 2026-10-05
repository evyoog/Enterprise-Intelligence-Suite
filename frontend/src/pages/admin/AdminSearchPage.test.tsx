import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminSearchPage } from './AdminSearchPage'

const api = {
  status: vi.fn(), rebuild: vi.fn(), insights: vi.fn(), synonyms: vi.fn(), createSynonym: vi.fn(), deleteSynonym: vi.fn(),
}
vi.mock('../../api/searchAdminApi', () => ({
  searchAdminApi: {
    status: () => api.status(), rebuild: (r: boolean) => api.rebuild(r), insights: (d: number) => api.insights(d),
    synonyms: () => api.synonyms(), createSynonym: (t: string[]) => api.createSynonym(t), deleteSynonym: (id: number) => api.deleteSynonym(id),
  },
}))

const status = {
  engine: 'POSTGRES', keywordIndexInstalled: true, semanticInstalled: true,
  documents: { PRODUCT: 12, KNOWLEDGE: 30, TICKET: 4 }, chunks: 80, embeddedChunks: 75, pendingChunks: 5,
  embedding: { configured: true, available: true, provider: 'stub', model: 'stub-hashing-v1', dimension: 384 },
  lastRun: { id: 1, trigger: 'ADMIN', status: 'DONE', documents: 46, chunks: 80, embedded: 75, startedAt: '2026-10-05T10:00:00Z', finishedAt: '2026-10-05T10:01:00Z' },
  settings: { typoThreshold: 0.5, chunkSize: 600 },
}

describe('AdminSearchPage', () => {
  beforeEach(() => {
    Object.values(api).forEach((f) => f.mockReset())
    api.status.mockResolvedValue(status)
    api.rebuild.mockResolvedValue(undefined)
    api.synonyms.mockResolvedValue([{ id: 7, terms: ['invoice', 'bill', 'factura'], createdAt: '2026-10-05T10:00:00Z' }])
    api.createSynonym.mockResolvedValue({ id: 8, terms: ['receipt', 'voucher'], createdAt: '2026-10-05T10:00:00Z' })
    api.deleteSynonym.mockResolvedValue(undefined)
    api.insights.mockResolvedValue({
      days: 30, totalSearches: 200, zeroResultSearches: 10, zeroResultRate: 0.05, semanticUsedRate: 0.6, averageTookMs: 40, p95TookMs: 120,
      topQueries: [{ query: 'invoice', count: 25 }], topZeroResultQueries: [{ query: 'payroll export', count: 4 }],
    })
  })

  it('shows the index state and the settings in use', async () => {
    renderWithProviders(<AdminSearchPage />)
    expect(await screen.findByText('46')).toBeInTheDocument()
    expect(screen.getByText('75 / 80')).toBeInTheDocument()
    expect(screen.getByText('5 waiting for the model')).toBeInTheDocument()
    expect(screen.getByText(/test model \(stub\)/)).toBeInTheDocument()
    expect(screen.getByText('Typo match threshold (title similarity)')).toBeInTheDocument()
  })

  it('rebuilds after confirmation', async () => {
    renderWithProviders(<AdminSearchPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Rebuild index' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Rebuild index' }))
    expect(api.rebuild).toHaveBeenCalledWith(false)
    expect(await screen.findByText(/The rebuild has started/)).toBeInTheDocument()
  })

  it('warns when the index tables are not installed', async () => {
    api.status.mockResolvedValue({ ...status, engine: 'BASIC', keywordIndexInstalled: false, semanticInstalled: false })
    renderWithProviders(<AdminSearchPage />)
    expect(await screen.findByText(/migration V020/)).toBeInTheDocument()
  })

  it('adds and deletes synonym groups', async () => {
    renderWithProviders(<AdminSearchPage />)
    const user = userEvent.setup()
    await user.click(screen.getByRole('tab', { name: 'Synonyms' }))
    expect(await screen.findByText('factura')).toBeInTheDocument()
    await user.type(screen.getByRole('textbox', { name: 'Terms' }), 'receipt')
    await user.click(screen.getByRole('button', { name: 'Add group' }))
    expect(await screen.findByText(/at least two different terms/)).toBeInTheDocument()
    await user.type(screen.getByRole('textbox', { name: 'Terms' }), ', voucher')
    await user.click(screen.getByRole('button', { name: 'Add group' }))
    expect(api.createSynonym).toHaveBeenCalledWith(['receipt', 'voucher'])
    await user.click(screen.getByRole('button', { name: 'Delete synonym group invoice, bill, factura' }))
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Delete' }))
    expect(api.deleteSynonym).toHaveBeenCalledWith(7)
  })

  it('shows search insights for the chosen period', async () => {
    renderWithProviders(<AdminSearchPage />)
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Insights' }))
    expect(await screen.findByText('5%')).toBeInTheDocument()
    expect(screen.getByText('payroll export')).toBeInTheDocument()
    expect(api.insights).toHaveBeenCalledWith(30)
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<AdminSearchPage />)
    await screen.findByText('75 / 80')
    expect(await axe(container)).toHaveNoViolations()
  })
})
