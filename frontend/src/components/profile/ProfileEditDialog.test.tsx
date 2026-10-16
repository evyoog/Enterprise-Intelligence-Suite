import '../../i18n'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { makeProfile } from '../../test/profileFixtures'
import { ProfileEditDialog } from './ProfileEditDialog'

const getForCustomer = vi.fn()
const updateForCustomer = vi.fn()
vi.mock('../../api/profileApi', async (importActual) => {
  const actual = await importActual<typeof import('../../api/profileApi')>()
  return {
    ...actual,
    profileApi: {
      getForCustomer: (id: number) => getForCustomer(id),
      updateForCustomer: (id: number, b: unknown) => updateForCustomer(id, b),
    },
  }
})

const onClose = vi.fn()
const onSaved = vi.fn()
const open = (id: number | null = 42) => render(<ProfileEditDialog customerId={id} name="Asha Rao" onClose={onClose} onSaved={onSaved} />)

beforeEach(() => {
  for (const m of [getForCustomer, updateForCustomer, onClose, onSaved]) m.mockReset()
  getForCustomer.mockResolvedValue(makeProfile())
})

describe('ProfileEditDialog (platform administrator)', () => {
  it('renders nothing without a person', () => {
    open(null)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(getForCustomer).not.toHaveBeenCalled()
  })

  it('loads the person\'s record from the admin endpoint and shows the same form without photo or Account tab', async () => {
    open()
    const dialog = await screen.findByRole('dialog', { name: 'Edit profile: Asha Rao' })
    expect(await within(dialog).findByLabelText(/First name/)).toHaveValue('Asha')
    expect(getForCustomer).toHaveBeenCalledWith(42)
    expect(within(dialog).getAllByRole('tab').map((t) => t.textContent)).toEqual(['Personal', 'Work', 'Address', 'Online'])
    expect(within(dialog).queryByRole('button', { name: /photo/i })).not.toBeInTheDocument()
    expect(within(dialog).queryByTestId('profile-photo-input')).not.toBeInTheDocument()
    expect(within(dialog).getByText('Editing the profile of asha.rao@acme.example. The email and photo cannot be changed here.')).toBeInTheDocument()
    expect(within(dialog).getByRole('button', { name: 'Save changes' })).toBeDisabled()
  })

  it('saves through the admin endpoint, then reports the result and closes', async () => {
    const saved = makeProfile({ work: { ...makeProfile().work, department: 'Finance' } })
    updateForCustomer.mockResolvedValue(saved)
    open()
    const dialog = await screen.findByRole('dialog')
    const user = userEvent.setup()
    await user.click(await within(dialog).findByRole('tab', { name: 'Work' }))
    await user.type(within(dialog).getByLabelText('Department'), 'Finance')
    await user.click(within(dialog).getByRole('button', { name: 'Save changes' }))
    await waitFor(() => expect(updateForCustomer).toHaveBeenCalledTimes(1))
    const [id, body] = updateForCustomer.mock.calls[0]
    expect(id).toBe(42)
    expect(body.work).toEqual({ jobTitle: 'Director', department: 'Finance', companyName: 'Acme Industries', industry: null })
    expect(Object.keys(body)).toEqual(['personal', 'work', 'address', 'online'])
    await waitFor(() => expect(onClose).toHaveBeenCalled())
    expect(onSaved).toHaveBeenCalledWith(saved)
  })

  it('applies the same validation and does not call the server when a rule is broken', async () => {
    open()
    const dialog = await screen.findByRole('dialog')
    const user = userEvent.setup()
    const mobile = await within(dialog).findByLabelText('Mobile number')
    await user.clear(mobile)
    await user.type(mobile, '12345')
    await user.click(within(dialog).getByRole('button', { name: 'Save changes' }))
    expect(await within(dialog).findByText('Enter the number with country code, for example +919876543210.')).toBeInTheDocument()
    expect(updateForCustomer).not.toHaveBeenCalled()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('shows the server\'s field errors on the right tab and stays open', async () => {
    updateForCustomer.mockRejectedValue(new ApiError(400, 'Validation failed', { fieldErrors: { 'online.websiteUrl': 'Website is not reachable.' } }))
    open()
    const dialog = await screen.findByRole('dialog')
    const user = userEvent.setup()
    await user.type(await within(dialog).findByLabelText('About you'), 'hello')
    await user.click(within(dialog).getByRole('button', { name: 'Save changes' }))
    await waitFor(() => expect(within(dialog).getByRole('tab', { name: /Online/, selected: true })).toBeInTheDocument())
    expect(await within(dialog).findByText('Website is not reachable.')).toBeInTheDocument()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('shows a refusal such as a missing permission as returned', async () => {
    updateForCustomer.mockRejectedValue(new ApiError(403, 'You do not have permission to edit profiles.'))
    open()
    const dialog = await screen.findByRole('dialog')
    await userEvent.setup().type(await within(dialog).findByLabelText('About you'), 'hello')
    await userEvent.setup().click(within(dialog).getByRole('button', { name: 'Save changes' }))
    expect(await within(dialog).findByText('You do not have permission to edit profiles.')).toBeInTheDocument()
  })

  it('shows a load error and closes on Cancel', async () => {
    getForCustomer.mockRejectedValue(new ApiError(404, 'Customer not found'))
    open()
    expect(await screen.findByText('Customer not found')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Cancel' }))
    expect(onClose).toHaveBeenCalled()
  })
})
