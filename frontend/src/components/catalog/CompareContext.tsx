import { createContext, useContext, useState, useSyncExternalStore, type ReactNode } from 'react'
import { MAX_COMPARE_ITEMS } from '../../api/compareApi'

/** C102 (REQ-MKT-005): the products ticked "Compare" on the catalog cards; at most 4, kept for the browser tab in sessionStorage. */
export const COMPARE_STORAGE_KEY = 'eis.compare.items'

export interface CompareState {
  /** The chosen product ids, in the order they were chosen. */
  ids: readonly number[]
  max: number
  full: boolean
  has: (id: number) => boolean
  /** Adds the product, or removes it when it is already chosen. Adding at the limit does nothing. */
  toggle: (id: number) => void
  remove: (id: number) => void
  clear: () => void
}

interface Store {
  getSnapshot: () => readonly number[]
  subscribe: (listener: () => void) => () => void
  set: (ids: readonly number[]) => void
}

function read(): readonly number[] {
  try {
    const parsed: unknown = JSON.parse(sessionStorage.getItem(COMPARE_STORAGE_KEY) ?? '[]')
    if (!Array.isArray(parsed)) return []
    const ids = parsed.filter((v): v is number => Number.isInteger(v) && v > 0)
    return [...new Set(ids)].slice(0, MAX_COMPARE_ITEMS)
  } catch {
    return []
  }
}

function createStore(): Store {
  let ids = read()
  const listeners = new Set<() => void>()
  return {
    getSnapshot: () => ids,
    subscribe: (listener) => { listeners.add(listener); return () => { listeners.delete(listener) } },
    set: (next) => {
      ids = next
      try { sessionStorage.setItem(COMPARE_STORAGE_KEY, JSON.stringify(next)) } catch { /* the choice is just not remembered */ }
      listeners.forEach((l) => l())
    },
  }
}

const CompareContext = createContext<Store | null>(null)
/** Without a provider the same store is shared by every consumer on the page. */
let standalone: Store | null = null

export function CompareProvider({ children }: { children: ReactNode }) {
  const [store] = useState(createStore)
  return <CompareContext.Provider value={store}>{children}</CompareContext.Provider>
}

/** Test hook: forgets the shared standalone store so the next consumer re-reads sessionStorage. */
export function resetStandaloneCompareStore() {
  standalone = null
}

/** The compare selection. Works without a {@link CompareProvider} (a store shared by all consumers of the page). */
// eslint-disable-next-line react-refresh/only-export-components
export function useCompare(): CompareState {
  const provided = useContext(CompareContext)
  const store = provided ?? (standalone ??= createStore())
  const ids = useSyncExternalStore(store.subscribe, store.getSnapshot, store.getSnapshot)
  return {
    ids,
    max: MAX_COMPARE_ITEMS,
    full: ids.length >= MAX_COMPARE_ITEMS,
    has: (id) => ids.includes(id),
    toggle: (id) => {
      if (ids.includes(id)) store.set(ids.filter((i) => i !== id))
      else if (ids.length < MAX_COMPARE_ITEMS) store.set([...ids, id])
    },
    remove: (id) => store.set(ids.filter((i) => i !== id)),
    clear: () => store.set([]),
  }
}
