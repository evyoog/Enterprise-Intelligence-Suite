import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, CircularProgress, IconButton, Tab, Tabs, TextField, Typography } from '@mui/material'
import { Check, Pencil, X } from 'lucide-react'
import {
  adminRegistrationApi,
  type CustomerAdmin,
  type OrganizationAdmin,
  type OrganizationLifecycleAction,
  type OrganizationLifecycleResult,
  type OrganizationLifecycleStatus,
  type PendingProvisioning,
} from '../../api/adminRegistrationApi'
import { ApiError } from '../../api/client'
import { OrganizationEditDialog } from '../../components/admin/OrganizationEditDialog'
import { OrganizationLifecycleDialog, type LifecycleTarget } from '../../components/admin/OrganizationLifecycleDialog'
import { PageHeader } from '../../components/layout/PageHeader'

type TabKey = 'organizations' | 'individuals' | 'pending'

/** "/admin/registrations" — the platform admin's full view of everyone who's
 * registered: every organization (full company details) and every
 * standalone individual, plus the manual Keycloak-linking bridge this phase
 * relies on instead of automatic provisioning. */
export function RegistrationsAdminPage() {
  const [tab, setTab] = useState<TabKey>('organizations')

  return (
    <>
      <PageHeader title="Registrations" subtitle="Every organization and individual that has registered with Vyoog." />

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="organizations" label="Organizations" />
        <Tab value="individuals" label="Individuals" />
        <Tab value="pending" label="Pending Keycloak provisioning" />
      </Tabs>

      {tab === 'organizations' && <OrganizationsTab />}
      {tab === 'individuals' && <IndividualsTab />}
      {tab === 'pending' && <PendingProvisioningTab />}
    </>
  )
}

function DetailRow({ label, value }: { label: string; value?: React.ReactNode }) {
  if (!value) return null
  return (
    <Box sx={{ display: 'flex', gap: 1, fontSize: 13 }}>
      <Typography variant="body2" sx={{ color: 'text.secondary', flexShrink: 0, minWidth: 90 }}>{label}</Typography>
      <Typography variant="body2">{value}</Typography>
    </Box>
  )
}

