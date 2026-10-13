import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { PlatformDefaultsSection } from './PlatformDefaultsSection'

const get = vi.fn()
const save = vi.fn()

vi.mock('../../api/messageTemplatesApi', () => ({
  platformDefaultsApi: { get: () => get(), save: (b: unknown) => save(b) },
}))
vi.mock('../../api/platformAdministrationApi', () => ({
  platformAdministrationApi: {
    listSupportedLanguages: () => Promise.resolve([{ code: 'en', label: 'English' }, { code: 'es', label: 'Español' }]),
    listCurrencies: () => Promise.resolve([{ code: 'USD', name: 'US Dollar', enabled: true }, { code: 'INR', name: 'Indian Rupee', enabled: true }]),
  },
}))

const empty = { language: null, region: null, currency: null, timeZone: null, dateFormat: null, reminderDays: 7, updatedAt: null }

// TC-GOV-017
describe('PlatformDefaultsSection', () => {
  beforeEach(() => { get.mockReset(); save.mockReset(); get.mockResolvedValue(empty) })

  it('saves the defaults, sending an empty text for a field left without a default', async () => {
    const user = userEvent.setup()
    save.mockResolvedValue({ ...empty, region: 'en-IN', timeZone: 'Asia/Kolkata', reminderDays: 10 })
    renderWithProviders(<PlatformDefaultsSection />)
    const region = await screen.findByLabelText('Region')
    await waitFor(() => expect(region).toBeEnabled())
    await user.type(region, 'en-IN')
    await user.type(screen.getByLabelText('Time zone'), 'Asia/Kolkata')
    await user.clear(screen.getByLabelText('Renewal reminder days'))
    await user.type(screen.getByLabelText('Renewal reminder days'), '10')
    await user.click(screen.getByRole('button', { name: 'Save defaults' }))
    await waitFor(() => expect(save).toHaveBeenCalledWith({
      language: '', region: 'en-IN', currency: '', timeZone: 'Asia/Kolkata', dateFormat: '', reminderDays: 10,
    }))
    expect(await screen.findByText('Defaults saved.')).toBeInTheDocument()
  })

  it('shows the server message when a value is rejected', async () => {
    const user = userEvent.setup()
    const { ApiError } = await import('../../api/client')
    save.mockRejectedValue(new ApiError(400, 'Not a real time zone: Mars/Base'))
    renderWithProviders(<PlatformDefaultsSection />)
    const tz = await screen.findByLabelText('Time zone')
    await waitFor(() => expect(tz).toBeEnabled())
    await user.type(tz, 'Mars/Base')
    await user.click(screen.getByRole('button', { name: 'Save defaults' }))
    expect(await screen.findByText('Not a real time zone: Mars/Base')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<PlatformDefaultsSection />)
    await screen.findByLabelText('Region')
    expect(await axe(container)).toHaveNoViolations()
  })
})
