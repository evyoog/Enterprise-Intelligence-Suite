import { Globe as PHGlobe } from 'lucide-react'
import { PageHeader } from '../../../components/layout/PageHeader'
import { useEffect, useState } from 'react'
import {
  Alert, Box, Button, Chip, Divider, IconButton, Paper, Switch, TextField, Typography,
} from '@mui/material'
import { Plus, Trash2 } from 'lucide-react'
import { ApiError } from '../../../api/client'
import {
  platformAdministrationApi,
  type PlatformCurrency,
  type PlatformFeatureFlag,
  type PlatformRegion,
  type SupportedLanguage,
} from '../../../api/platformAdministrationApi'

/**
 * "Configuration" tab of Platform → Products (C80; was "/admin/settings/common") — 15.01 Platform Administration (sprint 2026.4.2):
 * currencies, regions and feature flags an admin can manage; languages shown
 * read-only (see SupportedLanguageDto's own backend javadoc for why).
 * "Configure defaults" and "Manage templates" (15.01.02) are not built this
 * sprint — see SPRINT-2026.4.2.md.
 */
export function CommonSettingsPage({ embedded = false }: { embedded?: boolean }) {
  return (
    <Box sx={{ maxWidth: 720, display: 'flex', flexDirection: 'column', gap: 3 }}>
      {!embedded && <PageHeader icon={PHGlobe} accent="teal" area="settings" title="Common" subtitle="Platform-wide currencies, regions and feature flags." />}
      <LanguagesSection />
      <CurrenciesSection />
      <RegionsSection />
      <FeatureFlagsSection />
    </Box>
  )
}

function LanguagesSection() {
  const [languages, setLanguages] = useState<SupportedLanguage[] | null>(null)
  useEffect(() => {
    platformAdministrationApi.listSupportedLanguages().then(setLanguages).catch(() => setLanguages([]))
  }, [])

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }}>
      <Typography variant="subtitle1" component="h5" sx={{ fontWeight: 700, mb: 0.5 }}>Languages</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>
        Read-only — a language only appears here once real translated strings exist for it.
      </Typography>
      <Box sx={{ display: 'flex', gap: 1 }}>
        {languages?.map((l) => <Chip key={l.code} label={`${l.label} (${l.code})`} size="small" />)}
      </Box>
    </Paper>
  )
}

function CurrenciesSection() {
  const [currencies, setCurrencies] = useState<PlatformCurrency[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [savingCode, setSavingCode] = useState<string | null>(null)

  useEffect(() => {
    platformAdministrationApi.listCurrencies().then(setCurrencies).catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load currencies.'))
  }, [])

  const toggle = (currency: PlatformCurrency) => {
    setSavingCode(currency.code)
    setError(null)
    platformAdministrationApi.updateCurrency(currency.code, !currency.enabled)
      .then((updated) => setCurrencies((prev) => prev?.map((c) => (c.code === updated.code ? updated : c)) ?? prev))
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not update this currency.'))
      .finally(() => setSavingCode(null))
  }

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }}>
      <Typography variant="subtitle1" component="h5" sx={{ fontWeight: 700, mb: 0.5 }}>Currencies</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>
        Disabling a currency only hides it from the plan editor's currency choices — it does not affect existing plans.
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {currencies?.map((c) => (
          <Box key={c.code} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography>{c.name} ({c.code})</Typography>
            <Switch checked={c.enabled} disabled={savingCode === c.code} onChange={() => toggle(c)} slotProps={{ input: { 'aria-label': `Enable ${c.code}` } }} />
          </Box>
        ))}
      </Box>
    </Paper>
  )
}

