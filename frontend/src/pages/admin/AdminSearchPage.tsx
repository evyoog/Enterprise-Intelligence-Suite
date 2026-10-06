import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Grid, IconButton, MenuItem, Paper, Skeleton, Tab, Table, TableBody, TableCell, TableHead,
  TableRow, Tabs, TextField, Typography,
} from '@mui/material'
import { BrainCircuit, Database, FileText, Gauge, RefreshCw, SearchCheck, SearchX, Sparkles, Trash2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  searchAdminApi, type QueryCount, type SearchIndexStatus, type SearchInsights, type SynonymGroup,
} from '../../api/searchAdminApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { StatusTile } from '../../components/settings/SettingsSection'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

type TabKey = 'index' | 'synonyms' | 'insights'

/**
 * "/admin/search" — C70, `MANAGE_SEARCH`: the search index (status, rebuild),
 * synonyms and search insights.
 */
export function AdminSearchPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useState<TabKey>('index')
  return (
    <>
      <PageHeader icon={SearchCheck} accent="indigo" area="administration" title={t('searchAdmin.title')} subtitle={t('searchAdmin.subtitle')} />
      <Tabs value={tab} onChange={(_, v: TabKey) => setTab(v)} sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}>
        <Tab value="index" label={t('searchAdmin.tabIndex')} />
        <Tab value="synonyms" label={t('searchAdmin.tabSynonyms')} />
        <Tab value="insights" label={t('searchAdmin.tabInsights')} />
      </Tabs>
      {tab === 'index' && <IndexPanel />}
      {tab === 'synonyms' && <SynonymsPanel />}
      {tab === 'insights' && <InsightsPanel />}
    </>
  )
}

