import '../../i18n'
import { screen } from '@testing-library/react'
import { axe } from 'jest-axe'
import { describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminApiKeysPage } from './AdminApiKeysPage'

vi.mock('../../api/apiKeysApi', () => ({
  API_KEY_STATUS_COLOR: { ACTIVE: 'success', REVOKED: 'default', EXPIRED: 'warning' },
  apiKeysApi: {},
  adminApiKeysApi: {
    list: () => Promise.resolve({
      items: [{ id: 1, ownerCustomerId: 5, ownerEmail: 'jane@example.com', name: 'CRM sync', prefix: 'eis_ab12cd34', status: 'ACTIVE',
        createdAt: '2026-10-01T00:00:00Z', expiresAt: null, lastUsedAt: '2026-10-03T09:00:00Z', requestCount: 1234 }],
      totalElements: 1, page: 0, size: 20,
    }),
  },
}))

describe('AdminApiKeysPage', () => {
  it('shows each key with its owner and usage', async () => {
    const { container } = renderWithProviders(<AdminApiKeysPage />)
    expect(await screen.findByText('jane@example.com')).toBeInTheDocument()
    expect(screen.getByText('eis_ab12cd34_…')).toBeInTheDocument()
    expect(screen.getByText((1234).toLocaleString())).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })
})
