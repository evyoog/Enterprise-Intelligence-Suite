import { Box, Button, Chip, IconButton, Tooltip, Typography } from '@mui/material'
import { ExternalLink, Pencil, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import type { Product } from '../../api/productsApi'
import { appColor, showcasePalette } from '../../utils/showcaseColor'
import { formatPlanPrice } from '../../utils/productPricing'
import { ShowcaseCard } from '../ui/ShowcaseCard'

interface ProductTileProps {
  product: Product
  /** Admin view: edit / delete actions and status, SSO and platform chips. */
  admin?: boolean
  /** One chip per assigned platform — noise inside a page already scoped to
   * one platform, so callers scoped that way pass false. */
  showPlatformChips?: boolean
  /** Kept for existing callers; C66 cards no longer animate in. */
  animationDelay?: number
  /** Admin view only — no delete action when not passed. */
  onDelete?: () => void
  /** Public catalog (C48): a Subscribe action right on the card. */
  onSubscribe?: () => void
}

/**
 * The app card (C66): the shared ShowcaseCard with the app's colour (its own,
 * else its platform's, else the EIS default), category badge, feature tags,
 * pricing (tiers when present, otherwise the flat price — the existing rule),
 * Launch when a launch URL is set, and View details. Used by the catalog, the
 * platform pages and the admin app grids.
 */
export function ProductTile({ product, admin = false, showPlatformChips = true, onDelete, onSubscribe }: ProductTileProps) {
  const { t } = useTranslation()
  const color = appColor(product)
  const palette = showcasePalette(color)
  const inactive = product.status !== 'ACTIVE'

  const extra = (
    <Box sx={{ mt: 1.5, display: 'flex', flexDirection: 'column', gap: 1 }}>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.25 }}>
        {product.plans.length > 0
          ? product.plans.slice().sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)).map((plan) => (
            <Box key={plan.id} sx={{ display: 'flex', justifyContent: 'space-between', gap: 1 }}>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{plan.name}</Typography>
              <Typography variant="body2" sx={{ fontWeight: 700 }}>{formatPlanPrice(plan.price, plan.currency, plan.billingPeriod)}</Typography>
            </Box>
          ))
          : <Typography variant="body2" sx={{ fontWeight: 700 }}>{formatPlanPrice(product.price, 'USD', 'ONE_TIME')}</Typography>}
      </Box>
      {admin && (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
          {product.status === 'INACTIVE' && <Chip size="small" label={t('catalog.app.inactive')} />}
          {product.status === 'RETIRED' && <Chip size="small" label={t('catalog.app.retired')} />}
          {product.ssoConnected
            ? <Chip size="small" color="success" variant="outlined" label={t('catalog.app.ssoConnected')} />
            : <Chip size="small" variant="outlined" label={t('catalog.app.standalone')} />}
          {product.featured && <Chip size="small" variant="outlined" color="primary" label={t('catalog.app.featured')} />}
          {showPlatformChips && product.platforms.map((p) => <Chip key={p.id} size="small" variant="outlined" label={p.name} />)}
        </Box>
      )}
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
        {product.launchUrl && (
          <Button size="small" variant="outlined" href={product.launchUrl} target="_blank" rel="noopener"
            endIcon={<ExternalLink size={13} aria-hidden />} aria-label={t('catalog.app.launchLabel', { name: product.name })}
            sx={{ color: palette.text }}>
            {t('catalog.app.launch')}
          </Button>
        )}
        {onSubscribe && (
          <Button size="small" variant="contained" onClick={onSubscribe}>{t('catalog.app.subscribe')}</Button>
        )}
        {admin && (
          <Box sx={{ ml: 'auto', display: 'flex', gap: 0.25 }}>
            <Tooltip title={t('catalog.app.edit')}>
              <IconButton component={RouterLink} to={`/admin/products/${product.id}/edit`} size="small"
                aria-label={t('catalog.app.editLabel', { name: product.name })}>
                <Pencil size={15} />
              </IconButton>
            </Tooltip>
            {onDelete && (
              <Tooltip title={t('catalog.app.delete')}>
                <IconButton size="small" onClick={onDelete} aria-label={t('catalog.app.deleteLabel', { name: product.name })}
                  sx={{ '&:hover': { color: 'error.main' } }}>
                  <Trash2 size={15} />
                </IconButton>
              </Tooltip>
            )}
          </Box>
        )}
      </Box>
    </Box>
  )

  return (
    <ShowcaseCard
      name={product.name}
      typeLabel={product.category || t('catalog.app.type')}
      logoUrl={product.imageUrl}
      color={color}
      meta={[product.variantLabel, product.platforms[0]?.name].filter(Boolean).join(' · ') || undefined}
      description={product.description}
      features={product.featureTags ?? []}
      status={inactive
        ? { label: t(product.status === 'RETIRED' ? 'catalog.app.retired' : 'catalog.app.inactive'), tone: 'neutral' }
        : { label: t('catalog.status.available'), tone: 'success' }}
      actionLabel={t('catalog.viewDetails')}
      to={`/products/${product.id}`}
      extra={extra}
    />
  )
}
