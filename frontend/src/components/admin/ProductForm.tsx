import { useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  FormControl,
  FormControlLabel,
  IconButton,
  InputAdornment,
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
import { Link as RouterLink } from 'react-router-dom'
import { ApiError, resolveAssetUrl } from '../../api/client'
import { platformsApi, type Platform } from '../../api/platformsApi'
import { productsApi, type BillingPeriod, type Currency, type Product, type ProductCreateRequest } from '../../api/productsApi'
import { appColor, isHexColor } from '../../utils/showcaseColor'
import { formatPlanPrice } from '../../utils/productPricing'
import { ColorPicker } from '../ui/ColorPicker'
import { FormSection } from '../ui/FormSection'
import { ShowcaseCard } from '../ui/ShowcaseCard'

const BILLING_PERIODS: BillingPeriod[] = ['MONTHLY', 'YEARLY', 'ONE_TIME']

// 02.05.02.01 Configure currency (sprint 2026.4.1) — see Currency's own backend javadoc.
const CURRENCIES: Currency[] = ['USD', 'EUR', 'GBP', 'INR']

// Backend limits (ProductCreateRequest): at most 12 tags of up to 40 characters, no commas.
const MAX_TAGS = 12
const MAX_TAG_LENGTH = 40
const URL_PATTERN = /^https?:\/\/.+/

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
  /** Where Cancel goes. */
  cancelTo?: string
}

/**
 * The app create/edit form (C66) — shared by ProductSettingsPage (create) and
 * EditProductPage (edit). Sections: Basic, Appearance, Launch & availability,
 * Integration, Product relationships, Features, Pricing, Resources, plus a
 * live Showcase Preview built from the same card as the catalog. Every field,
 * validation rule and payload property of the previous form is kept; the
 * showcase fields (accent colour, feature tags, documentation and support
 * links) are additive. There is no Versions section: the backend has no
 * release data (`version` is only an edit counter, shown on EditProductPage).
 */
