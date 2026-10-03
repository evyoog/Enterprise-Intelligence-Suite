import '../../i18n'
import { screen } from '@testing-library/react'
import { axe } from 'jest-axe'
import { describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { PlatformsListPage } from './PlatformsListPage'

const platforms = [
  { id: 2, name: 'Thittam', description: 'Planning.', primaryColor: null, status: 'ACTIVE', showInCatalog: false, displayOrder: 2 },
  { id: 1, name: 'Thiran', description: 'Operations.', primaryColor: '#7C3AED', status: 'ACTIVE', showInCatalog: true, displayOrder: 1 },
]
const apps = [{ id: 9, name: 'Insights', platforms: [{ id: 1, name: 'Thiran' }] }]
vi.mock('../../api/platformsApi', () => ({ platformsApi: { list: () => Promise.resolve(platforms) } }))
vi.mock('../../api/productsApi', () => ({ productsApi: { listAdmin: () => Promise.resolve(apps) } }))

describe('PlatformsListPage (C66)', () => {
  it('lists platforms in display order with real app counts and catalog visibility', async () => {
    const { container } = renderWithProviders(<PlatformsListPage />)
    const headings = await screen.findAllByRole('heading', { level: 3 })
    expect(headings.map((h) => h.textContent)).toEqual(['Thiran', 'Thittam'])
    expect(screen.getByText('1 app')).toBeInTheDocument()
    expect(screen.getByText('0 apps')).toBeInTheDocument()
    expect(screen.getByText('In catalog')).toBeInTheDocument()
    expect(screen.getByText('Hidden from catalog')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Manage apps: Thiran' })).toHaveAttribute('href', '/admin/platforms/1')
    expect(screen.getByRole('link', { name: 'Edit Thittam' })).toHaveAttribute('href', '/admin/platforms/2/edit')
    expect(await axe(container)).toHaveNoViolations()
  })
})
