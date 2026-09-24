import { useEffect, useState } from 'react'
import { Box, Button, CircularProgress, Grid, Typography } from '@mui/material'
import { ArrowRight, Plus } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError, resolveAssetUrl } from '../../api/client'
import { platformsApi, type Platform } from '../../api/platformsApi'
import { productsApi, type Product } from '../../api/productsApi'
import { accentFor, iconFor } from '../../utils/accentColor'
import '../../styles/tiles.css'

/**
 * "/admin" — the admin landing page. Every high-level platform (e.g. Thittam),
 * each a doorway into its own scoped dashboard (PlatformDashboardPage), styled
 * as a services-console category grid rather than a plain list — this is
 * deliberately the FIRST thing an admin sees, not the flat all-apps list
 * (that lives at "/admin/apps" now), so the platform grouping stays the
 * primary mental model. App counts are computed client-side from the full
 * admin product list, same as AdminProductsPage's own stats.
 */
export function PlatformsListPage() {
  const [platforms, setPlatforms] = useState<Platform[] | null>(null)
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([platformsApi.list(), productsApi.listAdmin()])
      .then(([platformResult, productResult]) => {
        setPlatforms(platformResult)
        setProducts(productResult)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load platforms.'))
  }, [])

  const appCountFor = (platformId: number) =>
    products?.filter((p) => p.platforms.some((pl) => pl.id === platformId)).length ?? 0

  return (
    <>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3.5 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
            Product
          </Typography>
          <Typography sx={{ color: 'text.secondary' }}>
            Every product your apps are grouped under. Open one to see what's inside.
          </Typography>
        </Box>
        <Button
          component={RouterLink}
          to="/admin/settings/platform"
          variant="contained"
          startIcon={<Plus size={16} />}
        >
          Add Product
        </Button>
      </Box>

      {error && <Typography color="error" role="alert">{error}</Typography>}

      {!error && platforms === null && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress size={28} />
        </Box>
      )}

      {platforms !== null && platforms.length === 0 && (
        <Typography sx={{ color: 'text.secondary' }}>
          No platforms yet — add one to start grouping your apps.
        </Typography>
      )}

      {platforms !== null && platforms.length > 0 && (
        <Grid container spacing={2.5}>
          {platforms.map((platform, index) => {
            const accent = accentFor(platform.name)
            const Icon = iconFor(platform.name)
            const count = appCountFor(platform.id)
            return (
              <Grid key={platform.id} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
                <Box
                  component={RouterLink}
                  to={`/admin/platforms/${platform.id}`}
                  className="tile"
                  style={{ animationDelay: `${index * 45}ms`, ['--tile-accent' as string]: accent.fg }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                    <Box
                      className="tile-icon"
                      sx={{ bgcolor: accent.bg, color: accent.fg }}
                    >
                      {platform.imageUrl ? (
                        <Box
                          component="img"
                          src={resolveAssetUrl(platform.imageUrl)}
                          alt=""
                          sx={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '12px' }}
                        />
                      ) : (
                        <Icon size={22} strokeWidth={2} />
                      )}
                    </Box>
                    <Box sx={{ minWidth: 0, flex: 1, pt: 0.25 }}>
                      <Typography
                        sx={{
                          fontWeight: 700,
                          fontSize: 16,
                          color: accent.fg,
                          lineHeight: 1.3,
                        }}
                      >
                        {platform.name}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        {count} app{count === 1 ? '' : 's'}
                      </Typography>
                    </Box>
                  </Box>

                  {platform.description && (
                    <Typography
                      variant="body2"
                      sx={{
                        color: 'text.secondary',
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden',
                        flexGrow: 1,
                      }}
                    >
                      {platform.description}
                    </Typography>
                  )}

                  <Box
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.5,
                      mt: 'auto',
                      pt: 0.5,
                      fontSize: 13,
                      fontWeight: 600,
                      color: accent.fg,
                    }}
                  >
                    Explore apps
                    <ArrowRight size={14} className="tile-arrow" />
                  </Box>
                </Box>
              </Grid>
            )
          })}
        </Grid>
      )}
    </>
  )
}
