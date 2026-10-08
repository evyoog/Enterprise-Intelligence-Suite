import '../i18n'
import { screen } from '@testing-library/react'
import { axe } from 'jest-axe'
import { Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { OfferingDetailPage, OfferingsPage } from './OfferingsPage'

const list = vi.fn()
const get = vi.fn()
vi.mock('../api/offeringsApi', () => ({ offeringsApi: { list: () => list(), get: (id: number) => get(id) } }))

const offering = {
  id: 7, name: 'Analytics pack', description: 'Reports and planning', status: 'ACTIVE' as const,
  products: [{ id: 1, name: 'Valam.ai', status: 'ACTIVE' }, { id: 2, name: 'Varthan.ai', status: 'ACTIVE' }],
  createdAt: '2026-10-08T00:00:00Z', updatedAt: '2026-10-08T00:00:00Z',
}

describe('OfferingsPage', () => {
  beforeEach(() => { list.mockReset(); get.mockReset() })

  it('lists published offerings with their products', async () => {
    list.mockResolvedValue([offering])
    renderWithProviders(<OfferingsPage />)
    expect(await screen.findByRole('link', { name: 'Analytics pack' })).toHaveAttribute('href', '/offerings/7')
    expect(screen.getByText('Valam.ai')).toBeInTheDocument()
    expect(screen.getByText('Varthan.ai')).toBeInTheDocument()
  })

  it('says so when there are none', async () => {
    list.mockResolvedValue([])
    renderWithProviders(<OfferingsPage />)
    expect(await screen.findByText('No offerings are available yet.')).toBeInTheDocument()
  })

  it('shows one offering and links each product to its own page', async () => {
    get.mockResolvedValue(offering)
    renderWithProviders(
      <Routes><Route path="/offerings/:id" element={<OfferingDetailPage />} /></Routes>, { route: '/offerings/7' })
    expect(await screen.findByText('Included products')).toBeInTheDocument()
    expect(screen.getAllByRole('link', { name: 'View product' })[0]).toHaveAttribute('href', '/products/1')
    expect(get).toHaveBeenCalledWith(7)
  })

  it('has no detectable a11y violations', async () => {
    list.mockResolvedValue([offering])
    const { container } = renderWithProviders(<OfferingsPage />)
    await screen.findByText('Analytics pack')
    expect(await axe(container)).toHaveNoViolations()
  })
})
