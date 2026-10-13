import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Paper, Skeleton, Switch,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Tooltip, Typography,
} from '@mui/material'
import { Plus } from 'lucide-react'
import { taxApi, type TaxClass, type TaxReference, type TaxRule, type TaxStatus } from '../../api/taxApi'
import { ConfirmDialog } from '../ui/ConfirmDialog'
import { EmptyState } from '../ui/EmptyState'
import { StatusBadge, type StatusTone } from '../ui/StatusBadge'
import { TaxPreviewPanel } from './TaxPreviewPanel'
import { TaxRuleDialog } from './TaxRuleDialog'
import { componentsSummary, errorText, formatDate } from './taxFormat'

const TONE: Record<TaxRule['approvalStatus'], StatusTone> = { DRAFT: 'neutral', PENDING_APPROVAL: 'warning', APPROVED: 'success', REJECTED: 'error' }

type RuleAction = 'submit' | 'approve' | 'activate' | 'deactivate' | 'newVersion'

/** REQ-BIL-002.10 to .14, .21: the rules list with its life cycle actions, the approval policy and a calculation preview. */
export function TaxRulesPanel({ status, reference, onStatusChange, onToast }: {
  status: TaxStatus
  reference: TaxReference
  onStatusChange: () => void
  onToast: (message: string) => void
}) {
  const { t, i18n } = useTranslation()
  const [rules, setRules] = useState<TaxRule[] | null>(null)
  const [classes, setClasses] = useState<TaxClass[]>([])
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [editing, setEditing] = useState<TaxRule | 'new' | null>(null)
  const [rejecting, setRejecting] = useState<TaxRule | null>(null)
  const [rejectReason, setRejectReason] = useState('')
  const [deleting, setDeleting] = useState<TaxRule | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [policyBusy, setPolicyBusy] = useState(false)

  const load = useCallback(() => {
    return Promise.all([taxApi.rules(), taxApi.classes()])
      .then(([r, c]) => { setRules(r); setClasses(c); setError(null) })
      .catch((e) => setError(errorText(e, t('admin.tax.loadError'))))
  }, [t])
  useEffect(() => { void load() }, [load])

  const refresh = () => { void load(); onStatusChange() }

  const run = async (rule: TaxRule, action: RuleAction) => {
    setBusyId(rule.id)
    setActionError(null)
    try {
      if (action === 'submit') await taxApi.submitRule(rule.id)
      else if (action === 'approve') await taxApi.approveRule(rule.id)
      else if (action === 'activate') await taxApi.activateRule(rule.id)
      else if (action === 'deactivate') await taxApi.deactivateRule(rule.id)
      else await taxApi.newRuleVersion(rule.id)
      onToast(t(`admin.tax.rules.done.${action}`))
      refresh()
    } catch (e) {
      setActionError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setBusyId(null)
    }
  }

  const reject = async () => {
    if (!rejecting || !rejectReason.trim()) return
    setBusyId(rejecting.id)
    try {
      await taxApi.rejectRule(rejecting.id, rejectReason.trim())
      onToast(t('admin.tax.rules.done.reject'))
      setRejecting(null)
      setRejectReason('')
      refresh()
    } catch (e) {
      setActionError(errorText(e, t('admin.tax.rules.actionError')))
      setRejecting(null)
    } finally {
      setBusyId(null)
    }
  }

  const remove = async () => {
    if (!deleting) return
    setBusyId(deleting.id)
    try {
      await taxApi.deleteRule(deleting.id)
      onToast(t('admin.tax.rules.done.delete'))
      refresh()
    } catch (e) {
      setActionError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setDeleting(null)
      setBusyId(null)
    }
  }

  const togglePolicy = async (value: boolean) => {
    setPolicyBusy(true)
    try {
      await taxApi.saveApprovalPolicy(value)
      onStatusChange()
    } catch (e) {
      setActionError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setPolicyBusy(false)
    }
  }

  const where = (r: TaxRule) => [r.stateName ?? r.stateCode ?? t('admin.tax.rules.anywhere'), r.countryName].join(', ')
  const conditions = (r: TaxRule) => [
    r.supplyType !== 'ANY' ? t(`admin.tax.supplyType.${r.supplyType}`) : null,
    r.customerType !== 'ANY' ? t(`admin.tax.customerType.${r.customerType}`) : null,
    r.taxIdCondition !== 'ANY' ? t(`admin.tax.taxIdCondition.${r.taxIdCondition}`) : null,
    r.productClassCode ? `${t('admin.tax.ruleForm.taxClass')}: ${r.productClassCode}` : null,
  ].filter(Boolean).join(' · ') || t('admin.tax.rules.any')

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start', flexWrap: 'wrap', mb: 2 }}>
          <Box sx={{ flex: 1, minWidth: 260 }}>
            <Typography component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.rules.title')}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.tax.rules.description')}</Typography>
          </Box>
          <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setEditing('new')} disabled={!rules || classes === null}>{t('admin.tax.rules.new')}</Button>
        </Box>

        <FormControlLabel
          control={<Switch checked={status.allowSelfApproval} disabled={policyBusy} onChange={(e) => void togglePolicy(e.target.checked)} />}
          label={t('admin.tax.rules.selfApproval')} />
        <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 2 }}>{t('admin.tax.rules.selfApprovalHelp')}</Typography>

        {actionError && <Alert severity="error" role="alert" sx={{ mb: 2 }} onClose={() => setActionError(null)}>{actionError}</Alert>}
        {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('admin.tax.retry')}</Button>}>{error}</Alert>}
        {!error && rules === null && <Skeleton variant="rounded" height={160} />}
        {rules && rules.length === 0 && <EmptyState title={t('admin.tax.rules.title')} description={t('admin.tax.rules.empty')} />}
        {rules && rules.length > 0 && (
          <TableContainer>
            <Table size="small" aria-label={t('admin.tax.rules.title')}>
              <TableHead>
                <TableRow>
                  {(['rule', 'where', 'conditions', 'components', 'dates', 'status', 'actions'] as const).map((c) => <TableCell key={c}>{t(`admin.tax.rules.columns.${c}`)}</TableCell>)}
                </TableRow>
              </TableHead>
              <TableBody>
                {rules.map((r) => (
                  <TableRow key={r.id} hover sx={{ opacity: r.latestVersion || r.inForce ? 1 : 0.6 }}>
                    <TableCell>
                      <Typography sx={{ fontWeight: 600 }}>{r.name}</Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('admin.tax.rules.version', { version: r.version })} · {t(`admin.tax.taxType.${r.taxType}`)}</Typography>
                    </TableCell>
                    <TableCell>{where(r)}{r.jurisdiction ? <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>{r.jurisdiction}</Typography> : null}</TableCell>
                    <TableCell>{conditions(r)}</TableCell>
                    <TableCell>{componentsSummary(r, t)}</TableCell>
                    <TableCell>
                      {t('admin.tax.rules.from', { date: formatDate(r.effectiveFrom, i18n.language) })}
                      <br />
                      {r.effectiveTo ? t('admin.tax.rules.until', { date: formatDate(r.effectiveTo, i18n.language) }) : t('admin.tax.rules.noEnd')}
                    </TableCell>
                    <TableCell>
                      <StatusBadge tone={TONE[r.approvalStatus]} label={t(`admin.tax.rules.status.${r.approvalStatus}`)} />
                      {r.approvalStatus === 'APPROVED' && (
                        <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5, flexWrap: 'wrap' }}>
                          <Chip size="small" variant="outlined" color={r.active ? 'success' : 'default'} label={t(r.active ? 'admin.tax.rules.active' : 'admin.tax.rules.inactive')} />
                          <Chip size="small" variant="outlined" label={t(r.inForce ? 'admin.tax.rules.inForce' : 'admin.tax.rules.notYetInForce')} />
                        </Box>
                      )}
                      {r.approvalStatus === 'REJECTED' && r.rejectionReason && (
                        <Typography variant="caption" sx={{ display: 'block', color: 'error.main' }}>{t('admin.tax.rules.rejectedBecause', { reason: r.rejectionReason })}</Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', minWidth: 150 }}>
                        {busyId === r.id && <CircularProgress size={16} aria-label="…" />}
                        {(r.approvalStatus === 'DRAFT' || r.approvalStatus === 'REJECTED') && (
                          <>
                            <Button size="small" onClick={() => setEditing(r)} disabled={busyId === r.id}>{t('admin.tax.rules.action.edit')}</Button>
                            <Button size="small" onClick={() => void run(r, 'submit')} disabled={busyId === r.id}>{t('admin.tax.rules.action.submit')}</Button>
                            <Button size="small" color="error" onClick={() => setDeleting(r)} disabled={busyId === r.id}>{t('admin.tax.rules.action.delete')}</Button>
                          </>
                        )}
                        {r.approvalStatus === 'PENDING_APPROVAL' && (
                          <>
                            <Tooltip title={status.allowSelfApproval ? '' : t('admin.tax.rules.selfApprovalHelp')}>
                              <span><Button size="small" variant="contained" onClick={() => void run(r, 'approve')} disabled={busyId === r.id}>{t('admin.tax.rules.action.approve')}</Button></span>
                            </Tooltip>
                            <Button size="small" color="error" onClick={() => { setRejecting(r); setRejectReason('') }} disabled={busyId === r.id}>{t('admin.tax.rules.action.reject')}</Button>
                          </>
                        )}
                        {r.approvalStatus === 'APPROVED' && (
                          <>
                            {r.active
                              ? <Button size="small" onClick={() => void run(r, 'deactivate')} disabled={busyId === r.id}>{t('admin.tax.rules.action.deactivate')}</Button>
                              : <Button size="small" onClick={() => void run(r, 'activate')} disabled={busyId === r.id}>{t('admin.tax.rules.action.activate')}</Button>}
                            {r.latestVersion && <Button size="small" onClick={() => void run(r, 'newVersion')} disabled={busyId === r.id}>{t('admin.tax.rules.action.newVersion')}</Button>}
                          </>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      <TaxPreviewPanel reference={reference} classes={classes} />

      {editing !== null && (
        <TaxRuleDialog rule={editing === 'new' ? null : editing} reference={reference} classes={classes} onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); onToast(t('admin.tax.saved')); refresh() }} />
      )}

      <Dialog open={rejecting !== null} onClose={() => setRejecting(null)} aria-labelledby="tax-reject-title" maxWidth="xs" fullWidth>
        <DialogTitle id="tax-reject-title">{t('admin.tax.rules.rejectTitle')}</DialogTitle>
        <DialogContent>
          <TextField autoFocus fullWidth margin="dense" label={t('admin.tax.rules.rejectReason')} value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} required multiline minRows={2} />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setRejecting(null)}>{t('forms.cancel')}</Button>
          <Button variant="contained" color="error" onClick={() => void reject()} disabled={!rejectReason.trim()}>{t('admin.tax.rules.rejectConfirm')}</Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog open={deleting !== null} title={t('admin.tax.rules.deleteTitle')} body={t('admin.tax.rules.deleteBody')}
        confirmLabel={t('admin.tax.rules.deleteConfirm')} onConfirm={() => void remove()} onClose={() => setDeleting(null)} busy={busyId !== null && deleting !== null} />
    </Box>
  )
}
