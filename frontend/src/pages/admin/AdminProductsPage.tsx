import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Avatar, Box, Button, Chip, Dialog, DialogContent, DialogTitle, Grid, IconButton, InputAdornment, MenuItem, Paper,
  Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, ToggleButton, ToggleButtonGroup,
  Tooltip, Typography, useMediaQuery, useTheme,
} from '@mui/material'
import { AppWindow, FolderOpen, LayoutGrid, Link2, List, Pencil, Plus, Search, Star, Trash2, Unlink, X } from 'lucide-react'
import { Link as RouterLink, useSearchParams } from 'react-router-dom'
import { ApiError, resolveAssetUrl } from '../../api/client'
import { productsApi, type Product } from '../../api/productsApi'
import { ProductForm } from '../../components/admin/ProductForm'
import { PageHeader } from '../../components/layout/PageHeader'
import { ProductTile } from '../../components/products/ProductTile'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { EmptyState } from '../../components/ui/EmptyState'
import { ErrorState } from '../../components/ui/ErrorState'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { SummaryCard } from '../../components/ui/SummaryCard'
import { appColor, showcasePalette } from '../../utils/showcaseColor'
import { formatPlanPrice } from '../../utils/productPricing'

type StatusFilter = 'ALL' | 'ACTIVE' | 'INACTIVE' | 'RETIRED' | 'FEATURED'
type View = 'table' | 'cards'
const STATUS_TONE = { ACTIVE: 'success', INACTIVE: 'warning', RETIRED: 'neutral' } as const

function priceSummary(product: Product): string {
  if (product.plans.length === 0) return formatPlanPrice(product.price, 'USD', 'ONE_TIME')
  const cheapest = product.plans.reduce((min, p) => (p.price < min.price ? p : min))
  return formatPlanPrice(cheapest.price, cheapest.currency, cheapest.billingPeriod)
}

/**
 * "/admin/apps" — All Apps (C66): every app across every platform via the
 * ADMIN-only endpoint (INACTIVE and RETIRED included). Summary cards, a
 * filter bar (status, platform, search) and a table or card view. Full CRUD
 * stays on this page (C44): Create is the Add App dialog (same ProductForm as
 * the Create App page), Update is the edit link, Delete asks for confirmation.
 */
