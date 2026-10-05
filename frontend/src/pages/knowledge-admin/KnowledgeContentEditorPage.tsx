import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Paper,
  Radio, RadioGroup, TextField, Typography,
} from '@mui/material'
import { Eye, Save } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import {
  knowledgeAdminApi, type Compare, type Content, type ContentInput, type ContentType, type Item, type TypeFields, type Version,
} from '../../api/knowledgeApi'
import { BlockEditor } from '../../components/knowledge/BlockEditor'
import { ContentMetadataFields } from '../../components/knowledge/ContentMetadataFields'
import { KnowledgeItemView } from '../knowledge/KnowledgeItemPage'
import { StateChip } from './KnowledgeAdminLayout'
import { toInput, useKnowledgeAdmin } from './knowledgeAdminContext'

/** Type-specific fields per content type (REQ-KNW-002.3); list fields are one entry per line. */
const TYPE_FIELDS: Partial<Record<ContentType, { name: string; multiline?: boolean; list?: boolean }[]>> = {
  FAQ: [{ name: 'question' }, { name: 'answer', multiline: true }],
  GLOSSARY_TERM: [{ name: 'term' }, { name: 'definition', multiline: true }, { name: 'synonyms' }],
  ERROR_CODE: [{ name: 'code' }, { name: 'error', multiline: true }, { name: 'cause', multiline: true }, { name: 'solution', multiline: true }, { name: 'permission' }],
  TROUBLESHOOTING: [{ name: 'problem', multiline: true }, { name: 'cause', multiline: true }, { name: 'solution', multiline: true }, { name: 'code' }],
  RELEASE_NOTE: [{ name: 'version' }, { name: 'releaseDate' }, { name: 'newFeatures', multiline: true, list: true }, { name: 'improvements', multiline: true, list: true },
    { name: 'bugFixes', multiline: true, list: true }, { name: 'deprecated', multiline: true, list: true }, { name: 'notices', multiline: true }],
  DOCUMENT: [{ name: 'fileType' }, { name: 'version' }],
  TEMPLATE: [{ name: 'fileType' }, { name: 'version' }],
}

