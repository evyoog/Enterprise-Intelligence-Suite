import '../../i18n'
import { fireEvent, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { useState } from 'react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { ApiError } from '../../api/client'
import { ImageUploadField } from './ImageUploadField'

const uploadImage = vi.fn()
vi.mock('../../api/productsApi', () => ({ productsApi: { uploadImage: (f: File) => uploadImage(f) } }))

function Harness({ initial = '', onError = vi.fn(), onBusy = vi.fn() }: { initial?: string; onError?: (m: string | null) => void; onBusy?: (b: boolean) => void }) {
  const [icon, setIcon] = useState(initial)
  const [banner, setBanner] = useState('')
  return (
    <>
      <ImageUploadField id="app-icon" shape="square" label="App icon" hint="Square image" uploadLabel="Upload app icon" previewAlt="App icon preview"
        value={icon} onChange={setIcon} onBusyChange={onBusy} onError={onError} />
      <ImageUploadField id="app-banner" shape="wide" label="Banner image" hint="Wide image" uploadLabel="Upload banner image" previewAlt="Banner preview"
        value={banner} onChange={setBanner} onBusyChange={onBusy} onError={onError} />
    </>
  )
}
const png = (name: string) => new File(['x'], name, { type: 'image/png' })

// TC-CAT-060 and TC-CAT-061 (C95)
describe('ImageUploadField', () => {
  beforeEach(() => uploadImage.mockReset())

  it('has one upload button for the app icon and one for the banner image, each with its own preview', async () => {
    uploadImage.mockResolvedValueOnce({ url: '/products/images/icon.png' }).mockResolvedValueOnce({ url: '/products/images/banner.png' })
    renderWithProviders(<Harness />)
    expect(screen.getByRole('button', { name: 'Upload app icon' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Upload banner image' })).toBeInTheDocument()

    fireEvent.change(screen.getByTestId('app-icon'), { target: { files: [png('icon.png')] } })
    expect(await screen.findByAltText('App icon preview')).toHaveAttribute('src', expect.stringContaining('/products/images/icon.png'))
    expect(screen.queryByAltText('Banner preview')).not.toBeInTheDocument()

    fireEvent.change(screen.getByTestId('app-banner'), { target: { files: [png('banner.png')] } })
    expect(await screen.findByAltText('Banner preview')).toHaveAttribute('src', expect.stringContaining('/products/images/banner.png'))
    expect(uploadImage).toHaveBeenCalledTimes(2)
  })

  it('removes an image, and reports an upload error and the busy state', async () => {
    const user = userEvent.setup()
    const onError = vi.fn()
    const onBusy = vi.fn()
    uploadImage.mockRejectedValueOnce(new ApiError(400, 'Unsupported image type: .bmp'))
    renderWithProviders(<Harness initial="/products/images/old.png" onError={onError} onBusy={onBusy} />)
    await user.click(screen.getAllByRole('button', { name: 'Remove image' })[0])
    expect(screen.queryByAltText('App icon preview')).not.toBeInTheDocument()

    fireEvent.change(screen.getByTestId('app-banner'), { target: { files: [png('x.bmp')] } })
    await waitFor(() => expect(onError).toHaveBeenLastCalledWith('Unsupported image type: .bmp'))
    expect(onBusy).toHaveBeenCalledWith(true)
    expect(onBusy).toHaveBeenLastCalledWith(false)
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<Harness initial="/products/images/old.png" />)
    expect(await axe(container)).toHaveNoViolations()
  })
})
