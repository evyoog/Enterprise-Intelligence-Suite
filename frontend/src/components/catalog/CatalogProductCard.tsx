import { useTranslation } from 'react-i18next'
import { Box, Button, Card, Chip, IconButton, Tooltip, Typography } from '@mui/material'
import { ExternalLink, Heart, ShoppingCart, Star } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { resolveAssetUrl } from '../../api/client'
import type { Product } from '../../api/productsApi'
import { fromPlan, isNewProduct, pricingModelOf, type Rating } from '../../utils/catalogFilters'
import { formatPlanPrice } from '../../utils/productPricing'
import { appColor, showcasePalette } from '../../utils/showcaseColor'

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

const ICON = 44

/**
 * C94/C95 (REQ-CAT-003.13): the public catalog's compact product card. A banner strip (the product's banner image, else a
 * tint of its colour) with the Featured and New badges and the wishlist heart; the app icon overlaps the strip's lower
 * edge; then name and From price, category and rating, a short description and the Subscribe and Add to cart actions.
 * `variant="list"` lays the same content out as a row.
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
  const meta = [product.category, product.platforms[0]?.name].filter(Boolean).join(' · ')

  // The form preview must not repeat the product name in every label.
  const heartLabel = preview
    ? t('wishlist.add')
    : saved ? t('wishlist.removeNamed', { name: product.name }) : t('wishlist.addNamed', { name: product.name })

  const badge = (label: string, bg: string) => (
    <Box component="span" sx={{ px: 0.75, py: 0.125, borderRadius: 1, fontSize: 10.5, fontWeight: 700, bgcolor: bg, color: '#fff', lineHeight: 1.5 }}>{label}</Box>
  )

  const price = plan
    ? t('catalog.card.from', { price: formatPlanPrice(plan.price, plan.currency, plan.billingPeriod) })
    : model === 'ONE_TIME'
      ? t('catalog.card.from', { price: formatPlanPrice(product.price, 'USD', 'ONE_TIME') })
      : t('catalog.pricingModel.FREE')

  const strip = (
    <Box sx={{
      position: 'relative', flexShrink: 0, height: list ? 'auto' : 72, width: list ? 132 : 'auto', borderRadius: list ? 1.5 : 0, overflow: 'hidden',
      background: `linear-gradient(135deg, ${palette.soft} 0%, ${palette.border} 100%)`,
    }}>
      {product.bannerImageUrl && (
        <Box component="img" src={resolveAssetUrl(product.bannerImageUrl)} alt="" sx={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' }} />
      )}
      <Box sx={{ position: 'absolute', top: 6, left: 6, display: 'flex', gap: 0.5 }}>
        {product.featured && badge(t('catalog.card.featured'), palette.text)}
        {isNewProduct(product) && badge(t('catalog.card.new'), '#059669')}
      </Box>
      {!list && (
        <Tooltip title={saved ? t('wishlist.remove') : t('wishlist.add')}>
          <span>
            <IconButton size="small" aria-pressed={saved} disabled={preview} onClick={onToggleSaved}
              aria-label={heartLabel}
              sx={{ position: 'absolute', top: 4, right: 4, zIndex: 2, p: 0.5, bgcolor: 'background.paper', color: saved ? 'error.main' : 'text.secondary', '&:hover': { bgcolor: 'background.paper', color: 'error.main' } }}>
              <Heart size={15} aria-hidden fill={saved ? 'currentColor' : 'none'} />
            </IconButton>
          </span>
        </Tooltip>
      )}
    </Box>
  )

  const icon = (
    <Box sx={{
      width: ICON, height: ICON, flexShrink: 0, borderRadius: 2.5, display: 'grid', placeItems: 'center', overflow: 'hidden', bgcolor: 'background.paper', color: palette.text,
      fontWeight: 800, fontSize: 15, border: '3px solid', borderColor: 'background.paper', boxShadow: '0 3px 10px -4px rgba(15,23,42,.35)',
      ...(list ? {} : { position: 'absolute', left: 12, top: -ICON / 2 }),
    }}>
      {product.imageUrl
        ? <Box component="img" src={resolveAssetUrl(product.imageUrl)} alt="" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        : initials(product.name)}
    </Box>
  )

  const name = preview
    ? <span>{product.name}</span>
    : (
      <Box component={RouterLink} to={`/products/${product.id}`} sx={{ color: 'inherit', textDecoration: 'none', '&:hover': { color: palette.text }, '&::after': { content: '""', position: list ? 'static' : 'absolute', inset: 0, zIndex: 0 } }}>
        {product.name}
      </Box>
    )

  const body = (
    <Box sx={{ position: 'relative', display: 'flex', flexDirection: 'column', flex: 1, minWidth: 0, p: list ? 0 : 1.5, pt: list ? 0 : 1 }}>
      {!list && icon}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minHeight: list ? 0 : ICON / 2 + 4, pl: list ? 0 : `${ICON + 12}px`, mt: list ? 0 : -0.25 }}>
        <Typography component="h3" sx={{ fontWeight: 700, fontSize: 15, lineHeight: 1.25, overflowWrap: 'anywhere', flex: 1, minWidth: 0 }}>
          {name}
          {inactive && <Chip size="small" label={t(product.status === 'RETIRED' ? 'catalog.app.retired' : 'catalog.app.inactive')} sx={{ ml: 1, height: 18 }} />}
        </Typography>
        <Typography component="span" sx={{ fontWeight: 700, fontSize: 13, whiteSpace: 'nowrap' }}>{price}</Typography>
      </Box>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, mt: 0.5 }}>
        <Typography variant="caption" sx={{ color: 'text.secondary', minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{meta}</Typography>
        {rating && (
          <Typography component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.375, fontSize: 12, whiteSpace: 'nowrap' }}
            aria-label={t('catalog.card.ratingLabel', { rating: rating.averageRating.toFixed(1), count: rating.reviewCount })}>
            <Star size={12} aria-hidden fill="#F59E0B" color="#F59E0B" />
            <b>{rating.averageRating.toFixed(1)}</b>
            <Box component="span" sx={{ color: 'text.secondary' }}>({rating.reviewCount})</Box>
          </Typography>
        )}
      </Box>
      {product.description && (
        <Typography sx={{ color: 'text.secondary', fontSize: 12.5, lineHeight: 1.4, mt: 0.75, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {product.description}
        </Typography>
      )}
      <Box sx={{ flexGrow: 1 }} />
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, mt: 1, position: 'relative', zIndex: 1 }}>
        <Button size="small" variant="contained" disabled={preview} onClick={onSubscribe} sx={{ flex: list ? '0 0 auto' : 1, minWidth: list ? 120 : 0, py: 0.25 }}>{t('catalog.app.subscribe')}</Button>
        <Tooltip title={addToCartUnavailable ? t('cart.noPaidPlan') : ''}>
          <Box component="span" sx={{ display: 'inline-flex', flex: list ? '0 0 auto' : 1, minWidth: list ? 140 : 0 }}>
            <Button size="small" variant="outlined" fullWidth disabled={preview || addingToCart || addToCartUnavailable} onClick={onAddToCart}
              startIcon={<ShoppingCart size={13} aria-hidden />} aria-label={preview ? undefined : t('cart.addToCartLabel', { name: product.name })} sx={{ color: palette.text, py: 0.25, whiteSpace: 'nowrap' }}>
              {t('cart.addToCart')}
            </Button>
          </Box>
        </Tooltip>
        {product.launchUrl && (
          <Tooltip title={t('catalog.app.launch')}>
            <IconButton size="small" component="a" href={preview ? undefined : product.launchUrl} target="_blank" rel="noopener"
              aria-label={preview ? t('catalog.app.launch') : t('catalog.app.launchLabel', { name: product.name })} sx={{ color: palette.text }}>
              <ExternalLink size={15} aria-hidden />
            </IconButton>
          </Tooltip>
        )}
        {list && (
          <Tooltip title={saved ? t('wishlist.remove') : t('wishlist.add')}>
            <span>
              <IconButton size="small" aria-pressed={saved} disabled={preview} onClick={onToggleSaved}
                aria-label={heartLabel}
                sx={{ color: saved ? 'error.main' : 'text.secondary', '&:hover': { color: 'error.main' } }}>
                <Heart size={15} aria-hidden fill={saved ? 'currentColor' : 'none'} />
              </IconButton>
            </span>
          </Tooltip>
        )}
      </Box>
    </Box>
  )

  return (
    <Card sx={{
      height: '100%', display: 'flex', flexDirection: list ? 'row' : 'column', gap: list ? 1.5 : 0, p: list ? 1 : 0, position: 'relative', overflow: 'hidden',
      transition: 'border-color .15s, box-shadow .15s, transform .15s',
      ...(preview ? {} : { '&:hover': { borderColor: palette.border, boxShadow: '0 8px 24px -14px rgba(15,23,42,.35)', transform: 'translateY(-1px)' } }),
      '@media (prefers-reduced-motion: reduce)': { transition: 'none', '&:hover': { transform: 'none' } },
    }}>
      {strip}
      {list && icon}
      {body}
    </Card>
  )
}
