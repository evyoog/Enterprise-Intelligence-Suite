import {
  Boxes, Briefcase, Building2, Landmark, Layers, MapPin, Minus, MoreHorizontal, Network, Users,
  ZoomIn, ZoomOut, Maximize2, type LucideIcon,
} from 'lucide-react'
import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState, type PointerEvent as ReactPointerEvent, type RefObject } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Button, Chip, IconButton, Menu, MenuItem, Paper, Tooltip, Typography, useTheme } from '@mui/material'
import type { OrgNode } from '../../api/orgHierarchyApi'

/**
 * REQ-TEN-006 visual org chart (cloned from the Thittam org-hierarchy page): cards joined by
 * connector lines, an expand/collapse badge under each card, wheel-zoom, drag-to-pan, a zoom bar
 * and a minimap. Purely presentational: the tree data and every action come from the page.
 */
const TYPE_META: Record<string, { icon: LucideIcon; color: string }> = {
  ORGANIZATION: { icon: Building2, color: '#2563eb' },
  DIVISION: { icon: Boxes, color: '#7c3aed' },
  BUSINESS_UNIT: { icon: Briefcase, color: '#0e7da0' },
  DEPARTMENT: { icon: Layers, color: '#4f46e5' },
  LOCATION: { icon: MapPin, color: '#b7791f' },
  COST_CENTER: { icon: Landmark, color: '#0f766e' },
  TEAM: { icon: Users, color: '#64748b' },
}
const typeMeta = (type: string) => TYPE_META[type] ?? { icon: Building2, color: '#64748b' }

const OC_MIN = 0.3
const OC_MAX = 2
const OC_PAD = 48
const OC_FIT_MIN = 0.5

interface View { scale: number; x: number; y: number }
interface MiniBox { x: number; y: number; w: number; h: number; sel: boolean; match: boolean }

