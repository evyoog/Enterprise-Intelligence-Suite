import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { ApiKeysSection } from './ApiKeysSection'

const list = vi.fn()
const create = vi.fn()
const revoke = vi.fn()

vi.mock('../../api/apiKeysApi', () => ({
  apiKeysApi: { list: () => list(), create: (b: unknown) => create(b), revoke: (id: number) => revoke(id) },
  adminApiKeysApi: { list: vi.fn() },
}))

const active = {
  id: 3, name: 'CRM sync', prefix: 'eis_ab12cd34', status: 'ACTIVE' as const, createdAt: '2026-10-03T09:00:00Z',
  expiresAt: null, lastUsedAt: null, requestCount: 0,
}

describe('ApiKeysSection', () => {
  beforeEach(() => {
    for (const m of [list, create, revoke]) m.mockReset()
    list.mockResolvedValue([active])
  })

  it('lists keys by prefix only', async () => {
    renderWithProviders(<ApiKeysSection />)
    expect(await screen.findByText('eis_ab12cd34_…')).toBeInTheDocument()
    expect(screen.getByText('Active')).toBeInTheDocument()
  })

  it('creates a key and shows the full key once', async () => {
    const user = userEvent.setup()
    create.mockResolvedValue({ ...active, id: 4, name: 'Reports', key: 'eis_ab12cd34_' + 'Q'.repeat(40) })
    renderWithProviders(<ApiKeysSection />)
    await user.click(await screen.findByRole('button', { name: 'Create key' }))
    await user.click(screen.getByRole('button', { name: 'Create' }))
    expect(screen.getByText('Enter a name for the key.')).toBeInTheDocument()
    await user.type(screen.getByLabelText(/Name/), 'Reports')
    await user.click(screen.getByRole('button', { name: 'Create' }))
    await waitFor(() => expect(create).toHaveBeenCalledWith({ name: 'Reports', expiresAt: null }))
    expect(await screen.findByDisplayValue('eis_ab12cd34_' + 'Q'.repeat(40))).toBeInTheDocument()
    expect(screen.getByText("Copy this key now. You won't be able to see it again.")).toBeInTheDocument()
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
