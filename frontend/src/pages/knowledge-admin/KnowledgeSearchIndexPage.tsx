import { Alert, Box, Button, CircularProgress, Paper, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import { Database, FileSearch, Film, ListTree, Sparkles } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { knowledgeAdminApi, type SearchIndexStatus } from '../../api/knowledgeApi'
import { SummaryCard } from '../../components/ui/SummaryCard'

/** Knowledge search index (knowledge-admin-search-index, prompt 6.6). Publishers only. */
export function KnowledgeSearchIndexPage() {
  const { t, i18n } = useTranslation()
  const [status, setStatus] = useState<SearchIndexStatus | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState(false)
  const load = () => knowledgeAdminApi.searchIndex().then(setStatus).catch(() => setError(true))
  useEffect(() => { load() }, [])
  if (error) return <Alert severity="error">{t('knowledge.common.error')}</Alert>
  if (!status) return <CircularProgress aria-label={t('knowledge.common.loading')} />
  const s = status.search
  const semantic = s.semanticInstalled && s.embedding?.configured
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {message && <Alert severity="success" onClose={() => setMessage(null)}>{message}</Alert>}
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr 1fr', md: 'repeat(5, 1fr)' } }}>
        <SummaryCard icon={Database} label={t('knowledge.admin.searchIndex.total')} value={status.totalContent} />
        <SummaryCard icon={FileSearch} label={t('knowledge.admin.searchIndex.indexable')} value={status.indexable} />
        <SummaryCard icon={ListTree} label={t('knowledge.admin.searchIndex.indexed')} value={status.indexed} color="#10B981" />
        <SummaryCard icon={Film} label={t('knowledge.admin.searchIndex.transcripts')} value={status.videosWithTranscript} />
        <SummaryCard icon={Sparkles} label={t('knowledge.admin.searchIndex.embedding')}
          value={semantic ? t('knowledge.admin.searchIndex.embeddingOn', { embedded: s.embeddedChunks, chunks: s.chunks }) : t('knowledge.admin.searchIndex.embeddingOff')} />
      </Box>
      <Paper variant="outlined" sx={{ p: 2, display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
        <Typography sx={{ flex: 1 }}>
          {t('knowledge.admin.searchIndex.lastRun')}: {s.lastRun?.finishedAt ? new Date(s.lastRun.finishedAt).toLocaleString(i18n.language) : '—'}
        </Typography>
        <Button variant="contained" onClick={() => knowledgeAdminApi.reindexAll().then((r) => { setMessage(t('knowledge.admin.searchIndex.done', { count: r.items })); load() })}>
          {t('knowledge.admin.searchIndex.reindexAll')}
        </Button>
      </Paper>
      <Paper variant="outlined" component="section" aria-labelledby="ksi-not" sx={{ p: 2 }}>
        <Typography id="ksi-not" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.searchIndex.notIndexed')}</Typography>
        {status.notIndexed.length === 0 ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography> : (
          <Table size="small">
            <TableHead><TableRow><TableCell>{t('knowledge.admin.list.columns.title')}</TableCell><TableCell>{t('knowledge.admin.list.columns.type')}</TableCell><TableCell /><TableCell /></TableRow></TableHead>
            <TableBody>
              {status.notIndexed.map((n) => (
                <TableRow key={n.id}>
                  <TableCell><RouterLink to={`/knowledge-management/content/${n.id}`}>{n.title}</RouterLink></TableCell>
                  <TableCell>{t(`knowledge.type.${n.contentType}`)}</TableCell>
                  <TableCell>{t(`knowledge.admin.searchIndex.reason.${n.reason}`)}</TableCell>
                  <TableCell><Button size="small" onClick={() => knowledgeAdminApi.reindexOne(n.id).then(load)}>{t('knowledge.admin.searchIndex.reindexOne')}</Button></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Paper>
    </Box>
  )
}
