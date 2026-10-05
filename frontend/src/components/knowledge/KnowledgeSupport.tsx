import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Paper, TextField, Typography } from '@mui/material'
import { Bot, LifeBuoy, MessageSquarePlus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'

/**
 * "Still need help?" (REQ-KNW-005.9): Contact support, Create support ticket
 * (prefilled with the page's own context — the reader can edit or remove it
 * before sending, BR-KCEN-002) and Ask AI assistant ("Coming soon", C75).
 */
export function KnowledgeSupport({ contentId, title, product, module, query, errorCode }: {
  contentId?: number; title?: string; product?: string | null; module?: string | null; query?: string; errorCode?: string
}) {
  const { t } = useTranslation()
  const auth = useAuth()
  const [assistantOpen, setAssistantOpen] = useState(false)
  const details = [title && `Article: ${title}`, product && `Product: ${product}`, module && `Module: ${module}`,
    query && `Search: ${query}`, errorCode && `Error code: ${errorCode}`].filter(Boolean).join('\n')
  const params = new URLSearchParams()
  if (title || errorCode || query) params.set('subject', (errorCode ?? title ?? query ?? '').slice(0, 200))
  if (details) params.set('description', details)
  if (contentId) params.set('fromKnowledge', String(contentId))
  const ticketLink = `/support/tickets?${params.toString()}`
  return (
    <Paper variant="outlined" component="section" aria-labelledby="kc-support-title" sx={{
      p: { xs: 2, sm: 3 }, display: 'flex', gap: 2, alignItems: { md: 'center' }, flexDirection: { xs: 'column', md: 'row' },
      background: (theme) => theme.palette.mode === 'dark' ? 'rgba(99,102,241,0.08)' : 'linear-gradient(120deg, #EEEDFD, #F8FAFF)',
    }}>
      <Box sx={{ flex: 1 }}>
        <Typography id="kc-support-title" component="h2" sx={{ fontWeight: 700, fontSize: 18 }}>{t('knowledge.support.title')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.support.body')}</Typography>
        {!auth.isAuthenticated && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('knowledge.support.signIn')}</Typography>}
      </Box>
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        <Button component={RouterLink} to="/support/tickets" variant="outlined" startIcon={<LifeBuoy size={16} />}>{t('knowledge.support.contact')}</Button>
        <Button component={RouterLink} to={ticketLink} variant="contained" startIcon={<MessageSquarePlus size={16} />}>{t('knowledge.support.ticket')}</Button>
        <Button variant="outlined" startIcon={<Bot size={16} />} onClick={() => setAssistantOpen(true)}>{t('knowledge.support.ask')}</Button>
      </Box>
      <AssistantDialog open={assistantOpen} onClose={() => setAssistantOpen(false)} />
    </Paper>
  )
}

/** "Ask eVyoog Knowledge" — prepared, not built until D8 (REQ-KNW-007). */
export function AssistantDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { t } = useTranslation()
  return (
    <Dialog open={open} onClose={onClose} aria-labelledby="kc-assistant-title" fullWidth maxWidth="sm">
      <DialogTitle id="kc-assistant-title" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <Bot size={20} aria-hidden />{t('knowledge.assistant.title')}
        <Chip size="small" color="primary" variant="outlined" label={t('knowledge.assistant.comingSoon')} sx={{ ml: 1 }} />
      </DialogTitle>
      <DialogContent>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('knowledge.assistant.body')}</Typography>
        <TextField fullWidth disabled label={t('knowledge.assistant.questionLabel')} />
      </DialogContent>
      <DialogActions><Button onClick={onClose}>{t('knowledge.assistant.close')}</Button></DialogActions>
    </Dialog>
  )
}
