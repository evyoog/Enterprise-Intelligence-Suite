import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Autocomplete, Box, Button, Chip, CircularProgress, Skeleton, Snackbar, Switch,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, ToggleButton, ToggleButtonGroup, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { adminSlaApi, type BusinessDay, type SlaConfig, type SlaPolicy, type SlaSettings } from '../../api/slaApi'
import type { TicketPriority } from '../../api/supportApi'
import { TimeZonePicker } from '../../components/preferences/TimeZonePicker'
import { formatSlaDuration } from '../../components/support/SlaChip'
import { ErrorState } from '../../components/ui/ErrorState'
import { FormSection } from '../../components/ui/FormSection'

const PRIORITY_ORDER: TicketPriority[] = ['URGENT', 'HIGH', 'MEDIUM', 'LOW']
const DAYS: BusinessDay[] = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']
const MAX_EMAILS = 20
const EMAIL_SHAPE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

interface PolicyDraft { first: string; resolution: string; aroundTheClock: boolean }
type PolicyErrors = { first?: string; resolution?: string }

const toDraft = (p: SlaPolicy): PolicyDraft => ({
  first: String(p.firstResponseMinutes), resolution: String(p.resolutionMinutes), aroundTheClock: p.aroundTheClock,
})

/** A whole number of minutes, 1 or more; null when the text is not one. */
function minutesOf(text: string): number | null {
  return /^\d+$/.test(text.trim()) && Number(text) >= 1 ? Number(text) : null
}

/** The backend's rules, mirrored: both targets 1 minute or more, first response no later than resolution. */
function validatePolicy(d: PolicyDraft): PolicyErrors {
  const first = minutesOf(d.first)
  const resolution = minutesOf(d.resolution)
  const errors: PolicyErrors = {}
  if (first === null) errors.first = 'sla.settings.errMinutes'
  if (resolution === null) errors.resolution = 'sla.settings.errMinutes'
  if (first !== null && resolution !== null && first > resolution) errors.first = 'sla.settings.errOrder'
  return errors
}

function validateSettings(s: SlaSettings): Partial<Record<'days' | 'hours' | 'warn' | 'zone', string>> {
  const errors: Partial<Record<'days' | 'hours' | 'warn' | 'zone', string>> = {}
  if (s.businessDays.length === 0) errors.days = 'sla.settings.errDays'
  if (!/^\d{2}:\d{2}$/.test(s.businessStart) || !/^\d{2}:\d{2}$/.test(s.businessEnd) || s.businessStart >= s.businessEnd) errors.hours = 'sla.settings.errHours'
  if (!Number.isInteger(s.warnAtPercent) || s.warnAtPercent < 1 || s.warnAtPercent > 99) errors.warn = 'sla.settings.errWarn'
  if (!s.timeZone.trim()) errors.zone = 'sla.settings.errZone'
  return errors
}

const errorMessage = (e: unknown, fallback: string) => (e instanceof ApiError && e.message ? e.message : fallback)

/** "Support SLA" — C101 / REQ-SUP-002. Shown as a tab beside the tickets list
 * on /admin/support; MANAGE_SUPPORT_TICKETS. */
