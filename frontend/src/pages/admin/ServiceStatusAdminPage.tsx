import { useCallback, useEffect, useState, type HTMLAttributes } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, CircularProgress, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import {
  adminServiceStatusApi, SERVICE_STATUS_VALUES, type Incident, type ServiceStatusPage, type ServiceStatusValue,
} from '../../api/serviceStatusApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { ServiceStatusChip } from '../../components/status/ServiceStatusChip'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

/** A value for an `<input type="datetime-local">`, in the browser's local time. */
function toLocalInput(iso?: string | null) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const fromLocalInput = (value: string) => (value ? new Date(value).toISOString() : null)

interface IncidentForm {
  id?: number
  productId: number | ''
  title: string
  message: string
  startedAt: string
  endedAt: string
}

const emptyForm = (): IncidentForm => ({ productId: '', title: '', message: '', startedAt: toLocalInput(new Date().toISOString()), endedAt: '' })

/**
 * "/admin/service-status" — REQ-PRT-001.1, .2, .4 (decisions C20, C26).
 * Platform admins with MANAGE_SERVICE_STATUS post each product's status and
 * post or resolve incidents. Customers see the result on /status. The backend
 * validates everything (active product, end after start) and its messages
 * are shown as returned.
 */
export function ServiceStatusAdminPage() {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  const [page, setPage] = useState<ServiceStatusPage | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [drafts, setDrafts] = useState<Record<number, { status: ServiceStatusValue; note: string }>>({})
  const [savingId, setSavingId] = useState<number | null>(null)
  const [form, setForm] = useState<IncidentForm>(emptyForm)
  const [formError, setFormError] = useState<string | null>(null)
  const [posting, setPosting] = useState(false)
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

  const submitIncident = async () => {
    if (form.productId === '') return
    setPosting(true)
    setFormError(null)
    const payload = {
      productId: form.productId, title: form.title, message: form.message,
      startedAt: fromLocalInput(form.startedAt)!, endedAt: fromLocalInput(form.endedAt),
    }
    try {
      if (form.id) await adminServiceStatusApi.updateIncident(form.id, payload)
      else await adminServiceStatusApi.createIncident(payload)
      setNotice(form.id ? t('serviceStatus.admin.incidentUpdated') : t('serviceStatus.admin.incidentPosted'))
      setForm(emptyForm())
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('serviceStatus.admin.saveError'))
    } finally {
      setPosting(false)
    }
  }

  const editIncident = (i: Incident) => setForm({
    id: i.id, productId: i.productId, title: i.title, message: i.message,
    startedAt: toLocalInput(i.startedAt), endedAt: toLocalInput(i.endedAt),
  })

  const resolveNow = (i: Incident) => setForm({
    id: i.id, productId: i.productId, title: i.title, message: i.message,
    startedAt: toLocalInput(i.startedAt), endedAt: toLocalInput(new Date().toISOString()),
  })

  const canSubmit = form.productId !== '' && form.title.trim() && form.message.trim() && form.startedAt

  return (
    <Box sx={{ pb: 4 }}>
      <PageHeader title={t('serviceStatus.admin.title')} subtitle={t('serviceStatus.admin.subtitle')} />
      {page && !page.enabled && <Alert severity="info" sx={{ mb: 2 }}>{t('serviceStatus.admin.disabled')}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {notice && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setNotice(null)}>{notice}</Alert>}
      {page === null && !error && <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>}

      {page && (
        <>
          <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('serviceStatus.admin.productsTitle')}</Typography>
          <Paper variant="outlined" sx={{ mb: 4, overflowX: 'auto' }}>
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

          <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1 }}>
            {form.id ? t('serviceStatus.admin.editIncident') : t('serviceStatus.admin.newIncident')}
          </Typography>
          <Paper variant="outlined" sx={{ p: 2.5, mb: 4 }}>
            {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
              <TextField
                select size="small" label={t('serviceStatus.product')} required value={form.productId}
                onChange={(e) => setForm((f) => ({ ...f, productId: Number(e.target.value) }))}
              >
                {page.products.map((p) => <MenuItem key={p.productId} value={p.productId}>{p.productName}</MenuItem>)}
              </TextField>
              <TextField size="small" label={t('serviceStatus.admin.incidentTitle')} required value={form.title}
                onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} slotProps={{ htmlInput: { maxLength: 200 } }} />
              <TextField size="small" label={t('serviceStatus.admin.message')} required multiline minRows={2} value={form.message}
                onChange={(e) => setForm((f) => ({ ...f, message: e.target.value }))} sx={{ gridColumn: '1 / -1' }}
                slotProps={{ htmlInput: { maxLength: 4000 } }} />
              <TextField size="small" type="datetime-local" label={t('serviceStatus.admin.start')} required value={form.startedAt}
                onChange={(e) => setForm((f) => ({ ...f, startedAt: e.target.value }))} slotProps={{ inputLabel: { shrink: true } }} />
              <TextField size="small" type="datetime-local" label={t('serviceStatus.admin.end')} value={form.endedAt}
                helperText={t('serviceStatus.admin.endHint')}
                onChange={(e) => setForm((f) => ({ ...f, endedAt: e.target.value }))} slotProps={{ inputLabel: { shrink: true } }} />
            </Box>
            <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
              <Button variant="contained" disabled={posting || !canSubmit} onClick={submitIncident}>
                {form.id ? t('serviceStatus.admin.saveIncident') : t('serviceStatus.admin.postIncident')}
              </Button>
              {form.id && <Button onClick={() => setForm(emptyForm())}>{t('serviceStatus.admin.cancelEdit')}</Button>}
            </Box>
          </Paper>

          <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('serviceStatus.incidentsTitle')}</Typography>
          {page.incidents.length === 0 && <Typography sx={{ color: 'text.secondary' }}>{t('serviceStatus.noIncidents')}</Typography>}
          <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {page.incidents.map((i) => (
              <Paper component="li" key={i.id} variant="outlined" sx={{ p: 2, borderLeft: 4, borderLeftColor: i.open ? 'warning.main' : 'success.main' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
                  <Box>
                    <Typography sx={{ fontWeight: 700 }}>{i.productName} — {i.title}</Typography>
                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                      {i.open
                        ? t('serviceStatus.openSince', { date: formatDate(i.startedAt) })
                        : t('serviceStatus.resolvedRange', { start: formatDate(i.startedAt), end: formatDate(i.endedAt!) })}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button size="small" aria-label={t('serviceStatus.admin.editFor', { title: i.title })} onClick={() => editIncident(i)}>
                      {t('serviceStatus.admin.edit')}
                    </Button>
                    {i.open && (
                      <Button size="small" variant="outlined" aria-label={t('serviceStatus.admin.resolveFor', { title: i.title })} onClick={() => resolveNow(i)}>
                        {t('serviceStatus.admin.resolve')}
                      </Button>
                    )}
                  </Box>
                </Box>
                <Typography variant="body2" sx={{ mt: 0.5 }}>{i.message}</Typography>
              </Paper>
            ))}
          </Box>
        </>
      )}
    </Box>
  )
}
