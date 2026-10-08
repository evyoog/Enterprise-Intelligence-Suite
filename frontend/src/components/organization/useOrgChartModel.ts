import { useCallback, useMemo, useState } from 'react'
import type { OrgLevel, OrgNode } from '../../api/orgHierarchyApi'

/** Derived tree data and view state for the org chart (shared by read-only viewers). */
export function useOrgChartModel(nodes: OrgNode[], levels: OrgLevel[]) {
  const [expanded, setExpanded] = useState<Set<number>>(new Set())
  const [seededFor, setSeededFor] = useState<OrgNode[] | null>(null)
  const [query, setQuery] = useState('')
  const [typeFilter, setTypeFilter] = useState<Set<string>>(new Set())
  const [activeOnly, setActiveOnly] = useState(false)

  const byId = useMemo(() => new Map(nodes.map((n) => [n.id, n])), [nodes])
  const allChildren = useMemo(() => {
    const map = new Map<number | null, OrgNode[]>()
    nodes.forEach((n) => map.set(n.parentId, [...(map.get(n.parentId) ?? []), n]))
    return map
  }, [nodes])
  const byParent = useMemo(() => {
    const map = new Map<number | null, OrgNode[]>()
    nodes.filter((n) => !activeOnly || n.active).forEach((n) => map.set(n.parentId, [...(map.get(n.parentId) ?? []), n]))
    return map
  }, [nodes, activeOnly])
  const descendants = useMemo(() => {
    const out = new Map<number, number>()
    const count = (id: number): number => {
      const total = (byParent.get(id) ?? []).reduce((sum, c) => sum + 1 + count(c.id), 0)
      out.set(id, total)
      return total
    }
    ;(byParent.get(null) ?? []).forEach((r) => count(r.id))
    return out
  }, [byParent])
  const labelOf = useCallback((type: string) => levels.find((l) => l.type === type)?.label ?? type, [levels])

  // Progressive disclosure: the root and its children are open, deeper levels collapsed (re-seeded when new data arrives).
  if (seededFor !== nodes && nodes.length > 0) {
    const depth = new Map<number, number>()
    const walk = (id: number | null, d: number) => (allChildren.get(id) ?? []).forEach((c) => { depth.set(c.id, d); walk(c.id, d + 1) })
    walk(null, 0)
    setSeededFor(nodes)
    setExpanded(new Set(nodes.filter((n) => (depth.get(n.id) ?? 0) < 2).map((n) => n.id)))
  }

  const term = query.trim().toLowerCase()
  const matchIds = useMemo(() => {
    if (!term && typeFilter.size === 0) return null
    const set = new Set<number>()
    nodes.forEach((n) => {
      const termOk = !term || `${n.name} ${n.code ?? ''} ${labelOf(n.type)}`.toLowerCase().includes(term)
      if (termOk && (typeFilter.size === 0 || typeFilter.has(n.type))) set.add(n.id)
    })
    return set
  }, [term, typeFilter, nodes, labelOf])
  const effectiveExpanded = useMemo(() => {
    if (!matchIds) return expanded
    const set = new Set(expanded)
    matchIds.forEach((id) => {
      for (let cur = byId.get(id); cur && cur.parentId !== null; cur = byId.get(cur.parentId)) set.add(cur.parentId)
    })
    return set
  }, [matchIds, expanded, byId])

  const toggle = (id: number) => setExpanded((cur) => {
    const next = new Set(cur)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    return next
  })
  const toggleType = (type: string) => setTypeFilter((cur) => {
    const next = new Set(cur)
    if (next.has(type)) next.delete(type)
    else next.add(type)
    return next
  })
  const expandAll = () => setExpanded(new Set(nodes.filter((n) => (allChildren.get(n.id) ?? []).length).map((n) => n.id)))
  const collapseAll = () => setExpanded(new Set())

  return {
    byId, byParent, allChildren, descendants, roots: byParent.get(null) ?? [], labelOf, query, setQuery, typeFilter, toggleType,
    activeOnly, setActiveOnly, matchIds, effectiveExpanded, toggle, expandAll, collapseAll,
  }
}