/** Workflow actions for the current state and role (BR-KCON-002, BR-KPRM-002). */
export function WorkflowActions({ content, publisher, onChange, onError }: {
  content: Content; publisher: boolean; onChange: (c: Content) => void; onError: (m: string) => void
}) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [dialog, setDialog] = useState<'publish' | 'return' | 'delete' | null>(null)
  const [bump, setBump] = useState<'MINOR' | 'MAJOR'>('MINOR')
  const [scheduleAt, setScheduleAt] = useState('')
  const [comment, setComment] = useState('')
  const run = (p: Promise<Content>) => p.then((c) => { onChange(c); setDialog(null) }).catch((e: Error) => onError(e.message))
  const s = content.workflowState
  return (
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
      {s === 'DRAFT' && <Button variant="outlined" onClick={() => run(knowledgeAdminApi.action(content.id, 'submit'))}>{t('knowledge.admin.editor.submit')}</Button>}
      {publisher && s === 'IN_REVIEW' && <Button variant="contained" onClick={() => run(knowledgeAdminApi.action(content.id, 'approve'))}>{t('knowledge.admin.editor.approve')}</Button>}
      {publisher && (s === 'IN_REVIEW' || s === 'APPROVED') && <Button onClick={() => setDialog('return')}>{t('knowledge.admin.editor.return')}</Button>}
      {publisher && s === 'APPROVED' && <Button variant="contained" onClick={() => setDialog('publish')}>{t('knowledge.admin.editor.publish')}</Button>}
      {publisher && s === 'SCHEDULED' && <Button onClick={() => run(knowledgeAdminApi.action(content.id, 'unschedule'))}>{t('knowledge.admin.editor.unschedule')}</Button>}
      {publisher && content.live && <Button onClick={() => run(knowledgeAdminApi.action(content.id, 'unpublish'))}>{t('knowledge.admin.editor.unpublish')}</Button>}
      {publisher && s === 'PUBLISHED' && <Button onClick={() => run(knowledgeAdminApi.action(content.id, 'deprecate'))}>{t('knowledge.admin.editor.deprecate')}</Button>}
      {publisher && (s === 'PUBLISHED' || s === 'DEPRECATED' || s === 'DRAFT') && <Button onClick={() => run(knowledgeAdminApi.action(content.id, 'archive'))}>{t('knowledge.admin.editor.archive')}</Button>}
      {publisher && s === 'ARCHIVED' && <Button onClick={() => run(knowledgeAdminApi.action(content.id, 'restore'))}>{t('knowledge.admin.editor.restore')}</Button>}
      {publisher && <Button color="error" onClick={() => setDialog('delete')}>{t('knowledge.admin.editor.delete')}</Button>}

      <Dialog open={dialog === 'publish'} onClose={() => setDialog(null)} aria-labelledby="kp-title">
        <DialogTitle id="kp-title">{t('knowledge.admin.editor.publishTitle')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <RadioGroup value={bump} onChange={(e) => setBump(e.target.value as 'MINOR' | 'MAJOR')} aria-label={t('knowledge.admin.editor.publishTitle')}>
            <FormControlLabel value="MINOR" control={<Radio />} label={t('knowledge.admin.editor.minor')} />
            <FormControlLabel value="MAJOR" control={<Radio />} label={t('knowledge.admin.editor.major')} />
          </RadioGroup>
          <TextField type="datetime-local" size="small" label={t('knowledge.admin.editor.schedule')} slotProps={{ inputLabel: { shrink: true } }} value={scheduleAt} onChange={(e) => setScheduleAt(e.target.value)} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(null)}>{t('knowledge.admin.editor.cancel')}</Button>
          <Button variant="contained" onClick={() => run(knowledgeAdminApi.publish(content.id, bump, scheduleAt ? new Date(scheduleAt).toISOString() : null))}>{t('knowledge.admin.editor.publish')}</Button>
        </DialogActions>
      </Dialog>
      <Dialog open={dialog === 'return'} onClose={() => setDialog(null)} aria-labelledby="kr-title" fullWidth maxWidth="sm">
        <DialogTitle id="kr-title">{t('knowledge.admin.editor.return')}</DialogTitle>
        <DialogContent><TextField fullWidth multiline minRows={3} sx={{ mt: 1 }} label={t('knowledge.admin.editor.returnComment')} value={comment} onChange={(e) => setComment(e.target.value)} /></DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(null)}>{t('knowledge.admin.editor.cancel')}</Button>
          <Button variant="contained" onClick={() => run(knowledgeAdminApi.returnToDraft(content.id, comment))}>{t('knowledge.admin.editor.return')}</Button>
        </DialogActions>
      </Dialog>
      <Dialog open={dialog === 'delete'} onClose={() => setDialog(null)} aria-labelledby="kdel-title">
        <DialogTitle id="kdel-title">{t('knowledge.admin.editor.delete')}</DialogTitle>
        <DialogContent><Typography>{content.contentType === 'VIDEO' ? t('knowledge.admin.videos.deleteConfirm') : t('knowledge.admin.editor.deleteConfirm')}</Typography></DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(null)}>{t('knowledge.admin.editor.cancel')}</Button>
          <Button color="error" variant="contained" onClick={() => knowledgeAdminApi.remove(content.id)
            .then(() => navigate(content.contentType === 'VIDEO' ? '/knowledge-management/videos' : '/knowledge-management/content')).catch((e: Error) => onError(e.message))}>
            {t('knowledge.admin.editor.confirm')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

/** Versions with view, compare with the draft and restore (REQ-KNW-002.6). */
export function VersionsPanel({ content, publisher, onChange }: { content: Content; publisher: boolean; onChange: (c: Content) => void }) {
  const { t, i18n } = useTranslation()
  const [versions, setVersions] = useState<Version[]>([])
  const [compare, setCompare] = useState<Compare | null>(null)
  useEffect(() => { knowledgeAdminApi.versions(content.id).then(setVersions).catch(() => setVersions([])) }, [content.id, content.liveVersion])
  return (
    <Paper variant="outlined" sx={{ p: 2 }}>
      <Typography component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.editor.versions')}</Typography>
      {versions.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.editor.noVersions')}</Typography>}
      {versions.map((v) => (
        <Box key={v.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.75, flexWrap: 'wrap', borderBottom: 1, borderColor: 'divider' }}>
          <Chip size="small" label={v.versionLabel} color={v.live ? 'success' : 'default'} />
          <Typography variant="body2" sx={{ flex: 1, minWidth: 120 }}>{new Date(v.publishedAt).toLocaleString(i18n.language)}</Typography>
          <Button size="small" onClick={() => knowledgeAdminApi.compare(content.id, v.versionLabel, 'draft').then(setCompare).catch(() => undefined)}>{t('knowledge.admin.editor.compare')}</Button>
          {publisher && <Button size="small" onClick={() => knowledgeAdminApi.restoreVersion(content.id, v.versionLabel).then(onChange).catch(() => undefined)}>{t('knowledge.admin.editor.restoreVersion')}</Button>}
        </Box>
      ))}
      <Dialog open={!!compare} onClose={() => setCompare(null)} fullWidth maxWidth="md" aria-labelledby="kc-compare">
        <DialogTitle id="kc-compare">{compare && t('knowledge.admin.editor.compareTitle', { from: compare.from, to: compare.to })}</DialogTitle>
        <DialogContent>
          {compare?.titleChanged && <Alert severity="info" sx={{ mb: 1 }}>{compare.fromTitle} → {compare.toTitle}</Alert>}
          {compare?.changes.map((c) => (
            <Box key={c.index} sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '90px 1fr 1fr' }, gap: 1, py: 1, borderBottom: 1, borderColor: 'divider' }}>
              <Chip size="small" label={t(`knowledge.admin.editor.change.${c.kind}`)} color={c.kind === 'SAME' ? 'default' : c.kind === 'REMOVED' ? 'error' : 'primary'} />
              <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap', color: 'text.secondary' }}>{blockText(c.before)}</Typography>
              <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{blockText(c.after)}</Typography>
            </Box>
          ))}
        </DialogContent>
        <DialogActions><Button onClick={() => setCompare(null)}>{t('knowledge.assistant.close')}</Button></DialogActions>
      </Dialog>
    </Paper>
  )
}

