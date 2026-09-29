import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../../test/renderWithProviders'
import { CommonSettingsPage } from './CommonSettingsPage'

const listCurrencies = vi.fn()
const updateCurrency = vi.fn()
const listRegions = vi.fn()
const createRegion = vi.fn()
const updateRegion = vi.fn()
const deleteRegion = vi.fn()
const listFeatureFlags = vi.fn()
const createFeatureFlag = vi.fn()
const updateFeatureFlag = vi.fn()
const deleteFeatureFlag = vi.fn()
const listSupportedLanguages = vi.fn()

vi.mock('../../../api/platformAdministrationApi', () => ({
  platformAdministrationApi: {
    listCurrencies: () => listCurrencies(),
    updateCurrency: (code: string, enabled: boolean) => updateCurrency(code, enabled),
    listRegions: () => listRegions(),
    createRegion: (code: string, name: string) => createRegion(code, name),
    updateRegion: (id: number, name: string, enabled: boolean) => updateRegion(id, name, enabled),
    deleteRegion: (id: number) => deleteRegion(id),
    listFeatureFlags: () => listFeatureFlags(),
    createFeatureFlag: (flagKey: string, enabled: boolean, description?: string) => createFeatureFlag(flagKey, enabled, description),
    updateFeatureFlag: (flagKey: string, enabled: boolean, description?: string) => updateFeatureFlag(flagKey, enabled, description),
    deleteFeatureFlag: (flagKey: string) => deleteFeatureFlag(flagKey),
    listSupportedLanguages: () => listSupportedLanguages(),
  },
}))

const usd = { code: 'USD', name: 'US Dollar', enabled: true }
const eur = { code: 'EUR', name: 'Euro', enabled: true }
const region = { id: 1, code: 'APAC', name: 'Asia Pacific', enabled: true }
const flag = { flagKey: 'groups_enabled', enabled: true, description: 'Groups' }

// 15.01 Platform Administration (sprint 2026.4.2).
describe('CommonSettingsPage', () => {
  beforeEach(() => {
    [listCurrencies, updateCurrency, listRegions, createRegion, updateRegion, deleteRegion,
      listFeatureFlags, createFeatureFlag, updateFeatureFlag, deleteFeatureFlag, listSupportedLanguages]
      .forEach((m) => m.mockReset())
    listCurrencies.mockResolvedValue([usd, eur])
    listRegions.mockResolvedValue([region])
    listFeatureFlags.mockResolvedValue([flag])
    listSupportedLanguages.mockResolvedValue([{ code: 'en', label: 'English' }, { code: 'es', label: 'Español' }])
  })

  it('shows the read-only supported languages', async () => {
    renderWithProviders(<CommonSettingsPage />)
    expect(await screen.findByText('English (en)')).toBeInTheDocument()
    expect(screen.getByText('Español (es)')).toBeInTheDocument()
  })

  it('disables a currency', async () => {
    const user = userEvent.setup()
    updateCurrency.mockResolvedValue({ ...eur, enabled: false })
    renderWithProviders(<CommonSettingsPage />)

    await screen.findByText('Euro (EUR)')
    await user.click(screen.getByLabelText('Enable EUR'))

    expect(updateCurrency).toHaveBeenCalledWith('EUR', false)
    await waitFor(() => expect(screen.getByLabelText('Enable EUR')).not.toBeChecked())
  })

  it('creates a region', async () => {
    const user = userEvent.setup()
    createRegion.mockResolvedValue({ id: 2, code: 'EMEA', name: 'Europe', enabled: true })
    renderWithProviders(<CommonSettingsPage />)

    await screen.findByText('Asia Pacific')
    await user.type(screen.getByLabelText('Code'), 'EMEA')
    await user.type(screen.getByLabelText('Name'), 'Europe')
    await user.click(screen.getByRole('button', { name: 'Add region' }))

    expect(createRegion).toHaveBeenCalledWith('EMEA', 'Europe')
    expect(await screen.findByText('Europe')).toBeInTheDocument()
  })

  it('deletes a feature flag', async () => {
    const user = userEvent.setup()
    deleteFeatureFlag.mockResolvedValue(undefined)
    renderWithProviders(<CommonSettingsPage />)

    await user.click(await screen.findByLabelText('Delete groups_enabled'))
    expect(deleteFeatureFlag).toHaveBeenCalledWith('groups_enabled')
    await waitFor(() => expect(screen.queryByText('groups_enabled')).not.toBeInTheDocument())
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<CommonSettingsPage />)
    await screen.findByText('Asia Pacific')
    expect(await axe(container)).toHaveNoViolations()
  })
})
