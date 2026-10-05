import { Alert, Box, Button, CircularProgress, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography, useMediaQuery, useTheme } from '@mui/material'
import { CheckCircle2, Clock, Eye, FilePen, Film, Plus } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { knowledgeVideoApi, type ContentRow, type Page } from '../../api/knowledgeApi'
import { formatDate, formatDuration } from '../../components/knowledge/knowledgeUtils'
import { EmptyState } from '../../components/ui/EmptyState'
import { SummaryCard } from '../../components/ui/SummaryCard'
import { StateChip } from './KnowledgeAdminLayout'
import { useKnowledgeAdmin } from './knowledgeAdminContext'

/** Video management (knowledge-admin-videos, REQ-KNW-004.2): cards, filters, table (cards on mobile). */
export function KnowledgeVideosPage() {
  const { t, i18n } = useTranslation()
  const theme = useTheme()
  const mobile = useMediaQuery(theme.breakpoints.down('md'))
  const { taxonomy } = useKnowledgeAdmin()
  const [summary, setSummary] = useState<{ total: number; published: number; draft: number; inReview: number; views: number } | null>(null)
  const [data, setData] = useState<Page<ContentRow> | null>(null)
  const [q, setQ] = useState('')
  const [product, setProduct] = useState('')
  const [status, setStatus] = useState('')
  const [source, setSource] = useState('')
  const [error, setError] = useState(false)
  useEffect(() => { knowledgeVideoApi.summary().then(setSummary).catch(() => setError(true)) }, [])
  useEffect(() => {
    const h = setTimeout(() => {
      knowledgeVideoApi.list({ q: q || undefined, product: product ? Number(product) : undefined, status: status || undefined, source: source || undefined, size: 50 })
        .then(setData).catch(() => setError(true))
    }, 200)
    return () => clearTimeout(h)
  }, [q, product, status, source])
  const productLabel = (row: ContentRow) => {
    const p = taxonomy.products.find((x) => x.id === row.productId)
    const m = p?.modules.find((x) => x.id === row.moduleId)
    return [p?.name, m?.name].filter(Boolean).join(' · ') || '—'
  }
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr 1fr', md: 'repeat(5, 1fr)' } }}>
        <SummaryCard icon={Film} label={t('knowledge.admin.videos.summary.total')} value={summary?.total ?? '—'} />
        <SummaryCard icon={CheckCircle2} label={t('knowledge.admin.videos.summary.published')} value={summary?.published ?? '—'} color="#10B981" />
        <SummaryCard icon={FilePen} label={t('knowledge.admin.videos.summary.draft')} value={summary?.draft ?? '—'} />
        <SummaryCard icon={Clock} label={t('knowledge.admin.videos.summary.review')} value={summary?.inReview ?? '—'} color="#F59E0B" />
        <SummaryCard icon={Eye} label={t('knowledge.admin.videos.summary.views')} value={summary?.views ?? '—'} color="#6366F1" />
      </Box>
      <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField size="small" label={t('knowledge.admin.videos.search')} value={q} onChange={(e) => setQ(e.target.value)} sx={{ flex: 1, minWidth: 240 }} />
        <TextField select size="small" label={t('knowledge.admin.list.product')} value={product} onChange={(e) => setProduct(e.target.value)} sx={{ minWidth: 160 }}>
          <MenuItem value="">{t('knowledge.admin.list.allProducts')}</MenuItem>
          {taxonomy.products.map((p) => <MenuItem key={p.id} value={String(p.id)}>{p.name}</MenuItem>)}
        </TextField>
        <TextField select size="small" label={t('knowledge.admin.list.status')} value={status} onChange={(e) => setStatus(e.target.value)} sx={{ minWidth: 150 }}>
          <MenuItem value="">{t('knowledge.admin.list.allStates')}</MenuItem>
          {['DRAFT', 'IN_REVIEW', 'APPROVED', 'SCHEDULED', 'PUBLISHED', 'DEPRECATED', 'ARCHIVED'].map((s) => <MenuItem key={s} value={s}>{t(`knowledge.admin.state.${s}`)}</MenuItem>)}
        </TextField>
        <TextField select size="small" label={t('knowledge.admin.videos.source')} value={source} onChange={(e) => setSource(e.target.value)} sx={{ minWidth: 150 }}>
          <MenuItem value="">{t('knowledge.admin.videos.allSources')}</MenuItem>
          {['YOUTUBE', 'AWS_S3', 'EXTERNAL_URL'].map((s) => <MenuItem key={s} value={s}>{t(`knowledge.source.${s}`)}</MenuItem>)}
        </TextField>
        <Button variant="contained" startIcon={<Plus size={16} />} component={RouterLink} to="/knowledge-management/videos/new">{t('knowledge.admin.videos.add')}</Button>
      </Box>
      {error && <Alert severity="error">{t('knowledge.common.error')}</Alert>}
      {!data && !error && <CircularProgress aria-label={t('knowledge.common.loading')} />}
      {data && data.items.length === 0 && <EmptyState title={t('knowledge.admin.list.empty')} />}
      {data && data.items.length > 0 && (mobile ? (
        <Box sx={{ display: 'grid', gap: 1.5 }}>
          {data.items.map((row) => (
            <Paper key={row.id} variant="outlined" component={RouterLink} to={`/knowledge-management/videos/${row.id}`} sx={{ p: 1.5, display: 'flex', gap: 1.5, textDecoration: 'none', color: 'inherit' }}>
              <Box sx={{ width: 96, height: 54, borderRadius: 1, bgcolor: '#0E1A3A', backgroundImage: row.thumbnailUrl ? `url("${row.thumbnailUrl}")` : 'none', backgroundSize: 'cover', flexShrink: 0 }} />
              <Box sx={{ minWidth: 0 }}>
                <Typography sx={{ fontWeight: 600 }}>{row.title}</Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>{productLabel(row)}</Typography>
                <StateChip state={row.workflowState} />
              </Box>
            </Paper>
          ))}
        </Box>
      ) : (
        <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
          <Table size="small">
            <TableHead>
              <TableRow>{['video', 'product', 'source', 'duration', 'status', 'views', 'updated'].map((c) => <TableCell key={c}>{t(`knowledge.admin.videos.columns.${c}`)}</TableCell>)}</TableRow>
            </TableHead>
            <TableBody>
              {data.items.map((row) => (
                <TableRow key={row.id} hover>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center' }}>
                      <Box sx={{ width: 88, height: 50, borderRadius: 1, bgcolor: '#0E1A3A', backgroundImage: row.thumbnailUrl ? `url("${row.thumbnailUrl}")` : 'none', backgroundSize: 'cover', flexShrink: 0 }} />
                      <Box sx={{ minWidth: 0 }}>
                        <Box component={RouterLink} to={`/knowledge-management/videos/${row.id}`} sx={{ fontWeight: 600, color: 'text.primary' }}>{row.title}</Box>
                        {row.shortDescription && <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }} noWrap>{row.shortDescription}</Typography>}
                        {row.tags.length > 0 && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{row.tags.join(', ')}</Typography>}
                      </Box>
                    </Box>
                  </TableCell>
                  <TableCell>{productLabel(row)}</TableCell>
                  <TableCell>{row.videoSource ? t(`knowledge.source.${row.videoSource}`) : '—'}</TableCell>
                  <TableCell>{formatDuration(row.durationSeconds) ?? '—'}</TableCell>
                  <TableCell><StateChip state={row.workflowState} /></TableCell>
                  <TableCell>{row.views}</TableCell>
                  <TableCell>{formatDate(row.updatedAt, i18n.language)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      ))}
    </Box>
  )
}
