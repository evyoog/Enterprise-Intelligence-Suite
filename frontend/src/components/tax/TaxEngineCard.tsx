import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, List, ListItem, ListItemIcon, ListItemText, Paper, Switch, Typography, useTheme } from '@mui/material'
import { alpha } from '@mui/material/styles'
import { CheckCircle2, Cpu, Plug, XCircle } from 'lucide-react'
import { taxApi, type EngineStatus, type TaxStatus, type ValidationReport } from '../../api/taxApi'
import { ConfirmDialog } from '../ui/ConfirmDialog'
import { errorText, formatDateTime } from './taxFormat'

const STATUS_COLOR: Record<EngineStatus, 'success' | 'warning' | 'error'> = { READY: 'success', ATTENTION: 'warning', BLOCKING: 'error' }

/**
 * REQ-BIL-002.1 to .6: the External Tax Engine switch and the active mode with its status. Off = the EIS internal engine,
 * On = the external provider. Switching on runs the provider checks on the server; a refused switch lists every failed check.
 */
export function TaxEngineCard({ status, onChanged, onToast }: { status: TaxStatus; onChanged: (s: TaxStatus) => void; onToast: (message: string) => void }) {
  const { t, i18n } = useTranslation()
  const theme = useTheme()
  const [pending, setPending] = useState<boolean | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [refused, setRefused] = useState<ValidationReport | null>(null)

  const confirm = async () => {
    if (pending === null) return
    setBusy(true)
    setError(null)
    try {
      const result = await taxApi.setMode(pending)
      if (result.changed) {
        onChanged(result.status)
        onToast(t(pending ? 'admin.tax.engine.switchedOn' : 'admin.tax.engine.switchedOff'))
      } else if (result.report && !result.report.passed) {
        onChanged(result.status)
        setRefused(result.report)
      }
      setPending(null)
    } catch (e) {
      setError(errorText(e, t('admin.tax.engine.switchError')))
      setPending(null)
    } finally {
      setBusy(false)
    }
  }

  const external = status.externalEngineEnabled
  const color = theme.palette[STATUS_COLOR[status.status]].main
  return (
    <Paper variant="outlined" component="section" aria-labelledby="tax-engine-title" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3, mb: 3, borderColor: alpha(color, 0.5) }}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap' }}>
        <Box aria-hidden sx={{ width: 44, height: 44, borderRadius: 2, display: 'grid', placeItems: 'center', color, bgcolor: alpha(color, 0.12), flexShrink: 0 }}>
          {external ? <Plug size={22} /> : <Cpu size={22} />}
        </Box>
        <Box sx={{ flex: 1, minWidth: 260 }}>
          <Typography id="tax-engine-title" component="h2" variant="h6" sx={{ fontWeight: 700 }}>{t('admin.tax.engine.title')}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.tax.engine.description')}</Typography>
        </Box>
        <FormControlLabel
          labelPlacement="start"
          control={<Switch checked={external} onChange={(e) => setPending(e.target.checked)} disabled={busy}
            slotProps={{ input: { 'aria-label': t('admin.tax.engine.switch') } }} />}
          label={<Typography sx={{ fontWeight: 600 }}>{t('admin.tax.engine.switch')}: {t(external ? 'admin.tax.engine.on' : 'admin.tax.engine.off')}</Typography>}
          slotProps={{ typography: { component: 'span' } }}
          sx={{ ml: 0 }}
        />
      </Box>

      <Typography variant="body2" sx={{ mt: 2 }}>{t(external ? 'admin.tax.engine.helperOn' : 'admin.tax.engine.helperOff')}</Typography>

      <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap', mt: 2, alignItems: 'center' }}>
        <Box>
          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>{t('admin.tax.engine.activeMode')}</Typography>
          <Typography data-testid="tax-active-mode" sx={{ fontWeight: 700 }}>{t(external ? 'admin.tax.engine.external' : 'admin.tax.engine.internal')}</Typography>
        </Box>
        <Box>
          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>{t('admin.tax.engine.statusLabel')}</Typography>
          <Chip data-testid="tax-engine-status" size="small" color={STATUS_COLOR[status.status]} label={t(`admin.tax.engine.status.${status.status}`)} />
        </Box>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
          {t('admin.tax.engine.version', { version: status.configVersion })}
          {' · '}
          {status.modeChangedAt
            ? t('admin.tax.engine.changed', { when: formatDateTime(status.modeChangedAt, i18n.language), who: status.modeChangedBy ?? '—' })
            : t('admin.tax.engine.neverChanged')}
        </Typography>
      </Box>

      <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
        {status.messages.map((m) => (
          <Alert key={m.code} severity={m.level === 'ERROR' ? 'error' : m.level === 'WARNING' ? 'warning' : 'info'} variant="outlined">{m.text}</Alert>
        ))}
        {error && <Alert severity="error" role="alert">{error}</Alert>}
      </Box>

      <ConfirmDialog open={pending !== null} destructive={false} busy={busy}
        title={pending ? t('admin.tax.engine.confirmOnTitle') : t('admin.tax.engine.confirmOffTitle')}
        body={pending ? t('admin.tax.engine.confirmOnBody') : t('admin.tax.engine.confirmOffBody')}
        confirmLabel={t('admin.tax.engine.confirm')} onConfirm={() => void confirm()} onClose={() => setPending(null)} />

      <Dialog open={refused !== null} onClose={() => setRefused(null)} aria-labelledby="tax-refused-title" maxWidth="sm" fullWidth>
        <DialogTitle id="tax-refused-title">{t('admin.tax.engine.refusedTitle')}</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 1 }}>{t('admin.tax.engine.refusedBody')}</Typography>
          {refused && <ValidationChecks report={refused} />}
        </DialogContent>
        <DialogActions><Button onClick={() => setRefused(null)}>{t('admin.tax.close')}</Button></DialogActions>
      </Dialog>
    </Paper>
  )
}

/** Every provider check with its result (REQ-BIL-002.4 and .33). */
export function ValidationChecks({ report }: { report: ValidationReport }) {
  return (
    <List dense disablePadding>
      {report.checks.map((c) => (
        <ListItem key={c.code} disableGutters alignItems="flex-start">
          <ListItemIcon sx={{ minWidth: 30, mt: 0.5 }}>{c.passed ? <CheckCircle2 size={18} color="#10B981" aria-hidden /> : <XCircle size={18} color="#EF4444" aria-hidden />}</ListItemIcon>
          <ListItemText primary={c.label} secondary={c.detail} slotProps={{ primary: { sx: { fontWeight: 600 } } }} />
        </ListItem>
      ))}
    </List>
  )
}
