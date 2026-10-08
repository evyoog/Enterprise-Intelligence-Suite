import { Check, Pencil, X } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, IconButton, TextField } from '@mui/material'
import {
  adminRegistrationApi, type OrganizationAdmin, type OrganizationLifecycleAction, type OrganizationLifecycleResult,
} from '../../api/adminRegistrationApi'
import { ApiError } from '../../api/client'
import { OrganizationEditDialog } from './OrganizationEditDialog'
import { OrganizationLifecycleDialog, type LifecycleTarget } from './OrganizationLifecycleDialog'

export type OrgAction = 'edit' | 'suspend' | 'activate' | 'close'

/**
 * Edit, suspend, activate, close and licensed seats of one organization (REQ-TEN-001), moved from
 * the old Registrations list to the organization's detail page (REQ-TEN-007). `initialAction` opens
 * the matching dialog when the page is reached from a row menu.
 */
export function OrganizationActions({ organization, onChanged, initialAction }: {
  organization: OrganizationAdmin
  onChanged: (updated: OrganizationAdmin) => void
  initialAction?: OrgAction | null
}) {
  const { t } = useTranslation()
  const [editing, setEditing] = useState<OrganizationAdmin | null>(initialAction === 'edit' ? organization : null)
  const [target, setTarget] = useState<LifecycleTarget | null>(
    initialAction && initialAction !== 'edit' ? { organization, action: initialAction } : null)
  const [notice, setNotice] = useState<{ message: string; failures: string[] } | null>(null)
  const [editingSeats, setEditingSeats] = useState(false)
  const [seatInput, setSeatInput] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const org = organization

  const saveSeats = async () => {
    const parsed = Number(seatInput)
    if (!Number.isInteger(parsed) || parsed < 0) return
    setSaving(true)
    try {
      const result = await adminRegistrationApi.updateSeats(org.id, parsed)
      onChanged({ ...org, licensedSeats: result.licensedSeats })
      setEditingSeats(false)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('orgDirectory.detail.seatsError'))
    } finally {
      setSaving(false)
    }
  }

  const onLifecycleDone = (result: OrganizationLifecycleResult, action: OrganizationLifecycleAction) => {
    onChanged(result.organization)
    setTarget(null)
    setNotice({
      message: t(`adminOrgLifecycle.done.${action}`, { name: result.organization.name, count: result.accountsUpdated }),
      failures: result.accountsNotUpdated,
    })
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      {notice && (
        <Alert severity={notice.failures.length ? 'warning' : 'success'} onClose={() => setNotice(null)}>
          {notice.message}
          {notice.failures.length > 0 && <> {t('adminOrgLifecycle.partialFailure', { emails: notice.failures.join(', ') })}</>}
        </Alert>
      )}
      {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
      <OrganizationEditDialog organization={editing} onClose={() => setEditing(null)}
        onSaved={(updated) => { onChanged(updated); setEditing(null) }} />
      <OrganizationLifecycleDialog target={target} onClose={() => setTarget(null)} onDone={onLifecycleDone} />
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
        {editingSeats ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <TextField size="small" type="number" value={seatInput} onChange={(e) => setSeatInput(e.target.value)} sx={{ width: 90 }}
              slotProps={{ htmlInput: { min: 0, 'aria-label': t('orgDirectory.detail.seatCount') } }} />
            <IconButton aria-label={t('orgDirectory.detail.saveSeats')} size="small" disabled={saving} onClick={() => void saveSeats()}><Check size={14} /></IconButton>
            <IconButton aria-label={t('orgDirectory.detail.cancelSeats')} size="small" disabled={saving} onClick={() => setEditingSeats(false)}><X size={14} /></IconButton>
          </Box>
        ) : (
          <Chip size="small" label={t('orgDirectory.detail.seatsChip', { used: org.activeMemberCount, total: org.licensedSeats })}
            onDelete={() => { setSeatInput(String(org.licensedSeats)); setEditingSeats(true) }} deleteIcon={<Pencil size={12} />} />
        )}
        <Button size="small" variant="outlined" aria-label={t('adminOrgLifecycle.editLabel', { name: org.name })}
          disabled={org.lifecycleStatus === 'CLOSED'} onClick={() => setEditing(org)}>
          {t('adminOrgLifecycle.editButton')}
        </Button>
        {org.lifecycleStatus === 'ACTIVE' && (
          <Button size="small" variant="outlined" color="warning" aria-label={t('adminOrgLifecycle.suspendLabel', { name: org.name })}
            onClick={() => setTarget({ organization: org, action: 'suspend' })}>
            {t('adminOrgLifecycle.suspendButton')}
          </Button>
        )}
        {org.lifecycleStatus !== 'ACTIVE' && (
          <Button size="small" variant="outlined" aria-label={t('adminOrgLifecycle.activateLabel', { name: org.name })}
            onClick={() => setTarget({ organization: org, action: 'activate' })}>
            {t('adminOrgLifecycle.activateButton')}
          </Button>
        )}
        {org.lifecycleStatus !== 'CLOSED' && (
          <Button size="small" variant="outlined" color="error" aria-label={t('adminOrgLifecycle.closeLabel', { name: org.name })}
            onClick={() => setTarget({ organization: org, action: 'close' })}>
            {t('adminOrgLifecycle.closeButton')}
          </Button>
        )}
      </Box>
    </Box>
  )
}
