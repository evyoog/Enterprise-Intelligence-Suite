import { Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Paper, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material'
import { Upload } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { knowledgeMediaApi, type Media, type MediaKind, type Page, type StorageStatus } from '../../api/knowledgeApi'
import { FilePickerButton } from '../../components/knowledge/FilePickerButton'
import { UploadProgressCard } from '../../components/knowledge/UploadProgressCard'
import { formatBytes, formatDate } from '../../components/knowledge/knowledgeUtils'
import { useDirectUpload } from '../../components/knowledge/useDirectUpload'
import { EmptyState } from '../../components/ui/EmptyState'
import { useKnowledgeAdmin } from './knowledgeAdminContext'

const KINDS: MediaKind[] = ['IMAGE', 'DOCUMENT', 'AUDIO', 'TEMPLATE', 'VIDEO_FILE', 'THUMBNAIL', 'SUBTITLE']

/** Media library (knowledge-admin-media-library, REQ-KNW-003.5–.7). */
export function KnowledgeMediaLibraryPage() {
  const { t, i18n } = useTranslation()
  const { me } = useKnowledgeAdmin()
  const [storage, setStorage] = useState<StorageStatus | null>(null)
  const [kind, setKind] = useState<MediaKind | ''>('')
  const [uploadKind, setUploadKind] = useState<MediaKind>('DOCUMENT')
  const [q, setQ] = useState('')
  const [data, setData] = useState<Page<Media> | null>(null)
  const [selected, setSelected] = useState<Media | null>(null)
  const [deleting, setDeleting] = useState<{ media: Media; usedBy: Media['usedBy'] } | null>(null)
  const [error, setError] = useState<string | null>(null)
  const upload = useDirectUpload(knowledgeMediaApi.uploadUrl, knowledgeMediaApi.complete, knowledgeMediaApi.abort)

  const load = useCallback(() => {
    knowledgeMediaApi.list({ kind: kind || undefined, q: q || undefined, size: 50 }).then(setData).catch(() => setError(t('knowledge.common.error')))
  }, [kind, q, t])
  useEffect(() => { knowledgeMediaApi.storage().then(setStorage).catch(() => setStorage(null)) }, [])
  useEffect(() => { const h = setTimeout(load, 200); return () => clearTimeout(h) }, [load])

  const remove = (media: Media, confirm: boolean) => {
    knowledgeMediaApi.remove(media.id, confirm).then(() => { setDeleting(null); load() }).catch((e: unknown) => {
      if (e instanceof ApiError && e.status === 409 && Array.isArray(e.detail?.usedBy)) setDeleting({ media, usedBy: e.detail.usedBy as Media['usedBy'] })
      else setError((e as Error).message)
    })
  }
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {storage && !storage.configured && <Alert severity="info">{t('knowledge.admin.videos.storageOff')}</Alert>}
      {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
      <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField size="small" label={t('knowledge.admin.media.search')} value={q} onChange={(e) => setQ(e.target.value)} sx={{ flex: 1, minWidth: 220 }} />
        <TextField select size="small" label={t('knowledge.admin.media.kind')} value={kind} onChange={(e) => setKind(e.target.value as MediaKind | '')} sx={{ minWidth: 160 }}>
          <MenuItem value="">{t('knowledge.admin.media.allKinds')}</MenuItem>
          {KINDS.map((k) => <MenuItem key={k} value={k}>{t(`knowledge.admin.media.kinds.${k}`)}</MenuItem>)}
        </TextField>
        {storage?.configured && (
          <>
            <TextField select size="small" label={t('knowledge.admin.media.kind')} value={uploadKind} onChange={(e) => setUploadKind(e.target.value as MediaKind)} sx={{ minWidth: 150 }}>
              {KINDS.filter((k) => k !== 'VIDEO_FILE').map((k) => <MenuItem key={k} value={k}>{t(`knowledge.admin.media.kinds.${k}`)}</MenuItem>)}
            </TextField>
            <FilePickerButton variant="contained" startIcon={<Upload size={16} />} onFile={async (f) => { if (await upload.start(f, { kind: uploadKind })) load() }}>
              {t('knowledge.admin.media.upload')}
            </FilePickerButton>
          </>
        )}
      </Box>
      {upload.progress && upload.fileName && <UploadProgressCard fileName={upload.fileName} progress={upload.progress} verifying={upload.verifying} onCancel={upload.cancel} />}
      {!data && <CircularProgress aria-label={t('knowledge.common.loading')} />}
      {data && data.items.length === 0 && <EmptyState title={t('knowledge.admin.media.empty')} />}
      {data && data.items.length > 0 && (
        <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
          <Table size="small">
            <TableHead><TableRow>{['id', 'file', 'type', 'size', 'uploaded', 'usedBy', 'version'].map((c) => <TableCell key={c}>{t(`knowledge.admin.media.columns.${c}`)}</TableCell>)}<TableCell /></TableRow></TableHead>
            <TableBody>
              {data.items.map((m) => (
                <TableRow key={m.id} hover>
                  <TableCell>{m.id}</TableCell>
                  <TableCell sx={{ fontWeight: 600, wordBreak: 'break-all' }}>{m.fileName}</TableCell>
                  <TableCell>{t(`knowledge.admin.media.kinds.${m.kind}`)}</TableCell>
                  <TableCell>{formatBytes(m.size)}</TableCell>
                  <TableCell>{formatDate(m.uploadedAt ?? m.createdAt, i18n.language)}</TableCell>
                  <TableCell>{m.usedBy.length === 0 ? t('knowledge.admin.media.notUsed') : t('knowledge.admin.media.usedIn', { count: m.usedBy.length })}</TableCell>
                  <TableCell>{m.version}</TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>
                    <Button size="small" onClick={() => setSelected(m)}>{t('knowledge.admin.media.preview')}</Button>
                    {me.publisher && <Button size="small" color="error" onClick={() => remove(m, false)}>{t('knowledge.admin.media.delete')}</Button>}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
      <MediaDetailsDialog media={selected} onClose={() => setSelected(null)} />
      <Dialog open={!!deleting} onClose={() => setDeleting(null)} aria-labelledby="km-delete">
        <DialogTitle id="km-delete">{t('knowledge.admin.media.delete')}</DialogTitle>
        <DialogContent>
          <Typography sx={{ mb: 1 }}>{t('knowledge.admin.media.deleteConfirm')}</Typography>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>{t('knowledge.admin.media.inUse')}</Typography>
          <Box component="ul" sx={{ m: 0, pl: 3 }}>
            {deleting?.usedBy.map((u) => <li key={u.contentId}><RouterLink to={`/knowledge-management/content/${u.contentId}`}>{u.title}</RouterLink></li>)}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleting(null)}>{t('knowledge.admin.editor.cancel')}</Button>
          <Button color="error" variant="contained" onClick={() => deleting && remove(deleting.media, true)}>{t('knowledge.admin.media.deleteAnyway')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

function MediaDetailsDialog({ media, onClose }: { media: Media | null; onClose: () => void }) {
  const { t } = useTranslation()
  const [preview, setPreview] = useState<{ id: number; url: string } | null>(null)
  useEffect(() => {
    if (media && media.status === 'READY' && (media.kind === 'IMAGE' || media.kind === 'THUMBNAIL')) {
      knowledgeMediaApi.previewUrl(media.id).then((u) => setPreview({ id: media.id, url: u.url })).catch(() => undefined)
    }
  }, [media])
  const url = preview && media && preview.id === media.id ? preview.url : null
  return (
    <Dialog open={!!media} onClose={onClose} fullWidth maxWidth="sm" aria-labelledby="km-details">
      <DialogTitle id="km-details">{media?.fileName}</DialogTitle>
      <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {url && <Box component="img" src={url} alt={media?.fileName ?? ''} sx={{ maxWidth: '100%', borderRadius: 2 }} />}
        <Typography variant="body2">{t('knowledge.admin.media.copyId', { id: media?.id })}</Typography>
        {media?.storage && (
          <Box>
            <Typography sx={{ fontWeight: 700, mt: 1 }}>{t('knowledge.admin.media.storageInfo')}</Typography>
            {(['provider', 'bucket', 'region'] as const).map((k) => <Typography key={k} variant="body2">{t(`knowledge.admin.media.${k}`)}: {media.storage?.[k] ?? '—'}</Typography>)}
            <Typography variant="body2" sx={{ wordBreak: 'break-all' }}>{t('knowledge.admin.media.path')}: {media.storage.objectKey}</Typography>
            <Typography variant="body2">{t('knowledge.admin.media.format')}: {media.storage.format}</Typography>
            <Typography variant="body2">{t('knowledge.admin.media.uploadStatus')}: {media.storage.status}</Typography>
          </Box>
        )}
        {media && media.usedBy.length > 0 && (
          <Box>
            <Typography sx={{ fontWeight: 700, mt: 1 }}>{t('knowledge.admin.media.columns.usedBy')}</Typography>
            {media.usedBy.map((u) => <Typography key={u.contentId} variant="body2">{u.title}</Typography>)}
          </Box>
        )}
      </DialogContent>
      <DialogActions><Button onClick={onClose}>{t('knowledge.assistant.close')}</Button></DialogActions>
    </Dialog>
  )
}
