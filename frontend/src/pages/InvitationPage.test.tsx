import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { renderWithProviders } from '../test/renderWithProviders'
import { InvitationPage } from './InvitationPage'

const preview = vi.fn()
const accept = vi.fn()
const decline = vi.fn()
const createAccount = vi.fn()
const login = vi.fn()
const signOut = vi.fn()
const mockAuth = vi.fn()

vi.mock('../api/invitationsApi', () => ({
  invitationsApi: {
    preview: () => preview(), accept: (t: string) => accept(t), decline: (t: string) => decline(t),
    createAccount: (t: string, i: unknown) => createAccount(t, i),
  },
}))
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => mockAuth() }))
vi.mock('../auth/useSignOut', () => ({ useSignOut: () => signOut }))

const pending = {
  status: 'PENDING', email: 'sam@acme.example', organizationName: 'Acme', inviterName: 'Asha Rao', orgRole: 'MEMBER',
  orgNodeName: 'Acme › Ops', expiresAt: '2026-10-15T00:00:00Z', accountExists: true, organizationAvailable: true,
}
const signedOut = { isAuthenticated: false, user: null, login }
const signedInAs = (email: string) => ({ isAuthenticated: true, user: { username: email, email, roles: [] }, login })

const renderPage = () => renderWithProviders(
  <Routes><Route path="/invitations/:token" element={<InvitationPage />} /><Route path="*" element={<p>elsewhere</p>} /></Routes>,
  { route: '/invitations/abc123' })

// REQ-TEN-008 (C84).
describe('InvitationPage', () => {
  beforeEach(() => {
    for (const m of [preview, accept, decline, createAccount, login, signOut, mockAuth]) m.mockReset()
    mockAuth.mockReturnValue(signedOut)
  })

  it('sends a signed-out person with an account to the login page and back', async () => {
    preview.mockResolvedValue(pending)
    const { container } = renderPage()
    expect(await screen.findByText('Asha Rao invited you to join Acme.')).toBeInTheDocument()
    expect(screen.getByText('Acme › Ops')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Log in to accept' })).toHaveAttribute('href', '/login?returnTo=%2Finvitations%2Fabc123')
    expect(await axe(container)).toHaveNoViolations()
  })

  it('accepts as the signed-in matching account', async () => {
    preview.mockResolvedValue(pending)
    accept.mockResolvedValue({ accepted: true, organizationName: 'Acme' })
    mockAuth.mockReturnValue(signedInAs('Sam@Acme.example'))
    renderPage()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Accept invitation' }))
    expect(accept).toHaveBeenCalledWith('abc123')
    expect(await screen.findByText('You have joined Acme.')).toBeInTheDocument()
  })

  it('warns when another account is signed in and offers to sign out', async () => {
    preview.mockResolvedValue(pending)
    mockAuth.mockReturnValue(signedInAs('other@x.example'))
    renderPage()
    expect(await screen.findByText(/signed in as other@x\.example, but this invitation is for sam@acme\.example/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Accept invitation' })).not.toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Sign out' }))
    expect(signOut).toHaveBeenCalled()
  })

  it('creates an account for a new person, joins and signs in', async () => {
    preview.mockResolvedValue({ ...pending, accountExists: false })
    createAccount.mockResolvedValue({ accepted: true, organizationName: 'Acme' })
    login.mockResolvedValue([])
    renderPage()
    const user = userEvent.setup()
    await user.type(await screen.findByLabelText(/^First name/), 'Sam')
    await user.type(screen.getByLabelText(/^Last name/), 'Lee')
    await user.type(screen.getByLabelText(/^Password/), 'Str0ng!Pass')
    await user.type(screen.getByLabelText(/^Confirm password/), 'Str0ng!Pass')
    await user.click(screen.getByRole('button', { name: 'Create account and join' }))
    expect(createAccount).toHaveBeenCalledWith('abc123', { firstName: 'Sam', lastName: 'Lee', password: 'Str0ng!Pass', confirmPassword: 'Str0ng!Pass' })
    expect(login).toHaveBeenCalledWith('sam@acme.example', 'Str0ng!Pass')
    expect(await screen.findByText('You have joined Acme.')).toBeInTheDocument()
  })

  it('shows the server refusal, such as no seat', async () => {
    preview.mockResolvedValue(pending)
    accept.mockRejectedValue(new ApiError(409, 'There are no available seats in this organization.'))
    mockAuth.mockReturnValue(signedInAs('sam@acme.example'))
    renderPage()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Accept invitation' }))
    expect(await screen.findByText('There are no available seats in this organization.')).toBeInTheDocument()
  })

  it('declines', async () => {
    preview.mockResolvedValue(pending)
    decline.mockResolvedValue(undefined)
    renderPage()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Decline' }))
    expect(await screen.findByText('You declined this invitation.')).toBeInTheDocument()
  })

  it.each([['EXPIRED', 'This invitation has expired. Please request a new invitation.'], ['REVOKED', 'This invitation is no longer valid.']])(
    'explains a %s invitation', async (status, text) => {
      preview.mockResolvedValue({ status, accountExists: false, organizationAvailable: false })
      renderPage()
      expect(await screen.findByText(text)).toBeInTheDocument()
    })

  it('shows one generic message for an unknown link', async () => {
    preview.mockRejectedValue(new ApiError(404, 'x'))
    renderPage()
    expect(await screen.findByText('This invitation link is invalid or no longer available.')).toBeInTheDocument()
  })
})
