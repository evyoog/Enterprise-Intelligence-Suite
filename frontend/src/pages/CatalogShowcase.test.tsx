import '../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { ProductsPage } from './ProductsPage'
import { PlatformDetailPage } from './PlatformDetailPage'

const search = vi.fn()
const list = vi.fn()
const listPlatforms = vi.fn()
const getPlatform = vi.fn()
let isAdmin = false

vi.mock('../api/productsApi', () => ({ productsApi: { search: (p: unknown) => search(p), list: () => list() } }))
vi.mock('../api/catalogApi', () => ({ catalogApi: { listPlatforms: () => listPlatforms(), getPlatform: (id: number) => getPlatform(id) } }))
vi.mock('../api/searchHistoryApi', () => ({ searchHistoryApi: { list: () => Promise.resolve([]), record: () => Promise.resolve(), clear: () => Promise.resolve() } }))
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ isAuthenticated: false, isAdmin, user: null, logout: vi.fn() }) }))
vi.mock('../auth/AuthModalContext', () => ({ useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }) }))
vi.mock('../components/layout/SiteNavbar', () => ({ SiteNavbar: () => <nav aria-label="Site" /> }))

const insights = {
  id: 11, name: 'Insights', description: 'Reports for every team.', price: 49, category: 'Analytics', status: 'ACTIVE', ssoConnected: true,
  featured: true, platforms: [{ id: 1, name: 'Thiran', primaryColor: '#7C3AED' }], plans: [], version: 3, dependsOnProductIds: [],
  featureTags: ['Reports', 'Dashboard'], launchUrl: 'https://insights.example.com',
}
const planner = { ...insights, id: 12, name: 'Planner', category: 'Planning', featured: false, featureTags: [], launchUrl: undefined, platforms: [] }
const thiran = { id: 1, name: 'Thiran', description: 'Business operations platform.', primaryColor: '#7C3AED', appCount: 1, categories: ['Analytics'], featureTags: ['Reports', 'Dashboard'] }

describe('Product Catalog (C66)', () => {
  beforeEach(() => {
    isAdmin = false
    search.mockReset().mockResolvedValue({ items: [insights, planner], facets: { categories: [{ category: 'Analytics', count: 1 }, { category: 'Planning', count: 1 }], platforms: [] } })
    list.mockReset().mockResolvedValue([insights, planner])
    listPlatforms.mockReset().mockResolvedValue([thiran])
    getPlatform.mockReset().mockResolvedValue({ ...thiran, apps: [insights] })
  })

  it('shows real counts, platform cards and app cards', async () => {
    renderWithProviders(<ProductsPage />)
    expect(await screen.findByRole('heading', { name: 'Thiran' })).toBeInTheDocument()
    expect(screen.getByText('1 app')).toBeInTheDocument()
    expect(screen.getByText('Across 2 categories')).toBeInTheDocument()
    const products = screen.getByRole('region', { name: /Products/ })
    expect(within(products).getByRole('link', { name: 'View details: Thiran' })).toHaveAttribute('href', '/catalog/platforms/1')
    expect(screen.getByRole('heading', { name: 'Insights' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Launch Insights (opens in a new tab)' })).toHaveAttribute('href', 'https://insights.example.com')
    expect(screen.getByRole('link', { name: 'Contact Support' })).toHaveAttribute('href', '/support/tickets')
    expect(screen.queryByRole('link', { name: 'Add Product' })).not.toBeInTheDocument()
  })

  it('filters by category and offers Add Product to administrators', async () => {
    isAdmin = true
    renderWithProviders(<ProductsPage />)
    expect(await screen.findByRole('link', { name: 'Add Product' })).toHaveAttribute('href', '/admin/platforms/new')
    await userEvent.setup().click(screen.getByRole('button', { name: 'Planning (1)' }))
    expect(search).toHaveBeenLastCalledWith(expect.objectContaining({ category: 'Planning' }))
    // Thiran has no Planning apps, so its card is filtered out.
    expect(screen.queryByRole('heading', { name: 'Thiran' })).not.toBeInTheDocument()
  })

  it('shows an empty state with Clear filters', async () => {
    search.mockResolvedValue({ items: [], facets: { categories: [], platforms: [] } })
    renderWithProviders(<ProductsPage />)
    expect(await screen.findByText('No products found')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<ProductsPage />)
    await screen.findByRole('heading', { name: 'Thiran' })
    expect(await axe(container)).toHaveNoViolations()
  })
})

describe('Platform details (C66)', () => {
  beforeEach(() => {
    getPlatform.mockReset().mockResolvedValue({ ...thiran, apps: [insights] })
  })

  it('shows the overview, applications, features and information', async () => {
    const { container } = renderWithProviders(
      <Routes><Route path="/catalog/platforms/:id" element={<PlatformDetailPage />} /></Routes>,
      { route: '/catalog/platforms/1' },
    )
    expect(await screen.findByRole('heading', { name: 'Thiran', level: 4 })).toBeInTheDocument()
    expect(getPlatform).toHaveBeenCalledWith(1)
    expect(screen.getByText('Business operations platform.')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Insights' })).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Features' })).toHaveTextContent('Dashboard')
    expect(screen.getByRole('link', { name: 'Contact Support' })).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })
})
