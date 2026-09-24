import '../i18n'
import { render, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { LocalePreferenceProvider } from '../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../theming/ThemeModeProvider'
import { BusinessDashboardPage } from './BusinessDashboardPage'

const mockNavigate = vi.fn()
const mockGet = vi.fn()

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

vi.mock('../api/businessDashboardApi', () => ({
  businessDashboardApi: { get: () => mockGet() },
}))

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@example.com' }, logout: vi.fn() }),
}))

vi.mock('../auth/AuthModalContext', () => ({
  useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }),
}))

vi.mock('../api/productsApi', () => ({
  productsApi: { list: vi.fn().mockResolvedValue([]) },
}))

function renderPage() {
  return render(
    <MemoryRouter>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <BusinessDashboardPage />
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('BusinessDashboardPage — routes the wrong audience to their own dashboard', () => {
  beforeEach(() => {
    mockNavigate.mockReset()
    mockGet.mockReset()
  })

  it('redirects to /my/products on 403 (an org member without MANAGE_ORGANIZATION)', async () => {
    mockGet.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    renderPage()
    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/my/products', { replace: true }))
  })

  it('redirects to /my/products on 404 (an individual customer with no organization at all)', async () => {
    mockGet.mockRejectedValue(new ApiError(404, 'You are not a member of an organization'))
    renderPage()
    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/my/products', { replace: true }))
  })

  it('does not redirect on a genuine server error — shows it instead', async () => {
    mockGet.mockRejectedValue(new ApiError(500, 'Something broke'))
    const { findByText } = renderPage()
    await findByText('Something broke')
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})