function usePanZoom(viewportRef: RefObject<HTMLDivElement | null>, contentRef: RefObject<HTMLDivElement | null>, fitKey: string, ready: boolean) {
  const [view, setView] = useState<View>({ scale: 1, x: 0, y: 0 })
  const viewRef = useRef(view)
  const pan = useRef<{ sx: number; sy: number; ox: number; oy: number } | null>(null)
  const [mini, setMini] = useState<MiniBox[]>([])
  const [contentSize, setContentSize] = useState({ w: 0, h: 0 })
  const [viewport, setViewport] = useState({ w: 0, h: 0 })
  useEffect(() => { viewRef.current = view }, [view])

  const dims = useCallback(() => {
    const vp = viewportRef.current
    const ct = contentRef.current
    if (!vp || !ct) return null
    const d = { vw: vp.clientWidth, vh: vp.clientHeight, cw: ct.offsetWidth, ch: ct.offsetHeight }
    return d.vw && d.vh && d.cw && d.ch ? d : null
  }, [contentRef, viewportRef])

  const recenter = useCallback(() => {
    setView((v) => {
      const d = dims()
      if (!d) return v
      const y = d.ch * v.scale <= d.vh - OC_PAD * 2 ? (d.vh - d.ch * v.scale) / 2 : Math.min(v.y, OC_PAD)
      return { ...v, x: (d.vw - d.cw * v.scale) / 2, y: Math.max(y, OC_PAD / 2) }
    })
  }, [dims])

  const fit = useCallback(() => {
    const d = dims()
    if (!d) return
    const scale = Math.max(OC_FIT_MIN, Math.min(1, (d.vw - OC_PAD * 2) / d.cw, (d.vh - OC_PAD * 2) / d.ch))
    const y = d.ch * scale <= d.vh - OC_PAD * 2 ? (d.vh - d.ch * scale) / 2 : OC_PAD
    setView({ scale, x: (d.vw - d.cw * scale) / 2, y })
  }, [dims])

  const inited = useRef(false)
  useLayoutEffect(() => {
    if (!ready) { inited.current = false; return }
    const id = requestAnimationFrame(() => {
      if (!inited.current) {
        inited.current = true
        const d = dims()
        if (d) setView({ scale: 1, x: (d.vw - d.cw) / 2, y: OC_PAD })
      } else recenter()
    })
    return () => cancelAnimationFrame(id)
  }, [fitKey, ready, dims, recenter])

  useEffect(() => {
    const vp = viewportRef.current
    if (!vp || typeof ResizeObserver === 'undefined') return
    let raf = 0
    const ro = new ResizeObserver(() => {
      cancelAnimationFrame(raf)
      raf = requestAnimationFrame(() => { setViewport({ w: vp.clientWidth, h: vp.clientHeight }); recenter() })
    })
    ro.observe(vp)
    return () => { ro.disconnect(); cancelAnimationFrame(raf) }
  }, [recenter, viewportRef])

  useEffect(() => {
    const raf = requestAnimationFrame(() => {
      const ct = contentRef.current
      if (!ready || !ct) { setMini([]); return }
      const s = viewRef.current.scale || 1
      const cr = ct.getBoundingClientRect()
      setMini([...ct.querySelectorAll<HTMLElement>('[data-oc-card]')].map((el) => {
        const r = el.getBoundingClientRect()
        return {
          x: (r.left - cr.left) / s, y: (r.top - cr.top) / s, w: r.width / s, h: r.height / s,
          sel: el.dataset.selected === 'true', match: el.dataset.match === 'true',
        }
      }))
      setContentSize({ w: ct.offsetWidth, h: ct.offsetHeight })
      const vp = viewportRef.current
      if (vp) setViewport({ w: vp.clientWidth, h: vp.clientHeight })
    })
    return () => cancelAnimationFrame(raf)
  }, [fitKey, ready, contentRef, viewportRef])

  const zoomTo = useCallback((next: number, cx?: number, cy?: number) => {
    setView((v) => {
      const s = Math.max(OC_MIN, Math.min(OC_MAX, next))
      const vp = viewportRef.current
      const px = cx ?? (vp ? vp.clientWidth / 2 : 0)
      const py = cy ?? (vp ? vp.clientHeight / 2 : 0)
      return { scale: s, x: px - (px - v.x) * (s / v.scale), y: py - (py - v.y) * (s / v.scale) }
    })
  }, [viewportRef])
  const zoomBy = useCallback((f: number) => zoomTo(viewRef.current.scale * f), [zoomTo])

  const panToContentPoint = useCallback((cx: number, cy: number) => {
    setView((v) => {
      const vp = viewportRef.current
      return vp ? { ...v, x: vp.clientWidth / 2 - cx * v.scale, y: vp.clientHeight / 2 - cy * v.scale } : v
    })
  }, [viewportRef])

  useEffect(() => {
    const vp = viewportRef.current
    if (!vp) return
    const handler = (e: WheelEvent) => {
      e.preventDefault()
      const rect = vp.getBoundingClientRect()
      zoomTo(viewRef.current.scale * (e.deltaY < 0 ? 1.1 : 0.9), e.clientX - rect.left, e.clientY - rect.top)
    }
    vp.addEventListener('wheel', handler, { passive: false })
    return () => vp.removeEventListener('wheel', handler)
  }, [zoomTo, viewportRef])

  const onPointerDown = useCallback((e: ReactPointerEvent<HTMLDivElement>) => {
    if (e.button !== 0 || (e.target as HTMLElement).closest('[data-oc-card], button, input, a, [data-no-pan]')) return
    pan.current = { sx: e.clientX, sy: e.clientY, ox: viewRef.current.x, oy: viewRef.current.y }
    e.currentTarget.setPointerCapture?.(e.pointerId)
    e.currentTarget.style.cursor = 'grabbing'
  }, [])
  const onPointerMove = useCallback((e: ReactPointerEvent<HTMLDivElement>) => {
    if (!pan.current) return
    const { sx, sy, ox, oy } = pan.current
    setView((v) => ({ ...v, x: ox + (e.clientX - sx), y: oy + (e.clientY - sy) }))
  }, [])
  const onPointerUp = useCallback((e: ReactPointerEvent<HTMLDivElement>) => {
    pan.current = null
    e.currentTarget.style.cursor = ''
  }, [])

  return { view, fit, zoomBy, onPointerDown, onPointerMove, onPointerUp, mini, contentSize, viewport, panToContentPoint }
}

export interface OrgChartActions {
  onSelect: (node: OrgNode) => void
  onAddChild: (node: OrgNode) => void
  onEdit: (node: OrgNode) => void
  onMove: (node: OrgNode) => void
  onToggleActive: (node: OrgNode) => void
  onDelete: (node: OrgNode) => void
}

