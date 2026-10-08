import { ArrowDown, ArrowUp, ChevronDown, ChevronRight, Network, Trash2 } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState, type ReactElement } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControl,
  IconButton, InputLabel, MenuItem, Paper, Select, Stack, Tab, Tabs, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../api/client'
import {
  orgHierarchyApi, type OrgImportResult, type OrgLevel, type OrgNode, type OrgNodeDetail, type OrgNodeHistoryEntry,
  type OrgTree,
} from '../api/orgHierarchyApi'
import { organizationApi, type OrgMember } from '../api/registrationApi'
import { PageHeader } from '../components/layout/PageHeader'
import { ConfirmDialog } from '../components/ui/ConfirmDialog'

type DialogState =
  | { kind: 'create'; parent: OrgNode }
  | { kind: 'edit'; node: OrgNode }
  | { kind: 'move'; node: OrgNode }
  | { kind: 'deactivate'; node: OrgNode }
  | { kind: 'delete'; node: OrgNode }
  | { kind: 'levels' }
  | { kind: 'import' }
  | null

const message = (e: unknown) => (e instanceof ApiError ? e.message : 'Request failed')

/**
 * "/organization/structure" — REQ-TEN-006 Organization hierarchy. Organization
 * administrators (MANAGE_ORGANIZATION) model the organization as one tree of
 * nodes. The backend enforces every rule (level order, cycles, names, guards);
 * this page only offers what is valid and shows the backend's reason otherwise.
 */
