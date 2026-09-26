import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, Paper, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { oidcApi, type OidcProvider, type OidcProviderPayload, type OidcProviderTestResult } from '../../api/oidcApi'

const EMPTY: OidcProviderPayload = { name: '', issuerUrl: '', clientId: '', clientSecret: '', scopes: 'openid email profile' }

/**
 * REQ-IAM-006 (C22/C27): the organization's OpenID Connect providers, on the
 * identity federation page next to SAML. The client secret is write-only.
 * Enabling a provider disables any other enabled SAML or OIDC provider
 * (backend rule), so `onChanged` lets the page refresh its SAML list.
 */
export function OidcProvidersSection({ refreshKey = 0, onChanged }: { refreshKey?: number; onChanged?: () => void }) {
  const { t } = useTranslation()
  const [providers, setProviders] = useState<OidcProvider[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState<OidcProviderPayload | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [tests, setTests] = useState<Record<number, OidcProviderTestResult>>({})
  const [busyId, setBusyId] = useState<number | null>(null)

  const load = useCallback(() => {
    oidcApi.list()
      .then(setProviders)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('oidc.loadError')))
  }, [t])

  useEffect(() => { load() }, [load, refreshKey])

  const run = async (id: number, action: () => Promise<unknown>, changesOthers = false) => {
    setBusyId(id)
    setError(null)
    try {
      await action()
      load()
      if (changesOthers) onChanged?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('oidc.actionError'))
    } finally {
      setBusyId(null)
    }
  }

  const test = (id: number) => run(id, async () => {
    const result = await oidcApi.test(id)
    setTests((prev) => ({ ...prev, [id]: result }))
  })

  const openNew = () => { setEditingId(null); setForm({ ...EMPTY }); setFormError(null) }
  const openEdit = (p: OidcProvider) => {
    setEditingId(p.id)
    setForm({ name: p.name, issuerUrl: p.issuerUrl, clientId: p.clientId, clientSecret: '', scopes: p.scopes })
    setFormError(null)
  }

  const save = async () => {
    if (!form) return
    setSaving(true)
    setFormError(null)
    try {
      if (editingId !== null) await oidcApi.update(editingId, form)
      else await oidcApi.create(form)
      setForm(null)
      setEditingId(null)
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('oidc.saveError'))
    } finally {
      setSaving(false)
    }
  }

  const set = (key: keyof OidcProviderPayload) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => (f ? { ...f, [key]: e.target.value } : f))
  const canSave = form && form.name.trim() && form.issuerUrl.trim() && form.clientId.trim()
    && (editingId !== null || form.clientSecret?.trim())

  return (
    <Box component="section" aria-labelledby="oidc-title" sx={{ mt: 5 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, gap: 1, flexWrap: 'wrap' }}>
        <Typography id="oidc-title" variant="h6" component="h2" sx={{ fontWeight: 700 }}>{t('oidc.title')}</Typography>
        {!form && <Button variant="outlined" size="small" onClick={openNew}>{t('oidc.add')}</Button>}
      </Box>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('oidc.subtitle')}</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {providers?.length === 0 && !form && <Typography sx={{ color: 'text.secondary' }}>{t('oidc.none')}</Typography>}

      {providers?.map((p) => (
        <Paper key={p.id} variant="outlined" sx={{ p: 2, mb: 1.5 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
            <Box sx={{ minWidth: 0 }}>
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                <Typography sx={{ fontWeight: 700 }}>{p.name}</Typography>
                <Chip size="small" label={p.enabled ? t('oidc.enabled') : t('oidc.disabled')} color={p.enabled ? 'success' : 'default'} />
              </Box>
              <Typography variant="body2" sx={{ color: 'text.secondary', wordBreak: 'break-all' }}>{p.issuerUrl}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('oidc.clientIdValue', { id: p.clientId })}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary', wordBreak: 'break-all' }}>{t('oidc.redirectUriValue', { uri: p.redirectUri })}</Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'flex-start' }}>
              <Button size="small" variant={p.enabled ? 'outlined' : 'contained'} disabled={busyId === p.id}
                aria-label={t(p.enabled ? 'oidc.disableLabel' : 'oidc.enableLabel', { name: p.name })}
                onClick={() => run(p.id, () => (p.enabled ? oidcApi.disable(p.id) : oidcApi.enable(p.id)), true)}>
                {p.enabled ? t('oidc.disable') : t('oidc.enable')}
              </Button>
              <Button size="small" disabled={busyId === p.id} aria-label={t('oidc.testLabel', { name: p.name })} onClick={() => test(p.id)}>
                {t('oidc.test')}
              </Button>
              <Button size="small" disabled={busyId === p.id} aria-label={t('oidc.editLabel', { name: p.name })} onClick={() => openEdit(p)}>
                {t('oidc.edit')}
              </Button>
              <Button size="small" color="error" disabled={busyId === p.id} aria-label={t('oidc.deleteLabel', { name: p.name })}
                onClick={() => run(p.id, () => oidcApi.remove(p.id))}>
                {t('oidc.delete')}
              </Button>
            </Box>
          </Box>
          {tests[p.id] && (
            <Alert severity={tests[p.id].success ? 'success' : 'error'} sx={{ mt: 1.5 }}>
              {[...tests[p.id].checks, ...tests[p.id].errors].map((line) => <div key={line}>{line}</div>)}
            </Alert>
          )}
        </Paper>
      ))}

      {form && (
        <Paper variant="outlined" sx={{ p: 2.5, mt: 2 }} component="form" aria-label={editingId !== null ? t('oidc.editTitle') : t('oidc.addTitle')}
          onSubmit={(e: React.FormEvent) => { e.preventDefault(); save() }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1.5 }}>{editingId !== null ? t('oidc.editTitle') : t('oidc.addTitle')}</Typography>
          {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
            <TextField size="small" label={t('oidc.name')} required value={form.name} onChange={set('name')} />
            <TextField size="small" label={t('oidc.issuerUrl')} required value={form.issuerUrl} onChange={set('issuerUrl')}
              helperText={t('oidc.issuerHint')} />
            <TextField size="small" label={t('oidc.clientId')} required value={form.clientId} onChange={set('clientId')} />
            <TextField size="small" type="password" autoComplete="new-password" label={t('oidc.clientSecret')}
              required={editingId === null} value={form.clientSecret} onChange={set('clientSecret')}
              helperText={editingId !== null ? t('oidc.secretKeepHint') : t('oidc.secretHint')} />
            <TextField size="small" label={t('oidc.scopes')} value={form.scopes} onChange={set('scopes')} helperText={t('oidc.scopesHint')}
              sx={{ gridColumn: '1 / -1' }} />
          </Box>
          <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
            <Button type="submit" variant="contained" disabled={saving || !canSave}>{saving ? t('oidc.saving') : t('oidc.save')}</Button>
            <Button onClick={() => { setForm(null); setEditingId(null) }}>{t('oidc.cancel')}</Button>
          </Box>
        </Paper>
      )}
    </Box>
  )
}
