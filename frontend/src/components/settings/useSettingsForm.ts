import { useCallback, useEffect, useMemo, useState } from 'react'
import { ApiError } from '../../api/client'

/**
 * C60: load / edit / validate / save for one settings form. The form holds
 * strings and booleans only; `toForm` maps the API value in, `save` maps it
 * out. While there are unsaved edits, closing or reloading the tab asks
 * first (beforeunload).
 */
export function useSettingsForm<V extends { updatedAt?: string }, F extends Record<string, string | boolean>>(opts: {
  load: () => Promise<V>
  save: (form: F) => Promise<V>
  toForm: (value: V) => F
  validate: (form: F) => Partial<Record<keyof F, string>>
  loadErrorText: string
  saveErrorText: string
}) {
  const { load, save, toForm, validate, loadErrorText, saveErrorText } = opts
  const [saved, setSaved] = useState<V | null>(null)
  const [form, setForm] = useState<F | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [touched, setTouched] = useState(false)
  const [busy, setBusy] = useState(false)
  const [justSaved, setJustSaved] = useState(false)

  const fetchValue = useCallback(() => load()
    .then((v) => { setSaved(v); setForm(toForm(v)) })
    .catch((e) => setLoadError(e instanceof ApiError ? e.message : loadErrorText)),
  // eslint-disable-next-line react-hooks/exhaustive-deps -- load/toForm are stable per page
  [])
  // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
  useEffect(() => { void fetchValue() }, [])

  const retry = () => { setLoadError(null); void fetchValue() }
  const dirty = saved !== null && form !== null && JSON.stringify(form) !== JSON.stringify(toForm(saved))
  const errors = useMemo<Partial<Record<keyof F, string>>>(() => (form ? validate(form) : {}), [form, validate])
  const valid = Object.values(errors).every((e) => !e)

  useEffect(() => {
    if (!dirty) return
    const warn = (e: BeforeUnloadEvent) => { e.preventDefault() }
    window.addEventListener('beforeunload', warn)
    return () => window.removeEventListener('beforeunload', warn)
  }, [dirty])

  const set = <K extends keyof F>(key: K, value: F[K]) => setForm((f) => (f ? { ...f, [key]: value } : f))

  const submit = async () => {
    setTouched(true)
    if (!form || !valid) return false
    setBusy(true)
    setError(null)
    try {
      const v = await save(form)
      setSaved(v)
      setForm(toForm(v))
      setTouched(false)
      setJustSaved(true)
      return true
    } catch (e) {
      setError(e instanceof ApiError ? e.message : saveErrorText)
      return false
    } finally {
      setBusy(false)
    }
  }

  const reset = () => { if (saved) setForm(toForm(saved)); setTouched(false) }
  const shownError = (key: keyof F) => (touched ? errors[key] ?? null : null)

  return { saved, form, set, dirty, errors, shownError, valid, touched, busy, error, loadError, retry, submit, reset, justSaved, clearJustSaved: () => setJustSaved(false) }
}
