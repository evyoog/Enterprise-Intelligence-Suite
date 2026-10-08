import { ArrowLeft, Building2, UserRound } from 'lucide-react'
import { useCallback, useEffect, useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useParams, useSearchParams } from 'react-router-dom'
import {
  Alert, Box, Button, Chip, CircularProgress, LinearProgress, Paper, Stack, Tab, Table, TableBody, TableCell, TableHead, TableRow,
  Tabs, Typography,
} from '@mui/material'
import type { OrganizationAdmin } from '../../api/adminRegistrationApi'
import { ApiError } from '../../api/client'
import type { Invitation } from '../../api/invitationsApi'
import { auditLogApi, type AuditLogEntry } from '../../api/auditLogApi'
import {
  orgDirectoryApi, type Completion, type IndividualDetail, type InvoiceRow, type MemberRow, type SubscriptionRow, type TicketRow,
} from '../../api/orgDirectoryApi'
import { OrganizationActions, type OrgAction } from '../../components/admin/OrganizationActions'
import { PageHeader } from '../../components/layout/PageHeader'
import { useTabParam } from '../../components/layout/useTabParam'
import { OrgStructureViewer } from '../../components/organization/OrgStructureViewer'

const ORG_TABS = ['overview', 'structure', 'members', 'subscriptions', 'billing', 'support', 'security', 'activity'] as const
const IND_TABS = ['profile', 'subscriptions', 'billing', 'support', 'activity'] as const
const ACTIONS: readonly string[] = ['edit', 'suspend', 'activate', 'close']

const message = (e: unknown, fallback: string) => (e instanceof ApiError ? e.message : fallback)

/** Loads once per `key`; `loading` is true until the answer for the current key has arrived. */
function useAsync<T>(load: () => Promise<T>, key: string) {
  const { t } = useTranslation()
  const [state, setState] = useState<{ key: string; data?: T; error?: string }>({ key: '' })
  useEffect(() => {
    let alive = true
    load().then((data) => { if (alive) setState({ key, data }) })
      .catch((e) => { if (alive) setState({ key, error: message(e, t('orgDirectory.loadError')) }) })
    return () => { alive = false }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key])
  const ready = state.key === key
  return { data: ready ? state.data : undefined, error: ready ? state.error : undefined, loading: !ready }
}

function Async<T>({ result, children }: { result: { data?: T; error?: string; loading: boolean }; children: (data: T) => ReactNode }) {
  if (result.error) return <Alert severity="error">{result.error}</Alert>
  if (result.loading || result.data === undefined) return <CircularProgress size={24} aria-label="loading" />
  return <>{children(result.data)}</>
}

function Facts({ rows }: { rows: [string, ReactNode][] }) {
  return (
    <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: 'max-content 1fr', columnGap: 3, rowGap: 1, m: 0 }}>
      {rows.map(([label, value]) => (
        <Box key={label} sx={{ display: 'contents' }}>
          <Typography component="dt" color="text.secondary">{label}</Typography>
          <Typography component="dd" sx={{ m: 0 }}>{value || '—'}</Typography>
        </Box>
      ))}
    </Box>
  )
}

