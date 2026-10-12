import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, Typography } from '@mui/material'
import { accessApi } from '../../api/accessApi'
import type { AuditLogPage } from '../../api/auditLogApi'
import { ApiError } from '../../api/client'

const PAGE_SIZE = 20

/** The Activity tab (REQ-TEN-005.9): who changed whose access, from the organization's audit log. */
export function AccessActivityTab() {
  const { t, i18n } = useTranslation()
  const [page, setPage] = useState(0)
  const [data, setData] = useState<AuditLogPage | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    accessApi.activity(page, PAGE_SIZE).then((d) => { setError(null); setData(d) }).catch((e) => setError(e instanceof ApiError ? e.message : t('access.loadError')))
  }, [page, t])

  return (
    <Paper variant="outlined">
      {error && <Alert severity="error" sx={{ m: 2 }}>{error}</Alert>}
      <TableContainer>
        <Table size="small" aria-label={t('access.tabs.activity')}>
          <TableHead>
            <TableRow>
              <TableCell>{t('access.when')}</TableCell>
              <TableCell>{t('access.what')}</TableCell>
              <TableCell>{t('access.by')}</TableCell>
              <TableCell>{t('access.details')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data && data.items.length === 0 && (
              <TableRow><TableCell colSpan={4}><Typography sx={{ color: 'text.secondary', py: 1 }}>{t('access.noActivity')}</Typography></TableCell></TableRow>
            )}
            {data?.items.map((e) => (
              <TableRow key={e.id}>
                <TableCell>{new Date(e.timestamp).toLocaleString(i18n.language)}</TableCell>
                <TableCell>{t(`access.actions_.${e.action}`, { defaultValue: e.action })}</TableCell>
                <TableCell>{e.actorEmail ?? (e.actorCustomerId ? `#${e.actorCustomerId}` : '—')}</TableCell>
                <TableCell>{e.detail}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination component="div" count={data?.totalElements ?? 0} page={page} rowsPerPage={PAGE_SIZE} rowsPerPageOptions={[PAGE_SIZE]}
        onPageChange={(_, p) => setPage(p)} />
    </Paper>
  )
}
