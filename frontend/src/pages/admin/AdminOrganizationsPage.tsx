import { Building2, MoreHorizontal, UserRound } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useNavigate, useSearchParams } from 'react-router-dom'
import {
  Alert, Box, Button, Chip, CircularProgress, FormControl, IconButton, InputLabel, LinearProgress, Link, Menu, MenuItem, Paper,
  Select, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, TableSortLabel, TextField,
  Tooltip, Typography,
} from '@mui/material'
import { adminRegistrationApi } from '../../api/adminRegistrationApi'
import { ApiError } from '../../api/client'
import { orgDirectoryApi, type DirectoryRow, type DirectorySummary } from '../../api/orgDirectoryApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { ResetMfaDialog } from '../../components/security/ResetMfaDialog'

const FILTER_KEYS = ['q', 'type', 'status', 'lifecycle', 'country', 'region', 'industry', 'profile', 'seats', 'from', 'to'] as const
type FilterKey = (typeof FILTER_KEYS)[number]
type SortKey = 'name' | 'type' | 'location' | 'seats' | 'status' | 'products' | 'tickets' | 'createdAt' | 'profile'

const profileBand = (percent: number) => (percent === 100 ? 'complete' : percent === 0 ? 'notStarted' : 'inProgress')
const seatState = (r: DirectoryRow) =>
  r.kind !== 'ORGANIZATION' ? null : r.seatsUsed > r.seatsLicensed ? 'over' : r.seatsUsed === r.seatsLicensed ? 'full' : 'available'
const location = (r: DirectoryRow) => [r.city, r.state, r.country].filter(Boolean).join(', ')
const distinct = (values: (string | undefined)[]) => [...new Set(values.filter((v): v is string => !!v))].sort()

/**
 * "/admin/organizations" — REQ-TEN-007: every organization and individual customer in one list, with a
 * filter row, profile completion and a link to each one's detail page. Replaces "Registrations".
 */
