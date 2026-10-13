import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Paper, Tab, Tabs, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { messageTemplatesApi, type TemplatePreview, type TemplateType, type TemplateVariant } from '../../api/messageTemplatesApi'

/**
 * REQ-GOV-002 (C92, TM-1 A): the emails and notifications EIS sends, one template per message type and language,
 * with {placeholders}. Editing a language replaces the built-in text for it; "Reset" goes back to the built-in text.
 */
export function MessageTemplatesSection() {
  const { t } = useTranslation()
  const [types, setTypes] = useState<TemplateType[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [editing, setEditing] = useState<TemplateType | null>(null)

  const load = () => messageTemplatesApi.list()
    .then((r) => { setTypes(r); setError(null) })
    .catch((e) => setError(e instanceof ApiError ? e.message : t('messageTemplates.loadError')))
  useEffect(() => { void load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const replace = (updated: TemplateType) => {
    setTypes((prev) => prev?.map((x) => (x.code === updated.code ? updated : x)) ?? prev)
    setEditing(updated)
  }

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }} component="section" aria-labelledby="message-templates-title">
      <Typography id="message-templates-title" variant="subtitle1" component="h5" sx={{ fontWeight: 700, mb: 0.5 }}>{t('messageTemplates.title')}</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('messageTemplates.description')}</Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1 }}>
        {types?.map((m) => (
          <Box component="li" key={m.code} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography sx={{ fontWeight: 600 }}>{m.name}</Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>{m.description}</Typography>
            </Box>
            <Chip size="small" label={t(`messageTemplates.channel.${m.channel}`)} />
            {m.variants.some((v) => v.customized) && <Chip size="small" color="primary" label={t('messageTemplates.customized')} />}
            <Button size="small" onClick={() => setEditing(m)} aria-label={t('messageTemplates.editNamed', { name: m.name })}>{t('messageTemplates.edit')}</Button>
          </Box>
        ))}
      </Box>
      {editing && <TemplateEditor type={editing} onChange={replace} onClose={() => setEditing(null)} />}
    </Paper>
  )
}

function TemplateEditor({ type, onChange, onClose }: { type: TemplateType; onChange: (t: TemplateType) => void; onClose: () => void }) {
  const { t } = useTranslation()
  const [language, setLanguage] = useState(type.variants[0]?.language ?? 'en')
  const variant = useMemo(() => type.variants.find((v) => v.language === language) as TemplateVariant, [type, language])
  const [subject, setSubject] = useState(variant.subject)
  const [body, setBody] = useState(variant.body)
  const [preview, setPreview] = useState<TemplatePreview | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const isEmail = type.channel === 'EMAIL'

  const pick = (lang: string) => {
    const v = type.variants.find((x) => x.language === lang)
    if (!v) return
    setLanguage(lang); setSubject(v.subject); setBody(v.body); setPreview(null); setError(null)
  }
  const fail = (e: unknown) => setError(e instanceof ApiError ? e.message : t('messageTemplates.saveError'))
  const run = (op: Promise<TemplateType>, after?: (u: TemplateType) => void) => {
    setBusy(true); setError(null)
    op.then((u) => { onChange(u); after?.(u) }).catch(fail).finally(() => setBusy(false))
  }

  const save = () => run(messageTemplatesApi.save(type.code, language, { subject, body }))
  const reset = () => run(messageTemplatesApi.reset(type.code, language), (u) => {
    const v = u.variants.find((x) => x.language === language)
    if (v) { setSubject(v.subject); setBody(v.body); setPreview(null) }
  })
  const showPreview = () => {
    setError(null)
    messageTemplatesApi.preview(type.code, language, { subject, body }).then(setPreview).catch(fail)
  }

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="md" aria-labelledby="template-editor-title">
      <DialogTitle id="template-editor-title">{type.name}</DialogTitle>
      <DialogContent>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1 }}>{type.description}</Typography>
        <Tabs value={language} onChange={(_, v) => pick(v)} aria-label={t('messageTemplates.language')} sx={{ mb: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
          {type.variants.map((v) => (
            <Tab key={v.language} value={v.language} label={`${v.language.toUpperCase()}${v.customized ? ' •' : ''}`} />
          ))}
        </Tabs>
        {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField label={t('messageTemplates.subject')} value={subject} onChange={(e) => setSubject(e.target.value)}
            slotProps={{ htmlInput: { maxLength: 200 } }} helperText={isEmail ? undefined : t('messageTemplates.titleHelp')} />
          <TextField label={t('messageTemplates.body')} value={body} onChange={(e) => setBody(e.target.value)} multiline minRows={5}
            slotProps={{ htmlInput: { maxLength: 5000 } }} />
          <Box>
            <Typography variant="subtitle2" sx={{ mb: 0.5 }}>{t('messageTemplates.placeholders')}</Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
              {type.placeholders.map((p) => (
                <Chip key={p.name} size="small" variant="outlined" label={`{${p.name}}${p.required ? ' *' : ''}`}
                  title={`${p.description} (${t('messageTemplates.example')}: ${p.example})`} />
              ))}
            </Box>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('messageTemplates.requiredHelp')}</Typography>
          </Box>
          {preview && (
            <Paper variant="outlined" sx={{ p: 2, bgcolor: 'action.hover' }} aria-live="polite">
              <Typography variant="overline">{t('messageTemplates.preview')}</Typography>
              <Typography sx={{ fontWeight: 700 }}>{preview.subject}</Typography>
              <Typography sx={{ whiteSpace: 'pre-wrap' }}>{preview.body}</Typography>
            </Paper>
          )}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={reset} color="warning" disabled={busy || !variant.customized}>{t('messageTemplates.reset')}</Button>
        <Box sx={{ flex: 1 }} />
        <Button onClick={onClose}>{t('messageTemplates.close')}</Button>
        <Button onClick={showPreview} disabled={busy}>{t('messageTemplates.previewButton')}</Button>
        <Button variant="contained" onClick={save} disabled={busy}>{t('messageTemplates.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}
