import { useCallback, useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, CircularProgress, Paper, Snackbar, Typography } from '@mui/material'
import { UserRound } from 'lucide-react'
import { ApiError } from '../api/client'
import { profileApi, profileFieldErrors, type ProfileDto, type ProfileSection } from '../api/profileApi'
import { PageHeader } from '../components/layout/PageHeader'
import { useTabParam } from '../components/layout/useTabParam'
import { ErrorState } from '../components/ui/ErrorState'
import { ConfirmDialog } from '../components/ui/ConfirmDialog'
import { ProfileForm } from '../components/profile/ProfileForm'
import { ProfileHeaderCard } from '../components/profile/ProfileHeaderCard'
import { useProfileCompletion } from '../components/profile/ProfileCompletionContext'
import { useProfileFormState } from '../components/profile/useProfileFormState'
import { useProfilePhotoUrl } from '../components/profile/useProfilePhotoUrl'
import { useUnsavedChangesGuard } from '../components/profile/useUnsavedChangesGuard'
import {
  COMPLETION_ITEM_SECTION, PROFILE_SECTIONS, PROFILE_TABS, formToRequest, profileFieldId, sectionsWithErrors, type ProfileTab,
} from '../utils/profileValidation'
import { PhotoError, prepareProfilePhoto } from '../utils/profilePhoto'

/**
 * "/account/profile" — My profile (C107), for every signed-in person (organization admins,
 * platform administrators, members and individuals). A header card with the photo and the
 * completion status, then one form over five tabs (Personal, Work, Address, Online, and a
 * read-only Account tab) with one Save / Discard bar. Language and time zone stay on
 * Preferences, and the e-mail (the login name) is read-only. A platform administrator without a
 * customer record sees an optional "Create my profile" banner and is never forced to fill one in.
 */
