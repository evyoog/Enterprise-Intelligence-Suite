import { ShieldAlert as PHShieldAlert } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Box, Button, Chip, CircularProgress, Paper, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { platformPrivilegedAccessApi, type PrivilegedAccessRequest } from '../../api/platformPrivilegedAccessApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { ActiveGrantsList } from '../../components/security/ActiveGrantsList'

/**
 * "/admin/privileged-access" — the backend (PlatformPrivilegedAccessController,
 * built in Phase 6) had no frontend at all until now — a platform admin could
 * only approve/reject a PLATFORM-scope elevated-access request via a direct
 * API call. Organization-scope requests have their own review surface inside
 * each org's own admin view; this page is the PLATFORM-scope equivalent.
 */
export function AdminPrivilegedAccessPage() {
  const [requests, setRequests] = useState<PrivilegedAccessRequest[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notes, setNotes] = useState<Record<number, string>>({})
  const [busyId, setBusyId] = useState<number | null>(null)
  // Bumped after a decision so the active-grants list picks up an approval.
  const [refreshKey, setRefreshKey] = useState(0)

  const load = () => {
    platformPrivilegedAccessApi.listPending()
      .then(setRequests)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load pending requests.'))
  }

  useEffect(load, [])

  const decide = (id: number, decision: 'approve' | 'reject') => {
    setBusyId(id)
    const note = notes[id]
    const call = decision === 'approve' ? platformPrivilegedAccessApi.approve : platformPrivilegedAccessApi.reject
    call(id, note)
      .then(() => { load(); setRefreshKey((k) => k + 1) })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not record this decision.'))
      .finally(() => setBusyId(null))
  }

  return (
    <>
      <PageHeader icon={PHShieldAlert} accent="orange" area="accessControl"
        title="Privileged Access Requests"
        subtitle="Platform-scope requests for temporary elevated permissions, awaiting your decision."
      />

      {error && <Typography color="error" role="alert">{error}</Typography>}

      {!error && requests === null && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
      )}

      {requests?.length === 0 && (
        <Typography sx={{ color: 'text.secondary' }}>No pending requests.</Typography>
      )}

      {requests?.map((request) => (
        <Paper key={request.id} variant="outlined" sx={{ p: 2.5, mb: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
            <Box>
              <Typography sx={{ fontWeight: 700 }}>{request.permissionName}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                Requested {new Date(request.requestedAt).toLocaleString()} · {request.requestedDurationMinutes} min
              </Typography>
            </Box>
            <Chip size="small" label={request.effectiveStatus} />
          </Box>
          <Typography variant="body2" sx={{ mb: 1.5 }}>{request.justification}</Typography>
          <TextField
            size="small"
            fullWidth
            placeholder="Optional note (why approved/rejected)"
            value={notes[request.id] ?? ''}
            onChange={(e) => setNotes((prev) => ({ ...prev, [request.id]: e.target.value }))}
            sx={{ mb: 1.5 }}
          />
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button size="small" variant="contained" disabled={busyId === request.id} onClick={() => decide(request.id, 'approve')}>
              Approve
            </Button>
            <Button size="small" variant="outlined" color="error" disabled={busyId === request.id} onClick={() => decide(request.id, 'reject')}>
              Reject
            </Button>
          </Box>
        </Paper>
      ))}

      {/* REQ-IAM-004.7: early revocation of an approved PLATFORM-scope grant. */}
      <Paper variant="outlined" sx={{ p: 2.5, mt: 3 }}>
        <ActiveGrantsList
          load={platformPrivilegedAccessApi.listActive}
          revoke={platformPrivilegedAccessApi.revoke}
          refreshKey={refreshKey}
          headingId="platform-pam-active-title"
        />
      </Paper>
    </>
  )
}
