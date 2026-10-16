import '../../i18n'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { makeProfile } from '../../test/profileFixtures'
import { ProfileCompletionBar } from './ProfileCompletionBar'
import { ProfileCompletionContext, useProfileCompletion, type ProfileCompletionState } from './ProfileCompletionContext'
import { ProfileCompletionProvider } from './ProfileCompletionProvider'

const mockUseAuth = vi.fn()
vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => mockUseAuth() }))
const completion = vi.fn()
const photo = vi.fn()
vi.mock('../../api/profileApi', () => ({ profileApi: { completion: () => completion(), photo: () => photo() } }))

const state = (over: Partial<ProfileCompletionState> = {}): ProfileCompletionState => ({
  status: 'ready', exists: true, percent: 45, filled: 5, total: 11, missing: ['photo', 'department'], photoStamp: null, photoUrl: null, ...over,
})

function renderBar(s: ProfileCompletionState, route = '/my/products') {
  return render(
    <MemoryRouter initialEntries={[route]}>
      <ProfileCompletionContext.Provider value={{ state: s, refresh: vi.fn(), report: vi.fn() }}>
        <ProfileCompletionBar />
      </ProfileCompletionContext.Provider>
    </MemoryRouter>,
  )
}

beforeEach(() => {
  sessionStorage.clear()
  mockUseAuth.mockReset().mockReturnValue({ isAuthenticated: true, user: { username: 'ada@example.com' } })
  completion.mockReset()
  photo.mockReset().mockResolvedValue(null)
})

describe('ProfileCompletionBar', () => {
  it('shows the percentage, a progress bar and a link to the profile page', () => {
    renderBar(state())
    expect(screen.getByText('Your profile is 45% complete')).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Your profile is 45% complete' })).toHaveAttribute('aria-valuenow', '45')
    expect(screen.getByRole('link', { name: 'Complete profile' })).toHaveAttribute('href', '/account/profile')
  })

  it.each([
    ['the profile is 100% complete', state({ percent: 100, filled: 11, missing: [] })],
    ['the person has no customer record (an optional profile is never nagged)', state({ exists: false, percent: 0, filled: 0 })],
    ['the status is still loading', state({ status: 'loading' })],
    ['the status could not be read', state({ status: 'error' })],
  ])('is hidden when %s', (_name, s) => {
    renderBar(s)
    expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument()
  })

  it('is hidden on the profile page itself', () => {
    renderBar(state(), '/account/profile')
    expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument()
  })

  it('can be dismissed for the session, per user, and comes back once the session storage is cleared', async () => {
    const first = renderBar(state())
    await userEvent.setup().click(screen.getByRole('button', { name: 'Dismiss for this session' }))
    expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument()
    expect(sessionStorage.getItem('eis.profileBar.dismissed.ada@example.com')).toBe('1')

    // Still dismissed on another page in the same session.
    first.unmount()
    renderBar(state(), '/billing')
    expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument()

    // Another person signing in on the same browser tab sees it.
    mockUseAuth.mockReturnValue({ isAuthenticated: true, user: { username: 'grace@example.com' } })
    renderBar(state(), '/billing')
    expect(screen.getByTestId('profile-completion-bar')).toBeInTheDocument()
  })

  it('returns at the next sign-in (a new browser session has no dismissal)', () => {
    sessionStorage.setItem('eis.profileBar.dismissed.ada@example.com', '1')
    const { unmount } = renderBar(state())
    expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument()
    unmount()
    sessionStorage.clear()
    renderBar(state())
    expect(screen.getByTestId('profile-completion-bar')).toBeInTheDocument()
  })

  it('still shows (undismissed) when session storage is unavailable', async () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => { throw new Error('blocked') })
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => { throw new Error('blocked') })
    renderBar(state())
    expect(screen.getByTestId('profile-completion-bar')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Dismiss for this session' }))
    expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument()
    vi.restoreAllMocks()
  })
})

describe('ProfileCompletionProvider', () => {
  function Probe() {
    const { state: s, refresh, report } = useProfileCompletion()
    return (
      <>
        <p data-testid="probe">{s.status}:{s.exists ? 'exists' : 'none'}:{s.percent}:{s.photoUrl ?? 'no-photo'}</p>
        <button type="button" onClick={() => void refresh()}>refresh</button>
        <button type="button" onClick={() => report(makeProfile({ completion: { percent: 100, filled: 11, total: 11, missing: [] } }))}>report</button>
        <ProfileCompletionBar />
      </>
    )
  }
  const renderProvider = () => render(
    <MemoryRouter initialEntries={['/billing']}><ProfileCompletionProvider><Probe /></ProfileCompletionProvider></MemoryRouter>)

  it('reads GET /me/profile/completion once on sign-in and feeds the bar', async () => {
    completion.mockResolvedValue({ exists: true, percent: 64, filled: 7, total: 11, missing: ['photo', 'city', 'state', 'country'] })
    renderProvider()
    expect(await screen.findByText('Your profile is 64% complete')).toBeInTheDocument()
    expect(screen.getByTestId('probe')).toHaveTextContent('ready:exists:64:no-photo')
    expect(completion).toHaveBeenCalledTimes(1)
  })

  it('hides the bar for a person with no record and does not poll', async () => {
    completion.mockResolvedValue({ exists: false, percent: 0, filled: 0, total: 11, missing: [] })
    renderProvider()
    await waitFor(() => expect(screen.getByTestId('probe')).toHaveTextContent('ready:none:0'))
    expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument()
    expect(completion).toHaveBeenCalledTimes(1)
  })

  it('updates when the profile page reports a save, and when asked to refresh', async () => {
    completion.mockResolvedValueOnce({ exists: true, percent: 64, filled: 7, total: 11, missing: ['photo'] })
    renderProvider()
    await screen.findByText('Your profile is 64% complete')
    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: 'report' }))
    await waitFor(() => expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument())
    completion.mockResolvedValueOnce({ exists: true, percent: 82, filled: 9, total: 11, missing: ['photo', 'city'] })
    await user.click(screen.getByRole('button', { name: 'refresh' }))
    expect(await screen.findByText('Your profile is 82% complete')).toBeInTheDocument()
  })

  it('keeps the bar hidden when the status cannot be read', async () => {
    completion.mockRejectedValue(new Error('offline'))
    renderProvider()
    await waitFor(() => expect(screen.getByTestId('probe')).toHaveTextContent('error'))
    expect(screen.queryByTestId('profile-completion-bar')).not.toBeInTheDocument()
  })

  it('loads the photo as an object URL when the person has one, and revokes it afterwards', async () => {
    URL.createObjectURL = vi.fn(() => 'blob:me')
    URL.revokeObjectURL = vi.fn()
    completion.mockResolvedValue({ exists: true, percent: 100, filled: 11, total: 11, missing: [] })
    photo.mockResolvedValue(new Blob(['x'], { type: 'image/webp' }))
    const { unmount } = renderProvider()
    await waitFor(() => expect(screen.getByTestId('probe')).toHaveTextContent('blob:me'))
    unmount()
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:me')
  })

  it('does not fetch a photo when the completion says there is none', async () => {
    completion.mockResolvedValue({ exists: true, percent: 64, filled: 7, total: 11, missing: ['photo'] })
    renderProvider()
    await screen.findByText('Your profile is 64% complete')
    expect(photo).not.toHaveBeenCalled()
  })
})
