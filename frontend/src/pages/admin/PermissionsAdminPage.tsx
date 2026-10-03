import { ShieldCheck as PHShieldCheck } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, IconButton,
  Paper, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { Pencil, Trash2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import { permissionsApi, type Permission } from '../../api/permissionsApi'
import { PageHeader } from '../../components/layout/PageHeader'

/** null = closed; 'new' = create; otherwise the permission being edited. */
type Editing = null | 'new' | Permission

/**
 * "/admin/permissions" — REQ-IAM-003 (sprint 2026.3.3, 06.02.01 Define
 * permission). Unique, immutable names; system-managed permissions and
 * permissions still granted by a role cannot be deleted — all enforced by
 * PermissionAdminService, whose messages are shown as returned.
 */
export function PermissionsAdminPage() {
  const { t } = useTranslation()
  const [permissions, setPermissions] = useState<Permission[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [editing, setEditing] = useState<Editing>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    permissionsApi.list()
      .then(setPermissions)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('adminRbac.loadError')))
  }, [t])

  useEffect(load, [load])

  const open = (target: Editing) => {
    setEditing(target)
    setFormError(null)
    setName(target && target !== 'new' ? target.name : '')
    setDescription(target && target !== 'new' ? target.description ?? '' : '')
  }

  const save = async () => {
    setSaving(true)
    setFormError(null)
    try {
      if (editing === 'new') await permissionsApi.create({ name, description })
      else if (editing) await permissionsApi.update(editing.id, { description })
      setEditing(null)
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('adminRbac.saveError'))
    } finally {
      setSaving(false)
    }
  }

  const remove = async (permission: Permission) => {
    setError(null)
    try {
      await permissionsApi.remove(permission.id)
      load()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('adminRbac.deleteError'))
    }
  }

  const isNew = editing === 'new'

  return (
    <>
      <PageHeader icon={PHShieldCheck} accent="indigo" area="accessControl"
        title={t('adminRbac.permissionsTitle')}
        subtitle={t('adminRbac.permissionsSubtitle')}
        action={<Button variant="contained" onClick={() => open('new')}>{t('adminRbac.newPermission')}</Button>}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {!error && permissions === null && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
      )}

      {permissions && (
        <Paper variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>{t('adminRbac.name')}</TableCell>
                <TableCell>{t('adminRbac.description')}</TableCell>
                <TableCell align="right">{t('adminRbac.roleCount')}</TableCell>
                <TableCell align="right">{t('adminRbac.actions')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {permissions.length === 0 && (
                <TableRow><TableCell colSpan={4} sx={{ color: 'text.secondary' }}>{t('adminRbac.empty')}</TableCell></TableRow>
              )}
              {permissions.map((permission) => (
                <TableRow key={permission.id}>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography sx={{ fontWeight: 600 }}>{permission.name}</Typography>
                      {permission.systemManaged && <Chip size="small" label={t('adminRbac.systemManaged')} />}
                    </Box>
                  </TableCell>
                  <TableCell>{permission.description}</TableCell>
                  <TableCell align="right">{permission.roleCount}</TableCell>
                  <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                    <IconButton aria-label={t('adminRbac.editLabel', { name: permission.name })} onClick={() => open(permission)}>
                      <Pencil size={16} />
                    </IconButton>
                    <IconButton aria-label={t('adminRbac.deleteLabel', { name: permission.name })} onClick={() => remove(permission)}>
                      <Trash2 size={16} />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}

      <Dialog open={editing !== null} onClose={() => setEditing(null)} fullWidth maxWidth="sm">
        <DialogTitle>{isNew ? t('adminRbac.newPermission') : t('adminRbac.editPermission', { name })}</DialogTitle>
        <DialogContent>
          {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label={t('adminRbac.name')} required={isNew} value={name} onChange={(e) => setName(e.target.value)}
              disabled={!isNew} helperText={isNew ? undefined : t('adminRbac.immutableNameHint')}
            />
            <TextField label={t('adminRbac.description')} value={description} onChange={(e) => setDescription(e.target.value)} multiline />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditing(null)}>{t('adminRbac.cancel')}</Button>
          <Button variant="contained" disabled={saving || (isNew && !name)} onClick={save}>
            {isNew ? t('adminRbac.create') : t('adminRbac.save')}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
