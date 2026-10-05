import '../../i18n'
import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { TopBarSearch } from './TopBarSearch'

const suggest = vi.fn()
vi.mock('../../api/globalSearchApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/globalSearchApi')>('../../api/globalSearchApi')
  return { ...actual, globalSearchApi: { ...actual.globalSearchApi, suggest: (q: string) => suggest(q) } }
})
const history = vi.fn()
vi.mock('../../api/searchHistoryApi', () => ({ searchHistoryApi: { list: () => history() } }))

function renderSearch() {
  return render(
    <MemoryRouter initialEntries={['/admin']}>
      <Routes>
        <Route path="/admin" element={<><TopBarSearch /><p>Admin page</p></>} />
        <Route path="/search" element={<p>Full search results page</p>} />
        <Route path="/products/:id" element={<p>Product detail page</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('TopBarSearch', () => {
  beforeEach(() => {
    suggest.mockReset()
    history.mockReset()
    history.mockResolvedValue([])
  })

  it('shows suggestions while typing, with the typed text highlighted', async () => {
    suggest.mockResolvedValue([{ type: 'PRODUCT', id: 1, title: 'Valam.ai', reference: '#1' }])
    renderSearch()
    await userEvent.setup().type(screen.getByRole('combobox'), 'valam')
    const option = await screen.findByRole('option', { name: /Valam\.ai/ })
    expect(option.querySelector('mark')?.textContent).toBe('Valam')
    expect(suggest).toHaveBeenLastCalledWith('valam')
  })

  it('clicking a suggestion navigates straight to it', async () => {
    suggest.mockResolvedValue([{ type: 'PRODUCT', id: 1, title: 'Valam.ai', reference: '#1' }])
    renderSearch()
    const user = userEvent.setup()
    await user.type(screen.getByRole('combobox'), 'valam')
    await user.click(await screen.findByRole('option', { name: /Valam\.ai/ }))
    expect(await screen.findByText('Product detail page')).toBeInTheDocument()
  })

  it('arrow keys and Enter open the highlighted suggestion', async () => {
    suggest.mockResolvedValue([{ type: 'PRODUCT', id: 1, title: 'Valam.ai', reference: '#1' }])
    renderSearch()
    const user = userEvent.setup()
    await user.type(screen.getByRole('combobox'), 'valam')
    await screen.findByRole('option', { name: /Valam\.ai/ })
    await user.keyboard('{ArrowDown}')
    expect(screen.getByRole('option', { name: /Valam\.ai/ })).toHaveAttribute('aria-selected', 'true')
    await user.keyboard('{Enter}')
    expect(await screen.findByText('Product detail page')).toBeInTheDocument()
  })

  it('Enter without a highlighted suggestion goes to the full results page', async () => {
    suggest.mockResolvedValue([])
    renderSearch()
    const user = userEvent.setup()
    await user.type(screen.getByRole('combobox'), 'valam{Enter}')
    expect(await screen.findByText('Full search results page')).toBeInTheDocument()
  })

  it('"See all results" goes to the full search page', async () => {
    suggest.mockResolvedValue([])
    renderSearch()
    const user = userEvent.setup()
    await user.type(screen.getByRole('combobox'), 'valam')
    await user.click(await screen.findByText('See all results for "valam"'))
    expect(await screen.findByText('Full search results page')).toBeInTheDocument()
  })

  it('shows recent searches when the empty box is focused', async () => {
    history.mockResolvedValue([{ query: 'invoices', searchedAt: '2026-10-05T10:00:00Z' }])
    renderSearch()
    await userEvent.setup().click(screen.getByRole('combobox'))
    expect(await screen.findByRole('option', { name: 'invoices' })).toBeInTheDocument()
    expect(screen.getByText('Recent searches')).toBeInTheDocument()
  })

  it('Ctrl+K focuses the search box from anywhere', () => {
    renderSearch()
    expect(screen.getByRole('combobox')).not.toHaveFocus()
    fireEvent.keyDown(window, { key: 'k', ctrlKey: true })
    expect(screen.getByRole('combobox')).toHaveFocus()
  })
})
