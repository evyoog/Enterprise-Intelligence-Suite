import { Alert, Box, Button, CircularProgress, Dialog, DialogContent, DialogTitle, Paper, TextField, ToggleButton, ToggleButtonGroup, Typography } from '@mui/material'
import { Eye, Save, UploadCloud } from 'lucide-react'
import { useEffect, useRef, useState, type DragEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useParams } from 'react-router-dom'
import {
  knowledgeMediaApi, knowledgeVideoApi, knowledgeAdminApi, type Content, type ContentInput, type Media, type StorageStatus, type VideoSource,
} from '../../api/knowledgeApi'
import { ContentMetadataFields } from '../../components/knowledge/ContentMetadataFields'
import { KnowledgeVideoPlayer } from '../../components/knowledge/KnowledgeVideoPlayer'
import { FilePickerButton } from '../../components/knowledge/FilePickerButton'
import { UploadProgressCard } from '../../components/knowledge/UploadProgressCard'
import { formatBytes } from '../../components/knowledge/knowledgeUtils'
import { useDirectUpload } from '../../components/knowledge/useDirectUpload'
import { StateChip } from './KnowledgeAdminLayout'
import { toInput, useKnowledgeAdmin } from './knowledgeAdminContext'
import { VersionsPanel, WorkflowActions } from './KnowledgeContentEditorPage'

const chaptersText = (c: Content['video']) => (c?.chapters ?? []).map((ch) => {
  const m = Math.floor(ch.seconds / 60); const s = ch.seconds % 60; const h = Math.floor(m / 60)
  return `${h ? `${h}:${String(m % 60).padStart(2, '0')}` : String(m).padStart(2, '0')}:${String(s).padStart(2, '0')} ${ch.title}`
}).join('\n')

