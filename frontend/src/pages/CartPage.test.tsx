import '../i18n'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { CartPage } from './CartPage'
import type { Cart } from '../api/cartApi'

const cartGet = vi.fn()
const cartAdd = vi.fn()
const cartChangePlan = vi.fn()
const cartConfirmPrice = vi.fn()
const cartRemove = vi.fn()
const cartClear = vi.fn()
const cartValidate = vi.fn()
const cartCheckout = vi.fn()
const cartApplyCoupon = vi.fn()
const cartRemoveCoupon = vi.fn()
vi.mock('../api/cartApi', () => ({
  cartApi: {
    get: () => cartGet(),
    add: (p: number, plan: number) => cartAdd(p, plan),
    changePlan: (id: number, plan: number) => cartChangePlan(id, plan),
    confirmPrice: (id: number) => cartConfirmPrice(id),
    remove: (id: number) => cartRemove(id),
    clear: () => cartClear(),
    validate: () => cartValidate(),
    checkout: (reason?: string) => cartCheckout(reason),
    applyCoupon: (code: string) => cartApplyCoupon(code),
    removeCoupon: () => cartRemoveCoupon(),
  },
}))

const getProduct = vi.fn()
vi.mock('../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../api/productsApi')>('../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, get: (id: number) => getProduct(id) } }
})

const plans = [
  { id: 11, name: 'Pro monthly', billingPeriod: 'MONTHLY', price: 100000, currency: 'INR' },
  { id: 12, name: 'Pro yearly', billingPeriod: 'YEARLY', price: 1000000, currency: 'INR' },
]

function item(overrides: Record<string, unknown> = {}) {
  return {
    id: 41, productId: 7, productName: 'Valam.ai', planId: 11, planName: 'Pro monthly', billingPeriod: 'MONTHLY',
    unitPrice: 100000, unitPriceAtAdd: 100000, currency: 'INR', seats: 1, amount: 100000, addedAt: '2026-10-01T00:00:00Z', plans,
    ...overrides,
  }
}

function cart(items = [item()], overrides: Record<string, unknown> = {}): Cart {
  const subtotal = items.reduce((s, i) => s + (i.amount as number), 0)
  return {
    items, itemCount: items.length, currency: 'INR', subtotal, taxLines: [], taxCalculatedAtPayment: true, total: subtotal,
    continueAs: 'INDIVIDUAL', couponCode: null, discount: 0, couponMessage: null, ...overrides,
  } as Cart
}

function Where() {
  const location = useLocation()
  return <div>At {location.pathname}{location.search}</div>
}

function renderCart(url = '/cart') {
  return render(
    <MemoryRouter initialEntries={[url]}>
      <Routes>
        <Route path="/cart" element={<CartPage />} />
        <Route path="*" element={<Where />} />
      </Routes>
    </MemoryRouter>
  )
}

