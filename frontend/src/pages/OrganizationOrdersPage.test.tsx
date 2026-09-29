import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { renderWithProviders } from '../test/renderWithProviders'
import { OrganizationOrdersPage } from './OrganizationOrdersPage'

const submit = vi.fn()
const myOrders = vi.fn()
const pendingOrders = vi.fn()
const approve = vi.fn()
const reject = vi.fn()
const cancel = vi.fn()

vi.mock('../api/ordersApi', async () => {
  const actual = await vi.importActual<typeof import('../api/ordersApi')>('../api/ordersApi')
  return {
    ...actual,
    ordersApi: {
      submit: (productId: number, planId: number | null) => submit(productId, planId),
      myOrders: () => myOrders(),
      pendingOrders: () => pendingOrders(),
      approve: (id: number, note?: string) => approve(id, note),
      reject: (id: number, note?: string) => reject(id, note),
      cancel: (id: number) => cancel(id),
    },
  }
})

const productsList = vi.fn()
vi.mock('../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../api/productsApi')>('../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, list: () => productsList() } }
})

const product = { id: 10, name: 'Valam.ai', price: 0, status: 'ACTIVE', ssoConnected: false, platforms: [], plans: [], version: 1, dependsOnProductIds: [] }
const submittedOrder = {
  id: 1, productId: 10, productName: 'Valam.ai', status: 'SUBMITTED' as const,
  requestedByCustomerId: 5, requestedByName: 'Jane Member', createdAt: '2027-01-05T00:00:00Z',
}

describe('OrganizationOrdersPage', () => {
  beforeEach(() => {
    for (const m of [submit, myOrders, pendingOrders, approve, reject, cancel, productsList]) m.mockReset()
    productsList.mockResolvedValue([product])
  })

  it('submits a new order request', async () => {
    myOrders.mockResolvedValue([])
    pendingOrders.mockRejectedValue(new ApiError(403, 'Forbidden'))
    submit.mockResolvedValue({ ...submittedOrder })
    renderWithProviders(<OrganizationOrdersPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByLabelText('Product'))
    await user.click(await screen.findByRole('option', { name: 'Valam.ai' }))
    await user.click(screen.getByRole('button', { name: 'Submit request' }))
    expect(submit).toHaveBeenCalledWith(10, null)
  })

  it('shows the pending-approvals section for an org admin and approves an order', async () => {
    myOrders.mockResolvedValue([])
    pendingOrders.mockResolvedValue([submittedOrder])
    approve.mockResolvedValue({ ...submittedOrder, status: 'APPROVED' })
    renderWithProviders(<OrganizationOrdersPage />)

    await screen.findByText('Requested by Jane Member')
    await userEvent.setup().click(screen.getByRole('button', { name: 'Approve' }))
    expect(approve).toHaveBeenCalledWith(1, undefined)
  })

  it('hides the pending-approvals section for a plain member', async () => {
    myOrders.mockResolvedValue([submittedOrder])
    pendingOrders.mockRejectedValue(new ApiError(403, 'Forbidden'))
    renderWithProviders(<OrganizationOrdersPage />)

    await screen.findByText('Valam.ai')
    expect(screen.queryByText('Pending approvals')).not.toBeInTheDocument()
  })

  it('lets the requester cancel their own submitted order', async () => {
    myOrders.mockResolvedValue([submittedOrder])
    pendingOrders.mockRejectedValue(new ApiError(403, 'Forbidden'))
    cancel.mockResolvedValue({ ...submittedOrder, status: 'CANCELLED' })
    renderWithProviders(<OrganizationOrdersPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Cancel request' }))
    expect(cancel).toHaveBeenCalledWith(1)
  })

  it('has no detectable a11y violations', async () => {
    myOrders.mockResolvedValue([submittedOrder])
    pendingOrders.mockResolvedValue([submittedOrder])
    const { container } = renderWithProviders(<OrganizationOrdersPage />)
    await screen.findAllByText('Valam.ai')
    expect(await axe(container)).toHaveNoViolations()
  })
})
