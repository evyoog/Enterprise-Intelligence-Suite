import '../i18n'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import type { ProfileDto } from '../api/profileApi'
import { ProfileCompletionContext, INITIAL_COMPLETION_DATA } from '../components/profile/ProfileCompletionContext'
import { makeAdminWithoutRecord, makeProfile } from '../test/profileFixtures'
import { LocalePreferenceProvider } from '../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../theming/ThemeModeProvider'
import { PhotoError } from '../utils/profilePhoto'
import { ProfilePage } from './ProfilePage'

const get = vi.fn()
const update = vi.fn()
const uploadPhoto = vi.fn()
const removePhoto = vi.fn()
const photo = vi.fn()
vi.mock('../api/profileApi', async (importActual) => {
  const actual = await importActual<typeof import('../api/profileApi')>()
  return {
    ...actual,
    profileApi: {
      get: () => get(), update: (b: unknown) => update(b), uploadPhoto: (b: Blob, n: string) => uploadPhoto(b, n),
      removePhoto: () => removePhoto(), photo: () => photo(),
    },
  }
})
const prepare = vi.fn()
vi.mock('../utils/profilePhoto', async (importActual) => {
  const actual = await importActual<typeof import('../utils/profilePhoto')>()
  return { ...actual, prepareProfilePhoto: (f: File) => prepare(f) }
})

const report = vi.fn()
function renderPage(route = '/account/profile') {
  return render(
    <MemoryRouter initialEntries={[route]}>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <ProfileCompletionContext.Provider value={{ state: { ...INITIAL_COMPLETION_DATA, photoUrl: null }, refresh: vi.fn(), report }}>
            <Routes>
              <Route path="/account/profile" element={<ProfilePage />} />
              <Route path="/account/preferences" element={<p>Preferences page</p>} />
            </Routes>
          </ProfileCompletionContext.Provider>
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>,
  )
}

const tab = (name: RegExp | string) => screen.getByRole('tab', { name })
const saveBar = () => screen.getByRole('region', { name: 'Save changes' })

beforeEach(() => {
  for (const m of [get, update, uploadPhoto, removePhoto, photo, prepare, report]) m.mockReset()
  get.mockResolvedValue(makeProfile())
  photo.mockResolvedValue(new Blob(['x'], { type: 'image/webp' }))
  URL.createObjectURL = vi.fn(() => 'blob:profile-photo')
  URL.revokeObjectURL = vi.fn()
})
afterEach(() => { vi.restoreAllMocks() })

