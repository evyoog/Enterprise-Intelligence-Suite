import { ScrollText as PHScrollText } from 'lucide-react'
import { useEffect, useState } from 'react'
import {
  Box, Chip, CircularProgress, MenuItem, Pagination, Paper, Select, Table,
  TableBody, TableCell, TableHead, TableRow, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { auditLogApi, type AuditLogPage } from '../../api/auditLogApi'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'
import { PageHeader } from '../../components/layout/PageHeader'

const PAGE_SIZE = 25

const KNOWN_ACTIONS = [
  'LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGOUT', 'PASSWORD_RESET_REQUESTED', 'PASSWORD_RESET_COMPLETED',
  'MFA_POLICY_CHANGED', 'ORGANIZATION_CREATED', 'SUBSCRIPTION_CREATED', 'PRODUCT_ACCESS_GRANTED',
  'PRODUCT_ACCESS_REVOKED', 'PRIVILEGED_ACCESS_REQUESTED', 'PRIVILEGED_ACCESS_APPROVED', 'PRIVILEGED_ACCESS_REJECTED',
]

/**
 * "/admin/audit-log" — Phase 25: the platform-wide audit trail. Every row is
 * a real, already-occurred event (see AuditLog's own backend javadoc) — this
 * page is a pure read-only viewer with filters, no synthesized/derived rows.
 */
export function AdminAuditLogPage() {
  const [action, setAction] = useState('')
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<AuditLogPage | null>(null)
  const [error, setError] = useState<string | null>(null)
  const { formatDateTime } = useLocalePreference()

  useEffect(() => {
    auditLogApi.search({ action: action || undefined, page, size: PAGE_SIZE })
      .then(setResult)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load the audit log.'))
  }, [action, page])

  const pageCount = result ? Math.max(1, Math.ceil(result.totalElements / PAGE_SIZE)) : 1

  return (
    <>
      <PageHeader icon={PHScrollText} accent="cyan" area="compliance" title="Audit Log" subtitle="Every security and administrative event recorded across the platform." />

      <Select
        size="small"
        value={action}
        onChange={(e) => { setAction(e.target.value); setPage(0) }}
        displayEmpty
        sx={{ mb: 2, minWidth: 260 }}
      >
        <MenuItem value="">All actions</MenuItem>
        {KNOWN_ACTIONS.map((a) => <MenuItem key={a} value={a}>{a}</MenuItem>)}
      </Select>

      {error && <Typography color="error" role="alert">{error}</Typography>}

      {!error && !result && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
      )}

      {result && (
        <Paper variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Time</TableCell>
                <TableCell>Action</TableCell>
                <TableCell>Actor</TableCell>
                <TableCell>Target</TableCell>
                <TableCell>Organization</TableCell>
                <TableCell>Outcome</TableCell>
                <TableCell>Detail</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {result.items.length === 0 && (
                <TableRow><TableCell colSpan={7} sx={{ color: 'text.secondary' }}>No matching audit records.</TableCell></TableRow>
              )}
              {result.items.map((entry) => (
                <TableRow key={entry.id}>
                  <TableCell>{formatDateTime(entry.timestamp)}</TableCell>
                  <TableCell><Chip size="small" label={entry.action} /></TableCell>
                  <TableCell>{entry.actorEmail ?? (entry.actorCustomerId ? `#${entry.actorCustomerId}` : '—')}</TableCell>
                  <TableCell>{entry.targetType ? `${entry.targetType} ${entry.targetId ?? ''}` : '—'}</TableCell>
                  <TableCell>{entry.organizationId ?? '—'}</TableCell>
                  <TableCell>
                    <Chip size="small" label={entry.outcome} color={entry.outcome === 'SUCCESS' ? 'success' : 'error'} />
                  </TableCell>
                  <TableCell sx={{ maxWidth: 320, whiteSpace: 'normal' }}>{entry.detail ?? ''}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}

      {result && result.totalElements > PAGE_SIZE && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination count={pageCount} page={page + 1} onChange={(_, p) => setPage(p - 1)} />
        </Box>
      )}
    </>
  )
}