export function OrganizationStructurePage() {
  const { t } = useTranslation()
  const [tree, setTree] = useState<OrgTree | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [expanded, setExpanded] = useState<Set<number>>(new Set())
  const [query, setQuery] = useState('')
  const [dialog, setDialog] = useState<DialogState>(null)

  const [version, setVersion] = useState(0)
  const load = useCallback(async () => { setVersion((v) => v + 1) }, [])

  useEffect(() => {
    let alive = true
    orgHierarchyApi.tree().then((data) => {
      if (!alive) return
      setTree(data)
      setError(null)
      const root = data.nodes.find((n) => n.parentId === null)
      setSelectedId((cur) => (cur !== null && data.nodes.some((n) => n.id === cur) ? cur : root?.id ?? null))
      setExpanded((cur) => (cur.size === 0 && root ? new Set([root.id]) : cur))
    }).catch((e) => { if (alive) setError(message(e)) })
    return () => { alive = false }
  }, [version])

  const nodes = useMemo(() => tree?.nodes ?? [], [tree])
  const byId = useMemo(() => new Map(nodes.map((n) => [n.id, n])), [nodes])
  const children = useMemo(() => {
    const map = new Map<number | null, OrgNode[]>()
    nodes.forEach((n) => map.set(n.parentId, [...(map.get(n.parentId) ?? []), n]))
    return map
  }, [nodes])
  const levels = useMemo(() => tree?.levels ?? [], [tree])
  const labelOf = useCallback((type: string) => levels.find((l) => l.type === type)?.label ?? type, [levels])
  const rankOf = useCallback((type: string) => levels.find((l) => l.type === type)?.rank ?? Number.MAX_SAFE_INTEGER, [levels])

  const searching = query.trim().length > 0
  const visible = useMemo(() => {
    if (!searching) return null
    const q = query.trim().toLowerCase()
    const keep = new Set<number>()
    nodes.forEach((n) => {
      if (`${n.name} ${n.code ?? ''} ${labelOf(n.type)}`.toLowerCase().includes(q)) {
        for (let cur: OrgNode | undefined = n; cur; cur = cur.parentId === null ? undefined : byId.get(cur.parentId)) keep.add(cur.id)
      }
    })
    return keep
  }, [searching, query, nodes, byId, labelOf])

  const toggle = (id: number) => setExpanded((cur) => {
    const next = new Set(cur)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    return next
  })

  const selected = selectedId === null ? undefined : byId.get(selectedId)
  const done = async (text: string) => { setDialog(null); setNotice(text); await load() }

  const renderNode = (node: OrgNode): ReactElement | null => {
    if (visible && !visible.has(node.id)) return null
    const kids = children.get(node.id) ?? []
    const open = searching || expanded.has(node.id)
    return (
      <Box component="li" key={node.id} role="treeitem" aria-expanded={kids.length ? open : undefined}
        aria-selected={selectedId === node.id} sx={{ listStyle: 'none' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, pl: 0.5 }}>
          {kids.length > 0 ? (
            <IconButton size="small" onClick={() => toggle(node.id)} aria-label={open ? t('orgStructure.collapseNode', { name: node.name }) : t('orgStructure.expandNode', { name: node.name })}>
              {open ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
            </IconButton>
          ) : <Box sx={{ width: 30 }} />}
          <Button onClick={() => setSelectedId(node.id)} color={selectedId === node.id ? 'primary' : 'inherit'}
            variant={selectedId === node.id ? 'contained' : 'text'} size="small"
            sx={{ justifyContent: 'flex-start', textTransform: 'none', flex: 1, opacity: node.active ? 1 : 0.6 }}>
            <Box component="span" sx={{ fontWeight: 600, mr: 1 }}>{node.name}</Box>
            <Box component="span" sx={{ fontSize: 12, opacity: 0.8 }}>
              {labelOf(node.type)}{node.code ? ` · ${node.code}` : ''}{node.memberCount ? ` · ${t('orgStructure.membersCount', { count: node.memberCount })}` : ''}
            </Box>
            {!node.active && <Chip size="small" label={t('orgStructure.inactive')} sx={{ ml: 1 }} />}
          </Button>
        </Box>
        {kids.length > 0 && open && (
          <Box component="ul" role="group" sx={{ m: 0, pl: 3 }}>{kids.map(renderNode)}</Box>
        )}
      </Box>
    )
  }

  const root = children.get(null)?.[0]

  return (
    <Box>
      <PageHeader title={t('orgStructure.title')} subtitle={t('orgStructure.subtitle')} area="organization" icon={Network}
        action={(
          <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
            <Button variant="outlined" onClick={() => setDialog({ kind: 'levels' })}>{t('orgStructure.configureLevels')}</Button>
            <Button variant="outlined" onClick={() => setDialog({ kind: 'import' })}>{t('orgStructure.importCsv')}</Button>
            <Button variant="contained" disabled={!selected} onClick={() => selected && setDialog({ kind: 'create', parent: selected })}>
              {t('orgStructure.addNode')}
            </Button>
          </Stack>
        )} />
      {notice && <Alert severity="success" onClose={() => setNotice(null)} sx={{ mb: 2 }}>{notice}</Alert>}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} action={<Button color="inherit" size="small" onClick={() => void load()}>{t('orgStructure.retry')}</Button>}>
          {t('orgStructure.loadError')} {error}
        </Alert>
      )}
      {!tree && !error && <CircularProgress aria-label="loading" />}
      {tree && (
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: 'minmax(320px, 5fr) 7fr' } }}>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <Stack direction="row" spacing={1} sx={{ mb: 1, alignItems: 'center' }}>
              <TextField size="small" fullWidth label={t('orgStructure.search')} placeholder={t('orgStructure.searchHint')}
                value={query} onChange={(e) => setQuery(e.target.value)} />
              <Button size="small" onClick={() => setExpanded(new Set(nodes.filter((n) => (children.get(n.id) ?? []).length).map((n) => n.id)))}>
                {t('orgStructure.expandAll')}
              </Button>
              <Button size="small" onClick={() => setExpanded(root ? new Set([root.id]) : new Set())}>{t('orgStructure.collapseAll')}</Button>
            </Stack>
            {visible && visible.size === 0 && <Typography color="text.secondary">{t('orgStructure.noMatches')}</Typography>}
            <Box component="ul" role="tree" aria-label={t('orgStructure.treeLabel')} sx={{ m: 0, p: 0 }}>
              {root && renderNode(root)}
            </Box>
          </Paper>
          <Paper variant="outlined" sx={{ p: 2 }}>
            {selected
              ? <NodeDetails key={selected.id} node={selected} labelOf={labelOf} byId={byId}
                  onDialog={setDialog} onChanged={load} />
              : <Typography color="text.secondary">{t('orgStructure.select')}</Typography>}
          </Paper>
        </Box>
      )}

      {dialog?.kind === 'create' && (
        <NodeFormDialog mode="create" parent={dialog.parent} levels={levels} rankOf={rankOf} children={children} byId={byId}
          onClose={() => setDialog(null)} onSaved={() => done(t('orgStructure.saved'))} />
      )}
      {dialog?.kind === 'edit' && (
        <NodeFormDialog mode="edit" node={dialog.node} parent={dialog.node.parentId === null ? undefined : byId.get(dialog.node.parentId)}
          levels={levels} rankOf={rankOf} children={children} byId={byId}
          onClose={() => setDialog(null)} onSaved={() => done(t('orgStructure.saved'))} />
      )}
      {dialog?.kind === 'move' && (
        <MoveDialog node={dialog.node} nodes={nodes} byId={byId} rankOf={rankOf} labelOf={labelOf}
          onClose={() => setDialog(null)} onSaved={() => done(t('orgStructure.saved'))} />
      )}
      {dialog?.kind === 'deactivate' && (
        <ActionConfirm title={t('orgStructure.deactivateTitle', { name: dialog.node.name })}
          body={t('orgStructure.deactivateBody', { children: dialog.node.childCount, members: dialog.node.memberCount })}
          confirmLabel={t('orgStructure.deactivateConfirm')} destructive={false}
          run={() => orgHierarchyApi.update(dialog.node.id, {
            name: dialog.node.name, type: dialog.node.type, code: dialog.node.code ?? undefined,
            description: dialog.node.description ?? undefined, active: false, force: true,
          })}
          onClose={() => setDialog(null)} onDone={() => done(t('orgStructure.saved'))} />
      )}
      {dialog?.kind === 'delete' && (
        <ActionConfirm title={t('orgStructure.deleteTitle', { name: dialog.node.name })} body={t('orgStructure.deleteBody')}
          confirmLabel={t('orgStructure.deleteConfirm')} destructive
          run={() => orgHierarchyApi.remove(dialog.node.id)}
          onClose={() => setDialog(null)} onDone={() => { setSelectedId(dialog.node.parentId); return done(t('orgStructure.saved')) }} />
      )}
      {dialog?.kind === 'levels' && (
        <LevelsDialog levels={levels} onClose={() => setDialog(null)} onSaved={() => done(t('orgStructure.saved'))} />
      )}
      {dialog?.kind === 'import' && (
        <ImportDialog onClose={() => setDialog(null)} onImported={() => load()} />
      )}
    </Box>
  )
}