describe('ProfilePage — header card and tabs', () => {
  it('shows who the person is, the completion status and the five tabs', async () => {
    renderPage()
    expect(await screen.findByRole('heading', { name: 'Asha Rao' })).toBeInTheDocument()
    expect(screen.getAllByText('asha.rao@acme.example').length).toBeGreaterThan(0)
    expect(screen.getByText('Organization administrator')).toBeInTheDocument()
    expect(screen.getByText('Acme Industries', { selector: '.MuiChip-label' })).toBeInTheDocument()
    expect(screen.getByText('5 of 11 complete')).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Profile 45% complete' })).toHaveAttribute('aria-valuenow', '45')
    expect(screen.getAllByRole('tab').map((t) => t.textContent)).toEqual(['Personal', 'Work', 'Address', 'Online', 'Account'])
    expect(report).toHaveBeenCalledWith(expect.objectContaining({ exists: true }))
  })

  it('shows the preferred name instead of the full name when one is set', async () => {
    get.mockResolvedValue(makeProfile({ personal: { ...makeProfile().personal, preferredName: 'Ash' } }))
    renderPage()
    expect(await screen.findByRole('heading', { name: 'Ash' })).toBeInTheDocument()
  })

  it('switches tabs and fills each with its own fields', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    expect(screen.getByLabelText(/First name/)).toHaveValue('Asha')
    expect(screen.getByLabelText('Mobile number')).toHaveValue('+919876543210')

    const user = userEvent.setup()
    await user.click(tab('Work'))
    expect(screen.getByLabelText('Job title')).toHaveValue('Director')
    expect(screen.getByLabelText('Department')).toHaveValue('')
    await user.click(tab('Address'))
    expect(screen.getByLabelText('City')).toHaveValue('Chennai')
    expect(screen.getByLabelText('Postal code')).toBeInTheDocument()
    await user.click(tab('Online'))
    expect(screen.getByLabelText('LinkedIn profile')).toBeInTheDocument()
    expect(screen.getByLabelText('Website')).toBeInTheDocument()
  })

  it('keeps the Account tab read-only, with links to security and preferences and the organization pages', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    await userEvent.setup().click(tab('Account'))
    const panel = screen.getByRole('tabpanel')
    expect(within(panel).getByText('asha.rao@acme.example')).toBeInTheDocument()
    expect(within(panel).getByText('Acme Industries (ACME)')).toBeInTheDocument()
    expect(within(panel).getByText('Active')).toBeInTheDocument()
    expect(within(panel).getByText('March 4, 2026')).toBeInTheDocument()
    expect(within(panel).queryAllByRole('textbox')).toHaveLength(0)
    expect(within(panel).getByRole('link', { name: 'Sign-in security' })).toHaveAttribute('href', '/account/security')
    expect(within(panel).getByRole('link', { name: /Preferences/ })).toHaveAttribute('href', '/account/preferences')
    expect(within(panel).getByRole('link', { name: 'People and structure' })).toHaveAttribute('href', '/organization/members')
  })

  it('does not offer the organization links to a member', async () => {
    get.mockResolvedValue(makeProfile({ account: { ...makeProfile().account, role: 'MEMBER' } }))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    await userEvent.setup().click(tab('Account'))
    expect(screen.queryByRole('link', { name: 'People and structure' })).not.toBeInTheDocument()
  })

  it('opens the missing list and jumps to the tab and field that fills an item', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: '6 items missing' }))
    await user.click(screen.getByRole('button', { name: 'Department' }))
    await waitFor(() => expect(tab('Work')).toHaveAttribute('aria-selected', 'true'))
    await waitFor(() => expect(screen.getByLabelText('Department')).toHaveFocus())
    await user.click(screen.getByRole('button', { name: 'Postal code' }))
    await waitFor(() => expect(tab('Address')).toHaveAttribute('aria-selected', 'true'))
    await waitFor(() => expect(screen.getByLabelText('Postal code')).toHaveFocus())
  })

  it('says so when the profile is complete', async () => {
    get.mockResolvedValue(makeProfile({ completion: { percent: 100, filled: 11, total: 11, missing: [] } }))
    renderPage()
    expect(await screen.findByText('Your profile is complete.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /missing/ })).not.toBeInTheDocument()
  })

  it('shows the names as managed by the identity provider when they are not editable', async () => {
    get.mockResolvedValue(makeProfile({ account: { ...makeProfile().account, nameEditable: false } }))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    expect(screen.getByLabelText(/First name/)).toBeDisabled()
    expect(screen.getByLabelText(/Last name/)).toBeDisabled()
    expect(screen.getAllByText("Your name is managed by your organization's identity provider.")).toHaveLength(2)
  })

  it('offers a retry when the profile cannot be loaded', async () => {
    get.mockRejectedValueOnce(new ApiError(500, 'Server unavailable'))
    renderPage()
    expect(await screen.findByText(/Server unavailable/)).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByRole('heading', { name: 'Asha Rao' })).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    expect(await axe(container)).toHaveNoViolations()
  }, 20000)
})

