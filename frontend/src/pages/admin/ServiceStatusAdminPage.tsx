import { Activity as PHActivity } from 'lucide-react'
import { useCallback, useEffect, useState, type HTMLAttributes } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, CircularProgress, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import {
  adminServiceStatusApi, SERVICE_STATUS_VALUES, type ServiceStatusPage, type ServiceStatusValue,
} from '../../api/serviceStatusApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { ServiceStatusChip } from '../../components/status/ServiceStatusChip'

/**
 * "/admin/service-status" — REQ-PRT-001.1, .2, .4 (decisions C20, C26).
 * Platform admins with MANAGE_SERVICE_STATUS post each product's status.
 * Customers see the result on /status. Incidents moved to the Incidents tab
 * (C101, AdminIncidentsPage). The backend validates everything and its
 * messages are shown as returned.
 */
export function ServiceStatusAdminPage() {
  const { t } = useTranslation()
  const [page, setPage] = useState<ServiceStatusPage | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [drafts, setDrafts] = useState<Record<number, { status: ServiceStatusValue; note: string }>>({})
  const [savingId, setSavingId] = useState<number | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const load = useCallback(() => {
    adminServiceStatusApi.get()
      .then((p) => {
        setPage(p)
        setDrafts(Object.fromEntries(p.products.map((s) => [s.productId, { status: s.status, note: s.note ?? '' }])))
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('serviceStatus.loadError')))
  }, [t])

  useEffect(() => { load() }, [load])

  const saveStatus = async (productId: number) => {
    const draft = drafts[productId]
    setSavingId(productId)
    setError(null)
    try {
      await adminServiceStatusApi.updateStatus(productId, draft.status, draft.note)
      setNotice(t('serviceStatus.admin.statusSaved'))
      load()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('serviceStatus.admin.saveError'))
    } finally {
      setSavingId(null)
    }
  }

  return (
    <Box sx={{ pb: 4 }}>
      <PageHeader icon={PHActivity} accent="emerald" area="operations" title={t('serviceStatus.admin.title')} subtitle={t('serviceStatus.admin.subtitle')} />
      {page && !page.enabled && <Alert severity="info" sx={{ mb: 2 }}>{t('serviceStatus.admin.disabled')}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {notice && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setNotice(null)}>{notice}</Alert>}
      {page === null && !error && <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>}

      {page && (
        <>
          <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('serviceStatus.admin.productsTitle')}</Typography>
          <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>{t('serviceStatus.product')}</TableCell>
                  <TableCell>{t('serviceStatus.admin.current')}</TableCell>
                  <TableCell>{t('serviceStatus.admin.newStatus')}</TableCell>
                  <TableCell>{t('serviceStatus.admin.note')}</TableCell>
                  <TableCell>{t('serviceStatus.admin.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {page.products.map((p) => {
                  const draft = drafts[p.productId] ?? { status: p.status, note: '' }
                  return (
                    <TableRow key={p.productId}>
                      <TableCell sx={{ fontWeight: 600 }}>{p.productName}</TableCell>
                      <TableCell><ServiceStatusChip status={p.status} /></TableCell>
                      <TableCell>
                        <TextField
                          select size="small" value={draft.status}
                          onChange={(e) => setDrafts((d) => ({ ...d, [p.productId]: { ...draft, status: e.target.value as ServiceStatusValue } }))}
                          slotProps={{ select: { SelectDisplayProps: { 'aria-label': t('serviceStatus.admin.statusFor', { name: p.productName }) } as HTMLAttributes<HTMLDivElement> } }}
                        >
                          {SERVICE_STATUS_VALUES.map((v) => <MenuItem key={v} value={v}>{t(`serviceStatus.values.${v}`)}</MenuItem>)}
                        </TextField>
                      </TableCell>
                      <TableCell>
                        <TextField
                          size="small" value={draft.note}
                          onChange={(e) => setDrafts((d) => ({ ...d, [p.productId]: { ...draft, note: e.target.value } }))}
                          slotProps={{ htmlInput: { maxLength: 500, 'aria-label': t('serviceStatus.admin.noteFor', { name: p.productName }) } }}
                        />
                      </TableCell>
                      <TableCell>
                        <Button size="small" variant="contained" disabled={savingId === p.productId}
                          aria-label={t('serviceStatus.admin.saveFor', { name: p.productName })}
                          onClick={() => saveStatus(p.productId)}>
                          {t('serviceStatus.admin.save')}
                        </Button>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </Paper>
        </>
      )}
    </Box>
  )
}
