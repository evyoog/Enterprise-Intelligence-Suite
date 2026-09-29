import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, MenuItem, Paper, TextField, Typography,
} from '@mui/material'
import { Plus, Trash2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import { groupsApi, type Group } from '../../api/groupsApi'
import { organizationApi, type OrgMember } from '../../api/registrationApi'

/**
 * 05.04.01 Groups (sprint 2026.4.1) — same pattern as OrganizationMembersCard:
 * needs MANAGE_USERS (enforced server-side), hides itself on a 403/404.
 */
export function OrganizationGroupsCard() {
  const { t } = useTranslation()
  const [groups, setGroups] = useState<Group[] | null>(null)
  const [members, setMembers] = useState<OrgMember[]>([])
  const [hidden, setHidden] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [newGroupName, setNewGroupName] = useState('')
  const [isCreating, setIsCreating] = useState(false)
  const [addingTo, setAddingTo] = useState<Record<number, string>>({})
  const [busyGroupId, setBusyGroupId] = useState<number | null>(null)

  useEffect(() => {
    groupsApi.list()
      .then(setGroups)
      .catch((e) => {
        if (e instanceof ApiError && (e.status === 403 || e.status === 404)) setHidden(true)
        else setError(e instanceof ApiError ? e.message : t('groups.loadError'))
      })
    organizationApi.listMyOrgUsers().then(setMembers).catch(() => setMembers([]))
  }, [t])

  if (hidden) return null

  const displayName = (m: OrgMember) => [m.firstName, m.lastName].filter(Boolean).join(' ') || m.email || String(m.customerId)

  const createGroup = () => {
    if (!newGroupName.trim()) return
    setIsCreating(true)
    setError(null)
    groupsApi.create(newGroupName.trim())
      .then((group) => {
        setGroups((prev) => [...(prev ?? []), group])
        setNewGroupName('')
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('groups.createError')))
      .finally(() => setIsCreating(false))
  }

  const removeGroup = (groupId: number) => {
    setBusyGroupId(groupId)
    groupsApi.remove(groupId)
      .then(() => setGroups((prev) => prev?.filter((g) => g.id !== groupId) ?? prev))
      .catch((e) => setError(e instanceof ApiError ? e.message : t('groups.deleteError')))
      .finally(() => setBusyGroupId(null))
  }

  const addMember = (group: Group) => {
    const memberId = Number(addingTo[group.id])
    if (!memberId) return
    setBusyGroupId(group.id)
    groupsApi.addMember(group.id, memberId)
      .then((updated) => setGroups((prev) => prev?.map((g) => (g.id === updated.id ? updated : g)) ?? prev))
      .catch((e) => setError(e instanceof ApiError ? e.message : t('groups.addMemberError')))
      .finally(() => setBusyGroupId(null))
  }

  const removeMember = (group: Group, memberId: number) => {
    setBusyGroupId(group.id)
    groupsApi.removeMember(group.id, memberId)
      .then((updated) => setGroups((prev) => prev?.map((g) => (g.id === updated.id ? updated : g)) ?? prev))
      .catch((e) => setError(e instanceof ApiError ? e.message : t('groups.removeMemberError')))
      .finally(() => setBusyGroupId(null))
  }

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }} component="section" aria-labelledby="org-groups-title">
      <Typography id="org-groups-title" variant="subtitle1" component="h3" sx={{ fontWeight: 700, mb: 1 }}>
        {t('groups.title')}
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}

      <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
        <TextField
          size="small"
          label={t('groups.newGroupName')}
          value={newGroupName}
          onChange={(e) => setNewGroupName(e.target.value)}
          sx={{ flex: 1, maxWidth: 320 }}
        />
        <Button size="small" variant="outlined" startIcon={<Plus size={14} />} disabled={isCreating || !newGroupName.trim()} onClick={createGroup}>
          {t('groups.create')}
        </Button>
      </Box>

      {groups && groups.length === 0 && (
        <Typography sx={{ color: 'text.secondary' }}>{t('groups.noGroups')}</Typography>
      )}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {groups?.map((group) => {
          const memberIds = new Set(group.members.map((m) => m.organizationMemberId))
          const available = members.filter((m) => !memberIds.has(m.organizationMemberId))
          return (
            <Box key={group.id} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, p: 1.5 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <Typography sx={{ fontWeight: 600 }}>{group.name}</Typography>
                <Button
                  size="small" color="error" startIcon={<Trash2 size={14} />}
                  disabled={busyGroupId === group.id}
                  aria-label={`${t('groups.deleteGroup')} ${group.name}`}
                  onClick={() => removeGroup(group.id)}
                >
                  {t('groups.deleteGroup')}
                </Button>
              </Box>

              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: 1 }}>
                {group.members.length === 0 && (
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('groups.noMembers')}</Typography>
                )}
                {group.members.map((m) => (
                  <Chip
                    key={m.organizationMemberId}
                    size="small"
                    label={displayName(m)}
                    onDelete={() => removeMember(group, m.organizationMemberId)}
                    disabled={busyGroupId === group.id}
                  />
                ))}
              </Box>

              {available.length > 0 && (
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <TextField
                    select
                    size="small"
                    label={t('groups.addMember')}
                    sx={{ minWidth: 200 }}
                    value={addingTo[group.id] ?? ''}
                    onChange={(e) => setAddingTo((prev) => ({ ...prev, [group.id]: e.target.value }))}
                  >
                    {available.map((m) => (
                      <MenuItem key={m.organizationMemberId} value={m.organizationMemberId}>{displayName(m)}</MenuItem>
                    ))}
                  </TextField>
                  <Button size="small" disabled={busyGroupId === group.id || !addingTo[group.id]} onClick={() => addMember(group)}>
                    {t('groups.add')}
                  </Button>
                </Box>
              )}
            </Box>
          )
        })}
      </Box>
    </Paper>
  )
}
