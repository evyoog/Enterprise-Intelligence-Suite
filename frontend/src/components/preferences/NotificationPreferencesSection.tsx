import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Button, Skeleton, Switch, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { notificationsApi, type NotificationCategory } from '../../api/notificationsApi'
import { PreferenceRow, PreferenceSection } from './PreferenceLayout'

/**
 * C67: the notification categories the page offers, each a group of the
 * backend's existing categories (`NotificationCategory`). Support ticket
 * replies are sent as SYSTEM, so Support and Platform share one switch.
 */
const GROUPS: { key: 'products' | 'billing' | 'platform' | 'security'; categories: NotificationCategory[] }[] = [
  { key: 'products', categories: ['ORDER'] },
  { key: 'billing', categories: ['BILLING', 'SUBSCRIPTION'] },
  { key: 'platform', categories: ['SYSTEM', 'ORGANIZATION'] },
  { key: 'security', categories: ['SECURITY', 'PRIVILEGED_ACCESS'] },
]
const ALL: NotificationCategory[] = GROUPS.flatMap((g) => g.categories)

/**
 * Reuses the existing email opt-out (`/me/notifications/preferences`, the same
 * one the bell menu edits). In-app notifications are always recorded by the
 * backend, so that channel is shown as always on rather than as a switch.
 * Changes save immediately, like the bell menu.
 */
export function NotificationPreferencesSection() {
  const { t } = useTranslation()
  const [disabled, setDisabled] = useState<NotificationCategory[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [saveError, setSaveError] = useState<string | null>(null)

  const load = useCallback(() => notificationsApi.getPreferences()
    .then((p) => { setDisabled(p.emailDisabledCategories); setLoadError(null) })
    .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('preferences.notifications.loadError'))), [t])
  useEffect(() => { void load() }, [load])

  const save = (next: NotificationCategory[]) => {
    const previous = disabled
    setDisabled(next)
    setSaveError(null)
    notificationsApi.updatePreferences({ emailDisabledCategories: next })
      .then((p) => setDisabled(p.emailDisabledCategories))
      .catch((e) => { setDisabled(previous); setSaveError(e instanceof ApiError ? e.message : t('preferences.notifications.saveError')) })
  }

  const emailOn = disabled !== null && !ALL.every((c) => disabled.includes(c))
  const groupOn = (categories: NotificationCategory[]) => disabled !== null && categories.every((c) => !disabled.includes(c))
  const setGroup = (categories: NotificationCategory[], on: boolean) => {
    if (!disabled) return
    const rest = disabled.filter((c) => !categories.includes(c))
    save(on ? rest : [...rest, ...categories])
  }

  return (
    <PreferenceSection id="notifications" title={t('preferences.notifications.title')} description={t('preferences.notifications.description')}>
      <PreferenceRow label={t('preferences.notifications.inApp')} hint={t('preferences.notifications.inAppHint')}
        control={<Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 600 }}>{t('preferences.notifications.alwaysOn')}</Typography>} />

      {loadError && (
        <Alert severity="error" sx={{ my: 1.5 }} action={<Button onClick={() => void load()}>{t('ui.retry')}</Button>}>{loadError}</Alert>
      )}
      {!loadError && disabled === null && <Skeleton variant="rounded" height={180} sx={{ my: 1.5 }} />}

      {disabled !== null && (
        <>
          <PreferenceRow label={t('preferences.notifications.email')} hint={t('preferences.notifications.emailHint')} labelId="notif-email-label"
            control={<Switch checked={emailOn} onChange={(e) => save(e.target.checked ? [] : ALL)} slotProps={{ input: { 'aria-labelledby': 'notif-email-label' } }} />} />
          {GROUPS.map((group) => {
            const on = groupOn(group.categories)
            const labelId = `notif-${group.key}-label`
            return (
              <PreferenceRow key={group.key} labelId={labelId} label={t(`preferences.notifications.group.${group.key}`)}
                hint={group.key === 'security' && !on && emailOn
                  ? <Typography component="span" variant="body2" sx={{ color: 'warning.main' }}>{t('preferences.notifications.securityOff')}</Typography>
                  : t(`preferences.notifications.group.${group.key}Hint`)}
                control={<Switch checked={on && emailOn} disabled={!emailOn} onChange={(e) => setGroup(group.categories, e.target.checked)}
                  slotProps={{ input: { 'aria-labelledby': labelId } }} />} />
            )
          })}
        </>
      )}
      {saveError && <Alert severity="error" sx={{ mt: 1.5 }}>{saveError}</Alert>}
    </PreferenceSection>
  )
}