/** Read-only charts (platform administrators, REQ-TEN-007) only select; they have no node menu. */
type ChartActions = Pick<OrgChartActions, 'onSelect'> & Partial<OrgChartActions>

interface ChartProps extends ChartActions {
  roots: OrgNode[]
  byParent: Map<number | null, OrgNode[]>
  descendants: Map<number, number>
  expanded: Set<number>
  onToggle: (id: number) => void
  selectedId: number | null
  matchIds: Set<number> | null
  labelOf: (type: string) => string
  fitKey: string
}

function NodeMenu({ node, actions }: { node: OrgNode; actions: OrgChartActions }) {
  const { t } = useTranslation()
  const [anchor, setAnchor] = useState<HTMLElement | null>(null)
  const isRoot = node.parentId === null
  const close = () => setAnchor(null)
  const run = (fn: (n: OrgNode) => void) => () => { close(); fn(node) }
  return (
    <>
      <IconButton size="small" aria-label={t('orgStructure.actionsFor', { name: node.name })} aria-haspopup="menu"
        onClick={(e) => setAnchor(e.currentTarget)}>
        <MoreHorizontal size={16} />
      </IconButton>
      <Menu anchorEl={anchor} open={!!anchor} onClose={close}>
        <MenuItem onClick={run(actions.onAddChild)}>{t('orgStructure.addChild')}</MenuItem>
        <MenuItem onClick={run(actions.onEdit)}>{t('orgStructure.edit')}</MenuItem>
        {!isRoot && <MenuItem onClick={run(actions.onMove)}>{t('orgStructure.move')}</MenuItem>}
        {!isRoot && <MenuItem onClick={run(actions.onToggleActive)}>{node.active ? t('orgStructure.deactivate') : t('orgStructure.activate')}</MenuItem>}
        {!isRoot && <MenuItem onClick={run(actions.onDelete)} sx={{ color: 'error.main' }}>{t('orgStructure.delete')}</MenuItem>}
      </Menu>
    </>
  )
}

function isEditable(a: ChartActions): a is OrgChartActions {
  return !!(a.onAddChild && a.onEdit && a.onMove && a.onToggleActive && a.onDelete)
}

function OrgCard({ node, isRoot, total, selected, dimmed, matched, labelOf, actions }: {
  node: OrgNode; isRoot: boolean; total: number; selected: boolean; dimmed: boolean; matched: boolean
  labelOf: (type: string) => string; actions: ChartActions
}) {
  const { t } = useTranslation()
  const meta = typeMeta(node.type)
  const Icon = meta.icon
  return (
    <Paper variant="outlined" component="article" aria-label={node.name} data-oc-card data-selected={selected} data-match={matched}
      sx={(th) => ({
        width: 240, borderRadius: 3, opacity: dimmed ? 0.34 : node.active ? 1 : 0.6,
        borderColor: selected || matched ? th.palette.primary.main : th.palette.divider,
        boxShadow: selected ? `0 0 0 3px ${th.palette.primary.main}33` : 'none',
      })}>
      <Box sx={{ p: 1.5 }}>
        <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.25 }}>
          <Box sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            background: `${meta.color}1f`, color: meta.color }}>
            <Icon size={18} aria-hidden />
          </Box>
          <Box sx={{ minWidth: 0, flex: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
              <Typography noWrap sx={{ fontWeight: 600, fontSize: 13.5 }}>{node.name}</Typography>
              {isRoot && <Chip size="small" label={t('orgStructure.root')} sx={{ height: 18, fontSize: 10 }} />}
            </Box>
            <Typography noWrap variant="caption" color="text.secondary">
              {labelOf(node.type)}{node.code ? ` · ${node.code}` : ''}
            </Typography>
          </Box>
          {isEditable(actions) && <NodeMenu node={node} actions={actions} />}
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 1.25 }}>
          <Chip size="small" label={labelOf(node.type)} sx={{ background: `${meta.color}1f`, color: meta.color, fontWeight: 600, height: 22 }} />
          {total > 0
            ? <Typography variant="caption" color="text.secondary" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5 }}>
              <Network size={14} aria-hidden />{t('orgStructure.total', { count: total })}
            </Typography>
            : !node.active && <Typography variant="caption" color="error">{t('orgStructure.inactive')}</Typography>}
        </Box>
      </Box>
      <Button fullWidth size="small" onClick={() => actions.onSelect(node)} aria-label={`${t('orgStructure.detailsButton')}: ${node.name}`}
        sx={{ borderTop: 1, borderColor: 'divider', borderRadius: '0 0 12px 12px', py: 0.75, textTransform: 'none' }}>
        {t('orgStructure.detailsButton')}
      </Button>
    </Paper>
  )
}

