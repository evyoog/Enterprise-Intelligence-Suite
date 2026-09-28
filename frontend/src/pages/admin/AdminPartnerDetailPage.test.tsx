import '../../i18n'
import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { LocalePreferenceProvider } from '../../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../../theming/ThemeModeProvider'
import { AdminPartnerDetailPage } from './AdminPartnerDetailPage'

const get = vi.fn()
const verify = vi.fn()
const approve = vi.fn()
const activate = vi.fn()
const reject = vi.fn()
const getContract = vi.fn()
const saveContract = vi.fn()

vi.mock('../../api/partnersApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/partnersApi')>('../../api/partnersApi')
  return {
    ...actual,
    adminPartnersApi: {
      ...actual.adminPartnersApi,
      get: (id: number) => get(id),
      verify: (id: number) => verify(id),
      approve: (id: number) => approve(id),
      activate: (id: number) => activate(id),
      reject: (id: number) => reject(id),
      getContract: (id: number) => getContract(id),
      saveContract: (id: number, r: unknown) => saveContract(id, r),
    },
  }
})

const provider = {
  id: 1, name: 'Acme Cloud', contactName: 'Jane Doe', contactEmail: 'jane@acme.example',
  status: 'REGISTERED' as const, createdAt: '2027-04-01T00:00:00Z',
}

function renderDetail() {
  return render(
    <MemoryRouter initialEntries={['/admin/partners/1']}>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <Routes>
            <Route path="/admin/partners/:id" element={<AdminPartnerDetailPage />} />
          </Routes>
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('AdminPartnerDetailPage', () => {
  beforeEach(() => {
    for (const m of [get, verify, approve, activate, reject, getContract, saveContract]) m.mockReset()
    getContract.mockRejectedValue(new ApiError(404, 'No contract on file for this provider'))
  })

  it('verifies a registered provider', async () => {
    get.mockResolvedValue(provider)
    verify.mockResolvedValue({ ...provider, status: 'VERIFIED' })
    renderDetail()

    await screen.findByText('Acme Cloud')
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Verify' }))
    expect(verify).toHaveBeenCalledWith(1)
    expect(await screen.findByText('Verified')).toBeInTheDocument()
  })

  it('does not offer a next-stage action once active', async () => {
    get.mockResolvedValue({ ...provider, status: 'ACTIVE' });
    renderDetail()
    await screen.findByText('Acme Cloud')
    expect(screen.queryByRole('button', { name: 'Verify' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Approve' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Activate' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Reject' })).not.toBeInTheDocument()
  })

  it('saves a contract for the provider', async () => {
    get.mockResolvedValue(provider)
    saveContract.mockResolvedValue({ id: 1, providerId: 1, terms: 'Standard terms', startDate: '2027-04-01', endDate: '2028-04-01', status: 'ACTIVE' })
    renderDetail()

    await screen.findByText('Acme Cloud')
    const user = userEvent.setup()
    await user.type(screen.getByLabelText('Terms'), 'Standard terms')
    fireEvent.change(screen.getByLabelText('Start date'), { target: { value: '2027-04-01' } })
    fireEvent.change(screen.getByLabelText('End date'), { target: { value: '2028-04-01' } })
    await user.click(screen.getByRole('button', { name: 'Save contract' }))

    expect(saveContract).toHaveBeenCalledWith(1, { terms: 'Standard terms', startDate: '2027-04-01', endDate: '2028-04-01' })
    expect(await screen.findByText('Active')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    get.mockResolvedValue(provider)
    const { container } = renderDetail()
    await screen.findByText('Acme Cloud')
    expect(await axe(container)).toHaveNoViolations()
  })
})