export function AdminOrganizationsPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const [rows, setRows] = useState<DirectoryRow[] | null>(null)
  const [summary, setSummary] = useState<DirectorySummary | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [sort, setSort] = useState<{ key: SortKey; dir: 'asc' | 'desc' }>({ key: 'createdAt', dir: 'desc' })
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(25)
  const [menu, setMenu] = useState<{ anchor: HTMLElement; row: DirectoryRow } | null>(null)
  const [resetOpen, setResetOpen] = useState(false)
  const [resetNotice, setResetNotice] = useState<string | null>(null)
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let alive = true
    orgDirectoryApi.directory().then((d) => { if (alive) { setRows(d.rows); setSummary(d.summary); setError(null) } })
      .catch((e) => { if (alive) setError(e instanceof ApiError ? e.message : t('orgDirectory.loadError')) })
    return () => { alive = false }
  }, [version, t])

  const f = (key: FilterKey) => params.get(key) ?? ''
  const setFilter = (key: FilterKey, value: string) => {
    setPage(0)
    setParams((prev) => {
      const next = new URLSearchParams(prev)
      if (value) next.set(key, value)
      else next.delete(key)
      return next
    }, { replace: true })
  }
  const anyFilter = FILTER_KEYS.some((k) => f(k))

  const options = useMemo(() => ({
    status: distinct((rows ?? []).map((r) => r.status)),
    country: distinct((rows ?? []).map((r) => r.country)),
    region: distinct((rows ?? []).map((r) => r.regionName)),
    industry: distinct((rows ?? []).map((r) => r.industry)),
  }), [rows])

  const filtered = useMemo(() => {
    const q = f('q').trim().toLowerCase()
    const from = f('from') ? new Date(`${f('from')}T00:00:00`).getTime() : null
    const to = f('to') ? new Date(`${f('to')}T23:59:59`).getTime() : null
    const list = (rows ?? []).filter((r) => {
      if (q && !`${r.name} ${r.code ?? ''} ${r.email ?? ''} ${r.contactEmail ?? ''}`.toLowerCase().includes(q)) return false
      if (f('type') && r.kind !== f('type')) return false
      if (f('status') && r.status !== f('status')) return false
      if (f('lifecycle') && r.lifecycleStatus !== f('lifecycle')) return false
      if (f('country') && r.country !== f('country')) return false
      if (f('region') && r.regionName !== f('region')) return false
      if (f('industry') && r.industry !== f('industry')) return false
      if (f('profile') && profileBand(r.profileCompletion) !== f('profile')) return false
      if (f('seats') && seatState(r) !== f('seats')) return false
      const created = r.createdAt ? new Date(r.createdAt).getTime() : null
      if (from !== null && (created === null || created < from)) return false
      if (to !== null && (created === null || created > to)) return false
      return true
    })
    const value = (r: DirectoryRow): string | number => {
      switch (sort.key) {
        case 'name': return r.name.toLowerCase()
        case 'type': return r.kind
        case 'location': return location(r).toLowerCase()
        case 'seats': return r.seatsUsed
        case 'status': return r.status
        case 'products': return r.productCount
        case 'tickets': return r.openTickets
        case 'profile': return r.profileCompletion
        default: return r.createdAt ?? ''
      }
    }
    return [...list].sort((a, b) => {
      const x = value(a), y = value(b)
      const c = x < y ? -1 : x > y ? 1 : 0
      return sort.dir === 'asc' ? c : -c
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rows, params, sort])

  const sortHeader = (key: SortKey, label: string) => (
    <TableSortLabel active={sort.key === key} direction={sort.key === key ? sort.dir : 'asc'}
      onClick={() => setSort((s) => ({ key, dir: s.key === key && s.dir === 'asc' ? 'desc' : 'asc' }))}>
      {label}
    </TableSortLabel>
  )
  const detailPath = (r: DirectoryRow) => `/admin/organizations/${r.kind === 'ORGANIZATION' ? 'organization' : 'individual'}/${r.id}`
  const pageRows = filtered.slice(page * pageSize, page * pageSize + pageSize)

  const select = (key: FilterKey, label: string, values: { value: string; label: string }[]) => (
    <FormControl size="small" sx={{ minWidth: 140 }}>
      <InputLabel id={`flt-${key}`}>{label}</InputLabel>
      <Select labelId={`flt-${key}`} label={label} value={f(key)} onChange={(e) => setFilter(key, e.target.value)}>
        <MenuItem value="">{t('orgDirectory.all')}</MenuItem>
        {values.map((v) => <MenuItem key={v.value} value={v.value}>{v.label}</MenuItem>)}
      </Select>
    </FormControl>
  )
  const same = (v: string) => ({ value: v, label: v.replaceAll('_', ' ') })

  return (
    <Box>
      <PageHeader icon={Building2} accent="teal" area="administration" title={t('orgDirectory.title')} subtitle={t('orgDirectory.subtitle')}
        action={<Button variant="outlined" color="error" size="small" onClick={() => setResetOpen(true)}>{t('mfaReset.openByEmail')}</Button>} />
      {resetNotice && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setResetNotice(null)}>{resetNotice}</Alert>}
      <ResetMfaDialog open={resetOpen} onConfirm={(email) => adminRegistrationApi.resetMfa(email ?? '')} onClose={() => setResetOpen(false)}
        onDone={(who) => { setResetOpen(false); setResetNotice(t('mfaReset.done', { name: who })) }} />
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} action={<Button color="inherit" size="small" onClick={() => setVersion((v) => v + 1)}>{t('orgStructure.retry')}</Button>}>
          {error}
        </Alert>
      )}
      {!rows && !error && <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} aria-label="loading" /></Box>}
      {rows && summary && (
        <>
          <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: { xs: 'repeat(2, 1fr)', md: 'repeat(7, 1fr)' }, gap: 1.5, m: 0, mb: 2 }}>
            {([
              ['total', summary.total], ['organizations', summary.organizations], ['individuals', summary.individuals],
              ['complete', summary.profileComplete], ['inProgress', summary.profileInProgress], ['notStarted', summary.profileNotStarted],
              ['average', `${summary.averageCompletion}%`],
            ] as const).map(([k, v]) => (
              <Paper key={k} variant="outlined" sx={{ p: 1.5 }}>
                <Typography component="dt" variant="caption" color="text.secondary">{t(`orgDirectory.summary.${k}`)}</Typography>
                <Typography component="dd" variant="h6" sx={{ m: 0 }}>{v}</Typography>
              </Paper>
            ))}
          </Box>
          <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
            <Stack direction="row" spacing={1.5} useFlexGap sx={{ flexWrap: 'wrap', alignItems: 'center' }} role="search" aria-label={t('orgDirectory.filters.label')}>
              <TextField size="small" label={t('orgDirectory.filters.search')} value={f('q')} onChange={(e) => setFilter('q', e.target.value)} sx={{ minWidth: 240 }} />
              {select('type', t('orgDirectory.filters.type'), [{ value: 'ORGANIZATION', label: t('orgDirectory.kind.ORGANIZATION') }, { value: 'INDIVIDUAL', label: t('orgDirectory.kind.INDIVIDUAL') }])}
              {select('status', t('orgDirectory.filters.status'), options.status.map(same))}
              {select('lifecycle', t('orgDirectory.filters.lifecycle'), ['ACTIVE', 'SUSPENDED', 'CLOSED'].map((v) => ({ value: v, label: t(`adminOrgLifecycle.lifecycleStatus.${v}`) })))}
              {select('country', t('orgDirectory.filters.country'), options.country.map(same))}
              {select('region', t('orgDirectory.filters.region'), options.region.map(same))}
              {select('industry', t('orgDirectory.filters.industry'), options.industry.map(same))}
              {select('profile', t('orgDirectory.filters.profile'), ['complete', 'inProgress', 'notStarted'].map((v) => ({ value: v, label: t(`orgDirectory.profileBand.${v}`) })))}
              {select('seats', t('orgDirectory.filters.seats'), ['full', 'available', 'over'].map((v) => ({ value: v, label: t(`orgDirectory.seatState.${v}`) })))}
              <TextField size="small" type="date" label={t('orgDirectory.filters.from')} value={f('from')} onChange={(e) => setFilter('from', e.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
              <TextField size="small" type="date" label={t('orgDirectory.filters.to')} value={f('to')} onChange={(e) => setFilter('to', e.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
              <Button size="small" disabled={!anyFilter} onClick={() => { setPage(0); setParams({}, { replace: true }) }}>{t('orgDirectory.filters.clear')}</Button>
            </Stack>
            <Typography variant="caption" color="text.secondary" role="status" sx={{ display: 'block', mt: 1 }}>
              {t('orgDirectory.filters.shown', { count: filtered.length, total: rows.length })}
            </Typography>
          </Paper>
          {filtered.length === 0 ? (
            <Typography color="text.secondary">{rows.length === 0 ? t('orgDirectory.none') : t('orgDirectory.noMatches')}</Typography>
          ) : (
            <Paper variant="outlined">
              <TableContainer>
                <Table size="small" aria-label={t('orgDirectory.title')}>
                  <TableHead>
                    <TableRow>
                      <TableCell>{sortHeader('name', t('orgDirectory.col.name'))}</TableCell>
                      <TableCell>{sortHeader('type', t('orgDirectory.col.type'))}</TableCell>
                      <TableCell>{t('orgDirectory.col.industry')}</TableCell>
                      <TableCell>{sortHeader('location', t('orgDirectory.col.location'))}</TableCell>
                      <TableCell>{t('orgDirectory.col.contact')}</TableCell>
                      <TableCell>{sortHeader('seats', t('orgDirectory.col.seats'))}</TableCell>
                      <TableCell>{sortHeader('status', t('orgDirectory.col.status'))}</TableCell>
                      <TableCell>{t('orgDirectory.col.region')}</TableCell>
                      <TableCell>{t('orgDirectory.col.parent')}</TableCell>
                      <TableCell align="right">{sortHeader('products', t('orgDirectory.col.products'))}</TableCell>
                      <TableCell align="right">{t('orgDirectory.col.nodes')}</TableCell>
                      <TableCell align="right">{sortHeader('tickets', t('orgDirectory.col.tickets'))}</TableCell>
                      <TableCell align="right">{t('orgDirectory.col.invoices')}</TableCell>
                      <TableCell>{t('orgDirectory.col.mfa')}</TableCell>
                      <TableCell>{t('orgDirectory.col.signIn')}</TableCell>
                      <TableCell>{sortHeader('createdAt', t('orgDirectory.col.registered'))}</TableCell>
                      <TableCell sx={{ minWidth: 150 }}>{sortHeader('profile', t('orgDirectory.col.profile'))}</TableCell>
                      <TableCell><span style={{ position: 'absolute', left: -9999 }}>{t('orgDirectory.col.actions')}</span></TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageRows.map((r) => (
                      <TableRow key={`${r.kind}-${r.id}`} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(detailPath(r))}>
                        <TableCell>
                          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                            {r.kind === 'ORGANIZATION' ? <Building2 size={16} aria-hidden /> : <UserRound size={16} aria-hidden />}
                            <Box>
                              <Link component={RouterLink} to={detailPath(r)} onClick={(e) => e.stopPropagation()} underline="hover"
                                aria-label={t('orgDirectory.openRow', { name: r.name })} sx={{ fontWeight: 600 }}>{r.name}</Link>
                              {r.code && <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{r.code}</Typography>}
                            </Box>
                          </Stack>
                        </TableCell>
                        <TableCell>{t(`orgDirectory.kind.${r.kind}`)}{r.type ? ` · ${r.type}` : ''}</TableCell>
                        <TableCell>{r.industry ?? '—'}</TableCell>
                        <TableCell>{location(r) || '—'}</TableCell>
                        <TableCell>
                          {r.contactName || '—'}
                          {(r.contactEmail ?? r.email) && <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{r.contactEmail ?? r.email}</Typography>}
                        </TableCell>
                        <TableCell>{r.kind === 'ORGANIZATION' ? `${r.seatsUsed} / ${r.seatsLicensed}` : '—'}</TableCell>
                        <TableCell>
                          <Chip size="small" label={r.status.replaceAll('_', ' ')} color={r.status === 'COMPLETED' ? 'success' : 'default'} />
                          {r.lifecycleStatus && r.lifecycleStatus !== 'ACTIVE' && (
                            <Chip size="small" variant="outlined" color={r.lifecycleStatus === 'SUSPENDED' ? 'warning' : 'default'}
                              label={t(`adminOrgLifecycle.lifecycleStatus.${r.lifecycleStatus}`)} sx={{ ml: 0.5 }} />
                          )}
                        </TableCell>
                        <TableCell>{r.regionName ?? '—'}</TableCell>
                        <TableCell>{r.parentOrganizationName ?? '—'}</TableCell>
                        <TableCell align="right">{r.productCount}</TableCell>
                        <TableCell align="right">{r.kind === 'ORGANIZATION' ? r.hierarchyNodes : '—'}</TableCell>
                        <TableCell align="right">{r.openTickets}</TableCell>
                        <TableCell align="right">{r.outstandingInvoices}</TableCell>
                        <TableCell>{r.kind === 'ORGANIZATION' ? (r.mfaRequired ? t('orgDirectory.mfaOn') : '—') : '—'}</TableCell>
                        <TableCell>
                          <Chip size="small" color={r.signInLinked ? 'success' : 'warning'}
                            label={r.signInLinked ? t('orgDirectory.signInLinked') : t('orgDirectory.signInNotLinked')} />
                        </TableCell>
                        <TableCell>{r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—'}</TableCell>
                        <TableCell>
                          <Tooltip title={r.missingFields.length ? t('orgDirectory.missingTitle', { items: r.missingFields.map((c) => t(`orgDirectory.fields.${c}`)).join(', ') }) : t('orgDirectory.noMissing')}>
                            <Box>
                              <LinearProgress variant="determinate" value={r.profileCompletion} color={r.profileCompletion === 100 ? 'success' : 'primary'}
                                aria-label={t('orgDirectory.profileOf', { name: r.name })} sx={{ height: 6, borderRadius: 3 }} />
                              <Typography variant="caption">{r.profileCompletion}%</Typography>
                            </Box>
                          </Tooltip>
                        </TableCell>
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          {r.kind === 'ORGANIZATION' && (
                            <IconButton size="small" aria-label={t('orgDirectory.rowActions', { name: r.name })} aria-haspopup="menu"
                              onClick={(e) => setMenu({ anchor: e.currentTarget, row: r })}>
                              <MoreHorizontal size={16} />
                            </IconButton>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
              <TablePagination component="div" count={filtered.length} page={page} rowsPerPage={pageSize}
                onPageChange={(_, p) => setPage(p)} onRowsPerPageChange={(e) => { setPageSize(Number(e.target.value)); setPage(0) }}
                rowsPerPageOptions={[10, 25, 50, 100]} />
            </Paper>
          )}
          <Menu anchorEl={menu?.anchor} open={!!menu} onClose={() => setMenu(null)}>
            {menu && (['edit', menu.row.lifecycleStatus === 'ACTIVE' ? 'suspend' : 'activate', ...(menu.row.lifecycleStatus === 'CLOSED' ? [] : ['close'])] as const)
              .filter((a) => !(a === 'edit' && menu.row.lifecycleStatus === 'CLOSED'))
              .map((a) => (
                <MenuItem key={a} onClick={() => { const row = menu.row; setMenu(null); navigate(`${detailPath(row)}?action=${a}`) }}>
                  {t(`adminOrgLifecycle.${a}Button`)}
                </MenuItem>
              ))}
          </Menu>
        </>
      )}
    </Box>
  )
}
