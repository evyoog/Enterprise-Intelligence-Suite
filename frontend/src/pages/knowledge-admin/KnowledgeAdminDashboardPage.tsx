import { Alert, Box, Button, CircularProgress, Paper, Typography } from '@mui/material'
import { AlertTriangle, CalendarClock, CheckCircle2, Clock, FileClock, FilePen, Layers } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { knowledgeAdminApi, type Dashboard } from '../../api/knowledgeApi'
import { SummaryCard } from '../../components/ui/SummaryCard'

/** Knowledge Management dashboard (knowledge-admin-dashboard, prompt 6.1). */
export function KnowledgeAdminDashboardPage() {
  const { t } = useTranslation()
  const [data, setData] = useState<Dashboard | null>(null)
  const [error, setError] = useState(false)
  useEffect(() => { knowledgeAdminApi.dashboard().then(setData).catch(() => setError(true)) }, [])
  if (error) return <Alert severity="error">{t('knowledge.common.error')}</Alert>
  if (!data) return <CircularProgress aria-label={t('knowledge.common.loading')} />
  const cards = [
    { icon: Layers, label: t('knowledge.admin.dashboard.total'), value: data.total },
    { icon: CheckCircle2, label: t('knowledge.admin.dashboard.published'), value: data.byState.PUBLISHED ?? 0, color: '#10B981' },
    { icon: FilePen, label: t('knowledge.admin.dashboard.drafts'), value: data.byState.DRAFT ?? 0 },
    { icon: Clock, label: t('knowledge.admin.dashboard.review'), value: data.byState.IN_REVIEW ?? 0, color: '#F59E0B' },
    { icon: CalendarClock, label: t('knowledge.admin.dashboard.scheduled'), value: data.scheduled, color: '#8B5CF6' },
    { icon: FileClock, label: t('knowledge.admin.dashboard.expired'), value: data.expired, color: '#EF4444' },
    { icon: AlertTriangle, label: t('knowledge.admin.dashboard.requiresReview'), value: data.requiresReview, color: '#F59E0B' },
  ]
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr 1fr', md: 'repeat(4, 1fr)', xl: 'repeat(7, 1fr)' } }}>
        {cards.map((c) => <SummaryCard key={c.label} icon={c.icon} label={c.label} value={c.value} color={c.color} />)}
      </Box>
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' } }}>
        <Paper variant="outlined" component="section" aria-labelledby="kd-gaps" sx={{ p: 2 }}>
          <Typography id="kd-gaps" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.dashboard.gaps')}</Typography>
          {data.gaps.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>}
          {data.gaps.map((g) => (
            <Box key={g.query} sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 1, borderBottom: 1, borderColor: 'divider', flexWrap: 'wrap' }}>
              <AlertTriangle size={16} aria-hidden />
              <Typography variant="body2" sx={{ flex: 1, minWidth: 200 }}>{t('knowledge.admin.dashboard.gap', { query: g.query, count: g.searches })}</Typography>
              <Button size="small" component={RouterLink} to={`/knowledge-management/content/new?title=${encodeURIComponent(g.query)}`}>{t('knowledge.admin.dashboard.createContent')}</Button>
            </Box>
          ))}
        </Paper>
        <Paper variant="outlined" component="section" aria-labelledby="kd-types" sx={{ p: 2 }}>
          <Typography id="kd-types" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.dashboard.byType')}</Typography>
          {Object.entries(data.byType).map(([type, count]) => {
            const max = Math.max(...Object.values(data.byType), 1)
            return (
              <Box key={type} sx={{ display: 'grid', gridTemplateColumns: '140px 1fr 40px', gap: 1, alignItems: 'center', py: 0.5 }}>
                <Typography variant="body2">{t(`knowledge.typePlural.${type}`)}</Typography>
                <Box sx={{ height: 8, borderRadius: 4, bgcolor: 'action.hover' }}><Box sx={{ height: 8, borderRadius: 4, bgcolor: 'primary.main', width: `${(count / max) * 100}%` }} /></Box>
                <Typography variant="body2" sx={{ textAlign: 'right', fontWeight: 600 }}>{count}</Typography>
              </Box>
            )
          })}
        </Paper>
        <Paper variant="outlined" component="section" aria-labelledby="kd-viewed" sx={{ p: 2 }}>
          <Typography id="kd-viewed" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.dashboard.mostViewed')}</Typography>
          {data.mostViewed.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>}
          {data.mostViewed.map((r) => (
            <Box key={r.contentId} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.75 }}>
              <RouterLink to={`/knowledge-management/content/${r.contentId}`}>{r.title}</RouterLink>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>{r.count}</Typography>
            </Box>
          ))}
        </Paper>
        <Paper variant="outlined" component="section" aria-labelledby="kd-rated" sx={{ p: 2 }}>
          <Typography id="kd-rated" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.dashboard.lowestRated')}</Typography>
          {data.lowestRated.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.dashboard.none')}</Typography>}
          {data.lowestRated.map((r) => (
            <Box key={r.contentId} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.75, gap: 1 }}>
              <RouterLink to={`/knowledge-management/content/${r.contentId}`}>{r.title}</RouterLink>
              <Typography variant="body2">{t('knowledge.admin.dashboard.helpful', { percent: r.helpfulPercent, votes: r.votes })}</Typography>
            </Box>
          ))}
        </Paper>
      </Box>
    </Box>
  )
}
