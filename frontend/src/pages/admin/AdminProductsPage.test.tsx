import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminProductsPage } from './AdminProductsPage'

const listAdmin = vi.fn()
const create = vi.fn()
const remove = vi.fn()
vi.mock('../../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/productsApi')>('../../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, listAdmin: () => listAdmin(), create: (r: unknown) => create(r), delete: (id: number) => remove(id) } }
})

const listPlatforms = vi.fn()
vi.mock('../../api/platformsApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/platformsApi')>('../../api/platformsApi')
  return { ...actual, platformsApi: { ...actual.platformsApi, list: () => listPlatforms() } }
})

const existingProduct = {
  id: 1, name: 'Valam.ai', price: 0, status: 'ACTIVE' as const, ssoConnected: false, featured: false,
  platforms: [], plans: [], version: 1, dependsOnProductIds: [],
}

describe('AdminProductsPage', () => {
  beforeEach(() => {
    listAdmin.mockReset()
    create.mockReset()
    listPlatforms.mockReset()
    listPlatforms.mockResolvedValue([])
  })

  it('creates a new app from the Add App dialog and refreshes the grid', { timeout: 30000 }, async () => {
    let catalog = [existingProduct]
    listAdmin.mockImplementation(() => Promise.resolve(catalog))
    create.mockImplementation((request: { name: string }) => {
      const created = { ...existingProduct, id: 2, name: request.name }
      catalog = [...catalog, created]
      return Promise.resolve(created)
    })
    renderWithProviders(<AdminProductsPage />)

    await screen.findByText('Valam.ai')
    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: 'Add App' }))

    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByLabelText(/App name/), 'Varthan.ai')
    await user.type(within(dialog).getByLabelText(/Price/), '9.99')
    await user.click(within(dialog).getByRole('button', { name: 'Add App' }))

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(create).toHaveBeenCalledWith(expect.objectContaining({ name: 'Varthan.ai', price: 9.99 }))
    expect(await screen.findByText('Varthan.ai')).toBeInTheDocument()
  })

  it('opens with no dialog visible and shows the existing catalog', async () => {
    listAdmin.mockResolvedValue([existingProduct])
    renderWithProviders(<AdminProductsPage />)

    expect(await screen.findByText('Valam.ai')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('filters by status, platform and search, and confirms before deleting', { timeout: 30000 }, async () => {
    const thiran = { id: 1, name: 'Thiran', primaryColor: '#7C3AED' }
    const insights = { ...existingProduct, id: 2, name: 'Insights', category: 'Analytics', featured: true, ssoConnected: true, platforms: [thiran] }
    const legacy = { ...existingProduct, id: 3, name: 'Legacy', status: 'RETIRED' as const }
    listAdmin.mockResolvedValue([existingProduct, insights, legacy])
    remove.mockResolvedValue(undefined)
    const user = userEvent.setup()
    const { container } = renderWithProviders(<AdminProductsPage />)

    const table = await screen.findByRole('table', { name: 'All Apps' })
    expect(within(table).getAllByRole('row')).toHaveLength(4)
    expect(screen.getByRole('button', { name: 'Retired (1)' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Featured (1)' }))
    expect(within(table).getAllByRole('row')).toHaveLength(2)
    expect(within(table).getByText('Insights')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'All (3)' }))

    await user.type(screen.getByRole('textbox', { name: 'Search apps' }), 'zzz')
    expect(await screen.findByText('No apps match these filters')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Clear filters' }))

    expect(await axe(container)).toHaveNoViolations()

    await user.click(await screen.findByRole('button', { name: 'Delete Legacy' }))
    const dialog = await screen.findByRole('dialog', { name: 'Delete Legacy?' })
    expect(remove).not.toHaveBeenCalled()
    await user.click(within(dialog).getByRole('button', { name: 'Delete app' }))
    await waitFor(() => expect(remove).toHaveBeenCalledWith(3))
    await waitFor(() => expect(screen.queryByText('Legacy')).not.toBeInTheDocument())
  })

  it('shows an empty state when there are no apps', async () => {
    listAdmin.mockResolvedValue([])
    renderWithProviders(<AdminProductsPage />)
    expect(await screen.findByText('No apps yet')).toBeInTheDocument()
  })
})
