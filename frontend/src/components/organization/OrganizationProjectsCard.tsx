import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, MenuItem, Paper, TextField, Typography } from '@mui/material'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import { projectsApi, type Project } from '../../api/projectsApi'
import { organizationApi, type OrgMember, type OrgProductAccess } from '../../api/registrationApi'

interface Props {
  /** MANAGE_USERS: create, rename, delete and manage members. */
  canManageUsers: boolean
  /** MANAGE_PRODUCT_ACCESS: manage the products of a project. */
  canManageProducts: boolean
}

/**
 * 05.04.02 Projects (REQ-TEN-009, C88): a group of members plus products. Members of a project can use its
 * products with the project's role. The backend enforces every permission; this card only hides what the caller
 * cannot change, and hides itself on a 403 or 404.
 */
export function OrganizationProjectsCard({ canManageUsers, canManageProducts }: Props) {
  const { t } = useTranslation()
  const [projects, setProjects] = useState<Project[] | null>(null)
  const [members, setMembers] = useState<OrgMember[]>([])
  const [products, setProducts] = useState<OrgProductAccess[]>([])
  const [hidden, setHidden] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [newName, setNewName] = useState('')
  const [newDescription, setNewDescription] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)
  const [addingMember, setAddingMember] = useState<Record<number, string>>({})
  const [addingProduct, setAddingProduct] = useState<Record<number, string>>({})
  const [productRole, setProductRole] = useState<Record<number, string>>({})
  const [editing, setEditing] = useState<{ id: number; name: string; description: string } | null>(null)

  useEffect(() => {
    projectsApi.list()
      .then(setProjects)
      .catch((e) => {
        if (e instanceof ApiError && (e.status === 403 || e.status === 404)) setHidden(true)
        else setError(e instanceof ApiError ? e.message : t('projects.loadError'))
      })
    if (canManageUsers) organizationApi.listMyOrgUsers().then(setMembers).catch(() => setMembers([]))
    if (canManageProducts) organizationApi.listMyOrgProducts().then(setProducts).catch(() => setProducts([]))
  }, [t, canManageUsers, canManageProducts])

  if (hidden) return null

  const displayName = (m: { firstName?: string; lastName?: string; email?: string; customerId: number }) =>
    [m.firstName, m.lastName].filter(Boolean).join(' ') || m.email || String(m.customerId)
  const replace = (updated: Project) => setProjects((prev) => prev?.map((p) => (p.id === updated.id ? updated : p)) ?? prev)
  const run = (id: number | null, call: () => Promise<unknown>, fallback: string) => {
    setBusyId(id)
    setError(null)
    return call().catch((e) => setError(e instanceof ApiError ? e.message : fallback)).finally(() => setBusyId(null))
  }

  const create = () => {
    if (!newName.trim()) return
    run(0, () => projectsApi.create(newName.trim(), newDescription.trim() || undefined).then((p) => {
      setProjects((prev) => [...(prev ?? []), p])
      setNewName('')
      setNewDescription('')
    }), t('projects.createError'))
  }
  const save = () => {
    if (!editing || !editing.name.trim()) return
    run(editing.id, () => projectsApi.update(editing.id, editing.name.trim(), editing.description.trim() || undefined).then((p) => {
      replace(p)
      setEditing(null)
    }), t('projects.updateError'))
  }
  const remove = (p: Project) =>
    run(p.id, () => projectsApi.remove(p.id).then(() => setProjects((prev) => prev?.filter((x) => x.id !== p.id) ?? prev)), t('projects.deleteError'))
  const addMember = (p: Project) => {
    const id = Number(addingMember[p.id])
    if (id) run(p.id, () => projectsApi.addMember(p.id, id).then(replace).then(() => setAddingMember((s) => ({ ...s, [p.id]: '' }))), t('projects.addMemberError'))
  }
  const removeMember = (p: Project, memberId: number) =>
    run(p.id, () => projectsApi.removeMember(p.id, memberId).then(replace), t('projects.removeMemberError'))
  const addProduct = (p: Project) => {
    const id = Number(addingProduct[p.id])
    const role = (productRole[p.id] ?? '').trim()
    if (id && role) {
      run(p.id, () => projectsApi.setProduct(p.id, id, role).then(replace).then(() => {
        setAddingProduct((s) => ({ ...s, [p.id]: '' }))
        setProductRole((s) => ({ ...s, [p.id]: '' }))
      }), t('projects.addProductError'))
    }
  }
  const removeProduct = (p: Project, productId: number) =>
    run(p.id, () => projectsApi.removeProduct(p.id, productId).then(replace), t('projects.removeProductError'))

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }} component="section" aria-labelledby="org-projects-title">
      <Typography id="org-projects-title" variant="subtitle1" component="h3" sx={{ fontWeight: 700, mb: 0.5 }}>
        {t('projects.title')}
      </Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('projects.intro')}</Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}

      {canManageUsers && (
        <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
          <TextField size="small" label={t('projects.newName')} value={newName} onChange={(e) => setNewName(e.target.value)} sx={{ flex: 1, minWidth: 200, maxWidth: 320 }} />
          <TextField size="small" label={t('projects.description')} value={newDescription} onChange={(e) => setNewDescription(e.target.value)} sx={{ flex: 2, minWidth: 200 }} />
          <Button size="small" variant="outlined" startIcon={<Plus size={14} />} disabled={busyId === 0 || !newName.trim()} onClick={create}>
            {t('projects.create')}
          </Button>
        </Box>
      )}

      {projects && projects.length === 0 && <Typography sx={{ color: 'text.secondary' }}>{t('projects.noProjects')}</Typography>}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {projects?.map((p) => {
          const memberIds = new Set(p.members.map((m) => m.memberId))
          const availableMembers = members.filter((m) => !memberIds.has(m.organizationMemberId) && m.status === 'ACTIVE')
          const subscribed = products.filter((x) => x.orgSubscriptionStatus === 'ACTIVE')
          const busy = busyId === p.id
          const isEditing = editing?.id === p.id
          return (
            <Box key={p.id} component="article" aria-label={p.name} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, p: 1.5 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1, mb: 1 }}>
                {isEditing ? (
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', flex: 1 }}>
                    <TextField size="small" label={t('projects.newName')} value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} />
                    <TextField size="small" label={t('projects.description')} value={editing.description} onChange={(e) => setEditing({ ...editing, description: e.target.value })} sx={{ flex: 1, minWidth: 200 }} />
                    <Button size="small" disabled={busy || !editing.name.trim()} onClick={save}>{t('projects.save')}</Button>
                    <Button size="small" onClick={() => setEditing(null)}>{t('projects.cancel')}</Button>
                  </Box>
                ) : (
                  <Box>
                    <Typography sx={{ fontWeight: 600 }}>{p.name}</Typography>
                    {p.description && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{p.description}</Typography>}
                  </Box>
                )}
                {canManageUsers && !isEditing && (
                  <Box sx={{ display: 'flex', gap: 0.5, flexShrink: 0 }}>
                    <Button size="small" startIcon={<Pencil size={14} />} aria-label={`${t('projects.edit')} ${p.name}`}
                      onClick={() => setEditing({ id: p.id, name: p.name, description: p.description ?? '' })}>
                      {t('projects.edit')}
                    </Button>
                    <Button size="small" color="error" startIcon={<Trash2 size={14} />} disabled={busy}
                      aria-label={`${t('projects.delete')} ${p.name}`} onClick={() => remove(p)}>
                      {t('projects.delete')}
                    </Button>
                  </Box>
                )}
              </Box>

              <Typography variant="overline" component="h4" sx={{ color: 'text.secondary' }}>{t('projects.members')} ({p.members.length})</Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: 1 }}>
                {p.members.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('projects.noMembers')}</Typography>}
                {p.members.map((m) => (
                  <Chip key={m.memberId} size="small" label={m.name || m.email}
                    onDelete={canManageUsers ? () => removeMember(p, m.memberId) : undefined} disabled={busy}
                    deleteIcon={canManageUsers ? <Trash2 size={12} aria-label={`${t('projects.removeMember')} ${m.name || m.email}`} /> : undefined} />
                ))}
              </Box>
              {canManageUsers && availableMembers.length > 0 && (
                <Box sx={{ display: 'flex', gap: 1, mb: 1.5 }}>
                  <TextField select size="small" label={`${t('projects.addMember')} · ${p.name}`} sx={{ minWidth: 220 }}
                    value={addingMember[p.id] ?? ''} onChange={(e) => setAddingMember((s) => ({ ...s, [p.id]: e.target.value }))}>
                    {availableMembers.map((m) => <MenuItem key={m.organizationMemberId} value={m.organizationMemberId}>{displayName(m)}</MenuItem>)}
                  </TextField>
                  <Button size="small" disabled={busy || !addingMember[p.id]} onClick={() => addMember(p)}>{t('projects.add')}</Button>
                </Box>
              )}

              <Typography variant="overline" component="h4" sx={{ color: 'text.secondary' }}>{t('projects.products')} ({p.products.length})</Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: 1 }}>
                {p.products.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('projects.noProducts')}</Typography>}
                {p.products.map((x) => (
                  <Chip key={x.productId} size="small" color="primary" variant="outlined" label={`${x.productName} · ${x.productRole}`}
                    onDelete={canManageProducts ? () => removeProduct(p, x.productId) : undefined} disabled={busy}
                    deleteIcon={canManageProducts ? <Trash2 size={12} aria-label={`${t('projects.removeProduct')} ${x.productName}`} /> : undefined} />
                ))}
              </Box>
              {canManageProducts && (
                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  <TextField select size="small" label={`${t('projects.addProduct')} · ${p.name}`} sx={{ minWidth: 220 }}
                    value={addingProduct[p.id] ?? ''} onChange={(e) => setAddingProduct((s) => ({ ...s, [p.id]: e.target.value }))}>
                    {subscribed.map((x) => <MenuItem key={x.productId} value={x.productId}>{x.productName}</MenuItem>)}
                  </TextField>
                  <TextField size="small" label={`${t('projects.role')} · ${p.name}`} value={productRole[p.id] ?? ''}
                    onChange={(e) => setProductRole((s) => ({ ...s, [p.id]: e.target.value }))} sx={{ width: 180 }} />
                  <Button size="small" disabled={busy || !addingProduct[p.id] || !(productRole[p.id] ?? '').trim()} onClick={() => addProduct(p)}>
                    {t('projects.add')}
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
