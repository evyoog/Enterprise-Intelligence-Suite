import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { RequireAuth } from './RequireAuth'

const mockUseAuth = vi.fn()
vi.mock('../../auth/AuthProvider', () => ({
  useAuth: () => mockUseAuth(),
}))

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/" element={<div>Home page</div>} />
        <Route path="/account/security" element={<RequireAuth><div>Security page</div></RequireAuth>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('RequireAuth', () => {
  it('redirects an unauthenticated visitor to home instead of rendering the page', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false })
    renderAt('/account/security')
    expect(screen.getByText('Home page')).toBeInTheDocument()
    expect(screen.queryByText('Security page')).not.toBeInTheDocument()
  })

  it('renders the page for an authenticated visitor', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true })
    renderAt('/account/security')
    expect(screen.getByText('Security page')).toBeInTheDocument()
  })
})
