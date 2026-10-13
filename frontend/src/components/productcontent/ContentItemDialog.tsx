import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Radio, RadioGroup, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { contentTypeOf, type UploadProgress } from '../../api/knowledgeApi'
import {
  productContentAdminApi, uploadProductFile, type AdminItem, type ContentKind, type DocumentationOption, type ItemRequest,
  type StorageInfo, type UploadedFile,
} from '../../api/productContentApi'
import { FilePickerButton } from '../knowledge/FilePickerButton'
import { UploadProgressCard } from '../knowledge/UploadProgressCard'
import { extensionOf, formatSize } from './productContentUtils'

type Slot = 'file' | 'logo'

/** One file the administrator uploaded in this dialog (browser → storage), waiting to be attached on Save. */
interface Pending { slot: Slot; uploaded: UploadedFile }

/**
 * Add or edit one content item (REQ-CAT-004.8). The fields depend on the kind.
 * A file is uploaded as soon as it is chosen, straight to storage with
 * progress and cancel; Save then attaches it and the backend verifies it.
 */
export function ContentItemDialog({ productId, kind, item, storage, onClose, onSaved }: {
  productId: number; kind: ContentKind; item: AdminItem | null; storage: StorageInfo
  onClose: () => void; onSaved: (saved: AdminItem) => void
}) {
  const { t } = useTranslation()
  const [title, setTitle] = useState(item?.title ?? '')
  const [description, setDescription] = useState(item?.description ?? '')
  const [altText, setAltText] = useState(item?.altText ?? '')
  const [videoUrl, setVideoUrl] = useState(item?.videoUrl ?? '')
  // PC-3 (C89): a video is a link or a file uploaded to the product storage, never both.
  const [videoMode, setVideoMode] = useState<'link' | 'file'>(item?.videoProvider === 'HOSTED' ? 'file' : 'link')
  const [customerName, setCustomerName] = useState(item?.customerName ?? '')
  const [problem, setProblem] = useState(item?.problem ?? '')
  const [result, setResult] = useState(item?.result ?? '')
  const [articleId, setArticleId] = useState<number | ''>(item?.articleId ?? '')
  const [options, setOptions] = useState<DocumentationOption[] | null>(null)
  const [pending, setPending] = useState<Pending[]>([])
  const [uploading, setUploading] = useState<{ slot: Slot; name: string; progress: UploadProgress } | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [saving, setSaving] = useState(false)
  // The running upload's cancel function; kept in state so the Cancel button can use it.
  const [cancelUpload, setCancelUpload] = useState<(() => void) | null>(null)

  useEffect(() => {
    if (kind !== 'DOCUMENTATION') return
    let active = true
    productContentAdminApi.documentationOptions(productId)
      .then((o) => { if (active) setOptions(o) })
      .catch(() => { if (active) setOptions([]) })
    return () => { active = false }
  }, [kind, productId])

  const hostedVideo = kind === 'VIDEO' && videoMode === 'file'
  const hasFileSlot = kind === 'DATASHEET' || kind === 'IMAGE' || kind === 'CASE_STUDY' || hostedVideo
  const imageAllowed = kind === 'IMAGE'
  const allowedFor = (slot: Slot) => (slot === 'logo' || imageAllowed ? storage.imageTypes : kind === 'VIDEO' ? storage.videoTypes : storage.documentTypes)
  const maxFor = (slot: Slot) => (slot === 'logo' || imageAllowed ? storage.imageMaxSize : kind === 'VIDEO' ? storage.videoMaxSize : storage.documentMaxSize)
  const pendingOf = (slot: Slot) => pending.find((p) => p.slot === slot)?.uploaded
  const busy = saving || uploading !== null && (uploading.progress.state === 'preparing' || uploading.progress.state === 'uploading')

  const pick = async (slot: Slot, file: File) => {
    setError(null)
    const types = allowedFor(slot)
    if (!types.includes(extensionOf(file.name))) {
      setError(t('productContent.errors.fileType', { types: types.join(', ').toUpperCase() }))
      return
    }
    if (file.size > maxFor(slot)) {
      setError(t('productContent.errors.fileSize', { size: formatSize(maxFor(slot)) }))
      return
    }
    const contentType = contentTypeOf(file)
    setUploading({ slot, name: file.name, progress: { state: 'preparing', loaded: 0, total: file.size, speed: null } })
    try {
      const url = await productContentAdminApi.uploadUrl(productId, { kind, purpose: slot, fileName: file.name, contentType, size: file.size })
      const handle = uploadProductFile(url, file, contentType, (progress) => setUploading({ slot, name: file.name, progress }))
      setCancelUpload(() => handle.cancel)
      await handle.promise
      setPending((p) => [...p.filter((x) => x.slot !== slot), { slot, uploaded: { uploadKey: url.uploadKey, fileName: file.name, contentType, size: file.size } }])
      setUploading(null)
    } catch (e) {
      const cancelled = e instanceof Error && e.message === 'cancelled'
      setUploading(null)
      if (!cancelled) setError(e instanceof ApiError ? e.message : t('productContent.dialog.uploadFailed'))
    }
  }

  const validate = () => {
    const errors: Record<string, string> = {}
    if (!title.trim() || title.trim().length > 200) errors.title = t('productContent.errors.title')
    if (kind === 'IMAGE' && !altText.trim()) errors.altText = t('productContent.errors.alt')
    if (kind === 'VIDEO' && videoMode === 'link' && !/^https:\/\/\S+$/i.test(videoUrl.trim())) errors.videoUrl = t('productContent.errors.videoUrl')
    if (hostedVideo && !pendingOf('file') && item?.videoProvider !== 'HOSTED') errors.file = t('productContent.errors.file')
    if (kind === 'CASE_STUDY' && !customerName.trim()) errors.customerName = t('productContent.errors.customer')
    if (kind === 'DOCUMENTATION' && articleId === '') errors.article = t('productContent.errors.article')
    if ((kind === 'DATASHEET' || kind === 'IMAGE') && !item && !pendingOf('file')) errors.file = t('productContent.errors.file')
    setFieldErrors(errors)
    return Object.keys(errors).length === 0
  }

  const save = async () => {
    setError(null)
    if (!validate()) return
    const request: ItemRequest = {
      kind, title: title.trim(), description: description.trim() || null, altText: altText.trim() || null,
      videoUrl: kind === 'VIDEO' && videoMode === 'file' ? null : videoUrl.trim() || null, customerName: customerName.trim() || null, problem: problem.trim() || null,
      result: result.trim() || null, articleId: articleId === '' ? null : articleId,
      file: kind === 'VIDEO' && videoMode === 'link' ? null : pendingOf('file') ?? null, logo: pendingOf('logo') ?? null,
    }
    setSaving(true)
    try {
      const saved = item
        ? await productContentAdminApi.update(productId, item.id, request)
        : await productContentAdminApi.create(productId, request)
      onSaved(saved)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('productContent.errors.save'))
    } finally {
      setSaving(false)
    }
  }

  const filePicker = (slot: Slot) => {
    const ready = pendingOf(slot)
    const current = slot === 'file' ? item?.file : item?.logo
    const accept = allowedFor(slot).map((x) => `.${x}`).join(',')
    const label = slot === 'logo'
      ? t(current || ready ? 'productContent.dialog.replaceLogo' : 'productContent.dialog.chooseLogo')
      : t(current || ready ? 'productContent.dialog.replaceFile' : 'productContent.dialog.chooseFile')
    return (
      <Box key={slot} sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
          <FilePickerButton variant="outlined" size="small" accept={accept} disabled={!storage.configured || busy}
            onFile={(f) => pick(slot, f)}>{label}</FilePickerButton>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            {ready ? t('productContent.dialog.fileReady', { name: ready.fileName })
              : current ? t(slot === 'logo' ? 'productContent.dialog.logoKept' : 'productContent.dialog.fileKept', { name: current.fileName })
                : `${allowedFor(slot).join(', ').toUpperCase()} · ${formatSize(maxFor(slot))}`}
          </Typography>
        </Box>
        {uploading?.slot === slot && (
          <UploadProgressCard fileName={uploading.name} progress={uploading.progress} onCancel={() => cancelUpload?.()} />
        )}
        {slot === 'file' && fieldErrors.file && <Typography variant="caption" color="error" role="alert">{fieldErrors.file}</Typography>}
      </Box>
    )
  }

  const kindName = t(`productContent.dialog.kind.${kind}`)
  return (
    <Dialog open onClose={busy ? undefined : onClose} maxWidth="sm" fullWidth aria-labelledby="content-dialog-title">
      <DialogTitle id="content-dialog-title">
        {t(item ? 'productContent.dialog.editTitle' : 'productContent.dialog.addTitle', { kind: kindName })}
      </DialogTitle>
      <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '8px !important' }}>
        {!storage.configured && (hasFileSlot || kind === 'VIDEO') && <Alert severity="info">{t('productContent.storageOff')}</Alert>}
        {error && <Alert severity="error" role="alert">{error}</Alert>}

        <TextField label={t('productContent.fields.title')} value={title} onChange={(e) => setTitle(e.target.value)} required
          error={!!fieldErrors.title} helperText={fieldErrors.title} slotProps={{ htmlInput: { maxLength: 200 } }} />
        {kind !== 'CASE_STUDY' && (
          <TextField label={t('productContent.fields.description')} value={description} onChange={(e) => setDescription(e.target.value)}
            multiline minRows={2} slotProps={{ htmlInput: { maxLength: 1000 } }} />
        )}

        {kind === 'IMAGE' && (
          <TextField label={t('productContent.fields.altText')} value={altText} onChange={(e) => setAltText(e.target.value)} required
            error={!!fieldErrors.altText} helperText={fieldErrors.altText} slotProps={{ htmlInput: { maxLength: 250 } }} />
        )}

        {kind === 'VIDEO' && (
          <RadioGroup row aria-label={t('productContent.fields.videoMode')} value={videoMode}
            onChange={(e) => setVideoMode(e.target.value === 'file' ? 'file' : 'link')}>
            <FormControlLabel value="link" control={<Radio />} label={t('productContent.fields.videoModeLink')} />
            <FormControlLabel value="file" control={<Radio />} label={t('productContent.fields.videoModeFile')} disabled={!storage.configured} />
          </RadioGroup>
        )}
        {kind === 'VIDEO' && videoMode === 'link' && (
          <TextField label={t('productContent.fields.videoUrl')} value={videoUrl} onChange={(e) => setVideoUrl(e.target.value)} required
            type="url" error={!!fieldErrors.videoUrl} helperText={fieldErrors.videoUrl ?? t('productContent.fields.videoHelp')}
            placeholder="https://www.youtube.com/watch?v=…" />
        )}

        {kind === 'CASE_STUDY' && (
          <>
            <TextField label={t('productContent.fields.customerName')} value={customerName} onChange={(e) => setCustomerName(e.target.value)} required
              error={!!fieldErrors.customerName} helperText={fieldErrors.customerName} slotProps={{ htmlInput: { maxLength: 200 } }} />
            <TextField label={t('productContent.fields.problem')} value={problem} onChange={(e) => setProblem(e.target.value)} multiline minRows={3}
              slotProps={{ htmlInput: { maxLength: 4000 } }} />
            <TextField label={t('productContent.fields.result')} value={result} onChange={(e) => setResult(e.target.value)} multiline minRows={3}
              slotProps={{ htmlInput: { maxLength: 4000 } }} />
            {filePicker('logo')}
          </>
        )}

        {kind === 'DOCUMENTATION' && (
          <TextField select label={t('productContent.fields.article')} value={articleId} required
            onChange={(e) => setArticleId(e.target.value === '' ? '' : Number(e.target.value))}
            error={!!fieldErrors.article} helperText={fieldErrors.article ?? t('productContent.fields.articleHelp')}>
            {(options ?? []).map((o) => (
              <MenuItem key={o.id} value={o.id}>
                {o.title}{o.forThisProduct ? ` (${t('productContent.fields.thisProduct')})` : ''}
              </MenuItem>
            ))}
            {item?.articleId != null && !(options ?? []).some((o) => o.id === item.articleId) && (
              <MenuItem value={item.articleId}>{item.articleTitle ?? `#${item.articleId}`}</MenuItem>
            )}
          </TextField>
        )}

        {hasFileSlot && filePicker('file')}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('productContent.dialog.cancel')}</Button>
        <Button variant="contained" onClick={save} disabled={busy}>
          {t(saving ? 'productContent.dialog.saving' : 'productContent.dialog.save')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
