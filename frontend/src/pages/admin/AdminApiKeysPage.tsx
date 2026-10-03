import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Pagination, Paper, Skeleton, Table, TableBody, TableCell, TableHead, TableRow,
} from '@mui/material'
import { KeyRound } from 'lucide-react'
import { ApiError } from '../../api/client'
import { adminApiKeysApi, type AdminApiKeyPage } from '../../api/apiKeysApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { API_KEY_STATUS_COLOR } from '../../components/security/ApiKeysSection'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

/** "/admin/integrations/api-keys" — REQ-INT-001.4 (C61), `MANAGE_INTEGRATIONS`:
 * every API key with its owner, last use and request count. Read-only. */
export function AdminApiKeysPage() {
  const { t } = useTranslation()
  const { formatDate, formatDateTime } = useLocalePreference()
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<AdminApiKeyPage | null>(null)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => adminApiKeysApi.list(page)
    .then((r) => { setResult(r); setError(null) })
    .catch((e) => setError(e instanceof ApiError ? e.message : t('apiKeys.loadError'))), [page, t])
  useEffect(() => { void load() }, [load])

  return (
    <>
      <PageHeader icon={KeyRound} accent="violet" area="integrations" title={t('admin.apiKeys.title')} subtitle={t('admin.apiKeys.subtitle')} />
      {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('apiKeys.retry')}</Button>}>{error}</Alert>}
      {!error && !result && <Skeleton variant="rounded" height={280} />}
      {result && (
        <Paper variant="outlined" sx={{ borderRadius: 3, overflowX: 'auto' }}>
          <Table size="small">
            <caption style={{ captionSide: 'top', textAlign: 'left', padding: '12px 16px' }}>
              {t('admin.apiKeys.caption', { count: result.totalElements })}
            </caption>
            <TableHead>
              <TableRow sx={{ bgcolor: 'action.hover' }}>
                <TableCell>{t('admin.apiKeys.owner')}</TableCell>
                <TableCell>{t('apiKeys.col.name')}</TableCell>
                <TableCell>{t('apiKeys.col.key')}</TableCell>
                <TableCell>{t('apiKeys.col.status')}</TableCell>
                <TableCell>{t('apiKeys.col.created')}</TableCell>
                <TableCell>{t('apiKeys.col.lastUsed')}</TableCell>
                <TableCell align="right">{t('admin.apiKeys.requests')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {result.items.length === 0 && (
                <TableRow><TableCell colSpan={7} sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>{t('admin.apiKeys.empty')}</TableCell></TableRow>
              )}
              {result.items.map((k) => (
                <TableRow key={k.id} hover>
                  <TableCell>{k.ownerEmail ?? `#${k.ownerCustomerId}`}</TableCell>
                  <TableCell>{k.name}</TableCell>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{`${k.prefix}_…`}</TableCell>
                  <TableCell><Chip size="small" color={API_KEY_STATUS_COLOR[k.status]} label={t(`apiKeys.status.${k.status}`)} /></TableCell>
                  <TableCell>{formatDate(k.createdAt)}</TableCell>
                  <TableCell>{k.lastUsedAt ? formatDateTime(k.lastUsedAt) : t('apiKeys.neverUsed')}</TableCell>
                  <TableCell align="right">{k.requestCount.toLocaleString()}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
      {result && result.totalElements > result.size && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination count={Math.ceil(result.totalElements / result.size)} page={page + 1} onChange={(_, p) => setPage(p - 1)} />
        </Box>
      )}
    </>
  )
}
