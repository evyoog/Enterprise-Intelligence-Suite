import { useState, type FormEvent } from 'react'
import { Box, Button, Container, TextField, Typography } from '@mui/material'
import { CheckCircle2, KeyRound, XCircle } from 'lucide-react'
import { Link as RouterLink, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { authApi } from '../api/authApi'
import { useAuthModal } from '../auth/AuthModalContext'
import { SiteNavbar } from '../components/layout/SiteNavbar'

type State = { kind: 'form' } | { kind: 'success' } | { kind: 'error'; message: string }

/** "/reset-password?token=..." — the link PasswordResetService emails. A
 * token is single-use, so once this succeeds the form is replaced entirely
 * rather than left submittable again. */
export function ResetPasswordPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const authModal = useAuthModal()
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [state, setState] = useState<State>(token ? { kind: 'form' } : { kind: 'error', message: 'This reset link is missing its token.' })

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (submitting || !token) return
    setSubmitting(true)
    try {
      await authApi.resetPassword(token, newPassword, confirmPassword)
      setState({ kind: 'success' })
    } catch (err) {
      setState({ kind: 'error', message: err instanceof ApiError ? err.message : 'Could not reset your password.' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container maxWidth="xs" sx={{ pt: '140px', pb: 8, textAlign: 'center' }}>
        {state.kind === 'form' && (
          <>
            <KeyRound size={40} color="#2563eb" />
            <Typography variant="h5" sx={{ fontWeight: 700, mt: 2, mb: 1 }}>Set a new password</Typography>
            <Typography sx={{ color: 'text.secondary', mb: 3 }}>Choose a new password for your account.</Typography>
            <Box component="form" onSubmit={onSubmit} sx={{ textAlign: 'left' }}>
              <TextField
                label="New password"
                type="password"
                fullWidth
                required
                autoComplete="new-password"
                autoFocus
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                sx={{ mb: 2 }}
              />
              <TextField
                label="Confirm new password"
                type="password"
                fullWidth
                required
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                sx={{ mb: 2 }}
              />
              <Button type="submit" variant="contained" fullWidth disabled={submitting}>
                {submitting ? 'Resetting…' : 'Reset password'}
              </Button>
            </Box>
          </>
        )}
        {state.kind === 'success' && (
          <>
            <CheckCircle2 size={40} color="#16a34a" />
            <Typography variant="h5" sx={{ fontWeight: 700, mt: 2, mb: 1 }}>Password reset</Typography>
            <Typography sx={{ color: 'text.secondary', mb: 3 }}>
              Your password has been changed and you've been signed out everywhere. Sign in with your new password.
            </Typography>
            <Button variant="contained" fullWidth onClick={authModal.openLogin}>Sign in</Button>
          </>
        )}
        {state.kind === 'error' && (
          <>
            <XCircle size={40} color="#dc2626" />
            <Typography variant="h5" sx={{ fontWeight: 700, mt: 2, mb: 1 }}>Couldn't reset your password</Typography>
            <Typography sx={{ color: 'text.secondary', mb: 3 }}>{state.message}</Typography>
            <Button component={RouterLink} to="/forgot-password" variant="outlined" fullWidth>Request a new link</Button>
          </>
        )}
      </Container>
    </Box>
  )
}