export function ProfilePage() {
  const { t } = useTranslation()
  const completion = useProfileCompletion()
  const [profile, setProfile] = useState<ProfileDto | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [reloadTick, setReloadTick] = useState(0)
  const fs = useProfileFormState()
  const [tab, setTab] = useTabParam(PROFILE_TABS, 'personal')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [photoBusy, setPhotoBusy] = useState(false)
  const [photoError, setPhotoError] = useState<string | null>(null)
  const pendingFocus = useRef<string | null>(null)
  const guard = useUnsavedChangesGuard(fs.dirty)
  const photoUrl = useProfilePhotoUrl(Boolean(profile?.photo.present), profile?.photo.updatedAt ?? null)

  const { load } = fs
  const { report } = completion
  useEffect(() => {
    let alive = true
    profileApi.get()
      .then((p) => {
        if (!alive) return
        setProfile(p)
        load(p)
        report(p)
        setLoadError(null)
      })
      .catch((e) => { if (alive) setLoadError(e instanceof ApiError ? e.message : '') })
    return () => { alive = false }
  }, [load, report, reloadTick])

  const focusField = (id: string) => document.getElementById(id)?.focus()
  useEffect(() => {
    if (pendingFocus.current) {
      focusField(pendingFocus.current)
      pendingFocus.current = null
    }
  }, [tab])

  const jump = useCallback((item: string) => {
    if (item === 'photo') {
      focusField('profile-photo-change')
      return
    }
    const section = COMPLETION_ITEM_SECTION[item]
    if (!section) return
    const id = profileFieldId(section, item)
    if (section === tab) focusField(id)
    else { pendingFocus.current = id; setTab(section) }
  }, [tab, setTab])

  const showTabWithError = (keys: string[], firstKey?: string) => {
    const sections = sectionsWithErrors(Object.fromEntries(keys.map((k) => [k, true])))
      .filter((s): s is ProfileSection => (PROFILE_SECTIONS as readonly string[]).includes(s))
    if (sections.length === 0) return
    const target: ProfileTab = sections.includes(tab as ProfileSection) ? tab : sections[0]
    if (target !== tab) setTab(target)
    const key = firstKey ?? keys.find((k) => k.startsWith(`${target}.`))
    if (key) {
      const [section, field] = key.split('.')
      pendingFocus.current = profileFieldId(section as ProfileSection, field)
      // Same tab: the effect on `tab` will not run, so focus now.
      if (target === tab) { focusField(pendingFocus.current); pendingFocus.current = null }
    }
  }

  const save = async () => {
    setSaveError(null)
    const errors = fs.attemptSubmit()
    const keys = Object.keys(errors)
    if (keys.length > 0) {
      showTabWithError(keys)
      return
    }
    setSaving(true)
    try {
      const wasMissing = profile ? !profile.exists : false
      const saved = await profileApi.update(formToRequest(fs.form))
      setProfile(saved)
      fs.load(saved)
      completion.report(saved)
      setToast(t(wasMissing ? 'profile.created' : 'profile.saved'))
    } catch (e) {
      const fieldErrors = profileFieldErrors(e)
      if (fieldErrors) {
        fs.setServerErrors(fieldErrors)
        setSaveError(t('profile.fixErrors'))
        showTabWithError(Object.keys(fieldErrors))
      } else if (e instanceof ApiError && e.status === 502) {
        setSaveError(t('profile.errors.identityProvider'))
      } else {
        setSaveError(e instanceof ApiError ? e.message : t('profile.saveError'))
      }
    } finally {
      setSaving(false)
    }
  }

  const changePhoto = async (file: File) => {
    setPhotoBusy(true)
    setPhotoError(null)
    try {
      const { blob, filename } = await prepareProfilePhoto(file)
      const updated = await profileApi.uploadPhoto(blob, filename)
      // Only the photo-related parts change: the person's unsaved edits stay as they are.
      setProfile(updated)
      completion.report(updated)
      setToast(t('profile.photo.saved'))
    } catch (e) {
      setPhotoError(e instanceof PhotoError ? t(`profile.photo.errors.${e.code}`)
        : e instanceof ApiError ? e.message : t('profile.photo.errors.failed'))
    } finally {
      setPhotoBusy(false)
    }
  }

  const removePhoto = async () => {
    setPhotoBusy(true)
    setPhotoError(null)
    try {
      const updated = await profileApi.removePhoto()
      setProfile(updated)
      completion.report(updated)
      setToast(t('profile.photo.removed'))
    } catch (e) {
      setPhotoError(e instanceof ApiError ? e.message : t('profile.photo.errors.failed'))
    } finally {
      setPhotoBusy(false)
    }
  }

  const header = <PageHeader area="account" icon={UserRound} title={t('profile.title')} subtitle={t('profile.subtitle')} />

  if (loadError !== null) {
    return (
      <Box sx={{ pb: 4 }}>
        {header}
        <ErrorState title={t('profile.loadError')} message={loadError || undefined} onRetry={() => { setLoadError(null); setReloadTick((n) => n + 1) }} />
      </Box>
    )
  }
  if (!profile) {
    return (
      <Box sx={{ pb: 4 }}>
        {header}
        <CircularProgress size={26} aria-label={t('common.loading')} />
      </Box>
    )
  }

  return (
    <Box sx={{ pb: 4 }}>
      {header}

      {!profile.exists && (
        <Alert severity="info" sx={{ mb: 3, flexWrap: { xs: 'wrap', sm: 'nowrap' }, '& .MuiAlert-message': { flex: 1 },
          '& .MuiAlert-action': { ml: { xs: 4.5, sm: 'auto' }, pl: { xs: 0, sm: 2 }, mt: { xs: 0.5, sm: 0 } } }}
          action={<Button color="inherit" size="small" onClick={save} disabled={saving}>{t('profile.createAction')}</Button>}>
          <Typography component="span" sx={{ fontWeight: 600 }}>{t('profile.notCreated.title')}</Typography>{' '}
          {t('profile.notCreated.body')}
        </Alert>
      )}

      <ProfileHeaderCard profile={profile} photoUrl={photoUrl} photoBusy={photoBusy} photoError={photoError}
        onPickPhoto={changePhoto} onRemovePhoto={removePhoto} onJump={jump} />

      <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
        {saveError && <Alert severity="error" sx={{ mb: 2 }} role="alert">{saveError}</Alert>}
        <ProfileForm form={fs.form} messages={fs.messages} onChange={fs.change} onBlur={fs.blur} tab={tab} onTab={setTab}
          nameEditable={fs.nameEditable} account={profile.account} disabled={saving} />
      </Paper>

      {fs.dirty && (
        <Paper role="region" aria-label={t('profile.saveBar.label')} elevation={6}
          sx={{ position: 'sticky', bottom: 16, zIndex: 5, mt: 3, px: { xs: 2, sm: 3 }, py: 1.5, borderRadius: 3, border: '1px solid', borderColor: 'divider',
            display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>{t('profile.saveBar.unsaved')}</Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button onClick={() => { fs.reset(); setSaveError(null) }} disabled={saving} color="inherit">{t('profile.saveBar.discard')}</Button>
            <Button variant="contained" onClick={save} disabled={saving}
              startIcon={saving ? <CircularProgress size={14} color="inherit" /> : undefined}>
              {saving ? t('profile.saveBar.saving') : t(profile.exists ? 'profile.saveBar.save' : 'profile.createAction')}
            </Button>
          </Box>
        </Paper>
      )}

      <ConfirmDialog open={guard.pendingPath !== null} title={t('profile.leave.title')} body={t('profile.leave.body')}
        confirmLabel={t('profile.leave.confirm')} onConfirm={guard.leave} onClose={guard.stay} />

      <Snackbar open={toast !== null} autoHideDuration={4000} onClose={() => setToast(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert severity="success" variant="filled" onClose={() => setToast(null)} role="status">{toast}</Alert>
      </Snackbar>
    </Box>
  )
}
