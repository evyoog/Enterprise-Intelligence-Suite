import { LifeBuoy as PHLifeBuoy } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, Paper, TextField, Typography } from '@mui/material'
import { ApiError } from '../api/client'
import { supportApi, type SupportTicket, type TicketStatus } from '../api/supportApi'
import { PageHeader } from '../components/layout/PageHeader'

/** "/support/tickets" — 12.01.01 Ticket Management (sprint 2027.1.2), any
 * authenticated customer's own tickets. Not AI-driven — see
 * `SupportTicket`'s own backend javadoc. */
export function MyTicketsPage() {
  const { t } = useTranslation()
  const [tickets, setTickets] = useState<SupportTicket[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [subject, setSubject] = useState('')
  const [description, setDescription] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  const load = () => {
    supportApi.myTickets()
      .then(setTickets)
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : 'Could not load your tickets.'))
  }

  useEffect(load, [])

  const submit = async () => {
    setSubmitting(true)
    setFormError(null)
    try {
      await supportApi.create(subject, description)
      setSubject('')
      setDescription('')
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : 'Could not create this ticket.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box>
      <PageHeader icon={PHLifeBuoy} accent="rose" area="support" title={t('support.myTickets.title')} subtitle={t('support.myTickets.subtitle')} />
      {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}

      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Typography sx={{ fontWeight: 700, mb: 1.5 }}>{t('support.myTickets.newTicket.title')}</Typography>
        {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
        <TextField
          fullWidth
          sx={{ mb: 2 }}
          label={t('support.myTickets.newTicket.subject')}
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
        />
        <TextField
          fullWidth
          multiline
          minRows={3}
          sx={{ mb: 2 }}
          label={t('support.myTickets.newTicket.description')}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <Button variant="contained" disabled={submitting || !subject.trim() || !description.trim()} onClick={submit}>
          {t('support.myTickets.newTicket.submit')}
        </Button>
      </Paper>

      <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{t('support.myTickets.list')}</Typography>
      {tickets && tickets.length === 0 && (
        <Typography sx={{ color: 'text.secondary' }}>{t('support.myTickets.noTickets')}</Typography>
      )}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {tickets?.map((ticket) => (
          <Paper key={ticket.id} variant="outlined" sx={{ p: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
              <Typography sx={{ fontWeight: 700 }}>{ticket.subject}</Typography>
              <TicketStatusChip status={ticket.status} />
            </Box>
            <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>{ticket.description}</Typography>
            {ticket.resolutionNote && (
              <Typography variant="body2" sx={{ mt: 1 }}>{ticket.resolutionNote}</Typography>
            )}
          </Paper>
        ))}
      </Box>
    </Box>
  )
}

const STATUS_COLOR: Record<TicketStatus, 'default' | 'warning' | 'error' | 'success'> = {
  OPEN: 'default',
  IN_PROGRESS: 'warning',
  ESCALATED: 'error',
  RESOLVED: 'success',
  CLOSED: 'default',
}

export function TicketStatusChip({ status }: { status: TicketStatus }) {
  const { t } = useTranslation()
  return <Chip size="small" color={STATUS_COLOR[status]} label={t(`support.myTickets.status.${status}`)} />
}
