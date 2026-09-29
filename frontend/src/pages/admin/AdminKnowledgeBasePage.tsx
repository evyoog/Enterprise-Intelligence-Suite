import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  Paper, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { adminKnowledgeBaseApi, type KnowledgeArticle } from '../../api/knowledgeBaseApi'
import { PageHeader } from '../../components/layout/PageHeader'

/** "/admin/knowledge-base" — MANAGE_KNOWLEDGE_BASE. One page: list, create,
 * edit, publish/unpublish, delete — mirrors ServiceStatusAdminPage's
 * inline-editable single-page pattern rather than a separate edit route. */
export function AdminKnowledgeBasePage() {
  const { t } = useTranslation()
  const [articles, setArticles] = useState<KnowledgeArticle[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [editing, setEditing] = useState<KnowledgeArticle | 'new' | null>(null)
  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)

  const load = () => {
    adminKnowledgeBaseApi.listAll()
      .then(setArticles)
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : 'Could not load articles.'))
  }

  useEffect(load, [])

  const openNew = () => { setEditing('new'); setTitle(''); setBody(''); setFormError(null) }
  const openEdit = (article: KnowledgeArticle) => { setEditing(article); setTitle(article.title); setBody(article.body); setFormError(null) }

  const save = async () => {
    setSaving(true)
    setFormError(null)
    try {
      if (editing === 'new') await adminKnowledgeBaseApi.create({ title, body })
      else if (editing) await adminKnowledgeBaseApi.update(editing.id, { title, body })
      setEditing(null)
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : 'Could not save this article.')
    } finally {
      setSaving(false)
    }
  }

  const togglePublish = async (article: KnowledgeArticle) => {
    setBusyId(article.id)
    try {
      const updated = article.status === 'PUBLISHED'
        ? await adminKnowledgeBaseApi.unpublish(article.id)
        : await adminKnowledgeBaseApi.publish(article.id)
      setArticles((prev) => prev?.map((a) => (a.id === updated.id ? updated : a)) ?? prev)
    } finally {
      setBusyId(null)
    }
  }

  const remove = async (article: KnowledgeArticle) => {
    setBusyId(article.id)
    try {
      await adminKnowledgeBaseApi.delete(article.id)
      setArticles((prev) => prev?.filter((a) => a.id !== article.id) ?? prev)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Box>
      <PageHeader
        title={t('knowledgeBase.admin.title')}
        action={<Button variant="contained" onClick={openNew}>{t('knowledgeBase.admin.newArticle')}</Button>}
      />
      {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {articles?.map((article) => (
          <Paper key={article.id} variant="outlined" sx={{ p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1 }}>
            <Box sx={{ cursor: 'pointer', flex: 1, minWidth: 200 }} onClick={() => openEdit(article)}>
              <Typography sx={{ fontWeight: 700 }}>{article.title}</Typography>
              <Chip size="small" sx={{ mr: 1, mt: 0.5 }} color={article.status === 'PUBLISHED' ? 'success' : 'default'} label={t(`knowledgeBase.admin.status.${article.status}`)} />
              <Typography component="span" variant="caption" sx={{ color: 'text.secondary' }}>{t('knowledgeBase.admin.version', { version: article.version })}</Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button size="small" variant="outlined" disabled={busyId === article.id} onClick={() => togglePublish(article)}>
                {article.status === 'PUBLISHED' ? t('knowledgeBase.admin.unpublish') : t('knowledgeBase.admin.publish')}
              </Button>
              <Button size="small" color="error" variant="outlined" disabled={busyId === article.id} onClick={() => remove(article)}>
                {t('knowledgeBase.admin.delete')}
              </Button>
            </Box>
          </Paper>
        ))}
      </Box>

      <Dialog open={editing !== null} onClose={() => setEditing(null)} fullWidth maxWidth="sm">
        <DialogTitle>{t('knowledgeBase.admin.newArticle')}</DialogTitle>
        <DialogContent>
          {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
          <TextField
            fullWidth
            sx={{ mt: 1, mb: 2 }}
            label={t('knowledgeBase.admin.titleLabel')}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <TextField
            fullWidth
            multiline
            minRows={6}
            label={t('knowledgeBase.admin.bodyLabel')}
            value={body}
            onChange={(e) => setBody(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditing(null)}>{t('common.cancel')}</Button>
          <Button variant="contained" disabled={saving || !title.trim() || !body.trim()} onClick={save}>{t('knowledgeBase.admin.save')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
