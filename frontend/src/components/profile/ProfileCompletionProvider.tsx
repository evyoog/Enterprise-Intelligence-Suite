import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useAuth } from '../../auth/AuthProvider'
import { profileApi, type ProfileDto } from '../../api/profileApi'
import {
  INITIAL_COMPLETION_DATA, ProfileCompletionContext, type ProfileCompletionData, type ProfileCompletionValue,
} from './ProfileCompletionContext'
import { useProfilePhotoUrl } from './useProfilePhotoUrl'

/**
 * Holds the signed-in person's profile completion for the whole app shell: read once on sign-in
 * (GET /me/profile/completion, cheap, no polling) and updated by the profile page after a save.
 */
export function ProfileCompletionProvider({ children }: { children: ReactNode }) {
  const auth = useAuth()
  const username = auth.user?.username
  const [raw, setRaw] = useState<ProfileCompletionData>(INITIAL_COMPLETION_DATA)

  const refresh = useCallback(() => profileApi.completion().then(
    (c) => setRaw((prev) => ({ ...prev, ...c, status: 'ready' })),
    () => setRaw((prev) => (prev.status === 'ready' ? prev : { ...prev, status: 'error' })),
  ), [])

  // Read once per signed-in person; the profile page reports later changes itself (no polling).
  useEffect(() => {
    let alive = true
    profileApi.completion().then(
      (c) => { if (alive) setRaw((prev) => ({ ...prev, ...c, status: 'ready' })) },
      () => { if (alive) setRaw((prev) => (prev.status === 'ready' ? prev : { ...prev, status: 'error' })) },
    )
    return () => { alive = false }
  }, [username])

  const report = useCallback((profile: ProfileDto) => {
    setRaw({ status: 'ready', exists: profile.exists, ...profile.completion, photoStamp: profile.photo.updatedAt })
  }, [])

  const hasPhoto = raw.status === 'ready' && raw.exists && !raw.missing.includes('photo')
  const photoUrl = useProfilePhotoUrl(hasPhoto, raw.photoStamp)

  const value = useMemo<ProfileCompletionValue>(
    () => ({ state: { ...raw, photoUrl }, refresh, report }),
    [raw, photoUrl, refresh, report],
  )
  return <ProfileCompletionContext.Provider value={value}>{children}</ProfileCompletionContext.Provider>
}
