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
import { productsApi, type BillingPeriod, type Currency, type Product, type ProductCreateRequest } from '../../api/productsApi'

const BILLING_PERIODS: { value: BillingPeriod; label: string }[] = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'YEARLY', label: 'Yearly' },
  { value: 'ONE_TIME', label: 'One-time' },
]

// 02.05.02.01 Configure currency (sprint 2026.4.1) — see Currency's own backend javadoc.
const CURRENCIES: Currency[] = ['USD', 'EUR', 'GBP', 'INR']

interface TierDraft {
  name: string
  price: string
  billingPeriod: BillingPeriod
  currency: Currency
  usageLimit: string
  includedFeatures: string
  usagePrice: string
  tierPricing: string
  overageCharge: string
}

const emptyTier = (): TierDraft => ({
  name: '', price: '', billingPeriod: 'MONTHLY', currency: 'USD',
  usageLimit: '', includedFeatures: '', usagePrice: '', tierPricing: '', overageCharge: '',
})

const tierDraftsFrom = (product?: Product): TierDraft[] =>
  (product?.plans ?? [])
    .slice()
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
    .map((p) => ({
      name: p.name, price: String(p.price), billingPeriod: p.billingPeriod, currency: p.currency,
      usageLimit: p.usageLimit !== undefined ? String(p.usageLimit) : '',
      includedFeatures: p.includedFeatures ?? '',
      usagePrice: p.usagePrice !== undefined ? String(p.usagePrice) : '',
      tierPricing: p.tierPricing ?? '',
      overageCharge: p.overageCharge !== undefined ? String(p.overageCharge) : '',
    }))

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
  // 02.01.01 Product Lifecycle (sprint 2026.4.1): editing a RETIRED product
  // through this form (e.g. to fix a typo) must not silently un-retire it —
  // that's what the dedicated Publish/Retire buttons on EditProductPage are
  // for. This switch still only ever toggles ACTIVE <-> INACTIVE.
  const wasRetired = initialProduct?.status === 'RETIRED'
  const [ssoConnected, setSsoConnected] = useState(initialProduct?.ssoConnected ?? false)
  const [platforms, setPlatforms] = useState<Platform[]>([])
  const [platformIds, setPlatformIds] = useState<number[]>(
    initialProduct?.platforms.map((p) => p.id) ?? []
  )
  const [tiers, setTiers] = useState<TierDraft[]>(tierDraftsFrom(initialProduct))
  // 02.01.02 Product Structure (sprint 2026.4.1).
  const [otherProducts, setOtherProducts] = useState<Product[]>([])
  const [parentProductId, setParentProductId] = useState<number | ''>(initialProduct?.parentProductId ?? '')
  const [variantLabel, setVariantLabel] = useState(initialProduct?.variantLabel ?? '')
  const [dependsOnProductIds, setDependsOnProductIds] = useState<number[]>(initialProduct?.dependsOnProductIds ?? [])
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
    // Every product, for the parent/dependency pickers — a product cannot be
    // its own parent or dependency, filtered out below at render time.
    productsApi.listAdmin().then(setOtherProducts).catch(() => setOtherProducts([]))
  }, [])

  const selectableProducts = otherProducts.filter((p) => p.id !== initialProduct?.id)

  const handlePlatformsChange = (e: SelectChangeEvent<number[]>) => {
    const value = e.target.value
    setPlatformIds(typeof value === 'string' ? value.split(',').map(Number) : value)
  }

  const handleDependsOnChange = (e: SelectChangeEvent<number[]>) => {
    const value = e.target.value
    setDependsOnProductIds(typeof value === 'string' ? value.split(',').map(Number) : value)
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
      status: wasRetired ? 'RETIRED' : (active ? 'ACTIVE' : 'INACTIVE'),
      ssoConnected,
      platformIds,
      plans: tiers.length > 0
        ? tiers.map((t, i) => ({
            name: t.name.trim(),
            price: Number(t.price),
            billingPeriod: t.billingPeriod,
            sortOrder: i + 1,
            currency: t.currency,
            usageLimit: t.usageLimit.trim() ? Number(t.usageLimit) : undefined,
            includedFeatures: t.includedFeatures.trim() || undefined,
            usagePrice: t.usagePrice.trim() ? Number(t.usagePrice) : undefined,
            tierPricing: t.tierPricing.trim() || undefined,
            overageCharge: t.overageCharge.trim() ? Number(t.overageCharge) : undefined,
          }))
        : undefined,
      parentProductId: parentProductId === '' ? undefined : parentProductId,
      variantLabel: variantLabel.trim() || undefined,
      dependsOnProductIds: dependsOnProductIds.length > 0 ? dependsOnProductIds : undefined,
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

        {wasRetired ? (
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            This product is retired. Use "Publish" above to bring it back on the storefront.
          </Typography>
        ) : (
          <FormControlLabel
            control={<Switch checked={active} onChange={(e) => setActive(e.target.checked)} />}
            label={active ? 'Active — visible on the storefront' : 'Inactive — hidden from the storefront'}
          />
        )}

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
          <Typography sx={{ fontWeight: 600, mb: 0.5 }}>Product structure</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>
            Optional — group this product under a parent, or list what it depends on.
          </Typography>

          <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
            <TextField
              select
              label="Parent product"
              size="small"
              sx={{ flex: 1 }}
              value={parentProductId}
              onChange={(e) => setParentProductId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <MenuItem value="">None — top-level</MenuItem>
              {selectableProducts.map((p) => (
                <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>
              ))}
            </TextField>
            <TextField
              label="Variant label"
              size="small"
              sx={{ flex: 1 }}
              placeholder="e.g. Enterprise, SMB"
              helperText="Only meaningful with a parent product."
              value={variantLabel}
              onChange={(e) => setVariantLabel(e.target.value)}
            />
          </Box>

          <FormControl fullWidth size="small">
            <InputLabel id="depends-on-label">Depends on</InputLabel>
            <Select
              labelId="depends-on-label"
              multiple
              value={dependsOnProductIds}
              onChange={handleDependsOnChange}
              input={<OutlinedInput label="Depends on" />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((id) => (
                    <Chip key={id} size="small" label={selectableProducts.find((p) => p.id === id)?.name ?? id} />
                  ))}
                </Box>
              )}
            >
              {selectableProducts.map((p) => (
                <MenuItem key={p.id} value={p.id}>
                  <Checkbox checked={dependsOnProductIds.includes(p.id)} />
                  <ListItemText primary={p.name} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        <Divider sx={{ my: 1 }} />

        <Box>
          <Typography sx={{ fontWeight: 600, mb: 0.5 }}>Pricing tiers</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>
            Optional — e.g. Basic / Pro / Enterprise, each with its own price and billing period.
          </Typography>

          {tiers.map((tier, index) => (
            <Box key={index} sx={{ mb: 2, p: 1.5, border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
              <Box sx={{ display: 'flex', gap: 1, mb: 1.5, alignItems: 'flex-start' }}>
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
                  label="Currency"
                  size="small"
                  value={tier.currency}
                  onChange={(e) => updateTier(index, { currency: e.target.value as Currency })}
                  sx={{ flex: 1 }}
                >
                  {CURRENCIES.map((c) => (
                    <MenuItem key={c} value={c}>{c}</MenuItem>
                  ))}
                </TextField>
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

              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <TextField
                  label="Included features"
                  size="small"
                  placeholder="Comma-separated, e.g. SSO, 5 seats, priority support"
                  value={tier.includedFeatures}
                  onChange={(e) => updateTier(index, { includedFeatures: e.target.value })}
                  sx={{ flex: 2, minWidth: 220 }}
                />
                <TextField
                  label="Usage limit"
                  size="small"
                  type="number"
                  slotProps={{ htmlInput: { min: 0 } }}
                  value={tier.usageLimit}
                  onChange={(e) => updateTier(index, { usageLimit: e.target.value })}
                  sx={{ flex: 1, minWidth: 120 }}
                />
                <TextField
                  label="Usage price"
                  size="small"
                  type="number"
                  helperText="Per unit beyond a flat plan"
                  slotProps={{ htmlInput: { min: 0, step: '0.0001' } }}
                  value={tier.usagePrice}
                  onChange={(e) => updateTier(index, { usagePrice: e.target.value })}
                  sx={{ flex: 1, minWidth: 140 }}
                />
                <TextField
                  label="Overage charge"
                  size="small"
                  type="number"
                  helperText="Per unit over the limit"
                  slotProps={{ htmlInput: { min: 0, step: '0.0001' } }}
                  value={tier.overageCharge}
                  onChange={(e) => updateTier(index, { overageCharge: e.target.value })}
                  sx={{ flex: 1, minWidth: 140 }}
                />
                <TextField
                  label="Tier pricing (display text)"
                  size="small"
                  placeholder="e.g. 1-100 units $2/unit, 101+ $1.50/unit"
                  value={tier.tierPricing}
                  onChange={(e) => updateTier(index, { tierPricing: e.target.value })}
                  sx={{ flex: 3, minWidth: 260 }}
                />
              </Box>
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