describe('ProfilePage — validation', () => {
  it('shows the rule inline when a field is left, marks the tab and sets aria-invalid', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    expect(screen.queryByTestId('tab-error-personal')).not.toBeInTheDocument()
    const mobile = screen.getByLabelText('Mobile number')
    await user.clear(mobile)
    await user.type(mobile, '98765')
    await user.tab()
    expect(await screen.findByText('Enter the number with country code, for example +919876543210.')).toBeInTheDocument()
    expect(mobile).toHaveAttribute('aria-invalid', 'true')
    expect(screen.getByTestId('tab-error-personal')).toBeInTheDocument()
    expect(tab(/Personal/)).toHaveAccessibleName('Personal (has errors)')
  })

  it('tidies what was typed when a field is left: phone separators and extra spaces', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    const mobile = screen.getByLabelText('Mobile number')
    await user.clear(mobile)
    await user.type(mobile, '+91 98765-43210')
    await user.tab()
    expect(mobile).toHaveValue('+919876543210')
    const last = screen.getByLabelText(/Last name/)
    await user.clear(last)
    await user.type(last, '  Rao   Iyer ')
    await user.tab()
    expect(last).toHaveValue('Rao Iyer')
  })

  it('requires the first name and refuses < and > in free text', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.clear(screen.getByLabelText(/First name/))
    await user.tab()
    expect(await screen.findByText('This field is required.')).toBeInTheDocument()
    await user.type(screen.getByLabelText('About you'), 'I <3 data')
    await user.tab()
    expect(await screen.findByText('Remove the characters < and > and any control characters.')).toBeInTheDocument()
  })

  it('applies the Indian postal code rule using the country on the same tab', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.click(tab('Address'))
    await user.type(screen.getByLabelText('Postal code'), 'SW1A 1AA')
    await user.tab()
    expect(await screen.findByText('An Indian postal code has exactly 6 digits.')).toBeInTheDocument()
    const country = screen.getByLabelText('Country')
    await user.clear(country)
    await user.type(country, 'United Kingdom')
    await user.tab()
    await waitFor(() => expect(screen.queryByText('An Indian postal code has exactly 6 digits.')).not.toBeInTheDocument())
  })

  it('checks the LinkedIn and website addresses', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.click(tab('Online'))
    await user.type(screen.getByLabelText('LinkedIn profile'), 'http://www.linkedin.com/in/asha')
    await user.type(screen.getByLabelText('Website'), 'ftp://example.com')
    await user.tab()
    expect(await screen.findByText('Enter a LinkedIn address starting with https://.')).toBeInTheDocument()
    expect(screen.getByText('Enter a web address starting with http:// or https://.')).toBeInTheDocument()
  })

  it('does not call the server when a save is tried with errors, and shows the tab and field to fix', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.click(tab('Online'))
    await user.type(screen.getByLabelText('Website'), 'nope')
    await user.click(tab('Work'))
    await user.type(screen.getByLabelText('Job title'), ' Lead')
    await user.click(within(saveBar()).getByRole('button', { name: 'Save changes' }))
    expect(update).not.toHaveBeenCalled()
    await waitFor(() => expect(tab(/Online/)).toHaveAttribute('aria-selected', 'true'))
    expect(await screen.findByText('Enter a web address starting with http:// or https://.')).toBeInTheDocument()
    await waitFor(() => expect(screen.getByLabelText('Website')).toHaveFocus())
  })
})

