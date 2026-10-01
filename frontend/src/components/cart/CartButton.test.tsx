import '../../i18n'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { CartButton } from './CartButton'
import { CartProvider } from './CartContext'

vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => ({ isAuthenticated: true }) }))
vi.mock('../../api/cartApi', () => ({ cartApi: { get: () => Promise.resolve({ itemCount: 3 }) } }))

describe('CartButton (C59, REQ-MKT-003.7)', () => {
  it('shows the item count in its accessible name and opens the cart', async () => {
    render(
      <MemoryRouter>
        <CartProvider>
          <Routes>
            <Route path="/" element={<CartButton />} />
            <Route path="/cart" element={<div>Cart page</div>} />
          </Routes>
        </CartProvider>
      </MemoryRouter>
    )
    const button = await screen.findByRole('button', { name: 'Cart, 3 items' })
    expect(button).toHaveTextContent('3')
    await userEvent.setup().click(button)
    expect(screen.getByText('Cart page')).toBeInTheDocument()
  })
})
