import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Paper,
  Skeleton, Snackbar, Table, TableBody, TableCell, TableHead, TableRow,
} from '@mui/material'
import { KeyRound } from 'lucide-react'
import { ApiError } from '../../api/client'
import { API_KEY_STATUS_COLOR, apiKeysApi, type ApiKey } from '../../api/apiKeysApi'
import { SettingsSection } from '../settings/SettingsSection'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

/**
 * REQ-INT-001.1 (C92): API keys now belong to the organization (Organization → Sign-in security → API keys).
 * Personal keys created before that keep working as read-write; this section, on Account → Security, lists them
 * so they can be revoked, and is hidden when there are none. New keys cannot be created here.
 */
export function ApiKeysSection() {
  const { t } = useTranslation()
  const { formatDate, formatDateTime } = useLocalePreference()
  const [keys, setKeys] = useState<ApiKey[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [revoking, setRevoking] = useState<ApiKey | null>(null)
  const [busy, setBusy] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  const load = useCallback(() => apiKeysApi.list()
    .then((k) => { setKeys(k); setError(null) })
    .catch((e) => setError(e instanceof ApiError ? e.message : t('apiKeys.loadError'))), [t])
  useEffect(() => { void load() }, [load])

  const revoke = () => {
    if (!revoking) return
    setBusy(true)
    apiKeysApi.revoke(revoking.id)
      .then(() => { setToast(t('apiKeys.revoked')); setRevoking(null); void load() })
      .catch((e) => setToast(e instanceof ApiError ? e.message : t('apiKeys.revokeError')))
      .finally(() => setBusy(false))
  }

  if (keys && keys.length === 0) return null
  return (
    <Box sx={{ mt: 3 }}>
      <SettingsSection id="api-keys" icon={KeyRound} accent="violet" title={t('apiKeys.legacyTitle')} description={t('apiKeys.legacyDescription')}>
        {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('apiKeys.retry')}</Button>}>{error}</Alert>}
        {!error && !keys && <Skeleton variant="rounded" height={120} />}
        {keys && keys.length > 0 && (
          <Paper variant="outlined" sx={{ borderRadius: 2, overflowX: 'auto' }}>
            <Table size="small" aria-labelledby="api-keys-title">
              <TableHead>
                <TableRow sx={{ bgcolor: 'action.hover' }}>
                  <TableCell>{t('apiKeys.col.name')}</TableCell>
                  <TableCell>{t('apiKeys.col.key')}</TableCell>
                  <TableCell>{t('apiKeys.col.scope')}</TableCell>
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
                    <TableCell>{t(`apiKeys.scope.${k.scope ?? 'READ_WRITE'}`)}</TableCell>
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
