import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Checkbox, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControlLabel, FormGroup, IconButton, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow,
  TextField, Typography,
} from '@mui/material'
import { Pencil, Trash2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import { permissionsApi, type Permission } from '../../api/permissionsApi'
import { rolesApi, type Role, type RoleScope } from '../../api/rolesApi'
import { PageHeader } from '../../components/layout/PageHeader'

const SCOPES: RoleScope[] = ['PLATFORM', 'ORGANIZATION']

/** null = closed; 'new' = create; otherwise the role being edited. */
type Editing = null | 'new' | Role

/**
 * "/admin/roles" — REQ-IAM-003 (sprint 2026.3.3, 06.02.01 Create role). Every
 * rule (unique name, organization-scope names limited to ORG_ADMIN/MEMBER,
 * system-managed roles not deletable, name and scope immutable) is enforced by
 * RoleAdminService; this page shows the backend's messages as returned.
 */
export function RolesAdminPage() {
  const { t } = useTranslation()
  const [roles, setRoles] = useState<Role[] | null>(null)
  const [permissions, setPermissions] = useState<Permission[]>([])
  const [error, setError] = useState<string | null>(null)

  const [editing, setEditing] = useState<Editing>(null)
  const [name, setName] = useState('')
  const [scope, setScope] = useState<RoleScope>('PLATFORM')
  const [description, setDescription] = useState('')
  const [selected, setSelected] = useState<Set<number>>(new Set())
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    Promise.all([rolesApi.list(), permissionsApi.list()])
      .then(([r, p]) => { setRoles(r); setPermissions(p) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('adminRbac.loadError')))
  }, [t])

  useEffect(load, [load])

  const open = (target: Editing) => {
    setEditing(target)
    setFormError(null)
    if (target && target !== 'new') {
      setName(target.name)
      setScope(target.scope)
      setDescription(target.description ?? '')
      setSelected(new Set(permissions.filter((p) => target.permissionNames.includes(p.name)).map((p) => p.id)))
    } else {
      setName('')
      setScope('PLATFORM')
      setDescription('')
      setSelected(new Set())
    }
  }

  const togglePermission = (id: number) => {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  const save = async () => {
    setSaving(true)
    setFormError(null)
    try {
      if (editing === 'new') await rolesApi.create({ name, scope, description, permissionIds: [...selected] })
      else if (editing) await rolesApi.update(editing.id, { description, permissionIds: [...selected] })
      setEditing(null)
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('adminRbac.saveError'))
    } finally {
      setSaving(false)
    }
  }

  const remove = async (role: Role) => {
    setError(null)
    try {
      await rolesApi.remove(role.id)
      load()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('adminRbac.deleteError'))
    }
  }

  const isNew = editing === 'new'

  return (
    <>
      <PageHeader
        title={t('adminRbac.rolesTitle')}
        subtitle={t('adminRbac.rolesSubtitle')}
        action={<Button variant="contained" onClick={() => open('new')}>{t('adminRbac.newRole')}</Button>}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {!error && roles === null && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
      )}

      {roles && (
        <Paper variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>{t('adminRbac.name')}</TableCell>
                <TableCell>{t('adminRbac.scope')}</TableCell>
                <TableCell>{t('adminRbac.description')}</TableCell>
                <TableCell>{t('adminRbac.permissions')}</TableCell>
                <TableCell align="right">{t('adminRbac.actions')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {roles.length === 0 && (
                <TableRow><TableCell colSpan={5} sx={{ color: 'text.secondary' }}>{t('adminRbac.empty')}</TableCell></TableRow>
              )}
              {roles.map((role) => (
                <TableRow key={role.id}>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography sx={{ fontWeight: 600 }}>{role.name}</Typography>
                      {role.systemManaged && <Chip size="small" label={t('adminRbac.systemManaged')} />}
                    </Box>
                  </TableCell>
                  <TableCell>{role.scope}</TableCell>
                  <TableCell>{role.description}</TableCell>
                  <TableCell>{role.permissionNames.join(', ')}</TableCell>
                  <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                    <IconButton aria-label={t('adminRbac.editLabel', { name: role.name })} onClick={() => open(role)}>
                      <Pencil size={16} />
                    </IconButton>
                    <IconButton aria-label={t('adminRbac.deleteLabel', { name: role.name })} onClick={() => remove(role)}>
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
        <DialogTitle>{isNew ? t('adminRbac.newRole') : t('adminRbac.editRole', { name })}</DialogTitle>
        <DialogContent>
          {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label={t('adminRbac.name')} required={isNew} value={name} onChange={(e) => setName(e.target.value)}
              disabled={!isNew} helperText={isNew ? undefined : t('adminRbac.immutableHint')}
            />
            <TextField select label={t('adminRbac.scope')} required={isNew} value={scope} onChange={(e) => setScope(e.target.value as RoleScope)} disabled={!isNew}>
              {SCOPES.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
            </TextField>
            <TextField label={t('adminRbac.description')} value={description} onChange={(e) => setDescription(e.target.value)} multiline />
            <Box component="fieldset" sx={{ border: 0, p: 0, m: 0 }}>
              <Typography component="legend" variant="subtitle2" sx={{ mb: 0.5 }}>{t('adminRbac.permissions')}</Typography>
              <FormGroup>
                {permissions.map((p) => (
                  <FormControlLabel
                    key={p.id}
                    control={<Checkbox checked={selected.has(p.id)} onChange={() => togglePermission(p.id)} />}
                    label={p.name}
                  />
                ))}
              </FormGroup>
            </Box>
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
