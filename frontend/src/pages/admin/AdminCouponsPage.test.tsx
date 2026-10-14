import '../../i18n'
import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import type { Coupon } from '../../api/couponApi'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminCouponsPage } from './AdminCouponsPage'

const list = vi.fn()
const create = vi.fn()
const update = vi.fn()
const setEnabled = vi.fn()
const remove = vi.fn()
vi.mock('../../api/couponApi', () => ({
  couponApi: {
    list: () => list(),
    create: (body: unknown) => create(body),
    update: (id: number, body: unknown) => update(id, body),
    setEnabled: (id: number, enabled: boolean) => setEnabled(id, enabled),
    remove: (id: number) => remove(id),
  },
}))

const percentCoupon: Coupon = {
  id: 1, code: 'SAVE10', description: 'Launch offer', discountType: 'PERCENT', percentOff: 10, amountOff: null, currency: null,
  expiresAt: '2099-12-31T18:30:00Z', enabled: true, status: 'ACTIVE', redeemedCount: 3, createdAt: '2026-10-01T00:00:00Z',
}
const fixedCoupon: Coupon = {
  id: 2, code: 'FLAT500', description: null, discountType: 'FIXED', percentOff: null, amountOff: 50000, currency: 'INR',
  expiresAt: '2099-06-30T00:00:00Z', enabled: false, status: 'DISABLED', redeemedCount: 0, createdAt: '2026-10-02T00:00:00Z',
}
const expiredCoupon: Coupon = {
  id: 3, code: 'OLD', description: null, discountType: 'PERCENT', percentOff: 5, amountOff: null, currency: null,
  expiresAt: '2026-01-01T00:00:00Z', enabled: true, status: 'EXPIRED', redeemedCount: 1, createdAt: '2025-12-01T00:00:00Z',
}