function Subtree({ node, p }: { node: OrgNode; p: ChartProps }) {
  const { t } = useTranslation()
  const kids = p.byParent.get(node.id) ?? []
  const open = p.expanded.has(node.id)
  return (
    <Box className="oc-subtree">
      <Box className="oc-node">
        <OrgCard node={node} isRoot={node.parentId === null} total={p.descendants.get(node.id) ?? 0}
          selected={p.selectedId === node.id} matched={!!p.matchIds?.has(node.id)} dimmed={!!p.matchIds && !p.matchIds.has(node.id)}
          labelOf={p.labelOf} actions={p} />
        {kids.length > 0 && (
          <button type="button" className={open ? 'oc-toggle oc-toggle-open' : 'oc-toggle'} onClick={() => p.onToggle(node.id)}
            aria-expanded={open}
            aria-label={open ? t('orgStructure.collapseNode', { name: node.name }) : t('orgStructure.expandNode', { name: node.name })}>
            {open ? <Minus size={14} aria-hidden /> : kids.length}
          </button>
        )}
      </Box>
      {kids.length > 0 && open && (
        <>
          <Box className="oc-trunk" />
          <Box className="oc-children">
            {kids.map((child, i) => {
              const pos = kids.length === 1 ? 'oc-only' : i === 0 ? 'oc-first' : i === kids.length - 1 ? 'oc-last' : 'oc-mid'
              return <Box key={child.id} className={`oc-child ${pos}`}><Subtree node={child} p={p} /></Box>
            })}
          </Box>
        </>
      )}
    </Box>
  )
}

function MiniMap({ mini, contentSize, view, viewport, onJump }: {
  mini: MiniBox[]; contentSize: { w: number; h: number }; view: View; viewport: { w: number; h: number }
  onJump: (x: number, y: number) => void
}) {
  const { t } = useTranslation()
  const BOX_W = 200, BOX_H = 120, PAD = 8
  if (!contentSize.w || !contentSize.h || mini.length === 0) return null
  const s = Math.min((BOX_W - PAD * 2) / contentSize.w, (BOX_H - PAD * 2) / contentSize.h)
  return (
    <Box data-no-pan role="img" aria-label={t('orgStructure.minimap')} title={t('orgStructure.minimap')}
      onClick={(e) => {
        const r = e.currentTarget.getBoundingClientRect()
        onJump((e.clientX - r.left - PAD) / s, (e.clientY - r.top - PAD) / s)
      }}
      sx={{ position: 'absolute', right: 16, bottom: 16, width: BOX_W, height: BOX_H, borderRadius: 2, overflow: 'hidden', cursor: 'pointer',
        bgcolor: 'background.paper', border: 1, borderColor: 'divider', boxShadow: 3, zIndex: 3 }}>
      <Box sx={{ position: 'absolute', inset: 0, p: `${PAD}px` }}>
        <Box sx={{ position: 'relative', width: '100%', height: '100%' }}>
          {mini.map((n, i) => (
            <Box key={i} sx={{ position: 'absolute', left: n.x * s, top: n.y * s, width: Math.max(3, n.w * s), height: Math.max(2, n.h * s),
              borderRadius: '2px', bgcolor: n.sel || n.match ? 'primary.main' : 'text.disabled', opacity: n.sel || n.match ? 1 : 0.7 }} />
          ))}
        </Box>
      </Box>
      <Box sx={{ position: 'absolute', pointerEvents: 'none', borderRadius: '3px', border: '1.5px solid', borderColor: 'primary.main',
        bgcolor: 'action.hover', left: Math.max(0, PAD + (-view.x / view.scale) * s), top: Math.max(0, PAD + (-view.y / view.scale) * s),
        width: Math.min(BOX_W, (viewport.w / view.scale) * s), height: Math.min(BOX_H, (viewport.h / view.scale) * s) }} />
    </Box>
  )
}

