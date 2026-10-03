import { useRef, useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, CircularProgress, FormControlLabel, MenuItem, Paper, Switch, TextField, Typography,
} from '@mui/material'
import { ImageUp, Save, Trash2 } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError, resolveAssetUrl } from '../../api/client'
import { platformsApi, type Platform, type PlatformCreateRequest, type PlatformStatus } from '../../api/platformsApi'
import { DEFAULT_SHOWCASE_COLOR, isHexColor } from '../../utils/showcaseColor'
import { ColorPicker } from '../ui/ColorPicker'
import { FormSection } from '../ui/FormSection'
import { ShowcaseCard } from '../ui/ShowcaseCard'

interface PlatformFormProps {
  /** Present for editing an existing platform; absent for creating a new one. */
  initialPlatform?: Platform
  onSubmit: (payload: PlatformCreateRequest) => Promise<unknown>
  submitLabel: string
  submittingLabel: string
  successMessage: string
  onSuccess?: () => void
  /** Where Cancel goes. */
  cancelTo?: string
  /** Real values for the preview when editing (apps assigned to the platform). */
  previewAppCount?: number
}

/**
 * The platform create/edit form (C66): Basic information, Appearance
 * (showcase colour), Catalog settings and a live Showcase Preview that uses
 * the same card as the Product Catalog. Shared by PlatformSettingsPage
 * (create) and EditPlatformPage (edit).
 */
