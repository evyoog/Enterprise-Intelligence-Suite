import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { HomePage } from './HomePage'

vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ isAuthenticated: false, isAdmin: false, user: null }) }))

const openRegister = vi.fn()
vi.mock('../auth/AuthModalContext', () => ({ useAuthModal: () => ({ openLogin: vi.fn(), openRegister }) }))

const list = vi.fn()
vi.mock('../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../api/productsApi')>('../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, list: () => list() } }
})

const product = {
  id: 1, name: 'Valam.ai', description: 'AI-powered analytics and business intelligence.', category: 'Analytics',
  price: 0, status: 'ACTIVE' as const, ssoConnected: false, featured: true, platforms: [], plans: [], version: 1, dependsOnProductIds: [],
}

describe('HomePage', () => {
  beforeEach(() => {
    list.mockReset()
    openRegister.mockReset()
  })

  it('lists every product from the catalog with a subscribe button that goes to get-started', async () => {
    list.mockResolvedValue([product])
    renderWithProviders(<HomePage />)

    expect((await screen.findAllByRole('heading', { name: 'Valam.ai', level: 3 })).length).toBeGreaterThan(0)
    expect(screen.getAllByText('Analytics').length).toBeGreaterThan(0)

    const subscribeButtons = screen.getAllByRole('button', { name: 'Subscribe' })
    await userEvent.setup().click(subscribeButtons[0])
    expect(openRegister).toHaveBeenCalled()
  })
})
