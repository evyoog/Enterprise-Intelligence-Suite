import { useCallback, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { ProfileDto, ProfileSection } from '../../api/profileApi'
import {
  emptyProfileForm, isProfileDirty, normalizePhone, normalizeText, profileToForm, validateProfile,
  type ProfileErrors, type ProfileForm,
} from '../../utils/profileValidation'

/**
 * The editing state shared by the profile page and the admin "Edit profile" dialog: the draft,
 * the last saved values, client-side errors (shown once a field has been left or a save was
 * tried) and the server's field errors mapped back onto the same fields.
 */
export function useProfileFormState() {
  const { t } = useTranslation()
  const [saved, setSaved] = useState<ProfileForm>(emptyProfileForm)
  const [form, setForm] = useState<ProfileForm>(emptyProfileForm)
  const [touched, setTouched] = useState<Record<string, true>>({})
  const [submitAttempted, setSubmitAttempted] = useState(false)
  const [serverErrors, setServerErrors] = useState<Record<string, string>>({})
  const [nameEditable, setNameEditable] = useState(true)

  /** Starts (or restarts) from a record the server returned. */
  const load = useCallback((profile: ProfileDto) => {
    const next = profileToForm(profile)
    setSaved(next)
    setForm(next)
    setNameEditable(profile.account.nameEditable)
    setTouched({})
    setSubmitAttempted(false)
    setServerErrors({})
  }, [])

  const change = useCallback((section: ProfileSection, field: string, value: string) => {
    setForm((f) => ({ ...f, [section]: { ...f[section], [field]: value } }))
    setServerErrors((e) => {
      const key = `${section}.${field}`
      // The postal code rule depends on the country, so changing the country clears its error too.
      const clearPostal = key === 'address.country' && e['address.postalCode']
      if (!e[key] && !clearPostal) return e
      const { [key]: _gone, ...rest } = e
      void _gone
      if (clearPostal) delete rest['address.postalCode']
      return rest
    })
  }, [])

  /** Leaving a field: remember it was visited and tidy what was typed (spaces, phone separators). */
  const blur = useCallback((section: ProfileSection, field: string) => {
    const key = `${section}.${field}`
    setTouched((t0) => (t0[key] ? t0 : { ...t0, [key]: true }))
    setForm((f) => {
      const current = (f[section] as Record<string, string>)[field] ?? ''
      const isPhone = field === 'mobile' || field === 'alternatePhone'
      const tidy = isPhone ? normalizePhone(current) : normalizeText(current)
      return tidy === current ? f : { ...f, [section]: { ...f[section], [field]: tidy } }
    })
  }, [])

  const errors: ProfileErrors = useMemo(() => validateProfile(form, { nameEditable }), [form, nameEditable])
  const dirty = useMemo(() => isProfileDirty(form, saved), [form, saved])

  /** Messages for display: server messages first, then client errors for visited fields (all after a save attempt). */
  const messages = useMemo(() => {
    const out: Record<string, string> = { ...serverErrors }
    for (const [key, error] of Object.entries(errors)) {
      if (out[key]) continue
      if (submitAttempted || touched[key]) out[key] = t(`profile.errors.${error.code}`, { max: error.max })
    }
    return out
  }, [errors, serverErrors, touched, submitAttempted, t])

  const reset = useCallback(() => {
    setForm(saved)
    setTouched({})
    setSubmitAttempted(false)
    setServerErrors({})
  }, [saved])

  /** Marks a save as tried; returns the client errors (empty means go ahead). */
  const attemptSubmit = useCallback(() => {
    setSubmitAttempted(true)
    setServerErrors({})
    return validateProfile(form, { nameEditable })
  }, [form, nameEditable])

  return {
    form, saved, load, change, blur, dirty, errors, messages, serverErrors, setServerErrors, attemptSubmit, reset, nameEditable,
  }
}