export function PlatformForm({
  initialPlatform, onSubmit, submitLabel, submittingLabel, successMessage, onSuccess, cancelTo, previewAppCount,
}: PlatformFormProps) {
  const { t } = useTranslation()
  const [name, setName] = useState(initialPlatform?.name ?? '')
  const [description, setDescription] = useState(initialPlatform?.description ?? '')
  const [imageUrl, setImageUrl] = useState(initialPlatform?.imageUrl ?? '')
  const [primaryColor, setPrimaryColor] = useState<string | null>(initialPlatform?.primaryColor ?? null)
  const [status, setStatus] = useState<PlatformStatus>(initialPlatform?.status ?? 'ACTIVE')
  const [showInCatalog, setShowInCatalog] = useState(initialPlatform?.showInCatalog ?? true)
  const [displayOrder, setDisplayOrder] = useState(String(initialPlatform?.displayOrder ?? 0))
  const [isUploadingImage, setIsUploadingImage] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [touched, setTouched] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const order = Number(displayOrder)
  const errors = {
    name: !name.trim() ? t('forms.required') : name.length > 255 ? t('forms.tooLong', { max: 255 }) : null,
    description: description.length > 2000 ? t('forms.tooLong', { max: 2000 }) : null,
    color: primaryColor !== null && !isHexColor(primaryColor) ? t('ui.color.hexError') : null,
    displayOrder: !/^\d+$/.test(displayOrder) || order > 9999 ? t('forms.platform.orderError') : null,
  }
  const valid = Object.values(errors).every((e) => !e)
  const show = (key: keyof typeof errors) => (touched ? errors[key] : null)

  const handleImageSelected = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    setIsUploadingImage(true)
    setError(null)
    platformsApi.uploadImage(file)
      .then((result) => setImageUrl(result.url))
      .catch((err) => setError(err instanceof ApiError ? err.message : t('forms.uploadError')))
      .finally(() => setIsUploadingImage(false))
  }

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setTouched(true)
    if (!valid || isSubmitting || isUploadingImage) return
    setIsSubmitting(true)
    setError(null)
    setSuccess(false)
    onSubmit({
      name: name.trim(),
      description: description.trim() || undefined,
      imageUrl: imageUrl || undefined,
      primaryColor: primaryColor ? primaryColor.toUpperCase() : null,
      status,
      showInCatalog,
      displayOrder: order,
    })
      .then(() => { setSuccess(true); onSuccess?.() })
      .catch((err) => setError(err instanceof ApiError ? err.message : t('forms.saveError')))
      .finally(() => setIsSubmitting(false))
  }

  return (
    <Box component="form" noValidate onSubmit={handleSubmit}
      sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1fr) 360px' }, alignItems: 'start' }}>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, minWidth: 0 }}>
        {success && <Alert severity="success" role="status">{successMessage}</Alert>}
        {error && <Alert severity="error">{t('forms.saveErrorTitle')} — {error}</Alert>}

        <FormSection id="platform-basic" title={t('forms.sections.basic')} description={t('forms.platform.basicHint')}>
          <TextField label={t('forms.platform.name')} required fullWidth placeholder={t('forms.platform.namePlaceholder')}
            value={name} onChange={(e) => setName(e.target.value)} error={!!show('name')} helperText={show('name')}
            slotProps={{ htmlInput: { maxLength: 255 } }} />
          <TextField label={t('forms.platform.description')} fullWidth multiline minRows={3}
            placeholder={t('forms.platform.descriptionPlaceholder')} value={description} onChange={(e) => setDescription(e.target.value)}
            error={!!show('description')} helperText={show('description') ?? t('forms.platform.descriptionHint')}
            slotProps={{ htmlInput: { maxLength: 2000 } }} />
          <Box>
            <Typography variant="body2" sx={{ fontWeight: 600, mb: 1 }}>{t('forms.platform.logo')}</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ width: 56, height: 56, borderRadius: 2.5, border: '1px dashed', borderColor: 'divider', overflow: 'hidden', display: 'grid', placeItems: 'center', bgcolor: 'action.hover' }}>
                {imageUrl
                  ? <Box component="img" src={resolveAssetUrl(imageUrl)} alt={t('forms.platform.logoAlt')} sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  : <ImageUp size={20} aria-hidden />}
              </Box>
              <Button variant="outlined" disabled={isUploadingImage} onClick={() => fileInputRef.current?.click()}
                startIcon={isUploadingImage ? <CircularProgress size={16} /> : <ImageUp size={16} />}>
                {isUploadingImage ? t('forms.uploading') : t('forms.platform.uploadLogo')}
              </Button>
              <input ref={fileInputRef} type="file" accept="image/png,image/jpeg,image/gif,image/webp" hidden tabIndex={-1} aria-hidden onChange={handleImageSelected} />
              {imageUrl && (
                <Button color="inherit" startIcon={<Trash2 size={16} />} onClick={() => setImageUrl('')}>{t('forms.removeImage')}</Button>
              )}
            </Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 1 }}>{t('forms.imageHint')}</Typography>
          </Box>
        </FormSection>

        <FormSection id="platform-appearance" title={t('forms.sections.appearance')} description={t('forms.platform.appearanceHint')}>
          <ColorPicker id="platform-color" value={primaryColor} onChange={setPrimaryColor}
            defaultLabel={t('forms.platform.defaultColor')} defaultColor={DEFAULT_SHOWCASE_COLOR} />
        </FormSection>

        <FormSection id="platform-catalog" title={t('forms.sections.catalog')} description={t('forms.platform.catalogHint')}>
          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
            <TextField select label={t('forms.platform.status')} value={status} onChange={(e) => setStatus(e.target.value as PlatformStatus)}
              helperText={t('forms.platform.statusHint')}>
              <MenuItem value="ACTIVE">{t('forms.active')}</MenuItem>
              <MenuItem value="INACTIVE">{t('forms.inactive')}</MenuItem>
            </TextField>
            <TextField label={t('forms.platform.displayOrder')} value={displayOrder} onChange={(e) => setDisplayOrder(e.target.value.replace(/[^0-9]/g, ''))}
              error={!!show('displayOrder')} helperText={show('displayOrder') ?? t('forms.platform.displayOrderHint')}
              slotProps={{ htmlInput: { inputMode: 'numeric', maxLength: 4 } }} />
          </Box>
          <FormControlLabel control={<Switch checked={showInCatalog} onChange={(e) => setShowInCatalog(e.target.checked)} />}
            label={t('forms.platform.showInCatalog')} />
        </FormSection>

        <Paper variant="outlined" sx={{ p: 2, borderRadius: 3, display: 'flex', justifyContent: 'flex-end', gap: 1.5, position: 'sticky', bottom: 16, zIndex: 2 }}>
          {cancelTo && <Button component={RouterLink} to={cancelTo}>{t('forms.cancel')}</Button>}
          <Button type="submit" variant="contained" disabled={isSubmitting || isUploadingImage}
            startIcon={isSubmitting ? <CircularProgress size={16} color="inherit" /> : <Save size={16} />}>
            {isSubmitting ? submittingLabel : submitLabel}
          </Button>
        </Paper>
      </Box>

      <Box component="aside" aria-labelledby="platform-preview-title" sx={{ position: { lg: 'sticky' }, top: { lg: 88 } }}>
        <Typography id="platform-preview-title" component="h2" sx={{ fontWeight: 700, fontSize: 16, mb: 0.5 }}>{t('forms.preview.title')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('forms.preview.hint')}</Typography>
        <ShowcaseCard
          name={name.trim() || t('forms.platform.previewName')}
          typeLabel={t('catalog.platformType')}
          logoUrl={imageUrl || null}
          color={primaryColor}
          meta={previewAppCount !== undefined ? t('catalog.appCount', { count: previewAppCount }) : undefined}
          description={description.trim() || t('forms.platform.previewDescription')}
          status={status === 'ACTIVE' && showInCatalog
            ? { label: t('catalog.status.available'), tone: 'success' }
            : { label: t('forms.preview.hidden'), tone: 'neutral' }}
          actionLabel={t('catalog.viewDetails')}
          headingLevel="h3"
        />
      </Box>
    </Box>
  )
}