describe('CartPage (C59, REQ-MKT-003)', () => {
  beforeEach(() => {
    for (const m of [cartGet, cartAdd, cartChangePlan, cartConfirmPrice, cartRemove, cartClear, cartValidate, cartCheckout, cartApplyCoupon, cartRemoveCoupon, getProduct]) m.mockReset()
    cartGet.mockResolvedValue(cart())
  })

  it('shows each item, the summary with "Tax calculated at payment", a coupon field, and no quantity or shipping', async () => {
    const { container } = renderCart()
    expect(await screen.findByRole('heading', { name: 'Valam.ai' })).toBeInTheDocument()
    expect(screen.getByText('Monthly')).toBeInTheDocument()
    const summary = screen.getByRole('region', { name: /Order summary/ })
    expect(within(summary).getByText('Tax calculated at payment')).toBeInTheDocument()
    expect(within(summary).getByRole('button', { name: 'Proceed to checkout' })).toBeInTheDocument()
    expect(within(summary).getByRole('list', { name: 'Accepted payment methods' })).toHaveTextContent('Visa')
    expect(within(summary).getByRole('textbox', { name: 'Coupon code' })).toBeInTheDocument()
    expect(screen.queryByText(/shipping/i)).not.toBeInTheDocument()
    expect(screen.queryByRole('spinbutton')).not.toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('adds the product from a Buy link, preferring the monthly paid plan, then cleans the URL', async () => {
    getProduct.mockResolvedValue({ id: 7, name: 'Valam.ai', plans: [
      { id: 10, name: 'Free', price: 0, billingPeriod: 'MONTHLY', currency: 'INR' },
      { id: 12, name: 'Pro yearly', price: 10000, billingPeriod: 'YEARLY', currency: 'INR' },
      { id: 11, name: 'Pro monthly', price: 1000, billingPeriod: 'MONTHLY', currency: 'INR' },
    ] })
    cartAdd.mockResolvedValue(cart())
    renderCart('/cart?add=7')
    expect(await screen.findByText('Added Valam.ai — Pro monthly to your cart')).toBeInTheDocument()
    expect(cartAdd).toHaveBeenCalledWith(7, 11)
    expect(cartAdd).toHaveBeenCalledTimes(1)
  })

  it('keeps the free-plan behaviour for a product with no paid plan', async () => {
    getProduct.mockResolvedValue({ id: 8, name: 'Free tool', plans: [{ id: 1, name: 'Free', price: 0, billingPeriod: 'MONTHLY', currency: 'INR' }] })
    renderCart('/cart?add=8')
    expect(await screen.findByText('At /checkout?productId=8')).toBeInTheDocument()
    expect(cartAdd).not.toHaveBeenCalled()
  })

  it('removes an item with Undo that restores it with the same plan', async () => {
    cartRemove.mockResolvedValue(cart([]))
    cartAdd.mockResolvedValue(cart())
    renderCart()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Remove Valam.ai' }))
    expect(await screen.findByText('Your cart is empty')).toBeInTheDocument()
    expect(screen.getByText('Removed Valam.ai')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Undo' }))
    await waitFor(() => expect(cartAdd).toHaveBeenCalledWith(7, 11))
    expect(await screen.findByRole('heading', { name: 'Valam.ai' })).toBeInTheDocument()
  })

  it('changes the plan immediately and rolls back when the server refuses', async () => {
    cartChangePlan.mockRejectedValue(new ApiError(400, 'That plan does not belong to this product.'))
    renderCart()
    const user = userEvent.setup()
    const card = (await screen.findByRole('heading', { name: 'Valam.ai' })).closest('li')!
    await user.click(within(card).getByRole('combobox', { name: /Change plan/ }))
    await user.click(await screen.findByRole('option', { name: /Pro yearly/ }))
    await waitFor(() => expect(cartChangePlan).toHaveBeenCalledWith(41, 12))
    expect(await screen.findByText('That plan does not belong to this product.')).toBeInTheDocument()
    expect(within(card).getByRole('combobox', { name: /Change plan/ })).toHaveTextContent('Pro monthly')
  })

  it('clears the cart only after confirmation', async () => {
    cartClear.mockResolvedValue(undefined)
    renderCart()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Clear cart' }))
    const dialog = screen.getByRole('dialog', { name: 'Clear cart?' })
    expect(await axe(dialog)).toHaveNoViolations()
    await user.click(within(dialog).getByRole('button', { name: 'Cancel' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(cartClear).not.toHaveBeenCalled()
    await user.click(screen.getByRole('button', { name: 'Clear cart' }))
    await user.click(within(screen.getByRole('dialog', { name: 'Clear cart?' })).getByRole('button', { name: 'Clear cart' }))
    expect(await screen.findByText('Your cart is empty')).toBeInTheDocument()
  })

  it('tells the customer when a product is not available for their account type (REQ-CAT-005)', async () => {
    cartGet.mockResolvedValue(cart([item({ id: 44, productId: 10, productName: 'Yukth.ai' })]))
    cartValidate.mockResolvedValue({ valid: false, issues: [{ itemId: 44, code: 'NOT_ELIGIBLE' }] })
    renderCart()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Proceed to checkout' }))
    expect(await screen.findByText('Yukth.ai is not available for your account type.')).toBeInTheDocument()
    expect(cartCheckout).not.toHaveBeenCalled()
  })

  it('keeps the customer on the cart and shows each validation issue in its item', async () => {
    cartGet.mockResolvedValue(cart([item(), item({ id: 42, productId: 8, productName: 'Varthan.ai' }), item({ id: 43, productId: 9, productName: 'Thiran.ai' })]))
    cartValidate.mockResolvedValue({
      valid: false, issues: [
        { itemId: 41, code: 'PRICE_CHANGED', oldPrice: 100000, newPrice: 120000 },
        { itemId: 42, code: 'ALREADY_SUBSCRIBED' },
        { itemId: 43, code: 'MISSING_DEPENDENCY', requiredProductId: 3, requiredProductName: 'Tharav.ai' },
      ],
    })
    cartConfirmPrice.mockResolvedValue(cart())
    renderCart()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Proceed to checkout' }))
    expect(await screen.findByText(/The price changed from .*1,000\.00 to .*1,200\.00\./)).toBeInTheDocument()
    expect(screen.getByText('You already have an active subscription to Varthan.ai.')).toBeInTheDocument()
    expect(screen.getByText('Thiran.ai requires Tharav.ai.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Add Tharav.ai' })).toHaveAttribute('href', '/products/3')
    expect(cartCheckout).not.toHaveBeenCalled()
    expect(screen.getAllByRole('button', { name: /Confirm new price/ })).toHaveLength(1)
    await user.click(screen.getByRole('button', { name: 'Confirm new prices' }))
    expect(cartConfirmPrice).toHaveBeenCalledWith(41)
  })

  it('confirms every changed price once for the whole cart (C100 CA-6)', async () => {
    cartGet.mockResolvedValue(cart([item(), item({ id: 42, productId: 8, productName: 'Varthan.ai' })]))
    cartValidate.mockResolvedValue({
      valid: false, issues: [
        { itemId: 41, code: 'PRICE_CHANGED', oldPrice: 100000, newPrice: 120000 },
        { itemId: 42, code: 'PRICE_CHANGED', oldPrice: 100000, newPrice: 90000 },
      ],
    })
    cartConfirmPrice.mockResolvedValue(cart())
    renderCart()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Proceed to checkout' }))
    expect(await screen.findByText('The prices of 2 items in your cart changed. Confirm the new prices once for the whole cart to continue.')).toBeInTheDocument()
    const confirm = screen.getAllByRole('button', { name: 'Confirm new prices' })
    expect(confirm).toHaveLength(1)
    await user.click(confirm[0])
    await waitFor(() => expect(cartConfirmPrice).toHaveBeenCalledTimes(2))
    expect(cartConfirmPrice).toHaveBeenNthCalledWith(1, 41)
    expect(cartConfirmPrice).toHaveBeenNthCalledWith(2, 42)
    await waitFor(() => expect(screen.queryByRole('button', { name: 'Confirm new prices' })).not.toBeInTheDocument())
  })

  it('opens the checkout for the new invoice', async () => {
    cartValidate.mockResolvedValue({ valid: true, issues: [] })
    cartCheckout.mockResolvedValue({ kind: 'INVOICE', invoiceId: 9 })
    renderCart()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Proceed to checkout' }))
    expect(await screen.findByText('At /checkout?invoiceId=9&from=cart')).toBeInTheDocument()
    expect(cartCheckout).toHaveBeenCalledWith(undefined)
  })

  it('shows the seats and the per-seat price of an organization item, and nothing for one seat', async () => {
    cartGet.mockResolvedValue(cart([item({ seats: 5, amount: 500000 }), item({ id: 42, productId: 8, productName: 'Varthan.ai' })], { continueAs: 'ORGANIZATION_BUYER' }))
    renderCart()
    const first = (await screen.findByRole('heading', { name: 'Valam.ai' })).closest('li')!
    expect(within(first).getByText(/^5 seats × .*1,000\.00$/)).toBeInTheDocument()
    expect(within(first).getByText(/5,000\.00/)).toBeInTheDocument()
    const second = screen.getByRole('heading', { name: 'Varthan.ai' }).closest('li')!
    expect(within(second).queryByText(/seats/)).not.toBeInTheDocument()
  })

  it('lets an organization buyer purchase directly, with a note that the admins are notified (C100 CA-2)', async () => {
    cartGet.mockResolvedValue(cart([item(), item({ id: 42, productId: 8, productName: 'Varthan.ai' })], { continueAs: 'ORGANIZATION_BUYER' }))
    cartValidate.mockResolvedValue({ valid: true, issues: [] })
    cartCheckout.mockResolvedValue({ kind: 'INVOICE', invoiceId: 12 })
    renderCart()
    expect(await screen.findByText('If you are not an organization admin, the organization admins are notified of this purchase.')).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: 'Coupon code' })).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Proceed to checkout' }))
    expect(await screen.findByText('At /checkout?invoiceId=12&from=cart')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(cartCheckout).toHaveBeenCalledTimes(1)
    expect(cartCheckout).toHaveBeenCalledWith(undefined)
  })

  it('shows a coupon on the summary with a negative discount line and the net total, and removes it', async () => {
    cartGet.mockResolvedValue(cart([item()], { couponCode: 'SAVE10', discount: 10000, total: 90000 }))
    cartRemoveCoupon.mockResolvedValue(cart())
    renderCart()
    const summary = await screen.findByRole('region', { name: /Order summary/ })
    expect(within(summary).getByText('Coupon SAVE10')).toBeInTheDocument()
    expect(within(summary).getByText('Discount (SAVE10)')).toBeInTheDocument()
    expect(within(summary).getByText(/^−.*100\.00$/)).toBeInTheDocument()
    expect(within(summary).getByText(/900\.00/)).toBeInTheDocument()
    expect(within(summary).queryByRole('textbox', { name: 'Coupon code' })).not.toBeInTheDocument()
    await userEvent.setup().click(within(summary).getByRole('button', { name: 'Remove coupon' }))
    expect(cartRemoveCoupon).toHaveBeenCalledTimes(1)
    expect(await within(summary).findByRole('textbox', { name: 'Coupon code' })).toBeInTheDocument()
    expect(within(summary).queryByText('Discount (SAVE10)')).not.toBeInTheDocument()
  })

  it('applies a coupon code in capitals and shows the discount', async () => {
    cartApplyCoupon.mockResolvedValue(cart([item()], { couponCode: 'SAVE10', discount: 10000, total: 90000 }))
    renderCart()
    const user = userEvent.setup()
    await user.type(await screen.findByRole('textbox', { name: 'Coupon code' }), 'save10')
    await user.click(screen.getByRole('button', { name: 'Apply' }))
    expect(cartApplyCoupon).toHaveBeenCalledWith('SAVE10')
    expect(await screen.findByText('Discount (SAVE10)')).toBeInTheDocument()
  })

  it('shows the reason a coupon is refused and keeps the field', async () => {
    cartApplyCoupon.mockRejectedValue(new ApiError(400, 'This coupon has expired.'))
    const { container } = renderCart()
    const user = userEvent.setup()
    await user.type(await screen.findByRole('textbox', { name: 'Coupon code' }), 'OLD')
    await user.click(screen.getByRole('button', { name: 'Apply' }))
    expect(await screen.findByText('This coupon has expired.')).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: 'Coupon code' })).toHaveValue('OLD')
    expect(await axe(container)).toHaveNoViolations()
  })

  it('warns when the applied coupon can no longer be used, and lets the customer remove it', async () => {
    cartGet.mockResolvedValue(cart([item()], { couponCode: 'SAVE10', discount: 0, couponMessage: 'This coupon has expired.' }))
    const { container } = renderCart()
    const summary = await screen.findByRole('region', { name: /Order summary/ })
    expect(within(summary).getByRole('alert')).toHaveTextContent('This coupon has expired.')
    expect(within(summary).getByText('Coupon SAVE10')).toBeInTheDocument()
    expect(within(summary).queryByText('Discount (SAVE10)')).not.toBeInTheDocument()
    expect(within(summary).getByRole('button', { name: 'Remove coupon' })).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('keeps the cart and shows the message when checkout is refused because the coupon became invalid', async () => {
    cartGet.mockResolvedValueOnce(cart([item()], { couponCode: 'SAVE10', discount: 10000, total: 90000 }))
    cartGet.mockResolvedValue(cart([item()], { couponCode: 'SAVE10', discount: 0, couponMessage: 'This coupon has expired.' }))
    cartValidate.mockResolvedValue({ valid: true, issues: [] })
    cartCheckout.mockRejectedValue(new ApiError(400, 'This coupon has expired.'))
    renderCart()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Proceed to checkout' }))
    expect((await screen.findAllByText('This coupon has expired.')).length).toBeGreaterThan(0)
    expect(screen.getByRole('heading', { name: 'Valam.ai' })).toBeInTheDocument()
    expect(screen.queryByText(/^At \/checkout/)).not.toBeInTheDocument()
  })

  it('submits an organization member\'s cart as one purchase request with a required reason (C100 CA-2)', async () => {
    cartGet.mockResolvedValue(cart([item(), item({ id: 42, productId: 8, productName: 'Varthan.ai' })], { continueAs: 'ORGANIZATION_MEMBER' }))
    cartValidate.mockResolvedValue({ valid: true, issues: [] })
    cartCheckout.mockResolvedValue({ kind: 'ORDER', orderIds: [15] })
    renderCart()
    expect(await screen.findByText(/You cannot buy products yourself/)).toBeInTheDocument()
    expect(screen.queryByRole('textbox', { name: 'Coupon code' })).not.toBeInTheDocument()
    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: 'Submit purchase request' }))
    const dialog = await screen.findByRole('dialog', { name: 'Submit a purchase request' })
    expect(cartCheckout).not.toHaveBeenCalled()
    expect(await axe(dialog)).toHaveNoViolations()
    // the reason is required
    await user.click(within(dialog).getByRole('button', { name: 'Submit request' }))
    expect(within(dialog).getByText('Tell your organization admin why you need this.')).toBeInTheDocument()
    expect(cartCheckout).not.toHaveBeenCalled()
    // and has at most 500 characters
    const field = within(dialog).getByRole('textbox', { name: /Why do you need this\?/ })
    expect(field).toHaveAttribute('maxlength', '500')
    await user.type(field, '  Needed for the Q4 forecast  ')
    await user.click(within(dialog).getByRole('button', { name: 'Submit request' }))
    expect(await screen.findByText('At /checkout?orders=15&step=complete')).toBeInTheDocument()
    expect(cartCheckout).toHaveBeenCalledTimes(1)
    expect(cartCheckout).toHaveBeenCalledWith('Needed for the Q4 forecast')
  })

  it('does not ask for a reason when the cart has validation issues', async () => {
    cartGet.mockResolvedValue(cart([item({ id: 44, productName: 'Yukth.ai' })], { continueAs: 'ORGANIZATION_MEMBER' }))
    cartValidate.mockResolvedValue({ valid: false, issues: [{ itemId: 44, code: 'NOT_ELIGIBLE' }] })
    renderCart()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Submit purchase request' }))
    await user.type(await screen.findByRole('textbox', { name: /Why do you need this\?/ }), 'Because')
    await user.click(screen.getByRole('button', { name: 'Submit request' }))
    expect(await screen.findByText('Yukth.ai is not available for your account type.')).toBeInTheDocument()
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(cartCheckout).not.toHaveBeenCalled()
  })

  it('shows the empty state with Browse products', async () => {
    cartGet.mockResolvedValue(cart([]))
    const { container } = renderCart()
    expect(await screen.findByText('Your cart is empty')).toBeInTheDocument()
    expect(screen.getByText('Find a product to get started.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Browse products' })).toHaveAttribute('href', '/products')
    expect(await axe(container)).toHaveNoViolations()
  })
})