function OrganizationsTab() {
  const { t } = useTranslation()
  const [rows, setRows] = useState<OrganizationAdmin[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  // REQ-TEN-001: edit and suspend / activate / close.
  const [editing, setEditing] = useState<OrganizationAdmin | null>(null)
  const [lifecycleTarget, setLifecycleTarget] = useState<LifecycleTarget | null>(null)
  const [notice, setNotice] = useState<{ message: string; failures: string[] } | null>(null)
  const [editingSeatsFor, setEditingSeatsFor] = useState<number | null>(null)
  const [seatInput, setSeatInput] = useState('')
  const [savingSeats, setSavingSeats] = useState(false)

  const load = () => {
    adminRegistrationApi.listAllOrganizations()
      .then(setRows)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load organizations.'))
  }

  useEffect(load, [])

  const startEditingSeats = (org: OrganizationAdmin) => {
    setEditingSeatsFor(org.id)
    setSeatInput(String(org.licensedSeats))
  }

  const saveSeats = async (organizationId: number) => {
    const parsed = Number(seatInput)
    if (!Number.isInteger(parsed) || parsed < 0) return
    setSavingSeats(true)
    try {
      await adminRegistrationApi.updateSeats(organizationId, parsed)
      setEditingSeatsFor(null)
      load()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not update seats.')
    } finally {
      setSavingSeats(false)
    }
  }

  const replaceRow = (updated: OrganizationAdmin) =>
    setRows((current) => current?.map((o) => (o.id === updated.id ? updated : o)) ?? current)

  const onSaved = (updated: OrganizationAdmin) => {
    replaceRow(updated)
    setEditing(null)
  }

  const onLifecycleDone = (result: OrganizationLifecycleResult, action: OrganizationLifecycleAction) => {
    replaceRow(result.organization)
    setLifecycleTarget(null)
    setNotice({
      message: t(`adminOrgLifecycle.done.${action}`, { name: result.organization.name, count: result.accountsUpdated }),
      failures: result.accountsNotUpdated,
    })
  }

  if (error) return <Typography color="error" role="alert">{error}</Typography>
  if (rows === null) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
  if (rows.length === 0) return <Typography sx={{ color: 'text.secondary' }}>No organizations have registered yet.</Typography>

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {notice && (
        <Alert severity={notice.failures.length ? 'warning' : 'success'} onClose={() => setNotice(null)}>
          {notice.message}
          {notice.failures.length > 0 && <> {t('adminOrgLifecycle.partialFailure', { emails: notice.failures.join(', ') })}</>}
        </Alert>
      )}
      <OrganizationEditDialog organization={editing} onClose={() => setEditing(null)} onSaved={onSaved} />
      <OrganizationLifecycleDialog target={lifecycleTarget} onClose={() => setLifecycleTarget(null)} onDone={onLifecycleDone} />
      {rows.map((org) => {
        const billing = org.billingSameAsAddress
          ? 'Same as organization address'
          : [org.billingAddress, org.billingCity, org.billingState, org.billingCountry].filter(Boolean).join(', ') || '—'
        return (
          <Box key={org.id} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, p: 2.5 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', mb: 1.5 }}>
              <Typography sx={{ fontWeight: 700, fontSize: 17 }}>{org.name}</Typography>
              <Chip size="small" label={org.code} variant="outlined" />
              <StatusChip status={org.status} />
              <LifecycleChip status={org.lifecycleStatus} />
              {editingSeatsFor === org.id ? (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <TextField
                    size="small"
                    type="number"
                    value={seatInput}
                    onChange={(e) => setSeatInput(e.target.value)}
                    sx={{ width: 90 }}
                    slotProps={{ htmlInput: { min: 0 } }}
                  />
                  <IconButton aria-label="Save seat count" size="small" disabled={savingSeats} onClick={() => saveSeats(org.id)}>
                    <Check size={14} />
                  </IconButton>
                  <IconButton aria-label="Cancel editing seats" size="small" disabled={savingSeats} onClick={() => setEditingSeatsFor(null)}>
                    <X size={14} />
                  </IconButton>
                </Box>
              ) : (
                <Chip
                  size="small"
                  label={`${org.activeMemberCount} / ${org.licensedSeats} seats`}
                  onDelete={() => startEditingSeats(org)}
                  deleteIcon={<Pencil size={12} />}
                />
              )}
              <Box sx={{ display: 'flex', gap: 1, ml: 'auto', flexWrap: 'wrap' }}>
                <Button
                  size="small" variant="outlined"
                  aria-label={t('adminOrgLifecycle.editLabel', { name: org.name })}
                  disabled={org.lifecycleStatus === 'CLOSED'}
                  onClick={() => setEditing(org)}
                >
                  {t('adminOrgLifecycle.editButton')}
                </Button>
                {org.lifecycleStatus === 'ACTIVE' && (
                  <Button
                    size="small" variant="outlined" color="warning"
                    aria-label={t('adminOrgLifecycle.suspendLabel', { name: org.name })}
                    onClick={() => setLifecycleTarget({ organization: org, action: 'suspend' })}
                  >
                    {t('adminOrgLifecycle.suspendButton')}
                  </Button>
                )}
                {org.lifecycleStatus !== 'ACTIVE' && (
                  <Button
                    size="small" variant="outlined"
                    aria-label={t('adminOrgLifecycle.activateLabel', { name: org.name })}
                    onClick={() => setLifecycleTarget({ organization: org, action: 'activate' })}
                  >
                    {t('adminOrgLifecycle.activateButton')}
                  </Button>
                )}
                {org.lifecycleStatus !== 'CLOSED' && (
                  <Button
                    size="small" variant="outlined" color="error"
                    aria-label={t('adminOrgLifecycle.closeLabel', { name: org.name })}
                    onClick={() => setLifecycleTarget({ organization: org, action: 'close' })}
                  >
                    {t('adminOrgLifecycle.closeButton')}
                  </Button>
                )}
              </Box>
            </Box>

            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 1.5 }}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                <DetailRow label="Business email" value={org.businessEmail} />
                <DetailRow label="Phone" value={org.phone} />
                <DetailRow label="Type" value={org.type} />
                <DetailRow label="Industry" value={org.industry} />
                <DetailRow label="Website" value={org.website} />
                <DetailRow label="Location" value={[org.city, org.state, org.country].filter(Boolean).join(', ')} />
                <DetailRow label="Address" value={org.address} />
                <DetailRow label="Billing" value={billing} />
              </Box>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                <DetailRow label="GSTIN" value={org.gstin} />
                <DetailRow label="PAN" value={org.pan} />
                <DetailRow label="Company reg. no." value={org.companyRegistrationNumber} />
                <DetailRow label="Tax / VAT no." value={org.taxVatNumber} />
                <DetailRow
                  label="Admin"
                  value={org.adminEmail ? (
                    <>
                      {org.adminFirstName} {org.adminLastName} — {org.adminEmail}{' '}
                      <Chip
                        size="small"
                        label={org.adminKeycloakLinked ? 'Keycloak linked' : 'Awaiting Keycloak link'}
                        color={org.adminKeycloakLinked ? 'success' : 'warning'}
                        sx={{ ml: 0.5 }}
                      />
                    </>
                  ) : undefined}
                />
                <DetailRow label="Registered" value={org.createdAt ? new Date(org.createdAt).toLocaleString() : undefined} />
              </Box>
            </Box>
          </Box>
        )
      })}
    </Box>
  )
}