describe('ProfilePage — saving', () => {
  it('shows the Save / Discard bar only once something changed, and Discard restores the saved values', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    expect(screen.queryByRole('region', { name: 'Save changes' })).not.toBeInTheDocument()
    const user = userEvent.setup()
    await user.click(tab('Work'))
    await user.type(screen.getByLabelText('Department'), 'Finance')
    expect(saveBar()).toHaveTextContent('You have unsaved changes')
    await user.click(within(saveBar()).getByRole('button', { name: 'Discard' }))
    expect(screen.getByLabelText('Department')).toHaveValue('')
    expect(screen.queryByRole('region', { name: 'Save changes' })).not.toBeInTheDocument()
  })

  it('saves every tab in one request, normalised, then reports the new completion and shows a toast', async () => {
    const saved = makeProfile({
      work: { jobTitle: 'Director', department: 'Finance', companyName: 'Acme Industries', industry: null },
      completion: { percent: 55, filled: 6, total: 11, missing: ['photo', 'industry', 'addressLine1', 'state', 'postalCode'] },
    })
    update.mockResolvedValue(saved)
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.type(screen.getByLabelText('About you'), '  Runs   operations ')
    await user.click(tab('Work'))
    await user.type(screen.getByLabelText('Department'), 'Finance')
    await user.click(within(saveBar()).getByRole('button', { name: 'Save changes' }))

    await waitFor(() => expect(update).toHaveBeenCalledTimes(1))
    const body = update.mock.calls[0][0]
    expect(body.personal).toEqual({
      firstName: 'Asha', lastName: 'Rao', preferredName: null, mobile: '+919876543210', alternatePhone: null, bio: 'Runs operations',
    })
    expect(body.work).toEqual({ jobTitle: 'Director', department: 'Finance', companyName: 'Acme Industries', industry: null })
    expect(body.address).toEqual({ addressLine1: null, addressLine2: null, city: 'Chennai', state: null, postalCode: null, country: 'India' })
    expect(body.online).toEqual({ linkedinUrl: null, websiteUrl: null })
    expect(await screen.findByText('Profile saved.')).toBeInTheDocument()
    expect(screen.getByText('6 of 11 complete')).toBeInTheDocument()
    expect(report).toHaveBeenLastCalledWith(saved)
    expect(screen.queryByRole('region', { name: 'Save changes' })).not.toBeInTheDocument()
  })

  it('maps the server field errors back onto their fields, on the right tab', async () => {
    update.mockRejectedValue(new ApiError(400, 'Validation failed', {
      status: 400, message: 'Validation failed',
      fieldErrors: { 'address.postalCode': 'Postal code is not valid for India.', 'personal.lastName': 'Last name is not allowed.' },
    }))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.click(tab('Work'))
    await user.type(screen.getByLabelText('Department'), 'Finance')
    await user.click(within(saveBar()).getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByText('Some fields need your attention. They are marked on their tabs.')).toBeInTheDocument()
    // The first tab with an error is shown, with the server's text under the field.
    await waitFor(() => expect(tab(/Personal/)).toHaveAttribute('aria-selected', 'true'))
    expect(await screen.findByText('Last name is not allowed.')).toBeInTheDocument()
    expect(screen.getByLabelText(/Last name/)).toHaveAttribute('aria-invalid', 'true')
    expect(screen.getByTestId('tab-error-address')).toBeInTheDocument()
    await user.click(tab(/Address/))
    expect(screen.getByText('Postal code is not valid for India.')).toBeInTheDocument()
    // Editing the field clears its server error.
    await user.type(screen.getByLabelText('Postal code'), '600001')
    await waitFor(() => expect(screen.queryByText('Postal code is not valid for India.')).not.toBeInTheDocument())
  })

  it('tells the person nothing was saved when the sign-in service could not be updated (502)', async () => {
    update.mockRejectedValue(new ApiError(502, 'Bad gateway'))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.clear(screen.getByLabelText(/First name/))
    await user.type(screen.getByLabelText(/First name/), 'Ashaa')
    await user.click(within(saveBar()).getByRole('button', { name: 'Save changes' }))
    expect(await screen.findByText(/could not be updated at the sign-in service, so nothing was saved/)).toBeInTheDocument()
    expect(saveBar()).toBeInTheDocument()
  })

  it('shows any other server message as returned and keeps the edits', async () => {
    update.mockRejectedValue(new ApiError(409, 'This email already belongs to another account.'))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.type(screen.getByLabelText('About you'), 'Hello')
    await user.click(within(saveBar()).getByRole('button', { name: 'Save changes' }))
    expect(await screen.findByText('This email already belongs to another account.')).toBeInTheDocument()
    expect(screen.getByLabelText('About you')).toHaveValue('Hello')
  })
})