describe('AdminCouponsPage (C100)', () => {
  beforeEach(() => {
    for (const m of [list, create, update, setEnabled, remove]) m.mockReset()
    list.mockResolvedValue([percentCoupon, fixedCoupon, expiredCoupon])
  })

  it('lists each coupon with its discount, status and redeemed count, and has no accessibility violations', async () => {
    const { container } = renderWithProviders(<AdminCouponsPage />)
    const row = (await screen.findByText('SAVE10')).closest('tr')!
    expect(within(row).getByText('Launch offer')).toBeInTheDocument()
    expect(within(row).getByText('10% off')).toBeInTheDocument()
    expect(within(row).getByText('Active')).toBeInTheDocument()
    expect(within(row).getByText('3')).toBeInTheDocument()
    const fixedRow = screen.getByText('FLAT500').closest('tr')!
    expect(within(fixedRow).getByText(/500\.00 off/)).toBeInTheDocument()
    expect(within(fixedRow).getByText('Disabled')).toBeInTheDocument()
    const expiredRow = screen.getByText('OLD').closest('tr')!
    expect(within(expiredRow).getByText('Expired')).toBeInTheDocument()
    // no Disable/Enable once the coupon has expired
    expect(within(expiredRow).queryByRole('button', { name: /Disable|Enable/ })).not.toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('shows an empty state with an Add coupon action', async () => {
    list.mockResolvedValue([])
    renderWithProviders(<AdminCouponsPage />)
    expect(await screen.findByText('No coupons yet')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: 'Add coupon' })).toHaveLength(2)
  })

  it('creates a percent coupon with the code in capitals and the expiry as an ISO instant in the browser time zone', async () => {
    create.mockResolvedValue({ ...percentCoupon, id: 9, code: 'WELCOME20', percentOff: 20 })
    renderWithProviders(<AdminCouponsPage />)
    const user = userEvent.setup()
    await user.click((await screen.findAllByRole('button', { name: 'Add coupon' }))[0])
    const dialog = await screen.findByRole('dialog', { name: 'Add coupon' })
    expect(await axe(dialog)).toHaveNoViolations()
    await user.type(within(dialog).getByRole('textbox', { name: /^Code/ }), 'welcome20')
    expect(within(dialog).getByRole('textbox', { name: /^Code/ })).toHaveValue('WELCOME20')
    await user.type(within(dialog).getByRole('textbox', { name: /Description/ }), 'Welcome offer')
    await user.type(within(dialog).getByRole('spinbutton', { name: /Percent off/ }), '20')
    fireEvent.change(within(dialog).getByLabelText(/Expires on/), { target: { value: '2099-12-31T18:30' } })
    const zone = Intl.DateTimeFormat().resolvedOptions().timeZone
    expect(within(dialog).getByText(`Date and time in your time zone (${zone}).`)).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(create).toHaveBeenCalledTimes(1))
    expect(create).toHaveBeenCalledWith({
      code: 'WELCOME20', description: 'Welcome offer', discountType: 'PERCENT', percentOff: 20, amountOff: null, currency: null,
      expiresAt: new Date('2099-12-31T18:30').toISOString(),
    })
    expect(await screen.findByText('Coupon WELCOME20 created')).toBeInTheDocument()
    expect(list).toHaveBeenCalledTimes(2)
  })

  it('converts a fixed amount in major units to minor units with its currency', async () => {
    create.mockResolvedValue(fixedCoupon)
    renderWithProviders(<AdminCouponsPage />)
    const user = userEvent.setup()
    await user.click((await screen.findAllByRole('button', { name: 'Add coupon' }))[0])
    const dialog = await screen.findByRole('dialog', { name: 'Add coupon' })
    await user.type(within(dialog).getByRole('textbox', { name: /^Code/ }), 'flat-5')
    await user.click(within(dialog).getByRole('combobox', { name: 'Discount type' }))
    await user.click(await screen.findByRole('option', { name: 'Fixed amount' }))
    await user.type(within(dialog).getByRole('spinbutton', { name: /Amount off/ }), '12.50')
    await user.click(within(dialog).getByRole('combobox', { name: 'Currency' }))
    await user.click(await screen.findByRole('option', { name: 'USD' }))
    fireEvent.change(within(dialog).getByLabelText(/Expires on/), { target: { value: '2099-01-02T03:04' } })
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(create).toHaveBeenCalledTimes(1))
    expect(create).toHaveBeenCalledWith({
      code: 'FLAT-5', description: '', discountType: 'FIXED', percentOff: null, amountOff: 1250, currency: 'USD',
      expiresAt: new Date('2099-01-02T03:04').toISOString(),
    })
  })

  it('checks the form before sending and shows what the server refuses', async () => {
    create.mockRejectedValue(new ApiError(400, 'A coupon with this code already exists.'))
    renderWithProviders(<AdminCouponsPage />)
    const user = userEvent.setup()
    await user.click((await screen.findAllByRole('button', { name: 'Add coupon' }))[0])
    const dialog = await screen.findByRole('dialog', { name: 'Add coupon' })
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(within(dialog).getByText('Use 3 to 40 letters, digits, dashes or underscores.')).toBeInTheDocument()
    expect(within(dialog).getByText('Enter a percentage from 1 to 99.')).toBeInTheDocument()
    expect(within(dialog).getByText('Set the expiry date and time.')).toBeInTheDocument()
    expect(create).not.toHaveBeenCalled()
    await user.type(within(dialog).getByRole('textbox', { name: /^Code/ }), 'SAVE10')
    await user.type(within(dialog).getByRole('spinbutton', { name: /Percent off/ }), '100')
    fireEvent.change(within(dialog).getByLabelText(/Expires on/), { target: { value: '2001-01-01T00:00' } })
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(within(dialog).getByText('Enter a percentage from 1 to 99.')).toBeInTheDocument()
    expect(within(dialog).getByText('The expiry must be in the future.')).toBeInTheDocument()
    await user.clear(within(dialog).getByRole('spinbutton', { name: /Percent off/ }))
    await user.type(within(dialog).getByRole('spinbutton', { name: /Percent off/ }), '99')
    fireEvent.change(within(dialog).getByLabelText(/Expires on/), { target: { value: '2099-01-01T00:00' } })
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(await within(dialog).findByText('A coupon with this code already exists.')).toBeInTheDocument()
  })

  it('edits a coupon with the code locked', async () => {
    update.mockResolvedValue({ ...percentCoupon, percentOff: 15 })
    renderWithProviders(<AdminCouponsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit SAVE10' }))
    const dialog = await screen.findByRole('dialog', { name: 'Edit coupon' })
    expect(within(dialog).getByRole('textbox', { name: /^Code/ })).toBeDisabled()
    expect(within(dialog).getByRole('textbox', { name: /^Code/ })).toHaveValue('SAVE10')
    const percent = within(dialog).getByRole('spinbutton', { name: /Percent off/ })
    expect(percent).toHaveValue(10)
    await user.clear(percent)
    await user.type(percent, '15')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(update).toHaveBeenCalledTimes(1))
    expect(update).toHaveBeenCalledWith(1, expect.objectContaining({
      code: 'SAVE10', description: 'Launch offer', discountType: 'PERCENT', percentOff: 15, amountOff: null, currency: null,
      expiresAt: '2099-12-31T18:30:00.000Z',
    }))
  })

  it('disables an active coupon and enables a disabled one', async () => {
    setEnabled.mockImplementation((id: number, enabled: boolean) =>
      Promise.resolve(id === 1 ? { ...percentCoupon, enabled, status: enabled ? 'ACTIVE' : 'DISABLED' } : { ...fixedCoupon, enabled, status: enabled ? 'ACTIVE' : 'DISABLED' }))
    renderWithProviders(<AdminCouponsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Disable SAVE10' }))
    expect(setEnabled).toHaveBeenCalledWith(1, false)
    expect(await screen.findByRole('button', { name: 'Enable SAVE10' })).toBeInTheDocument()
    expect(await screen.findByText('Coupon SAVE10 disabled')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Enable FLAT500' }))
    expect(setEnabled).toHaveBeenCalledWith(2, true)
    expect(await screen.findByRole('button', { name: 'Disable FLAT500' })).toBeInTheDocument()
  })

  it('deletes a coupon only after confirmation', async () => {
    remove.mockResolvedValue(undefined)
    renderWithProviders(<AdminCouponsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Delete FLAT500' }))
    const dialog = await screen.findByRole('dialog', { name: 'Delete FLAT500?' })
    await user.click(within(dialog).getByRole('button', { name: 'Cancel' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(remove).not.toHaveBeenCalled()
    await user.click(screen.getByRole('button', { name: 'Delete FLAT500' }))
    await user.click(within(await screen.findByRole('dialog', { name: 'Delete FLAT500?' })).getByRole('button', { name: 'Delete' }))
    await waitFor(() => expect(remove).toHaveBeenCalledWith(2))
    await waitFor(() => expect(screen.queryByText('FLAT500')).not.toBeInTheDocument())
  })

  it('shows the message when the server refuses to delete a redeemed coupon', async () => {
    remove.mockRejectedValue(new ApiError(409, 'This coupon has been redeemed, so it cannot be deleted. Disable it instead.'))
    renderWithProviders(<AdminCouponsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Delete SAVE10' }))
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Delete' }))
    expect(await screen.findByText('This coupon has been redeemed, so it cannot be deleted. Disable it instead.')).toBeInTheDocument()
    expect(screen.getByText('SAVE10')).toBeInTheDocument()
  })

  it('offers a retry when the list cannot be loaded', async () => {
    list.mockRejectedValueOnce(new ApiError(500, 'Boom'))
    renderWithProviders(<AdminCouponsPage />)
    expect(await screen.findByText('Boom')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByText('SAVE10')).toBeInTheDocument()
  })
})
