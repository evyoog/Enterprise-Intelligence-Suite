import { useState } from 'react'
import { Alert, Box, Button, Container, Typography } from '@mui/material'
import { MailCheck } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { registrationApi } from '../../api/registrationApi'
import { SiteNavbar } from '../../components/layout/SiteNavbar'

const RESEND_COOLDOWN_MS = 30_000

/** "/register/check-email" — landed on right after either registration form
 * submits successfully. Shared by both flows since the message is identical
 * either way: "go check your inbox." */
export function CheckEmailPage() {
  const [params] = useSearchParams()
  const registrationId = params.get('registrationId') ?? ''
  const email = params.get('email') ?? 'your inbox'
  const [sending, setSending] = useState(false)
  const [cooldownUntil, setCooldownUntil] = useState(0)
  const [notice, setNotice] = useState<string | null>(null)

  const onResend = async () => {
    if (sending || Date.now() < cooldownUntil) return
    setSending(true)
    setNotice(null)
    try {
      await registrationApi.resendVerification(registrationId)
      setNotice('If that registration is still pending, a new verification email is on its way.')
      setCooldownUntil(Date.now() + RESEND_COOLDOWN_MS)
    } catch (e) {
      setNotice(e instanceof ApiError ? e.message : 'Could not resend right now — please try again shortly.')
    } finally {
      setSending(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container maxWidth="xs" sx={{ pt: '140px', pb: 8, textAlign: 'center' }}>
        <MailCheck size={40} color="#2563eb" />
        <Typography variant="h5" sx={{ fontWeight: 700, mt: 2, mb: 1 }}>Check your email</Typography>
        <Typography sx={{ color: 'text.secondary', mb: 3 }}>
          We've sent a verification link to <strong>{email}</strong>. Click it to finish creating your account.
        </Typography>

        {notice && <Alert severity="info" sx={{ mb: 2, textAlign: 'left' }}>{notice}</Alert>}

        <Button variant="outlined" fullWidth disabled={sending || Date.now() < cooldownUntil} onClick={onResend}>
          {sending ? 'Sending…' : 'Resend verification email'}
        </Button>
      </Container>
    </Box>
  )
}