describe('ProfilePage — unsaved changes', () => {
  it('asks before an in-app link is followed, and lets the person stay or leave', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const user = userEvent.setup()
    await user.type(screen.getByLabelText('About you'), 'Hello')
    await user.click(screen.getByRole('link', { name: 'Change them in Preferences' }))
    const dialog = await screen.findByRole('dialog', { name: 'Leave without saving?' })
    expect(screen.queryByText('Preferences page')).not.toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Cancel' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(screen.getByLabelText('About you')).toHaveValue('Hello')

    await user.click(screen.getByRole('link', { name: 'Change them in Preferences' }))
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Leave' }))
    expect(await screen.findByText('Preferences page')).toBeInTheDocument()
  })

  it('follows a link without asking when nothing changed', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    await userEvent.setup().click(screen.getByRole('link', { name: 'Change them in Preferences' }))
    expect(await screen.findByText('Preferences page')).toBeInTheDocument()
  })

  it('warns on reload or close only while there are unsaved edits', async () => {
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    const fire = () => {
      const event = new Event('beforeunload', { cancelable: true })
      window.dispatchEvent(event)
      return event.defaultPrevented
    }
    expect(fire()).toBe(false)
    await userEvent.setup().type(screen.getByLabelText('About you'), 'x')
    expect(fire()).toBe(true)
  })
})

