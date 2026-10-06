import { Alert, Box, CircularProgress } from '@mui/material'
import { BookOpen } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Navigate, NavLink, Outlet } from 'react-router-dom'
import { knowledgeAdminApi, type Me, type Taxonomy } from '../../api/knowledgeApi'
import { KnowledgeAdminContext } from './knowledgeAdminContext'
import { useAuth } from '../../auth/AuthProvider'
import { PageHeader } from '../../components/layout/PageHeader'

const TABS = [
  { key: 'dashboard', path: '/knowledge-management', end: true },
  { key: 'content', path: '/knowledge-management/content' },
  { key: 'videos', path: '/knowledge-management/videos' },
  { key: 'media', path: '/knowledge-management/media' },
  { key: 'taxonomy', path: '/knowledge-management/taxonomy', publisher: true },
  { key: 'searchIndex', path: '/knowledge-management/search-index', publisher: true },
  { key: 'analytics', path: '/knowledge-management/analytics' },
]

/**
 * Knowledge Management frame (REQ-KNW-008.4): only users holding a knowledge
 * permission get here — the backend answers 403 to everyone else, and this
 * page then shows "no access". Publisher-only tabs are hidden for
 * contributors (BR-KPRM-004); the backend enforces it either way.
 */
export function KnowledgeAdminLayout() {
  const { t } = useTranslation()
  const auth = useAuth()
  const [me, setMe] = useState<Me | null>(null)
  const [taxonomy, setTaxonomy] = useState<Taxonomy | null>(null)
  const [denied, setDenied] = useState(false)

  const reloadTaxonomy = () => { knowledgeAdminApi.taxonomy().then(setTaxonomy).catch(() => setTaxonomy({ products: [], categories: [] })) }
  useEffect(() => {
    if (!auth.isAuthenticated) return
    knowledgeAdminApi.me().then((m) => { setMe(m); reloadTaxonomy() }).catch(() => setDenied(true))
  }, [auth.isAuthenticated])

  if (!auth.isAuthenticated) return <Navigate to="/" replace />
  if (denied) return <Alert severity="warning">{t('knowledge.admin.noAccess')}</Alert>
  if (!me || !taxonomy) return <Box sx={{ display: 'grid', placeItems: 'center', py: 6 }}><CircularProgress aria-label={t('knowledge.common.loading')} /></Box>
  return (
    <KnowledgeAdminContext.Provider value={{ me, taxonomy, reloadTaxonomy }}>
      <PageHeader icon={BookOpen} accent="cyan" area="content" title={t('knowledge.admin.title')} subtitle={t('knowledge.admin.subtitle')} />
      <Box component="nav" aria-label={t('knowledge.admin.nav.label')} sx={{ display: 'flex', gap: 0.5, overflowX: 'auto', pb: 1, mb: 2.5, borderBottom: 1, borderColor: 'divider' }}>
        {TABS.filter((tab) => !tab.publisher || me.publisher).map((tab) => (
          <Box key={tab.key} component={NavLink} to={tab.path} end={tab.end} sx={{
            px: 1.5, py: 0.75, borderRadius: 2, whiteSpace: 'nowrap', textDecoration: 'none', color: 'text.secondary', fontWeight: 600, fontSize: 14,
            '&.active': { color: 'primary.main', bgcolor: 'action.selected' }, '&:hover': { bgcolor: 'action.hover' },
          }}>
            {t(`knowledge.admin.nav.${tab.key}`)}
          </Box>
        ))}
      </Box>
      <Outlet />
    </KnowledgeAdminContext.Provider>
  )
}

export function StateChip({ state }: { state: string }) {
  const { t } = useTranslation()
  const tone: Record<string, string> = {
    PUBLISHED: '#047857', DRAFT: '#4A5568', IN_REVIEW: '#B45309', APPROVED: '#1D4ED8', SCHEDULED: '#6D28D9', DEPRECATED: '#B45309', ARCHIVED: '#6B7280',
  }
  return (
    <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.75, fontWeight: 600, fontSize: 13, color: tone[state] ?? 'text.secondary' }}>
      <Box component="span" aria-hidden sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: tone[state] ?? '#94A3B8' }} />
      {t(`knowledge.admin.state.${state}`)}
    </Box>
  )
}
