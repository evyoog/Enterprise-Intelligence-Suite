import '../i18n'
import { fireEvent, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { render } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { LocalePreferenceProvider } from '../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../theming/ThemeModeProvider'
import { ProductDetailPage } from './ProductDetailPage'

const getProduct = vi.fn()
vi.mock('../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../api/productsApi')>('../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, get: (id: number) => getProduct(id) } }
})

const getRatings = vi.fn()
const getMine = vi.fn()
const submit = vi.fn()
vi.mock('../api/reviewsApi', async () => {
  const actual = await vi.importActual<typeof import('../api/reviewsApi')>('../api/reviewsApi')
  return {
    ...actual,
    reviewsApi: {
      ...actual.reviewsApi,
      getRatings: (id: number) => getRatings(id),
      getMine: (id: number) => getMine(id),
      submit: (id: number, rating: number, comment?: string) => submit(id, rating, comment),
    },
  }
})

const getContent = vi.fn()
vi.mock('../api/productContentApi', () => ({ productContentApi: { get: (id: number) => getContent(id), download: vi.fn() } }))

const getWorksWith = vi.fn()
vi.mock('../api/offeringsApi', () => ({ offeringsApi: { worksWith: (id: number) => getWorksWith(id) } }))

// C102 (REQ-MKT-005): the Try it first panel — trials, demo requests, tutorials.
const startTrial = vi.fn()
const startOrganizationTrial = vi.fn()
const requestDemo = vi.fn()
vi.mock('../api/evaluationApi', () => ({
  evaluationApi: {
    startTrial: (id: number) => startTrial(id),
    startOrganizationTrial: (id: number) => startOrganizationTrial(id),
    requestDemo: (payload: unknown) => requestDemo(payload),
  },
}))
const cartGet = vi.fn()
vi.mock('../api/cartApi', async () => {
  const actual = await vi.importActual<typeof import('../api/cartApi')>('../api/cartApi')
  return { ...actual, cartApi: { ...actual.cartApi, get: () => cartGet() } }
})
const listMyProducts = vi.fn()
const listMyOrgProducts = vi.fn()
vi.mock('../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../api/registrationApi')>('../api/registrationApi')
  return {
    ...actual,
    myProductsApi: { ...actual.myProductsApi, listProducts: () => listMyProducts() },
    organizationApi: { ...actual.organizationApi, listMyOrgProducts: () => listMyOrgProducts() },
  }
})

let authState = { isAuthenticated: false }
vi.mock('../auth/AuthProvider', async () => {
  const actual = await vi.importActual<typeof import('../auth/AuthProvider')>('../auth/AuthProvider')
  return { ...actual, useAuth: () => authState }
})

const product = {
  id: 1, name: 'Valam.ai', description: 'Analytics for everyone.', price: 0, status: 'ACTIVE' as const,
  ssoConnected: false, featured: false, platforms: [], plans: [], version: 1, dependsOnProductIds: [],
}

