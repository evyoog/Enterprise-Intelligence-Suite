import { useEffect, useState } from 'react'
import { profileApi } from '../../api/profileApi'

/**
 * The signed-in person's photo as an object URL (the photo endpoint needs the bearer token, so
 * an <img> cannot load it directly). Reloads when `stamp` changes (the photo was replaced) and
 * revokes the URL it created. Returns null while loading, when there is no photo, or on failure.
 */
export function useProfilePhotoUrl(present: boolean, stamp: string | null): string | null {
  const key = `${stamp ?? ''}`
  const [loaded, setLoaded] = useState<{ key: string; url: string | null }>({ key: '', url: null })

  useEffect(() => {
    if (!present) return
    let alive = true
    let created: string | null = null
    profileApi.photo()
      .then((blob) => {
        if (!alive) return
        created = blob ? URL.createObjectURL(blob) : null
        setLoaded({ key, url: created })
      })
      .catch(() => { if (alive) setLoaded({ key, url: null }) })
    return () => {
      alive = false
      if (created) URL.revokeObjectURL(created)
    }
  }, [present, key])

  return present && loaded.key === key ? loaded.url : null
}
