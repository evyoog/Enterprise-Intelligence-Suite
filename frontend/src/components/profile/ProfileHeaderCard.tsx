import { useId, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Avatar, Box, Button, Chip, CircularProgress, Collapse, LinearProgress, Paper, Typography } from '@mui/material'
import { Camera, ChevronDown, ChevronUp, Trash2 } from 'lucide-react'
import type { ProfileDto } from '../../api/profileApi'
import { displayNameOf, initialsOf } from '../../utils/profileDisplay'
import { PHOTO_ACCEPTED_TYPES } from '../../utils/profilePhoto'

/**
 * The top of the profile page (C107): photo with change / remove, the person's display name (the
 * preferred name when set), role and organization, and the completion bar with an expandable list
 * of what is still missing. Each missing item jumps to the tab (and field) that fills it.
 */
export function ProfileHeaderCard({ profile, photoUrl, photoBusy, photoError, onPickPhoto, onRemovePhoto, onJump }: {
  profile: ProfileDto
  photoUrl: string | null
  photoBusy: boolean
  photoError: string | null
  onPickPhoto: (file: File) => void
  onRemovePhoto: () => void
  onJump: (item: string) => void
}) {
  const { t } = useTranslation()
  const fileRef = useRef<HTMLInputElement>(null)
  const [open, setOpen] = useState(false)
  const listId = useId()
  const { account, personal, completion } = profile
  const displayName = displayNameOf(personal, account.email)
  const hasPhoto = profile.photo.present
  const complete = completion.missing.length === 0
  const label = t('profile.completion.summary', { filled: completion.filled, total: completion.total })

  return (
    <Paper variant="outlined" component="section" aria-label={t('profile.header.label')} sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3, mb: 3 }}>
      <Box sx={{ display: 'flex', gap: { xs: 2, sm: 3 }, flexDirection: { xs: 'column', sm: 'row' }, alignItems: { xs: 'flex-start', sm: 'center' } }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1, flexShrink: 0 }}>
          <Box sx={{ position: 'relative' }}>
            <Avatar src={photoUrl ?? undefined} alt={hasPhoto ? t('profile.photo.alt', { name: displayName }) : ''}
              sx={{ width: 96, height: 96, fontSize: 34, fontWeight: 600, bgcolor: 'primary.main', color: 'primary.contrastText' }}>
              {initialsOf(personal.firstName, personal.lastName, account.email)}
            </Avatar>
            {photoBusy && (
              <Box sx={{ position: 'absolute', inset: 0, display: 'grid', placeItems: 'center', borderRadius: '50%', bgcolor: 'rgba(0,0,0,0.4)' }}>
                <CircularProgress size={28} color="inherit" sx={{ color: '#fff' }} aria-label={t('profile.photo.uploading')} />
              </Box>
            )}
          </Box>
          <input ref={fileRef} id="profile-photo-input" type="file" hidden accept={PHOTO_ACCEPTED_TYPES.join(',')}
            data-testid="profile-photo-input" aria-label={t('profile.photo.input')}
            onChange={(e) => {
              const file = e.target.files?.[0]
              e.target.value = ''
              if (file) onPickPhoto(file)
            }} />
          <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', justifyContent: 'center' }}>
            <Button id="profile-photo-change" size="small" startIcon={<Camera size={15} />} disabled={!profile.exists || photoBusy}
              onClick={() => fileRef.current?.click()}>
              {t(hasPhoto ? 'profile.photo.change' : 'profile.photo.add')}
            </Button>
            {hasPhoto && (
              <Button size="small" color="inherit" startIcon={<Trash2 size={15} />} disabled={photoBusy} onClick={onRemovePhoto}
                sx={{ color: 'text.secondary' }}>
                {t('profile.photo.remove')}
              </Button>
            )}
          </Box>
        </Box>

        <Box sx={{ minWidth: 0, flexGrow: 1, width: '100%' }}>
          <Typography component="h2" variant="h5" sx={{ fontWeight: 700, overflowWrap: 'anywhere' }}>{displayName}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', overflowWrap: 'anywhere' }}>{account.email}</Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1.25 }}>
            <Chip size="small" color="primary" variant="outlined" label={t(`profile.roles.${account.role}`)} />
            {account.organizationName && <Chip size="small" variant="outlined" label={account.organizationName} />}
          </Box>
          {!profile.exists && <Typography variant="caption" sx={{ display: 'block', mt: 1, color: 'text.secondary' }}>{t('profile.photo.needsProfile')}</Typography>}
          {photoError && <Alert severity="error" sx={{ mt: 1.5 }} role="alert">{photoError}</Alert>}

          <Box sx={{ mt: 2.5 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', gap: 2, mb: 0.75 }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>{label}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{completion.percent}%</Typography>
            </Box>
            <LinearProgress variant="determinate" value={completion.percent} color={complete ? 'success' : 'primary'}
              aria-label={t('profile.completion.aria', { percent: completion.percent })} sx={{ height: 8, borderRadius: 4 }} />
            {complete ? (
              <Typography variant="body2" sx={{ color: 'success.main', mt: 1 }}>{t('profile.completion.done')}</Typography>
            ) : (
              <>
                <Button size="small" onClick={() => setOpen((o) => !o)} aria-expanded={open} aria-controls={listId}
                  endIcon={open ? <ChevronUp size={15} /> : <ChevronDown size={15} />} sx={{ mt: 0.5, ml: -1 }}>
                  {t('profile.completion.toggle', { count: completion.missing.length })}
                </Button>
                <Collapse in={open} unmountOnExit>
                  <Box component="ul" id={listId} sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexWrap: 'wrap', gap: 1, mt: 0.5 }}>
                    {completion.missing.map((item) => (
                      <li key={item}>
                        <Chip size="small" clickable variant="outlined" label={t(`profile.completion.items.${item}`, { defaultValue: item })}
                          onClick={() => onJump(item)} />
                      </li>
                    ))}
                  </Box>
                </Collapse>
              </>
            )}
          </Box>
        </Box>
      </Box>
    </Paper>
  )
}
