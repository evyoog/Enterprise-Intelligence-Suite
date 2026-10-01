import { BookOpen as PHBookOpen } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Button, CircularProgress, Paper, TextField, Typography } from '@mui/material'
import { knowledgeBaseApi, type KnowledgeArticle } from '../api/knowledgeBaseApi'
import { PageHeader } from '../components/layout/PageHeader'

/** "/knowledge-base" — 11.01.01 Knowledge Articles (sprint 2027.1.1), public
 * (signed in or not, same as the product catalog). 11.01.02 AI Knowledge
 * (semantic search/retrieval) is not built — see the backend's own javadoc
 * on why; this is plain text search. */
export function KnowledgeBasePage() {
  const { t } = useTranslation()
  const [query, setQuery] = useState('')
  const [articles, setArticles] = useState<KnowledgeArticle[] | null>(null)
  const [selected, setSelected] = useState<KnowledgeArticle | null>(null)

  useEffect(() => {
    const handle = setTimeout(() => {
      knowledgeBaseApi.search(query).then(setArticles).catch(() => setArticles([]))
    }, 250)
    return () => clearTimeout(handle)
  }, [query])

  if (selected) {
    return (
      <Box>
        <Button sx={{ mb: 2 }} onClick={() => setSelected(null)}>{t('knowledgeBase.back')}</Button>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 2 }}>{selected.title}</Typography>
        <Typography sx={{ whiteSpace: 'pre-wrap' }}>{selected.body}</Typography>
      </Box>
    )
  }

  return (
    <Box>
      <PageHeader icon={PHBookOpen} accent="cyan" area="help" title={t('knowledgeBase.title')} subtitle={t('knowledgeBase.subtitle')} />
      <TextField
        fullWidth
        sx={{ mb: 3 }}
        placeholder={t('knowledgeBase.searchPlaceholder')}
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      {articles === null && <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress size={28} /></Box>}
      {articles && articles.length === 0 && (
        <Typography sx={{ color: 'text.secondary' }}>{t('knowledgeBase.noResults')}</Typography>
      )}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {articles?.map((article) => (
          <Paper
            key={article.id}
            variant="outlined"
            sx={{ p: 2, cursor: 'pointer' }}
            onClick={() => setSelected(article)}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => { if (e.key === 'Enter') setSelected(article) }}
          >
            <Typography sx={{ fontWeight: 700 }}>{article.title}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary' }} noWrap>{article.body}</Typography>
          </Paper>
        ))}
      </Box>
    </Box>
  )
}
