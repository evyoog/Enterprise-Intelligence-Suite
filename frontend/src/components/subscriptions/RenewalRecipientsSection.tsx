import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Autocomplete, Box, Button, Checkbox, Chip, FormControlLabel, IconButton, Skeleton, TextField, Typography } from '@mui/material'
import { BellRing, X } from 'lucide-react'
import { ApiError } from '../../api/client'
import { reminderRecipientsApi, type ReminderRecipient, type ReminderRecipientCandidate, type ReminderRecipients } from '../../api/renewalsApi'
import { SettingsSection } from '../settings/SettingsSection'

const sameList = (a: ReminderRecipient[], b: ReminderRecipient[]) =>
  a.length === b.length && a.every((p, i) => p.customerId === b[i].customerId && p.emailEnabled === b[i].emailEnabled && p.inAppEnabled === b[i].inAppEnabled)

/**
 * C100 RR-2 B / RR-5: who gets the renewal reminders of the organization's subscriptions. Organization admins and people
 * with Manage billing always do (shown read-only); an administrator adds any other active members and picks email,
 * in-app or both for each. Needs Manage billing (the API refuses everyone else, and the section then stays hidden).
 */
export function RenewalRecipientsSection() {
  const { t } = useTranslation()
  const [saved, setSaved] = useState<ReminderRecipients | null>(null)
  const [added, setAdded] = useState<ReminderRecipient[]>([])
  const [members, setMembers] = useState<ReminderRecipientCandidate[]>([])
  const [hidden, setHidden] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [busy, setBusy] = useState(false)

  const apply = (r: ReminderRecipients) => { setSaved(r); setAdded(r.added) }

  const load = useCallback(() => reminderRecipientsApi.get()
    .then((r) => { apply(r); setLoadError(null) })
    .catch((e) => {
      if (e instanceof ApiError && (e.status === 403 || e.status === 404)) setHidden(true)
      else setLoadError(e instanceof ApiError ? e.message : t('subscriptions.recipients.loadError'))
    }), [t])
  useEffect(() => { void load() }, [load])
  useEffect(() => { reminderRecipientsApi.candidates().then(setMembers).catch(() => setMembers([])) }, [])

  const options = useMemo(() => {
    const taken = new Set([...(saved?.defaults ?? []), ...added].map((p) => p.customerId))
    return members.filter((m) => !taken.has(m.customerId))
  }, [members, saved, added])

  if (hidden) return null

  const update = (customerId: number, patch: Partial<Pick<ReminderRecipient, 'emailEnabled' | 'inAppEnabled'>>) => {
    setSuccess(false)
    setAdded((prev) => prev.map((p) => (p.customerId === customerId ? { ...p, ...patch } : p)))
  }
  const invalid = added.find((p) => !p.emailEnabled && !p.inAppEnabled)
  const dirty = saved !== null && !sameList(saved.added, added)

  const save = () => {
    setBusy(true)
    setSaveError(null)
    setSuccess(false)
    reminderRecipientsApi.save({ recipients: added.map((p) => ({ customerId: p.customerId, email: p.emailEnabled, inApp: p.inAppEnabled })) })
      .then((r) => { apply(r); setSuccess(true) })
      .catch((e) => setSaveError(e instanceof ApiError ? e.message : t('subscriptions.recipients.saveError')))
      .finally(() => setBusy(false))
  }

  return (
    <Box sx={{ mt: 3 }}>
      <SettingsSection id="renewal-recipients" icon={BellRing} accent="amber" title={t('subscriptions.recipients.title')}
        description={t('subscriptions.recipients.description')}>
        {loadError && <Alert severity="error">{loadError}</Alert>}
        {saved === null && !loadError && <Skeleton variant="rounded" height={80} />}
        {saved !== null && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            <Box component="section" aria-labelledby="renewal-recipients-defaults">
              <Typography id="renewal-recipients-defaults" component="h3" variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                {t('subscriptions.recipients.defaultsTitle')}
              </Typography>
              <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'grid', gap: 1 }}>
                {saved.defaults.map((p) => (
                  <Box component="li" key={p.customerId} sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                    <Box sx={{ minWidth: 0, flex: '1 1 220px' }}>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>{p.name}</Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>{p.email}</Typography>
                    </Box>
                    <Chip size="small" variant="outlined" label={t(`subscriptions.recipients.reason.${p.reason}`)} />
                  </Box>
                ))}
              </Box>
            </Box>

            <Box component="section" aria-labelledby="renewal-recipients-added">
              <Typography id="renewal-recipients-added" component="h3" variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                {t('subscriptions.recipients.addedTitle')}
              </Typography>
              {added.length === 0 ? (
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('subscriptions.recipients.none')}</Typography>
              ) : (
                <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'grid', gap: 1 }}>
                  {added.map((p) => (
                    <Box component="li" key={p.customerId} sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                      <Box sx={{ minWidth: 0, flex: '1 1 220px' }}>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>{p.name}</Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>{p.email}</Typography>
                      </Box>
                      <FormControlLabel label={t('subscriptions.recipients.email')}
                        control={<Checkbox size="small" checked={p.emailEnabled} onChange={(_, v) => update(p.customerId, { emailEnabled: v })}
                          slotProps={{ input: { 'aria-label': t('subscriptions.recipients.emailFor', { name: p.name }) } }} />} />
                      <FormControlLabel label={t('subscriptions.recipients.inApp')}
                        control={<Checkbox size="small" checked={p.inAppEnabled} onChange={(_, v) => update(p.customerId, { inAppEnabled: v })}
                          slotProps={{ input: { 'aria-label': t('subscriptions.recipients.inAppFor', { name: p.name }) } }} />} />
                      <IconButton size="small" aria-label={t('subscriptions.recipients.remove', { name: p.name })}
                        onClick={() => { setSuccess(false); setAdded((prev) => prev.filter((x) => x.customerId !== p.customerId)) }}>
                        <X size={16} />
                      </IconButton>
                    </Box>
                  ))}
                </Box>
              )}
              {invalid && <Alert severity="warning" sx={{ mt: 1.5 }}>{t('subscriptions.recipients.needChannel', { name: invalid.name })}</Alert>}

              <Autocomplete<ReminderRecipientCandidate> sx={{ mt: 2, maxWidth: 420 }} size="small" options={options} value={null} blurOnSelect clearOnBlur
                getOptionLabel={(m) => `${m.name}${m.email ? ` (${m.email})` : ''}`}
                isOptionEqualToValue={(a, b) => a.customerId === b.customerId}
                noOptionsText={t('subscriptions.recipients.noOptions')}
                onChange={(_, m) => {
                  if (!m) return
                  setSuccess(false)
                  setAdded((prev) => [...prev, {
                    customerId: m.customerId, name: m.name, email: m.email, reason: 'ADDED', emailEnabled: true, inAppEnabled: true,
                  }])
                }}
                renderInput={(params) => <TextField {...params} label={t('subscriptions.recipients.addPerson')} />} />
            </Box>

            {saveError && <Alert severity="error" onClose={() => setSaveError(null)}>{saveError}</Alert>}
            {success && <Alert severity="success" onClose={() => setSuccess(false)}>{t('subscriptions.recipients.saved')}</Alert>}
            <Box>
              <Button variant="contained" disabled={busy || !dirty || Boolean(invalid)} onClick={save}>{t('subscriptions.recipients.save')}</Button>
            </Box>
          </Box>
        )}
      </SettingsSection>
    </Box>
  )
}
