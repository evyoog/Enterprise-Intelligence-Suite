import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { render } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { TopBarSearch } from './TopBarSearch'

const search = vi.fn()
vi.mock('../../api/globalSearchApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/globalSearchApi')>('../../api/globalSearchApi')
  return { ...actual, globalSearchApi: { ...actual.globalSearchApi, search: (q: string) => search(q) } }
})

function renderSearch() {
  return render(
    <MemoryRouter initialEntries={['/admin']}>
      <Routes>
        <Route path="/admin" element={<TopBarSearch />} />
        <Route path="/search" element={<p>Full search results page</p>} />
        <Route path="/products/:id" element={<p>Product detail page</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('TopBarSearch', () => {
  beforeEach(() => { search.mockReset() })

  it('shows grouped live results while typing', async () => {
    search.mockResolvedValue({
      products: [{ type: 'PRODUCT', id: 1, title: 'Valam.ai', snippet: 'Analytics' }],
      knowledgeArticles: [],
      tickets: [],
    })
    renderSearch()
    await userEvent.setup().type(screen.getByRole('textbox'), 'valam')
    expect(await screen.findByText('Valam.ai')).toBeInTheDocument()
  })

  it('clicking a result navigates straight to it', async () => {
    search.mockResolvedValue({
      products: [{ type: 'PRODUCT', id: 1, title: 'Valam.ai' }],
      knowledgeArticles: [],
      tickets: [],
    })
    renderSearch()
    const user = userEvent.setup()
    await user.type(screen.getByRole('textbox'), 'valam')
    await user.click(await screen.findByText('Valam.ai'))
    expect(await screen.findByText('Product detail page')).toBeInTheDocument()
  })

  it('"See all results" goes to the full search page', async () => {
    search.mockResolvedValue({ products: [], knowledgeArticles: [], tickets: [] })
    renderSearch()
    const user = userEvent.setup()
    await user.type(screen.getByRole('textbox'), 'valam')
    await user.click(await screen.findByText('See all results for "valam"'))
    expect(await screen.findByText('Full search results page')).toBeInTheDocument()
  })
})
