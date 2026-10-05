import { Alert, Box, CircularProgress, MenuItem, Paper, TextField, Typography } from '@mui/material'
import { Download, Eye, PlayCircle, Search, SearchX, ThumbsUp, Ticket, Timer } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { knowledgeAdminApi, type Analytics, type Ranked } from '../../api/knowledgeApi'
import { SummaryCard } from '../../components/ui/SummaryCard'

function RankedList({ title, rows }: { title: string; rows: Ranked[] }) {
  const { t } = useTranslation()
  return (
    <Paper variant="outlined" component="section" sx={{ p: 2 }} aria-label={title}>
      <Typography component="h2" sx={{ fontWeight: 700, mb: 1 }}>{title}</Typography>
      {rows.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>}
      {rows.map((r) => (
        <Box key={r.contentId} sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, py: 0.5 }}>
          <RouterLink to={`/knowledge-management/content/${r.contentId}`}>{r.title}</RouterLink>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>{r.count}</Typography>
        </Box>
      ))}
    </Paper>
  )
}

/** Knowledge analytics (knowledge-admin-analytics, REQ-KNW-006.3–.6): aggregates only. */
export function KnowledgeAnalyticsPage() {
  const { t } = useTranslation()
  const [days, setDays] = useState(30)
  const [loaded, setLoaded] = useState<{ days: number; data: Analytics } | null>(null)
  const [error, setError] = useState(false)
  useEffect(() => { knowledgeAdminApi.analytics(days).then((data) => setLoaded({ days, data })).catch(() => setError(true)) }, [days])
  const data = loaded?.days === days ? loaded.data : null
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <TextField select size="small" label={t('knowledge.admin.analytics.period')} value={days} onChange={(e) => setDays(Number(e.target.value))} sx={{ width: 180 }}>
        {[7, 30, 90].map((d) => <MenuItem key={d} value={d}>{t('knowledge.admin.analytics.days', { count: d })}</MenuItem>)}
      </TextField>
      {error && <Alert severity="error">{t('knowledge.common.error')}</Alert>}
      {!data && !error && <CircularProgress aria-label={t('knowledge.common.loading')} />}
      {data && (
        <>
          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr 1fr', md: 'repeat(4, 1fr)' } }}>
            <SummaryCard icon={Eye} label={t('knowledge.admin.analytics.views')} value={data.contentViews} />
            <SummaryCard icon={PlayCircle} label={t('knowledge.admin.analytics.plays')} value={data.videoPlays} color="#6366F1" />
            <SummaryCard icon={Download} label={t('knowledge.admin.analytics.downloads')} value={data.downloads} />
            <SummaryCard icon={Search} label={t('knowledge.admin.analytics.searches')} value={data.knowledgeSearches} />
            <SummaryCard icon={SearchX} label={t('knowledge.admin.analytics.noResult')} value={data.noResultSearches} color="#EF4444" />
            <SummaryCard icon={ThumbsUp} label={t('knowledge.admin.analytics.helpful')} value={data.helpfulPercent == null ? '—' : `${data.helpfulPercent}%`} color="#10B981" />
            <SummaryCard icon={Ticket} label={t('knowledge.admin.analytics.tickets')} value={data.ticketsFromKnowledge} />
            <SummaryCard icon={Timer} label={t('knowledge.admin.analytics.avgWatch')}
              value={data.averageWatchSeconds == null ? '—' : t('knowledge.admin.analytics.seconds', { count: data.averageWatchSeconds })}
              hint={`${t('knowledge.admin.analytics.completions')}: ${data.videoCompletions}`} />
          </Box>
          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '1fr 1fr', xl: 'repeat(3, 1fr)' } }}>
            <RankedList title={t('knowledge.admin.analytics.mostViewed')} rows={data.mostViewed} />
            <RankedList title={t('knowledge.admin.analytics.mostWatched')} rows={data.mostWatched} />
            <RankedList title={t('knowledge.admin.analytics.mostDownloaded')} rows={data.mostDownloaded} />
            <Paper variant="outlined" component="section" aria-labelledby="ka-searched" sx={{ p: 2 }}>
              <Typography id="ka-searched" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.analytics.mostSearched')}</Typography>
              {data.mostSearched.length === 0 ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>
                : <Box component="ol" sx={{ m: 0, pl: 3 }}>{data.mostSearched.map((q) => <li key={q}>{q}</li>)}</Box>}
            </Paper>
            <Paper variant="outlined" component="section" aria-labelledby="ka-gaps" sx={{ p: 2 }}>
              <Typography id="ka-gaps" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.analytics.gaps')}</Typography>
              {data.gaps.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>}
              {data.gaps.map((g) => (
                <Box key={g.query} sx={{ py: 0.5 }}>
                  <Typography variant="body2">{t('knowledge.admin.dashboard.gap', { query: g.query, count: g.searches })}</Typography>
                  <RouterLink to={`/knowledge-management/content/new?title=${encodeURIComponent(g.query)}`}>{t('knowledge.admin.dashboard.createContent')}</RouterLink>
                </Box>
              ))}
            </Paper>
            <Paper variant="outlined" component="section" aria-labelledby="ka-source" sx={{ p: 2 }}>
              <Typography id="ka-source" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.analytics.bySource')}</Typography>
              {Object.keys(data.playsBySource).length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>}
              {Object.entries(data.playsBySource).map(([k, v]) => (
                <Box key={k} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.5 }}>
                  <Typography variant="body2">{k === 'UNKNOWN' ? '—' : t(`knowledge.source.${k}`)}</Typography><Typography variant="body2" sx={{ fontWeight: 600 }}>{v}</Typography>
                </Box>
              ))}
            </Paper>
            <Paper variant="outlined" component="section" aria-labelledby="ka-rated" sx={{ p: 2 }}>
              <Typography id="ka-rated" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.analytics.lowestRated')}</Typography>
              {data.lowestRated.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>}
              {data.lowestRated.map((r) => (
                <Box key={r.contentId} sx={{ py: 0.5 }}>
                  <RouterLink to={`/knowledge-management/content/${r.contentId}`}>{r.title}</RouterLink>
                  <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>{t('knowledge.admin.dashboard.helpful', { percent: r.helpfulPercent, votes: r.votes })}</Typography>
                </Box>
              ))}
            </Paper>
            <Paper variant="outlined" component="section" aria-labelledby="ka-feedback" sx={{ p: 2 }}>
              <Typography id="ka-feedback" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.analytics.feedback')}</Typography>
              {data.recentFeedback.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>}
              {data.recentFeedback.map((f, i) => (
                <Box key={i} sx={{ py: 0.5 }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>{f.title}</Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary' }}>{[f.reason ? t(`knowledge.feedback.reason.${f.reason}`) : f.kind === 'OUTDATED' ? t('knowledge.feedback.reportOutdated') : f.kind === 'SUGGESTION' ? t('knowledge.feedback.suggest') : null, f.comment].filter(Boolean).join(' — ')}</Typography>
                </Box>
              ))}
            </Paper>
          </Box>
        </>
      )}
    </Box>
  )
}
