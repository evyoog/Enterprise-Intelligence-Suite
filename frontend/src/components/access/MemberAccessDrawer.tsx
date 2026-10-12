import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Divider, Drawer, IconButton, MenuItem, TextField, Typography, useMediaQuery, useTheme } from '@mui/material'
import { X } from 'lucide-react'
import { accessApi, type AccessMemberDetail } from '../../api/accessApi'
import { ApiError } from '../../api/client'
import { organizationApi } from '../../api/registrationApi'
import { PermissionToggleRow, ProductAccessCard, StickySaveBar } from './AccessControls'

interface Props {
  memberId: number | null
  onClose: () => void
  /** Called after anything was saved, so the list can refresh. */
  onChanged: () => void
}

interface Staged {
  permissions: Record<string, boolean>
  products: Record<number, boolean>
  reset: string[]
}

const EMPTY: Staged = { permissions: {}, products: {}, reset: [] }

/**
 * The member access panel (REQ-TEN-005): a member's products and feature permissions with where each comes from.
 * Changes are staged and saved together; the backend enforces every guard rail, the panel only shows why a switch is
 * locked. Individual access can be reset to the role default.
 */
export function MemberAccessDrawer({ memberId, onClose, onChanged }: Props) {
  const { t, i18n } = useTranslation()
  const theme = useTheme()
  const fullScreen = useMediaQuery(theme.breakpoints.down('md'))
  const [detail, setDetail] = useState<AccessMemberDetail | null>(null)
  const [staged, setStaged] = useState<Staged>(EMPTY)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback((id: number) => {
    return accessApi.member(id).then((d) => { setError(null); setDetail(d); setStaged(EMPTY) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('access.loadError')))
  }, [t])

  // The parent keys this component by member, so each member starts from fresh state.
  useEffect(() => {
    if (memberId !== null) void load(memberId)
  }, [memberId, load])

  const dirty = useMemo(
    () => Object.keys(staged.permissions).length + Object.keys(staged.products).length + staged.reset.length,
    [staged],
  )
  const readOnly = !detail?.canChange

  const togglePermission = (code: string, original: boolean, next: boolean) =>
    setStaged((s) => {
      const permissions = { ...s.permissions }
      if (next === original) delete permissions[code]; else permissions[code] = next
      return { ...s, permissions, reset: s.reset.filter((r) => r !== `permission:${code}`) }
    })
  const toggleProduct = (id: number, original: boolean, next: boolean) =>
    setStaged((s) => {
      const products = { ...s.products }
      if (next === original) delete products[id]; else products[id] = next
      return { ...s, products, reset: s.reset.filter((r) => r !== `product:${id}`) }
    })
  const stageReset = (key: string, kind: 'permission' | 'product', id: string) =>
    setStaged((s) => {
      const permissions = { ...s.permissions }
      const products = { ...s.products }
      if (kind === 'permission') delete permissions[id]; else delete products[Number(id)]
      return { permissions, products, reset: s.reset.includes(key) ? s.reset : [...s.reset, key] }
    })

  const save = () => {
    if (memberId === null) return
    setBusy(true)
    setError(null)
    accessApi.setOverrides(memberId, { permissions: staged.permissions, products: staged.products, reset: staged.reset })
      .then((d) => { setDetail(d); setStaged(EMPTY); setSaved(t('access.savedFor', { name: d.name })); onChanged() })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('access.saveError')))
      .finally(() => setBusy(false))
  }
  const resetAll = () => {
    if (memberId === null || !window.confirm(t('access.resetAllConfirm'))) return
    setBusy(true)
    accessApi.resetAll(memberId)
      .then((d) => { setDetail(d); setStaged(EMPTY); setSaved(t('access.savedFor', { name: d.name })); onChanged() })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('access.saveError')))
      .finally(() => setBusy(false))
  }
  const markReviewed = () => {
    if (memberId === null) return
    organizationApi.reviewMemberAccess(memberId).then(() => load(memberId)).then(onChanged)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('access.saveError')))
  }
  const changeRole = (role: 'ORG_ADMIN' | 'MEMBER') => {
    if (memberId === null || !detail || role === detail.role) return
    setBusy(true)
    organizationApi.changeMemberRole(memberId, role).then(() => load(memberId)).then(onChanged)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('access.saveError')))
      .finally(() => setBusy(false))
  }
  const close = () => {
    if (dirty > 0 && !window.confirm(t('access.discardConfirm'))) return
    onClose()
  }

  const areas = useMemo(() => {
    const groups = new Map<string, AccessMemberDetail['permissions']>()
    detail?.permissions.forEach((p) => groups.set(p.area, [...(groups.get(p.area) ?? []), p]))
    return [...groups.entries()]
  }, [detail])

  const reviewedText = detail?.lastReviewedAt
    ? t('access.reviewedOn', { date: new Date(detail.lastReviewedAt).toLocaleDateString(i18n.language), by: detail.lastReviewedBy ?? '—' })
    : t('access.neverReviewed')

  return (
    <Drawer anchor="right" open={memberId !== null} onClose={close}
      slotProps={{ paper: { 'aria-labelledby': 'member-access-title', sx: { width: fullScreen ? '100%' : 560, display: 'flex', flexDirection: 'column' } } }}>
      <Box component="section" aria-label={detail?.name ?? t('access.manage')} sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box sx={{ p: 2.5, flex: 1, overflowY: 'auto' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1 }}>
          <Box>
            <Typography id="member-access-title" variant="h6" component="h2">{detail?.name ?? t('access.loading')}</Typography>
            {detail?.email && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{detail.email}</Typography>}
          </Box>
          <IconButton aria-label={t('access.close')} onClick={close}><X size={18} /></IconButton>
        </Box>

        {error && <Alert severity="error" sx={{ mt: 1.5 }}>{error}</Alert>}
        {saved && <Alert severity="success" sx={{ mt: 1.5 }} onClose={() => setSaved(null)}>{saved}</Alert>}

        {detail && (
          <>
            {readOnly && detail.memberLockedReason && <Alert severity="info" sx={{ mt: 1.5 }}>{detail.memberLockedReason}</Alert>}
            <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', flexWrap: 'wrap', mt: 2 }}>
              <TextField select size="small" label={t('access.role')} value={detail.role} disabled={readOnly || busy}
                onChange={(e) => changeRole(e.target.value as 'ORG_ADMIN' | 'MEMBER')} sx={{ minWidth: 200 }}>
                <MenuItem value="ORG_ADMIN">{t('access.roles.ORG_ADMIN')}</MenuItem>
                <MenuItem value="MEMBER">{t('access.roles.MEMBER')}</MenuItem>
              </TextField>
              <Typography variant="body2" sx={{ color: 'text.secondary', flex: 1 }}>{reviewedText}</Typography>
              <Button size="small" variant="outlined" disabled={readOnly} onClick={markReviewed}>{t('access.markReviewed')}</Button>
            </Box>

            <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 700, mt: 3, mb: 1 }}>{t('access.productAccess')}</Typography>
            {detail.products.length === 0 && <Typography sx={{ color: 'text.secondary' }}>{t('access.noSubscriptions')}</Typography>}
            {detail.products.map((p) => {
              const staged_ = staged.products[p.productId]
              const value = staged_ ?? p.enabled
              return (
                <ProductAccessCard key={p.productId} name={p.name} plan={p.plan} seats={p.seats} checked={value} source={p.source}
                  disabledReason={readOnly ? detail.memberLockedReason : (p.lockedReason && !p.enabled ? p.lockedReason : p.source === 'PROJECT' ? p.lockedReason : null)}
                  changed={staged_ !== undefined} resetPending={staged.reset.includes(`product:${p.productId}`)}
                  onChange={(next) => toggleProduct(p.productId, p.enabled, next)}
                  onReset={() => stageReset(`product:${p.productId}`, 'product', String(p.productId))} />
              )
            })}

            <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 700, mt: 3, mb: 0.5 }}>{t('access.featurePermissions')}</Typography>
            {areas.map(([area, items]) => {
              const on = items.filter((i) => (staged.permissions[i.code] ?? i.enabled)).length
              return (
                <Box key={area} component="section" aria-label={t(`access.areas.${area}`)} sx={{ mb: 1.5 }}>
                  <Typography variant="overline" sx={{ color: 'text.secondary' }}>
                    {t(`access.areas.${area}`)} · {t('access.countOn', { on, total: items.length })}
                  </Typography>
                  {items.map((p) => {
                    const staged_ = staged.permissions[p.code]
                    return (
                      <PermissionToggleRow key={p.code} name={p.name} description={p.description} code={p.code}
                        checked={staged_ ?? p.enabled} source={p.source}
                        disabledReason={readOnly ? detail.memberLockedReason : p.lockedReason}
                        changed={staged_ !== undefined} resetPending={staged.reset.includes(`permission:${p.code}`)}
                        onChange={(next) => togglePermission(p.code, p.enabled, next)}
                        onReset={() => stageReset(`permission:${p.code}`, 'permission', p.code)} />
                    )
                  })}
                  <Divider />
                </Box>
              )
            })}
          </>
        )}
      </Box>
      {detail && !readOnly && (
        <StickySaveBar dirtyCount={dirty} busy={busy} onSave={save} onCancel={() => setStaged(EMPTY)}
          secondary={<Button color="inherit" size="small" onClick={resetAll} disabled={busy}>{t('access.resetAll')}</Button>} />
      )}
      </Box>
    </Drawer>
  )
}
