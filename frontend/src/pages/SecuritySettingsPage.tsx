import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Container, Paper, Table, TableBody, TableCell,
  TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../api/client'
import { mfaApi, type MfaStatus } from '../api/mfaApi'
import { sessionsApi, type SessionInfo } from '../api/sessionsApi'
import { PageHeader } from '../components/layout/PageHeader'
import { PrivilegedAccessRequestsCard } from '../components/security/PrivilegedAccessRequestsCard'

type EnrollStep = 'password' | 'scan' | 'recoveryCodes'
type ManageAction = 'disable' | 'regenerate'

/**
 * Phase 2 (2026.3.3): self-service Platform TOTP management — enroll (with
 * QR + verification), view status, regenerate recovery codes, disable.
 * Every state-changing call requires the current password (see mfaApi's own
 * doc), asked inline here rather than via a separate re-auth page.
 *
 * Phase 6 (2026.3.3): fully migrated to the i18n mechanism (see
 * i18n/locales/*.json's own "security" namespace) — one of the first pages
 * beyond the navbar/login flow to complete this; see this phase's own
 * completion report for which pages still remain English-only.
 */
export function SecuritySettingsPage() {
  const { t } = useTranslation()
  const [status, setStatus] = useState<MfaStatus | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [enrolling, setEnrolling] = useState(false)
  const [enrollStep, setEnrollStep] = useState<EnrollStep>('password')
  const [enrollPassword, setEnrollPassword] = useState('')
  const [enrollSecret, setEnrollSecret] = useState('')
  const [enrollQr, setEnrollQr] = useState('')
  const [enrollCode, setEnrollCode] = useState('')
  const [recoveryCodes, setRecoveryCodes] = useState<string[] | null>(null)

  const [manageAction, setManageAction] = useState<ManageAction | null>(null)
  const [managePassword, setManagePassword] = useState('')
  const [manageCode, setManageCode] = useState('')

  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const [sessions, setSessions] = useState<SessionInfo[] | null>(null)
  const [sessionsError, setSessionsError] = useState<string | null>(null)
  const [revokingSessionId, setRevokingSessionId] = useState<string | null>(null)

  const load = () => {
    mfaApi.getStatus().then(setStatus).catch((e) => setLoadError(e instanceof ApiError ? e.message : t('security.loadError')))
  }

  const loadSessions = () => {
    sessionsApi.list().then(setSessions).catch((e) => setSessionsError(e instanceof ApiError ? e.message : t('security.sessionsLoadError')))
  }

  useEffect(load, []) // eslint-disable-line react-hooks/exhaustive-deps
  useEffect(loadSessions, []) // eslint-disable-line react-hooks/exhaustive-deps

  const revokeSession = async (sessionId: string) => {
    setRevokingSessionId(sessionId)
    try {
      await sessionsApi.revoke(sessionId)
      loadSessions()
    } catch (e) {
      setSessionsError(e instanceof ApiError ? e.message : t('security.revokeError'))
    } finally {
      setRevokingSessionId(null)
    }
  }

  const resetEnrollment = () => {
    setEnrolling(false)
    setEnrollStep('password')
    setEnrollPassword('')
    setEnrollSecret('')
    setEnrollQr('')
    setEnrollCode('')
    setFormError(null)
  }

  const resetManage = () => {
    setManageAction(null)
    setManagePassword('')
    setManageCode('')
    setFormError(null)
  }

  const startEnrollment = async () => {
    setFormError(null)
    setSubmitting(true)
    try {
      const response = await mfaApi.enroll(enrollPassword)
      setEnrollSecret(response.secret)
      setEnrollQr(response.qrCodePngBase64)
      setEnrollStep('scan')
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('security.enrollError'))
    } finally {
      setSubmitting(false)
    }
  }

  const finishEnrollment = async () => {
    setFormError(null)
    setSubmitting(true)
    try {
      const response = await mfaApi.verifyEnrollment(enrollCode)
      setRecoveryCodes(response.codes)
      setEnrollStep('recoveryCodes')
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('security.invalidCode'))
    } finally {
      setSubmitting(false)
    }
  }

  const submitManageAction = async () => {
    setFormError(null)
    setSubmitting(true)
    try {
      if (manageAction === 'disable') {
        await mfaApi.disable(managePassword, manageCode)
        resetManage()
        load()
      } else if (manageAction === 'regenerate') {
        const response = await mfaApi.regenerateRecoveryCodes(managePassword, manageCode)
        setRecoveryCodes(response.codes)
        resetManage()
        load()
      }
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : t('security.actionError'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box>
      <Container maxWidth="sm" disableGutters sx={{ pb: 4 }}>
        <PageHeader title={t('security.title')} subtitle={t('security.subtitle')} />

        {loadError && <Alert severity="error">{loadError}</Alert>}

        {!loadError && !status && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress size={28} /></Box>
        )}

        {status && !enrolling && !manageAction && (
          <Paper variant="outlined" sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1.5 }}>
              <Typography variant="h6" component="h2" sx={{ fontWeight: 700 }}>{t('security.twoFactorAuth')}</Typography>
              <Chip size="small" label={status.enabled ? t('security.enabled') : t('security.notEnabled')} color={status.enabled ? 'success' : 'default'} />
            </Box>

            {!status.enabled && (
              <>
                <Typography sx={{ color: 'text.secondary', mb: 2 }}>
                  {t('security.enableDescription')}
                </Typography>
                <Button variant="contained" onClick={() => setEnrolling(true)}>{t('security.enableButton')}</Button>
              </>
            )}

            {status.enabled && (
              <>
                <Typography sx={{ color: 'text.secondary', mb: 0.5 }}>
                  {status.enrolledAt ? t('security.enabledOn', { date: new Date(status.enrolledAt).toLocaleDateString() }) : t('security.enabledPlain')}
                </Typography>
                <Typography sx={{ color: 'text.secondary', mb: 2 }}>
                  {t('security.recoveryCodesRemaining', { count: status.remainingRecoveryCodes })}
                </Typography>
                <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap' }}>
                  <Button variant="outlined" onClick={() => setManageAction('regenerate')}>{t('security.regenerateCodes')}</Button>
                  <Button variant="outlined" color="error" onClick={() => setManageAction('disable')}>{t('security.disable2fa')}</Button>
                </Box>
              </>
            )}
          </Paper>
        )}

        {enrolling && enrollStep === 'password' && (
          <Paper variant="outlined" sx={{ p: 3 }}>
            <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('security.confirmPassword')}</Typography>
            {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
            <TextField
              label={t('security.currentPassword')} type="password" fullWidth required autoFocus
              value={enrollPassword} onChange={(e) => setEnrollPassword(e.target.value)}
              sx={{ mb: 2 }}
            />
            <Box sx={{ display: 'flex', gap: 1.5 }}>
              <Button variant="contained" disabled={submitting || !enrollPassword} onClick={startEnrollment}>{t('security.continue')}</Button>
              <Button variant="text" onClick={resetEnrollment}>{t('common.cancel')}</Button>
            </Box>
          </Paper>
        )}

        {enrolling && enrollStep === 'scan' && (
          <Paper variant="outlined" sx={{ p: 3 }}>
            <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('security.setupAuthenticator')}</Typography>
            <Typography sx={{ color: 'text.secondary', mb: 2 }}>
              {t('security.setupStep1')}<br />
              {t('security.setupStep2')}
            </Typography>
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
              <img src={`data:image/png;base64,${enrollQr}`} alt={t('security.qrAlt')} width={200} height={200} />
            </Box>
            <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary', mb: 2, wordBreak: 'break-all' }}>
              {t('security.manualKeyHint', { secret: enrollSecret })}
            </Typography>
            {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
            <TextField
              label={t('security.sixDigitCode')} fullWidth required autoFocus inputMode="numeric"
              value={enrollCode} onChange={(e) => setEnrollCode(e.target.value)}
              sx={{ mb: 2 }}
            />
            <Box sx={{ display: 'flex', gap: 1.5 }}>
              <Button variant="contained" disabled={submitting || !enrollCode} onClick={finishEnrollment}>{t('security.verifyAndEnable')}</Button>
              <Button variant="text" onClick={resetEnrollment}>{t('common.cancel')}</Button>
            </Box>
          </Paper>
        )}

        {enrolling && enrollStep === 'recoveryCodes' && recoveryCodes && (
          <Paper variant="outlined" sx={{ p: 3 }}>
            <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('security.saveRecoveryCodes')}</Typography>
            <Alert severity="warning" sx={{ mb: 2 }}>
              {t('security.recoveryCodesWarning')}
            </Alert>
            <Box component="ul" sx={{ fontFamily: 'monospace', fontSize: 14, columns: 2, mb: 2, pl: 2 }}>
              {recoveryCodes.map((code) => <li key={code}>{code}</li>)}
            </Box>
            <Button
              variant="contained"
              onClick={() => {
                navigator.clipboard?.writeText(recoveryCodes.join('\n')).catch(() => {})
                resetEnrollment()
                setRecoveryCodes(null)
              }}
            >
              {t('security.savedCodesConfirm')}
            </Button>
          </Paper>
        )}

        {manageAction && (
          <Paper variant="outlined" sx={{ p: 3 }}>
            <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>
              {manageAction === 'disable' ? t('security.disableTitle') : t('security.regenerateTitle')}
            </Typography>
            <Typography sx={{ color: 'text.secondary', mb: 2 }}>
              {t('security.confirmPasswordAndCode')}
            </Typography>
            {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
            <TextField
              label={t('security.currentPassword')} type="password" fullWidth required autoFocus
              value={managePassword} onChange={(e) => setManagePassword(e.target.value)}
              sx={{ mb: 2 }}
            />
            <TextField
              label={t('security.verificationOrRecoveryCode')} fullWidth required
              value={manageCode} onChange={(e) => setManageCode(e.target.value)}
              sx={{ mb: 2 }}
            />
            <Box sx={{ display: 'flex', gap: 1.5 }}>
              <Button
                variant="contained"
                color={manageAction === 'disable' ? 'error' : 'primary'}
                disabled={submitting || !managePassword || !manageCode}
                onClick={submitManageAction}
              >
                {manageAction === 'disable' ? t('security.disable') : t('security.regenerate')}
              </Button>
              <Button variant="text" onClick={resetManage}>{t('common.cancel')}</Button>
            </Box>
          </Paper>
        )}

        {!enrolling && !manageAction && recoveryCodes && (
          <Paper variant="outlined" sx={{ p: 3, mt: 3 }}>
            <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('security.newRecoveryCodes')}</Typography>
            <Alert severity="warning" sx={{ mb: 2 }}>
              {t('security.oldCodesWarning')}
            </Alert>
            <Box component="ul" sx={{ fontFamily: 'monospace', fontSize: 14, columns: 2, mb: 2, pl: 2 }}>
              {recoveryCodes.map((code) => <li key={code}>{code}</li>)}
            </Box>
            <Button variant="contained" onClick={() => setRecoveryCodes(null)}>{t('security.done')}</Button>
          </Paper>
        )}

        <Paper variant="outlined" sx={{ p: 3, mt: 3 }}>
          <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('security.activeSessions')}</Typography>
          {sessionsError && <Alert severity="error" sx={{ mb: 2 }}>{sessionsError}</Alert>}
          {!sessionsError && !sessions && (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}><CircularProgress size={22} /></Box>
          )}
          {sessions && sessions.length === 0 && (
            <Typography sx={{ color: 'text.secondary' }}>{t('security.noActiveSessions')}</Typography>
          )}
          {sessions && sessions.length > 0 && (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>{t('security.ipAddress')}</TableCell>
                  <TableCell>{t('security.app')}</TableCell>
                  <TableCell>{t('security.started')}</TableCell>
                  <TableCell>{t('security.lastActive')}</TableCell>
                  <TableCell />
                </TableRow>
              </TableHead>
              <TableBody>
                {sessions.map((session) => (
                  <TableRow key={session.id}>
                    <TableCell>{session.ipAddress ?? '—'}</TableCell>
                    <TableCell>{session.clients.join(', ') || '—'}</TableCell>
                    <TableCell>{session.startedAt ? new Date(session.startedAt).toLocaleString() : '—'}</TableCell>
                    <TableCell>{session.lastAccessAt ? new Date(session.lastAccessAt).toLocaleString() : '—'}</TableCell>
                    <TableCell align="right">
                      {session.current ? (
                        <Chip size="small" label={t('security.thisDevice')} color="success" />
                      ) : (
                        <Button
                          size="small" color="error" disabled={revokingSessionId === session.id}
                          onClick={() => revokeSession(session.id)}
                        >
                          {t('security.revoke')}
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </Paper>

        {/* Sprint 2026.3.3, REQ-IAM-004: request elevated access and see your own requests. */}
        <PrivilegedAccessRequestsCard />
      </Container>
    </Box>
  )
}
