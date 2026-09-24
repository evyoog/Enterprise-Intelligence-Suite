import { useEffect, useRef, useState, type FormEvent } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  Divider,
  FormControl,
  FormControlLabel,
  IconButton,
  InputLabel,
  ListItemText,
  MenuItem,
  OutlinedInput,
  Paper,
  Select,
  type SelectChangeEvent,
  Switch,
  TextField,
  Typography,
} from '@mui/material'
import { ImageUp, Plus, Save, Trash2 } from 'lucide-react'
import { ApiError, resolveAssetUrl } from '../../api/client'
import { platformsApi, type Platform } from '../../api/platformsApi'
import { productsApi, type BillingPeriod, type Product, type ProductCreateRequest } from '../../api/productsApi'

const BILLING_PERIODS: { value: BillingPeriod; label: string }[] = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'YEARLY', label: 'Yearly' },
  { value: 'ONE_TIME', label: 'One-time' },
]

interface TierDraft {
  name: string
  price: string
  billingPeriod: BillingPeriod
}

const emptyTier = (): TierDraft => ({ name: '', price: '', billingPeriod: 'MONTHLY' })

const tierDraftsFrom = (product?: Product): TierDraft[] =>
  (product?.plans ?? [])
    .slice()
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
    .map((p) => ({ name: p.name, price: String(p.price), billingPeriod: p.billingPeriod }))

interface ProductFormProps {
  /** Present for editing an existing product; absent for creating a new one. */
  initialProduct?: Product
  onSubmit: (payload: ProductCreateRequest) => Promise<unknown>
  submitLabel: string
  submittingLabel: string
  successMessage: string
  /** Called after a successful submit — e.g. to navigate back to the list. */
  onSuccess?: () => void
}

/**
 * The product create/edit form — shared by ProductSettingsPage ("Add a
 * product") and EditProductPage ("Edit this product"). Both are the exact
 * same fields and validation; the only difference is whether `initialProduct`
 * prefills the state and what `onSubmit` actually calls (productsApi.create
 * vs productsApi.update).
 */
