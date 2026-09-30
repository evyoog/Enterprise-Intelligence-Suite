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
            <Route path="/checkout/:productId" element={<div>Checkout page</div>} />
          </Routes>
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('ProductDetailPage', () => {
  beforeEach(() => {
    for (const m of [getProduct, getRatings, getMine, submit]) m.mockReset()
    authState = { isAuthenticated: false }
    getMine.mockRejectedValue(new ApiError(404, 'Review not found'))
  })

  it('sends a signed-out visitor to sign in first, with a way back to checkout', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    renderDetail()

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Subscribe' }))
    expect(await screen.findByText('Sign in page')).toBeInTheDocument()
  })

  it('sends a signed-in customer straight to checkout', async () => {
    authState = { isAuthenticated: true }
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    renderDetail()

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Subscribe' }))
    expect(await screen.findByText('Checkout page')).toBeInTheDocument()
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
    expect(await screen.findByText('Great!')).toBeInTheDocument()
  })

  it('shows a no-reviews message when there are none', async () => {
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    renderDetail()
    expect(await screen.findByText('No reviews yet.')).toBeInTheDocument()
  })

  it('lets a signed-in customer submit a review', async () => {
    authState = { isAuthenticated: true }
    getProduct.mockResolvedValue(product)
    getRatings.mockResolvedValue({ averageRating: null, reviewCount: 0, reviews: [] })
    submit.mockResolvedValue({ id: 1, productId: 1, productName: 'Valam.ai', customerId: 5, customerName: 'Jane', rating: 4, status: 'PENDING', createdAt: '2027-03-01T00:00:00Z' })
    renderDetail()

    const user = userEvent.setup()
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
    await screen.findByText('Valam.ai')
    expect(await axe(container)).toHaveNoViolations()
  })
})
