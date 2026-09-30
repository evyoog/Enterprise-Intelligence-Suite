import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminProductsPage } from './AdminProductsPage'

const listAdmin = vi.fn()
const create = vi.fn()
vi.mock('../../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/productsApi')>('../../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, listAdmin: () => listAdmin(), create: (r: unknown) => create(r) } }
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

  it('creates a new app from the Add App dialog and refreshes the grid', async () => {
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
})
