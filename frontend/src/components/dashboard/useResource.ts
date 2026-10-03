import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../../api/client'

export interface Resource<T> {
  data: T | null
  error: string | null
  loading: boolean
  reload: () => void
}

/**
 * C69: one independently loaded dashboard source. A failure stays inside its
 * own section (with Retry) instead of breaking the whole dashboard.
 */
export function useResource<T>(fetcher: () => Promise<T>, fallbackError: string, enabled = true): Resource<T> {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [token, setToken] = useState(0)
  // Loading is derived: the latest request (token) has not settled yet.
  const [settledToken, setSettledToken] = useState(-1)

  useEffect(() => {
    if (!enabled) return
    let cancelled = false
    fetcher()
      .then((result) => { if (!cancelled) { setData(result); setError(null) } })
      .catch((e) => { if (!cancelled) setError(e instanceof ApiError ? e.message : fallbackError) })
      .finally(() => { if (!cancelled) setSettledToken(token) })
    return () => { cancelled = true }
    // The fetcher is a stable API call; `token` drives reloads.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, enabled])

  const reload = useCallback(() => setToken((n) => n + 1), [])
  return { data, error, loading: enabled && settledToken !== token, reload }
}
