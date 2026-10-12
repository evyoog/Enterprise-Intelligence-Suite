import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router-dom'
import { Alert, Box, Button, Checkbox, Chip, MenuItem, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, TextField, Typography } from '@mui/material'
import { accessApi, type AccessMemberPage, type PermissionCatalogEntry, type RoleDefaultProduct } from '../../api/accessApi'
import { ApiError } from '../../api/client'
import { BulkAccessDialog } from './BulkAccessDialog'
import { MemberAccessDrawer } from './MemberAccessDrawer'

const PAGE_SIZE = 20

/** The Members tab (REQ-TEN-005): who has what, with filters, bulk change and the member access panel (`?member=`). */
export function AccessMembersTab() {
  const { t, i18n } = useTranslation()
  const [params, setParams] = useSearchParams()
  const [page, setPage] = useState(0)
  const [q, setQ] = useState('')
  const [role, setRole] = useState('')
  const [productId, setProductId] = useState('')
  const [custom, setCustom] = useState(false)
  const [data, setData] = useState<AccessMemberPage | null>(null)
  const [products, setProducts] = useState<RoleDefaultProduct[]>([])
  const [catalogue, setCatalogue] = useState<PermissionCatalogEntry[]>([])
  const [selected, setSelected] = useState<number[]>([])
  const [bulkOpen, setBulkOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const memberParam = params.get('member')
  const openMember = memberParam && !Number.isNaN(Number(memberParam)) ? Number(memberParam) : null

  const load = useCallback(() => {
    accessApi.members({ page, size: PAGE_SIZE, q: q.trim() || undefined, role: role || undefined, productId: productId ? Number(productId) : undefined, custom })
      .then((d) => { setError(null); setData(d) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('access.loadError')))
  }, [page, q, role, productId, custom, t])

  useEffect(() => { load() }, [load])
  useEffect(() => {
    accessApi.roleDefaults('MEMBER').then((d) => setProducts(d.products)).catch(() => setProducts([]))
    accessApi.permissions().then(setCatalogue).catch(() => setCatalogue([]))
  }, [])

  const setMember = (id: number | null) =>
    setParams((prev) => {
      const next = new URLSearchParams(prev)
      if (id === null) next.delete('member'); else next.set('member', String(id))
      return next
    }, { replace: true })
  const clearFilters = () => { setQ(''); setRole(''); setProductId(''); setCustom(false); setPage(0) }
  const toggle = (id: number) => setSelected((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]))
  const rows = data?.items ?? []
  const allOnPage = rows.length > 0 && rows.every((r) => selected.includes(r.memberId))
  const filtered = q.trim() !== '' || role !== '' || productId !== '' || custom

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap', alignItems: 'center', mb: 2 }}>
        <TextField size="small" label={t('access.search')} value={q} onChange={(e) => { setQ(e.target.value); setPage(0) }} sx={{ minWidth: 220 }} />
        <TextField select size="small" label={t('access.role')} value={role} onChange={(e) => { setRole(e.target.value); setPage(0) }} sx={{ minWidth: 170 }}>
          <MenuItem value="">{t('access.any')}</MenuItem>
          <MenuItem value="ORG_ADMIN">{t('access.roles.ORG_ADMIN')}</MenuItem>
          <MenuItem value="MEMBER">{t('access.roles.MEMBER')}</MenuItem>
        </TextField>
        <TextField select size="small" label={t('access.product')} value={productId} onChange={(e) => { setProductId(e.target.value); setPage(0) }} sx={{ minWidth: 170 }}>
          <MenuItem value="">{t('access.any')}</MenuItem>
          {products.map((p) => <MenuItem key={p.productId} value={String(p.productId)}>{p.name}</MenuItem>)}
        </TextField>
        <Chip label={t('access.hasCustom')} clickable color={custom ? 'primary' : 'default'} variant={custom ? 'filled' : 'outlined'}
          onClick={() => { setCustom(!custom); setPage(0) }} aria-pressed={custom} />
        {filtered && <Button size="small" onClick={clearFilters}>{t('access.clearFilters')}</Button>}
        <Box sx={{ flex: 1 }} />
        <Button variant="outlined" disabled={selected.length === 0} onClick={() => setBulkOpen(true)}>
          {t('access.bulkChange', { count: selected.length })}
        </Button>
      </Box>

      {error && <Alert severity="error" action={<Button color="inherit" size="small" onClick={load}>{t('access.retry')}</Button>} sx={{ mb: 2 }}>{error}</Alert>}

      <Paper variant="outlined">
        <TableContainer>
          <Table size="small" aria-label={t('access.tabs.members')}>
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox checked={allOnPage} indeterminate={!allOnPage && rows.some((r) => selected.includes(r.memberId))}
                    slotProps={{ input: { 'aria-label': t('access.selectPage') } }}
                    onChange={() => setSelected((s) => (allOnPage ? s.filter((id) => !rows.some((r) => r.memberId === id)) : [...new Set([...s, ...rows.filter((r) => r.canChange).map((r) => r.memberId)])]))} />
                </TableCell>
                <TableCell>{t('access.member')}</TableCell>
                <TableCell>{t('access.role')}</TableCell>
                <TableCell>{t('access.products')}</TableCell>
                <TableCell>{t('access.customAccess')}</TableCell>
                <TableCell>{t('access.lastReviewed')}</TableCell>
                <TableCell>{t('access.status')}</TableCell>
                <TableCell align="right">{t('access.actions')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {data && rows.length === 0 && (
                <TableRow><TableCell colSpan={8}>
                  <Typography sx={{ color: 'text.secondary', py: 1 }}>{t('access.noMembers')}</Typography>
                  {filtered && <Button size="small" onClick={clearFilters}>{t('access.clearFilters')}</Button>}
                </TableCell></TableRow>
              )}
              {rows.map((r) => (
                <TableRow key={r.memberId} hover selected={selected.includes(r.memberId)}>
                  <TableCell padding="checkbox">
                    <Checkbox checked={selected.includes(r.memberId)} disabled={!r.canChange} onChange={() => toggle(r.memberId)}
                      slotProps={{ input: { 'aria-label': t('access.select', { name: r.name }) } }} />
                  </TableCell>
                  <TableCell>
                    <Typography sx={{ fontWeight: 600 }}>{r.name}</Typography>
                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>{r.email}</Typography>
                  </TableCell>
                  <TableCell><Chip size="small" color={r.role === 'ORG_ADMIN' ? 'primary' : 'default'} variant="outlined" label={t(`access.roles.${r.role}`)} /></TableCell>
                  <TableCell>{t('access.productsOf', { on: r.productsOn, total: r.productsEntitled })}</TableCell>
                  <TableCell>{r.customAccessCount > 0 ? <Chip size="small" color="primary" label={t('access.customCount', { count: r.customAccessCount })} /> : ''}</TableCell>
                  <TableCell>{r.lastReviewedAt ? new Date(r.lastReviewedAt).toLocaleDateString(i18n.language) : t('access.never')}</TableCell>
                  <TableCell><Chip size="small" variant="outlined" label={t(`access.statuses.${r.status}`, { defaultValue: r.status })} /></TableCell>
                  <TableCell align="right">
                    <Button size="small" aria-label={t('access.manageFor', { name: r.name })} onClick={() => setMember(r.memberId)}>{t('access.manage')}</Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination component="div" count={data?.totalElements ?? 0} page={page} rowsPerPage={PAGE_SIZE} rowsPerPageOptions={[PAGE_SIZE]}
          onPageChange={(_, p) => setPage(p)} />
      </Paper>

      <MemberAccessDrawer key={openMember ?? 'closed'} memberId={openMember} onClose={() => setMember(null)} onChanged={load} />
      <BulkAccessDialog open={bulkOpen} memberIds={selected} products={products} permissions={catalogue}
        onClose={() => setBulkOpen(false)} onDone={() => { load(); setSelected([]) }} />
    </Box>
  )
}
