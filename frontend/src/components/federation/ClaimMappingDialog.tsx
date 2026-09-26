import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Stack, TextField } from '@mui/material'
import { ApiError } from '../../api/client'
import { CLAIM_FIELDS, DEFAULT_CLAIMS, type ClaimMapping } from '../../api/claimMapping'

/**
 * REQ-IAM-007 (C23/C28): edit which attribute (SAML) or claim (OIDC) supplies
 * email, first name, last name and display name. A configured name is tried
 * first, then the defaults shown under each field; the fixed fallbacks
 * (display name / email for first name, "SSO User" for last name) still apply.
 */
export function ClaimMappingDialog({ open, providerName, protocol, initial, onSave, onClose }: {
  open: boolean
  providerName: string
  protocol: 'SAML' | 'OIDC'
  initial?: ClaimMapping | null
  onSave: (mapping: ClaimMapping) => Promise<unknown>
  onClose: () => void
}) {
  if (!open) return null
  return <Form key={providerName} providerName={providerName} protocol={protocol} initial={initial} onSave={onSave} onClose={onClose} />
}

function Form({ providerName, protocol, initial, onSave, onClose }: {
  providerName: string
  protocol: 'SAML' | 'OIDC'
  initial?: ClaimMapping | null
  onSave: (mapping: ClaimMapping) => Promise<unknown>
  onClose: () => void
}) {
  const { t } = useTranslation()
  const [mapping, setMapping] = useState<ClaimMapping>({
    email: initial?.email ?? '', firstName: initial?.firstName ?? '', lastName: initial?.lastName ?? '', displayName: initial?.displayName ?? '',
  })
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const save = async (value: ClaimMapping) => {
    setSaving(true)
    setError(null)
    try {
      await onSave(value)
      onClose()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('claimMapping.saveError'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{t('claimMapping.title', { name: providerName })}</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>{t(protocol === 'SAML' ? 'claimMapping.bodySaml' : 'claimMapping.bodyOidc')}</DialogContentText>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Stack spacing={2}>
          {CLAIM_FIELDS.map((field) => (
            <TextField
              key={field}
              size="small"
              label={t(`claimMapping.fields.${field}`)}
              value={mapping[field] ?? ''}
              onChange={(e) => setMapping((m) => ({ ...m, [field]: e.target.value }))}
              helperText={t('claimMapping.defaults', { names: DEFAULT_CLAIMS[protocol][field] })}
              slotProps={{ htmlInput: { maxLength: 255 } }}
            />
          ))}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button color="inherit" disabled={saving} onClick={() => save({ email: null, firstName: null, lastName: null, displayName: null })}>
          {t('claimMapping.reset')}
        </Button>
        <Button onClick={onClose} disabled={saving}>{t('claimMapping.cancel')}</Button>
        <Button variant="contained" onClick={() => save(mapping)} disabled={saving}>{t('claimMapping.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}