export function AdminSlaSettingsPage() {
  const { t } = useTranslation()
  const [config, setConfig] = useState<SlaConfig | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  const [drafts, setDrafts] = useState<Record<string, PolicyDraft>>({})
  const [settings, setSettings] = useState<SlaSettings | null>(null)
  const [warnText, setWarnText] = useState('')
  const [policyBusy, setPolicyBusy] = useState(false)
  const [policyError, setPolicyError] = useState<string | null>(null)
  const [settingsBusy, setSettingsBusy] = useState(false)
  const [settingsError, setSettingsError] = useState<string | null>(null)
  const [emailError, setEmailError] = useState<string | null>(null)

  const adopt = useCallback((c: SlaConfig) => {
    setConfig(c)
    setDrafts(Object.fromEntries(c.policies.map((p) => [p.priority, toDraft(p)])))
    setSettings(c.settings)
    setWarnText(String(c.settings.warnAtPercent))
  }, [])

  const load = useCallback(() => {
    setLoadError(null)
    adminSlaApi.get().then(adopt).catch((e) => setLoadError(errorMessage(e, t('sla.settings.loadFailed'))))
  }, [adopt, t])

  useEffect(load, [load])

  const policies = useMemo(() => config?.policies ?? [], [config])
  const policyErrors = useMemo(
    () => Object.fromEntries(policies.map((p) => [p.priority, validatePolicy(drafts[p.priority] ?? toDraft(p))])) as Record<string, PolicyErrors>,
    [policies, drafts],
  )
  const policiesInvalid = Object.values(policyErrors).some((e) => e.first || e.resolution)
  const policiesDirty = policies.some((p) => {
    const d = drafts[p.priority]
    return d !== undefined && JSON.stringify(d) !== JSON.stringify(toDraft(p))
  })

  const settingsWithWarn: SlaSettings | null = settings && { ...settings, warnAtPercent: /^\d+$/.test(warnText.trim()) ? Number(warnText) : NaN }
  const settingsErrors = settingsWithWarn ? validateSettings(settingsWithWarn) : {}
  const settingsInvalid = Object.keys(settingsErrors).length > 0
  const settingsDirty = Boolean(config && settingsWithWarn && JSON.stringify(settingsWithWarn) !== JSON.stringify(config.settings))

  const savePolicies = async () => {
    setPolicyBusy(true)
    setPolicyError(null)
    try {
      const body: SlaPolicy[] = policies.map((p) => {
        const d = drafts[p.priority]
        return { priority: p.priority, firstResponseMinutes: Number(d.first), resolutionMinutes: Number(d.resolution), aroundTheClock: d.aroundTheClock }
      })
      adopt(await adminSlaApi.updatePolicies(body))
      setToast(t('sla.settings.policiesSaved'))
    } catch (e) {
      setPolicyError(errorMessage(e, t('sla.settings.saveFailed')))
    } finally {
      setPolicyBusy(false)
    }
  }

  const saveSettings = async () => {
    if (!settingsWithWarn) return
    setSettingsBusy(true)
    setSettingsError(null)
    try {
      adopt(await adminSlaApi.updateSettings(settingsWithWarn))
      setToast(t('sla.settings.settingsSaved'))
    } catch (e) {
      setSettingsError(errorMessage(e, t('sla.settings.saveFailed')))
    } finally {
      setSettingsBusy(false)
    }
  }

  const setDraft = (priority: string, patch: Partial<PolicyDraft>) =>
    setDrafts((prev) => ({ ...prev, [priority]: { ...prev[priority], ...patch } }))

  const changeEmails = (values: string[]) => {
    const next: string[] = []
    let problem: string | null = null
    for (const raw of values) {
      const v = raw.trim()
      if (!v) continue
      if (!EMAIL_SHAPE.test(v)) { problem = t('sla.settings.errEmail', { email: v }); continue }
      if (next.some((e) => e.toLowerCase() === v.toLowerCase())) continue
      if (next.length >= MAX_EMAILS) { problem = t('sla.settings.errEmailMax', { max: MAX_EMAILS }); continue }
      next.push(v)
    }
    setEmailError(problem)
    setSettings((s) => s && { ...s, alertEmails: next })
  }

  if (loadError) return <ErrorState title={t('sla.settings.loadFailedTitle')} message={loadError} onRetry={load} />
  if (!config || !settings) return <Skeleton variant="rounded" height={320} aria-label={t('sla.settings.loading')} />

  const busyIcon = (busy: boolean) => (busy ? <CircularProgress size={16} color="inherit" /> : undefined)

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Box>
        <Typography component="h2" sx={{ fontWeight: 700, fontSize: 18 }}>{t('sla.settings.title')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.25 }}>{t('sla.settings.intro')}</Typography>
      </Box>
      <Alert severity="info">{t('sla.settings.note')}</Alert>

      <FormSection id="sla-targets" title={t('sla.settings.targets.title')} description={t('sla.settings.targets.description')}>
        {policyError && <Alert severity="error">{policyError}</Alert>}
        <TableContainer>
          <Table size="small" aria-label={t('sla.settings.targets.title')}>
            <TableHead>
              <TableRow>
                <TableCell>{t('sla.settings.targets.priority')}</TableCell>
                <TableCell>{t('sla.settings.targets.firstResponse')}</TableCell>
                <TableCell>{t('sla.settings.targets.resolution')}</TableCell>
                <TableCell>{t('sla.settings.targets.aroundTheClock')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {policies.map((p) => {
                const d = drafts[p.priority] ?? toDraft(p)
                const errors = policyErrors[p.priority] ?? {}
                const priorityLabel = t(`sla.priority.${p.priority}`)
                const human = (text: string) => {
                  const m = minutesOf(text)
                  return m === null ? ' ' : formatSlaDuration(m, d.aroundTheClock, t)
                }
                return (
                  <TableRow key={p.priority}>
                    <TableCell component="th" scope="row" sx={{ fontWeight: 600 }}>{priorityLabel}</TableCell>
                    <TableCell sx={{ verticalAlign: 'top', minWidth: 170 }}>
                      <TextField
                        size="small" type="number" value={d.first} error={Boolean(errors.first)}
                        onChange={(e) => setDraft(p.priority, { first: e.target.value })}
                        helperText={errors.first ? t(errors.first) : human(d.first)}
                        slotProps={{ htmlInput: { min: 1, step: 1, 'aria-label': t('sla.settings.targets.firstResponseFor', { priority: priorityLabel }) } }}
                      />
                    </TableCell>
                    <TableCell sx={{ verticalAlign: 'top', minWidth: 170 }}>
                      <TextField
                        size="small" type="number" value={d.resolution} error={Boolean(errors.resolution)}
                        onChange={(e) => setDraft(p.priority, { resolution: e.target.value })}
                        helperText={errors.resolution ? t(errors.resolution) : human(d.resolution)}
                        slotProps={{ htmlInput: { min: 1, step: 1, 'aria-label': t('sla.settings.targets.resolutionFor', { priority: priorityLabel }) } }}
                      />
                    </TableCell>
                    <TableCell sx={{ verticalAlign: 'top' }}>
                      <Switch
                        checked={d.aroundTheClock}
                        onChange={(e) => setDraft(p.priority, { aroundTheClock: e.target.checked })}
                        slotProps={{ input: { 'aria-label': t('sla.settings.targets.aroundTheClockFor', { priority: priorityLabel }) } }}
                      />
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </TableContainer>
        <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('sla.settings.targets.unitHint')}</Typography>
        <Box>
          <Button variant="contained" disabled={!policiesDirty || policiesInvalid || policyBusy} onClick={savePolicies} startIcon={busyIcon(policyBusy)}>
            {t('sla.settings.targets.save')}
          </Button>
        </Box>
      </FormSection>

      <FormSection id="sla-business-hours" title={t('sla.settings.hours.title')} description={t('sla.settings.hours.description')}>
        {settingsError && <Alert severity="error">{settingsError}</Alert>}
        <Box>
          <Typography id="sla-days-label" variant="body2" sx={{ fontWeight: 600, mb: 0.75 }}>{t('sla.settings.hours.days')}</Typography>
          <ToggleButtonGroup
            size="small" color="primary" aria-labelledby="sla-days-label" value={settings.businessDays} sx={{ flexWrap: 'wrap' }}
            onChange={(_, value: BusinessDay[]) => setSettings({ ...settings, businessDays: DAYS.filter((d) => value.includes(d)) })}
          >
            {DAYS.map((d) => (
              <ToggleButton key={d} value={d} aria-label={t(`sla.day.long.${d}`)} sx={{ px: 1.75 }}>{t(`sla.day.short.${d}`)}</ToggleButton>
            ))}
          </ToggleButtonGroup>
          {settingsErrors.days && <Typography variant="caption" role="alert" sx={{ color: 'error.main', display: 'block', mt: 0.5 }}>{t(settingsErrors.days)}</Typography>}
        </Box>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <TextField
            size="small" type="time" label={t('sla.settings.hours.start')} value={settings.businessStart}
            error={Boolean(settingsErrors.hours)} onChange={(e) => setSettings({ ...settings, businessStart: e.target.value })}
            slotProps={{ inputLabel: { shrink: true } }} sx={{ width: 160 }}
          />
          <TextField
            size="small" type="time" label={t('sla.settings.hours.end')} value={settings.businessEnd}
            error={Boolean(settingsErrors.hours)} onChange={(e) => setSettings({ ...settings, businessEnd: e.target.value })}
            slotProps={{ inputLabel: { shrink: true } }} sx={{ width: 160 }}
            helperText={settingsErrors.hours ? t(settingsErrors.hours) : undefined}
          />
        </Box>
        <Box sx={{ maxWidth: 420 }}>
          <TimeZonePicker
            fullWidth label={t('sla.settings.hours.timeZone')} helperText={t('sla.settings.hours.timeZoneHint')}
            value={settings.timeZone || 'Asia/Kolkata'} error={Boolean(settingsErrors.zone)}
            onChange={(zone) => setSettings({ ...settings, timeZone: zone })}
          />
        </Box>
        <TextField
          size="small" label={t('sla.settings.hours.warnAt')} value={warnText} error={Boolean(settingsErrors.warn)}
          onChange={(e) => setWarnText(e.target.value)} type="number" sx={{ maxWidth: 240 }}
          helperText={settingsErrors.warn ? t(settingsErrors.warn) : t('sla.settings.hours.warnAtHint')}
          slotProps={{ htmlInput: { min: 1, max: 99, step: 1 }, input: { endAdornment: <Typography aria-hidden sx={{ color: 'text.secondary' }}>%</Typography> } }}
        />
        <Autocomplete
          multiple freeSolo autoSelect options={[] as string[]} value={settings.alertEmails}
          onChange={(_, values) => changeEmails(values as string[])}
          renderValue={(values, getItemProps) =>
            values.map((email, index) => {
              const { key, ...itemProps } = getItemProps({ index })
              return <Chip key={key} size="small" label={email} {...itemProps} />
            })}
          renderInput={(params) => (
            <TextField
              {...params} size="small" label={t('sla.settings.hours.alertEmails')} error={Boolean(emailError)}
              helperText={emailError ?? t('sla.settings.hours.alertEmailsHint', { max: MAX_EMAILS })}
            />
          )}
        />
        <Box>
          <Button variant="contained" disabled={!settingsDirty || settingsInvalid || settingsBusy} onClick={saveSettings} startIcon={busyIcon(settingsBusy)}>
            {t('sla.settings.hours.save')}
          </Button>
        </Box>
      </FormSection>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </Box>
  )
}
