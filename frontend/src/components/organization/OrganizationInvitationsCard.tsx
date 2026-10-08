import { Send } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Drawer, FormControl, InputLabel, MenuItem,
  Paper, Select, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import {
  invitationsApi, type Invitation, type InvitationStatus, type NodeOption,
} from '../../api/invitationsApi'
import { ConfirmDialog } from '../ui/ConfirmDialog'

const STATUSES: InvitationStatus[] = ['PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'REVOKED']
const ROLES = ['MEMBER', 'ORG_ADMIN']
const COLOR: Record<InvitationStatus, 'warning' | 'success' | 'default' | 'error'> = {
  PENDING: 'warning', ACCEPTED: 'success', DECLINED: 'default', EXPIRED: 'default', REVOKED: 'error',
}
const date = (v?: string) => (v ? new Date(v).toLocaleString() : '—')
const message = (e: unknown, fallback: string) => (e instanceof ApiError ? e.message : fallback)

/**
 * REQ-TEN-008 Invitations tab: invite, list, resend, revoke. Needs ORG_ADMIN or INVITE_USERS; the
 * backend enforces it and what each person sees (a delegated inviter sees only their own).
 */
export function OrganizationInvitationsCard({ canInviteAdmin }: { canInviteAdmin: boolean }) {
  const { t } = useTranslation()
  const [rows, setRows] = useState<Invitation[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [filter, setFilter] = useState('')
  const [search, setSearch] = useState('')
  const [version, setVersion] = useState(0)
  const [inviteOpen, setInviteOpen] = useState(false)
  const [revokeTarget, setRevokeTarget] = useState<Invitation | null>(null)
  const [detail, setDetail] = useState<Invitation | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const reload = useCallback(() => setVersion((v) => v + 1), [])

  useEffect(() => {
    let alive = true
    invitationsApi.list().then((r) => { if (alive) { setRows(r); setError(null) } })
      .catch((e) => { if (alive) setError(message(e, t('invitations.loadError'))) })
    return () => { alive = false }
  }, [version, t])

  const visible = (rows ?? []).filter((r) => (!filter || r.status === filter)
    && (!search.trim() || r.email.toLowerCase().includes(search.trim().toLowerCase())))

  const resend = async (row: Invitation) => {
    setBusyId(row.id)
    setError(null)
    try {
      const result = await invitationsApi.resend(row.id)
      setNotice(result.emailSent ? t('invitations.resent', { email: row.email }) : t('invitations.emailFailed'))
      reload()
    } catch (e) { setError(message(e, t('invitations.actionError'))) } finally { setBusyId(null) }
  }
  const revoke = async () => {
    if (!revokeTarget) return
    setBusyId(revokeTarget.id)
    try {
      await invitationsApi.revoke(revokeTarget.id)
      setNotice(t('invitations.revoked', { email: revokeTarget.email }))
      setRevokeTarget(null)
      reload()
    } catch (e) { setError(message(e, t('invitations.actionError'))); setRevokeTarget(null) } finally { setBusyId(null) }
  }

  return (
    <Box>
      <Stack direction="row" spacing={1.5} useFlexGap sx={{ mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField size="small" label={t('invitations.search')} value={search} onChange={(e) => setSearch(e.target.value)} sx={{ minWidth: 240 }} />
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel id="inv-status">{t('invitations.status')}</InputLabel>
          <Select labelId="inv-status" label={t('invitations.status')} value={filter} onChange={(e) => setFilter(e.target.value)}>
            <MenuItem value="">{t('orgDirectory.all')}</MenuItem>
            {STATUSES.map((s) => <MenuItem key={s} value={s}>{t(`invitations.statuses.${s}`)}</MenuItem>)}
          </Select>
        </FormControl>
        <Box sx={{ flex: 1 }} />
        <Button variant="contained" startIcon={<Send size={16} />} onClick={() => setInviteOpen(true)}>{t('invitations.invite')}</Button>
      </Stack>
      {notice && <Alert severity="success" onClose={() => setNotice(null)} sx={{ mb: 2 }}>{notice}</Alert>}
      {error && <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>{error}</Alert>}
      {rows && visible.length === 0 && <Typography color="text.secondary">{rows.length === 0 ? t('invitations.none') : t('invitations.noMatches')}</Typography>}
      {visible.length > 0 && (
        <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
          <Table size="small" aria-label={t('invitations.title')}>
            <TableHead>
              <TableRow>
                {['email', 'status', 'role', 'node', 'invitedBy', 'invitedAt', 'expiresAt', 'acceptedAt', 'lastResent', 'actions'].map((c) => (
                  <TableCell key={c}>{t(`invitations.col.${c}`)}</TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {visible.map((r) => (
                <TableRow key={r.id}>
                  <TableCell>{r.email}</TableCell>
                  <TableCell><Chip size="small" color={COLOR[r.status]} label={t(`invitations.statuses.${r.status}`)} /></TableCell>
                  <TableCell>{t(`orgSettings.roles.${r.orgRole}`)}</TableCell>
                  <TableCell>{r.orgNodeName ?? '—'}</TableCell>
                  <TableCell>{r.invitedByName ?? '—'}</TableCell>
                  <TableCell>{date(r.invitedAt)}</TableCell>
                  <TableCell>{date(r.expiresAt)}</TableCell>
                  <TableCell>{date(r.acceptedAt)}</TableCell>
                  <TableCell>{r.sendCount > 1 ? date(r.lastSentAt) : '—'}</TableCell>
                  <TableCell>
                    <Stack direction="row" spacing={0.5} useFlexGap sx={{ flexWrap: 'wrap' }}>
                      {r.status !== 'ACCEPTED' && (
                        <Button size="small" disabled={busyId === r.id} aria-label={t('invitations.resendFor', { email: r.email })} onClick={() => void resend(r)}>
                          {t('invitations.resend')}
                        </Button>
                      )}
                      {r.status === 'PENDING' && (
                        <Button size="small" color="error" disabled={busyId === r.id} aria-label={t('invitations.revokeFor', { email: r.email })} onClick={() => setRevokeTarget(r)}>
                          {t('invitations.revoke')}
                        </Button>
                      )}
                      <Button size="small" aria-label={t('invitations.detailsFor', { email: r.email })} onClick={() => setDetail(r)}>{t('invitations.details')}</Button>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
      {inviteOpen && (
        <InviteDialog canInviteAdmin={canInviteAdmin} onClose={() => setInviteOpen(false)}
          onSent={(sent, email) => { setInviteOpen(false); setNotice(sent ? t('invitations.sent', { email }) : t('invitations.emailFailed')); reload() }} />
      )}
      <ConfirmDialog open={!!revokeTarget} title={t('invitations.revokeTitle', { email: revokeTarget?.email })} body={t('invitations.revokeBody')}
        confirmLabel={t('invitations.revoke')} busy={busyId !== null} onConfirm={() => void revoke()} onClose={() => setRevokeTarget(null)} />
      <Drawer anchor="right" open={!!detail} onClose={() => setDetail(null)}>
        {detail && (
          <Box sx={{ width: { xs: 300, sm: 380 }, p: 3 }} role="region" aria-label={t('invitations.detailsFor', { email: detail.email })}>
            <Typography variant="h6" component="h2" sx={{ mb: 2 }}>{detail.email}</Typography>
            <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: 'max-content 1fr', columnGap: 2, rowGap: 1, m: 0 }}>
              {([
                ['status', t(`invitations.statuses.${detail.status}`)], ['role', t(`orgSettings.roles.${detail.orgRole}`)], ['node', detail.orgNodeName ?? '—'],
                ['invitedBy', detail.invitedByName ?? '—'], ['invitedAt', date(detail.invitedAt)], ['expiresAt', date(detail.expiresAt)],
                ['acceptedAt', date(detail.acceptedAt)], ['declinedAt', date(detail.declinedAt)], ['revokedAt', date(detail.revokedAt)],
                ['acceptedBy', detail.acceptedByName ?? '—'], ['sendCount', String(detail.sendCount)],
              ] as [string, string][]).map(([k, v]) => (
                <Box key={k} sx={{ display: 'contents' }}>
                  <Typography component="dt" color="text.secondary">{t(`invitations.col.${k}`)}</Typography>
                  <Typography component="dd" sx={{ m: 0 }}>{v}</Typography>
                </Box>
              ))}
            </Box>
            <Button sx={{ mt: 3 }} onClick={() => setDetail(null)}>{t('orgStructure.close')}</Button>
          </Box>
        )}
      </Drawer>
    </Box>
  )
}

function InviteDialog({ canInviteAdmin, onClose, onSent }: { canInviteAdmin: boolean; onClose: () => void; onSent: (emailSent: boolean, email: string) => void }) {
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [role, setRole] = useState('MEMBER')
  const [nodeId, setNodeId] = useState<number | ''>('')
  const [nodes, setNodes] = useState<NodeOption[]>([])
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const [duplicate, setDuplicate] = useState(false)

  useEffect(() => {
    let alive = true
    invitationsApi.structureNodes().then((n) => { if (alive) setNodes(n) }).catch(() => {})
    return () => { alive = false }
  }, [])

  const valid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())
  const send = async () => {
    setBusy(true)
    setErr(null)
    setDuplicate(false)
    try {
      const result = await invitationsApi.create({ email: email.trim(), orgRole: role, orgNodeId: nodeId === '' ? null : nodeId })
      onSent(result.emailSent, result.invitation.email)
    } catch (e) {
      setErr(message(e, t('invitations.actionError')))
      setDuplicate(e instanceof ApiError && e.status === 409 && /pending/i.test(e.message))
      setBusy(false)
    }
  }
  const node = nodes.find((n) => n.id === nodeId)

  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="sm" aria-labelledby="invite-title">
      <DialogTitle id="invite-title">{t('invitations.inviteTitle')}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {err && <Alert severity="error">{err}{duplicate && ' ' + t('invitations.useResend')}</Alert>}
          <TextField label={t('invitations.form.email')} type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus
            helperText={t('invitations.form.emailHelp')} />
          <FormControl>
            <InputLabel id="inv-role">{t('invitations.form.role')}</InputLabel>
            <Select labelId="inv-role" label={t('invitations.form.role')} value={role} onChange={(e) => setRole(e.target.value)}>
              {ROLES.map((r) => <MenuItem key={r} value={r} disabled={r === 'ORG_ADMIN' && !canInviteAdmin}>{t(`orgSettings.roles.${r}`)}</MenuItem>)}
            </Select>
            {!canInviteAdmin && <Typography variant="caption" color="text.secondary">{t('invitations.form.adminOnly')}</Typography>}
          </FormControl>
          {nodes.length > 0 && (
            <FormControl>
              <InputLabel id="inv-node">{t('invitations.form.node')}</InputLabel>
              <Select labelId="inv-node" label={t('invitations.form.node')} value={nodeId} onChange={(e) => setNodeId(e.target.value as number | '')}>
                <MenuItem value="">{t('invitations.form.noNode')}</MenuItem>
                {nodes.map((n) => <MenuItem key={n.id} value={n.id}>{n.path}</MenuItem>)}
              </Select>
            </FormControl>
          )}
          <Alert severity="info" icon={false}>
            {t('invitations.form.summary', {
              email: email.trim() || '…', role: t(`orgSettings.roles.${role}`), node: node ? ` · ${node.path}` : '',
            })}
          </Alert>
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('orgStructure.cancel')}</Button>
        <Button variant="contained" disabled={busy || !valid} onClick={() => void send()}>{t('invitations.form.send')}</Button>
      </DialogActions>
    </Dialog>
  )
}

