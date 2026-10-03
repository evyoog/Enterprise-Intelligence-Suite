import { Handshake as PHHandshake } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, CircularProgress, Paper, TextField, Typography } from '@mui/material'
import { useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import {
  adminPartnersApi, type ContractStatus, type PartnerContract, type Provider, type ProviderStatus,
} from '../../api/partnersApi'
import { PageHeader } from '../../components/layout/PageHeader'

const STATUS_COLOR: Record<ProviderStatus, 'default' | 'info' | 'success' | 'error'> = {
  REGISTERED: 'default',
  VERIFIED: 'info',
  APPROVED: 'info',
  ACTIVE: 'success',
  REJECTED: 'error',
}

const CONTRACT_STATUS_COLOR: Record<ContractStatus, 'success' | 'default'> = {
  ACTIVE: 'success',
  EXPIRED: 'default',
}

/** 14.01.01.02-.04 Verify/Approve/Activate provider — one action at a time,
 * each its own admin decision (see decision C42). */
const NEXT_ACTION: Record<ProviderStatus, { key: 'verify' | 'approve' | 'activate'; labelKey: string } | null> = {
  REGISTERED: { key: 'verify', labelKey: 'partners.admin.verify' },
  VERIFIED: { key: 'approve', labelKey: 'partners.admin.approve' },
  APPROVED: { key: 'activate', labelKey: 'partners.admin.activate' },
  ACTIVE: null,
  REJECTED: null,
}

/** "/admin/partners/:id" — 14.01 Provider Onboarding (sprint 2027.2.1), MANAGE_PARTNERS. */
export function AdminPartnerDetailPage() {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const providerId = Number(id)

  const [provider, setProvider] = useState<Provider | null>(null)
  const [contract, setContract] = useState<PartnerContract | null>(null)
  const [busy, setBusy] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  const [terms, setTerms] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [contractError, setContractError] = useState<string | null>(null)
  const [savingContract, setSavingContract] = useState(false)

  const load = () => {
    adminPartnersApi.get(providerId).then(setProvider).catch(() => {})
    adminPartnersApi.getContract(providerId)
      .then((c) => { setContract(c); setTerms(c.terms); setStartDate(c.startDate); setEndDate(c.endDate) })
      .catch(() => setContract(null))
  }

  useEffect(load, [providerId])

  const runAction = async (action: 'verify' | 'approve' | 'activate' | 'reject') => {
    setBusy(true)
    setActionError(null)
    try {
      const updated = await adminPartnersApi[action](providerId)
      setProvider(updated)
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : 'Could not update this provider.')
    } finally {
      setBusy(false)
    }
  }

  const saveContract = async () => {
    setSavingContract(true)
    setContractError(null)
    try {
      const saved = await adminPartnersApi.saveContract(providerId, { terms, startDate, endDate })
      setContract(saved)
    } catch (e) {
      setContractError(e instanceof ApiError ? e.message : 'Could not save this contract.')
    } finally {
      setSavingContract(false)
    }
  }

  if (!provider) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
  }

  const nextAction = NEXT_ACTION[provider.status]

  return (
    <Box>
      <PageHeader icon={PHHandshake} accent="pink" area="partners" title={provider.name} subtitle={provider.contactEmail} />
      {actionError && <Alert severity="error" sx={{ mb: 2 }}>{actionError}</Alert>}

      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
          <Chip color={STATUS_COLOR[provider.status]} label={t(`partners.admin.status.${provider.status}`)} />
        </Box>
        {provider.description && <Typography sx={{ mb: 2 }}>{provider.description}</Typography>}
        <Box sx={{ display: 'flex', gap: 1 }}>
          {nextAction && (
            <Button variant="contained" disabled={busy} onClick={() => runAction(nextAction.key)}>
              {t(nextAction.labelKey)}
            </Button>
          )}
          {provider.status !== 'ACTIVE' && provider.status !== 'REJECTED' && (
            <Button color="error" variant="outlined" disabled={busy} onClick={() => runAction('reject')}>
              {t('partners.admin.reject')}
            </Button>
          )}
        </Box>
      </Paper>

      <Paper variant="outlined" sx={{ p: 2.5, maxWidth: 520 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
          <Typography sx={{ fontWeight: 700 }}>{t('partners.admin.contract.title')}</Typography>
          {contract && <Chip size="small" color={CONTRACT_STATUS_COLOR[contract.status]} label={t(`partners.admin.contract.status.${contract.status}`)} />}
        </Box>
        {contractError && <Alert severity="error" sx={{ mb: 2 }}>{contractError}</Alert>}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            multiline minRows={3} label={t('partners.admin.contract.terms')} value={terms}
            onChange={(e) => setTerms(e.target.value)}
          />
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              type="date" label={t('partners.admin.contract.startDate')} value={startDate}
              onChange={(e) => setStartDate(e.target.value)} slotProps={{ inputLabel: { shrink: true } }} fullWidth
            />
            <TextField
              type="date" label={t('partners.admin.contract.endDate')} value={endDate}
              onChange={(e) => setEndDate(e.target.value)} slotProps={{ inputLabel: { shrink: true } }} fullWidth
            />
          </Box>
          <Box>
            <Button variant="contained" disabled={savingContract || !terms || !startDate || !endDate} onClick={saveContract}>
              {t('partners.admin.contract.save')}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  )
}