export function OrgChart(p: ChartProps) {
  const { t } = useTranslation()
  const theme = useTheme()
  const viewportRef = useRef<HTMLDivElement>(null)
  const contentRef = useRef<HTMLDivElement>(null)
  const z = usePanZoom(viewportRef, contentRef, p.fitKey, p.roots.length > 0)
  const line = theme.palette.mode === 'dark' ? '#475569' : '#b6c0cf'
  const styles = useMemo(() => ({
    '& .oc-subtree': { display: 'inline-flex', flexDirection: 'column', alignItems: 'center' },
    '& .oc-node': { position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'center' },
    '& .oc-trunk': { width: 2, height: 26, background: line, flex: '0 0 auto' },
    '& .oc-children': { display: 'flex', justifyContent: 'center', alignItems: 'flex-start' },
    '& .oc-child': { position: 'relative', padding: '26px 16px 0' },
    '& .oc-child::before': { content: '""', position: 'absolute', top: 0, left: '50%', width: 2, height: 26, background: line, transform: 'translateX(-50%)' },
    '& .oc-child::after': { content: '""', position: 'absolute', top: 0, height: 2, background: line },
    '& .oc-first::after': { left: '50%', right: 0 },
    '& .oc-last::after': { left: 0, right: '50%' },
    '& .oc-mid::after': { left: 0, right: 0 },
    '& .oc-only::after': { display: 'none' },
    '& .oc-toggle': {
      position: 'absolute', bottom: -13, left: '50%', transform: 'translateX(-50%)', zIndex: 2, display: 'inline-flex',
      alignItems: 'center', justifyContent: 'center', minWidth: 26, height: 26, padding: '0 6px', borderRadius: 999, fontSize: 11,
      fontWeight: 700, lineHeight: 1, cursor: 'pointer', background: theme.palette.primary.main, color: theme.palette.primary.contrastText,
      border: `2px solid ${theme.palette.background.paper}`,
    },
    '& .oc-toggle-open': { background: theme.palette.action.selected, color: theme.palette.text.secondary },
  }), [line, theme])

  return (
    <Box ref={viewportRef} onPointerDown={z.onPointerDown} onPointerMove={z.onPointerMove} onPointerUp={z.onPointerUp}
      role="region" aria-label={t('orgStructure.treeLabel')}
      sx={{ position: 'relative', height: { xs: 480, md: 640 }, overflow: 'hidden', touchAction: 'none', borderRadius: 2,
        bgcolor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.02)' : '#f4f6f9',
        backgroundImage: `radial-gradient(${theme.palette.mode === 'dark' ? '#334155' : '#cfd6e1'} 1.5px, transparent 1.5px)`, backgroundSize: '22px 22px' }}>
      <Box ref={contentRef} sx={{ position: 'absolute', top: 0, left: 0, transformOrigin: '0 0',
        transform: `translate(${z.view.x}px, ${z.view.y}px) scale(${z.view.scale})`, ...styles }}>
        <Box sx={{ display: 'flex', gap: 6 }}>
          {p.roots.map((r) => <Subtree key={r.id} node={r} p={p} />)}
        </Box>
      </Box>
      <Box data-no-pan sx={{ position: 'absolute', left: 12, top: 12, zIndex: 3, display: 'flex', alignItems: 'center', gap: 0.5,
        bgcolor: 'background.paper', border: 1, borderColor: 'divider', borderRadius: 2, p: 0.25 }}>
        <Tooltip title={t('orgStructure.zoomOut')}><IconButton size="small" aria-label={t('orgStructure.zoomOut')} onClick={() => z.zoomBy(0.8)}><ZoomOut size={16} /></IconButton></Tooltip>
        <Typography variant="caption" sx={{ minWidth: 40, textAlign: 'center' }} aria-live="polite">{Math.round(z.view.scale * 100)}%</Typography>
        <Tooltip title={t('orgStructure.zoomIn')}><IconButton size="small" aria-label={t('orgStructure.zoomIn')} onClick={() => z.zoomBy(1.25)}><ZoomIn size={16} /></IconButton></Tooltip>
        <Tooltip title={t('orgStructure.fit')}><IconButton size="small" aria-label={t('orgStructure.fit')} onClick={z.fit}><Maximize2 size={16} /></IconButton></Tooltip>
      </Box>
      <MiniMap mini={z.mini} contentSize={z.contentSize} view={z.view} viewport={z.viewport} onJump={z.panToContentPoint} />
    </Box>
  )
}

