import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Paper, TextField, Typography } from '@mui/material'
import { ApiError } from '../api/client'
import { partnersApi } from '../api/partnersApi'
import { PageHeader } from '../components/layout/PageHeader'

interface FormState {
  name: string
  contactName: string
  contactEmail: string
  description: string
}

const INITIAL: FormState = { name: '', contactName: '', contactEmail: '', description: '' }

/** "/partners/apply" — 14.01.01.01 Register provider (sprint 2027.2.1):
 * public, no Vyoog account required, same reasoning as organization
 * registration. */
export function ProviderApplicationPage() {
  const { t } = useTranslation()
  const [form, setForm] = useState<FormState>(INITIAL)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  const set = <K extends keyof FormState>(field: K) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [field]: e.target.value }))

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await partnersApi.apply({
        name: form.name,
        contactName: form.contactName,
        contactEmail: form.contactEmail,
        description: form.description || undefined,
      })
      setSubmitted(true)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('partners.apply.error'))
    } finally {
      setSubmitting(false)
    }
  }

  if (submitted) {
    return (
      <Box>
        <PageHeader title={t('partners.apply.title')} />
        <Alert severity="success">{t('partners.apply.submitted')}</Alert>
      </Box>
    )
  }

  return (
    <Box>
      <PageHeader title={t('partners.apply.title')} subtitle={t('partners.apply.subtitle')} />
      <Paper variant="outlined" sx={{ p: 2.5, maxWidth: 480 }}>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField required label={t('partners.apply.companyName')} value={form.name} onChange={set('name')} />
          <TextField required label={t('partners.apply.contactName')} value={form.contactName} onChange={set('contactName')} />
          <TextField required type="email" label={t('partners.apply.contactEmail')} value={form.contactEmail} onChange={set('contactEmail')} />
          <TextField multiline minRows={3} label={t('partners.apply.description')} value={form.description} onChange={set('description')} />
          <Box>
            <Button type="submit" variant="contained" disabled={submitting}>{t('partners.apply.submit')}</Button>
          </Box>
        </Box>
      </Paper>
      <Typography variant="body2" sx={{ color: 'text.secondary', mt: 2 }}>{t('partners.apply.reviewNotice')}</Typography>
    </Box>
  )
}
