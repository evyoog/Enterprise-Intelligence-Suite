import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { MessageTemplatesSection } from './MessageTemplatesSection'

const list = vi.fn()
const save = vi.fn()
const reset = vi.fn()
const preview = vi.fn()

vi.mock('../../api/messageTemplatesApi', () => ({
  messageTemplatesApi: {
    list: () => list(),
    save: (c: string, l: string, b: unknown) => save(c, l, b),
    reset: (c: string, l: string) => reset(c, l),
    preview: (c: string, l: string, b: unknown) => preview(c, l, b),
  },
}))

const variant = (language: string, over = {}) => ({
  language, customized: false, subject: `Subject ${language}`, body: `Hello {name} (${language})`,
  defaultSubject: `Subject ${language}`, defaultBody: `Hello {name} (${language})`, updatedAt: null, ...over,
})
const type = {
  code: 'EMAIL_INVITATION', name: 'Invitation email', description: 'Sent when someone is invited.', channel: 'EMAIL' as const,
  placeholders: [{ name: 'name', description: 'Recipient name', example: 'Asha', required: true }],
  variants: [variant('en'), variant('es')],
}

// TC-GOV-018
describe('MessageTemplatesSection', () => {
  beforeEach(() => { for (const m of [list, save, reset, preview]) m.mockReset(); list.mockResolvedValue([type]) })

  it('edits and saves the text for one language', async () => {
    const user = userEvent.setup()
    save.mockResolvedValue({ ...type, variants: [variant('en', { customized: true, body: 'Hi {name}' }), variant('es')] })
    renderWithProviders(<MessageTemplatesSection />)
    await user.click(await screen.findByRole('button', { name: 'Edit Invitation email' }))
    const body = screen.getByLabelText('Message')
    await user.clear(body)
    await user.type(body, 'Hi {{name}')
    await user.click(screen.getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(save).toHaveBeenCalledWith('EMAIL_INVITATION', 'en', { subject: 'Subject en', body: 'Hi {name}' }))
  })

  it('shows a preview with example values and lets the other language be edited', async () => {
    const user = userEvent.setup()
    preview.mockResolvedValue({ subject: 'Subject es', body: 'Hola Asha' })
    renderWithProviders(<MessageTemplatesSection />)
    await user.click(await screen.findByRole('button', { name: 'Edit Invitation email' }))
    await user.click(screen.getByRole('tab', { name: 'ES' }))
    expect(screen.getByLabelText('Message')).toHaveValue('Hello {name} (es)')
    await user.click(screen.getByRole('button', { name: 'Preview' }))
    expect(await screen.findByText('Hola Asha')).toBeInTheDocument()
  })

  it('resets a customized language to the built-in text', async () => {
    const user = userEvent.setup()
    list.mockResolvedValue([{ ...type, variants: [variant('en', { customized: true, body: 'Custom' }), variant('es')] }])
    reset.mockResolvedValue(type)
    renderWithProviders(<MessageTemplatesSection />)
    await user.click(await screen.findByRole('button', { name: 'Edit Invitation email' }))
    await user.click(screen.getByRole('button', { name: 'Reset to built-in' }))
    await waitFor(() => expect(reset).toHaveBeenCalledWith('EMAIL_INVITATION', 'en'))
    await waitFor(() => expect(screen.getByLabelText('Message')).toHaveValue('Hello {name} (en)'))
  })

  it('shows the server message when a placeholder is rejected', async () => {
    const user = userEvent.setup()
    const { ApiError } = await import('../../api/client')
    save.mockRejectedValue(new ApiError(400, 'Unknown placeholder {nmae}.'))
    renderWithProviders(<MessageTemplatesSection />)
    await user.click(await screen.findByRole('button', { name: 'Edit Invitation email' }))
    await user.click(screen.getByRole('button', { name: 'Save' }))
    expect(await screen.findByText('Unknown placeholder {nmae}.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<MessageTemplatesSection />)
    await screen.findByText('Invitation email')
    expect(await axe(container)).toHaveNoViolations()
  })
})
