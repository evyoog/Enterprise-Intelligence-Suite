import '../../i18n'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { LocalePreferenceProvider } from '../../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../../theming/ThemeModeProvider'
import { EditProductPage } from './EditProductPage'

const product = {
  id: 7, name: 'Thiran Suite', description: 'd', price: 0, status: 'ACTIVE' as const, ssoConnected: false, featured: false,
  platforms: [], plans: [], version: 4, dependsOnProductIds: [],
}
vi.mock('../../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/productsApi')>('../../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, get: () => Promise.resolve(product) } }
})
vi.mock('../../components/admin/ProductForm', () => ({ ProductForm: () => <p>Details form</p> }))
vi.mock('../../components/productcontent/ProductContentManager', () => ({
  ProductContentManager: ({ productId }: { productId: number }) => <p>Content manager for {productId}</p>,
}))

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <Routes><Route path="/admin/products/:id/edit" element={<EditProductPage />} /></Routes>
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>,
  )
}

describe('Edit application: Details and Content tabs (REQ-CAT-004)', () => {
  it('opens on the details form and switches to the product content', async () => {
    renderAt('/admin/products/7/edit')
    expect(await screen.findByText('Details form')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Content' }))
    expect(screen.getByText('Content manager for 7')).toBeInTheDocument()
    expect(screen.queryByText('Details form')).not.toBeInTheDocument()
  })

  it('opens the Content tab straight from ?tab=content (the list action and refresh)', async () => {
    renderAt('/admin/products/7/edit?tab=content')
    expect(await screen.findByText('Content manager for 7')).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: 'Content' })).toHaveAttribute('aria-selected', 'true')
  })
})