function IndexPanel() {
  const { t } = useTranslation()
  const { formatDateTime } = useLocalePreference()
  const [status, setStatus] = useState<SearchIndexStatus | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [confirm, setConfirm] = useState<'rebuild' | 'reembed' | null>(null)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<string | null>(null)

  const load = useCallback(() => {
    searchAdminApi.status().then((s) => { setStatus(s); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('searchAdmin.loadError')))
  }, [t])

  useEffect(() => { load() }, [load])
  const running = status?.lastRun?.status === 'RUNNING'
  useEffect(() => {
    if (!running) return
    const handle = setInterval(load, 3000)
    return () => clearInterval(handle)
  }, [running, load])

  const rebuild = (reembed: boolean) => {
    setBusy(true)
    searchAdminApi.rebuild(reembed)
      .then(() => { setNotice(t('searchAdmin.rebuildStarted')); setTimeout(load, 800) })
      .catch((e) => setNotice(e instanceof ApiError ? e.message : t('searchAdmin.rebuildFailed')))
      .finally(() => { setBusy(false); setConfirm(null) })
  }

  if (error) return <Alert severity="error">{error}</Alert>
  if (!status) return <Grid container spacing={2}>{[0, 1, 2, 3].map((i) => <Grid key={i} size={{ xs: 12, sm: 6, lg: 3 }}><Skeleton variant="rounded" height={84} /></Grid>)}</Grid>

  const totalDocs = Object.values(status.documents).reduce((a, b) => a + b, 0)
  const embedding = status.embedding
  const embeddingValue = !embedding.configured ? t('searchAdmin.notConfigured')
    : embedding.available ? (embedding.model ?? t('searchAdmin.available')) : t('searchAdmin.notAvailable')
  const run = status.lastRun

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      {notice && <Alert severity="info" onClose={() => setNotice(null)}>{notice}</Alert>}
      {!status.keywordIndexInstalled && <Alert severity="warning">{t('searchAdmin.basicEngineWarning')}</Alert>}
      {status.keywordIndexInstalled && !status.semanticInstalled && <Alert severity="info">{t('searchAdmin.noVectorWarning')}</Alert>}
      {embedding.provider === 'stub' && <Alert severity="warning">{t('searchAdmin.stubWarning')}</Alert>}

      <Grid container spacing={2}>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatusTile icon={Database} accent="indigo" label={t('searchAdmin.engine')}
            value={status.engine === 'POSTGRES' ? t('searchAdmin.enginePostgres') : t('searchAdmin.engineBasic')}
            status={status.engine === 'POSTGRES' ? t('searchAdmin.ok') : t('searchAdmin.limited')}
            statusColor={status.engine === 'POSTGRES' ? 'success' : 'warning'} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatusTile icon={FileText} accent="blue" label={t('searchAdmin.documents')} value={String(totalDocs)}
            status={Object.entries(status.documents).map(([k, v]) => `${t(`search.filter.${k}`)} ${v}`).join(' · ')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatusTile icon={Sparkles} accent="violet" label={t('searchAdmin.passages')}
            value={`${status.embeddedChunks} / ${status.chunks}`}
            status={status.pendingChunks > 0 ? t('searchAdmin.pending', { count: status.pendingChunks }) : t('searchAdmin.allEmbedded')}
            statusColor={status.pendingChunks > 0 ? 'warning' : 'success'} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatusTile icon={BrainCircuit} accent="teal" label={t('searchAdmin.embeddingModel')} value={embeddingValue}
            status={embedding.available ? t('searchAdmin.dimension', { count: embedding.dimension ?? 0 }) : (embedding.message ?? '')}
            statusColor={embedding.available ? 'success' : embedding.configured ? 'error' : 'default'} />
        </Grid>
      </Grid>

      <Paper variant="outlined" sx={{ p: 2.5 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap' }}>
          <Box sx={{ minWidth: 0 }}>
            <Typography component="h2" sx={{ fontWeight: 700 }}>{t('searchAdmin.rebuildTitle')}</Typography>
            <Typography sx={{ color: 'text.secondary', fontSize: 14, maxWidth: '70ch' }}>{t('searchAdmin.rebuildBody')}</Typography>
            {run && (
              <Typography sx={{ mt: 1, fontSize: 13 }}>
                {t('searchAdmin.lastRun', {
                  status: t(`searchAdmin.runStatus.${run.status}`), trigger: t(`searchAdmin.runTrigger.${run.trigger}`),
                  when: formatDateTime(run.finishedAt ?? run.startedAt), documents: run.documents, chunks: run.chunks, embedded: run.embedded,
                })}
                {run.error ? ` — ${run.error}` : ''}
              </Typography>
            )}
          </Box>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <Button variant="contained" startIcon={<RefreshCw size={16} />} disabled={running || busy} onClick={() => setConfirm('rebuild')}>
              {running ? t('searchAdmin.rebuilding') : t('searchAdmin.rebuild')}
            </Button>
            {status.semanticInstalled && (
              <Button variant="outlined" disabled={running || busy} onClick={() => setConfirm('reembed')}>{t('searchAdmin.reembed')}</Button>
            )}
          </Box>
        </Box>
      </Paper>

      <Paper variant="outlined" sx={{ p: 2.5 }}>
        <Typography component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('searchAdmin.settingsTitle')}</Typography>
        <Table size="small" aria-label={t('searchAdmin.settingsTitle')}>
          <TableBody>
            {Object.entries(status.settings).map(([key, value]) => (
              <TableRow key={key}>
                <TableCell sx={{ width: '50%' }}>{t(`searchAdmin.setting.${key}`, { defaultValue: key })}</TableCell>
                <TableCell sx={{ fontVariantNumeric: 'tabular-nums' }}>{String(value)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      <ConfirmDialog
        open={confirm !== null}
        destructive={false}
        title={confirm === 'reembed' ? t('searchAdmin.confirmReembedTitle') : t('searchAdmin.confirmRebuildTitle')}
        body={confirm === 'reembed' ? t('searchAdmin.confirmReembedBody') : t('searchAdmin.confirmRebuildBody')}
        confirmLabel={confirm === 'reembed' ? t('searchAdmin.reembed') : t('searchAdmin.rebuild')}
        busy={busy}
        onConfirm={() => rebuild(confirm === 'reembed')}
        onClose={() => setConfirm(null)}
      />
    </Box>
  )
}

function SynonymsPanel() {
  const { t } = useTranslation()
  const [groups, setGroups] = useState<SynonymGroup[] | null>(null)
  const [terms, setTerms] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [toDelete, setToDelete] = useState<SynonymGroup | null>(null)

  const load = useCallback(() => {
    searchAdminApi.synonyms().then(setGroups).catch((e) => setError(e instanceof ApiError ? e.message : t('searchAdmin.loadError')))
  }, [t])
  useEffect(() => { load() }, [load])

  const parsed = terms.split(',').map((s) => s.trim()).filter(Boolean)
  const add = (e: React.FormEvent) => {
    e.preventDefault()
    if (parsed.length < 2) { setError(t('searchAdmin.synonymMinimum')); return }
    setSaving(true)
    searchAdminApi.createSynonym(parsed)
      .then(() => { setTerms(''); setError(null); load() })
      .catch((err) => setError(err instanceof ApiError ? err.message : t('searchAdmin.saveFailed')))
      .finally(() => setSaving(false))
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Paper variant="outlined" sx={{ p: 2.5 }} component="form" onSubmit={add}>
        <Typography component="h2" sx={{ fontWeight: 700 }}>{t('searchAdmin.addSynonymTitle')}</Typography>
        <Typography sx={{ color: 'text.secondary', fontSize: 14, mb: 2, maxWidth: '70ch' }}>{t('searchAdmin.addSynonymBody')}</Typography>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'flex-start' }}>
          <TextField
            size="small"
            label={t('searchAdmin.synonymTerms')}
            placeholder={t('searchAdmin.synonymPlaceholder')}
            value={terms}
            onChange={(e) => setTerms(e.target.value)}
            sx={{ flex: '1 1 320px' }}
            error={!!error}
            helperText={error ?? t('searchAdmin.synonymHelper')}
          />
          <Button type="submit" variant="contained" disabled={saving}>{t('searchAdmin.addSynonym')}</Button>
        </Box>
      </Paper>

      <Paper variant="outlined" sx={{ p: 2.5 }}>
        <Typography component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('searchAdmin.synonymGroups')}</Typography>
        {groups === null && <Skeleton variant="rounded" height={60} />}
        {groups?.length === 0 && <Typography sx={{ color: 'text.secondary' }}>{t('searchAdmin.noSynonyms')}</Typography>}
        <Box component="ul" sx={{ listStyle: 'none', m: 0, p: 0, display: 'grid', gap: 1 }}>
          {groups?.map((group) => (
            <Box component="li" key={group.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', py: 0.5, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap', flex: 1 }}>
                {group.terms.map((term) => <Chip key={term} label={term} size="small" />)}
              </Box>
              <IconButton aria-label={t('searchAdmin.deleteSynonym', { terms: group.terms.join(', ') })} onClick={() => setToDelete(group)}>
                <Trash2 size={16} />
              </IconButton>
            </Box>
          ))}
        </Box>
      </Paper>

      <ConfirmDialog
        open={toDelete !== null}
        title={t('searchAdmin.deleteSynonymTitle')}
        body={toDelete ? toDelete.terms.join(', ') : undefined}
        confirmLabel={t('searchAdmin.delete')}
        onConfirm={() => {
          if (!toDelete) return
          searchAdminApi.deleteSynonym(toDelete.id).then(load).catch(() => setError(t('searchAdmin.saveFailed')))
          setToDelete(null)
        }}
        onClose={() => setToDelete(null)}
      />
    </Box>
  )
}

function InsightsPanel() {
  const { t } = useTranslation()
  const [days, setDays] = useState(30)
  const [insights, setInsights] = useState<SearchInsights | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    searchAdminApi.insights(days)
      .then((i) => { if (!cancelled) { setInsights(i); setError(null) } })
      .catch((e) => { if (!cancelled) setError(e instanceof ApiError ? e.message : t('searchAdmin.loadError')) })
    return () => { cancelled = true }
  }, [days, t])

  const pct = (rate: number) => `${Math.round(rate * 1000) / 10}%`

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
        <TextField select size="small" label={t('searchAdmin.period')} value={days} onChange={(e) => setDays(Number(e.target.value))} sx={{ width: 200 }}>
          {[7, 30, 90].map((d) => <MenuItem key={d} value={d}>{t('searchAdmin.lastDays', { count: d })}</MenuItem>)}
        </TextField>
        <Typography sx={{ color: 'text.secondary', fontSize: 13 }}>{t('searchAdmin.insightsNote')}</Typography>
      </Box>
      {error && <Alert severity="error">{error}</Alert>}
      {!insights && !error && <Skeleton variant="rounded" height={84} />}
      {insights && (
        <>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
              <StatusTile icon={SearchCheck} accent="blue" label={t('searchAdmin.totalSearches')} value={String(insights.totalSearches)} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
              <StatusTile icon={SearchX} accent="rose" label={t('searchAdmin.zeroResults')} value={pct(insights.zeroResultRate)}
                status={t('searchAdmin.searchesCount', { count: insights.zeroResultSearches })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
              <StatusTile icon={Sparkles} accent="violet" label={t('searchAdmin.semanticShare')} value={pct(insights.semanticUsedRate)} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
              <StatusTile icon={Gauge} accent="teal" label={t('searchAdmin.speed')} value={t('searchAdmin.ms', { count: insights.p95TookMs })}
                status={t('searchAdmin.averageMs', { count: insights.averageTookMs })} />
            </Grid>
          </Grid>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}><QueryTable title={t('searchAdmin.topQueries')} rows={insights.topQueries} /></Grid>
            <Grid size={{ xs: 12, md: 6 }}><QueryTable title={t('searchAdmin.topZeroQueries')} rows={insights.topZeroResultQueries} /></Grid>
          </Grid>
        </>
      )}
    </Box>
  )
}

function QueryTable({ title, rows }: { title: string; rows: QueryCount[] }) {
  const { t } = useTranslation()
  return (
    <Paper variant="outlined" sx={{ p: 2.5, height: '100%' }}>
      <Typography component="h2" sx={{ fontWeight: 700, mb: 1 }}>{title}</Typography>
      {rows.length === 0 ? <Typography sx={{ color: 'text.secondary', fontSize: 14 }}>{t('searchAdmin.noData')}</Typography> : (
        <Table size="small" aria-label={title}>
          <TableHead><TableRow><TableCell>{t('searchAdmin.query')}</TableCell><TableCell align="right">{t('searchAdmin.count')}</TableCell></TableRow></TableHead>
          <TableBody>
            {rows.map((r) => (
              <TableRow key={r.query}><TableCell>{r.query}</TableCell><TableCell align="right" sx={{ fontVariantNumeric: 'tabular-nums' }}>{r.count}</TableCell></TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Paper>
  )
}