export function ProductForm({
  initialProduct, onSubmit, submitLabel, submittingLabel, successMessage, onSuccess, cancelTo,
}: ProductFormProps) {
  const { t } = useTranslation()
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
  const [featured, setFeatured] = useState(initialProduct?.featured ?? false)
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
  // C66 showcase fields.
  const [accentColor, setAccentColor] = useState<string | null>(initialProduct?.accentColor ?? null)
  const [featureTags, setFeatureTags] = useState<string[]>(initialProduct?.featureTags ?? [])
  const [tagDraft, setTagDraft] = useState('')
  const [documentationUrl, setDocumentationUrl] = useState(initialProduct?.documentationUrl ?? '')
  const [supportUrl, setSupportUrl] = useState(initialProduct?.supportUrl ?? '')
  const [isUploadingImage, setIsUploadingImage] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [touched, setTouched] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const priceValue = Number(price)
  const errors = {
    name: !name.trim() ? t('forms.required') : null,
    price: !price.trim() || !(priceValue > 0) ? t('forms.app.priceError') : null,
    color: accentColor !== null && !isHexColor(accentColor) ? t('ui.color.hexError') : null,
    documentationUrl: documentationUrl.trim() && !URL_PATTERN.test(documentationUrl.trim()) ? t('forms.app.urlError') : null,
    supportUrl: supportUrl.trim() && !URL_PATTERN.test(supportUrl.trim()) ? t('forms.app.urlError') : null,
  }
  // Every tier row that exists must be fully filled in — an admin who wants to
  // abandon one should remove it, not leave it half-empty.
  const tierInvalid = (tier: TierDraft) => !tier.name.trim() || !(Number(tier.price) > 0)
  const tiersValid = tiers.every((tier) => !tierInvalid(tier))
  const valid = Object.values(errors).every((e) => !e) && tiersValid
  const show = (key: keyof typeof errors) => (touched ? errors[key] : null)

  useEffect(() => {
    platformsApi.list().then(setPlatforms).catch(() => setPlatforms([]))
    // Every product, for the parent/dependency pickers — a product cannot be
    // its own parent or dependency, filtered out below at render time.
    productsApi.listAdmin().then(setOtherProducts).catch(() => setOtherProducts([]))
  }, [])

  const selectableProducts = otherProducts.filter((p) => p.id !== initialProduct?.id)
  const selectedPlatforms = platforms.filter((p) => platformIds.includes(p.id))
  const inheritedColor = appColor({ platforms: selectedPlatforms })
  const previewColor = accentColor ?? inheritedColor

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
    setTiers((prev) => prev.map((tier, i) => (i === index ? { ...tier, ...patch } : tier)))

  const tagDraftValue = tagDraft.trim()
  const tagDuplicate = featureTags.some((tag) => tag.toLowerCase() === tagDraftValue.toLowerCase())
  const tagError = tagDraftValue.length > MAX_TAG_LENGTH ? t('forms.app.tagTooLong', { max: MAX_TAG_LENGTH })
    : tagDuplicate ? t('forms.app.tagDuplicate') : null
  const canAddTag = !!tagDraftValue && !tagError && featureTags.length < MAX_TAGS
  const addTag = () => {
    if (!canAddTag) return
    setFeatureTags((prev) => [...prev, tagDraftValue])
    setTagDraft('')
  }
  const handleTagKey = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') { e.preventDefault(); addTag() }
  }

  const handleImageSelected = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    setIsUploadingImage(true)
    setError(null)

    productsApi.uploadImage(file)
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
      price: priceValue,
      imageUrl: imageUrl || undefined,
      launchUrl: launchUrl.trim() || undefined,
      category: category.trim() || undefined,
      status: wasRetired ? 'RETIRED' : (active ? 'ACTIVE' : 'INACTIVE'),
      ssoConnected,
      featured,
      platformIds,
      plans: tiers.length > 0
        ? tiers.map((tier, i) => ({
            name: tier.name.trim(),
            price: Number(tier.price),
            billingPeriod: tier.billingPeriod,
            sortOrder: i + 1,
            currency: tier.currency,
            usageLimit: tier.usageLimit.trim() ? Number(tier.usageLimit) : undefined,
            includedFeatures: tier.includedFeatures.trim() || undefined,
            usagePrice: tier.usagePrice.trim() ? Number(tier.usagePrice) : undefined,
            tierPricing: tier.tierPricing.trim() || undefined,
            overageCharge: tier.overageCharge.trim() ? Number(tier.overageCharge) : undefined,
          }))
        : undefined,
      parentProductId: parentProductId === '' ? undefined : parentProductId,
      variantLabel: variantLabel.trim() || undefined,
      dependsOnProductIds: dependsOnProductIds.length > 0 ? dependsOnProductIds : undefined,
      accentColor: accentColor ? accentColor.toUpperCase() : null,
      featureTags,
      documentationUrl: documentationUrl.trim() || null,
      supportUrl: supportUrl.trim() || null,
    })
      .then(() => {
        setSuccess(true)
        onSuccess?.()
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : t('forms.saveError')))
      .finally(() => setIsSubmitting(false))
  }

  const sortedPreviewPlans = tiers.filter((tier) => tier.name.trim() && Number(tier.price) > 0)
  const previewExtra = (
    <Box sx={{ mt: 1.5, display: 'flex', flexDirection: 'column', gap: 0.25 }}>
      {sortedPreviewPlans.length > 0
        ? sortedPreviewPlans.map((tier, i) => (
          <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between', gap: 1 }}>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{tier.name}</Typography>
            <Typography variant="body2" sx={{ fontWeight: 700 }}>{formatPlanPrice(Number(tier.price), tier.currency, tier.billingPeriod)}</Typography>
          </Box>
        ))
        : priceValue > 0 && <Typography variant="body2" sx={{ fontWeight: 700 }}>{formatPlanPrice(priceValue, 'USD', 'ONE_TIME')}</Typography>}
    </Box>
  )
  const previewStatus = wasRetired
    ? { label: t('catalog.app.retired'), tone: 'neutral' as const }
    : active
      ? { label: t('catalog.status.available'), tone: 'success' as const }
      : { label: t('catalog.app.inactive'), tone: 'neutral' as const }

  return (
    <Box component="form" noValidate onSubmit={handleSubmit}
      sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1fr) 360px' }, alignItems: 'start' }}>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, minWidth: 0 }}>
        {success && <Alert severity="success" role="status">{successMessage}</Alert>}
        {error && <Alert severity="error">{t('forms.saveErrorTitle')} — {error}</Alert>}

        <FormSection id="app-basic" title={t('forms.sections.basic')} description={t('forms.app.basicHint')}>
          <TextField label={t('forms.app.name')} required fullWidth value={name} onChange={(e) => setName(e.target.value)}
            error={!!show('name')} helperText={show('name')} />
          <TextField label={t('forms.app.description')} fullWidth multiline minRows={3} value={description}
            onChange={(e) => setDescription(e.target.value)} helperText={t('forms.app.descriptionHint')} />
          <TextField label={t('forms.app.category')} fullWidth placeholder={t('forms.app.categoryPlaceholder')}
            value={category} onChange={(e) => setCategory(e.target.value)} helperText={t('forms.app.categoryHint')} />
          <Box>
            <Typography variant="body2" sx={{ fontWeight: 600, mb: 1 }}>{t('forms.app.logo')}</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ width: 56, height: 56, borderRadius: 2.5, border: '1px dashed', borderColor: 'divider', overflow: 'hidden', display: 'grid', placeItems: 'center', bgcolor: 'action.hover' }}>
                {imageUrl
                  ? <Box component="img" src={resolveAssetUrl(imageUrl)} alt={t('forms.app.logoAlt')} sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  : <ImageUp size={20} aria-hidden />}
              </Box>
              <Button variant="outlined" disabled={isUploadingImage} onClick={() => fileInputRef.current?.click()}
                startIcon={isUploadingImage ? <CircularProgress size={16} /> : <ImageUp size={16} />}>
                {isUploadingImage ? t('forms.uploading') : t('forms.app.uploadLogo')}
              </Button>
              <input ref={fileInputRef} type="file" accept="image/png,image/jpeg,image/gif,image/webp" hidden tabIndex={-1} aria-hidden onChange={handleImageSelected} />
              {imageUrl && (
                <Button color="inherit" startIcon={<Trash2 size={16} />} onClick={() => setImageUrl('')}>{t('forms.removeImage')}</Button>
              )}
            </Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 1 }}>{t('forms.imageHint')}</Typography>
          </Box>
        </FormSection>

        <FormSection id="app-appearance" title={t('forms.sections.appearance')} description={t('forms.app.appearanceHint')}>
          <ColorPicker id="app-color" value={accentColor} onChange={setAccentColor}
            defaultLabel={t('forms.app.inheritColor')} defaultColor={inheritedColor} />
        </FormSection>

        <FormSection id="app-launch" title={t('forms.sections.launch')} description={t('forms.app.launchHint')}>
          <TextField label={t('forms.app.launchUrl')} fullWidth placeholder="https://pms.evyoog.com or http://localhost:5173"
            helperText={t('forms.app.launchUrlHint')} value={launchUrl} onChange={(e) => setLaunchUrl(e.target.value)} />
          {wasRetired ? (
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('forms.app.retiredNote')}</Typography>
          ) : (
            <FormControlLabel control={<Switch checked={active} onChange={(e) => setActive(e.target.checked)} />}
              label={active ? t('forms.app.activeOn') : t('forms.app.activeOff')} />
          )}
          <Box>
            <FormControlLabel control={<Switch checked={featured} onChange={(e) => setFeatured(e.target.checked)} />}
              label={t('forms.app.featured')} />
            <Typography variant="body2" sx={{ color: 'text.secondary', ml: 1.5, mt: -0.5 }}>{t('forms.app.featuredHint')}</Typography>
          </Box>
        </FormSection>

        <FormSection id="app-integration" title={t('forms.sections.integration')} description={t('forms.app.integrationHint')}>
          <Box>
            <FormControlLabel control={<Switch checked={ssoConnected} onChange={(e) => setSsoConnected(e.target.checked)} />}
              label={t('forms.app.sso')} />
            <Typography variant="body2" sx={{ color: 'text.secondary', ml: 1.5, mt: -0.5 }}>{t('forms.app.ssoHint')}</Typography>
          </Box>
        </FormSection>

        <FormSection id="app-relationships" title={t('forms.sections.relationships')} description={t('forms.app.relationshipsHint')}>
          <FormControl fullWidth>
            <InputLabel id="platforms-label">{t('forms.app.platforms')}</InputLabel>
            <Select labelId="platforms-label" multiple value={platformIds} onChange={handlePlatformsChange}
              input={<OutlinedInput label={t('forms.app.platforms')} />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((id) => <Chip key={id} size="small" label={platforms.find((p) => p.id === id)?.name ?? id} />)}
                </Box>
              )}>
              {platforms.map((platform) => (
                <MenuItem key={platform.id} value={platform.id}>
                  <Checkbox checked={platformIds.includes(platform.id)} />
                  <ListItemText primary={platform.name} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
            <TextField select label={t('forms.app.parent')} value={parentProductId}
              onChange={(e) => setParentProductId(e.target.value === '' ? '' : Number(e.target.value))}>
              <MenuItem value="">{t('forms.app.parentNone')}</MenuItem>
              {selectableProducts.map((p) => <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>)}
            </TextField>
            <TextField label={t('forms.app.variant')} placeholder={t('forms.app.variantPlaceholder')}
              helperText={t('forms.app.variantHint')} value={variantLabel} onChange={(e) => setVariantLabel(e.target.value)} />
          </Box>
          <FormControl fullWidth>
            <InputLabel id="depends-on-label">{t('forms.app.dependsOn')}</InputLabel>
            <Select labelId="depends-on-label" multiple value={dependsOnProductIds} onChange={handleDependsOnChange}
              input={<OutlinedInput label={t('forms.app.dependsOn')} />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((id) => <Chip key={id} size="small" label={selectableProducts.find((p) => p.id === id)?.name ?? id} />)}
                </Box>
              )}>
              {selectableProducts.map((p) => (
                <MenuItem key={p.id} value={p.id}>
                  <Checkbox checked={dependsOnProductIds.includes(p.id)} />
                  <ListItemText primary={p.name} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </FormSection>

        <FormSection id="app-features" title={t('forms.sections.features')} description={t('forms.app.featuresHint', { max: MAX_TAGS })}>
          <TextField label={t('forms.app.addFeature')} fullWidth value={tagDraft} onChange={(e) => setTagDraft(e.target.value.replace(/,/g, ''))}
            onKeyDown={handleTagKey} error={!!tagError}
            helperText={tagError ?? (featureTags.length >= MAX_TAGS ? t('forms.app.tagLimit', { max: MAX_TAGS }) : t('forms.app.addFeatureHint'))}
            slotProps={{ input: { endAdornment: (
              <InputAdornment position="end">
                <Button size="small" onClick={addTag} disabled={!canAddTag} startIcon={<Plus size={14} />}>{t('forms.app.add')}</Button>
              </InputAdornment>
            ) } }} />
          {featureTags.length > 0 && (
            <Box role="list" aria-label={t('forms.sections.features')} sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
              {featureTags.map((tag) => (
                <Chip role="listitem" key={tag} label={tag} size="small"
                  aria-label={t('forms.app.removeFeature', { name: tag })}
                  onDelete={() => setFeatureTags((prev) => prev.filter((x) => x !== tag))} />
              ))}
            </Box>
          )}
        </FormSection>

        <FormSection id="app-pricing" title={t('forms.sections.pricing')} description={t('forms.app.pricingHint')}>
          <TextField label={t('forms.app.price')} required fullWidth type="number" sx={{ maxWidth: { sm: 280 } }}
            helperText={show('price') ?? t('forms.app.priceHint')} error={!!show('price')}
            slotProps={{ htmlInput: { min: 0, step: '0.01' } }} value={price} onChange={(e) => setPrice(e.target.value)} />

          {tiers.map((tier, index) => {
            const invalid = touched && tierInvalid(tier)
            return (
              <Box key={index} role="group" aria-label={t('forms.app.tierLabel', { n: index + 1 })}
                sx={{ p: 2, border: '1px solid', borderColor: invalid ? 'error.main' : 'divider', borderRadius: 2 }}>
                <Box sx={{ display: 'flex', gap: 1, mb: 1.5, alignItems: 'flex-start', flexWrap: { xs: 'wrap', md: 'nowrap' } }}>
                  <TextField label={t('forms.app.tierName')} size="small" value={tier.name} required
                    error={invalid && !tier.name.trim()} onChange={(e) => updateTier(index, { name: e.target.value })} sx={{ flex: 2, minWidth: 160 }} />
                  <TextField label={t('forms.app.price')} size="small" type="number" required
                    error={invalid && !(Number(tier.price) > 0)} slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                    value={tier.price} onChange={(e) => updateTier(index, { price: e.target.value })} sx={{ flex: 1, minWidth: 100 }} />
                  <TextField select label={t('forms.app.currency')} size="small" value={tier.currency}
                    onChange={(e) => updateTier(index, { currency: e.target.value as Currency })} sx={{ flex: 1, minWidth: 100 }}>
                    {CURRENCIES.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                  </TextField>
                  <TextField select label={t('forms.app.billing')} size="small" value={tier.billingPeriod}
                    onChange={(e) => updateTier(index, { billingPeriod: e.target.value as BillingPeriod })} sx={{ flex: 1, minWidth: 120 }}>
                    {BILLING_PERIODS.map((p) => <MenuItem key={p} value={p}>{t(`forms.app.period.${p}`)}</MenuItem>)}
                  </TextField>
                  <IconButton size="small" onClick={() => removeTier(index)} sx={{ mt: 0.5 }} aria-label={t('forms.app.removeTier')}>
                    <Trash2 size={16} />
                  </IconButton>
                </Box>
                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  <TextField label={t('forms.app.includedFeatures')} size="small" placeholder={t('forms.app.includedFeaturesPlaceholder')}
                    value={tier.includedFeatures} onChange={(e) => updateTier(index, { includedFeatures: e.target.value })} sx={{ flex: 2, minWidth: 220 }} />
                  <TextField label={t('forms.app.usageLimit')} size="small" type="number" slotProps={{ htmlInput: { min: 0 } }}
                    value={tier.usageLimit} onChange={(e) => updateTier(index, { usageLimit: e.target.value })} sx={{ flex: 1, minWidth: 120 }} />
                  <TextField label={t('forms.app.usagePrice')} size="small" type="number" helperText={t('forms.app.usagePriceHint')}
                    slotProps={{ htmlInput: { min: 0, step: '0.0001' } }} value={tier.usagePrice}
                    onChange={(e) => updateTier(index, { usagePrice: e.target.value })} sx={{ flex: 1, minWidth: 140 }} />
                  <TextField label={t('forms.app.overageCharge')} size="small" type="number" helperText={t('forms.app.overageChargeHint')}
                    slotProps={{ htmlInput: { min: 0, step: '0.0001' } }} value={tier.overageCharge}
                    onChange={(e) => updateTier(index, { overageCharge: e.target.value })} sx={{ flex: 1, minWidth: 140 }} />
                  <TextField label={t('forms.app.tierPricing')} size="small" placeholder={t('forms.app.tierPricingPlaceholder')}
                    value={tier.tierPricing} onChange={(e) => updateTier(index, { tierPricing: e.target.value })} sx={{ flex: 3, minWidth: 260 }} />
                </Box>
              </Box>
            )
          })}
          {touched && !tiersValid && <Typography variant="body2" color="error" role="alert">{t('forms.app.tierError')}</Typography>}
          <Box><Button size="small" startIcon={<Plus size={14} />} onClick={addTier}>{t('forms.app.addTier')}</Button></Box>
        </FormSection>

        <FormSection id="app-resources" title={t('forms.sections.resources')} description={t('forms.app.resourcesHint')}>
          <TextField label={t('forms.app.documentationUrl')} fullWidth placeholder="https://" value={documentationUrl}
            onChange={(e) => setDocumentationUrl(e.target.value)} error={!!show('documentationUrl')} helperText={show('documentationUrl')} />
          <TextField label={t('forms.app.supportUrl')} fullWidth placeholder="https://" value={supportUrl}
            onChange={(e) => setSupportUrl(e.target.value)} error={!!show('supportUrl')} helperText={show('supportUrl')} />
        </FormSection>

        <Paper variant="outlined" sx={{ p: 2, borderRadius: 3, display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 1.5, position: 'sticky', bottom: 16, zIndex: 2 }}>
          {touched && !valid && <Typography variant="body2" color="error" sx={{ mr: 'auto' }}>{t('forms.fixErrors')}</Typography>}
          {cancelTo && <Button component={RouterLink} to={cancelTo}>{t('forms.cancel')}</Button>}
          <Button type="submit" variant="contained" disabled={isSubmitting || isUploadingImage}
            startIcon={isSubmitting ? <CircularProgress size={16} color="inherit" /> : <Save size={16} />}>
            {isSubmitting ? submittingLabel : submitLabel}
          </Button>
        </Paper>
      </Box>

      <Box component="aside" aria-labelledby="app-preview-title" sx={{ position: { lg: 'sticky' }, top: { lg: 88 } }}>
        <Typography id="app-preview-title" component="h2" sx={{ fontWeight: 700, fontSize: 16, mb: 0.5 }}>{t('forms.preview.title')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('forms.preview.hint')}</Typography>
        <ShowcaseCard
          name={name.trim() || t('forms.app.previewName')}
          typeLabel={category.trim() || t('catalog.app.type')}
          logoUrl={imageUrl || null}
          color={isHexColor(previewColor) ? previewColor : inheritedColor}
          meta={[variantLabel.trim(), selectedPlatforms[0]?.name].filter(Boolean).join(' · ') || undefined}
          description={description.trim() || t('forms.app.previewDescription')}
          features={featureTags}
          status={previewStatus}
          actionLabel={t('catalog.viewDetails')}
          extra={previewExtra}
          headingLevel="h3"
        />
      </Box>
    </Box>
  )
}