function renderDetail() {
  return render(
    <MemoryRouter initialEntries={['/products/1']}>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <Routes>
            <Route path="/products/:id" element={<ProductDetailPage />} />
            <Route path="/login" element={<div>Sign in page</div>} />
            <Route path="/cart" element={<div>Cart page</div>} />
            <Route path="/my/subscriptions" element={<div>My subscriptions page</div>} />
            <Route path="/support/tickets" element={<div>My tickets page</div>} />
          </Routes>
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('ProductDetailPage', () => {
  beforeEach(() => {
    for (const m of [getProduct, getRatings, getMine, submit, getContent, startTrial, startOrganizationTrial, requestDemo, cartGet, listMyProducts, listMyOrgProducts]) m.mockReset()
    cartGet.mockResolvedValue({ continueAs: 'INDIVIDUAL' })
    listMyProducts.mockResolvedValue([{ productId: 1, productName: 'Valam.ai', subscriptionStatus: null }])
    listMyOrgProducts.mockResolvedValue([{ productId: 1, productName: 'Valam.ai', orgSubscriptionStatus: null, myAccessAssigned: false }])
    getContent.mockRejectedValue(new Error('none'))
    getWorksWith.mockReset()
    getWorksWith.mockResolvedValue([])
    authState = { isAuthenticated: false }
    getMine.mockRejectedValue(new ApiError(404, 'Review not found'))
  })

  it('shows a Resources tab only when the product has published content (REQ-CAT-004.9)', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: 0, reviewCount: 0, reviews: [] })
    getContent.mockResolvedValue({
      datasheets: [{ id: 1, kind: 'DATASHEET', title: 'Platform datasheet', description: null, version: 1, updatedAt: '2026-10-07T10:00:00Z',
        file: { fileName: 'd.pdf', size: 1024, mimeType: 'application/pdf' }, imageUrl: null, altText: null, logoUrl: null, videoProvider: null,
        videoUrl: null, embedUrl: null, thumbnailUrl: null, customerName: null, problem: null, result: null, articleRef: null, articleType: null }],
      documentation: [], images: [], videos: [], caseStudies: [],
    })
    renderDetail()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('tab', { name: 'Resources' }))
    expect(await screen.findByText('Platform datasheet')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Download Platform datasheet/ })).toBeInTheDocument()
  })

  it('lists the products it works with on the overview, and nothing when there are none (REQ-CAT-005, OF-7)', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: 0, reviewCount: 0, reviews: [] })
    getWorksWith.mockResolvedValue([{ id: 2, name: 'Varthan.ai' }])
    const { unmount } = renderDetail()
    const link = await screen.findByRole('link', { name: 'Varthan.ai' })
    expect(link).toHaveAttribute('href', '/products/2')
    expect(screen.getByText('Works with')).toBeInTheDocument()
    unmount()

    getWorksWith.mockResolvedValue([])
    renderDetail()
    await screen.findByRole('tab', { name: 'Overview' })
    expect(screen.queryByText('Works with')).not.toBeInTheDocument()
  })

  it('has no Resources tab when nothing is published', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: 0, reviewCount: 0, reviews: [] })
    getContent.mockResolvedValue({ datasheets: [], documentation: [], images: [], videos: [], caseStudies: [] })
    renderDetail()
    await screen.findByRole('tab', { name: 'Overview' })
    expect(screen.queryByRole('tab', { name: 'Resources' })).not.toBeInTheDocument()
  })

  it('sends a signed-out visitor to sign in first, with a way back to the cart', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    renderDetail()

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Subscribe' }))
    expect(await screen.findByText('Sign in page')).toBeInTheDocument()
  })

  it('adds the product to the cart for a signed-in customer (C59)', async () => {
    authState = { isAuthenticated: true }
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    renderDetail()

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Subscribe' }))
    expect(await screen.findByText('Cart page')).toBeInTheDocument()
  })

  it('shows the product-access flow showcase', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    renderDetail()

    expect(await screen.findByText('How you get access')).toBeInTheDocument()
    expect(screen.getByText('Discover')).toBeInTheDocument()
    expect(screen.getByText('Launch')).toBeInTheDocument()
  })

  it('shows the average rating and approved reviews', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({
      averageRating: 4.5, reviewCount: 2,
      reviews: [{ id: 1, productId: 1, productName: 'Valam.ai', customerId: 5, customerName: 'Jane', rating: 5, comment: 'Great!', status: 'APPROVED', createdAt: '2027-03-01T00:00:00Z' }],
    })
    renderDetail()
    await userEvent.setup().click(await screen.findByRole('tab', { name: /Reviews/ }))
    expect(await screen.findByText('Great!')).toBeInTheDocument()
  })

  it('shows a no-reviews message when there are none', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    renderDetail()
    await userEvent.setup().click(await screen.findByRole('tab', { name: /Reviews/ }))
    expect(await screen.findByText('No reviews yet.')).toBeInTheDocument()
  })

  it('filters the review list by clicking a star-rating bar, and clears the filter again', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({
      averageRating: 4,
      reviewCount: 2,
      reviews: [
        { id: 1, productId: 1, productName: 'Valam.ai', customerId: 5, customerName: 'Jane', rating: 5, comment: 'Excellent!', status: 'APPROVED', createdAt: '2027-03-01T00:00:00Z' },
        { id: 2, productId: 1, productName: 'Valam.ai', customerId: 6, customerName: 'Tom', rating: 3, comment: 'It is okay.', status: 'APPROVED', createdAt: '2027-03-02T00:00:00Z' },
      ],
    })
    renderDetail()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('tab', { name: /Reviews/ }))
    expect(await screen.findByText('Excellent!')).toBeInTheDocument()
    expect(screen.getByText('It is okay.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Show only 5-star reviews' }))
    expect(screen.getByText('Excellent!')).toBeInTheDocument()
    expect(screen.queryByText('It is okay.')).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Clear filter' }))
    expect(screen.getByText('It is okay.')).toBeInTheDocument()
  })

  it('lets a signed-in customer submit a review', async () => {
    authState = { isAuthenticated: true }
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    submit.mockResolvedValue({ id: 1, productId: 1, productName: 'Valam.ai', customerId: 5, customerName: 'Jane', rating: 4, status: 'PENDING', createdAt: '2027-03-01T00:00:00Z' })
    renderDetail()

    const user = userEvent.setup()
    await user.click(await screen.findByRole('tab', { name: /Reviews/ }))
    await screen.findByText('Write a review')
    fireEvent.click(screen.getByLabelText('4 Stars'))
    await user.click(await screen.findByRole('button', { name: 'Submit review' }))
    expect(submit).toHaveBeenCalledWith(1, 4, undefined)
    expect(await screen.findByText(/pending approval/)).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    const { container } = renderDetail()
    await screen.findByRole('heading', { name: 'Valam.ai' })
    expect(await axe(container)).toHaveNoViolations()
  })
})

