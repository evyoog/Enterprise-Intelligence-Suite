import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { renderWithProviders } from '../test/renderWithProviders'
import { MySubscriptionsPage } from './MySubscriptionsPage'

const listSubscriptions = vi.fn()
const suspend = vi.fn()
const reactivate = vi.fn()
const cancel = vi.fn()
const renew = vi.fn()
const changePlan = vi.fn()

vi.mock('../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../api/registrationApi')>('../api/registrationApi')
  return {
    ...actual,
    myProductsApi: {
      listSubscriptions: () => listSubscriptions(),
      suspend: (id: number) => suspend(id),
      reactivate: (id: number) => reactivate(id),
      cancel: (id: number) => cancel(id),
      renew: (id: number) => renew(id),
      changePlan: (id: number, planId: number | null) => changePlan(id, planId),
    },
    organizationApi: {
      ...actual.organizationApi,
      listOrganizationSubscriptions: () => Promise.reject(new ApiError(403, 'Forbidden')),
    },
  }
})

const myRenewals = vi.fn()
vi.mock('../api/renewalsApi', () => ({ renewalsApi: { myRenewals: () => myRenewals() }, adminRenewalsApi: {} }))

const productGet = vi.fn()
vi.mock('../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../api/productsApi')>('../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, get: (id: number) => productGet(id) } }
})

const activeSub = {
  id: 1, productId: 10, productName: 'Valam.ai', status: 'ACTIVE' as const,
  planId: undefined, planName: undefined, expiresAt: undefined,
}
const suspendedSub = { ...activeSub, id: 2, status: 'SUSPENDED' as const }

describe('MySubscriptionsPage', () => {
  beforeEach(() => {
    for (const m of [listSubscriptions, suspend, reactivate, cancel, renew, changePlan, productGet, myRenewals]) m.mockReset()
    myRenewals.mockResolvedValue([])
  })

  it('shows auto-renew, the renewal date and the next reminder (REQ-SUB-004)', async () => {
    listSubscriptions.mockResolvedValue([activeSub])
    myRenewals.mockResolvedValue([{ subscriptionId: 1, productName: 'Valam.ai', planName: null, autoRenew: true,
      renewalDate: '2026-11-02T10:00:00Z', remindersEnabled: false, nextReminderAt: null }])
    renderWithProviders(<MySubscriptionsPage />)
    expect(await screen.findByText('Auto-renew on')).toBeInTheDocument()
    expect(screen.getByText(/Renews on/)).toBeInTheDocument()
    expect(screen.getByText(/Reminders are off/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Change reminder settings' })).toHaveAttribute('href', '/account/preferences')
  })

  it('suspends an active subscription', async () => {
    listSubscriptions.mockResolvedValue([activeSub])
    suspend.mockResolvedValue({ ...activeSub, status: 'SUSPENDED' })
    renderWithProviders(<MySubscriptionsPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Suspend' }))
    expect(suspend).toHaveBeenCalledWith(1)
    expect(await screen.findByText('Suspended')).toBeInTheDocument()
  })

  it('reactivates a suspended subscription', async () => {
    listSubscriptions.mockResolvedValue([suspendedSub])
    reactivate.mockResolvedValue({ ...suspendedSub, status: 'ACTIVE' })
    renderWithProviders(<MySubscriptionsPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Reactivate' }))
    expect(reactivate).toHaveBeenCalledWith(2)
    expect(await screen.findByText('Active')).toBeInTheDocument()
  })

  it('opens the change plan dialog and saves a new plan', async () => {
    listSubscriptions.mockResolvedValue([activeSub])
    productGet.mockResolvedValue({ id: 10, plans: [{ id: 99, name: 'Pro', price: 10, billingPeriod: 'MONTHLY', currency: 'USD' }] })
    changePlan.mockResolvedValue({ ...activeSub, planId: 99, planName: 'Pro' })
    renderWithProviders(<MySubscriptionsPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Change plan' }))
    await screen.findByRole('dialog')
    await user.click(await screen.findByRole('button', { name: 'Save' }))
    expect(changePlan).toHaveBeenCalledWith(1, null)
  })

  it('shows the backend message when an action is refused', async () => {
    listSubscriptions.mockResolvedValue([suspendedSub])
    reactivate.mockRejectedValue(new ApiError(400, 'Only a suspended subscription can be reactivated.'))
    renderWithProviders(<MySubscriptionsPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Reactivate' }))
    expect(await screen.findByText('Only a suspended subscription can be reactivated.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    listSubscriptions.mockResolvedValue([activeSub, suspendedSub])
    const { container } = renderWithProviders(<MySubscriptionsPage />)
    await screen.findAllByText('Valam.ai')
    expect(await axe(container)).toHaveNoViolations()
  })
})
