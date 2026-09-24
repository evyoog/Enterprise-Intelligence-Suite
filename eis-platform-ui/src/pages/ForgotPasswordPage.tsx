import { useState, type FormEvent } from 'react'
import { Box, Button, Container, TextField, Typography } from '@mui/material'
import { KeyRound } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { SiteNavbar } from '../components/layout/SiteNavbar'

/**
 * "/forgot-password" — deliberately shows the exact same success state
 * whether or not the email matches a real account (see
 * PasswordResetService's own anti-enumeration handling on the backend);
 * this page never learns which case happened, so it can't leak it either.
 */
export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (submitting) return
    setSubmitting(true)
    try {
      await authApi.forgotPassword(email)
    } catch {
      // Deliberately ignored — see this page's own note above. A network-
      // level failure and "no such account" must look identical here too.
    } finally {
      setSubmitting(false)
      setSubmitted(true)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container maxWidth="xs" sx={{ pt: '140px', pb: 8, textAlign: 'center' }}>
        <KeyRound size={40} color="#2563eb" />
        <Typography variant="h5" sx={{ fontWeight: 700, mt: 2, mb: 1 }}>Reset your password</Typography>

        {!submitted ? (
          <>
            <Typography sx={{ color: 'text.secondary', mb: 3 }}>
              Enter the email address on your Vyoog account and we'll send you a link to reset your password.
            </Typography>
            <Box component="form" onSubmit={onSubmit} sx={{ textAlign: 'left' }}>
              <TextField
                label="Email address"
                type="email"
                fullWidth
                required
                autoComplete="email"
                autoFocus
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                sx={{ mb: 2 }}
              />
              <Button type="submit" variant="contained" fullWidth disabled={submitting}>
                {submitting ? 'Sending…' : 'Send reset link'}
              </Button>
            </Box>
          </>
        ) : (
          <Typography sx={{ color: 'text.secondary', mb: 3 }}>
            If an account exists for <strong>{email}</strong>, we've sent a link to reset its password.
          </Typography>
        )}

        <Button component={RouterLink} to="/" sx={{ mt: 3 }}>Back to home</Button>
      </Container>
    </Box>
  )
}