describe('ProfilePage — photo', () => {
  const pick = async (type = 'image/jpeg') => {
    const file = new File(['img'], 'me.jpg', { type })
    await userEvent.setup({ applyAccept: false }).upload(screen.getByTestId('profile-photo-input'), file)
    return file
  }

  it('uploads the scaled photo and reports the new completion', async () => {
    const blob = new Blob(['webp'], { type: 'image/webp' })
    prepare.mockResolvedValue({ blob, filename: 'photo.webp' })
    uploadPhoto.mockResolvedValue(makeProfile({ photo: { present: true, updatedAt: '2026-10-16T10:00:00Z' } }))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    expect(screen.getByRole('button', { name: 'Add photo' })).toBeEnabled()
    expect(screen.queryByRole('button', { name: 'Remove photo' })).not.toBeInTheDocument()

    const file = await pick()
    await waitFor(() => expect(uploadPhoto).toHaveBeenCalledWith(blob, 'photo.webp'))
    expect(prepare).toHaveBeenCalledWith(file)
    expect(await screen.findByText('Photo updated.')).toBeInTheDocument()
    expect(report).toHaveBeenLastCalledWith(expect.objectContaining({ photo: { present: true, updatedAt: '2026-10-16T10:00:00Z' } }))
    expect(await screen.findByRole('button', { name: 'Change photo' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Remove photo' })).toBeInTheDocument()
    await waitFor(() => expect(screen.getByRole('img', { name: 'Profile photo of Asha Rao' })).toHaveAttribute('src', 'blob:profile-photo'))
  })

  it('keeps the person\'s unsaved edits when the photo changes', async () => {
    prepare.mockResolvedValue({ blob: new Blob(['w']), filename: 'photo.webp' })
    uploadPhoto.mockResolvedValue(makeProfile({ photo: { present: true, updatedAt: 'now' } }))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    await userEvent.setup().type(screen.getByLabelText('About you'), 'Draft text')
    await pick()
    await screen.findByText('Photo updated.')
    expect(screen.getByLabelText('About you')).toHaveValue('Draft text')
  })

  it('removes the photo', async () => {
    get.mockResolvedValue(makeProfile({ photo: { present: true, updatedAt: '2026-10-16T10:00:00Z' } }))
    removePhoto.mockResolvedValue(makeProfile())
    renderPage()
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Remove photo' }))
    await waitFor(() => expect(removePhoto).toHaveBeenCalled())
    expect(await screen.findByText('Photo removed.')).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Add photo' })).toBeInTheDocument()
  })

  it('explains a wrong type or an oversized result without calling the server', async () => {
    prepare.mockRejectedValueOnce(new PhotoError('type')).mockRejectedValueOnce(new PhotoError('tooLarge'))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    await pick('image/svg+xml')
    expect(await screen.findByText('Use a JPEG, PNG or WebP image.')).toBeInTheDocument()
    await pick()
    expect(await screen.findByText('The photo is still larger than 1 MB after resizing. Choose a simpler image.')).toBeInTheDocument()
    expect(uploadPhoto).not.toHaveBeenCalled()
  })

  it('shows the server\'s message when it refuses the photo', async () => {
    prepare.mockResolvedValue({ blob: new Blob(['w']), filename: 'photo.webp' })
    uploadPhoto.mockRejectedValue(new ApiError(400, 'The file is not a valid image.'))
    renderPage()
    await screen.findByRole('heading', { name: 'Asha Rao' })
    await pick()
    expect(await screen.findByText('The file is not a valid image.')).toBeInTheDocument()
  })
})

describe('ProfilePage — a platform administrator without a customer record', () => {
  const adminProfile = makeAdminWithoutRecord()
  beforeEach(() => { get.mockResolvedValue(adminProfile) })

  it('says the profile is optional, prefilled with the sign-in names, and shows no progress nagging', async () => {
    renderPage()
    expect(await screen.findByText('Your customer profile has not been created.')).toBeInTheDocument()
    expect(screen.getByText('It is optional.')).toBeInTheDocument()
    expect(screen.getByLabelText(/First name/)).toHaveValue('Root')
    expect(screen.getByLabelText(/Last name/)).toHaveValue('Admin')
    expect(screen.getByText('Platform administrator', { selector: '.MuiChip-label' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add photo' })).toBeDisabled()
    expect(screen.getByText('Create your profile to add a photo.')).toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Save changes' })).not.toBeInTheDocument()
  })

  it('creates the record from the banner and then works like any other profile', async () => {
    update.mockResolvedValue(makeProfile({
      account: { ...adminProfile.account, status: 'ACTIVE' },
      personal: { ...makeProfile().personal, firstName: 'Root', lastName: 'Admin' },
    }))
    renderPage()
    await screen.findByText('Your customer profile has not been created.')
    await userEvent.setup().click(screen.getByRole('button', { name: 'Create my profile' }))
    await waitFor(() => expect(update).toHaveBeenCalledTimes(1))
    expect(update.mock.calls[0][0].personal).toMatchObject({ firstName: 'Root', lastName: 'Admin', mobile: null })
    expect(await screen.findByText('Your profile has been created.')).toBeInTheDocument()
    expect(screen.queryByText('Your customer profile has not been created.')).not.toBeInTheDocument()
    expect(report).toHaveBeenLastCalledWith(expect.objectContaining({ exists: true }))
    expect(screen.getByRole('button', { name: 'Add photo' })).toBeEnabled()
  })

  it('does not create the record when the names are blank, and shows the problem', async () => {
    renderPage()
    await screen.findByText('Your customer profile has not been created.')
    const user = userEvent.setup()
    await user.clear(screen.getByLabelText(/First name/))
    await user.click(within(saveBar()).getByRole('button', { name: 'Create my profile' }))
    expect(update).not.toHaveBeenCalled()
    expect(await screen.findByText('This field is required.')).toBeInTheDocument()
  })
})

describe('ProfilePage — loading', () => {
  it('shows a spinner first', () => {
    get.mockReturnValue(new Promise<ProfileDto>(() => {}))
    renderPage()
    expect(screen.getByRole('progressbar', { name: 'Loading…' })).toBeInTheDocument()
  })
})
