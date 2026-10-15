import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { renderWithProviders } from '../test/renderWithProviders'
import { MyAccessRequestsPage } from './MyAccessRequestsPage'

const myRequests = vi.fn()
const cancel = vi.fn()

vi.mock('../api/accessRequestApi', async () => {
  const actual = await vi.importActual<typeof import('../api/accessRequestApi')>('../api/accessRequestApi')
  return {
    ...actual,
    accessRequestApi: {
      ...actual.accessRequestApi,
      myRequests: () => myRequests(),
      cancel: (id: number) => cancel(id),
    },
  }
})

const pendingRequest = {
  id: 1, productId: 10, productName: 'Valam.ai', memberId: 5, memberName: 'Jane Member',
  status: 'PENDING' as const, message: 'Need it for Q1', createdAt: '2027-01-05T00:00:00Z',
}

describe('MyAccessRequestsPage', () => {
  beforeEach(() => {
    myRequests.mockReset()
    cancel.mockReset()
  })

  it('shows an empty state when there are no requests', async () => {
    myRequests.mockResolvedValue([])
    renderWithProviders(<MyAccessRequestsPage />)
    expect(await screen.findByText('You have not requested access to any product.')).toBeInTheDocument()
  })

  it('lists requests and cancels a pending one', async () => {
    myRequests.mockResolvedValue([pendingRequest])
    cancel.mockResolvedValue({ ...pendingRequest, status: 'CANCELLED' })
    renderWithProviders(<MyAccessRequestsPage />)

    expect(await screen.findByText('Valam.ai')).toBeInTheDocument()
    expect(screen.getByText('Need it for Q1')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Cancel request' }))
    expect(cancel).toHaveBeenCalledWith(1)
  })

  it('shows the rejection note for a rejected request', async () => {
    myRequests.mockResolvedValue([{ ...pendingRequest, status: 'REJECTED', decisionNote: 'No seats left' }])
    renderWithProviders(<MyAccessRequestsPage />)
    expect(await screen.findByText('Note: No seats left')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Cancel request' })).not.toBeInTheDocument()
  })

  it('shows the backend message when requests cannot be loaded', async () => {
    myRequests.mockRejectedValue(new ApiError(500, 'Something went wrong'))
    renderWithProviders(<MyAccessRequestsPage />)
    expect(await screen.findByText(/Something went wrong/)).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    myRequests.mockResolvedValue([pendingRequest])
    const { container } = renderWithProviders(<MyAccessRequestsPage />)
    await screen.findByText('Valam.ai')
    expect(await axe(container)).toHaveNoViolations()
  })
})