function NodeDetails({ node, byId, labelOf, onDialog, onChanged }: {
  node: OrgNode
  byId: Map<number, OrgNode>
  labelOf: (type: string) => string
  onDialog: (d: DialogState) => void
  onChanged: () => Promise<void>
}) {
  const { t } = useTranslation()
  const [tab, setTab] = useState<'details' | 'history'>('details')
  const [detail, setDetail] = useState<OrgNodeDetail | null>(null)
  const [history, setHistory] = useState<OrgNodeHistoryEntry[] | null>(null)
  const [members, setMembers] = useState<OrgMember[]>([])
  const [memberId, setMemberId] = useState<number | ''>('')
  const [err, setErr] = useState<string | null>(null)
  const isRoot = node.parentId === null

  useEffect(() => {
    let alive = true
    orgHierarchyApi.detail(node.id).then((d) => { if (alive) setDetail(d) }).catch((e) => { if (alive) setErr(message(e)) })
    organizationApi.listMyOrgUsers().then((m) => { if (alive) setMembers(m) }).catch(() => {})
    return () => { alive = false }
  }, [node.id, node.memberCount])

  useEffect(() => {
    if (tab !== 'history') return
    let alive = true
    orgHierarchyApi.history(node.id).then((h) => { if (alive) setHistory(h) }).catch((e) => { if (alive) setErr(message(e)) })
    return () => { alive = false }
  }, [tab, node.id, node.parentId])

  const placedIds = new Set(detail?.members.map((m) => m.memberId))
  const candidates = members.filter((m) => !placedIds.has(m.organizationMemberId))
  const run = async (fn: () => Promise<OrgNodeDetail>) => {
    try { setDetail(await fn()); setErr(null); setMemberId(''); await onChanged() } catch (e) { setErr(message(e)) }
  }
  const nameOf = (m: { firstName?: string; lastName?: string; email?: string }) =>
    [m.firstName, m.lastName].filter(Boolean).join(' ') || m.email || ''

  return (
    <Box>
      <Stack direction="row" spacing={1} useFlexGap sx={{ justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap' }}>
        <Box>
          <Typography variant="h6" component="h2">{node.name}</Typography>
          <Typography variant="body2" color="text.secondary">{labelOf(node.type)}{node.code ? ` · ${node.code}` : ''}</Typography>
        </Box>
        <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
          <Button size="small" onClick={() => onDialog({ kind: 'create', parent: node })}>{t('orgStructure.addChild')}</Button>
          <Button size="small" onClick={() => onDialog({ kind: 'edit', node })}>{t('orgStructure.edit')}</Button>
          {!isRoot && <Button size="small" onClick={() => onDialog({ kind: 'move', node })}>{t('orgStructure.move')}</Button>}
          {!isRoot && (node.active
            ? <Button size="small" onClick={() => onDialog({ kind: 'deactivate', node })}>{t('orgStructure.deactivate')}</Button>
            : <Button size="small" onClick={async () => {
              try {
                await orgHierarchyApi.update(node.id, { name: node.name, type: node.type, code: node.code ?? undefined, description: node.description ?? undefined, active: true })
                await onChanged()
              } catch (e) { setErr(message(e)) }
            }}>{t('orgStructure.activate')}</Button>)}
          {!isRoot && <Button size="small" color="error" onClick={() => onDialog({ kind: 'delete', node })}>{t('orgStructure.delete')}</Button>}
        </Stack>
      </Stack>
      {err && <Alert severity="error" sx={{ mt: 2 }} onClose={() => setErr(null)}>{err}</Alert>}
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mt: 1, mb: 2 }}>
        <Tab value="details" label={t('orgStructure.details')} />
        <Tab value="history" label={t('orgStructure.history')} />
      </Tabs>
      {tab === 'details' && (
        <Box>
          <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: 'max-content 1fr', gap: 1, columnGap: 3, m: 0 }}>
            <Typography component="dt" color="text.secondary">{t('orgStructure.path')}</Typography>
            <Typography component="dd" sx={{ m: 0 }}>{detail?.path.join(' › ') ?? node.name}</Typography>
            <Typography component="dt" color="text.secondary">{t('orgStructure.status')}</Typography>
            <Typography component="dd" sx={{ m: 0 }}>{node.active ? t('orgStructure.active') : t('orgStructure.inactive')}</Typography>
            <Typography component="dt" color="text.secondary">{t('orgStructure.description')}</Typography>
            <Typography component="dd" sx={{ m: 0 }}>{node.description || '—'}</Typography>
            <Typography component="dt" color="text.secondary">{t('orgStructure.children', { count: node.childCount })}</Typography>
            <Typography component="dd" sx={{ m: 0 }}>{byId.size > 0 ? node.childCount : ''}</Typography>
          </Box>
          <Typography variant="subtitle1" component="h3" sx={{ mt: 3, mb: 1 }}>{t('orgStructure.members')}</Typography>
          {detail && detail.members.length === 0 && <Typography color="text.secondary">{t('orgStructure.noMembers')}</Typography>}
          <Box component="ul" sx={{ m: 0, p: 0, listStyle: 'none', display: 'flex', flexDirection: 'column', gap: 1 }}>
            {detail?.members.map((m) => (
              <Box component="li" key={m.memberId} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Typography>{nameOf(m)} <Typography component="span" variant="body2" color="text.secondary">{m.email}</Typography></Typography>
                <IconButton size="small" aria-label={`${t('orgStructure.remove')}: ${nameOf(m)}`}
                  onClick={() => void run(() => orgHierarchyApi.removeMember(node.id, m.memberId))}>
                  <Trash2 size={16} />
                </IconButton>
              </Box>
            ))}
          </Box>
          <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
            <FormControl size="small" sx={{ minWidth: 240 }}>
              <InputLabel id="place-member-label">{t('orgStructure.memberSelect')}</InputLabel>
              <Select labelId="place-member-label" label={t('orgStructure.memberSelect')} value={memberId}
                onChange={(e) => setMemberId(e.target.value as number)}>
                {candidates.map((m) => <MenuItem key={m.organizationMemberId} value={m.organizationMemberId}>{nameOf(m)}</MenuItem>)}
              </Select>
            </FormControl>
            <Button variant="outlined" disabled={memberId === ''}
              onClick={() => memberId !== '' && void run(() => orgHierarchyApi.placeMember(node.id, memberId))}>
              {t('orgStructure.place')}
            </Button>
          </Stack>
        </Box>
      )}
      {tab === 'history' && (
        <Box>
          {history && history.length === 0 && <Typography color="text.secondary">{t('orgStructure.noHistory')}</Typography>}
          <Box component="ul" sx={{ m: 0, p: 0, listStyle: 'none', display: 'flex', flexDirection: 'column', gap: 1 }}>
            {history?.map((h) => (
              <Box component="li" key={h.id}>
                <Typography>
                  {t('orgStructure.movedFromTo', { from: h.previousParentName ?? t('orgStructure.noParent'), to: h.newParentName ?? t('orgStructure.noParent') })}
                </Typography>
                <Typography variant="body2" color="text.secondary">{new Date(h.effectiveAt).toLocaleString()}</Typography>
              </Box>
            ))}
          </Box>
        </Box>
      )}
    </Box>
  )
}