function IndividualsTab() {
  const [rows, setRows] = useState<CustomerAdmin[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    adminRegistrationApi.listAllIndividuals()
      .then(setRows)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load individuals.'))
  }, [])

  if (error) return <Typography color="error" role="alert">{error}</Typography>
  if (rows === null) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
  if (rows.length === 0) return <Typography sx={{ color: 'text.secondary' }}>No individual customers yet.</Typography>

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      {rows.map((c) => (
        <Box key={c.id} sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 2, border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
              <Typography sx={{ fontWeight: 700 }}>{c.firstName} {c.lastName}</Typography>
              <StatusChip status={c.status} />
              <Chip size="small" label={c.keycloakLinked ? 'Keycloak linked' : 'Awaiting Keycloak link'} color={c.keycloakLinked ? 'success' : 'warning'} />
            </Box>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{c.email}</Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              {[c.companyName, c.jobTitle, c.country].filter(Boolean).join(' · ')}
            </Typography>
          </Box>
        </Box>
      ))}
    </Box>
  )
}

function PendingProvisioningTab() {
  const [rows, setRows] = useState<PendingProvisioning[] | null>(null)
  const [subInputs, setSubInputs] = useState<Record<number, string>>({})
  const [linking, setLinking] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const load = () => {
    adminRegistrationApi.listPendingProvisioning()
      .then(setRows)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load the provisioning queue.'))
  }

  useEffect(load, [])

  const link = async (customerId: number) => {
    const sub = (subInputs[customerId] ?? '').trim()
    if (!sub || linking !== null) return
    setLinking(customerId)
    setError(null)
    try {
      await adminRegistrationApi.linkKeycloakUser(customerId, sub)
      load()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not link this Keycloak user.')
    } finally {
      setLinking(null)
    }
  }

  return (
    <>
      <Typography sx={{ color: 'text.secondary', mb: 2 }}>
        People who've verified their email but have no Keycloak login yet. Create their user in the Keycloak
        admin console, then paste that user's "sub" here to link it.
      </Typography>

      {error && <Typography color="error" role="alert" sx={{ mb: 2 }}>{error}</Typography>}

      {rows === null && <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>}
      {rows !== null && rows.length === 0 && <Typography sx={{ color: 'text.secondary' }}>Nothing waiting — everyone verified has a linked Keycloak login.</Typography>}

      {rows !== null && rows.map((row) => (
        <Box key={row.customerId} sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 2, mb: 1.5, border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography sx={{ fontWeight: 700 }}>{row.firstName} {row.lastName}</Typography>
              <Chip size="small" label={row.kind === 'ORG_ADMIN' ? `Org admin — ${row.organizationName}` : 'Individual'} />
            </Box>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{row.email}</Typography>
          </Box>
          <TextField
            size="small" placeholder="Keycloak user sub"
            value={subInputs[row.customerId] ?? ''}
            onChange={(e) => setSubInputs((s) => ({ ...s, [row.customerId]: e.target.value }))}
          />
          <Button variant="contained" size="small" disabled={linking === row.customerId} onClick={() => link(row.customerId)}>
            {linking === row.customerId ? 'Linking…' : 'Link'}
          </Button>
        </Box>
      ))}
    </>
  )
}

function StatusChip({ status }: { status: string }) {
  const color = status === 'COMPLETED' ? 'success' : status === 'CANCELLED' || status === 'EXPIRED' ? 'default' : 'warning'
  return <Chip size="small" label={status.replaceAll('_', ' ')} color={color as 'success' | 'default' | 'warning'} />
}

function LifecycleChip({ status }: { status: OrganizationLifecycleStatus }) {
  const { t } = useTranslation()
  const color = status === 'ACTIVE' ? 'success' : status === 'SUSPENDED' ? 'warning' : 'default'
  return (
    <Chip
      size="small"
      variant="outlined"
      color={color}
      label={t('adminOrgLifecycle.lifecycleLabel', { status: t(`adminOrgLifecycle.lifecycleStatus.${status}`) })}
    />
  )
}