function RegionsSection() {
  const [regions, setRegions] = useState<PlatformRegion[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [code, setCode] = useState('')
  const [name, setName] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)

  useEffect(() => {
    platformAdministrationApi.listRegions().then(setRegions).catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load regions.'))
  }, [])

  const create = () => {
    if (!code.trim() || !name.trim()) return
    setError(null)
    platformAdministrationApi.createRegion(code.trim(), name.trim())
      .then((region) => { setRegions((prev) => [...(prev ?? []), region]); setCode(''); setName('') })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not create this region.'))
  }

  const toggle = (region: PlatformRegion) => {
    setBusyId(region.id)
    setError(null)
    platformAdministrationApi.updateRegion(region.id, region.name, !region.enabled)
      .then((updated) => setRegions((prev) => prev?.map((r) => (r.id === updated.id ? updated : r)) ?? prev))
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not update this region.'))
      .finally(() => setBusyId(null))
  }

  const remove = (region: PlatformRegion) => {
    setBusyId(region.id)
    setError(null)
    platformAdministrationApi.deleteRegion(region.id)
      .then(() => setRegions((prev) => prev?.filter((r) => r.id !== region.id) ?? prev))
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not delete this region.'))
      .finally(() => setBusyId(null))
  }

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }}>
      <Typography variant="subtitle1" component="h5" sx={{ fontWeight: 700, mb: 0.5 }}>Regions</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>
        05.02.01.03 Assign region reads this list — organizations are assigned a region on the admin registrations page.
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 2 }}>
        {regions?.map((r) => (
          <Box key={r.id} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography sx={{ flex: 1 }}>{r.name} <Typography component="span" variant="caption" sx={{ color: 'text.secondary' }}>({r.code})</Typography></Typography>
            <Switch size="small" checked={r.enabled} disabled={busyId === r.id} onChange={() => toggle(r)} slotProps={{ input: { 'aria-label': `Enable ${r.name}` } }} />
            <IconButton size="small" disabled={busyId === r.id} aria-label={`Delete ${r.name}`} onClick={() => remove(r)}>
              <Trash2 size={16} />
            </IconButton>
          </Box>
        ))}
        {regions?.length === 0 && <Typography sx={{ color: 'text.secondary' }}>No regions yet.</Typography>}
      </Box>
      <Divider sx={{ mb: 2 }} />
      <Box sx={{ display: 'flex', gap: 1 }}>
        <TextField size="small" label="Code" value={code} onChange={(e) => setCode(e.target.value)} sx={{ width: 140 }} />
        <TextField size="small" label="Name" value={name} onChange={(e) => setName(e.target.value)} sx={{ flex: 1 }} />
        <Button size="small" variant="outlined" startIcon={<Plus size={14} />} disabled={!code.trim() || !name.trim()} onClick={create}>
          Add region
        </Button>
      </Box>
    </Paper>
  )
}

function FeatureFlagsSection() {
  const [flags, setFlags] = useState<PlatformFeatureFlag[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [key, setKey] = useState('')
  const [description, setDescription] = useState('')
  const [busyKey, setBusyKey] = useState<string | null>(null)

  useEffect(() => {
    platformAdministrationApi.listFeatureFlags().then(setFlags).catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load feature flags.'))
  }, [])

  const create = () => {
    if (!key.trim()) return
    setError(null)
    platformAdministrationApi.createFeatureFlag(key.trim(), true, description.trim() || undefined)
      .then((flag) => { setFlags((prev) => [...(prev ?? []), flag]); setKey(''); setDescription('') })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not create this feature flag.'))
  }

  const toggle = (flag: PlatformFeatureFlag) => {
    setBusyKey(flag.flagKey)
    setError(null)
    platformAdministrationApi.updateFeatureFlag(flag.flagKey, !flag.enabled, flag.description)
      .then((updated) => setFlags((prev) => prev?.map((f) => (f.flagKey === updated.flagKey ? updated : f)) ?? prev))
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not update this feature flag.'))
      .finally(() => setBusyKey(null))
  }

  const remove = (flag: PlatformFeatureFlag) => {
    setBusyKey(flag.flagKey)
    setError(null)
    platformAdministrationApi.deleteFeatureFlag(flag.flagKey)
      .then(() => setFlags((prev) => prev?.filter((f) => f.flagKey !== flag.flagKey) ?? prev))
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not delete this feature flag.'))
      .finally(() => setBusyKey(null))
  }

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }}>
      <Typography variant="subtitle1" component="h5" sx={{ fontWeight: 700, mb: 0.5 }}>Feature flags</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>
        "groups_enabled" gates 05.04.01 Groups on the business dashboard — turning it off hides that section for every organization.
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 2 }}>
        {flags?.map((f) => (
          <Box key={f.flagKey} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box sx={{ flex: 1 }}>
              <Typography sx={{ fontFamily: 'monospace', fontSize: 14 }}>{f.flagKey}</Typography>
              {f.description && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{f.description}</Typography>}
            </Box>
            <Switch checked={f.enabled} disabled={busyKey === f.flagKey} onChange={() => toggle(f)} slotProps={{ input: { 'aria-label': `Enable ${f.flagKey}` } }} />
            <IconButton size="small" disabled={busyKey === f.flagKey} aria-label={`Delete ${f.flagKey}`} onClick={() => remove(f)}>
              <Trash2 size={16} />
            </IconButton>
          </Box>
        ))}
        {flags?.length === 0 && <Typography sx={{ color: 'text.secondary' }}>No feature flags yet.</Typography>}
      </Box>
      <Divider sx={{ mb: 2 }} />
      <Box sx={{ display: 'flex', gap: 1 }}>
        <TextField size="small" label="Key" value={key} onChange={(e) => setKey(e.target.value)} sx={{ width: 200 }} />
        <TextField size="small" label="Description" value={description} onChange={(e) => setDescription(e.target.value)} sx={{ flex: 1 }} />
        <Button size="small" variant="outlined" startIcon={<Plus size={14} />} disabled={!key.trim()} onClick={create}>
          Add flag
        </Button>
      </Box>
    </Paper>
  )
}
