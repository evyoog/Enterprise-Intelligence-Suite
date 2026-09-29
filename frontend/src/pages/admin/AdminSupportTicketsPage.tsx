import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, FormControl, InputLabel, MenuItem, Paper, Select,
  type SelectChangeEvent, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { adminSupportApi, type SupportTicket, type TicketPriority } from '../../api/supportApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { TicketStatusChip } from '../MyTicketsPage'

const PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']

/** "/admin/support/tickets" — MANAGE_SUPPORT_TICKETS. */
export function AdminSupportTicketsPage() {
  const { t } = useTranslation()
  const [tickets, setTickets] = useState<SupportTicket[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [categoryDrafts, setCategoryDrafts] = useState<Record<number, string>>({})
  const [assigneeDrafts, setAssigneeDrafts] = useState<Record<number, string>>({})
  const [noteDrafts, setNoteDrafts] = useState<Record<number, string>>({})

  const load = () => {
    adminSupportApi.listAll()
      .then(setTickets)
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : 'Could not load tickets.'))
  }

  useEffect(load, [])

  const applyUpdate = (ticket: SupportTicket, patch: Partial<SupportTicket>) => {
    setTickets((prev) => prev?.map((t) => (t.id === ticket.id ? { ...t, ...patch } : t)) ?? prev)
  }

  const save = async (ticket: SupportTicket) => {
    setBusyId(ticket.id)
    try {
      const assignee = assigneeDrafts[ticket.id]
      const updated = await adminSupportApi.update(ticket.id, {
        category: categoryDrafts[ticket.id] ?? ticket.category,
        priority: ticket.priority,
        assignedToCustomerId: assignee ? Number(assignee) : undefined,
      })
      applyUpdate(ticket, updated)
    } finally {
      setBusyId(null)
    }
  }

  const changePriority = async (ticket: SupportTicket, priority: TicketPriority) => {
    setBusyId(ticket.id)
    try {
      const updated = await adminSupportApi.update(ticket.id, { priority })
      applyUpdate(ticket, updated)
    } finally {
      setBusyId(null)
    }
  }

  const escalate = async (ticket: SupportTicket) => {
    setBusyId(ticket.id)
    try {
      applyUpdate(ticket, await adminSupportApi.escalate(ticket.id))
    } finally {
      setBusyId(null)
    }
  }

  const resolve = async (ticket: SupportTicket) => {
    setBusyId(ticket.id)
    try {
      applyUpdate(ticket, await adminSupportApi.resolve(ticket.id, noteDrafts[ticket.id]))
    } finally {
      setBusyId(null)
    }
  }

  const close = async (ticket: SupportTicket) => {
    setBusyId(ticket.id)
    try {
      applyUpdate(ticket, await adminSupportApi.close(ticket.id))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Box>
      <PageHeader title={t('support.admin.title')} />
      {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {tickets?.map((ticket) => {
          const terminal = ticket.status === 'RESOLVED' || ticket.status === 'CLOSED'
          return (
            <Paper key={ticket.id} variant="outlined" sx={{ p: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
                <Typography sx={{ fontWeight: 700 }}>{ticket.subject}</Typography>
                <TicketStatusChip status={ticket.status} />
              </Box>
              <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5, mb: 1.5 }}>{ticket.description}</Typography>

              <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
                <TextField
                  size="small"
                  label={t('support.admin.category')}
                  disabled={terminal}
                  value={categoryDrafts[ticket.id] ?? ticket.category ?? ''}
                  onChange={(e) => setCategoryDrafts((prev) => ({ ...prev, [ticket.id]: e.target.value }))}
                />
                <FormControl size="small" sx={{ minWidth: 140 }} disabled={terminal}>
                  <InputLabel id={`priority-${ticket.id}`}>{t('support.admin.priority')}</InputLabel>
                  <Select
                    labelId={`priority-${ticket.id}`}
                    label={t('support.admin.priority')}
                    value={ticket.priority}
                    onChange={(e: SelectChangeEvent<TicketPriority>) => changePriority(ticket, e.target.value as TicketPriority)}
                  >
                    {PRIORITIES.map((p) => <MenuItem key={p} value={p}>{t(`support.admin.priorityLevel.${p}`)}</MenuItem>)}
                  </Select>
                </FormControl>
                <TextField
                  size="small"
                  label={t('support.admin.assignTo')}
                  disabled={terminal}
                  value={assigneeDrafts[ticket.id] ?? ticket.assignedToCustomerId ?? ''}
                  onChange={(e) => setAssigneeDrafts((prev) => ({ ...prev, [ticket.id]: e.target.value }))}
                />
                <Button size="small" variant="outlined" disabled={terminal || busyId === ticket.id} onClick={() => save(ticket)}>
                  {t('support.admin.save')}
                </Button>
                <Button size="small" color="error" variant="outlined" disabled={terminal || busyId === ticket.id} onClick={() => escalate(ticket)}>
                  {t('support.admin.escalate')}
                </Button>
              </Box>

              {!terminal && (
                <Box sx={{ display: 'flex', gap: 1.5, mt: 1.5, alignItems: 'center', flexWrap: 'wrap' }}>
                  <TextField
                    size="small"
                    label={t('support.admin.resolutionNote')}
                    value={noteDrafts[ticket.id] ?? ''}
                    onChange={(e) => setNoteDrafts((prev) => ({ ...prev, [ticket.id]: e.target.value }))}
                  />
                  <Button size="small" variant="contained" disabled={busyId === ticket.id} onClick={() => resolve(ticket)}>
                    {t('support.admin.resolve')}
                  </Button>
                </Box>
              )}
              {ticket.status === 'RESOLVED' && (
                <Box sx={{ mt: 1.5 }}>
                  <Button size="small" variant="contained" disabled={busyId === ticket.id} onClick={() => close(ticket)}>
                    {t('support.admin.close')}
                  </Button>
                </Box>
              )}
            </Paper>
          )
        })}
      </Box>
    </Box>
  )
}
