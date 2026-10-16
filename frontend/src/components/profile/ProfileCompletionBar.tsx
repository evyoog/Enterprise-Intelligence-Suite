import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useLocation } from 'react-router-dom'
import { Box, Button, IconButton, LinearProgress, Typography } from '@mui/material'
import { alpha } from '@mui/material/styles'
import { X } from 'lucide-react'
import { useAuth } from '../../auth/AuthProvider'
import { useProfileCompletion } from './ProfileCompletionContext'

export const PROFILE_PATH = '/account/profile'

const dismissKey = (username: string) => `eis.profileBar.dismissed.${username}`

function readDismissed(username: string): boolean {
  try { return sessionStorage.getItem(dismissKey(username)) === '1' } catch { return false }
}

/**
 * The global "profile filling" bar (C107), shown by the app shell under the top bar on every
 * page while the person has a profile that is less than 100% complete. Hidden on the profile
 * page itself and for a person who has no customer record (a platform administrator who has not
 * created one: that is optional and never nagged). Dismissing hides it for this browser session
 * (sessionStorage, per user), so it returns at the next sign-in.
 */
export function ProfileCompletionBar() {
  const { t } = useTranslation()
  const { pathname } = useLocation()
  const { user } = useAuth()
  const { state } = useProfileCompletion()
  const username = user?.username ?? ''
  const [dismissedFor, setDismissedFor] = useState<string | null>(() => (readDismissed(username) ? username : null))
  const dismissed = dismissedFor === username || readDismissed(username)

  if (state.status !== 'ready' || !state.exists || state.percent >= 100) return null
  if (pathname === PROFILE_PATH || dismissed) return null

  const dismiss = () => {
    try { sessionStorage.setItem(dismissKey(username), '1') } catch { /* private mode: dismissed for this view only */ }
    setDismissedFor(username)
  }
  const text = t('profile.bar.message', { percent: state.percent })

  return (
    <Box component="aside" aria-label={t('profile.bar.label')} data-testid="profile-completion-bar"
      sx={(theme) => ({
        display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: { xs: 1, sm: 2 }, px: { xs: 2, sm: 3, md: 4 }, py: 1,
        borderBottom: '1px solid', borderColor: 'divider',
        bgcolor: alpha(theme.palette.primary.main, theme.palette.mode === 'dark' ? 0.14 : 0.06),
      })}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flex: '1 1 260px', minWidth: 0 }}>
        <LinearProgress variant="determinate" value={state.percent} aria-label={text}
          sx={{ width: { xs: 72, sm: 140 }, height: 8, borderRadius: 4, flexShrink: 0, bgcolor: 'action.selected' }} />
        <Typography variant="body2" sx={{ fontWeight: 600 }}>{text}</Typography>
      </Box>
      <Button component={RouterLink} to={PROFILE_PATH} size="small" variant="contained" disableElevation>
        {t('profile.bar.complete')}
      </Button>
      <IconButton size="small" onClick={dismiss} aria-label={t('profile.bar.dismiss')}>
        <X size={16} />
      </IconButton>
    </Box>
  )
}
