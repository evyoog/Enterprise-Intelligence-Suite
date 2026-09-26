import { useEffect, useState, type HTMLAttributes } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Button, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { organizationApi, type OrgMember } from '../../api/registrationApi'
import { ResetMfaDialog } from '../security/ResetMfaDialog'

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
              <TableCell>{t('mfaReset.column')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {members.length === 0 && (
              <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>{t('orgSettings.noMembers')}</TableCell></TableRow>
            )}
            {members.map((member) => (
              <TableRow key={member.organizationMemberId}>
                <TableCell>{displayName(member)}</TableCell>
                <TableCell>{member.email}</TableCell>
                <TableCell>{member.status}</TableCell>
                <TableCell>
                  <TextField
                    select
                    size="small"
                    value={member.orgRole}
                    disabled={savingId === member.organizationMemberId}
                    onChange={(e) => changeRole(member, e.target.value as OrgMember['orgRole'])}
                    slotProps={{ select: { SelectDisplayProps: { 'aria-label': t('orgSettings.roleFor', { name: displayName(member) }) } as HTMLAttributes<HTMLDivElement> } }}
                  >
                    {ROLES.map((role) => (
                      <MenuItem key={role} value={role}>{t(`orgSettings.roles.${role}`)}</MenuItem>
                    ))}
                  </TextField>
                </TableCell>
                <TableCell>
                  <Button
                    size="small"
                    variant="text"
                    color="error"
                    aria-label={t('mfaReset.buttonFor', { name: displayName(member) })}
                    onClick={() => setResetTarget(member)}
                  >
                    {t('mfaReset.button')}
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Paper>
  )
}
