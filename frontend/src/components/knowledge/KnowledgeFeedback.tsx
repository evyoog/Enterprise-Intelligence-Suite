import { Alert, Box, Button, FormControl, FormControlLabel, FormLabel, Paper, Radio, RadioGroup, TextField, Typography } from '@mui/material'
import { ThumbsDown, ThumbsUp } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { knowledgeApi } from '../../api/knowledgeApi'
import { useAuth } from '../../auth/AuthProvider'

const REASONS = ['UNCLEAR', 'OUTDATED', 'MISSING', 'NOT_SOLVED', 'OTHER'] as const

/** "Was this helpful?" plus Report outdated and Suggest improvement (REQ-KNW-005.13). */
export function KnowledgeFeedback({ contentId }: { contentId: number }) {
  const { t } = useTranslation()
  const auth = useAuth()
  const [mode, setMode] = useState<'idle' | 'no' | 'OUTDATED' | 'SUGGESTION' | 'done'>('idle')
  const [reason, setReason] = useState<string>('')
  const [comment, setComment] = useState('')
  const [error, setError] = useState<string | null>(null)

  const send = (body: Parameters<typeof knowledgeApi.feedback>[1]) =>
    knowledgeApi.feedback(contentId, body).then(() => setMode('done')).catch((e: Error) => setError(e.message || t('knowledge.common.error')))

  if (!auth.isAuthenticated) {
    return <Paper variant="outlined" sx={{ p: 2 }}><Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.feedback.signIn')}</Typography></Paper>
  }
  if (mode === 'done') {
    return <Alert severity="success" variant="outlined">{t('knowledge.feedback.thanks')}</Alert>
  }
  return (
    <Paper variant="outlined" component="section" aria-labelledby={`fb-${contentId}`} sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
        <Typography id={`fb-${contentId}`} sx={{ fontWeight: 700, mr: 1 }}>{t('knowledge.feedback.question')}</Typography>
        <Button size="small" variant="outlined" startIcon={<ThumbsUp size={14} />} onClick={() => send({ kind: 'VOTE', helpful: true })}>{t('knowledge.feedback.yes')}</Button>
        <Button size="small" variant="outlined" startIcon={<ThumbsDown size={14} />} onClick={() => setMode('no')}>{t('knowledge.feedback.no')}</Button>
        <Box sx={{ flex: 1 }} />
        <Button size="small" onClick={() => setMode('OUTDATED')}>{t('knowledge.feedback.reportOutdated')}</Button>
        <Button size="small" onClick={() => setMode('SUGGESTION')}>{t('knowledge.feedback.suggest')}</Button>
      </Box>
      {mode === 'no' && (
        <FormControl>
          <FormLabel id={`reason-${contentId}`}>{t('knowledge.feedback.reasonLabel')}</FormLabel>
          <RadioGroup aria-labelledby={`reason-${contentId}`} value={reason} onChange={(e) => setReason(e.target.value)}>
            {REASONS.map((r) => <FormControlLabel key={r} value={r} control={<Radio size="small" />} label={t(`knowledge.feedback.reason.${r}`)} />)}
          </RadioGroup>
        </FormControl>
      )}
      {mode !== 'idle' && (
        <>
          <TextField label={t(mode === 'no' ? 'knowledge.feedback.commentLabel' : 'knowledge.feedback.suggestLabel')} multiline minRows={2}
            value={comment} onChange={(e) => setComment(e.target.value)} slotProps={{ htmlInput: { maxLength: 1000 } }} />
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button variant="contained" disabled={(mode === 'no' && !reason) || (mode !== 'no' && !comment.trim())}
              onClick={() => send(mode === 'no' ? { kind: 'VOTE', helpful: false, reason, comment: comment || undefined } : { kind: mode, comment })}>
              {t('knowledge.feedback.send')}
            </Button>
            <Button onClick={() => { setMode('idle'); setComment(''); setReason('') }}>{t('knowledge.feedback.cancel')}</Button>
          </Box>
        </>
      )}
      {error && <Alert severity="error">{error}</Alert>}
    </Paper>
  )
}