function NodeFormDialog({ mode, node, parent, levels, rankOf, children, byId, onClose, onSaved }: {
  mode: 'create' | 'edit'
  node?: OrgNode
  parent?: OrgNode
  levels: OrgLevel[]
  rankOf: (type: string) => number
  children: Map<number | null, OrgNode[]>
  byId: Map<number, OrgNode>
  onClose: () => void
  onSaved: () => void
}) {
  const { t } = useTranslation()
  const isRoot = mode === 'edit' && node?.parentId === null
  const minRank = parent ? rankOf(parent.type) + 1 : 0
  const kids = node ? children.get(node.id) ?? [] : []
  const maxRank = kids.length ? Math.min(...kids.map((k) => rankOf(k.type))) - 1 : Number.MAX_SAFE_INTEGER
  const allowed = levels.filter((l) => l.rank >= minRank && l.rank <= maxRank)
  const [name, setName] = useState(node?.name ?? '')
  const [type, setType] = useState(node?.type ?? allowed[0]?.type ?? '')
  const [code, setCode] = useState(node?.code ?? '')
  const [description, setDescription] = useState(node?.description ?? '')
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  void byId

  const save = async () => {
    setBusy(true)
    setErr(null)
    try {
      const input = { name, type, code: code || undefined, description: description || undefined }
      if (mode === 'create' && parent) await orgHierarchyApi.create(parent.id, input)
      else if (node) await orgHierarchyApi.update(node.id, input)
      onSaved()
    } catch (e) {
      setErr(message(e))
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="sm" aria-labelledby="node-form-title">
      <DialogTitle id="node-form-title">
        {mode === 'create' ? t('orgStructure.nodeForm.createTitle', { parent: parent?.name }) : t('orgStructure.nodeForm.editTitle', { name: node?.name })}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {err && <Alert severity="error">{err}</Alert>}
          <TextField label={t('orgStructure.nodeForm.name')} value={name} onChange={(e) => setName(e.target.value)} required autoFocus />
          <FormControl disabled={isRoot}>
            <InputLabel id="node-type-label">{t('orgStructure.nodeForm.type')}</InputLabel>
            <Select labelId="node-type-label" label={t('orgStructure.nodeForm.type')} value={type} onChange={(e) => setType(e.target.value)}>
              {(isRoot ? levels.filter((l) => l.type === node?.type) : allowed).map((l) => <MenuItem key={l.type} value={l.type}>{l.label}</MenuItem>)}
            </Select>
          </FormControl>
          {isRoot && <Typography variant="body2" color="text.secondary">{t('orgStructure.nodeForm.rootTypeFixed')}</Typography>}
          <TextField label={t('orgStructure.nodeForm.code')} value={code} onChange={(e) => setCode(e.target.value)} />
          <TextField label={t('orgStructure.nodeForm.description')} value={description} onChange={(e) => setDescription(e.target.value)} multiline minRows={2} />
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('orgStructure.cancel')}</Button>
        <Button variant="contained" onClick={() => void save()} disabled={busy || !name.trim() || !type}>{t('orgStructure.nodeForm.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}

function MoveDialog({ node, nodes, byId, rankOf, labelOf, onClose, onSaved }: {
  node: OrgNode
  nodes: OrgNode[]
  byId: Map<number, OrgNode>
  rankOf: (type: string) => number
  labelOf: (type: string) => string
  onClose: () => void
  onSaved: () => void
}) {
  const { t } = useTranslation()
  const isUnder = (n: OrgNode) => {
    for (let cur: OrgNode | undefined = n; cur; cur = cur.parentId === null ? undefined : byId.get(cur.parentId)) {
      if (cur.id === node.id) return true
    }
    return false
  }
  const options = nodes.filter((n) => !isUnder(n) && n.id !== node.parentId && rankOf(n.type) < rankOf(node.type))
  const [target, setTarget] = useState<number | ''>('')
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const save = async () => {
    if (target === '') return
    setBusy(true)
    try { await orgHierarchyApi.move(node.id, target); onSaved() } catch (e) { setErr(message(e)); setBusy(false) }
  }
  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="sm" aria-labelledby="move-title">
      <DialogTitle id="move-title">{t('orgStructure.moveTitle', { name: node.name })}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {err && <Alert severity="error">{err}</Alert>}
          <FormControl>
            <InputLabel id="move-target-label">{t('orgStructure.moveTo')}</InputLabel>
            <Select labelId="move-target-label" label={t('orgStructure.moveTo')} value={target} onChange={(e) => setTarget(e.target.value as number)}>
              {options.map((n) => <MenuItem key={n.id} value={n.id}>{n.name} ({labelOf(n.type)})</MenuItem>)}
            </Select>
          </FormControl>
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('orgStructure.cancel')}</Button>
        <Button variant="contained" onClick={() => void save()} disabled={busy || target === ''}>{t('orgStructure.moveConfirm')}</Button>
      </DialogActions>
    </Dialog>
  )
}

function ActionConfirm({ title, body, confirmLabel, destructive, run, onClose, onDone }: {
  title: string; body: string; confirmLabel: string; destructive: boolean
  run: () => Promise<unknown>; onClose: () => void; onDone: () => void | Promise<void>
}) {
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  return (
    <>
      <ConfirmDialog open title={title} body={err ?? body} confirmLabel={confirmLabel} destructive={destructive} busy={busy}
        onClose={onClose}
        onConfirm={async () => {
          setBusy(true)
          try { await run(); await onDone() } catch (e) { setErr(message(e)); setBusy(false) }
        }} />
    </>
  )
}

function LevelsDialog({ levels, onClose, onSaved }: { levels: OrgLevel[]; onClose: () => void; onSaved: () => void }) {
  const { t } = useTranslation()
  const [rows, setRows] = useState(() => levels.map((l) => ({ type: l.type, label: l.label, isNew: false })))
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const update = (i: number, patch: Partial<(typeof rows)[number]>) => setRows((r) => r.map((x, j) => (j === i ? { ...x, ...patch } : x)))
  const swap = (i: number, j: number) => setRows((r) => {
    const next = [...r]
    ;[next[i], next[j]] = [next[j], next[i]]
    return next
  })
  const save = async () => {
    setBusy(true)
    try {
      await orgHierarchyApi.saveLevels(rows.map((r) => ({ type: r.type || r.label, label: r.label || undefined })))
      onSaved()
    } catch (e) { setErr(message(e)); setBusy(false) }
  }
  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="sm" aria-labelledby="levels-title">
      <DialogTitle id="levels-title">{t('orgStructure.levels.title')}</DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>{t('orgStructure.levels.help')}</Typography>
        {err && <Alert severity="error" sx={{ mb: 2 }}>{err}</Alert>}
        <Stack spacing={1.5}>
          {rows.map((r, i) => (
            <Stack key={r.isNew ? `new-${i}` : r.type} direction="row" spacing={1} sx={{ alignItems: 'center' }}>
              <TextField size="small" label={t('orgStructure.levels.type')} value={r.type} disabled={!r.isNew}
                onChange={(e) => update(i, { type: e.target.value })} sx={{ flex: 1 }} />
              <TextField size="small" label={t('orgStructure.levels.label')} value={r.label}
                onChange={(e) => update(i, { label: e.target.value })} sx={{ flex: 1 }} />
              <IconButton size="small" disabled={i < 2} aria-label={t('orgStructure.levels.up', { name: r.label || r.type })} onClick={() => swap(i, i - 1)}><ArrowUp size={16} /></IconButton>
              <IconButton size="small" disabled={i === 0 || i === rows.length - 1} aria-label={t('orgStructure.levels.down', { name: r.label || r.type })} onClick={() => swap(i, i + 1)}><ArrowDown size={16} /></IconButton>
              <IconButton size="small" disabled={i === 0} aria-label={t('orgStructure.levels.removeLevel', { name: r.label || r.type })}
                onClick={() => setRows((x) => x.filter((_, j) => j !== i))}><Trash2 size={16} /></IconButton>
            </Stack>
          ))}
        </Stack>
        <Button sx={{ mt: 2 }} onClick={() => setRows((r) => [...r, { type: '', label: '', isNew: true }])}>{t('orgStructure.levels.add')}</Button>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('orgStructure.cancel')}</Button>
        <Button variant="contained" onClick={() => void save()} disabled={busy}>{t('orgStructure.levels.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}

function ImportDialog({ onClose, onImported }: { onClose: () => void; onImported: () => void | Promise<void> }) {
  const { t } = useTranslation()
  const [file, setFile] = useState<File | null>(null)
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const [result, setResult] = useState<OrgImportResult | null>(null)
  const upload = async () => {
    if (!file) return
    setBusy(true)
    setErr(null)
    try {
      setResult(await orgHierarchyApi.importCsv(file))
      await onImported()
    } catch (e) { setErr(message(e)) } finally { setBusy(false) }
  }
  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="sm" aria-labelledby="import-title">
      <DialogTitle id="import-title">{t('orgStructure.import.title')}</DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>{t('orgStructure.import.help')}</Typography>
        {err && <Alert severity="error" sx={{ mb: 2 }}>{err}</Alert>}
        <Box component="label" sx={{ display: 'block' }}>
          <Typography component="span" variant="body2">{t('orgStructure.import.file')}</Typography>
          <Box component="input" type="file" accept=".csv,text/csv" sx={{ display: 'block', mt: 0.5 }}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => { setFile(e.target.files?.[0] ?? null); setResult(null) }} />
        </Box>
        {result && (
          <Box sx={{ mt: 2 }} role="status">
            <Typography>{t('orgStructure.import.result', { created: result.created, failed: result.failed })}</Typography>
            <Box component="ul" sx={{ m: 0, pl: 3 }}>
              {result.errors.map((x) => <li key={x.row}>{t('orgStructure.import.row', { row: x.row, message: x.message })}</li>)}
            </Box>
          </Box>
        )}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('orgStructure.close')}</Button>
        <Button variant="contained" onClick={() => void upload()} disabled={busy || !file}>{t('orgStructure.import.upload')}</Button>
      </DialogActions>
    </Dialog>
  )
}
