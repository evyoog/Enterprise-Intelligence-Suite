import { useRef, useState, type FormEvent } from 'react'
import { Alert, Box, Button, CircularProgress, Paper, TextField } from '@mui/material'
import { ImageUp, Save } from 'lucide-react'
import { ApiError, resolveAssetUrl } from '../../api/client'
import { platformsApi, type Platform, type PlatformCreateRequest } from '../../api/platformsApi'

interface PlatformFormProps {
  /** Present for editing an existing platform; absent for creating a new one. */
  initialPlatform?: Platform
  onSubmit: (payload: PlatformCreateRequest) => Promise<unknown>
  submitLabel: string
  submittingLabel: string
  successMessage: string
  onSuccess?: () => void
}

/**
 * The platform create/edit form — shared by PlatformSettingsPage ("Add a
 * platform") and EditPlatformPage. Mirrors ProductForm's shape but for the
 * much smaller Platform entity: just a name, description, and logo.
 */
export function PlatformForm({
  initialPlatform, onSubmit, submitLabel, submittingLabel, successMessage, onSuccess,
}: PlatformFormProps) {
  const [name, setName] = useState(initialPlatform?.name ?? '')
  const [description, setDescription] = useState(initialPlatform?.description ?? '')
  const [imageUrl, setImageUrl] = useState(initialPlatform?.imageUrl ?? '')
  const [isUploadingImage, setIsUploadingImage] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const canSubmit = name.trim().length > 0 && !isSubmitting && !isUploadingImage

  const handleImageSelected = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    setIsUploadingImage(true)
    setError(null)

    platformsApi.uploadImage(file)
      .then((result) => setImageUrl(result.url))
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Could not upload the logo.'))
      .finally(() => setIsUploadingImage(false))
  }

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!canSubmit) return

    setIsSubmitting(true)
    setError(null)
    setSuccess(false)

    onSubmit({
      name: name.trim(),
      description: description.trim() || undefined,
      imageUrl: imageUrl || undefined,
    })
      .then(() => {
        setSuccess(true)
        onSuccess?.()
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Something went wrong. Please try again.'))
      .finally(() => setIsSubmitting(false))
  }

  return (
    <Paper variant="outlined" sx={{ p: 3 }}>
      {success && <Alert severity="success" sx={{ mb: 2 }}>{successMessage}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <TextField
          label="Platform name"
          required
          fullWidth
          placeholder="e.g. Thittam"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <TextField
          label="Description"
          fullWidth
          multiline
          minRows={2}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <Box>
          <Button
            component="label"
            variant="outlined"
            startIcon={isUploadingImage ? <CircularProgress size={16} /> : <ImageUp size={16} />}
            disabled={isUploadingImage}
          >
            {isUploadingImage ? 'Uploading…' : 'Upload Logo'}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/png,image/jpeg,image/gif,image/webp"
              hidden
              onChange={handleImageSelected}
            />
          </Button>

          {imageUrl && (
            <Box
              component="img"
              src={resolveAssetUrl(imageUrl)}
              alt="Preview"
              sx={{ display: 'block', mt: 1.5, maxHeight: 120, borderRadius: 1, border: '1px solid', borderColor: 'divider' }}
            />
          )}
        </Box>

        <Button
          type="submit"
          variant="contained"
          size="large"
          disabled={!canSubmit}
          startIcon={isSubmitting ? <CircularProgress size={16} color="inherit" /> : <Save size={16} />}
        >
          {isSubmitting ? submittingLabel : submitLabel}
        </Button>
      </Box>
    </Paper>
  )
}
