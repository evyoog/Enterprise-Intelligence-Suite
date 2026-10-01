import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminBillingSettingsPage } from './AdminBillingSettingsPage'

const offlineBankDetails = vi.fn()
const saveOfflineBankDetails = vi.fn()

vi.mock('../../api/billingApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/billingApi')>('../../api/billingApi')
  return {
    ...actual,
    adminBillingApi: {
      ...actual.adminBillingApi,
      offlineBankDetails: () => offlineBankDetails(),
      saveOfflineBankDetails: (body: unknown) => saveOfflineBankDetails(body),
    },
  }
})

describe('AdminBillingSettingsPage (C55)', () => {
  beforeEach(() => {
    offlineBankDetails.mockReset()
    saveOfflineBankDetails.mockReset()
    offlineBankDetails.mockResolvedValue({})
  })

  it('shows the empty state and keeps Save disabled until something changes', async () => {
    const { container } = renderWithProviders(<AdminBillingSettingsPage />)
    expect(await screen.findByText(/No bank details yet/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('validates and saves the bank details', async () => {
    saveOfflineBankDetails.mockImplementation(async (body) => ({ ...body, updatedAt: '2026-10-01T00:00:00Z' }))
    renderWithProviders(<AdminBillingSettingsPage />)
    const user = userEvent.setup()
    await user.type(await screen.findByLabelText(/Account name/), 'eVyoog Pvt Ltd')
    expect(screen.getByText('You have unsaved changes.')).toBeInTheDocument()
    await user.type(screen.getByLabelText(/IFSC/), 'SHORT')
    await user.click(screen.getByRole('button', { name: 'Save' }))
    expect(screen.getByText('IFSC must be 11 characters.')).toBeInTheDocument()
    expect(screen.getAllByText('This field is required.')).toHaveLength(2)
    expect(saveOfflineBankDetails).not.toHaveBeenCalled()

    await user.clear(screen.getByLabelText(/IFSC/))
    await user.type(screen.getByLabelText(/IFSC/), 'DEMO0001234')
    await user.type(screen.getByLabelText(/Bank name/), 'Demo Bank')
    await user.type(screen.getByLabelText(/Account number/), '0011223344')
    await user.click(screen.getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(saveOfflineBankDetails).toHaveBeenCalledWith({
      accountName: 'eVyoog Pvt Ltd', bankName: 'Demo Bank', accountNumber: '0011223344', ifsc: 'DEMO0001234', swiftBic: undefined,
    }))
    expect(await screen.findByText('Bank details saved.')).toBeInTheDocument()
    expect(screen.queryByText('You have unsaved changes.')).not.toBeInTheDocument()
  })

  it('shows a load error with Retry', async () => {
    offlineBankDetails.mockRejectedValueOnce(new Error('boom'))
    renderWithProviders(<AdminBillingSettingsPage />)
    expect(await screen.findByText('Could not load the bank details.')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByText(/No bank details yet/)).toBeInTheDocument()
  })
})