function CompletionPanel({ completion }: { completion: Completion }) {
  const { t } = useTranslation()
  return (
    <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
      <Typography variant="subtitle1" component="h3">{t('orgDirectory.detail.profileCompletion', { percent: completion.percent })}</Typography>
      <LinearProgress variant="determinate" value={completion.percent} color={completion.percent === 100 ? 'success' : 'primary'}
        aria-label={t('orgDirectory.detail.profileCompletion', { percent: completion.percent })} sx={{ height: 8, borderRadius: 4, my: 1 }} />
      {completion.missing.length === 0
        ? <Typography color="text.secondary">{t('orgDirectory.noMissing')}</Typography>
        : (
          <Stack direction="row" spacing={0.5} useFlexGap sx={{ flexWrap: 'wrap', alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">{t('orgDirectory.detail.missing')}</Typography>
            {completion.missing.map((c) => <Chip key={c} size="small" variant="outlined" label={t(`orgDirectory.fields.${c}`)} />)}
          </Stack>
        )}
    </Paper>
  )
}

function Empty({ children }: { children: ReactNode }) {
  return <Typography color="text.secondary">{children}</Typography>
}

const date = (v?: string) => (v ? new Date(v).toLocaleString() : '—')

function InvitationsList({ id }: { id: number }) {
  const { t } = useTranslation()
  const r = useAsync<Invitation[]>(() => orgDirectoryApi.invitations(id), `v${id}`)
  return (
    <Async result={r}>
      {(rows) => rows.length === 0 ? <Empty>{t('invitations.none')}</Empty> : (
        <Table size="small" aria-label={t('invitations.title')}>
          <TableHead><TableRow>
            {['email', 'status', 'role', 'invitedBy', 'expiresAt'].map((c) => <TableCell key={c}>{t(`invitations.col.${c}`)}</TableCell>)}
          </TableRow></TableHead>
          <TableBody>{rows.map((i) => (
            <TableRow key={i.id}>
              <TableCell>{i.email}</TableCell><TableCell>{t(`invitations.statuses.${i.status}`)}</TableCell>
              <TableCell>{t(`orgSettings.roles.${i.orgRole}`)}</TableCell><TableCell>{i.invitedByName ?? '—'}</TableCell>
              <TableCell>{date(i.expiresAt)}</TableCell>
            </TableRow>
          ))}</TableBody>
        </Table>
      )}
    </Async>
  )
}

function MembersTab({ id }: { id: number }) {
  const { t } = useTranslation()
  const r = useAsync<MemberRow[]>(() => orgDirectoryApi.members(id), `m${id}`)
  return (
    <>
    <Async result={r}>
      {(rows) => rows.length === 0 ? <Empty>{t('orgDirectory.detail.noMembers')}</Empty> : (
        <Table size="small" aria-label={t('orgDirectory.detail.tabs.members')}>
          <TableHead><TableRow>
            {['name', 'email', 'role', 'status', 'node', 'joined'].map((c) => <TableCell key={c}>{t(`orgDirectory.detail.col.${c}`)}</TableCell>)}
          </TableRow></TableHead>
          <TableBody>{rows.map((m) => (
            <TableRow key={m.memberId}>
              <TableCell>{m.name}</TableCell><TableCell>{m.email}</TableCell><TableCell>{m.orgRole}</TableCell>
              <TableCell>{m.status}</TableCell><TableCell>{m.orgNodeName ?? '—'}</TableCell><TableCell>{date(m.joinedAt)}</TableCell>
            </TableRow>
          ))}</TableBody>
        </Table>
      )}
    </Async>
    <Typography variant="subtitle1" component="h3" sx={{ mt: 3, mb: 1 }}>{t('invitations.title')}</Typography>
    <InvitationsList id={id} />
    </>
  )
}

function SubscriptionsTable({ rows }: { rows: SubscriptionRow[] }) {
  const { t } = useTranslation()
  if (rows.length === 0) return <Empty>{t('orgDirectory.detail.noSubscriptions')}</Empty>
  return (
    <Table size="small" aria-label={t('orgDirectory.detail.tabs.subscriptions')}>
      <TableHead><TableRow>
        {['product', 'status', 'seats', 'started', 'expires', 'autoRenew'].map((c) => <TableCell key={c}>{t(`orgDirectory.detail.col.${c}`)}</TableCell>)}
      </TableRow></TableHead>
      <TableBody>{rows.map((s) => (
        <TableRow key={s.id}>
          <TableCell>{s.productName ?? s.productId}</TableCell><TableCell>{s.status.replaceAll('_', ' ')}</TableCell><TableCell>{s.quantity}</TableCell>
          <TableCell>{date(s.startedAt)}</TableCell><TableCell>{date(s.expiresAt)}</TableCell>
          <TableCell>{s.autoRenew ? t('orgDirectory.yes') : t('orgDirectory.no')}</TableCell>
        </TableRow>
      ))}</TableBody>
    </Table>
  )
}

function InvoicesTable({ rows }: { rows: InvoiceRow[] }) {
  const { t } = useTranslation()
  if (rows.length === 0) return <Empty>{t('orgDirectory.detail.noInvoices')}</Empty>
  return (
    <Table size="small" aria-label={t('orgDirectory.detail.tabs.billing')}>
      <TableHead><TableRow>
        {['invoice', 'status', 'total', 'issued', 'due'].map((c) => <TableCell key={c}>{t(`orgDirectory.detail.col.${c}`)}</TableCell>)}
      </TableRow></TableHead>
      <TableBody>{rows.map((i) => (
        <TableRow key={i.id}>
          <TableCell>{i.number}</TableCell><TableCell>{i.status.replaceAll('_', ' ')}</TableCell>
          <TableCell>{i.currency} {(i.total / 100).toFixed(2)}</TableCell><TableCell>{date(i.issuedAt)}</TableCell><TableCell>{date(i.dueAt)}</TableCell>
        </TableRow>
      ))}</TableBody>
    </Table>
  )
}

function TicketsTable({ rows }: { rows: TicketRow[] }) {
  const { t } = useTranslation()
  if (rows.length === 0) return <Empty>{t('orgDirectory.detail.noTickets')}</Empty>
  return (
    <Table size="small" aria-label={t('orgDirectory.detail.tabs.support')}>
      <TableHead><TableRow>
        {['subject', 'status', 'priority', 'requestedBy', 'created'].map((c) => <TableCell key={c}>{t(`orgDirectory.detail.col.${c}`)}</TableCell>)}
      </TableRow></TableHead>
      <TableBody>{rows.map((k) => (
        <TableRow key={k.id}>
          <TableCell>{k.subject}</TableCell><TableCell>{k.status.replaceAll('_', ' ')}</TableCell><TableCell>{k.priority}</TableCell>
          <TableCell>{k.requestedBy ?? '—'}</TableCell><TableCell>{date(k.createdAt)}</TableCell>
        </TableRow>
      ))}</TableBody>
    </Table>
  )
}

function ActivityTab({ organizationId, customerId }: { organizationId?: number; customerId?: number }) {
  const { t } = useTranslation()
  const r = useAsync<AuditLogEntry[]>(
    () => auditLogApi.search({ organizationId, actorCustomerId: customerId, size: 50 }).then((p) => p.items), `a${organizationId ?? ''}-${customerId ?? ''}`)
  return (
    <Async result={r}>
      {(rows) => rows.length === 0 ? <Empty>{t('orgDirectory.detail.noActivity')}</Empty> : (
        <Table size="small" aria-label={t('orgDirectory.detail.tabs.activity')}>
          <TableHead><TableRow>
            {['when', 'action', 'actor', 'outcome', 'detail'].map((c) => <TableCell key={c}>{t(`orgDirectory.detail.col.${c}`)}</TableCell>)}
          </TableRow></TableHead>
          <TableBody>{rows.map((a) => (
            <TableRow key={a.id}>
              <TableCell>{date(a.timestamp)}</TableCell><TableCell>{a.action}</TableCell><TableCell>{a.actorEmail ?? '—'}</TableCell>
              <TableCell>{a.outcome}</TableCell><TableCell>{a.detail ?? ''}</TableCell>
            </TableRow>
          ))}</TableBody>
        </Table>
      )}
    </Async>
  )
}

/** "/admin/organizations/:kind/:id" — REQ-TEN-007 detail with tabs (read-only apart from the organization actions). */
export function AdminOrganizationDetailPage() {
  const { kind, id } = useParams()
  const numeric = Number(id)
  return kind === 'individual'
    ? <IndividualDetailView key={numeric} id={numeric} />
    : <OrganizationDetailView key={numeric} id={numeric} />
}

function Header({ title, icon, chips, completion, children }: {
  title: string; icon: typeof Building2; chips: ReactNode; completion: number; children?: ReactNode
}) {
  const { t } = useTranslation()
  return (
    <>
      <Button component={RouterLink} to="/admin/organizations" size="small" startIcon={<ArrowLeft size={14} />} sx={{ mb: 1 }}>
        {t('orgDirectory.detail.back')}
      </Button>
      <PageHeader icon={icon} accent="teal" area="administration" title={title}
        subtitle={t('orgDirectory.detail.profileCompletion', { percent: completion })} />
      <Stack direction="row" spacing={1} useFlexGap sx={{ mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>{chips}</Stack>
      {children}
    </>
  )
}

function OrganizationDetailView({ id }: { id: number }) {
  const { t } = useTranslation()
  const [params] = useSearchParams()
  const [tab, setTab] = useTabParam(ORG_TABS, 'overview')
  const overview = useAsync(() => orgDirectoryApi.overview(id), `o${id}`)
  const [changed, setChanged] = useState<OrganizationAdmin | null>(null)
  const tree = useAsync(() => orgDirectoryApi.hierarchy(id), `t${id}`)
  const subs = useAsync(() => orgDirectoryApi.subscriptions(id), `s${id}`)
  const invoices = useAsync(() => orgDirectoryApi.invoices(id), `i${id}`)
  const tickets = useAsync(() => orgDirectoryApi.tickets(id), `k${id}`)
  const loadNode = useCallback((nodeId: number) => orgDirectoryApi.hierarchyNode(id, nodeId), [id])
  const loadHistory = useCallback((nodeId: number) => orgDirectoryApi.hierarchyHistory(id, nodeId), [id])
  const action = params.get('action')
  const initial = action && ACTIONS.includes(action) ? (action as OrgAction) : null

  return (
    <Async result={overview}>
      {(ov) => {
        const org = changed ?? ov.organization
        const billing = org.billingSameAsAddress ? t('orgDirectory.detail.sameAsAddress')
          : [org.billingAddress, org.billingCity, org.billingState, org.billingCountry].filter(Boolean).join(', ')
        return (
          <Box sx={{ pb: 4 }}>
            <Header title={org.name} icon={Building2} completion={ov.completion.percent}
              chips={(
                <>
                  <Chip size="small" label={org.code} variant="outlined" />
                  <Chip size="small" label={org.status.replaceAll('_', ' ')} color={org.status === 'COMPLETED' ? 'success' : 'default'} />
                  <Chip size="small" variant="outlined" color={org.lifecycleStatus === 'ACTIVE' ? 'success' : org.lifecycleStatus === 'SUSPENDED' ? 'warning' : 'default'}
                    label={t('adminOrgLifecycle.lifecycleLabel', { status: t(`adminOrgLifecycle.lifecycleStatus.${org.lifecycleStatus}`) })} />
                </>
              )}>
              <OrganizationActions organization={org} onChanged={setChanged} initialAction={initial} />
            </Header>
            <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" aria-label={org.name} sx={{ my: 2, borderBottom: 1, borderColor: 'divider' }}>
              {ORG_TABS.map((k) => <Tab key={k} value={k} label={t(`orgDirectory.detail.tabs.${k}`)} />)}
            </Tabs>
            {tab === 'overview' && (
              <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' } }}>
                <Box sx={{ gridColumn: '1 / -1' }}><CompletionPanel completion={ov.completion} /></Box>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="subtitle1" component="h3" sx={{ mb: 1 }}>{t('orgDirectory.detail.company')}</Typography>
                  <Facts rows={[
                    [t('orgDirectory.col.name'), org.name], [t('orgDirectory.detail.code'), org.code], [t('orgDirectory.col.type'), org.type],
                    [t('orgDirectory.col.industry'), org.industry], [t('orgDirectory.detail.website'), org.website],
                    [t('orgDirectory.detail.businessEmail'), org.businessEmail], [t('orgDirectory.detail.phone'), org.phone],
                    [t('orgDirectory.col.region'), org.regionName], [t('orgDirectory.col.registered'), date(org.createdAt)],
                    [t('orgDirectory.detail.parent'), org.parentOrganizationId ? `#${org.parentOrganizationId}` : undefined],
                  ]} />
                </Paper>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="subtitle1" component="h3" sx={{ mb: 1 }}>{t('orgDirectory.detail.addressTax')}</Typography>
                  <Facts rows={[
                    [t('orgDirectory.col.location'), [org.city, org.state, org.country].filter(Boolean).join(', ')],
                    [t('orgDirectory.detail.address'), org.address], [t('orgDirectory.detail.billingAddress'), billing],
                    ['GSTIN', org.gstin], ['PAN', org.pan], [t('orgDirectory.detail.cin'), org.companyRegistrationNumber],
                    [t('orgDirectory.detail.vat'), org.taxVatNumber],
                  ]} />
                </Paper>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="subtitle1" component="h3" sx={{ mb: 1 }}>{t('orgDirectory.detail.administrator')}</Typography>
                  <Facts rows={[
                    [t('orgDirectory.detail.name'), [org.adminFirstName, org.adminLastName].filter(Boolean).join(' ')],
                    [t('orgDirectory.detail.email'), org.adminEmail],
                    [t('orgDirectory.col.signIn'), org.adminEmail ? (org.adminKeycloakLinked ? t('orgDirectory.signInLinked') : t('orgDirectory.signInNotLinked')) : undefined],
                  ]} />
                </Paper>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="subtitle1" component="h3" sx={{ mb: 1 }}>{t('orgDirectory.detail.seats')}</Typography>
                  <Facts rows={[
                    [t('orgDirectory.col.seats'), `${org.activeMemberCount} / ${org.licensedSeats}`],
                    [t('orgDirectory.detail.seatOverage'), org.allowSeatOverage ? t('orgDirectory.yes') : t('orgDirectory.no')],
                  ]} />
                </Paper>
              </Box>
            )}
            {tab === 'structure' && <Async result={tree}>{(data) => <OrgStructureViewer tree={data} loadDetail={loadNode} loadHistory={loadHistory} />}</Async>}
            {tab === 'members' && <MembersTab id={id} />}
            {tab === 'subscriptions' && <Async result={subs}>{(rows) => <SubscriptionsTable rows={rows} />}</Async>}
            {tab === 'billing' && <Async result={invoices}>{(rows) => <InvoicesTable rows={rows} />}</Async>}
            {tab === 'support' && <Async result={tickets}>{(rows) => <TicketsTable rows={rows} />}</Async>}
            {tab === 'security' && (
              <Paper variant="outlined" sx={{ p: 2, maxWidth: 560 }}>
                <Facts rows={[
                  [t('orgDirectory.detail.mfaPolicy'), ov.mfaRequired ? t('orgDirectory.mfaOn') : t('orgDirectory.detail.mfaOptional')],
                  [t('orgDirectory.col.signIn'), org.adminKeycloakLinked ? t('orgDirectory.signInLinked') : t('orgDirectory.signInNotLinked')],
                  [t('orgDirectory.detail.seatOverage'), org.allowSeatOverage ? t('orgDirectory.yes') : t('orgDirectory.no')],
                ]} />
              </Paper>
            )}
            {tab === 'activity' && <ActivityTab organizationId={id} />}
          </Box>
        )
      }}
    </Async>
  )
}

function IndividualDetailView({ id }: { id: number }) {
  const { t } = useTranslation()
  const [tab, setTab] = useTabParam(IND_TABS, 'profile')
  const r = useAsync<IndividualDetail>(() => orgDirectoryApi.individual(id), `c${id}`)
  return (
    <Async result={r}>
      {(c) => {
        const name = [c.firstName, c.lastName].filter(Boolean).join(' ') || c.email
        return (
          <Box sx={{ pb: 4 }}>
            <Header title={name} icon={UserRound} completion={c.completion.percent}
              chips={(
                <>
                  <Chip size="small" label={t('orgDirectory.kind.INDIVIDUAL')} variant="outlined" />
                  <Chip size="small" label={c.status.replaceAll('_', ' ')} color={c.status === 'COMPLETED' ? 'success' : 'default'} />
                  <Chip size="small" color={c.signInLinked ? 'success' : 'warning'} label={c.signInLinked ? t('orgDirectory.signInLinked') : t('orgDirectory.signInNotLinked')} />
                </>
              )} />
            <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" aria-label={name} sx={{ my: 2, borderBottom: 1, borderColor: 'divider' }}>
              {IND_TABS.map((k) => <Tab key={k} value={k} label={t(`orgDirectory.detail.tabs.${k}`)} />)}
            </Tabs>
            {tab === 'profile' && (
              <>
                <CompletionPanel completion={c.completion} />
                <Paper variant="outlined" sx={{ p: 2, maxWidth: 560 }}>
                  <Facts rows={[
                    [t('orgDirectory.detail.name'), name], [t('orgDirectory.detail.email'), c.email], [t('orgDirectory.detail.mobile'), c.mobile],
                    [t('orgDirectory.col.location'), c.country], [t('orgDirectory.detail.company'), c.companyName],
                    [t('orgDirectory.detail.jobTitle'), c.jobTitle], [t('orgDirectory.col.industry'), c.industry],
                    [t('orgDirectory.col.registered'), date(c.createdAt)],
                  ]} />
                </Paper>
              </>
            )}
            {tab === 'subscriptions' && <SubscriptionsTable rows={c.subscriptions} />}
            {tab === 'billing' && <InvoicesTable rows={c.invoices} />}
            {tab === 'support' && <TicketsTable rows={c.tickets} />}
            {tab === 'activity' && <ActivityTab customerId={c.id} />}
          </Box>
        )
      }}
    </Async>
  )
}
