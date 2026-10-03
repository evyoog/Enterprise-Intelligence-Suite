import { KeyRound as PHKeyRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, IconButton, List, ListItem,
  ListItemText, Paper, TextField, ToggleButton, ToggleButtonGroup, Typography,
} from '@mui/material'
import { Pencil, Trash2 } from 'lucide-react'
import { ApiError, resolveAssetUrl } from '../api/client'
import { samlApi, type SamlProvider, type SamlProviderTestResult } from '../api/samlApi'
import { organizationApi } from '../api/registrationApi'
import { PageHeader } from '../components/layout/PageHeader'
import { OidcProvidersSection } from '../components/federation/OidcProvidersSection'
import { ClaimMappingDialog } from '../components/federation/ClaimMappingDialog'

/** 'keep' exists only when editing: change the name, leave the connection details as they are. */
type EntryMode = 'metadata' | 'manual' | 'keep'

/**
 * Phase 4 (2026.3.3): ORG_ADMIN self-service SAML Identity Federation
 * management — List/Add/Edit/Enable/Disable/Test/Delete, per this phase's
 * own admin UI requirement. Login itself isn't built yet (Phase 5) — this
 * is configuration only, exactly as scoped.
 */
export function OrganizationSamlProvidersPage() {
  const { t } = useTranslation()
  const [organizationId, setOrganizationId] = useState<number | null>(null)
  const [providers, setProviders] = useState<SamlProvider[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [adding, setAdding] = useState(false)
  // REQ-IAM-005 (sprint 2026.3.3): the provider being edited, reusing the create form.
  const [editingId, setEditingId] = useState<number | null>(null)
  const [entryMode, setEntryMode] = useState<EntryMode>('metadata')
  const [name, setName] = useState('')
  const [metadataXml, setMetadataXml] = useState('')
  const [entityId, setEntityId] = useState('')
  const [ssoUrl, setSsoUrl] = useState('')
  const [certificatePem, setCertificatePem] = useState('')
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const [testResults, setTestResults] = useState<Record<number, SamlProviderTestResult>>({})
  const [testingId, setTestingId] = useState<number | null>(null)
  // REQ-IAM-006: enabling SAML disables an enabled OIDC provider, so the
  // OIDC section reloads after a SAML toggle (and SAML reloads after an OIDC one).
  const [oidcRefresh, setOidcRefresh] = useState(0)
  // REQ-IAM-007: the provider whose claim mapping is being edited.
  const [mappingFor, setMappingFor] = useState<SamlProvider | null>(null)

  const load = () => {
    samlApi.list().then(setProviders).catch((e) => setLoadError(e instanceof ApiError ? e.message : 'Could not load SAML providers.'))
  }

  useEffect(() => {
    organizationApi.getMyOrganization().then((org) => setOrganizationId(org.id)).catch(() => {})
    load()
  }, [])

  const resetForm = () => {
    setAdding(false)
    setEditingId(null)
    setEntryMode('metadata')
    setName('')
    setMetadataXml('')
    setEntityId('')
    setSsoUrl('')
    setCertificatePem('')
    setFormError(null)
  }

  const submitCreate = async () => {
    setFormError(null)
    setSubmitting(true)
    try {
      await samlApi.create({
        name,
        ...(entryMode === 'metadata' ? { metadataXml } : { entityId, ssoUrl, certificatePem }),
      })
      resetForm()
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : 'Could not add this SAML provider.')
    } finally {
      setSubmitting(false)
    }
  }

  const startEdit = (provider: SamlProvider) => {
    resetForm()
    setEditingId(provider.id)
    setEntryMode('keep')
    setName(provider.name)
    setEntityId(provider.entityId)
    setSsoUrl(provider.ssoUrl)
    setCertificatePem(provider.certificatePem)
  }

  const submitEdit = async () => {
    if (editingId === null) return
    setFormError(null)
    setSubmitting(true)
    try {
      await samlApi.update(editingId, {
        name,
        ...(entryMode === 'metadata' ? { metadataXml } : entryMode === 'manual' ? { entityId, ssoUrl, certificatePem } : {}),
      })
      resetForm()
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('saml.saveError'))
    } finally {
      setSubmitting(false)
    }
  }

  const toggleEnabled = async (provider: SamlProvider) => {
    try {
      if (provider.enabled) await samlApi.disable(provider.id)
      else await samlApi.enable(provider.id)
      load()
      setOidcRefresh((k) => k + 1)
    } catch (e) {
      setLoadError(e instanceof ApiError ? e.message : 'Could not update this provider.')
    }
  }

  const runTest = async (id: number) => {
    setTestingId(id)
    try {
      const result = await samlApi.test(id)
      setTestResults((prev) => ({ ...prev, [id]: result }))
    } catch (e) {
      setTestResults((prev) => ({ ...prev, [id]: { success: false, checks: [], errors: [e instanceof ApiError ? e.message : 'Test failed.'] } }))
    } finally {
      setTestingId(null)
    }
  }

  const remove = async (id: number) => {
    try {
      await samlApi.remove(id)
      load()
    } catch (e) {
      setLoadError(e instanceof ApiError ? e.message : 'Could not delete this provider.')
    }
  }

  return (
    <Box>
      <Box sx={{ pb: 4 }}>
        <PageHeader icon={PHKeyRound} accent="indigo" area="organization" title="Identity Federation" subtitle="Let your organization's members sign in through your own SAML identity provider" />

        {organizationId !== null && (
          <Alert severity="info" sx={{ mb: 3 }}>
            Give your IdP administrator this Service Provider metadata URL:{' '}
            <code>{resolveAssetUrl(`/saml/${organizationId}/metadata`)}</code>
          </Alert>
        )}

        {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}

        {!loadError && !providers && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress size={28} /></Box>
        )}

        {providers && (
          <Paper variant="outlined" sx={{ mb: 3 }}>
            {providers.length === 0 && (
              <Typography sx={{ p: 3, color: 'text.secondary' }}>No SAML providers configured yet.</Typography>
            )}
            <List disablePadding>
              {providers.map((provider) => (
                <ListItem
                  key={provider.id}
                  divider
                  sx={{ flexDirection: 'column', alignItems: 'stretch', py: 2 }}
                  secondaryAction={
                    <IconButton edge="end" aria-label={`Delete ${provider.name}`} onClick={() => remove(provider.id)}>
                      <Trash2 size={18} />
                    </IconButton>
                  }
                >
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.5, pr: 5 }}>
                    <ListItemText
                      primary={provider.name}
                      secondary={`${provider.entityId} — expires ${provider.certificateExpiresAt ? new Date(provider.certificateExpiresAt).toLocaleDateString() : 'unknown'}`}
                    />
                    {provider.enabled && <Chip size="small" label="Active" color="success" />}
                    {provider.certificateExpired && <Chip size="small" label="Certificate expired" color="error" />}
                  </Box>
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button size="small" variant={provider.enabled ? 'outlined' : 'contained'} onClick={() => toggleEnabled(provider)}>
                      {provider.enabled ? 'Disable' : 'Enable'}
                    </Button>
                    <Button size="small" variant="text" disabled={testingId === provider.id} onClick={() => runTest(provider.id)}>
                      {testingId === provider.id ? 'Testing…' : 'Test'}
                    </Button>
                    <Button
                      size="small" variant="text" startIcon={<Pencil size={14} />}
                      aria-label={t('saml.editLabel', { name: provider.name })}
                      onClick={() => startEdit(provider)}
                    >
                      {t('saml.edit')}
                    </Button>
                    <Button size="small" variant="text" aria-label={t('claimMapping.openFor', { name: provider.name })}
                      onClick={() => setMappingFor(provider)}>
                      {t('claimMapping.open')}
                    </Button>
                  </Box>
                  {testResults[provider.id] && (
                    <Box sx={{ mt: 1.5 }}>
                      {testResults[provider.id].checks.map((c) => (
                        <Alert key={c} severity="success" sx={{ mb: 0.5, py: 0 }}>{c}</Alert>
                      ))}
                      {testResults[provider.id].errors.map((e) => (
                        <Alert key={e} severity="error" sx={{ mb: 0.5, py: 0 }}>{e}</Alert>
                      ))}
                    </Box>
                  )}
                </ListItem>
              ))}
            </List>
          </Paper>
        )}

        {!adding && editingId === null && (
          <Button variant="contained" onClick={() => setAdding(true)}>Add SAML provider</Button>
        )}

        {(adding || editingId !== null) && (
          <Paper variant="outlined" sx={{ p: 3 }} component="section" aria-labelledby="saml-form-title">
            <Typography id="saml-form-title" variant="h6" component="h2" sx={{ fontWeight: 700, mb: 2 }}>
              {editingId !== null ? t('saml.editTitle') : 'Add SAML provider'}
            </Typography>
            {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
            <TextField label="Name" fullWidth required value={name} onChange={(e) => setName(e.target.value)} sx={{ mb: 2 }} />

            <ToggleButtonGroup
              exclusive value={entryMode} onChange={(_, v) => v && setEntryMode(v)}
              size="small" sx={{ mb: 2 }}
            >
              {editingId !== null && <ToggleButton value="keep">{t('saml.keepConnection')}</ToggleButton>}
              <ToggleButton value="metadata">Paste IdP metadata XML</ToggleButton>
              <ToggleButton value="manual">Enter details manually</ToggleButton>
            </ToggleButtonGroup>

            {entryMode === 'keep' ? (
              <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('saml.keepConnectionHint')}</Typography>
            ) : entryMode === 'metadata' ? (
              <TextField
                label="IdP metadata XML" fullWidth required multiline minRows={6}
                value={metadataXml} onChange={(e) => setMetadataXml(e.target.value)}
                helperText="Entity ID, SSO URL, and certificate are extracted automatically."
                sx={{ mb: 2, fontFamily: 'monospace' }}
              />
            ) : (
              <>
                <TextField label="Entity ID" fullWidth required value={entityId} onChange={(e) => setEntityId(e.target.value)} sx={{ mb: 2 }} />
                <TextField label="SSO URL" fullWidth required value={ssoUrl} onChange={(e) => setSsoUrl(e.target.value)} sx={{ mb: 2 }} />
                <TextField
                  label="Certificate (PEM)" fullWidth required multiline minRows={4}
                  value={certificatePem} onChange={(e) => setCertificatePem(e.target.value)}
                  sx={{ mb: 2, fontFamily: 'monospace' }}
                />
              </>
            )}

            <Box sx={{ display: 'flex', gap: 1.5 }}>
              <Button
                variant="contained"
                disabled={submitting || !name || (entryMode === 'metadata' ? !metadataXml : entryMode === 'manual' ? !entityId || !ssoUrl || !certificatePem : false)}
                onClick={editingId !== null ? submitEdit : submitCreate}
              >
                {editingId !== null
                  ? (submitting ? t('saml.saving') : t('saml.save'))
                  : (submitting ? 'Adding…' : 'Add provider')}
              </Button>
              <Button variant="text" onClick={resetForm}>Cancel</Button>
            </Box>
          </Paper>
        )}

        <ClaimMappingDialog
          open={mappingFor !== null}
          providerName={mappingFor?.name ?? ''}
          protocol="SAML"
          initial={mappingFor?.claimMapping}
          onSave={async (mapping) => { await samlApi.updateClaimMapping(mappingFor!.id, mapping); load() }}
          onClose={() => setMappingFor(null)}
        />

        <OidcProvidersSection refreshKey={oidcRefresh} onChanged={load} />
      </Box>
    </Box>
  )
}