/** Add / edit video (knowledge-admin-video-editor, REQ-KNW-004.3–.5). */
export function KnowledgeVideoEditorPage() {
  const { t } = useTranslation()
  const { id } = useParams()
  const navigate = useNavigate()
  const { me, taxonomy } = useKnowledgeAdmin()
  const isNew = !id || id === 'new'
  const [content, setContent] = useState<Content | null>(null)
  const [form, setForm] = useState<ContentInput>({ contentType: 'VIDEO', title: '', audience: 'PUBLIC', blocks: [] })
  const [source, setSource] = useState<VideoSource>('YOUTUBE')
  const [youtubeUrl, setYoutubeUrl] = useState('')
  const [url, setUrl] = useState('')
  const [mediaId, setMediaId] = useState<number | null>(null)
  const [duration, setDuration] = useState('')
  const [channel, setChannel] = useState('')
  const [thumbnailUrl, setThumbnailUrl] = useState('')
  const [thumbnailMediaId, setThumbnailMediaId] = useState<number | null>(null)
  const [transcript, setTranscript] = useState('')
  const [chapters, setChapters] = useState('')
  const [storage, setStorage] = useState<StorageStatus | null>(null)
  const [message, setMessage] = useState<{ tone: 'success' | 'error' | 'info'; text: string } | null>(null)
  const [previewOpen, setPreviewOpen] = useState(false)
  const [storageInfo, setStorageInfo] = useState<Media | null>(null)
  const fileInput = useRef<HTMLInputElement>(null)
  const video = useDirectUpload(knowledgeVideoApi.uploadUrl, knowledgeVideoApi.complete, knowledgeMediaApi.abort)
  const thumb = useDirectUpload(knowledgeMediaApi.uploadUrl, knowledgeMediaApi.complete, knowledgeMediaApi.abort)

  useEffect(() => { knowledgeMediaApi.storage().then(setStorage).catch(() => setStorage(null)) }, [])
  useEffect(() => {
    if (isNew) return
    knowledgeAdminApi.get(Number(id)).then((c) => {
      setContent(c); setForm(toInput(c))
      const v = c.video
      if (v) {
        setSource(v.sourceType); setYoutubeUrl(v.youtubeId ? `https://www.youtube.com/watch?v=${v.youtubeId}` : ''); setUrl(v.videoUrl ?? '')
        setMediaId(v.mediaId); setDuration(v.durationSeconds?.toString() ?? ''); setChannel(v.channel ?? ''); setThumbnailMediaId(v.thumbnailMediaId)
        setThumbnailUrl(v.sourceType === 'AWS_S3' || v.thumbnailMediaId ? '' : v.thumbnailUrl ?? ''); setTranscript(v.transcript ?? ''); setChapters(chaptersText(v))
        if (v.mediaId && me.publisher) knowledgeMediaApi.get(v.mediaId).then(setStorageInfo).catch(() => undefined)
      }
    }).catch(() => setMessage({ tone: 'error', text: t('knowledge.item.notFound') }))
  }, [id, isNew, me.publisher, t])

  const fetchYouTube = () => {
    knowledgeVideoApi.fetchYouTube(youtubeUrl).then((d) => {
      setForm((f) => ({ ...f, title: f.title || d.title || '', shortDescription: f.shortDescription || d.description || null }))
      if (d.durationSeconds) setDuration(String(d.durationSeconds))
      if (d.channel) setChannel(d.channel)
      if (d.thumbnailUrl) setThumbnailUrl(d.thumbnailUrl)
      setMessage({ tone: 'info', text: d.source === 'OEMBED' ? t('knowledge.admin.videos.fetchedOembed') : t('knowledge.admin.videos.fetched', { source: d.source }) })
    }).catch((e: Error) => setMessage({ tone: 'error', text: e.message }))
  }

  const uploadVideo = async (file: File) => {
    const media = await video.start(file, { productId: form.productId, moduleId: form.moduleId, folder: 'tutorials' })
    if (media) { setMediaId(media.id); setMessage({ tone: 'success', text: t('knowledge.admin.videos.uploaded') }) }
  }
  const onDrop = (e: DragEvent) => {
    e.preventDefault()
    const file = e.dataTransfer.files?.[0]
    if (file) uploadVideo(file)
  }
  const readTranscriptFile = (file: File) => { file.text().then(setTranscript).catch(() => undefined) }

  const save = () => {
    if (!form.title.trim() || !form.productId || !form.moduleId || !form.categoryId) {
      setMessage({ tone: 'error', text: t('knowledge.admin.videos.required') }); return
    }
    const body = {
      content: { ...form, contentType: 'VIDEO' as const }, sourceType: source,
      youtubeUrl: source === 'YOUTUBE' ? youtubeUrl : null, url: source === 'EXTERNAL_URL' ? url : null, mediaId: source === 'AWS_S3' ? mediaId : null,
      durationSeconds: duration ? Number(duration) : null, channel: channel || null, thumbnailUrl: source !== 'AWS_S3' && thumbnailUrl ? thumbnailUrl : null,
      thumbnailMediaId, transcript, chapters,
    }
    const call = isNew || !content ? knowledgeVideoApi.create(body) : knowledgeVideoApi.update(content.id, body)
    call.then((c) => {
      setContent(c); setForm(toInput(c)); setMessage({ tone: 'success', text: t('knowledge.admin.editor.saved') })
      if (isNew) navigate(`/knowledge-management/videos/${c.id}`, { replace: true })
    }).catch((e: Error) => setMessage({ tone: 'error', text: e.message }))
  }
  if (!isNew && !content && !message) return <CircularProgress aria-label={t('knowledge.common.loading')} />
  const readOnly = !!content && !me.publisher && ['IN_REVIEW', 'APPROVED', 'SCHEDULED'].includes(content.workflowState)
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
        <Typography component="h1" variant="h5" sx={{ flex: 1 }}>{isNew ? t('knowledge.admin.videos.add') : form.title}</Typography>
        {content && <StateChip state={content.workflowState} />}
      </Box>
      {message && <Alert severity={message.tone} onClose={() => setMessage(null)}>{message.text}</Alert>}
      {readOnly && <Alert severity="info">{t('knowledge.admin.editor.contributorReadOnly')}</Alert>}
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        <Button variant="contained" startIcon={<Save size={16} />} onClick={save} disabled={readOnly || video.verifying}>{t('knowledge.admin.videos.save')}</Button>
        {content?.video && <Button variant="outlined" startIcon={<Eye size={16} />} onClick={() => setPreviewOpen(true)}>{t('knowledge.admin.editor.preview')}</Button>}
        {content && <WorkflowActions content={content} publisher={me.publisher} onChange={(c) => { setContent(c); setForm(toInput(c)) }} onError={(m) => setMessage({ tone: 'error', text: m })} />}
      </Box>
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: '2fr 1fr' }, alignItems: 'start' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, minWidth: 0 }}>
          <Paper variant="outlined" component="section" aria-labelledby="kv-source" sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Typography id="kv-source" component="h2" sx={{ fontWeight: 700 }}>{t('knowledge.admin.videos.chooseSource')}</Typography>
            <ToggleButtonGroup exclusive value={source} onChange={(_, v: VideoSource | null) => v && setSource(v)} aria-labelledby="kv-source" disabled={readOnly}>
              <ToggleButton value="YOUTUBE">{t('knowledge.admin.videos.youtube')}</ToggleButton>
              <ToggleButton value="AWS_S3">{t('knowledge.admin.videos.s3')}</ToggleButton>
              <ToggleButton value="EXTERNAL_URL">{t('knowledge.admin.videos.external')}</ToggleButton>
            </ToggleButtonGroup>
            {source === 'YOUTUBE' && (
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <TextField size="small" label={t('knowledge.admin.videos.youtubeUrl')} value={youtubeUrl} onChange={(e) => setYoutubeUrl(e.target.value)} sx={{ flex: 1, minWidth: 260 }} disabled={readOnly} />
                <Button variant="outlined" onClick={fetchYouTube} disabled={!youtubeUrl || readOnly}>{t('knowledge.admin.videos.fetch')}</Button>
              </Box>
            )}
            {source === 'EXTERNAL_URL' && (
              <TextField size="small" label={t('knowledge.admin.videos.externalUrl')} value={url} onChange={(e) => setUrl(e.target.value)} disabled={readOnly} />
            )}
            {source === 'AWS_S3' && (storage && !storage.configured ? <Alert severity="info">{t('knowledge.admin.videos.storageOff')}</Alert> : (
              <>
                <Box onDragOver={(e) => e.preventDefault()} onDrop={onDrop} sx={{
                  border: 2, borderStyle: 'dashed', borderColor: 'divider', borderRadius: 3, p: 3, textAlign: 'center',
                  transition: 'border-color 160ms ease', '&:hover': { borderColor: 'primary.light' },
                }}>
                  <UploadCloud size={28} aria-hidden />
                  <Typography sx={{ mt: 1 }}>{t('knowledge.admin.videos.dropHere')}</Typography>
                  <Button variant="outlined" sx={{ mt: 1 }} onClick={() => fileInput.current?.click()} disabled={readOnly || (!isNew && !me.publisher && !!mediaId)}>
                    {mediaId && !isNew ? t('knowledge.admin.videos.replace') : t('knowledge.admin.videos.choose')}
                  </Button>
                  <input ref={fileInput} type="file" hidden accept="video/mp4,video/webm,video/quicktime,.mp4,.webm,.mov"
                    onChange={(e) => { const f = e.target.files?.[0]; if (f) uploadVideo(f); e.target.value = '' }} />
                  {storage && <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary', mt: 1 }}>{t('knowledge.admin.videos.formats', { size: formatBytes(storage.videoMaxSize) })}</Typography>}
                </Box>
                {video.progress && video.fileName && <UploadProgressCard fileName={video.fileName} progress={video.progress} verifying={video.verifying} onCancel={video.cancel} />}
              </>
            ))}
            <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' } }}>
              <TextField size="small" type="number" label={t('knowledge.admin.videos.duration')} value={duration} onChange={(e) => setDuration(e.target.value)} disabled={readOnly} />
              <TextField size="small" label={t('knowledge.admin.videos.channel')} value={channel} onChange={(e) => setChannel(e.target.value)} disabled={readOnly} />
              {source !== 'AWS_S3' && <TextField size="small" label={t('knowledge.admin.videos.thumbnailUrl')} value={thumbnailUrl} onChange={(e) => setThumbnailUrl(e.target.value)} disabled={readOnly} />}
              {storage?.configured && (
                <Box>
                  <FilePickerButton variant="outlined" size="small" disabled={readOnly} accept="image/png,image/jpeg,image/webp"
                    onFile={async (f) => { const m = await thumb.start(f, { kind: 'THUMBNAIL', productId: form.productId, moduleId: form.moduleId }); if (m) setThumbnailMediaId(m.id) }}>
                    {t('knowledge.admin.videos.thumbnailUpload')}
                  </FilePickerButton>
                  <Button size="small" disabled sx={{ ml: 1 }}>{t('knowledge.admin.videos.generateThumbnail')}</Button>
                  {thumb.progress && thumb.fileName && <Box sx={{ mt: 1 }}><UploadProgressCard fileName={thumb.fileName} progress={thumb.progress} verifying={thumb.verifying} onCancel={thumb.cancel} /></Box>}
                </Box>
              )}
            </Box>
          </Paper>
          <Paper variant="outlined" component="section" aria-labelledby="kv-details" sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Typography id="kv-details" component="h2" sx={{ fontWeight: 700 }}>{t('knowledge.admin.editor.details')}</Typography>
            <TextField size="small" required label={t('knowledge.admin.editor.titleLabel')} value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} disabled={readOnly} />
            <TextField size="small" multiline minRows={2} label={t('knowledge.admin.editor.shortDescription')} value={form.shortDescription ?? ''}
              onChange={(e) => setForm({ ...form, shortDescription: e.target.value })} disabled={readOnly} />
          </Paper>
          <Paper variant="outlined" component="section" aria-labelledby="kv-tracks" sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Typography id="kv-tracks" component="h2" sx={{ fontWeight: 700 }}>{t('knowledge.admin.videos.transcript')}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.videos.transcriptHint')}</Typography>
            <TextField multiline minRows={4} label={t('knowledge.admin.videos.transcript')} value={transcript} onChange={(e) => setTranscript(e.target.value)} disabled={readOnly} />
            <Box sx={{ display: 'flex', gap: 1 }}>
              <FilePickerButton size="small" variant="outlined" disabled={readOnly} accept=".vtt,.srt,.txt,text/plain,text/vtt" onFile={readTranscriptFile}>
                {t('knowledge.admin.videos.uploadTranscript')}
              </FilePickerButton>
              <Button size="small" disabled>{t('knowledge.admin.videos.autoTranscript')}</Button>
            </Box>
            <TextField multiline minRows={3} label={t('knowledge.admin.videos.chapters')} helperText={t('knowledge.admin.videos.chaptersHint')} value={chapters}
              onChange={(e) => setChapters(e.target.value)} disabled={readOnly} />
          </Paper>
        </Box>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Paper variant="outlined" component="section" aria-labelledby="kv-meta" sx={{ p: 2 }}>
            <Typography id="kv-meta" component="h2" sx={{ fontWeight: 700, mb: 1.5 }}>{t('knowledge.admin.editor.audienceSection')}</Typography>
            <ContentMetadataFields value={form} onChange={(p) => setForm({ ...form, ...p })} taxonomy={taxonomy} disabled={readOnly} required />
          </Paper>
          {storageInfo?.storage && (
            <Paper variant="outlined" component="section" aria-labelledby="kv-storage" sx={{ p: 2 }}>
              <Typography id="kv-storage" component="h2" sx={{ fontWeight: 700, mb: 1 }}>{t('knowledge.admin.videos.storage')}</Typography>
              {[['provider', storageInfo.storage.provider], ['bucket', storageInfo.storage.bucket], ['region', storageInfo.storage.region], ['path', storageInfo.storage.objectKey],
                ['format', storageInfo.storage.format], ['uploadStatus', storageInfo.storage.status]].map(([k, v]) => (
                <Box key={k} sx={{ mb: 0.75 }}><Typography variant="caption" sx={{ color: 'text.secondary' }}>{t(`knowledge.admin.media.${k}`)}</Typography>
                  <Typography variant="body2" sx={{ wordBreak: 'break-all' }}>{v ?? '—'}</Typography></Box>
              ))}
              <Typography variant="body2">{formatBytes(storageInfo.storage.size)}</Typography>
            </Paper>
          )}
          {content && <VersionsPanel content={content} publisher={me.publisher} onChange={(c) => { setContent(c); setForm(toInput(c)) }} />}
        </Box>
      </Box>
      <Dialog open={previewOpen} onClose={() => setPreviewOpen(false)} fullWidth maxWidth="lg" aria-labelledby="kv-preview">
        <DialogTitle id="kv-preview">{t('knowledge.admin.editor.preview')}</DialogTitle>
        <DialogContent>{content?.video && <KnowledgeVideoPlayer contentId={content.id} title={content.title} video={content.video} staffPreview />}</DialogContent>
      </Dialog>
    </Box>
  )
}
