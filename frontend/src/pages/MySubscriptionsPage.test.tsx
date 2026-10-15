import '../i18n'
import { screen, waitFor, within } from '@testing-library/react'
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
const cancelScheduledPlanChange = vi.fn()
const setAutoRenew = vi.fn()
const entitlementsMine = vi.fn()

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
      cancelScheduledPlanChange: (id: number) => cancelScheduledPlanChange(id),
      setAutoRenew: (id: number, v: boolean) => setAutoRenew(id, v),
    },
    entitlementsApi: { ...actual.entitlementsApi, mine: () => entitlementsMine() },
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
    for (const m of [listSubscriptions, suspend, reactivate, cancel, renew, changePlan, productGet, myRenewals, cancelScheduledPlanChange, entitlementsMine, setAutoRenew]) m.mockReset()
    myRenewals.mockResolvedValue([])
    entitlementsMine.mockResolvedValue([])
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

  // C102 (REQ-MKT-005): a free trial shows its end, cannot be renewed or changed, and offers Subscribe.
  describe('free trial', () => {
    const daysFromNow = (days: number) => new Date(Date.now() + days * 86_400_000 - 3_600_000).toISOString()
    const trialSub = { ...activeSub, id: 3, productId: 10, planName: 'Enterprise', trial: true, autoRenew: false, expiresAt: daysFromNow(10) }

    it('shows the Free trial chip with the end date and days left, and no renew, auto-renew or change-plan controls', async () => {
      listSubscriptions.mockResolvedValue([trialSub])
      myRenewals.mockResolvedValue([{ subscriptionId: 3, productName: 'Valam.ai', planName: null, autoRenew: false,
        renewalDate: trialSub.expiresAt, remindersEnabled: true, nextReminderAt: null }])
      renderWithProviders(<MySubscriptionsPage />)
      expect(await screen.findByText('Free trial')).toBeInTheDocument()
      expect(screen.getByText(/^Ends .* \(10 days left\)$/)).toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Renew' })).not.toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Change plan' })).not.toBeInTheDocument()
      expect(screen.queryByRole('switch')).not.toBeInTheDocument()
      expect(screen.queryByText(/Renews on/)).not.toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Suspend' })).toBeInTheDocument()
    })

    it('Subscribe goes to the product page so the customer can buy', async () => {
      listSubscriptions.mockResolvedValue([trialSub])
      renderWithProviders(<MySubscriptionsPage />)
      expect(await screen.findByRole('link', { name: 'Subscribe to Valam.ai' })).toHaveAttribute('href', '/products/10')
    })

    it('says "1 day left" in the singular, and offers Subscribe (not Renew) for an ended trial', async () => {
      listSubscriptions.mockResolvedValue([
        { ...trialSub, expiresAt: daysFromNow(0.5) },
        { ...trialSub, id: 4, productName: 'Varthan.ai', productId: 11, status: 'EXPIRED' as const, expiresAt: '2026-01-02T10:00:00Z' },
      ])
      renderWithProviders(<MySubscriptionsPage />)
      expect(await screen.findByText(/\(1 day left\)/)).toBeInTheDocument()
      expect(screen.getByText(/^Ended /)).toBeInTheDocument()
      expect(screen.getByRole('link', { name: 'Subscribe to Varthan.ai' })).toHaveAttribute('href', '/products/11')
      expect(screen.queryByRole('button', { name: 'Renew' })).not.toBeInTheDocument()
    })

    it('has no detectable a11y violations', async () => {
      listSubscriptions.mockResolvedValue([trialSub])
      const { container } = renderWithProviders(<MySubscriptionsPage />)
      await screen.findByText('Free trial')
      expect(await axe(container)).toHaveNoViolations()
    })
  })

  it('has no detectable a11y violations', async () => {
    listSubscriptions.mockResolvedValue([activeSub, suspendedSub])
    const { container } = renderWithProviders(<MySubscriptionsPage />)
    await screen.findAllByText('Valam.ai')
    expect(await axe(container)).toHaveNoViolations()
  })

  it('shows a downgrade scheduled for the next renewal and lets the customer keep the current plan (C97 SC-1)', async () => {
    const scheduled = { ...activeSub, planId: 1, planName: 'Pro', expiresAt: '2026-11-02T10:00:00Z', pendingPlanId: 2, pendingPlanName: 'Basic' }
    listSubscriptions.mockResolvedValue([scheduled])
    cancelScheduledPlanChange.mockResolvedValue({ ...scheduled, pendingPlanId: undefined, pendingPlanName: undefined })
    renderWithProviders(<MySubscriptionsPage />)

    expect(await screen.findByText(/Your plan changes to Basic on/)).toBeInTheDocument()
    expect(screen.getByText(/You keep your current plan until then/)).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Keep current plan' }))

    expect(cancelScheduledPlanChange).toHaveBeenCalledWith(1)
    await waitFor(() => expect(screen.queryByText(/Your plan changes to Basic/)).not.toBeInTheDocument())
  })

  it('explains the upgrade and downgrade rule in the change plan dialog (C97 SC-1)', async () => {
    listSubscriptions.mockResolvedValue([activeSub])
    productGet.mockResolvedValue({ plans: [{ id: 1, name: 'Pro' }] })
    renderWithProviders(<MySubscriptionsPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Change plan' }))

    expect(await screen.findByText('An upgrade takes effect now. A downgrade takes effect at your next renewal.')).toBeInTheDocument()
  })

  it('lists what the active plans include, read-only (C97 EN-1)', async () => {
    listSubscriptions.mockResolvedValue([activeSub])
    entitlementsMine.mockResolvedValue([{ subscriptionId: 1, productId: 10, productName: 'Valam.ai', ownerType: 'INDIVIDUAL', planName: 'Pro',
      status: 'ACTIVE', expiresAt: '2026-11-02T10:00:00Z', includedFeatures: ['Reports', 'API access'], usageLimit: 1000, quantity: 1 }])
    renderWithProviders(<MySubscriptionsPage />)

    expect(await screen.findByRole('heading', { name: 'Valam.ai' })).toBeInTheDocument()
    expect(screen.getByText('Reports')).toBeInTheDocument()
    expect(screen.getByText('API access')).toBeInTheDocument()
    expect(screen.getByText(/Limit: 1,000/)).toBeInTheDocument()
  })
  it('turns auto-renew off after a confirmation that explains the 7-day read-only grace period (C100 RR-1)', async () => {
    const withTerm = { ...activeSub, expiresAt: '2026-11-02T10:00:00Z', autoRenew: true }
    listSubscriptions.mockResolvedValue([withTerm])
    setAutoRenew.mockResolvedValue({ ...withTerm, autoRenew: false })
    renderWithProviders(<MySubscriptionsPage />)
    const user = userEvent.setup()

    const toggle = await screen.findByRole('switch', { name: 'Auto-renew' })
    expect(toggle).toBeChecked()
    await user.click(toggle)
    const dialog = screen.getByRole('dialog', { name: 'Turn off auto-renew for Valam.ai?' })
    expect(within(dialog).getByText(/read-only for 7 days/)).toBeInTheDocument()
    expect(setAutoRenew).not.toHaveBeenCalled()
    await user.click(within(dialog).getByRole('button', { name: 'Turn off auto-renew' }))
    expect(setAutoRenew).toHaveBeenCalledWith(1, false)
    await waitFor(() => expect(screen.getByRole('switch', { name: 'Auto-renew' })).not.toBeChecked())
  })

  it('turns auto-renew on straight away', async () => {
    const off = { ...activeSub, expiresAt: '2026-11-02T10:00:00Z', autoRenew: false }
    listSubscriptions.mockResolvedValue([off])
    setAutoRenew.mockResolvedValue({ ...off, autoRenew: true })
    renderWithProviders(<MySubscriptionsPage />)
    await userEvent.setup().click(await screen.findByRole('switch', { name: 'Auto-renew' }))
    expect(setAutoRenew).toHaveBeenCalledWith(1, true)
  })

  it('shows the server message when auto-renew cannot be changed', async () => {
    const withTerm = { ...activeSub, expiresAt: '2026-11-02T10:00:00Z', autoRenew: false }
    listSubscriptions.mockResolvedValue([withTerm])
    setAutoRenew.mockRejectedValue(new ApiError(409, 'This subscription has expired.'))
    renderWithProviders(<MySubscriptionsPage />)
    await userEvent.setup().click(await screen.findByRole('switch', { name: 'Auto-renew' }))
    expect(await screen.findByText('This subscription has expired.')).toBeInTheDocument()
  })

  it('shows a read-only banner with Renew while the grace period runs, and leaves it once auto-renew is on again (C100 RR-1)', async () => {
    const graceEndsAt = new Date(Date.now() + 4 * 86_400_000).toISOString()
    const inGrace = { ...activeSub, expiresAt: '2026-10-10T10:00:00Z', autoRenew: false, graceEndsAt }
    listSubscriptions.mockResolvedValue([inGrace])
    myRenewals.mockResolvedValue([{ subscriptionId: 1, productName: 'Valam.ai', planName: null, autoRenew: false,
      renewalDate: '2026-10-10T10:00:00Z', remindersEnabled: true, nextReminderAt: null, graceEndsAt }])
    setAutoRenew.mockResolvedValue({ ...inGrace, autoRenew: true, graceEndsAt: null })
    renderWithProviders(<MySubscriptionsPage />)

    expect(await screen.findByText(/^Read-only until /)).toBeInTheDocument()
    expect(screen.getByText(/The term of Valam\.ai has ended\. It is read-only until/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Renew now' })).toBeInTheDocument()

    await userEvent.setup().click(screen.getByRole('switch', { name: 'Auto-renew' }))
    expect(setAutoRenew).toHaveBeenCalledWith(1, true)
    await waitFor(() => expect(screen.queryByText(/^Read-only until /)).not.toBeInTheDocument())
    expect(screen.queryByRole('button', { name: 'Renew now' })).not.toBeInTheDocument()
  })

  it('renews from the grace banner', async () => {
    const graceEndsAt = new Date(Date.now() + 4 * 86_400_000).toISOString()
    const inGrace = { ...activeSub, expiresAt: '2026-10-10T10:00:00Z', autoRenew: false, graceEndsAt }
    listSubscriptions.mockResolvedValue([inGrace])
    renew.mockResolvedValue({ ...inGrace, graceEndsAt: null })
    renderWithProviders(<MySubscriptionsPage />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Renew now' }))
    expect(renew).toHaveBeenCalledWith(1)
    await waitFor(() => expect(screen.queryByText(/^Read-only until /)).not.toBeInTheDocument())
  })

  it('does not show a banner once the grace period has passed', async () => {
    listSubscriptions.mockResolvedValue([{ ...activeSub, expiresAt: '2026-01-01T10:00:00Z', autoRenew: false, graceEndsAt: '2026-01-08T10:00:00Z' }])
    renderWithProviders(<MySubscriptionsPage />)
    await screen.findByText('Valam.ai')
    expect(screen.queryByText(/^Read-only until /)).not.toBeInTheDocument()
  })

  it('marks an entitlement read-only during the grace period (C100 RR-1)', async () => {
    listSubscriptions.mockResolvedValue([activeSub])
    entitlementsMine.mockResolvedValue([{ subscriptionId: 1, productId: 10, productName: 'Valam.ai', ownerType: 'INDIVIDUAL', planName: 'Pro',
      status: 'ACTIVE', expiresAt: '2026-10-10T10:00:00Z', includedFeatures: [], quantity: 1, accessMode: 'READ_ONLY', graceEndsAt: '2026-10-17T10:00:00Z' }])
    renderWithProviders(<MySubscriptionsPage />)
    expect(await screen.findByText(/^Read-only until /)).toBeInTheDocument()
  })

  it('has no detectable a11y violations with a grace banner and the auto-renew switch', async () => {
    const graceEndsAt = new Date(Date.now() + 4 * 86_400_000).toISOString()
    listSubscriptions.mockResolvedValue([{ ...activeSub, expiresAt: '2026-10-10T10:00:00Z', autoRenew: false, graceEndsAt }])
    const { container } = renderWithProviders(<MySubscriptionsPage />)
    await screen.findByRole('switch', { name: 'Auto-renew' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
