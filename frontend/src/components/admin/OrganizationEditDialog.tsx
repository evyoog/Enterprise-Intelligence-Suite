import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem,
  TextField, Typography,
} from '@mui/material'
import {
  adminRegistrationApi,
  type OrganizationAdmin,
  type UpdateOrganizationPayload,
} from '../../api/adminRegistrationApi'
import { ApiError } from '../../api/client'
import { platformAdministrationApi, type PlatformRegion } from '../../api/platformAdministrationApi'

type TextKey = Exclude<keyof UpdateOrganizationPayload, 'billingSameAsAddress' | 'allowSeatOverage' | 'regionId'>

const REQUIRED: TextKey[] = ['name', 'businessEmail', 'country']
const DETAIL_FIELDS: TextKey[] = [
  'name', 'businessEmail', 'phone', 'type', 'industry', 'website', 'country', 'state', 'city', 'address',
  'gstin', 'pan', 'companyRegistrationNumber', 'taxVatNumber',
]
const BILLING_FIELDS: TextKey[] = ['billingAddress', 'billingCountry', 'billingState', 'billingCity']

function toPayload(org: OrganizationAdmin): UpdateOrganizationPayload {
  return {
    name: org.name, type: org.type ?? '', industry: org.industry ?? '', website: org.website ?? '',
    businessEmail: org.businessEmail, phone: org.phone ?? '', country: org.country, state: org.state ?? '',
    city: org.city ?? '', address: org.address ?? '', gstin: org.gstin ?? '', pan: org.pan ?? '',
    companyRegistrationNumber: org.companyRegistrationNumber ?? '', taxVatNumber: org.taxVatNumber ?? '',
    billingSameAsAddress: org.billingSameAsAddress, billingAddress: org.billingAddress ?? '',
    billingCountry: org.billingCountry ?? '', billingState: org.billingState ?? '', billingCity: org.billingCity ?? '',
    // 05.02 Tenant Lifecycle (sprint 2026.4.2, carried from 2026.4.1).
    regionId: org.regionId, allowSeatOverage: org.allowSeatOverage,
  }
}

/**
 * REQ-TEN-001 (05.01.01.02 Update organization): platform-admin edit of an
 * organization's company details. The code, seats, MFA policy and parent are
 * not editable here; the backend refuses edits to a CLOSED organization and
 * its message is shown as returned.
 */
export function OrganizationEditDialog({ organization, onClose, onSaved }: {
  organization: OrganizationAdmin | null
  onClose: () => void
  onSaved: (updated: OrganizationAdmin) => void
}) {
  if (!organization) return null
  // Keyed so the form starts from the organization's current values each time it opens.
  return <EditForm key={organization.id} organization={organization} onClose={onClose} onSaved={onSaved} />
}

function EditForm({ organization, onClose, onSaved }: {
  organization: OrganizationAdmin
  onClose: () => void
  onSaved: (updated: OrganizationAdmin) => void
}) {
  const { t } = useTranslation()
  const [form, setForm] = useState<UpdateOrganizationPayload>(() => toPayload(organization))
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  // 05.02 Tenant Lifecycle (sprint 2026.4.2, carried from 2026.4.1).
  const [regions, setRegions] = useState<PlatformRegion[]>([])

  useEffect(() => {
    platformAdministrationApi.listRegions().then(setRegions).catch(() => setRegions([]))
  }, [])

  const set = (key: TextKey) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [key]: e.target.value }))
  const missingRequired = REQUIRED.some((k) => !form[k]?.trim())

  const save = async () => {
    setSaving(true)
    setError(null)
    try {
      onSaved(await adminRegistrationApi.updateOrganization(organization.id, form))
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('adminOrgLifecycle.saveError'))
    } finally {
      setSaving(false)
    }
  }

  const field = (key: TextKey) => (
    <TextField
      key={key}
      size="small"
      label={t(`adminOrgLifecycle.fields.${key}`)}
      required={REQUIRED.includes(key)}
      type={key === 'businessEmail' ? 'email' : 'text'}
      value={form[key] ?? ''}
      onChange={set(key)}
    />
  )

  return (
    <Dialog open onClose={saving ? undefined : onClose} fullWidth maxWidth="md">
      <DialogTitle>{t('adminOrgLifecycle.editTitle', { name: organization.name })}</DialogTitle>
      <DialogContent>
        <Alert severity="info" sx={{ mb: 2 }}>{t('adminOrgLifecycle.editHint', { code: organization.code })}</Alert>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, pt: 1 }}>
          {DETAIL_FIELDS.map(field)}
        </Box>
        <FormControlLabel
          sx={{ mt: 1 }}
          control={(
            <Checkbox
              checked={form.billingSameAsAddress}
              onChange={(e) => setForm((f) => ({ ...f, billingSameAsAddress: e.target.checked }))}
            />
          )}
          label={t('adminOrgLifecycle.fields.billingSameAsAddress')}
        />
        {!form.billingSameAsAddress && (
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, pt: 1 }}>
            {BILLING_FIELDS.map(field)}
          </Box>
        )}

        <Typography variant="subtitle2" sx={{ fontWeight: 700, mt: 3, mb: 1 }}>
          Tenant lifecycle
        </Typography>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
          <TextField
            select
            size="small"
            label="Region"
            value={form.regionId ?? ''}
            onChange={(e) => setForm((f) => ({ ...f, regionId: e.target.value === '' ? undefined : Number(e.target.value) }))}
          >
            <MenuItem value="">Not assigned</MenuItem>
            {regions.map((r) => (
              <MenuItem key={r.id} value={r.id}>{r.name}</MenuItem>
            ))}
          </TextField>
        </Box>
        <FormControlLabel
          sx={{ mt: 1 }}
          control={(
            <Checkbox
              checked={form.allowSeatOverage}
              onChange={(e) => setForm((f) => ({ ...f, allowSeatOverage: e.target.checked }))}
            />
          )}
          label="Allow seats beyond the licensed limit"
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={saving}>{t('adminOrgLifecycle.cancel')}</Button>
        <Button variant="contained" onClick={save} disabled={saving || missingRequired}>
          {saving ? t('adminOrgLifecycle.saving') : t('adminOrgLifecycle.save')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
