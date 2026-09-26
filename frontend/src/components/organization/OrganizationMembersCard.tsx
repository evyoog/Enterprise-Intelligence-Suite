import { useEffect, useState, type HTMLAttributes } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { organizationApi, type MemberStatusAction, type OrgMember } from '../../api/registrationApi'
import { ResetMfaDialog } from '../security/ResetMfaDialog'

const STATUS_COLOR: Record<OrgMember['status'], 'success' | 'warning' | 'default'> = {
  ACTIVE: 'success',
  SUSPENDED: 'warning',
  INACTIVE: 'default',
}

const ROLES: OrgMember['orgRole'][] = ['ORG_ADMIN', 'MEMBER']

/**
 * REQ-IAM-002 (sprint 2026.3.3, 06.02.01 Assign role): the organization's
 * members with a role selector. Needs MANAGE_USERS (enforced server-side);
 * hidden on a 403/404. Every refusal — for example demoting the last
 * administrator — is shown exactly as the backend returns it.
 */
export function OrganizationMembersCard() {
  const { t } = useTranslation()
  const [members, setMembers] = useState<OrgMember[] | null>(null)
  const [hidden, setHidden] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [savingId, setSavingId] = useState<number | null>(null)
  // C30: the member whose two-factor authentication is being reset.
  const [resetTarget, setResetTarget] = useState<OrgMember | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => {
    organizationApi.listMyOrgUsers()
      .then(setMembers)
      .catch((e) => {
        if (e instanceof ApiError && (e.status === 403 || e.status === 404)) setHidden(true)
        else setError(e instanceof ApiError ? e.message : t('orgSettings.loadError'))
      })
  }, [t])

  const changeRole = async (member: OrgMember, orgRole: OrgMember['orgRole']) => {
    if (orgRole === member.orgRole) return
    setSavingId(member.organizationMemberId)
    setError(null)
    try {
      const updated = await organizationApi.changeMemberRole(member.organizationMemberId, orgRole)
      setMembers((prev) => prev?.map((m) => (m.organizationMemberId === updated.organizationMemberId ? updated : m)) ?? prev)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('orgSettings.roleSaveError'))
    } finally {
      setSavingId(null)
    }
  }

  // 05.03.01 User Lifecycle (sprint 2026.4.1): suspend, reactivate or remove.
  const changeStatus = async (member: OrgMember, action: MemberStatusAction) => {
    if (action === 'REMOVE' && !window.confirm(t('orgSettings.confirmRemove', { name: displayName(member) }))) return
    setSavingId(member.organizationMemberId)
    setError(null)
    try {
      const updated = await organizationApi.changeMemberStatus(member.organizationMemberId, action)
      setMembers((prev) => prev?.map((m) => (m.organizationMemberId === updated.organizationMemberId ? updated : m)) ?? prev)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('orgSettings.statusSaveError'))
    } finally {
      setSavingId(null)
    }
  }

  // 05.03.02 Review access (sprint 2026.4.1): just stamps who reviewed this member and when.
  const reviewAccess = async (member: OrgMember) => {
    setSavingId(member.organizationMemberId)
    setError(null)
    try {
      const updated = await organizationApi.reviewMemberAccess(member.organizationMemberId)
      setMembers((prev) => prev?.map((m) => (m.organizationMemberId === updated.organizationMemberId ? updated : m)) ?? prev)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('orgSettings.reviewSaveError'))
    } finally {
      setSavingId(null)
    }
  }

  if (hidden) return null

  const displayName = (m: OrgMember) => [m.firstName, m.lastName].filter(Boolean).join(' ') || m.email || String(m.customerId)

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }} component="section" aria-labelledby="org-members-title">
      <Typography id="org-members-title" variant="subtitle1" component="h3" sx={{ fontWeight: 700, mb: 1 }}>
        {t('orgSettings.membersTitle')}
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      {notice && <Alert severity="success" sx={{ mb: 1.5 }} onClose={() => setNotice(null)}>{notice}</Alert>}
      <ResetMfaDialog
        open={resetTarget !== null}
        name={resetTarget ? displayName(resetTarget) : undefined}
        onConfirm={() => organizationApi.resetMemberMfa(resetTarget!.organizationMemberId)}
        onClose={() => setResetTarget(null)}
        onDone={(who) => { setResetTarget(null); setNotice(t('mfaReset.done', { name: who })) }}
      />
      {members && (
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{t('orgSettings.name')}</TableCell>
              <TableCell>{t('orgSettings.email')}</TableCell>
              <TableCell>{t('orgSettings.status')}</TableCell>
              <TableCell>{t('orgSettings.role')}</TableCell>
              <TableCell>{t('orgSettings.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {members.length === 0 && (
              <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>{t('orgSettings.noMembers')}</TableCell></TableRow>
            )}
            {members.map((member) => {
              const saving = savingId === member.organizationMemberId
              const name = displayName(member)
              return (
              <TableRow key={member.organizationMemberId}>
                <TableCell>{name}</TableCell>
                <TableCell>{member.email}</TableCell>
                <TableCell>
                  <Chip size="small" color={STATUS_COLOR[member.status]} label={t(`orgSettings.statuses.${member.status}`)} />
                  <Typography variant="caption" component="div" sx={{ color: 'text.secondary', mt: 0.5 }}>
                    {member.lastReviewedAt
                      ? t('orgSettings.reviewedOn', { date: new Date(member.lastReviewedAt).toLocaleDateString() })
                      : t('orgSettings.neverReviewed')}
                  </Typography>
                </TableCell>
                <TableCell>
                  <TextField
                    select
                    size="small"
                    value={member.orgRole}
                    disabled={saving || member.status !== 'ACTIVE'}
                    onChange={(e) => changeRole(member, e.target.value as OrgMember['orgRole'])}
                    slotProps={{ select: { SelectDisplayProps: { 'aria-label': t('orgSettings.roleFor', { name }) } as HTMLAttributes<HTMLDivElement> } }}
                  >
                    {ROLES.map((role) => (
                      <MenuItem key={role} value={role}>{t(`orgSettings.roles.${role}`)}</MenuItem>
                    ))}
                  </TextField>
                </TableCell>
                <TableCell>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {member.status === 'ACTIVE' && (
                      <>
                        <Button size="small" disabled={saving} aria-label={`${t('orgSettings.suspend')} ${name}`} onClick={() => changeStatus(member, 'SUSPEND')}>
                          {t('orgSettings.suspend')}
                        </Button>
                        <Button size="small" disabled={saving} aria-label={t('orgSettings.review') + ' ' + name} onClick={() => reviewAccess(member)}>
                          {t('orgSettings.review')}
                        </Button>
                        <Button
                          size="small"
                          variant="text"
                          color="error"
                          aria-label={t('mfaReset.buttonFor', { name })}
                          onClick={() => setResetTarget(member)}
                        >
                          {t('mfaReset.button')}
                        </Button>
                      </>
                    )}
                    {member.status === 'SUSPENDED' && (
                      <Button size="small" disabled={saving} aria-label={`${t('orgSettings.reactivate')} ${name}`} onClick={() => changeStatus(member, 'REACTIVATE')}>
                        {t('orgSettings.reactivate')}
                      </Button>
                    )}
                    {member.status !== 'INACTIVE' && (
                      <Button size="small" color="error" disabled={saving} aria-label={`${t('orgSettings.remove')} ${name}`} onClick={() => changeStatus(member, 'REMOVE')}>
                        {t('orgSettings.remove')}
                      </Button>
                    )}
                  </Box>
                </TableCell>
              </TableRow>
              )
            })}
          </TableBody>
        </Table>
      )}
    </Paper>
  )
}
