import { useState, type FormEvent } from 'react'
import { Alert, Box, Button, Container, TextField, Typography } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { registrationApi } from '../../api/registrationApi'
import { SiteNavbar } from '../../components/layout/SiteNavbar'

interface FormState {
  name: string
  gstin: string
  email: string
  phone: string
  password: string
}

const INITIAL: FormState = { name: '', gstin: '', email: '', phone: '', password: '' }

/**
 * "/register/organization" — the ONLY registration path (individual
 * self-registration was removed; every Vyoog customer is a member of some
 * organization). Deliberately just 5 fields: organization name, GSTIN,
 * email, phone, password — everything else (org code, country, seats,
 * products, the admin's personal name) is defaulted server-side rather than
 * asked for here (see RegistrationService's own javadoc for exactly what
 * each default is). The email doubles as the admin's Keycloak username; the
 * password is the one they'll actually sign in with — a real Keycloak user
 * is created automatically on submit (disabled until email verification
 * completes), not by a Vyoog admin manually later.
 */
export function OrganizationRegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState<FormState>(INITIAL)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const set = <K extends keyof FormState>(field: K) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [field]: e.target.value }))

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const response = await registrationApi.registerOrganization({
        name: form.name,
        businessEmail: form.email,
        phone: form.phone,
        gstin: form.gstin,
        billingSameAsAddress: true,
        firstAdmin: {
          email: form.email,
          password: form.password,
          confirmPassword: form.password,
        },
      })
      navigate(`/register/check-email?registrationId=${response.registrationId}&email=${encodeURIComponent(form.email)}`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not complete registration. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container maxWidth="xs" sx={{ pt: '112px', pb: 8 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>Register your organization</Typography>
        <Typography sx={{ color: 'text.secondary', mb: 3 }}>
          You'll be the organization admin — everything else can be set up afterward.
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Organization name"
            value={form.name}
            onChange={set('name')}
            required
            fullWidth
          />
          <TextField
            label="GSTIN"
            value={form.gstin}
            onChange={set('gstin')}
            required
            fullWidth
          />
          <TextField
            label="Email"
            type="email"
            value={form.email}
            onChange={set('email')}
            required
            fullWidth
          />
          <TextField
            label="Phone number"
            value={form.phone}
            onChange={set('phone')}
            required
            fullWidth
          />
          <TextField
            label="Password"
            type="password"
            value={form.password}
            onChange={set('password')}
            helperText="At least 8 characters, with a letter and a number."
            required
            fullWidth
          />

          <Button type="submit" variant="contained" size="large" disabled={submitting} sx={{ mt: 1 }}>
            {submitting ? 'Registering…' : 'Register'}
          </Button>
        </Box>
      </Container>
    </Box>
  )
}
