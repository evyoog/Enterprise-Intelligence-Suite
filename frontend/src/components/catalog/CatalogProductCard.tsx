import { useTranslation } from 'react-i18next'
import { Box, Button, Card, Chip, IconButton, Tooltip, Typography } from '@mui/material'
import { ExternalLink, Heart, ShoppingCart, Star } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { resolveAssetUrl } from '../../api/client'
import type { Product } from '../../api/productsApi'
import { fromPlan, isNewProduct, pricingModelOf, type Rating } from '../../utils/catalogFilters'
import { formatPlanPrice } from '../../utils/productPricing'
import { appColor, showcasePalette } from '../../utils/showcaseColor'
import { FeatureTag } from '../ui/FeatureTag'

export interface CatalogProductCardProps {
  product: Product
  variant?: 'grid' | 'list'
  rating?: Rating
  saved: boolean
  onToggleSaved: () => void
  onSubscribe: () => void
  onAddToCart: () => void
  addToCartUnavailable: boolean
  addingToCart: boolean
  /** The admin form's live preview: the same card, with nothing clickable. */
  preview?: boolean
}

function initials(name: string) {
  const words = name.trim().split(/\s+/)
  return (words.length >= 2 ? words[0][0] + words[1][0] : name.slice(0, 2)).toUpperCase()
}

/**
 * C94/C95 (REQ-CAT-003.13): the public catalog's product card. A banner (the product's banner image, else a tint of its colour) with the app icon, the Featured and New badges and the
 * wishlist heart; then name, category, description, feature tags, rating, the From price and the Subscribe and Add to cart
 * actions. `variant="list"` lays the same content out as a row.
 */
