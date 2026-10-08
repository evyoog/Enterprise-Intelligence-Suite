import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { X } from 'lucide-react'
import { Alert, Box, Button, Chip, FormControlLabel, IconButton, Paper, Stack, Switch, TextField, Typography } from '@mui/material'
import type { OrgNode, OrgNodeDetail, OrgNodeHistoryEntry, OrgTree } from '../../api/orgHierarchyApi'
import { OrgChart } from './OrgChart'
import { useOrgChartModel } from './useOrgChartModel'

/** Read-only org chart with search, filters and a details panel (REQ-TEN-007 Structure tab). */
export function OrgStructureViewer({ tree, loadDetail, loadHistory }: {
  tree: OrgTree
  loadDetail: (nodeId: number) => Promise<OrgNodeDetail>
  loadHistory: (nodeId: number) => Promise<OrgNodeHistoryEntry[]>
}) {
  const { t } = useTranslation()
  const m = useOrgChartModel(tree.nodes, tree.levels)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const selected = selectedId === null ? undefined : m.byId.get(selectedId)
  const fitKey = `${tree.nodes.length}|${[...m.effectiveExpanded].sort().join(',')}|${m.activeOnly}|${selectedId !== null}`

  if (tree.nodes.length === 0) {
    return <Alert severity="info">{t('orgDirectory.detail.noStructure')}</Alert>
  }
  return (
    <Box>
      <Stack direction="row" spacing={1.5} useFlexGap sx={{ mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField size="small" label={t('orgStructure.search')} placeholder={t('orgStructure.searchHint')}
          value={m.query} onChange={(e) => m.setQuery(e.target.value)} sx={{ minWidth: 260 }} />
        <Stack direction="row" spacing={0.5} useFlexGap role="group" aria-label={t('orgStructure.filterByType')} sx={{ flexWrap: 'wrap' }}>
          {tree.levels.map((l) => (
            <Chip key={l.type} size="small" label={l.label} clickable onClick={() => m.toggleType(l.type)}
              color={m.typeFilter.has(l.type) ? 'primary' : 'default'} aria-pressed={m.typeFilter.has(l.type)} />
          ))}
        </Stack>
        <FormControlLabel control={<Switch size="small" checked={m.activeOnly} onChange={(e) => m.setActiveOnly(e.target.checked)} />}
          label={t('orgStructure.activeOnly')} />
        <Box sx={{ flex: 1 }} />
        <Button size="small" onClick={m.expandAll}>{t('orgStructure.expandAll')}</Button>
        <Button size="small" onClick={m.collapseAll}>{t('orgStructure.collapseAll')}</Button>
      </Stack>
      {m.matchIds && m.matchIds.size === 0 && <Typography color="text.secondary" sx={{ mb: 1 }}>{t('orgStructure.noMatches')}</Typography>}
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: selected ? 'minmax(0, 1fr) 380px' : '1fr' } }}>
        <OrgChart roots={m.roots} byParent={m.byParent} descendants={m.descendants} expanded={m.effectiveExpanded}
          onToggle={m.toggle} selectedId={selectedId} matchIds={m.matchIds} labelOf={m.labelOf} fitKey={fitKey}
          onSelect={(n: OrgNode) => setSelectedId(n.id)} />
        {selected && (
          <Paper variant="outlined" sx={{ p: 2, maxHeight: 640, overflow: 'auto' }}>
            <ViewerDetails key={selected.id} node={selected} labelOf={m.labelOf} loadDetail={loadDetail} loadHistory={loadHistory}
              onClose={() => setSelectedId(null)} />
          </Paper>
        )}
      </Box>
    </Box>
  )
}

function ViewerDetails({ node, labelOf, loadDetail, loadHistory, onClose }: {
  node: OrgNode
  labelOf: (type: string) => string
  loadDetail: (nodeId: number) => Promise<OrgNodeDetail>
  loadHistory: (nodeId: number) => Promise<OrgNodeHistoryEntry[]>
  onClose: () => void
}) {
  const { t } = useTranslation()
  const [detail, setDetail] = useState<OrgNodeDetail | null>(null)
  const [history, setHistory] = useState<OrgNodeHistoryEntry[] | null>(null)
  useEffect(() => {
    let alive = true
    loadDetail(node.id).then((d) => { if (alive) setDetail(d) }).catch(() => {})
    loadHistory(node.id).then((h) => { if (alive) setHistory(h) }).catch(() => {})
    return () => { alive = false }
  }, [node.id, loadDetail, loadHistory])
  const person = (m: { firstName?: string; lastName?: string; email?: string }) => [m.firstName, m.lastName].filter(Boolean).join(' ') || m.email
  return (
    <Box>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box>
          <Typography variant="h6" component="h3">{node.name}</Typography>
          <Typography variant="body2" color="text.secondary">{labelOf(node.type)}{node.code ? ` · ${node.code}` : ''}</Typography>
        </Box>
        <IconButton size="small" aria-label={t('orgStructure.close')} onClick={onClose}><X size={16} /></IconButton>
      </Stack>
      <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: 'max-content 1fr', gap: 1, columnGap: 3, mt: 2, mb: 0 }}>
        <Typography component="dt" color="text.secondary">{t('orgStructure.path')}</Typography>
        <Typography component="dd" sx={{ m: 0 }}>{detail?.path.join(' › ') ?? node.name}</Typography>
        <Typography component="dt" color="text.secondary">{t('orgStructure.status')}</Typography>
        <Typography component="dd" sx={{ m: 0 }}>{node.active ? t('orgStructure.active') : t('orgStructure.inactive')}</Typography>
        <Typography component="dt" color="text.secondary">{t('orgStructure.description')}</Typography>
        <Typography component="dd" sx={{ m: 0 }}>{node.description || '—'}</Typography>
      </Box>
      <Typography variant="subtitle1" component="h4" sx={{ mt: 2, mb: 1 }}>{t('orgStructure.members')}</Typography>
      {detail && detail.members.length === 0 && <Typography color="text.secondary">{t('orgStructure.noMembers')}</Typography>}
      <Box component="ul" sx={{ m: 0, p: 0, listStyle: 'none' }}>
        {detail?.members.map((mm) => <li key={mm.memberId}>{person(mm)} <Typography component="span" variant="body2" color="text.secondary">{mm.email}</Typography></li>)}
      </Box>
      <Typography variant="subtitle1" component="h4" sx={{ mt: 2, mb: 1 }}>{t('orgStructure.history')}</Typography>
      {history && history.length === 0 && <Typography color="text.secondary">{t('orgStructure.noHistory')}</Typography>}
      <Box component="ul" sx={{ m: 0, p: 0, listStyle: 'none' }}>
        {history?.map((h) => (
          <li key={h.id}>
            {t('orgStructure.movedFromTo', { from: h.previousParentName ?? t('orgStructure.noParent'), to: h.newParentName ?? t('orgStructure.noParent') })}
            {' · '}<Typography component="span" variant="body2" color="text.secondary">{new Date(h.effectiveAt).toLocaleString()}</Typography>
          </li>
        ))}
      </Box>
    </Box>
  )
}
