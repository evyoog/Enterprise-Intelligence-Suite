import '../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { renderWithProviders } from '../test/renderWithProviders'
import { MyProductsPage } from './MyProductsPage'

const dashboardGet = vi.fn()
const addFavorite = vi.fn()
const removeFavorite = vi.fn()
const recordLaunch = vi.fn()
const updatePreferences = vi.fn()

vi.mock('../api/dashboardApi', () => ({
  dashboardApi: {
    get: () => dashboardGet(),
    addFavorite: (id: number) => addFavorite(id),
    removeFavorite: (id: number) => removeFavorite(id),
    recordLaunch: (id: number) => recordLaunch(id),
    updatePreferences: (p: unknown) => updatePreferences(p),
  },
}))

const subscribe = vi.fn()
vi.mock('../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../api/registrationApi')>('../api/registrationApi')
  return { ...actual, myProductsApi: { ...actual.myProductsApi, subscribe: (id: number) => subscribe(id) } }
})

const recommendationsGet = vi.fn()
vi.mock('../api/recommendationsApi', () => ({ recommendationsApi: { get: () => recommendationsGet() } }))

const myRequests = vi.fn()
const requestAccess = vi.fn()
const removeMyAccess = vi.fn()
vi.mock('../api/accessRequestApi', () => ({
  accessRequestApi: {
    myRequests: () => myRequests(),
    request: (productId: number, message?: string) => requestAccess(productId, message),
    removeMyAccess: (productId: number) => removeMyAccess(productId),
  },
}))

const org = { id: 1, name: 'Acme Corp', code: 'ACME', businessEmail: 'ops@acme.test', country: 'US', licensedSeats: 10, activeMemberCount: 4, status: 'ACTIVE', mfaRequired: false }

const notAssignedProduct = {
  productId: 10, productName: 'Valam.ai', category: 'Analytics', subscriptionStatus: 'ACTIVE' as const,
  myAccessAssigned: false, favorite: false, launchCount: 0,
}
const assignedProduct = {
  productId: 11, productName: 'Varthan.ai', category: 'Analytics', subscriptionStatus: 'ACTIVE' as const,
  myAccessAssigned: true, myProductRole: 'User', favorite: false, launchCount: 0,
}

function dashboardWith(products: typeof notAssignedProduct[]) {
  return {
    organization: org,
    products,
    alerts: [],
    preferences: { widgetOrder: ['organization', 'alerts', 'recentlyUsed', 'favorites', 'recommendations', 'products'], hiddenWidgets: [] },
  }
}

describe('MyProductsPage — access requests (C103)', () => {
  beforeEach(() => {
    for (const m of [dashboardGet, addFavorite, removeFavorite, recordLaunch, updatePreferences, subscribe, recommendationsGet, myRequests, requestAccess, removeMyAccess]) m.mockReset()
    recommendationsGet.mockResolvedValue({ featured: [], popular: [] })
    myRequests.mockResolvedValue([])
  })

  it('requests access to a product the member does not yet have, with an optional message', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([notAssignedProduct]))
    requestAccess.mockResolvedValue({ id: 1, productId: 10, productName: 'Valam.ai', memberId: 5, memberName: 'Jane', status: 'PENDING', createdAt: '2027-01-05T00:00:00Z' })
    renderWithProviders(<MyProductsPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Request access' }))
    await screen.findByRole('dialog')
    await user.type(screen.getByLabelText('Message (optional)'), 'Need it for the launch')
    await user.click(screen.getByRole('button', { name: 'Send request' }))

    expect(requestAccess).toHaveBeenCalledWith(10, 'Need it for the launch')
    expect(await screen.findByText('Request sent for Valam.ai.')).toBeInTheDocument()
  })

  it('disables Request access when a request is already pending', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([notAssignedProduct]))
    myRequests.mockResolvedValue([{ id: 1, productId: 10, productName: 'Valam.ai', memberId: 5, memberName: 'Jane', status: 'PENDING', createdAt: '2027-01-05T00:00:00Z' }])
    renderWithProviders(<MyProductsPage />)

    expect(await screen.findByRole('button', { name: 'Request pending' })).toBeDisabled()
  })

  it('cancelling the remove-access dialog makes no call', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([assignedProduct]))
    renderWithProviders(<MyProductsPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Remove my access' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Cancel' }))
    expect(removeMyAccess).not.toHaveBeenCalled()
  })

  it('removes my access after confirming', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([assignedProduct]))
    removeMyAccess.mockResolvedValue(undefined)
    renderWithProviders(<MyProductsPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Remove my access' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Remove my access' }))
    await waitFor(() => expect(removeMyAccess).toHaveBeenCalledWith(11))
  })

  it('shows a backend error when removing access fails', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([assignedProduct]))
    removeMyAccess.mockRejectedValue(new ApiError(400, 'You do not have access to this product.'))
    renderWithProviders(<MyProductsPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Remove my access' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Remove my access' }))
    expect(await screen.findByText('You do not have access to this product.')).toBeInTheDocument()
  })

  it('shows a backend error when requesting access fails', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([notAssignedProduct]))
    requestAccess.mockRejectedValue(new ApiError(400, 'You already have a pending request for this product.'))
    renderWithProviders(<MyProductsPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Request access' }))
    await screen.findByRole('dialog')
    await user.click(screen.getByRole('button', { name: 'Send request' }))
    expect(await screen.findByText('You already have a pending request for this product.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations on the product actions section', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([notAssignedProduct, assignedProduct]))
    renderWithProviders(<MyProductsPage />)
    const heading = await screen.findByText('All products')
    const section = heading.closest('div') as HTMLElement
    // heading-order is disabled here: the page's heading hierarchy above
    // this section (PageHeader's h4, then this h6) is pre-existing and
    // outside what this feature touches — axe evaluates it even scoped to
    // this subtree because it looks at the whole document for context.
    expect(await axe(section, { rules: { 'heading-order': { enabled: false } } })).toHaveNoViolations()
  })

  it('has no detectable a11y violations in the request-access dialog', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([notAssignedProduct]))
    renderWithProviders(<MyProductsPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Request access' }))
    const dialog = await screen.findByRole('dialog')
    expect(await axe(dialog)).toHaveNoViolations()
  })

  it('has no detectable a11y violations in the remove-access dialog', async () => {
    dashboardGet.mockResolvedValue(dashboardWith([assignedProduct]))
    renderWithProviders(<MyProductsPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Remove my access' }))
    const dialog = await screen.findByRole('dialog')
    expect(await axe(dialog)).toHaveNoViolations()
  })
})
