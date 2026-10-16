import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { ApiError } from '../../api/client'
import { profileApi, profileFieldErrors, type ProfileDto, type ProfileSection } from '../../api/profileApi'
import { PROFILE_SECTIONS, formToRequest, sectionsWithErrors, type ProfileTab } from '../../utils/profileValidation'
import { displayNameOf } from '../../utils/profileDisplay'
import { ProfileForm } from './ProfileForm'
import { useProfileFormState } from './useProfileFormState'

/**
 * C107: a platform administrator's "Edit profile" for any person's record, opened from the lists
 * of people (organization members, individuals). Same form as My profile, without the photo and
 * the Account tab; reads and writes through the admin endpoints.
 */
export function ProfileEditDialog({ customerId, name, onClose, onSaved }: {
  customerId: number | null
  /** The person's name as the list shows it, for the title until the record is loaded. */
  name?: string
  onClose: () => void
  onSaved?: (profile: ProfileDto) => void
}) {
  if (customerId === null) return null
  // Keyed so the form starts from the person's current values each time it opens.
  return <EditForm key={customerId} customerId={customerId} name={name} onClose={onClose} onSaved={onSaved} />
}

function EditForm({ customerId, name, onClose, onSaved }: {
  customerId: number; name?: string; onClose: () => void; onSaved?: (profile: ProfileDto) => void
}) {
  const { t } = useTranslation()
  const fs = useProfileFormState()
  const [profile, setProfile] = useState<ProfileDto | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [tab, setTab] = useState<ProfileTab>('personal')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)
  const { load } = fs

  useEffect(() => {
    let alive = true
    profileApi.getForCustomer(customerId)
      .then((p) => { if (alive) { setProfile(p); load(p) } })
      .catch((e) => { if (alive) setLoadError(e instanceof ApiError ? e.message : '') })
    return () => { alive = false }
  }, [customerId, load])

  const showTabFor = (keys: string[]) => {
    const found = sectionsWithErrors(Object.fromEntries(keys.map((k) => [k, true])))
      .filter((s): s is ProfileSection => (PROFILE_SECTIONS as readonly string[]).includes(s))
    if (found.length > 0 && !found.includes(tab as ProfileSection)) setTab(found[0])
  }

  const save = async () => {
    setSaveError(null)
    const errors = Object.keys(fs.attemptSubmit())
    if (errors.length > 0) { showTabFor(errors); return }
    setSaving(true)
    try {
      const saved = await profileApi.updateForCustomer(customerId, formToRequest(fs.form))
      onSaved?.(saved)
      onClose()
    } catch (e) {
      const fieldErrors = profileFieldErrors(e)
      if (fieldErrors) {
        fs.setServerErrors(fieldErrors)
        setSaveError(t('profile.fixErrors'))
        showTabFor(Object.keys(fieldErrors))
      } else if (e instanceof ApiError && e.status === 502) {
        setSaveError(t('profile.errors.identityProvider'))
      } else {
        setSaveError(e instanceof ApiError ? e.message : t('profile.saveError'))
      }
    } finally {
      setSaving(false)
    }
  }

  const title = profile ? displayNameOf(profile.personal, profile.account.email) : name ?? ''

  return (
    <Dialog open onClose={saving ? undefined : onClose} fullWidth maxWidth="md" aria-labelledby="profile-edit-title">
      <DialogTitle id="profile-edit-title">{t('profile.admin.title', { name: title })}</DialogTitle>
      <DialogContent>
        {loadError !== null && <Alert severity="error">{loadError || t('profile.loadError')}</Alert>}
        {loadError === null && !profile && <CircularProgress size={24} aria-label={t('common.loading')} />}
        {profile && (
          <>
            <Alert severity="info" sx={{ mb: 2 }}>{t('profile.admin.hint', { email: profile.account.email })}</Alert>
            {saveError && <Alert severity="error" sx={{ mb: 2 }} role="alert">{saveError}</Alert>}
            <ProfileForm form={fs.form} messages={fs.messages} onChange={fs.change} onBlur={fs.blur} tab={tab} onTab={setTab}
              nameEditable={fs.nameEditable} disabled={saving} />
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={saving}>{t('profile.admin.cancel')}</Button>
        <Button variant="contained" onClick={save} disabled={saving || !profile || !fs.dirty}>
          {saving ? t('profile.saveBar.saving') : t('profile.saveBar.save')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
