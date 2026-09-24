import { Box, Chip, IconButton, Tooltip, Typography } from '@mui/material'
import { ExternalLink, Pencil, Trash2 } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { resolveAssetUrl } from '../../api/client'
import { type BillingPeriod, type Product } from '../../api/productsApi'
import { accentFor, iconFor } from '../../utils/accentColor'
import '../../styles/tiles.css'

const BILLING_SUFFIX: Record<BillingPeriod, string> = {
  MONTHLY: '/mo',
  YEARLY: '/yr',
  ONE_TIME: '',
}

interface ProductTileProps {
  product: Product
  /** Admin view: edit pencil + status/SSO/platform chips. */
  admin?: boolean
  /** Repeats a chip per assigned platform — noise inside a page already
   * scoped to one platform (a platform dashboard, or a single-category
   * storefront section), so callers scoped that way pass false. */
  showPlatformChips?: boolean
  /** Staggers this tile's entrance animation relative to its siblings. */
  animationDelay?: number
  /** Admin view only — omitted entirely (no trash icon shown) if not passed. */
  onDelete?: () => void
}

/**
 * One services-console tile: colored icon square, the app's name as a real
 * hyperlink straight to its `launchUrl` (not a separate button), then
 * description and pricing. Shared by the admin ProductGrid and the public
 * ProductsPage so both catalogs — and any future one — look identical.
 */
export function ProductTile({ product, admin = false, showPlatformChips = true, animationDelay = 0, onDelete }: ProductTileProps) {
  const accent = accentFor(product.name)
  const Icon = iconFor(product.name)
  const launchable = Boolean(product.launchUrl)

  return (
    <Box
      className="tile"
      style={{ animationDelay: `${animationDelay}ms`, ['--tile-accent' as string]: accent.fg }}
    >
      {admin && (
        <Box sx={{ position: 'absolute', top: 10, right: 10, display: 'flex', gap: 0.25 }}>
          <Tooltip title="Edit app">
            <IconButton
              component={RouterLink}
              to={`/admin/products/${product.id}/edit`}
              size="small"
              aria-label={`Edit ${product.name}`}
              sx={{
                color: 'text.secondary',
                '&:hover': { color: accent.fg, bgcolor: accent.bg },
              }}
            >
              <Pencil size={14} />
            </IconButton>
          </Tooltip>
          {onDelete && (
            <Tooltip title="Delete app">
              <IconButton
                size="small"
                onClick={onDelete}
                aria-label={`Delete ${product.name}`}
                sx={{
                  color: 'text.secondary',
                  '&:hover': { color: 'error.main', bgcolor: 'action.hover' },
                }}
              >
                <Trash2 size={14} />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      )}

      <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5, pr: admin ? 5.5 : 0 }}>
        <Box className="tile-icon" sx={{ bgcolor: accent.bg, color: accent.fg }}>
          {product.imageUrl ? (
            <Box
              component="img"
              src={resolveAssetUrl(product.imageUrl)}
              alt=""
              sx={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '12px' }}
            />
          ) : (
            <Icon size={22} strokeWidth={2} />
          )}
        </Box>
        <Box sx={{ minWidth: 0, flex: 1, pt: 0.25 }}>
          {/* A real heading, not just styled text — lets a screen-reader user
              jump card-to-card via heading navigation in a long grid, the
              same way a sighted user scans by the bold name (see this
              component's own doc on "accessible product cards"). */}
          <Typography component="h3" sx={{ fontWeight: 700, fontSize: 16, lineHeight: 1.3, m: 0 }}>
            {launchable ? (
              <Box
                component="a"
                href={product.launchUrl}
                target="_blank"
                rel="noopener"
                aria-label={`${product.name} (opens in a new tab)`}
                sx={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 0.5,
                  color: accent.fg,
                  textDecoration: 'none',
                  '&:hover': { textDecoration: 'underline' },
                }}
              >
                {product.name}
                <ExternalLink size={13} aria-hidden="true" style={{ opacity: 0.7, flexShrink: 0 }} />
              </Box>
            ) : (
              product.name
            )}
          </Typography>
          {product.category && (
            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
              {product.category}
            </Typography>
          )}
        </Box>
      </Box>

      {product.description && (
        <Typography
          variant="body2"
          sx={{
            color: 'text.secondary',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
          }}
        >
          {product.description}
        </Typography>
      )}

      {admin && (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
          {product.status === 'INACTIVE' && (
            <Chip size="small" label="Inactive" sx={{ bgcolor: 'action.disabledBackground', fontSize: 11 }} />
          )}
          {product.ssoConnected ? (
            <Chip size="small" label="SSO Connected" color="success" sx={{ fontSize: 11 }} />
          ) : (
            <Chip size="small" label="Standalone" variant="outlined" sx={{ fontSize: 11 }} />
          )}
          {showPlatformChips && product.platforms.map((platform) => (
            <Chip
              key={platform.id}
              size="small"
              label={platform.name}
              variant="outlined"
              sx={{ fontSize: 11, borderColor: accentFor(platform.name).fg, color: accentFor(platform.name).fg }}
            />
          ))}
        </Box>
      )}

      <Box sx={{ mt: 'auto', pt: 0.5 }}>
        {product.plans.length > 0 ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
            {product.plans.map((plan) => (
              <Box key={plan.id} sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>{plan.name}</Typography>
                <Typography variant="body2" sx={{ fontWeight: 700 }}>
                  ${plan.price.toFixed(2)}{BILLING_SUFFIX[plan.billingPeriod]}
                </Typography>
              </Box>
            ))}
          </Box>
        ) : (
          <Typography sx={{ fontWeight: 700 }}>${product.price.toFixed(2)}</Typography>
        )}
      </Box>
    </Box>
  )
}
