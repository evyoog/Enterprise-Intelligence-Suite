import { useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'

/** The selected tab lives in the URL (`?tab=`), so refresh, back and deep links keep it. */
export function useTabParam<T extends string>(tabs: readonly T[], fallback: T): [T, (tab: T) => void] {
  const [params, setParams] = useSearchParams()
  const raw = params.get('tab')
  const tab = (tabs as readonly string[]).includes(raw ?? '') ? (raw as T) : fallback
  const select = useCallback((next: T) => {
    setParams((prev) => {
      const p = new URLSearchParams(prev)
      if (next === fallback) p.delete('tab'); else p.set('tab', next)
      return p
    }, { replace: true })
  }, [fallback, setParams])
  return [tab, select]
}
