import { createContext, useContext } from 'react'
import type { ProfileCompletionStatus, ProfileDto } from '../../api/profileApi'

/** Where the shared "profile filling" status stands (feeds the global bar and the top-bar avatar). */
export interface ProfileCompletionData extends ProfileCompletionStatus {
  /** `loading` until the first answer; `error` when it could not be read (the bar then stays hidden). */
  status: 'loading' | 'ready' | 'error'
  /** Changes whenever the photo does, so the avatar knows to reload it. */
  photoStamp: string | null
}

export interface ProfileCompletionState extends ProfileCompletionData {
  /** An object URL for the signed-in person's photo, or null. */
  photoUrl: string | null
}

export interface ProfileCompletionValue {
  state: ProfileCompletionState
  /** Reads GET /me/profile/completion again. */
  refresh: () => Promise<void>
  /** Takes the completion (and photo stamp) from a profile the page has just loaded or saved. */
  report: (profile: ProfileDto) => void
}

export const INITIAL_COMPLETION_DATA: ProfileCompletionData = {
  status: 'loading', exists: false, percent: 0, filled: 0, total: 11, missing: [], photoStamp: null,
}

/** Outside the provider (a page rendered on its own) everything is inert. */
export const ProfileCompletionContext = createContext<ProfileCompletionValue>({
  state: { ...INITIAL_COMPLETION_DATA, photoUrl: null },
  refresh: async () => {},
  report: () => {},
})

export function useProfileCompletion(): ProfileCompletionValue {
  return useContext(ProfileCompletionContext)
}
