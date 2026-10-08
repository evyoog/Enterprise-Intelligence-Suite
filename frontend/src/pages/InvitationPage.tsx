import { MailCheck } from 'lucide-react'
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom'
import { Alert, Box, Button, CircularProgress, Paper, Stack, TextField, Typography } from '@mui/material'
import { ApiError } from '../api/client'
import { invitationsApi, type InvitationPreview } from '../api/invitationsApi'
import { useAuth } from '../auth/AuthProvider'
import { useSignOut } from '../auth/useSignOut'

const message = (e: unknown, fallback: string) => (e instanceof ApiError ? e.message : fallback)

/**
 * "/invitations/:token" — REQ-TEN-008 invited side. The token in the address is the credential. Shows
 * the organization, role and expiry, then (a) a sign-in for an existing account, (b) a short form that
 * creates the account for a new person, or (c) Accept and Decline for the signed-in matching account.
 */
export function InvitationPage() {
  const { t } = useTranslation()
  const { token = '' } = useParams()
  const navigate = useNavigate()
  const auth = useAuth()
  const signOut = useSignOut()
  const [preview, setPreview] = useState<InvitationPreview | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [done, setDone] = useState<'accepted' | 'declined' | null>(null)
  const [joined, setJoined] = useState('')
  const [form, setForm] = useState({ firstName: '', lastName: '', password: '', confirmPassword: '' })

  useEffect(() => {
    let alive = true
    invitationsApi.preview(token).then((p) => { if (alive) setPreview(p) }).catch(() => { if (alive) setLoadError(true) })
    return () => { alive = false }
  }, [token])

  const signedInEmail = (auth.user?.email ?? auth.user?.username ?? '').toLowerCase()
  const sameAccount = !!preview?.email && signedInEmail === preview.email.toLowerCase()
  const returnTo = encodeURIComponent(`/invitations/${token}`)

  const run = useCallback(async (action: () => Promise<void>) => {
    setBusy(true)
    setError(null)
    try { await action() } catch (e) { setError(message(e, t('invitations.page.error'))) } finally { setBusy(false) }
  }, [t])

  const accept = () => run(async () => {
    const r = await invitationsApi.accept(token)
    setJoined(r.organizationName)
    setDone('accepted')
  })
  const decline = () => run(async () => {
    await invitationsApi.decline(token)
    setDone('declined')
  })
  const createAccount = (e: FormEvent) => {
    e.preventDefault()
    void run(async () => {
      const r = await invitationsApi.createAccount(token, form)
      setJoined(r.organizationName)
      try {
        await auth.login(preview?.email ?? '', form.password)
        setDone('accepted')
      } catch {
        // The account exists and the membership is created; sign in from the login page.
        navigate('/login', { replace: true })
      }
    })
  }

  let body: React.ReactNode
  if (loadError) {
    body = <Alert severity="error">{t('invitations.page.invalid')}</Alert>
  } else if (!preview) {
    body = <CircularProgress aria-label="loading" />
  } else if (done === 'accepted') {
    body = (
      <Stack spacing={2}>
        <Alert severity="success">{t('invitations.page.joined', { organization: joined })}</Alert>
        <Button variant="contained" onClick={() => navigate('/organization/business-dashboard', { replace: true })}>{t('invitations.page.continue')}</Button>
      </Stack>
    )
  } else if (done === 'declined') {
    body = <Alert severity="info">{t('invitations.page.declined')}</Alert>
  } else if (preview.status !== 'PENDING') {
    body = <Alert severity={preview.status === 'EXPIRED' ? 'warning' : 'error'}>{t(`invitations.page.status.${preview.status}`)}</Alert>
  } else {
    body = (
      <Stack spacing={2}>
        <Typography>{t('invitations.page.intro', { inviter: preview.inviterName ?? t('invitations.page.someone'), organization: preview.organizationName })}</Typography>
        <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: 'max-content 1fr', columnGap: 2, rowGap: 0.5, m: 0 }}>
          <Typography component="dt" color="text.secondary">{t('invitations.form.email')}</Typography>
          <Typography component="dd" sx={{ m: 0 }}>{preview.email}</Typography>
          <Typography component="dt" color="text.secondary">{t('invitations.form.role')}</Typography>
          <Typography component="dd" sx={{ m: 0 }}>{t(`orgSettings.roles.${preview.orgRole}`)}</Typography>
          {preview.orgNodeName && (
            <>
              <Typography component="dt" color="text.secondary">{t('invitations.form.node')}</Typography>
              <Typography component="dd" sx={{ m: 0 }}>{preview.orgNodeName}</Typography>
            </>
          )}
          <Typography component="dt" color="text.secondary">{t('invitations.col.expiresAt')}</Typography>
          <Typography component="dd" sx={{ m: 0 }}>{preview.expiresAt ? new Date(preview.expiresAt).toLocaleDateString() : ''}</Typography>
        </Box>
        {!preview.organizationAvailable && <Alert severity="warning">{t('invitations.page.unavailable')}</Alert>}
        {error && <Alert severity="error">{error}</Alert>}
        {auth.isAuthenticated && !sameAccount && (
          <Stack spacing={1}>
            <Alert severity="warning">{t('invitations.page.wrongAccount', { signedIn: signedInEmail, invited: preview.email })}</Alert>
            <Button variant="outlined" onClick={() => signOut()}>{t('invitations.page.signOut')}</Button>
          </Stack>
        )}
        {auth.isAuthenticated && sameAccount && (
          <Stack direction="row" spacing={1}>
            <Button variant="contained" disabled={busy || !preview.organizationAvailable} onClick={() => void accept()}>{t('invitations.page.accept')}</Button>
            <Button disabled={busy} onClick={() => void decline()}>{t('invitations.page.decline')}</Button>
          </Stack>
        )}
        {!auth.isAuthenticated && preview.accountExists && (
          <Stack spacing={1}>
            <Typography variant="body2" color="text.secondary">{t('invitations.page.haveAccount')}</Typography>
            <Stack direction="row" spacing={1}>
              <Button variant="contained" component={RouterLink} to={`/login?returnTo=${returnTo}`}>{t('invitations.page.login')}</Button>
              <Button disabled={busy} onClick={() => void decline()}>{t('invitations.page.decline')}</Button>
            </Stack>
          </Stack>
        )}
        {!auth.isAuthenticated && !preview.accountExists && (
          <Box component="form" onSubmit={createAccount} noValidate>
            <Stack spacing={2}>
              <Typography variant="subtitle1" component="h2">{t('invitations.page.createTitle')}</Typography>
              <TextField label={t('invitations.page.firstName')} value={form.firstName} required autoComplete="given-name"
                onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
              <TextField label={t('invitations.page.lastName')} value={form.lastName} required autoComplete="family-name"
                onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
              <TextField label={t('invitations.page.password')} type="password" value={form.password} required autoComplete="new-password"
                helperText={t('invitations.page.passwordHelp')} onChange={(e) => setForm({ ...form, password: e.target.value })} />
              <TextField label={t('invitations.page.confirmPassword')} type="password" value={form.confirmPassword} required autoComplete="new-password"
                onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })} />
              <Stack direction="row" spacing={1}>
                <Button type="submit" variant="contained" disabled={busy || !preview.organizationAvailable || !form.firstName.trim() || !form.lastName.trim() || !form.password}>
                  {t('invitations.page.createAndJoin')}
                </Button>
                <Button disabled={busy} onClick={() => void decline()}>{t('invitations.page.decline')}</Button>
              </Stack>
            </Stack>
          </Box>
        )}
        {preview.accountExists && auth.isAuthenticated && sameAccount && <Typography variant="caption" color="text.secondary">{t('invitations.page.personalKept')}</Typography>}
      </Stack>
    )
  }

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', p: { xs: 2, sm: 6 } }}>
      <Paper variant="outlined" sx={{ p: { xs: 2.5, sm: 4 }, width: 'min(520px, 100%)' }}>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', mb: 2 }}>
          <MailCheck size={24} aria-hidden />
          <Typography variant="h5" component="h1">{t('invitations.page.title')}</Typography>
        </Stack>
        {body}
      </Paper>
    </Box>
  )
}
