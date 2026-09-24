import '../i18n'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { LocalePreferenceProvider } from '../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../theming/ThemeModeProvider'
import { ProductsPage } from './ProductsPage'

const mockSearch = vi.fn().mockResolvedValue({ items: [], facets: { categories: [], platforms: [] } })
const mockList = vi.fn()
const mockRecord = vi.fn().mockResolvedValue(undefined)
const mockClear = vi.fn().mockResolvedValue(undefined)

vi.mock('../api/productsApi', () => ({
  productsApi: { search: (...args: unknown[]) => mockSearch(...args), list: vi.fn().mockResolvedValue([]) },
}))

vi.mock('../api/searchHistoryApi', () => ({
  searchHistoryApi: {
    list: () => mockList(),
    record: (q: string) => mockRecord(q),
    clear: () => mockClear(),
  },
}))

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@example.com' }, logout: vi.fn() }),
}))

vi.mock('../auth/AuthModalContext', () => ({
  useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }),
}))

function renderPage() {
  return render(
    <MemoryRouter>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <ProductsPage />
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('ProductsPage — search history "Clear" control (Phase 9)', () => {
  beforeEach(() => {
    mockSearch.mockClear()
    mockList.mockReset().mockResolvedValue([{ query: 'planner', searchedAt: new Date().toISOString() }])
    mockRecord.mockClear()
    mockClear.mockClear()
  })

  it('shows previously-recorded searches and lets the user clear them, calling the real backend endpoint', async () => {
    const user = userEvent.setup()
    renderPage()

    await waitFor(() => expect(mockList).toHaveBeenCalled())

    const searchBox = screen.getByPlaceholderText(/search products/i)
    await user.click(searchBox)

    const recentChip = await screen.findByText('planner')
    expect(recentChip).toBeInTheDocument()

    const clearButton = screen.getByRole('button', { name: 'Clear' })
    await user.click(clearButton)

    expect(mockClear).toHaveBeenCalledTimes(1)
    await waitFor(() => expect(screen.queryByText('planner')).not.toBeInTheDocument())
  })
})
