import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, List, ListItem, ListItemText, ToggleButton, ToggleButtonGroup, Typography } from '@mui/material'
import { accessApi, type RoleDefaults, type RoleDefaultsResult } from '../../api/accessApi'
import { ApiError } from '../../api/client'
import { PermissionToggleRow, ProductAccessCard, StickySaveBar } from './AccessControls'

type Role = 'ORG_ADMIN' | 'MEMBER'

/**
 * The Role defaults tab (REQ-TEN-005.2): which products and feature permissions every member of a role gets. Saving
 * first shows the impact; members with an individual override keep it. Organization admins are locked on (BR-6).
 */
export function RoleDefaultsTab() {
  const { t } = useTranslation()
  const [role, setRole] = useState<Role>('MEMBER')
  return (
    <Box>
      <ToggleButtonGroup exclusive size="small" value={role} onChange={(_, v) => v && setRole(v)} aria-label={t('access.role')} sx={{ mb: 2 }}>
        <ToggleButton value="MEMBER">{t('access.roles.MEMBER')}</ToggleButton>
        <ToggleButton value="ORG_ADMIN">{t('access.roles.ORG_ADMIN')}</ToggleButton>
      </ToggleButtonGroup>
      <RoleDefaultsEditor key={role} role={role} />
    </Box>
  )
}

function RoleDefaultsEditor({ role }: { role: Role }) {
  const { t } = useTranslation()
  const [defaults, setDefaults] = useState<RoleDefaults | null>(null)
  const [permissions, setPermissions] = useState<Record<string, boolean>>({})
  const [products, setProducts] = useState<Record<number, boolean>>({})
  const [impact, setImpact] = useState<RoleDefaultsResult | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  // Keyed by role in the parent, so each role starts from fresh state.
  useEffect(() => {
    accessApi.roleDefaults(role).then(setDefaults).catch((e) => setError(e instanceof ApiError ? e.message : t('access.loadError')))
  }, [role, t])

  const dirty = Object.keys(permissions).length + Object.keys(products).length
  const areas = useMemo(() => {
    const groups = new Map<string, RoleDefaults['permissions']>()
    defaults?.permissions.forEach((p) => groups.set(p.area, [...(groups.get(p.area) ?? []), p]))
    return [...groups.entries()]
  }, [defaults])

  const request = () => ({ permissions, products })
  const review = () => {
    setBusy(true)
    setError(null)
    accessApi.saveRoleDefaults(role, request(), true).then(setImpact)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('access.saveError')))
      .finally(() => setBusy(false))
  }
  const apply = () => {
    setBusy(true)
    accessApi.saveRoleDefaults(role, request(), false)
      .then((r) => { setDefaults(r.defaults); setPermissions({}); setProducts({}); setImpact(null); setSaved(t('access.defaultsSaved', { count: r.affectedMembers })) })
      .catch((e) => { setImpact(null); setError(e instanceof ApiError ? e.message : t('access.saveError')) })
      .finally(() => setBusy(false))
  }
  const describeItem = (i: RoleDefaultsResult['items'][number]) =>
    t('access.impactItem', { name: i.name, from: i.from ? t('access.on') : t('access.off'), to: i.to ? t('access.on') : t('access.off'), count: i.affectedMembers })

  return (
    <Box>
      {defaults && <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('access.roleMembers', { count: defaults.memberCount })} {t('access.roleDefaultsIntro')}</Typography>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {saved && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSaved(null)}>{saved}</Alert>}

      {defaults && (
        <>
          <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 700, mb: 1 }}>{t('access.productAccess')}</Typography>
          {defaults.products.length === 0 && <Typography sx={{ color: 'text.secondary' }}>{t('access.noSubscriptions')}</Typography>}
          {defaults.products.map((p) => (
            <ProductAccessCard key={p.productId} name={p.name} checked={products[p.productId] ?? p.enabled} source={p.customized ? 'OVERRIDE' : 'ROLE'}
              changed={products[p.productId] !== undefined}
              onChange={(next) => setProducts((s) => { const n = { ...s }; if (next === p.enabled) delete n[p.productId]; else n[p.productId] = next; return n })} />
          ))}

          <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 700, mt: 3, mb: 0.5 }}>{t('access.featurePermissions')}</Typography>
          {role === 'ORG_ADMIN' && <Alert severity="info" sx={{ mb: 1 }}>{t('access.adminLocked')}</Alert>}
          {areas.map(([area, items]) => (
            <Box key={area} component="section" aria-label={t(`access.areas.${area}`)} sx={{ mb: 1.5 }}>
              <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t(`access.areas.${area}`)}</Typography>
              {items.map((p) => (
                <PermissionToggleRow key={p.code} name={p.name} description={p.description} code={p.code}
                  checked={permissions[p.code] ?? p.enabled} source={p.locked ? 'LOCKED' : p.customized ? 'OVERRIDE' : 'ROLE'}
                  disabledReason={p.locked ? t('access.adminLockedShort') : null} changed={permissions[p.code] !== undefined}
                  onChange={(next) => setPermissions((s) => { const n = { ...s }; if (next === p.enabled) delete n[p.code]; else n[p.code] = next; return n })} />
              ))}
            </Box>
          ))}
          <StickySaveBar dirtyCount={dirty} busy={busy} onSave={review} saveLabel={t('access.reviewChanges')}
            onCancel={() => { setPermissions({}); setProducts({}) }} />
        </>
      )}

      <Dialog open={impact !== null} onClose={() => setImpact(null)} fullWidth maxWidth="sm" aria-labelledby="role-impact-title">
        <DialogTitle id="role-impact-title">{t('access.impactTitle')}</DialogTitle>
        <DialogContent>
          <Alert severity="info">{t('access.impactSummary', { count: impact?.affectedMembers ?? 0 })}</Alert>
          <List dense>
            {impact?.items.map((i) => <ListItem key={i.item} disableGutters><ListItemText primary={describeItem(i)} /></ListItem>)}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setImpact(null)}>{t('access.cancel')}</Button>
          <Button variant="contained" onClick={apply} disabled={busy}>{t('access.apply')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
