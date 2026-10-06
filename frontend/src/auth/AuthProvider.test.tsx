import { act, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider, useAuth } from './AuthProvider'

const session = vi.fn()
const logout = vi.fn()
vi.mock('../api/authApi', () => ({
  authApi: { session: () => session(), logout: () => logout() },
}))

// A token whose payload is {"preferred_username":"ada"}.
const TOKEN = `x.${btoa(JSON.stringify({ preferred_username: 'ada' }))}.y`

function deferred<T>() {
  let resolve!: (v: T) => void
  let reject!: (e: unknown) => void
  const promise = new Promise<T>((res, rej) => { resolve = res; reject = rej })
  return { promise, resolve, reject }
}

function Probe() {
  const auth = useAuth()
  if (auth.isBootstrapping) return <p>loading</p>
  return (
    <>
      <p>{auth.isAuthenticated ? `signed in as ${auth.user?.username}` : 'signed out'}</p>
      <button type="button" onClick={auth.logout}>Sign out</button>
    </>
  )
}

describe('AuthProvider sign-out (C79)', () => {
  beforeEach(() => {
    session.mockReset()
    logout.mockReset()
  })

  it('a session check already running when the user signs out cannot sign them back in', async () => {
    session.mockResolvedValueOnce({ accessToken: TOKEN })
    render(<AuthProvider><Probe /></AuthProvider>)
    expect(await screen.findByText('signed in as ada')).toBeInTheDocument()

    // A focus-triggered check starts, then the user signs out before it answers.
    const late = deferred<{ accessToken: string }>()
    session.mockReturnValueOnce(late.promise)
    const serverLogout = deferred<void>()
    logout.mockReturnValueOnce(serverLogout.promise)
    act(() => { window.dispatchEvent(new Event('focus')) })
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }))
    expect(screen.getByText('signed out')).toBeInTheDocument()

    await act(async () => { late.resolve({ accessToken: TOKEN }) })
    expect(screen.getByText('signed out')).toBeInTheDocument()

    // A check that starts while the server logout is still running waits for it,
    // so the old session cookie is never read back.
    session.mockRejectedValue(new Error('401'))
    act(() => { window.dispatchEvent(new Event('focus')) })
    expect(session).toHaveBeenCalledTimes(2)
    await act(async () => { serverLogout.resolve() })
    await waitFor(() => expect(session).toHaveBeenCalledTimes(3))
    expect(screen.getByText('signed out')).toBeInTheDocument()
  })
})