function blockText(b: Compare['changes'][number]['before']) {
  if (!b) return ''
  return [b.title, b.text, b.question, b.answer, b.label, b.url, ...(b.items ?? []).map((i) => (typeof i === 'string' ? i : `${i.title}: ${i.text}`))]
    .filter(Boolean).join('\n') || b.type
}

/** Content editor (knowledge-admin-content-editor). Videos use the video editor. */
export function KnowledgeContentEditorPage() {
  const { t } = useTranslation()
  const { id } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const { me, taxonomy } = useKnowledgeAdmin()
  const isNew = !id || id === 'new'
  const [content, setContent] = useState<Content | null>(null)
  const [form, setForm] = useState<ContentInput | null>(() => {
    if (!isNew) return null
    const type = (params.get('type') as ContentType | null) ?? 'ARTICLE'
    return { contentType: type === 'VIDEO' ? 'ARTICLE' : type, title: params.get('title') ?? '', audience: 'PUBLIC', blocks: [{ type: 'paragraph', text: '' }], typeFields: {} }
  })
  const [message, setMessage] = useState<{ tone: 'success' | 'error'; text: string } | null>(null)
  const [preview, setPreview] = useState<Item | null>(null)

  useEffect(() => {
    if (isNew) return
    knowledgeAdminApi.get(Number(id)).then((c) => {
      if (c.contentType === 'VIDEO') { navigate(`/knowledge-management/videos/${c.id}`, { replace: true }); return }
      setContent(c); setForm(toInput(c))
    }).catch(() => setMessage({ tone: 'error', text: t('knowledge.item.notFound') }))
  }, [id, isNew, navigate, t])

  if (!form) return message ? <Alert severity="error">{message.text}</Alert> : <CircularProgress aria-label={t('knowledge.common.loading')} />
  const readOnly = !!content && !me.publisher && ['IN_REVIEW', 'APPROVED', 'SCHEDULED'].includes(content.workflowState)
  const patch = (p: Partial<ContentInput>) => setForm({ ...form, ...p })
  const fields = TYPE_FIELDS[form.contentType] ?? []
  const typeValue = (name: string, list?: boolean) => {
    const v = form.typeFields?.[name]
    return Array.isArray(v) ? v.join('\n') : list ? (v ?? '') : (v ?? '')
  }
  const setTypeField = (name: string, raw: string, list?: boolean) => {
    const next: TypeFields = { ...(form.typeFields ?? {}) }
    next[name] = list ? raw.split('\n').map((x) => x.trim()).filter(Boolean) : name === 'synonyms' ? raw.split(',').map((x) => x.trim()).filter(Boolean) : raw
    patch({ typeFields: next })
  }
  const save = () => {
    const call = isNew || !content ? knowledgeAdminApi.create(form) : knowledgeAdminApi.update(content.id, form)
    call.then((c) => {
      setContent(c); setForm(toInput(c)); setMessage({ tone: 'success', text: t('knowledge.admin.editor.saved') })
      if (isNew) navigate(`/knowledge-management/content/${c.id}`, { replace: true })
    }).catch((e: Error) => setMessage({ tone: 'error', text: e.message }))
  }
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
        <Typography component="h1" variant="h5" sx={{ flex: 1 }}>
          {t(isNew ? 'knowledge.admin.editor.newTitle' : 'knowledge.admin.editor.editTitle', { type: t(`knowledge.type.${form.contentType}`) })}
        </Typography>
        {content && <StateChip state={content.workflowState} />}
        {content && <Chip size="small" variant="outlined" label={content.live ? t('knowledge.admin.editor.live', { version: content.liveVersion }) : t('knowledge.admin.editor.notLive')} />}
      </Box>
      {message && <Alert severity={message.tone} onClose={() => setMessage(null)}>{message.text}</Alert>}
      {content?.reviewComment && <Alert severity="info">{t('knowledge.admin.editor.returnedComment', { comment: content.reviewComment })}</Alert>}
      {readOnly && <Alert severity="info">{t('knowledge.admin.editor.contributorReadOnly')}</Alert>}
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        <Button variant="contained" startIcon={<Save size={16} />} onClick={save} disabled={readOnly || !form.title.trim()}>{t('knowledge.admin.editor.save')}</Button>
        {content && <Button variant="outlined" startIcon={<Eye size={16} />} onClick={() => knowledgeAdminApi.preview(content.id).then(setPreview).catch(() => undefined)}>{t('knowledge.admin.editor.preview')}</Button>}
        {content && <WorkflowActions content={content} publisher={me.publisher} onChange={(c) => { setContent(c); setForm(toInput(c)) }} onError={(m) => setMessage({ tone: 'error', text: m })} />}
      </Box>
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: '2fr 1fr' }, alignItems: 'start' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, minWidth: 0 }}>
          <Paper variant="outlined" component="section" aria-labelledby="ke-details" sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Typography id="ke-details" component="h2" sx={{ fontWeight: 700 }}>{t('knowledge.admin.editor.details')}</Typography>
            <TextField select size="small" label={t('knowledge.admin.editor.contentType')} value={form.contentType} disabled={!isNew}
              onChange={(e) => patch({ contentType: e.target.value as ContentType })}>
              {me.contentTypes.filter((ct) => ct !== 'VIDEO').map((ct) => <MenuItem key={ct} value={ct}>{t(`knowledge.type.${ct}`)}</MenuItem>)}
            </TextField>
            <TextField size="small" required label={t('knowledge.admin.editor.titleLabel')} value={form.title} disabled={readOnly} onChange={(e) => patch({ title: e.target.value })} slotProps={{ htmlInput: { maxLength: 200 } }} />
            <TextField size="small" multiline minRows={2} label={t('knowledge.admin.editor.shortDescription')} value={form.shortDescription ?? ''} disabled={readOnly}
              onChange={(e) => patch({ shortDescription: e.target.value })} slotProps={{ htmlInput: { maxLength: 500 } }} />
            <TextField size="small" label={t('knowledge.admin.editor.slug')} value={form.slug ?? ''} disabled={readOnly} onChange={(e) => patch({ slug: e.target.value })} />
          </Paper>
          {fields.length > 0 && (
            <Paper variant="outlined" component="section" aria-labelledby="ke-type" sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography id="ke-type" component="h2" sx={{ fontWeight: 700 }}>{t('knowledge.admin.editor.typeFields')}</Typography>
              {fields.map((f) => (
                <TextField key={f.name} size="small" multiline={f.multiline} minRows={f.multiline ? 2 : undefined} disabled={readOnly}
                  label={t(`knowledge.admin.editor.fields.${f.name}`)} value={typeValue(f.name, f.list)} onChange={(e) => setTypeField(f.name, e.target.value, f.list)} />
              ))}
            </Paper>
          )}
          <Paper variant="outlined" component="section" aria-labelledby="ke-content" sx={{ p: 2 }}>
            <Typography id="ke-content" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.editor.content')}</Typography>
            <BlockEditor blocks={form.blocks ?? []} onChange={(blocks) => patch({ blocks })} disabled={readOnly} />
          </Paper>
        </Box>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Paper variant="outlined" component="section" aria-labelledby="ke-meta" sx={{ p: 2 }}>
            <Typography id="ke-meta" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('knowledge.admin.editor.audienceSection')}</Typography>
            <ContentMetadataFields value={form} onChange={patch} taxonomy={taxonomy} disabled={readOnly} />
          </Paper>
          {content && <VersionsPanel content={content} publisher={me.publisher} onChange={(c) => { setContent(c); setForm(toInput(c)) }} />}
        </Box>
      </Box>
      <Dialog open={!!preview} onClose={() => setPreview(null)} fullWidth maxWidth="lg" aria-labelledby="ke-preview">
        <DialogTitle id="ke-preview">{t('knowledge.admin.editor.preview')}</DialogTitle>
        <DialogContent>{preview && <KnowledgeItemView item={preview} glossary={[]} />}</DialogContent>
        <DialogActions><Button onClick={() => setPreview(null)}>{t('knowledge.assistant.close')}</Button></DialogActions>
      </Dialog>
    </Box>
  )
}
