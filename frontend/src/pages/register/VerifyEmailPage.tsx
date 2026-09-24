import { useEffect, useRef, useState } from 'react'
import { Box, Button, CircularProgress, Container, Typography } from '@mui/material'
import { CheckCircle2, XCircle } from 'lucide-react'
import { Link as RouterLink, useSearchParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { registrationApi } from '../../api/registrationApi'
import { useAuthModal } from '../../auth/AuthModalContext'
import { SiteNavbar } from '../../components/layout/SiteNavbar'

type State = { kind: 'loading' } | { kind: 'success' } | { kind: 'error'; message: string }

/** "/register/verify?token=..." — shared by both registration flows (the
 * emailed link always points here). A token is single-use, so this must call
 * verify-email exactly once even if React re-renders/re-mounts in dev
 * StrictMode — the ref guard below is what makes that safe. */
export function VerifyEmailPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const [state, setState] = useState<State>({ kind: 'loading' })
  const authModal = useAuthModal()
  const calledRef = useRef(false)

  useEffect(() => {
    if (calledRef.current) return
    calledRef.current = true

    if (!token) {
      setState({ kind: 'error', message: 'This verification link is missing its token.' });
      return
    }
    registrationApi.verifyEmail(token)
      .then(() => setState({ kind: 'success' }))
      .catch((e) => setState({ kind: 'error', message: e instanceof ApiError ? e.message : 'Could not verify this link.' }))
  }, [token])

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container maxWidth="xs" sx={{ pt: '140px', pb: 8, textAlign: 'center' }}>
        {state.kind === 'loading' && (
          <>
            <CircularProgress size={32} />
            <Typography sx={{ mt: 2, color: 'text.secondary' }}>Verifying your email…</Typography>
          </>
        )}
        {state.kind === 'success' && (
          <>
            <CheckCircle2 size={40} color="#16a34a" />
            <Typography variant="h5" sx={{ fontWeight: 700, mt: 2, mb: 1 }}>You're all set</Typography>
            <Typography sx={{ color: 'text.secondary', mb: 3 }}>
              Your email is verified and your account is ready. Sign in to get started.
            </Typography>
            <Button variant="contained" fullWidth onClick={authModal.openLogin}>Sign in</Button>
          </>
        )}
        {state.kind === 'error' && (
          <>
            <XCircle size={40} color="#dc2626" />
            <Typography variant="h5" sx={{ fontWeight: 700, mt: 2, mb: 1 }}>Couldn't verify this link</Typography>
            <Typography sx={{ color: 'text.secondary', mb: 3 }}>{state.message}</Typography>
            <Button component={RouterLink} to="/register" variant="outlined" fullWidth>Back to registration</Button>
          </>
        )}
      </Container>
    </Box>
  )
}
