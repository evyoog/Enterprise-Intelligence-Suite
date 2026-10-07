import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Avatar, Box, Button, Chip, IconButton, Paper, Skeleton, Snackbar, Tooltip, Typography } from '@mui/material'
import { ArrowDown, ArrowUp, Eye, EyeOff, Pencil, Plus, Trash2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  productContentAdminApi, type AdminContent, type AdminItem, type ContentKind,
} from '../../api/productContentApi'
import { ConfirmDialog } from '../ui/ConfirmDialog'
import { ErrorState } from '../ui/ErrorState'
import { ContentItemDialog } from './ContentItemDialog'
import { CONTENT_KINDS, formatDay, formatSize } from './productContentUtils'

/**
 * Product content (REQ-CAT-004, 02.04): the administrator's Content tab on an
 * application. Datasheet, Documentation, Images, Videos and Case studies, each
 * with add, edit (replace file), publish / unpublish, move up / down and
 * delete. Files go browser → storage with a presigned URL; nothing here holds
 * a credential.
 */
export function ProductContentManager({ productId }: { productId: number }) {
  const { t, i18n } = useTranslation()
  const [data, setData] = useState<AdminContent | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [dialog, setDialog] = useState<{ kind: ContentKind; item: AdminItem | null } | null>(null)
  const [deleting, setDeleting] = useState<AdminItem | null>(null)
  const [busy, setBusy] = useState(false)
  const [toast, setToast] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const load = useCallback(() => {
    productContentAdminApi.list(productId)
      .then((d) => { setData(d); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('productContent.loadError')))
  }, [productId, t])

  useEffect(() => { load() }, [load])

  const run = async (action: () => Promise<unknown>, message: string) => {
    setBusy(true)
    setActionError(null)
    try {
      await action()
      setToast(message)
      load()
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : t('productContent.errors.save'))
    } finally {
      setBusy(false)
    }
  }

  const move = (item: AdminItem, direction: -1 | 1) => {
    if (!data) return
    const all = data.items
    const ofKind = all.filter((i) => i.kind === item.kind)
    const index = ofKind.findIndex((i) => i.id === item.id)
    const swapWith = index + direction
    if (swapWith < 0 || swapWith >= ofKind.length) return
    const reordered = [...ofKind]
    ;[reordered[index], reordered[swapWith]] = [reordered[swapWith], reordered[index]]
    let next = 0
    const ids = all.map((i) => (i.kind === item.kind ? reordered[next++].id : i.id))
    run(() => productContentAdminApi.reorder(productId, ids), t('productContent.toast.saved'))
  }

  if (error) return <ErrorState title={t('productContent.loadError')} message={error} onRetry={load} />
  if (!data) {
    return (
      <Box aria-busy="true" sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {[0, 1, 2].map((i) => <Skeleton key={i} variant="rounded" height={110} />)}
      </Box>
    )
  }

  const { storage } = data
  const sizeFor = (kind: ContentKind) => formatSize(kind === 'IMAGE' ? storage.imageMaxSize : storage.documentMaxSize)
  const needsStorage = (kind: ContentKind) => kind === 'DATASHEET' || kind === 'IMAGE'

  const meta = (item: AdminItem) => {
    const parts = [t('productContent.meta.version', { n: item.version }), t('productContent.meta.updated', { date: formatDay(item.updatedAt, i18n.language) })]
    if (item.file) parts.push(t('productContent.meta.file', { name: item.file.fileName, size: formatSize(item.file.size) }))
    if (item.kind === 'CASE_STUDY' && item.customerName) parts.unshift(item.customerName)
    return parts.join(' · ')
  }

  const thumb = (item: AdminItem) => {
    const src = item.kind === 'IMAGE' ? item.fileUrl : item.kind === 'VIDEO' ? item.thumbnailUrl : item.kind === 'CASE_STUDY' ? item.logoUrl : null
    return src ? <Avatar variant="rounded" src={src} alt="" sx={{ width: 56, height: 56 }} /> : null
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, pb: 4 }}>
      <Box>
        <Typography component="h2" variant="h6" sx={{ fontWeight: 700 }}>{t('productContent.title')}</Typography>
        <Typography sx={{ color: 'text.secondary', maxWidth: 760 }}>{t('productContent.intro')}</Typography>
      </Box>
      {!storage.configured && <Alert severity="info">{t('productContent.storageOff')}</Alert>}
      {actionError && <Alert severity="error" role="alert" onClose={() => setActionError(null)}>{actionError}</Alert>}

      {CONTENT_KINDS.map((kind) => {
        const items = data.items.filter((i) => i.kind === kind)
        return (
          <Paper key={kind} component="section" variant="outlined" aria-labelledby={`pc-${kind}`} sx={{ p: 2.5, borderRadius: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap', mb: 1.5 }}>
              <Box sx={{ minWidth: 0 }}>
                <Typography id={`pc-${kind}`} component="h3" sx={{ fontWeight: 700, fontSize: 16 }}>{t(`productContent.sections.${kind}.title`)}</Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary', maxWidth: 640 }}>{t(`productContent.sections.${kind}.help`, { size: sizeFor(kind) })}</Typography>
              </Box>
              <Button size="small" variant="outlined" startIcon={<Plus size={16} />} disabled={busy || (needsStorage(kind) && !storage.configured)}
                onClick={() => setDialog({ kind, item: null })}>{t(`productContent.sections.${kind}.add`)}</Button>
            </Box>

            {items.length === 0 ? (
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t(`productContent.sections.${kind}.empty`)}</Typography>
            ) : (
              <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1 }}>
                {items.map((item, index) => (
                  <Box component="li" key={item.id} sx={{ display: 'flex', alignItems: 'center', gap: 1.5, p: 1.25, border: '1px solid', borderColor: 'divider', borderRadius: 2, flexWrap: 'wrap' }}>
                    {thumb(item)}
                    <Box sx={{ flex: 1, minWidth: 200 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                        <Typography sx={{ fontWeight: 600 }}>{item.title}</Typography>
                        <Chip size="small" label={t(`productContent.status.${item.status}`)} color={item.status === 'PUBLISHED' ? 'success' : 'default'} variant={item.status === 'PUBLISHED' ? 'filled' : 'outlined'} />
                      </Box>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{meta(item)}</Typography>
                      {item.kind === 'DOCUMENTATION' && !item.articleAvailable && (
                        <Typography variant="body2" sx={{ color: 'warning.main' }}>{t('productContent.meta.orphan')}</Typography>
                      )}
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, flexWrap: 'wrap' }}>
                      <Tooltip title={t('productContent.actions.moveUp', { title: item.title })}>
                        <span><IconButton size="small" aria-label={t('productContent.actions.moveUp', { title: item.title })} disabled={busy || index === 0}
                          onClick={() => move(item, -1)}><ArrowUp size={16} /></IconButton></span>
                      </Tooltip>
                      <Tooltip title={t('productContent.actions.moveDown', { title: item.title })}>
                        <span><IconButton size="small" aria-label={t('productContent.actions.moveDown', { title: item.title })} disabled={busy || index === items.length - 1}
                          onClick={() => move(item, 1)}><ArrowDown size={16} /></IconButton></span>
                      </Tooltip>
                      <Button size="small" startIcon={item.status === 'PUBLISHED' ? <EyeOff size={15} /> : <Eye size={15} />} disabled={busy}
                        aria-label={t(item.status === 'PUBLISHED' ? 'productContent.actions.unpublishItem' : 'productContent.actions.publishItem', { title: item.title })}
                        onClick={() => run(() => (item.status === 'PUBLISHED' ? productContentAdminApi.unpublish(productId, item.id) : productContentAdminApi.publish(productId, item.id)),
                          t(item.status === 'PUBLISHED' ? 'productContent.toast.unpublished' : 'productContent.toast.published'))}>
                        {t(item.status === 'PUBLISHED' ? 'productContent.actions.unpublish' : 'productContent.actions.publish')}
                      </Button>
                      <IconButton size="small" aria-label={t('productContent.actions.editItem', { title: item.title })} disabled={busy}
                        onClick={() => setDialog({ kind, item })}><Pencil size={16} /></IconButton>
                      <IconButton size="small" color="error" aria-label={t('productContent.actions.deleteItem', { title: item.title })} disabled={busy}
                        onClick={() => setDeleting(item)}><Trash2 size={16} /></IconButton>
                    </Box>
                  </Box>
                ))}
              </Box>
            )}
          </Paper>
        )
      })}

      {dialog && (
        <ContentItemDialog productId={productId} kind={dialog.kind} item={dialog.item} storage={storage}
          onClose={() => setDialog(null)}
          onSaved={() => { setDialog(null); setToast(t('productContent.toast.saved')); load() }} />
      )}
      <ConfirmDialog open={deleting !== null} title={t('productContent.delete.title')}
        body={deleting ? t('productContent.delete.body', { title: deleting.title }) : undefined}
        confirmLabel={t('productContent.actions.delete')} busy={busy}
        onClose={() => setDeleting(null)}
        onConfirm={() => { const item = deleting; setDeleting(null); if (item) run(() => productContentAdminApi.remove(productId, item.id), t('productContent.toast.deleted')) }} />
      <Snackbar open={toast !== null} autoHideDuration={3500} onClose={() => setToast(null)} message={toast ?? ''} />
    </Box>
  )
}
