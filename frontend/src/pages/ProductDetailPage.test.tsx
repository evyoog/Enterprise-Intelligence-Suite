import '../i18n'
import { fireEvent, screen } from '@testing-library/react'
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
          </Routes>
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('ProductDetailPage', () => {
  beforeEach(() => {
    for (const m of [getProduct, getRatings, getMine, submit, getContent]) m.mockReset()
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
