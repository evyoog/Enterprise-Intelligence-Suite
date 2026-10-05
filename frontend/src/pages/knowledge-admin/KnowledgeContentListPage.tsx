import { Alert, Box, Button, Chip, CircularProgress, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow, TablePagination, TextField, Typography } from '@mui/material'
import { Plus } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useSearchParams } from 'react-router-dom'
import { CONTENT_TYPES, knowledgeAdminApi, type ContentRow, type ContentType, type Page } from '../../api/knowledgeApi'
import { formatDate } from '../../components/knowledge/knowledgeUtils'
import { EmptyState } from '../../components/ui/EmptyState'
import { StateChip } from './KnowledgeAdminLayout'
import { useKnowledgeAdmin } from './knowledgeAdminContext'

const STATES = ['DRAFT', 'IN_REVIEW', 'APPROVED', 'SCHEDULED', 'PUBLISHED', 'DEPRECATED', 'ARCHIVED', 'EXPIRED', 'REQUIRES_REVIEW']

/** Content list (knowledge-admin-content-list): every type except videos (own screen). */
export function KnowledgeContentListPage() {
  const { t, i18n } = useTranslation()
  const { taxonomy } = useKnowledgeAdmin()
  const [params, setParams] = useSearchParams()
  const type = (params.get('type') as ContentType | null) ?? ''
  const status = params.get('status') ?? ''
  const product = params.get('product') ?? ''
  const [q, setQ] = useState(params.get('q') ?? '')
  const [page, setPage] = useState(0)
  const [data, setData] = useState<Page<ContentRow> | null>(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    const handle = setTimeout(() => {
      knowledgeAdminApi.list({ type: type || undefined, status: status || undefined, product: product ? Number(product) : undefined, q: q || undefined, page, size: 25 })
        .then(setData).catch(() => setError(true))
    }, 200)
    return () => clearTimeout(handle)
  }, [type, status, product, q, page])

  const set = (key: string, value: string) => {
    const next = new URLSearchParams(params)
    if (value) next.set(key, value); else next.delete(key)
    setParams(next)
    setPage(0)
  }
  const productName = (id: number | null) => taxonomy.products.find((p) => p.id === id)?.name ?? '—'
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField size="small" label={t('knowledge.admin.list.search')} value={q} onChange={(e) => { setQ(e.target.value); setPage(0) }} sx={{ minWidth: 260, flex: 1 }} />
        <TextField select size="small" label={t('knowledge.admin.list.type')} value={type} onChange={(e) => set('type', e.target.value)} sx={{ minWidth: 180 }}>
          <MenuItem value="">{t('knowledge.admin.list.allTypes')}</MenuItem>
          {CONTENT_TYPES.map((ct) => <MenuItem key={ct} value={ct}>{t(`knowledge.typePlural.${ct}`)}</MenuItem>)}
        </TextField>
        <TextField select size="small" label={t('knowledge.admin.list.status')} value={status} onChange={(e) => set('status', e.target.value)} sx={{ minWidth: 170 }}>
          <MenuItem value="">{t('knowledge.admin.list.allStates')}</MenuItem>
          {STATES.map((s) => <MenuItem key={s} value={s}>{t(`knowledge.admin.state.${s}`)}</MenuItem>)}
        </TextField>
        <TextField select size="small" label={t('knowledge.admin.list.product')} value={product} onChange={(e) => set('product', e.target.value)} sx={{ minWidth: 170 }}>
          <MenuItem value="">{t('knowledge.admin.list.allProducts')}</MenuItem>
          {taxonomy.products.map((p) => <MenuItem key={p.id} value={String(p.id)}>{p.name}</MenuItem>)}
        </TextField>
        <Button variant="contained" startIcon={<Plus size={16} />} component={RouterLink}
          to={`/knowledge-management/content/new${type ? `?type=${type}` : ''}`}>{t('knowledge.admin.list.new')}</Button>
      </Box>
      {error && <Alert severity="error">{t('knowledge.common.error')}</Alert>}
      {!data && !error && <CircularProgress aria-label={t('knowledge.common.loading')} />}
      {data && data.items.length === 0 && <EmptyState title={t('knowledge.admin.list.empty')} />}
      {data && data.items.length > 0 && (
        <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
          <Table size="small">
            <TableHead>
              <TableRow>
                {['title', 'type', 'status', 'version', 'views', 'updated'].map((c) => <TableCell key={c}>{t(`knowledge.admin.list.columns.${c}`)}</TableCell>)}
              </TableRow>
            </TableHead>
            <TableBody>
              {data.items.map((row) => (
                <TableRow key={row.id} hover>
                  <TableCell>
                    <Box component={RouterLink} to={row.contentType === 'VIDEO' ? `/knowledge-management/videos/${row.id}` : `/knowledge-management/content/${row.id}`} sx={{ fontWeight: 600, color: 'text.primary' }}>{row.title}</Box>
                    <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>{productName(row.productId)}</Typography>
                  </TableCell>
                  <TableCell>{t(`knowledge.type.${row.contentType}`)}</TableCell>
                  <TableCell>
                    <StateChip state={row.workflowState} />
                    {row.expired && <Chip size="small" color="error" variant="outlined" label={t('knowledge.admin.state.EXPIRED')} sx={{ ml: 1 }} />}
                    {row.requiresReview && <Chip size="small" color="warning" variant="outlined" label={t('knowledge.admin.state.REQUIRES_REVIEW')} sx={{ ml: 1 }} />}
                  </TableCell>
                  <TableCell>{row.liveVersion ?? '—'}</TableCell>
                  <TableCell>{row.views}</TableCell>
                  <TableCell>{formatDate(row.updatedAt, i18n.language)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination component="div" count={data.totalElements} page={page} rowsPerPage={25} rowsPerPageOptions={[25]} onPageChange={(_, p) => setPage(p)} />
        </Paper>
      )}
    </Box>
  )
}