export function AdminProductsPage() {
  const { t } = useTranslation()
  const theme = useTheme()
  const narrow = useMediaQuery(theme.breakpoints.down('md'))
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [reload, setReload] = useState(0)
  // C80: the old Settings → App page redirects here with ?new=1 to open the Add dialog.
  const [searchParams, setSearchParams] = useSearchParams()
  const [createOpen, setCreateOpen] = useState(searchParams.get('new') === '1')
  useEffect(() => {
    if (searchParams.get('new') !== '1') return
    setSearchParams((prev) => { const p = new URLSearchParams(prev); p.delete('new'); return p }, { replace: true })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])
  const [status, setStatus] = useState<StatusFilter>('ALL')
  const [platformId, setPlatformId] = useState<number | ''>('')
  const [query, setQuery] = useState('')
  const [view, setView] = useState<View>('table')
  const [pendingDelete, setPendingDelete] = useState<Product | null>(null)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  useEffect(() => {
    productsApi.listAdmin()
      .then((result) => { setProducts(result); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.apps.loadError')))
  }, [reload, t])

  const platformOptions = useMemo(() => {
    const byId = new Map<number, string>()
    for (const app of products ?? []) for (const p of app.platforms) byId.set(p.id, p.name)
    return [...byId.entries()].sort((a, b) => a[1].localeCompare(b[1]))
  }, [products])

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase()
    return (products ?? []).filter((app) =>
      (status === 'ALL' || (status === 'FEATURED' ? app.featured : app.status === status))
      && (platformId === '' || app.platforms.some((p) => p.id === platformId))
      && (!q || [app.name, app.category, app.description].some((v) => v?.toLowerCase().includes(q))))
  }, [products, status, platformId, query])

  const count = (filter: StatusFilter) => (products ?? []).filter((app) =>
    filter === 'ALL' || (filter === 'FEATURED' ? app.featured : app.status === filter)).length
  const ssoCount = (products ?? []).filter((p) => p.ssoConnected).length
  const filtersActive = status !== 'ALL' || platformId !== '' || !!query
  const effectiveView: View = narrow ? 'cards' : view

  const handleDelete = () => {
    if (!pendingDelete) return
    setDeleting(true)
    setDeleteError(null)
    productsApi.delete(pendingDelete.id)
      .then(() => {
        setProducts((current) => current?.filter((p) => p.id !== pendingDelete.id) ?? current)
        setPendingDelete(null)
      })
      .catch((e) => { setDeleteError(e instanceof ApiError ? e.message : t('admin.apps.deleteError')); setPendingDelete(null) })
      .finally(() => setDeleting(false))
  }

  const handleCreated = () => {
    setCreateOpen(false)
    setReload((n) => n + 1)
  }

  return (
    <>
      <PageHeader icon={LayoutGrid} area="catalog" title={t('admin.apps.title')} subtitle={t('admin.apps.subtitle')}
        action={(
          <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setCreateOpen(true)}>
            {t('admin.apps.add')}
          </Button>
        )} />

      <Grid container spacing={2} sx={{ mb: 3 }}>
        {[
          { icon: AppWindow, label: t('admin.apps.summary.total'), value: products?.length, color: '#6366F1' },
          { icon: Link2, label: t('admin.apps.summary.sso'), value: products ? ssoCount : undefined, color: '#10B981' },
          { icon: Unlink, label: t('admin.apps.summary.standalone'), value: products ? products.length - ssoCount : undefined, color: '#F97316' },
          { icon: Star, label: t('admin.apps.summary.featured'), value: products ? count('FEATURED') : undefined, color: '#8B5CF6' },
        ].map((card) => (
          <Grid key={card.label} size={{ xs: 12, sm: 6, lg: 3 }}>
            <SummaryCard icon={card.icon} label={card.label} color={card.color}
              value={card.value === undefined ? <Skeleton width={32} /> : card.value} />
          </Grid>
        ))}
      </Grid>

      <Paper variant="outlined" sx={{ p: 1.5, mb: 3, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
        <Box role="group" aria-label={t('admin.apps.statusFilter')} sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap', flex: 1, minWidth: 0 }}>
          {(['ALL', 'ACTIVE', 'INACTIVE', 'RETIRED', 'FEATURED'] as StatusFilter[]).map((f) => (
            <Chip key={f} label={`${t(`admin.apps.filter.${f}`)}${products ? ` (${count(f)})` : ''}`} onClick={() => setStatus(f)}
              color={status === f ? 'primary' : 'default'} variant={status === f ? 'filled' : 'outlined'} aria-pressed={status === f} />
          ))}
        </Box>
        <TextField select size="small" label={t('admin.apps.platform')} value={platformId} sx={{ minWidth: 180 }}
          onChange={(e) => setPlatformId(e.target.value === '' ? '' : Number(e.target.value))}>
          <MenuItem value="">{t('admin.apps.allPlatforms')}</MenuItem>
          {platformOptions.map(([id, name]) => <MenuItem key={id} value={id}>{name}</MenuItem>)}
        </TextField>
        <TextField size="small" placeholder={t('admin.apps.search')} value={query} onChange={(e) => setQuery(e.target.value)}
          sx={{ width: { xs: '100%', sm: 240 } }}
          slotProps={{ htmlInput: { 'aria-label': t('admin.apps.search') }, input: { startAdornment: <InputAdornment position="start"><Search size={16} aria-hidden /></InputAdornment> } }} />
        {!narrow && (
          <ToggleButtonGroup size="small" exclusive value={view} onChange={(_, v: View | null) => v && setView(v)} aria-label={t('admin.apps.view')}>
            <ToggleButton value="table" aria-label={t('admin.apps.viewTable')}><List size={16} /></ToggleButton>
            <ToggleButton value="cards" aria-label={t('admin.apps.viewCards')}><LayoutGrid size={16} /></ToggleButton>
          </ToggleButtonGroup>
        )}
      </Paper>

      {deleteError && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setDeleteError(null)}>{deleteError}</Alert>}
      {error && <ErrorState title={t('admin.apps.loadErrorTitle')} message={error} onRetry={() => setReload((n) => n + 1)} />}

      {!error && products === null && <Skeleton variant="rounded" height={320} />}

      {products && visible.length === 0 && (
        <EmptyState
          title={filtersActive ? t('admin.apps.noMatchTitle') : t('admin.apps.emptyTitle')}
          description={filtersActive ? t('admin.apps.noMatchBody') : t('admin.apps.emptyBody')}
          action={filtersActive
            ? <Button onClick={() => { setStatus('ALL'); setPlatformId(''); setQuery('') }}>{t('catalog.clearFilters')}</Button>
            : <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setCreateOpen(true)}>{t('admin.apps.add')}</Button>} />
      )}

      {products && visible.length > 0 && effectiveView === 'cards' && (
        <Grid container spacing={2.5}>
          {visible.map((product) => (
            <Grid key={product.id} size={{ xs: 12, sm: 6, lg: 4, xl: 3 }}>
              <ProductTile product={product} admin onDelete={() => setPendingDelete(product)} />
            </Grid>
          ))}
        </Grid>
      )}

      {products && visible.length > 0 && effectiveView === 'table' && (
        <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 3 }}>
          <Table aria-label={t('admin.apps.title')}>
            <TableHead>
              <TableRow>
                <TableCell>{t('admin.apps.col.app')}</TableCell>
                <TableCell>{t('admin.apps.col.platform')}</TableCell>
                <TableCell>{t('admin.apps.col.status')}</TableCell>
                <TableCell>{t('admin.apps.col.sso')}</TableCell>
                <TableCell>{t('admin.apps.col.price')}</TableCell>
                <TableCell align="right">{t('admin.apps.col.actions')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {visible.map((app) => {
                const palette = showcasePalette(appColor(app))
                return (
                  <TableRow key={app.id} hover>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Avatar variant="rounded" src={app.imageUrl ? resolveAssetUrl(app.imageUrl) : undefined} alt=""
                          sx={{ width: 36, height: 36, bgcolor: palette.soft, color: palette.text, fontWeight: 700, fontSize: 15 }}>
                          {app.name.charAt(0)}
                        </Avatar>
                        <Box sx={{ minWidth: 0 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                            <Typography component={RouterLink} to={`/products/${app.id}`}
                              sx={{ fontWeight: 600, color: 'text.primary', textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}>
                              {app.name}
                            </Typography>
                            {app.featured && (
                              <Tooltip title={t('catalog.app.featured')}>
                                <Box component="span" sx={{ display: 'inline-flex', color: 'warning.main' }} aria-label={t('catalog.app.featured')} role="img">
                                  <Star size={14} fill="currentColor" />
                                </Box>
                              </Tooltip>
                            )}
                          </Box>
                          {app.category && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{app.category}</Typography>}
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell>
                      {app.platforms.length > 0
                        ? app.platforms.map((p) => p.name).join(', ')
                        : <Typography component="span" variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.apps.unassigned')}</Typography>}
                    </TableCell>
                    <TableCell><StatusBadge label={t(`forms.app.status.${app.status}`)} tone={STATUS_TONE[app.status]} /></TableCell>
                    <TableCell>
                      <StatusBadge label={app.ssoConnected ? t('catalog.app.ssoConnected') : t('catalog.app.standalone')}
                        tone={app.ssoConnected ? 'success' : 'neutral'} />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {app.plans.length > 1 ? t('admin.apps.from', { price: priceSummary(app) }) : priceSummary(app)}
                      </Typography>
                    </TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <Tooltip title={t('productContent.listAction')}>
                        <IconButton component={RouterLink} to={`/admin/products/${app.id}/edit?tab=content`} size="small"
                          aria-label={t('productContent.listActionLabel', { name: app.name })}>
                          <FolderOpen size={16} />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title={t('catalog.app.edit')}>
                        <IconButton component={RouterLink} to={`/admin/products/${app.id}/edit`} size="small"
                          aria-label={t('catalog.app.editLabel', { name: app.name })}>
                          <Pencil size={16} />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title={t('catalog.app.delete')}>
                        <IconButton size="small" onClick={() => setPendingDelete(app)} aria-label={t('catalog.app.deleteLabel', { name: app.name })}
                          sx={{ '&:hover': { color: 'error.main' } }}>
                          <Trash2 size={16} />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} maxWidth="lg" fullWidth fullScreen={narrow} scroll="body"
        aria-labelledby="add-app-title">
        <DialogTitle id="add-app-title" sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          {t('admin.apps.addTitle')}
          <IconButton size="small" aria-label={t('admin.apps.close')} onClick={() => setCreateOpen(false)}>
            <X size={18} />
          </IconButton>
        </DialogTitle>
        <DialogContent sx={{ bgcolor: 'background.default', pt: '16px !important' }}>
          <Alert severity="info" sx={{ mb: 2 }}>{t('productContent.saveFirst')}</Alert>
          <ProductForm
            onSubmit={productsApi.create}
            submitLabel={t('admin.apps.add')}
            submittingLabel={t('forms.app.creating')}
            successMessage={t('forms.app.created')}
            onSuccess={handleCreated}
          />
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={pendingDelete !== null} busy={deleting}
        title={t('admin.apps.deleteTitle', { name: pendingDelete?.name ?? '' })} body={t('admin.apps.deleteBody')}
        confirmLabel={t('catalog.app.delete')} onConfirm={handleDelete} onClose={() => setPendingDelete(null)} />
    </>
  )
}
