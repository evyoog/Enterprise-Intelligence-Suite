import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

/**
 * Asks before unsaved edits are lost. A reload or closing the tab uses the browser's own prompt
 * (beforeunload). A click on an in-app link (sidebar, breadcrumbs, menu links) is held back and
 * `pendingPath` is set so the page can show its own "Leave without saving?" dialog; `leave()`
 * continues to that link, `stay()` cancels. The app uses a plain BrowserRouter (no data router),
 * so useBlocker is not available; the browser's Back button is therefore not intercepted.
 */
export function useUnsavedChangesGuard(dirty: boolean) {
  const navigate = useNavigate()
  const [pendingPath, setPendingPath] = useState<string | null>(null)
  const dirtyRef = useRef(dirty)
  useEffect(() => { dirtyRef.current = dirty }, [dirty])

  useEffect(() => {
    if (!dirty) return
    const warn = (e: BeforeUnloadEvent) => { e.preventDefault() }
    window.addEventListener('beforeunload', warn)
    return () => window.removeEventListener('beforeunload', warn)
  }, [dirty])

  useEffect(() => {
    const onClick = (e: MouseEvent) => {
      if (!dirtyRef.current || e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return
      const anchor = (e.target as Element | null)?.closest?.('a[href]') as HTMLAnchorElement | null
      if (!anchor || (anchor.target && anchor.target !== '_self') || anchor.hasAttribute('download')) return
      const url = new URL(anchor.href, window.location.href)
      if (url.origin !== window.location.origin) return
      const next = `${url.pathname}${url.search}${url.hash}`
      if (next === `${window.location.pathname}${window.location.search}${window.location.hash}`) return
      e.preventDefault()
      e.stopPropagation()
      setPendingPath(next)
    }
    document.addEventListener('click', onClick, true)
    return () => document.removeEventListener('click', onClick, true)
  }, [])

  const stay = useCallback(() => setPendingPath(null), [])
  const leave = useCallback(() => {
    const target = pendingPath
    dirtyRef.current = false
    setPendingPath(null)
    if (target) navigate(target)
  }, [pendingPath, navigate])

  return { pendingPath, stay, leave }
}
