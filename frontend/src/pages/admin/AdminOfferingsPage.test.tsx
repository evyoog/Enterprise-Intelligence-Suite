import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminOfferingsPage } from './AdminOfferingsPage'

const list = vi.fn()
const create = vi.fn()
const update = vi.fn()
const remove = vi.fn()
const rules = vi.fn()
const setRules = vi.fn()
vi.mock('../../api/offeringsApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/offeringsApi')>('../../api/offeringsApi')
  return {
    ...actual,
    adminOfferingsApi: {
      list: () => list(), create: (i: unknown) => create(i), update: (id: number, i: unknown) => update(id, i),
      remove: (id: number) => remove(id), rules: () => rules(), setRules: (id: number, i: unknown) => setRules(id, i),
    },
  }
})

const listAdmin = vi.fn()
vi.mock('../../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/productsApi')>('../../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, listAdmin: () => listAdmin() } }
})

const product = (id: number, name: string, status = 'ACTIVE') => ({
  id, name, price: 0, status, ssoConnected: false, featured: false, platforms: [], plans: [], version: 1, dependsOnProductIds: [],
})

const draftOffering = {
  id: 1, name: 'Starter pack', description: 'Two apps', status: 'DRAFT' as const,
  products: [{ id: 1, name: 'Valam.ai', status: 'ACTIVE' }], createdAt: '2026-10-08T00:00:00Z', updatedAt: '2026-10-08T00:00:00Z',
}

describe('AdminOfferingsPage', () => {
  beforeEach(() => {
    for (const m of [list, create, update, remove, rules, setRules, listAdmin]) m.mockReset()
    list.mockResolvedValue([draftOffering])
    rules.mockResolvedValue([
      { productId: 1, productName: 'Valam.ai', productStatus: 'ACTIVE', audience: 'BOTH', worksWithProductIds: [] },
      { productId: 2, productName: 'Varthan.ai', productStatus: 'ACTIVE', audience: 'ORGANIZATION', worksWithProductIds: [1] },
    ])
    listAdmin.mockResolvedValue([product(1, 'Valam.ai'), product(2, 'Varthan.ai')])
  })

  it('states that an offering has no price of its own', async () => {
    renderWithProviders(<AdminOfferingsPage />)
    expect(await screen.findByText(/no price of its own/)).toBeInTheDocument()
    expect(await screen.findByText('Starter pack')).toBeInTheDocument()
  })

  it('creates an offering from a name and chosen products', async () => {
    create.mockResolvedValue(draftOffering)
    renderWithProviders(<AdminOfferingsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'New offering' }))
    const dialog = await screen.findByRole('dialog')
    const save = within(dialog).getByRole('button', { name: 'Save' })
    expect(save).toBeDisabled()
    await user.type(within(dialog).getByRole('textbox', { name: /Name/ }), 'Bundle')
    await user.click(within(dialog).getByRole('checkbox', { name: 'Varthan.ai' }))
    await user.click(save)
    expect(create).toHaveBeenCalledWith({ name: 'Bundle', description: undefined, status: 'DRAFT', productIds: [2] })
  })

  it('shows the server message when saving fails', async () => {
    const { ApiError } = await import('../../api/client')
    update.mockRejectedValue(new ApiError(409, 'An offering can be published only when at least one of its products is active.'))
    renderWithProviders(<AdminOfferingsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit Starter pack' }))
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Save' }))
    expect(await screen.findByText(/published only when/)).toBeInTheDocument()
  })

  it('deletes a draft offering after confirming', async () => {
    remove.mockResolvedValue(undefined)
    renderWithProviders(<AdminOfferingsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Delete Starter pack' }))
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Delete' }))
    expect(remove).toHaveBeenCalledWith(1)
  })

  it('does not offer delete for a published offering', async () => {
    list.mockResolvedValue([{ ...draftOffering, status: 'ACTIVE' }])
    renderWithProviders(<AdminOfferingsPage />)
    await screen.findByText('Starter pack')
    expect(screen.queryByRole('button', { name: 'Delete Starter pack' })).not.toBeInTheDocument()
  })

  it('saves a product rule: audience and works-with', async () => {
    setRules.mockResolvedValue({})
    renderWithProviders(<AdminOfferingsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('tab', { name: 'Product rules' }))
    expect(await screen.findByText('Organizations only')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Edit rules for Valam.ai' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('radio', { name: 'Individuals only' }))
    await user.click(within(dialog).getByRole('checkbox', { name: 'Varthan.ai' }))
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(setRules).toHaveBeenCalledWith(1, { audience: 'INDIVIDUAL', worksWithProductIds: [2] })
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<AdminOfferingsPage />)
    await screen.findByText('Starter pack')
    expect(await axe(container)).toHaveNoViolations()
  })
})