const trialProduct = { ...product, trialDays: 14 }
const trialSubscription = {
  id: 9, productId: 1, productName: 'Valam.ai', status: 'ACTIVE', trial: true, autoRenew: false, expiresAt: '2030-01-15T12:00:00Z',
}
const videoContent = {
  datasheets: [], documentation: [], images: [], caseStudies: [],
  videos: [{ id: 5, kind: 'VIDEO', title: 'Getting started video', description: null, version: 1, updatedAt: '2026-10-07T10:00:00Z',
    file: null, imageUrl: null, altText: null, logoUrl: null, videoProvider: 'EXTERNAL', videoUrl: 'https://example.com/v', embedUrl: null,
    thumbnailUrl: null, customerName: null, problem: null, result: null, articleRef: null, articleType: null, playUrl: null }],
}

function loadProduct(p: object = trialProduct) {
  getProduct.mockResolvedValue(p)
  getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
}

describe('ProductDetailPage: Try it first (C102, REQ-MKT-005)', () => {
  beforeEach(() => {
    for (const m of [getProduct, getRatings, getMine, submit, getContent, startTrial, startOrganizationTrial, requestDemo, cartGet, listMyProducts, listMyOrgProducts]) m.mockReset()
    getContent.mockRejectedValue(new Error('none'))
    getWorksWith.mockReset()
    getWorksWith.mockResolvedValue([])
    getMine.mockRejectedValue(new ApiError(404, 'Review not found'))
    authState = { isAuthenticated: true }
    cartGet.mockResolvedValue({ continueAs: 'INDIVIDUAL' })
    listMyProducts.mockResolvedValue([{ productId: 1, productName: 'Valam.ai', subscriptionStatus: null }])
    listMyOrgProducts.mockResolvedValue([{ productId: 1, productName: 'Valam.ai', orgSubscriptionStatus: null, myAccessAssigned: false }])
  })

  it('offers no free trial when the product sets no trial days, but still a demo', async () => {
    loadProduct(product)
    renderDetail()
    expect(await screen.findByRole('heading', { name: 'Try it first' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Start free trial' })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Request a demo' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Watch tutorials' })).not.toBeInTheDocument()
  })

  it('shows the trial offer and sends a signed-out visitor to sign in', async () => {
    authState = { isAuthenticated: false }
    loadProduct()
    renderDetail()
    expect(await screen.findByText('14-day free trial, no card needed, all features')).toBeInTheDocument()
    expect(screen.getByText('You will be asked to sign in first.')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Start free trial' }))
    expect(await screen.findByText('Sign in page')).toBeInTheDocument()
    expect(startTrial).not.toHaveBeenCalled()
  })

  it('starts the trial for an individual and shows the end date with a link to My subscriptions', async () => {
    loadProduct()
    startTrial.mockResolvedValue(trialSubscription)
    renderDetail()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Start free trial' }))
    expect(startTrial).toHaveBeenCalledWith(1)
    expect(startOrganizationTrial).not.toHaveBeenCalled()
    const status = await screen.findByRole('status')
    expect(status).toHaveTextContent(/Your free trial of Valam\.ai has started and ends on/)
    expect(status).toHaveTextContent(/2030|15/)
    expect(screen.getByRole('button', { name: 'Start free trial' })).toBeDisabled()
    await user.click(screen.getByRole('link', { name: 'View my subscriptions' }))
    expect(await screen.findByText('My subscriptions page')).toBeInTheDocument()
  })

  it('shows the backend message when the trial cannot be started', async () => {
    loadProduct()
    startTrial.mockRejectedValue(new ApiError(400, 'Valam.ai does not offer a free trial.'))
    renderDetail()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Start free trial' }))
    expect(await screen.findByText('Valam.ai does not offer a free trial.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Start free trial' })).toBeEnabled()
  })

  it('disables the trial, with the reason, when the customer already has a subscription to the product', async () => {
    loadProduct()
    listMyProducts.mockResolvedValue([{ productId: 1, productName: 'Valam.ai', subscriptionStatus: 'EXPIRED' }])
    renderDetail()
    expect(await screen.findByText(/You already have, or had, a subscription to this product/)).toBeInTheDocument()
    const button = screen.getByRole('button', { name: 'Start free trial' })
    expect(button).toBeDisabled()
    expect(button).toHaveAccessibleDescription(/already have, or had/)
  })

  it('starts the trial for the organization when the customer may buy for it', async () => {
    loadProduct()
    cartGet.mockResolvedValue({ continueAs: 'ORGANIZATION_BUYER' })
    startOrganizationTrial.mockResolvedValue(trialSubscription)
    renderDetail()
    expect(await screen.findByText('The trial will be for your organization.')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Start free trial' }))
    expect(startOrganizationTrial).toHaveBeenCalledWith(1)
    expect(startTrial).not.toHaveBeenCalled()
  })

  it('disables the trial when the organization already has a subscription', async () => {
    loadProduct()
    cartGet.mockResolvedValue({ continueAs: 'ORGANIZATION_BUYER' })
    listMyOrgProducts.mockResolvedValue([{ productId: 1, productName: 'Valam.ai', orgSubscriptionStatus: 'ACTIVE', myAccessAssigned: true }])
    renderDetail()
    expect(await screen.findByText(/Your organization already has, or had, a subscription/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Start free trial' })).toBeDisabled()
  })

  it('tells an ordinary organization member to ask an administrator', async () => {
    loadProduct()
    cartGet.mockResolvedValue({ continueAs: 'ORGANIZATION_MEMBER' })
    renderDetail()
    expect(await screen.findByText(/Only an organization administrator or a person with Manage billing/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Start free trial' })).toBeDisabled()
  })

  it('shows the organization trial refusal from the backend', async () => {
    loadProduct()
    cartGet.mockResolvedValue({ continueAs: 'ORGANIZATION_BUYER' })
    startOrganizationTrial.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    renderDetail()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Start free trial' }))
    expect(await screen.findByText('You do not have permission to do this')).toBeInTheDocument()
  })

  it('requests a demo: sends the form, shows the ticket number and a link to My tickets', async () => {
    loadProduct(product)
    requestDemo.mockResolvedValue({ id: 321, subject: 'Demo request: Valam.ai', category: 'Demo request' })
    renderDetail()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Request a demo' }))
    const dialog = await screen.findByRole('dialog', { name: 'Request a demo of Valam.ai' })
    await user.type(within(dialog).getByLabelText('Preferred time'), 'Weekdays after 2 pm')
    await user.type(within(dialog).getByLabelText('Message (optional)'), 'Show the dashboards')
    await user.click(within(dialog).getByRole('button', { name: 'Send request' }))
    expect(requestDemo).toHaveBeenCalledWith({ productId: 1, preferredTime: 'Weekdays after 2 pm', message: 'Show the dashboards' })
    expect(await within(dialog).findByText(/We opened ticket #321/)).toBeInTheDocument()
    await user.click(within(dialog).getByRole('link', { name: 'View my tickets' }))
    expect(await screen.findByText('My tickets page')).toBeInTheDocument()
  })

  it('sends a demo request with no optional fields when they are left empty', async () => {
    loadProduct(product)
    requestDemo.mockResolvedValue({ id: 5 })
    renderDetail()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Request a demo' }))
    await user.click(await screen.findByRole('button', { name: 'Send request' }))
    expect(requestDemo).toHaveBeenCalledWith({ productId: 1, preferredTime: undefined, message: undefined })
  })

  it('shows the error and keeps the form when the demo request fails', async () => {
    loadProduct(product)
    requestDemo.mockRejectedValue(new ApiError(404, 'Product not found'))
    renderDetail()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Request a demo' }))
    await user.type(await screen.findByLabelText('Message (optional)'), 'Hello')
    await user.click(screen.getByRole('button', { name: 'Send request' }))
    expect(await screen.findByText('Product not found')).toBeInTheDocument()
    expect(screen.getByLabelText('Message (optional)')).toHaveValue('Hello')
  })

  it('rejects an over-long message before sending', async () => {
    loadProduct(product)
    renderDetail()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Request a demo' }))
    fireEvent.change(await screen.findByLabelText('Message (optional)'), { target: { value: 'x'.repeat(1001) } })
    expect(screen.getByText('Keep this under 1000 characters.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Send request' })).toBeDisabled()
  })

  it('sends a signed-out visitor to sign in to request a demo', async () => {
    authState = { isAuthenticated: false }
    loadProduct(product)
    renderDetail()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Request a demo' }))
    expect(await screen.findByText('Sign in page')).toBeInTheDocument()
  })

  it('shows Watch tutorials only when the product has videos, and opens the Resources tab on its videos', async () => {
    loadProduct(product)
    getContent.mockResolvedValue(videoContent)
    renderDetail()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Watch tutorials' }))
    expect(await screen.findByText('Getting started video')).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: 'Resources' })).toHaveAttribute('aria-selected', 'true')
    expect(document.activeElement).toHaveAttribute('id', 'res-videos')
  })

  it('hides Watch tutorials when published content has no videos', async () => {
    loadProduct(product)
    getContent.mockResolvedValue({ ...videoContent, videos: [], documentation: [{ ...videoContent.videos[0], id: 6, kind: 'DOCUMENTATION' }] })
    renderDetail()
    await screen.findByRole('heading', { name: 'Try it first' })
    expect(screen.queryByRole('button', { name: 'Watch tutorials' })).not.toBeInTheDocument()
  })

  it('has no detectable a11y violations with the trial panel and the demo dialog', async () => {
    loadProduct()
    getContent.mockResolvedValue(videoContent)
    const { container } = renderDetail()
    await screen.findByRole('button', { name: 'Start free trial' })
    expect(await axe(container)).toHaveNoViolations()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Request a demo' }))
    const dialog = await screen.findByRole('dialog')
    expect(await axe(dialog)).toHaveNoViolations()
  })
})