export function CatalogProductCard({
  product, variant = 'grid', rating, saved, onToggleSaved, onSubscribe, onAddToCart, addToCartUnavailable, addingToCart, preview = false,
}: CatalogProductCardProps) {
  const { t } = useTranslation()
  const palette = showcasePalette(appColor(product))
  const list = variant === 'list'
  const inactive = product.status !== 'ACTIVE'
  const plan = fromPlan(product)
  const model = pricingModelOf(product)
  const features = product.featureTags ?? []
  const meta = [product.category, product.platforms[0]?.name].filter(Boolean).join(' · ')

  // The form preview must not repeat the product name in every label.
  const heartLabel = preview
    ? t('wishlist.add')
    : saved ? t('wishlist.removeNamed', { name: product.name }) : t('wishlist.addNamed', { name: product.name })

  const badge = (label: string, bg: string, fg = '#fff') => (
    <Box component="span" sx={{ px: 1, py: 0.25, borderRadius: 1.5, fontSize: 11, fontWeight: 700, bgcolor: bg, color: fg, lineHeight: 1.4 }}>{label}</Box>
  )

  const price = plan
    ? t('catalog.card.from', { price: formatPlanPrice(plan.price, plan.currency, plan.billingPeriod) })
    : model === 'ONE_TIME'
      ? t('catalog.card.from', { price: formatPlanPrice(product.price, 'USD', 'ONE_TIME') })
      : t('catalog.pricingModel.FREE')

  const banner = (
    <Box sx={{
      position: 'relative', flexShrink: 0, height: list ? 'auto' : 112, width: list ? 150 : 'auto', borderRadius: list ? 2 : 0, overflow: 'hidden',
      background: `linear-gradient(135deg, ${palette.soft} 0%, ${palette.border} 100%)`, display: 'grid', placeItems: 'center',
    }}>
      {product.bannerImageUrl && (
        <Box component="img" src={resolveAssetUrl(product.bannerImageUrl)} alt="" sx={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover', borderRadius: list ? 2 : 0 }} />
      )}
      <Box sx={{ position: 'relative', width: 56, height: 56, borderRadius: 3, display: 'grid', placeItems: 'center', overflow: 'hidden', bgcolor: 'background.paper', color: palette.text, fontWeight: 800, fontSize: 18, boxShadow: '0 4px 14px -6px rgba(15,23,42,.35)' }}>
        {product.imageUrl
          ? <Box component="img" src={resolveAssetUrl(product.imageUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          : initials(product.name)}
      </Box>
      <Box sx={{ position: 'absolute', top: 8, left: 8, display: 'flex', gap: 0.5 }}>
        {product.featured && badge(t('catalog.card.featured'), palette.text)}
        {isNewProduct(product) && badge(t('catalog.card.new'), '#059669')}
      </Box>
      {product.launchUrl && (
        <Tooltip title={t('catalog.app.launch')}>
          <IconButton size="small" component="a" href={preview ? undefined : product.launchUrl} target="_blank" rel="noopener"
            aria-label={preview ? t('catalog.app.launch') : t('catalog.app.launchLabel', { name: product.name })}
            sx={{ position: 'absolute', bottom: 6, right: 6, zIndex: 2, bgcolor: 'background.paper', color: palette.text, '&:hover': { bgcolor: 'background.paper' } }}>
            <ExternalLink size={15} aria-hidden />
          </IconButton>
        </Tooltip>
      )}
      <Tooltip title={saved ? t('wishlist.remove') : t('wishlist.add')}>
        <span><IconButton size="small" aria-pressed={saved} disabled={preview} onClick={onToggleSaved}
          aria-label={heartLabel}
          sx={{ position: 'absolute', top: 6, right: 6, zIndex: 2, bgcolor: 'background.paper', color: saved ? 'error.main' : 'text.secondary', '&:hover': { bgcolor: 'background.paper', color: 'error.main' } }}>
          <Heart size={16} aria-hidden fill={saved ? 'currentColor' : 'none'} />
        </IconButton></span>
      </Tooltip>
    </Box>
  )

  const body = (
    <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1, minWidth: 0, p: list ? 0 : 1.75 }}>
      <Typography component="h3" sx={{ fontWeight: 700, fontSize: 16, lineHeight: 1.3, overflowWrap: 'anywhere' }}>
        {preview ? product.name : (
          <Box component={RouterLink} to={`/products/${product.id}`} sx={{ color: 'inherit', textDecoration: 'none', '&:hover': { color: palette.text }, '&::after': { content: '""', position: list ? 'static' : 'absolute', inset: 0, zIndex: 0 } }}>
            {product.name}
          </Box>
        )}
        {inactive && <Chip size="small" label={t(product.status === 'RETIRED' ? 'catalog.app.retired' : 'catalog.app.inactive')} sx={{ ml: 1 }} />}
      </Typography>
      {meta && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.25 }}>{meta}</Typography>}
      {product.description && (
        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {product.description}
        </Typography>
      )}
      {features.length > 0 && (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75, mt: 1.25 }}>
          {features.slice(0, 3).map((f) => <FeatureTag key={f} label={f} />)}
          {features.length > 3 && <FeatureTag label={`+${features.length - 3}`} />}
        </Box>
      )}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mt: 1.25, flexWrap: 'wrap' }}>
        {rating && (
          <Typography component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, fontSize: 13 }}
            aria-label={t('catalog.card.ratingLabel', { rating: rating.averageRating.toFixed(1), count: rating.reviewCount })}>
            <Star size={14} aria-hidden fill="#F59E0B" color="#F59E0B" />
            <b>{rating.averageRating.toFixed(1)}</b>
            <Box component="span" sx={{ color: 'text.secondary' }}>{t('catalog.card.reviews', { count: rating.reviewCount })}</Box>
          </Typography>
        )}
        <Typography component="span" sx={{ fontWeight: 700, fontSize: 14 }}>{price}</Typography>
      </Box>
      <Box sx={{ flexGrow: 1 }} />
      <Box sx={{ display: 'flex', gap: 1, mt: 1.75, position: 'relative', zIndex: 1, flexWrap: 'wrap' }}>
        <Button size="small" variant="contained" disabled={preview} onClick={onSubscribe} sx={{ flex: '0 0 auto', px: 1.5 }}>{t('catalog.app.subscribe')}</Button>
        <Tooltip title={addToCartUnavailable ? t('cart.noPaidPlan') : ''}>
          <Box component="span" sx={{ display: 'inline-flex', flex: list ? '0 0 auto' : 1, minWidth: 0 }}>
            <Button size="small" variant="outlined" fullWidth disabled={preview || addingToCart || addToCartUnavailable} onClick={onAddToCart}
              startIcon={<ShoppingCart size={14} aria-hidden />} aria-label={preview ? undefined : t('cart.addToCartLabel', { name: product.name })} sx={{ color: palette.text, px: 1, whiteSpace: 'nowrap' }}>
              {t('cart.addToCart')}
            </Button>
          </Box>
        </Tooltip>
      </Box>
    </Box>
  )

  return (
    <Card sx={{
      height: '100%', display: 'flex', flexDirection: list ? 'row' : 'column', gap: list ? 2 : 0, p: list ? 1.5 : 0, position: 'relative', overflow: 'hidden',
      transition: 'border-color .15s, box-shadow .15s, transform .15s',
      '&:hover': { borderColor: palette.border, boxShadow: '0 8px 24px -14px rgba(15,23,42,.35)', transform: 'translateY(-1px)' },
      '@media (prefers-reduced-motion: reduce)': { transition: 'none', '&:hover': { transform: 'none' } },
      flexWrap: { xs: list ? 'wrap' : 'nowrap', sm: 'nowrap' },
    }}>
      {banner}
      {body}
    </Card>
  )
}
