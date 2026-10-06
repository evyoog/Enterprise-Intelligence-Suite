import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { SignedOutRedirect } from './SignedOutRedirect'
import { recentSearches, rememberSearch } from '../components/knowledge/knowledgeUtils'

const mockUseAuth = vi.fn()
vi.mock('./AuthProvider', () => ({ useAuth: () => mockUseAuth() }))

function Where() {
  return <p>at {useLocation().pathname}</p>
}

function ui() {
  return (
    <MemoryRouter initialEntries={['/knowledge/content/setup-guide']}>
      <SignedOutRedirect />
      <Routes><Route path="*" element={<Where />} /></Routes>
    </MemoryRouter>
  )
}

describe('SignedOutRedirect (C79)', () => {
  beforeEach(() => mockUseAuth.mockReset())

  it('opens the home page and forgets recent searches when a session ends anywhere', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true })
    rememberSearch('invoice for acme')
    const { rerender } = render(ui())
    expect(screen.getByText('at /knowledge/content/setup-guide')).toBeInTheDocument()

    // For example signed out in another tab, or the session expired.
    mockUseAuth.mockReturnValue({ isAuthenticated: false })
    rerender(ui())
    expect(screen.getByText('at /')).toBeInTheDocument()
    expect(recentSearches()).toEqual([])
  })

  it('leaves a visitor who was never signed in where they are', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false })
    rememberSearch('pricing')
    render(ui())
    expect(screen.getByText('at /knowledge/content/setup-guide')).toBeInTheDocument()
    expect(recentSearches()).toEqual(['pricing'])
  })
})
