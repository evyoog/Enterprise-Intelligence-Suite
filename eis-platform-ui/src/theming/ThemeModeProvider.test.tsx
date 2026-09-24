import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { ThemeModeProvider, useThemeMode } from './ThemeModeProvider'

/** A minimal real consumer — exercises the actual context, not a mock of it. */
function Consumer() {
  const { mode, resolvedMode, setMode, reducedMotion, setReducedMotion } = useThemeMode()
  return (
    <div>
      <span data-testid="mode">{mode}</span>
      <span data-testid="resolved">{resolvedMode}</span>
      <span data-testid="reduced">{String(reducedMotion)}</span>
      <button onClick={() => setMode('dark')}>dark</button>
      <button onClick={() => setMode('light')}>light</button>
      <button onClick={() => setReducedMotion(true)}>reduce</button>
    </div>
  )
}

describe('ThemeModeProvider', () => {
  it('defaults to system mode and persists an explicit choice', async () => {
    localStorage.clear()
    const user = userEvent.setup()
    render(<ThemeModeProvider><Consumer /></ThemeModeProvider>)

    expect(screen.getByTestId('mode')).toHaveTextContent('system')

    await user.click(screen.getByText('dark'))
    expect(screen.getByTestId('mode')).toHaveTextContent('dark')
    expect(screen.getByTestId('resolved')).toHaveTextContent('dark')
    expect(localStorage.getItem('vyoog-theme-mode')).toBe('dark')
  })

  it('applies the reduced-motion preference to <html> for tiles.css to key off', async () => {
    localStorage.clear()
    const user = userEvent.setup()
    render(<ThemeModeProvider><Consumer /></ThemeModeProvider>)

    expect(document.documentElement.getAttribute('data-reduced-motion')).toBe('false')
    await user.click(screen.getByText('reduce'))
    expect(screen.getByTestId('reduced')).toHaveTextContent('true')
    expect(document.documentElement.getAttribute('data-reduced-motion')).toBe('true')
    expect(localStorage.getItem('vyoog-reduced-motion')).toBe('true')
  })

  it('reads a previously-stored mode back on a fresh mount', () => {
    localStorage.setItem('vyoog-theme-mode', 'light')
    render(<ThemeModeProvider><Consumer /></ThemeModeProvider>)
    expect(screen.getByTestId('mode')).toHaveTextContent('light')
  })

  it('throws a clear error if used outside the provider', () => {
    function Bare() {
      useThemeMode()
      return null
    }
    expect(() => render(<Bare />)).toThrow(/useThemeMode must be used within/)
  })
})
