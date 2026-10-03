import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, IconButton, Paper,
  Skeleton, Snackbar, Table, TableBody, TableCell, TableHead, TableRow, TextField, Tooltip,
} from '@mui/material'
import { Copy, KeyRound, Plus } from 'lucide-react'
import { ApiError } from '../../api/client'
import { API_KEY_STATUS_COLOR, apiKeysApi, type ApiKey } from '../../api/apiKeysApi'
import { SettingsSection } from '../settings/SettingsSection'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'


function tomorrow() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().slice(0, 10)
}

/**
 * REQ-INT-001.1 (C61): the signed-in user's API keys, on Account → Security.
 * The full key is shown once, right after it is created (BR-1).
 */
export function ApiKeysSection() {
  const { t } = useTranslation()
  const { formatDate, formatDateTime } = useLocalePreference()
  const [keys, setKeys] = useState<ApiKey[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [createOpen, setCreateOpen] = useState(false)
  const [name, setName] = useState('')
  const [expires, setExpires] = useState('')
  const [createError, setCreateError] = useState<string | null>(null)
  const [created, setCreated] = useState<ApiKey | null>(null)
  const [revoking, setRevoking] = useState<ApiKey | null>(null)
  const [busy, setBusy] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  const load = useCallback(() => apiKeysApi.list()
    .then((k) => { setKeys(k); setError(null) })
    .catch((e) => setError(e instanceof ApiError ? e.message : t('apiKeys.loadError'))), [t])
  useEffect(() => { void load() }, [load])

  const openCreate = () => { setName(''); setExpires(''); setCreateError(null); setCreated(null); setCreateOpen(true) }
  const closeCreate = () => { setCreateOpen(false); setCreated(null) }

  const create = () => {
    if (!name.trim()) { setCreateError(t('apiKeys.nameRequired')); return }
    setBusy(true)
    setCreateError(null)
    apiKeysApi.create({ name: name.trim(), expiresAt: expires ? new Date(`${expires}T23:59:59`).toISOString() : null })
      .then((k) => { setCreated(k); void load() })
      .catch((e) => setCreateError(e instanceof ApiError ? e.message : t('apiKeys.createError')))
      .finally(() => setBusy(false))
  }

  const revoke = () => {
    if (!revoking) return
    setBusy(true)
    apiKeysApi.revoke(revoking.id)
      .then(() => { setToast(t('apiKeys.revoked')); setRevoking(null); void load() })
      .catch((e) => setToast(e instanceof ApiError ? e.message : t('apiKeys.revokeError')))
      .finally(() => setBusy(false))
  }

  return (
    <Box sx={{ mt: 3 }}>
      <SettingsSection id="api-keys" icon={KeyRound} accent="violet" title={t('apiKeys.title')} description={t('apiKeys.description')}
        action={<Button variant="contained" startIcon={<Plus size={16} />} onClick={openCreate}>{t('apiKeys.create')}</Button>}>
        {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('apiKeys.retry')}</Button>}>{error}</Alert>}
        {!error && !keys && <Skeleton variant="rounded" height={120} />}
        {keys && keys.length === 0 && <Alert severity="info">{t('apiKeys.empty')}</Alert>}
        {keys && keys.length > 0 && (
          <Paper variant="outlined" sx={{ borderRadius: 2, overflowX: 'auto' }}>
            <Table size="small" aria-labelledby="api-keys-title">
              <TableHead>
                <TableRow sx={{ bgcolor: 'action.hover' }}>
                  <TableCell>{t('apiKeys.col.name')}</TableCell>
                  <TableCell>{t('apiKeys.col.key')}</TableCell>
                  <TableCell>{t('apiKeys.col.status')}</TableCell>
                  <TableCell>{t('apiKeys.col.created')}</TableCell>
                  <TableCell>{t('apiKeys.col.expires')}</TableCell>
                  <TableCell>{t('apiKeys.col.lastUsed')}</TableCell>
                  <TableCell align="right">{t('apiKeys.col.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {keys.map((k) => (
                  <TableRow key={k.id}>
                    <TableCell>{k.name}</TableCell>
                    <TableCell sx={{ fontFamily: 'monospace' }}>{`${k.prefix}_…`}</TableCell>
                    <TableCell><Chip size="small" color={API_KEY_STATUS_COLOR[k.status]} label={t(`apiKeys.status.${k.status}`)} /></TableCell>
                    <TableCell>{formatDate(k.createdAt)}</TableCell>
                    <TableCell>{k.expiresAt ? formatDate(k.expiresAt) : t('apiKeys.never')}</TableCell>
                    <TableCell>{k.lastUsedAt ? formatDateTime(k.lastUsedAt) : t('apiKeys.neverUsed')}</TableCell>
                    <TableCell align="right">
                      {k.status === 'ACTIVE' && (
                        <Button size="small" color="error" onClick={() => setRevoking(k)} aria-label={t('apiKeys.revokeNamed', { name: k.name })}>
                          {t('apiKeys.revoke')}
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>
        )}
      </SettingsSection>

      <Dialog open={createOpen} onClose={closeCreate} fullWidth maxWidth="sm" aria-labelledby="api-key-create-title">
        <DialogTitle id="api-key-create-title">{created ? t('apiKeys.createdTitle') : t('apiKeys.createTitle')}</DialogTitle>
        <DialogContent>
          {!created && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
              <TextField label={t('apiKeys.name')} value={name} required autoFocus onChange={(e) => setName(e.target.value)}
                error={Boolean(createError)} helperText={createError ?? t('apiKeys.nameHelp')} slotProps={{ htmlInput: { maxLength: 100 } }} />
              <TextField type="date" label={t('apiKeys.expiresOn')} value={expires} onChange={(e) => setExpires(e.target.value)}
                helperText={t('apiKeys.expiresHelp')} slotProps={{ inputLabel: { shrink: true }, htmlInput: { min: tomorrow() } }} />
            </Box>
          )}
          {created?.key && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
              <Alert severity="warning">{t('apiKeys.copyWarning')}</Alert>
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                <TextField label={t('apiKeys.yourKey')} value={created.key} fullWidth
                  slotProps={{ htmlInput: { readOnly: true, style: { fontFamily: 'monospace' } } }} />
                <Tooltip title={t('apiKeys.copy')}>
                  <IconButton aria-label={t('apiKeys.copy')} onClick={() => { void navigator.clipboard?.writeText(created.key ?? ''); setToast(t('apiKeys.copied')) }}>
                    <Copy size={18} />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          {!created && <Button onClick={closeCreate}>{t('apiKeys.cancel')}</Button>}
          {!created && <Button variant="contained" disabled={busy} onClick={create}>{t('apiKeys.createButton')}</Button>}
          {created && <Button variant="contained" onClick={closeCreate}>{t('apiKeys.done')}</Button>}
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(revoking)} onClose={() => setRevoking(null)} aria-labelledby="api-key-revoke-title">
        <DialogTitle id="api-key-revoke-title">{t('apiKeys.revokeTitle', { name: revoking?.name ?? '' })}</DialogTitle>
        <DialogContent><DialogContentText>{t('apiKeys.revokeBody')}</DialogContentText></DialogContent>
        <DialogActions>
          <Button onClick={() => setRevoking(null)}>{t('apiKeys.cancel')}</Button>
          <Button color="error" variant="contained" disabled={busy} onClick={revoke}>{t('apiKeys.revoke')}</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </Box>
  )
}
