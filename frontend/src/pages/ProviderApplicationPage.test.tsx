import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { renderWithProviders } from '../test/renderWithProviders'
import { ProviderApplicationPage } from './ProviderApplicationPage'

const apply = vi.fn()
vi.mock('../api/partnersApi', async () => {
  const actual = await vi.importActual<typeof import('../api/partnersApi')>('../api/partnersApi')
  return { ...actual, partnersApi: { ...actual.partnersApi, apply: (r: unknown) => apply(r) } }
})

describe('ProviderApplicationPage', () => {
  beforeEach(() => {
    apply.mockReset()
  })

  it('submits an application and shows a confirmation', async () => {
    apply.mockResolvedValue({ id: 1, name: 'Acme', contactName: 'Jane', contactEmail: 'jane@acme.example', status: 'REGISTERED', createdAt: '2027-04-01T00:00:00Z' })
    renderWithProviders(<ProviderApplicationPage />)

    const user = userEvent.setup()
    await user.type(screen.getByLabelText(/Company name/), 'Acme')
    await user.type(screen.getByLabelText(/Contact name/), 'Jane')
    await user.type(screen.getByLabelText(/Contact email/), 'jane@acme.example')
    await user.click(screen.getByRole('button', { name: 'Submit application' }))

    expect(apply).toHaveBeenCalledWith({ name: 'Acme', contactName: 'Jane', contactEmail: 'jane@acme.example', description: undefined })
    expect(await screen.findByText(/application has been submitted/)).toBeInTheDocument()
  })

  it('shows an error when the submission fails', async () => {
    apply.mockRejectedValue(new ApiError(400, 'That email is already in use.'))
    renderWithProviders(<ProviderApplicationPage />)

    const user = userEvent.setup()
    await user.type(screen.getByLabelText(/Company name/), 'Acme')
    await user.type(screen.getByLabelText(/Contact name/), 'Jane')
    await user.type(screen.getByLabelText(/Contact email/), 'jane@acme.example')
    await user.click(screen.getByRole('button', { name: 'Submit application' }))

    expect(await screen.findByText('That email is already in use.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<ProviderApplicationPage />)
    await screen.findByText('Become a partner')
    expect(await axe(container)).toHaveNoViolations()
  })
})
