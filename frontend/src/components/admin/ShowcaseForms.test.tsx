import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { PlatformForm } from './PlatformForm'
import { ProductForm } from './ProductForm'

const listPlatforms = vi.fn()
const listAdmin = vi.fn()
vi.mock('../../api/platformsApi', () => ({ platformsApi: { list: () => listPlatforms(), uploadImage: vi.fn() } }))
vi.mock('../../api/productsApi', () => ({ productsApi: { listAdmin: () => listAdmin(), uploadImage: vi.fn() } }))

const thiran = { id: 1, name: 'Thiran', primaryColor: '#7C3AED', status: 'ACTIVE', showInCatalog: true, displayOrder: 0 }

describe('PlatformForm (C66)', () => {
  it('updates the live preview and submits the showcase settings', { timeout: 30000 }, async () => {
    const onSubmit = vi.fn().mockResolvedValue({})
    const user = userEvent.setup()
    renderWithProviders(<PlatformForm onSubmit={onSubmit} submitLabel="Create Platform" submittingLabel="Saving…" successMessage="Saved" />)

    const preview = screen.getByRole('complementary', { name: 'Showcase Preview' })
    expect(within(preview).getByRole('heading', { name: 'Platform name' })).toBeInTheDocument()
    await user.type(screen.getByLabelText(/Platform name/), 'Thiran.ai')
    expect(within(preview).getByRole('heading', { name: 'Thiran.ai' })).toBeInTheDocument()

    await user.click(screen.getByRole('radio', { name: 'Custom colour' }))
    await user.click(screen.getByRole('button', { name: 'Green' }))
    expect(screen.getByRole('button', { name: 'Green' })).toHaveAttribute('aria-pressed', 'true')

    await user.click(screen.getByRole('switch', { name: 'Show in catalog' }))
    expect(within(preview).getByText('Hidden from catalog')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Create Platform' }))
    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
      name: 'Thiran.ai', primaryColor: '#10B981', status: 'ACTIVE', showInCatalog: false, displayOrder: 0,
    })))
    expect(await screen.findByText('Saved')).toBeInTheDocument()
  })

  it('shows validation only after the first submit and does not submit invalid data', async () => {
    const onSubmit = vi.fn()
    const user = userEvent.setup()
    renderWithProviders(<PlatformForm onSubmit={onSubmit} submitLabel="Create Platform" submittingLabel="Saving…" successMessage="Saved" />)
    expect(screen.queryByText('This field is required.')).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Create Platform' }))
    expect(screen.getByText('This field is required.')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('has no detectable accessibility violations', async () => {
    const { container } = renderWithProviders(
      <PlatformForm initialPlatform={thiran as never} onSubmit={vi.fn()} submitLabel="Save" submittingLabel="Saving…" successMessage="Saved" previewAppCount={2} />)
    expect(screen.getByText('2 apps')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })
})

describe('ProductForm (C66)', () => {
  beforeEach(() => {
    listPlatforms.mockReset().mockResolvedValue([thiran])
    listAdmin.mockReset().mockResolvedValue([])
  })

  it('keeps the existing payload and adds feature tags, links and the inherited colour', { timeout: 30000 }, async () => {
    const onSubmit = vi.fn().mockResolvedValue({})
    const user = userEvent.setup()
    renderWithProviders(<ProductForm onSubmit={onSubmit} submitLabel="Create App" submittingLabel="Creating…" successMessage="App created." />)

    for (const name of ['Basic information', 'Appearance', 'Launch & availability', 'Integration', 'Product relationships', 'Features', 'Pricing', 'Resources']) {
      expect(screen.getByRole('region', { name })).toBeInTheDocument()
    }
    expect(screen.getByText("Turn this on once the app's own backend has the SSO bridge wired up — it just controls the badge shown on this app's card, it doesn't configure anything itself.")).toBeInTheDocument()
    expect(screen.getByText('Shown only if no pricing tiers are added.')).toBeInTheDocument()

    await user.type(screen.getByLabelText(/App name/), 'Insights')
    await user.type(screen.getByRole('spinbutton', { name: /Price/ }), '49')
    await user.type(screen.getByLabelText('Add a feature'), 'Reports{Enter}')
    await user.type(screen.getByLabelText('Add a feature'), 'reports{Enter}')
    expect(screen.getByText('This feature is already added.')).toBeInTheDocument()
    await user.clear(screen.getByLabelText('Add a feature'))
    await user.type(screen.getByLabelText('Add a feature'), 'Dashboard')
    await user.click(screen.getByRole('button', { name: 'Add' }))
    expect(within(screen.getByRole('list', { name: 'Features' })).getAllByRole('listitem')).toHaveLength(2)

    const preview = screen.getByRole('complementary', { name: 'Showcase Preview' })
    expect(within(preview).getByRole('heading', { name: 'Insights' })).toBeInTheDocument()
    expect(within(preview).getByText('Reports')).toBeInTheDocument()

    await user.type(screen.getByLabelText('Documentation URL'), 'docs.example.com')
    await user.click(screen.getByRole('button', { name: 'Create App' }))
    expect(screen.getByText('Enter a URL starting with http:// or https://.')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()

    await user.clear(screen.getByLabelText('Documentation URL'))
    await user.type(screen.getByLabelText('Documentation URL'), 'https://docs.example.com')
    await user.click(screen.getByRole('button', { name: 'Create App' }))
    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
      name: 'Insights', price: 49, status: 'ACTIVE', ssoConnected: false, featured: false, platformIds: [],
      accentColor: null, featureTags: ['Reports', 'Dashboard'], documentationUrl: 'https://docs.example.com', supportUrl: null,
    })))
  })

  it('requires every pricing tier to be complete', { timeout: 30000 }, async () => {
    const onSubmit = vi.fn()
    const user = userEvent.setup()
    renderWithProviders(<ProductForm onSubmit={onSubmit} submitLabel="Create App" submittingLabel="Creating…" successMessage="ok" />)
    await user.type(screen.getByLabelText(/App name/), 'Insights')
    await user.type(screen.getByRole('spinbutton', { name: /Price/ }), '49')
    await user.click(screen.getByRole('button', { name: 'Add tier' }))
    await user.click(screen.getByRole('button', { name: 'Create App' }))
    expect(screen.getByRole('alert')).toHaveTextContent('Every tier needs a name')
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('keeps a retired app retired and has no detectable accessibility violations', { timeout: 30000 }, async () => {
    const onSubmit = vi.fn().mockResolvedValue({})
    const retired = {
      id: 5, name: 'Legacy', price: 10, status: 'RETIRED', ssoConnected: true, featured: false, platforms: [{ id: 1, name: 'Thiran', primaryColor: '#7C3AED' }],
      plans: [], version: 4, dependsOnProductIds: [], featureTags: ['Old'], accentColor: '#F97316',
    }
    const { container } = renderWithProviders(
      <ProductForm initialProduct={retired as never} onSubmit={onSubmit} submitLabel="Save Changes" submittingLabel="Saving…" successMessage="ok" />)
    expect(screen.getByText(/This app is retired/)).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Save Changes' }))
    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ status: 'RETIRED', accentColor: '#F97316', featureTags: ['Old'] })))
  })
})
