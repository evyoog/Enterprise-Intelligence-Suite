import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { ApiKeysSection } from './ApiKeysSection'

const list = vi.fn()
const revoke = vi.fn()

vi.mock('../../api/apiKeysApi', () => ({
  API_KEY_STATUS_COLOR: { ACTIVE: 'success', REVOKED: 'default', EXPIRED: 'warning' },
  apiKeysApi: { list: () => list(), revoke: (id: number) => revoke(id) },
  adminApiKeysApi: { list: vi.fn() },
}))

const active = {
  id: 3, name: 'CRM sync', prefix: 'eis_ab12cd34', status: 'ACTIVE' as const, scope: 'READ_WRITE' as const, personal: true, createdAt: '2026-10-03T09:00:00Z',
  expiresAt: null, lastUsedAt: null, requestCount: 0,
}

describe('ApiKeysSection', () => {
  beforeEach(() => {
    for (const m of [list, revoke]) m.mockReset()
    list.mockResolvedValue([active])
  })

  it('lists keys by prefix only', async () => {
    renderWithProviders(<ApiKeysSection />)
    expect(await screen.findByText('eis_ab12cd34_…')).toBeInTheDocument()
    expect(screen.getByText('Active')).toBeInTheDocument()
  })

  it('shows the legacy scope and no create button', async () => {
    renderWithProviders(<ApiKeysSection />)
    await screen.findByText('eis_ab12cd34_…')
    expect(screen.getByText('Read-write')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Create key' })).not.toBeInTheDocument()
  })

  it('renders nothing when there are no personal keys', async () => {
    list.mockResolvedValue([])
    const { container } = renderWithProviders(<ApiKeysSection />)
    await waitFor(() => expect(list).toHaveBeenCalled())
    expect(container.querySelector('section')).toBeNull()
  })

  it('revokes a key after confirmation', async () => {
    const user = userEvent.setup()
    revoke.mockResolvedValue({ ...active, status: 'REVOKED' })
    renderWithProviders(<ApiKeysSection />)
    await user.click(await screen.findByRole('button', { name: 'Revoke CRM sync' }))
    expect(screen.getByRole('dialog', { name: 'Revoke CRM sync?' })).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Revoke' }))
    expect(revoke).toHaveBeenCalledWith(3)
    expect(await screen.findByText('API key revoked.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<ApiKeysSection />)
    await screen.findByText('eis_ab12cd34_…')
    expect(await axe(container)).toHaveNoViolations()
  })
})
