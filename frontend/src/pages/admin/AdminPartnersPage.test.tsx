import '../../i18n'
import { screen } from '@testing-library/react'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminPartnersPage } from './AdminPartnersPage'

const listAll = vi.fn()
vi.mock('../../api/partnersApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/partnersApi')>('../../api/partnersApi')
  return { ...actual, adminPartnersApi: { ...actual.adminPartnersApi, listAll: () => listAll() } }
})

const provider = {
  id: 1, name: 'Acme Cloud', contactName: 'Jane Doe', contactEmail: 'jane@acme.example',
  status: 'REGISTERED' as const, createdAt: '2027-04-01T00:00:00Z',
}

describe('AdminPartnersPage', () => {
  beforeEach(() => {
    listAll.mockReset()
  })

  it('lists providers with their status', async () => {
    listAll.mockResolvedValue([provider])
    renderWithProviders(<AdminPartnersPage />)
    expect(await screen.findByText('Acme Cloud')).toBeInTheDocument()
    expect(screen.getByText('Registered')).toBeInTheDocument()
  })

  it('shows an empty state when there are no providers', async () => {
    listAll.mockResolvedValue([])
    renderWithProviders(<AdminPartnersPage />)
    expect(await screen.findByText('No provider applications yet.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    listAll.mockResolvedValue([provider])
    const { container } = renderWithProviders(<AdminPartnersPage />)
    await screen.findByText('Acme Cloud')
    expect(await axe(container)).toHaveNoViolations()
  })
})
