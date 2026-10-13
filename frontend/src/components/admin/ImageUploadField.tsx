import { useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Button, CircularProgress, Typography } from '@mui/material'
import { ImageUp, Trash2 } from 'lucide-react'
import { ApiError, resolveAssetUrl } from '../../api/client'
import { productsApi } from '../../api/productsApi'

/**
 * C95: one image upload control of the app form (the app icon or the banner image): a preview, an upload button,
 * Remove, and a hint with the recommended size. The file goes to the same product image upload as before and the
 * field holds the returned URL. `square` previews the icon, `wide` the banner.
 */
export function ImageUploadField({ id, label, hint, uploadLabel, previewAlt, value, onChange, onBusyChange, onError, shape }: {
  id: string
  label: string
  hint: string
  uploadLabel: string
  previewAlt: string
  value: string
  onChange: (url: string) => void
  onBusyChange: (busy: boolean) => void
  onError: (message: string | null) => void
  shape: 'square' | 'wide'
}) {
  const { t } = useTranslation()
  const inputRef = useRef<HTMLInputElement>(null)
  const [busy, setBusy] = useState(false)

  const setBusyBoth = (next: boolean) => { setBusy(next); onBusyChange(next) }

  const selected = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setBusyBoth(true)
    onError(null)
    productsApi.uploadImage(file)
      .then((result) => onChange(result.url))
      .catch((err) => onError(err instanceof ApiError ? err.message : t('forms.uploadError')))
      .finally(() => setBusyBoth(false))
  }

  const wide = shape === 'wide'
  return (
    <Box role="group" aria-labelledby={`${id}-label`}>
      <Typography id={`${id}-label`} variant="body2" sx={{ fontWeight: 600, mb: 1 }}>{label}</Typography>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
        <Box sx={{
          width: wide ? 200 : 56, height: wide ? 50 : 56, borderRadius: wide ? 1.5 : 2.5, border: '1px dashed', borderColor: 'divider', overflow: 'hidden',
          display: 'grid', placeItems: 'center', bgcolor: 'action.hover', flexShrink: 0,
        }}>
          {value
            ? <Box component="img" src={resolveAssetUrl(value)} alt={previewAlt} sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            : <ImageUp size={20} aria-hidden />}
        </Box>
        <Button variant="outlined" disabled={busy} onClick={() => inputRef.current?.click()}
          startIcon={busy ? <CircularProgress size={16} /> : <ImageUp size={16} />}>
          {busy ? t('forms.uploading') : uploadLabel}
        </Button>
        <input id={id} ref={inputRef} type="file" accept="image/png,image/jpeg,image/gif,image/webp" hidden tabIndex={-1} aria-hidden onChange={selected} data-testid={id} />
        {value && (
          <Button color="inherit" startIcon={<Trash2 size={16} />} onClick={() => onChange('')}>{t('forms.removeImage')}</Button>
        )}
      </Box>
      <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 1 }}>{hint}</Typography>
    </Box>
  )
}
