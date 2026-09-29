import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { ClaimMappingDialog } from './ClaimMappingDialog'

// REQ-IAM-007 (C28) acceptance criteria (UI).
describe('ClaimMappingDialog', () => {
  it('saves the entered names and shows the protocol defaults', async () => {
    const onSave = vi.fn().mockResolvedValue(undefined)
    const onClose = vi.fn()
    renderWithProviders(
      <ClaimMappingDialog open providerName="Okta" protocol="OIDC" initial={{ email: 'upn' }} onSave={onSave} onClose={onClose} />,
    )
    const dialog = screen.getByRole('dialog')
    expect(within(dialog).getByRole('textbox', { name: 'Email' })).toHaveValue('upn')
    expect(within(dialog).getByText('Defaults: given_name')).toBeInTheDocument()

    const user = userEvent.setup()
    await user.type(within(dialog).getByRole('textbox', { name: 'First name' }), 'first')
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(onSave).toHaveBeenCalledWith({ email: 'upn', firstName: 'first', lastName: '', displayName: '' })
    expect(onClose).toHaveBeenCalled()
  })

  it('resets every field to the defaults', async () => {
    const onSave = vi.fn().mockResolvedValue(undefined)
    renderWithProviders(
      <ClaimMappingDialog open providerName="ADFS" protocol="SAML" initial={{ email: 'mail', lastName: 'sn' }} onSave={onSave} onClose={vi.fn()} />,
    )
    await userEvent.setup().click(screen.getByRole('button', { name: 'Reset to defaults' }))
    expect(onSave).toHaveBeenCalledWith({ email: null, firstName: null, lastName: null, displayName: null })
  })

  it('keeps the dialog open and shows the backend message on failure', async () => {
    const onSave = vi.fn().mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    const onClose = vi.fn()
    renderWithProviders(<ClaimMappingDialog open providerName="ADFS" protocol="SAML" onSave={onSave} onClose={onClose} />)
    await userEvent.setup().click(screen.getByRole('button', { name: 'Save' }))
    expect(await screen.findByText('You do not have permission to do this')).toBeInTheDocument()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('has no detectable a11y violations', async () => {
    const { baseElement } = renderWithProviders(
      <ClaimMappingDialog open providerName="ADFS" protocol="SAML" onSave={vi.fn()} onClose={vi.fn()} />,
    )
    expect(await axe(baseElement)).toHaveNoViolations()
  })
})
