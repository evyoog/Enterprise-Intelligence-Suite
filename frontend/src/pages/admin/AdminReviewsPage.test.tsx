import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminReviewsPage } from './AdminReviewsPage'

const listAll = vi.fn()
const approve = vi.fn()
const reject = vi.fn()

vi.mock('../../api/reviewsApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/reviewsApi')>('../../api/reviewsApi')
  return {
    ...actual,
    adminReviewsApi: {
      ...actual.adminReviewsApi,
      listAll: () => listAll(),
      approve: (id: number) => approve(id),
      reject: (id: number) => reject(id),
    },
  }
})

const pendingReview = {
  id: 1, productId: 1, productName: 'Valam.ai', customerId: 5, customerName: 'Jane Customer',
  rating: 4, comment: 'Pretty good.', status: 'PENDING' as const, createdAt: '2027-03-01T00:00:00Z',
}

describe('AdminReviewsPage', () => {
  beforeEach(() => {
    for (const m of [listAll, approve, reject]) m.mockReset()
  })

  it('approves a pending review', async () => {
    listAll.mockResolvedValue([pendingReview])
    approve.mockResolvedValue({ ...pendingReview, status: 'APPROVED' })
    renderWithProviders(<AdminReviewsPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Approve' }))
    expect(approve).toHaveBeenCalledWith(1)
  })

  it('rejects a pending review', async () => {
    listAll.mockResolvedValue([pendingReview])
    reject.mockResolvedValue({ ...pendingReview, status: 'REJECTED' })
    renderWithProviders(<AdminReviewsPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Reject' }))
    expect(reject).toHaveBeenCalledWith(1)
  })

  it('has no detectable a11y violations', async () => {
    listAll.mockResolvedValue([pendingReview])
    const { container } = renderWithProviders(<AdminReviewsPage />)
    await screen.findByText('Pretty good.')
    expect(await axe(container)).toHaveNoViolations()
  })
})