export function ProductForm({
  initialProduct, onSubmit, submitLabel, submittingLabel, successMessage, onSuccess,
}: ProductFormProps) {
  const [name, setName] = useState(initialProduct?.name ?? '')
  const [description, setDescription] = useState(initialProduct?.description ?? '')
  const [price, setPrice] = useState(initialProduct ? String(initialProduct.price) : '')
  const [imageUrl, setImageUrl] = useState(initialProduct?.imageUrl ?? '')
  const [launchUrl, setLaunchUrl] = useState(initialProduct?.launchUrl ?? '')
  const [category, setCategory] = useState(initialProduct?.category ?? '')
  const [active, setActive] = useState(initialProduct ? initialProduct.status === 'ACTIVE' : true)
  const [ssoConnected, setSsoConnected] = useState(initialProduct?.ssoConnected ?? false)
  const [platforms, setPlatforms] = useState<Platform[]>([])
  const [platformIds, setPlatformIds] = useState<number[]>(
    initialProduct?.platforms.map((p) => p.id) ?? []
  )
  const [tiers, setTiers] = useState<TierDraft[]>(tierDraftsFrom(initialProduct))
  const [isUploadingImage, setIsUploadingImage] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const priceValue = Number(price)
  // Every tier row that exists must be fully filled in — an admin who wants to
  // abandon one should remove it, not leave it half-empty.
  const tiersValid = tiers.every((t) => t.name.trim().length > 0 && Number(t.price) > 0)
  const canSubmit =
    name.trim().length > 0 && price.trim().length > 0 && priceValue > 0 &&
    tiersValid && !isSubmitting && !isUploadingImage

  useEffect(() => {
    platformsApi.list().then(setPlatforms).catch(() => setPlatforms([]))
  }, [])

  const handlePlatformsChange = (e: SelectChangeEvent<number[]>) => {
    const value = e.target.value
    setPlatformIds(typeof value === 'string' ? value.split(',').map(Number) : value)
  }

  const addTier = () => setTiers((prev) => [...prev, emptyTier()])
  const removeTier = (index: number) => setTiers((prev) => prev.filter((_, i) => i !== index))
  const updateTier = (index: number, patch: Partial<TierDraft>) =>
    setTiers((prev) => prev.map((t, i) => (i === index ? { ...t, ...patch } : t)))

  const handleImageSelected = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    setIsUploadingImage(true)
    setError(null)

    productsApi.uploadImage(file)
      .then((result) => setImageUrl(result.url))
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Could not upload the image.'))
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
      price: priceValue,
      imageUrl: imageUrl || undefined,
      launchUrl: launchUrl.trim() || undefined,
      category: category.trim() || undefined,
      status: active ? 'ACTIVE' : 'INACTIVE',
      ssoConnected,
      platformIds,
      plans: tiers.length > 0
        ? tiers.map((t, i) => ({
            name: t.name.trim(),
            price: Number(t.price),
            billingPeriod: t.billingPeriod,
            sortOrder: i + 1,
          }))
        : undefined,
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
          label="App name"
          required
          fullWidth
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

        <TextField
          label="Price"
          required
          fullWidth
          type="number"
          helperText="Shown only if no pricing tiers are added below."
          slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
          value={price}
          onChange={(e) => setPrice(e.target.value)}
        />

        <TextField
          label="Category"
          fullWidth
          placeholder="e.g. PMS, HR, ERP"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
        />

        <Box>
          <Button
            component="label"
            variant="outlined"
            startIcon={isUploadingImage ? <CircularProgress size={16} /> : <ImageUp size={16} />}
            disabled={isUploadingImage}
          >
            {isUploadingImage ? 'Uploading…' : 'Upload Image'}
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
              sx={{ display: 'block', mt: 1.5, maxHeight: 160, borderRadius: 1, border: '1px solid', borderColor: 'divider' }}
            />
          )}
        </Box>

        <TextField
          label="Launch URL"
          fullWidth
          placeholder="https://pms.evyoog.com or http://localhost:5173"
          helperText="Where the product card's Launch button opens — a live URL or a local one for testing."
          value={launchUrl}
          onChange={(e) => setLaunchUrl(e.target.value)}
        />

        <FormControlLabel
          control={<Switch checked={active} onChange={(e) => setActive(e.target.checked)} />}
          label={active ? 'Active — visible on the storefront' : 'Inactive — hidden from the storefront'}
        />

        <Box>
          <FormControlLabel
            control={<Switch checked={ssoConnected} onChange={(e) => setSsoConnected(e.target.checked)} />}
            label="Connected via Vyoog SSO"
          />
          <Typography variant="body2" sx={{ color: 'text.secondary', ml: 1.5, mt: -0.5 }}>
            Turn this on once the app's own backend has the SSO bridge wired up — it just
            controls the badge shown on this app's card, it doesn't configure anything itself.
          </Typography>
        </Box>

        <FormControl fullWidth>
          <InputLabel id="platforms-label">Platforms</InputLabel>
          <Select
            labelId="platforms-label"
            multiple
            value={platformIds}
            onChange={handlePlatformsChange}
            input={<OutlinedInput label="Platforms" />}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {selected.map((id) => (
                  <Chip key={id} size="small" label={platforms.find((p) => p.id === id)?.name ?? id} />
                ))}
              </Box>
            )}
          >
            {platforms.map((platform) => (
              <MenuItem key={platform.id} value={platform.id}>
                <Checkbox checked={platformIds.includes(platform.id)} />
                <ListItemText primary={platform.name} />
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <Divider sx={{ my: 1 }} />

        <Box>
          <Typography sx={{ fontWeight: 600, mb: 0.5 }}>Pricing tiers</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>
            Optional — e.g. Basic / Pro / Enterprise, each with its own price and billing period.
          </Typography>

          {tiers.map((tier, index) => (
            <Box key={index} sx={{ display: 'flex', gap: 1, mb: 1.5, alignItems: 'flex-start' }}>
              <TextField
                label="Tier name"
                size="small"
                value={tier.name}
                onChange={(e) => updateTier(index, { name: e.target.value })}
                sx={{ flex: 2 }}
              />
              <TextField
                label="Price"
                size="small"
                type="number"
                slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                value={tier.price}
                onChange={(e) => updateTier(index, { price: e.target.value })}
                sx={{ flex: 1 }}
              />
              <TextField
                select
                label="Billing"
                size="small"
                value={tier.billingPeriod}
                onChange={(e) => updateTier(index, { billingPeriod: e.target.value as BillingPeriod })}
                sx={{ flex: 1 }}
              >
                {BILLING_PERIODS.map((p) => (
                  <MenuItem key={p.value} value={p.value}>{p.label}</MenuItem>
                ))}
              </TextField>
              <IconButton size="small" onClick={() => removeTier(index)} sx={{ mt: 0.5 }} aria-label="Remove tier">
                <Trash2 size={16} />
              </IconButton>
            </Box>
          ))}

          <Button size="small" startIcon={<Plus size={14} />} onClick={addTier}>
            Add tier
          </Button>
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
