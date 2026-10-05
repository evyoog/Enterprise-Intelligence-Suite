import { Accordion, AccordionDetails, AccordionSummary, Alert, Box, Button, Paper, Table, TableBody, TableCell, TableRow, Tooltip, Typography } from '@mui/material'
import { ChevronDown, Copy, Download, ExternalLink, FileText } from 'lucide-react'
import { useEffect, useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { knowledgeApi, knowledgeMediaApi, type Block, type GlossaryTerm } from '../../api/knowledgeApi'
import { KnowledgeVideoPlayer } from './KnowledgeVideoPlayer'

/**
 * Renders content blocks (REQ-KNW-002.2) in order. Text is rendered as
 * text (React escapes it) — never as HTML (BR-KCON-008). Glossary terms in
 * paragraphs become links at render time (BR-KCEN-005). Files are fetched
 * through short-lived download URLs only when the reader clicks.
 */
export function KnowledgeBlocksView({ blocks, glossary = [], preview = false }: {
  blocks: Block[]; glossary?: GlossaryTerm[]; preview?: boolean
}) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {blocks.map((block, i) => <BlockView key={block.id ?? i} block={block} glossary={glossary} preview={preview} />)}
    </Box>
  )
}

function BlockView({ block, glossary, preview }: { block: Block; glossary: GlossaryTerm[]; preview: boolean }) {
  const { t } = useTranslation()
  switch (block.type) {
    case 'heading': {
      const level = Math.min(4, Math.max(2, block.level ?? 2))
      return <Typography component={`h${level}` as 'h2'} variant={level === 2 ? 'h5' : 'h6'} sx={{ mt: 1 }}>{block.text}</Typography>
    }
    case 'paragraph':
      return <Typography sx={{ lineHeight: 1.75, whiteSpace: 'pre-wrap' }}>{withGlossary(block.text ?? '', glossary)}</Typography>
    case 'bulleted_list':
    case 'numbered_list': {
      const List = block.type === 'numbered_list' ? 'ol' : 'ul'
      return (
        <Box component={List} sx={{ pl: 3, m: 0, '& li': { mb: 0.5, lineHeight: 1.7 } }}>
          {(block.items ?? []).map((item, i) => <li key={i}>{typeof item === 'string' ? withGlossary(item, glossary) : item.text}</li>)}
        </Box>
      )
    }
    case 'quote':
      return <Box component="blockquote" sx={{ m: 0, pl: 2, borderLeft: 3, borderColor: 'divider', color: 'text.secondary', fontStyle: 'italic' }}>{block.text}</Box>
    case 'callout':
    case 'note':
    case 'warning':
      return (
        <Alert severity={block.type === 'warning' ? 'warning' : block.type === 'note' ? 'info' : 'success'} variant="outlined">
          {block.title && <Typography sx={{ fontWeight: 700 }}>{block.title}</Typography>}
          <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{block.text}</Typography>
        </Alert>
      )
    case 'step':
      return (
        <Paper variant="outlined" sx={{ p: 2 }}>
          <Typography sx={{ fontWeight: 700 }}>{block.title}</Typography>
          <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap', mt: 0.5 }}>{block.text}</Typography>
        </Paper>
      )
    case 'code':
      return <CodeBlock text={block.text ?? ''} language={block.language} />
    case 'table':
      return (
        <Box sx={{ overflowX: 'auto' }}>
          <Table size="small" aria-label={block.caption ?? undefined}>
            <TableBody>
              {(block.rows ?? []).map((row, r) => (
                <TableRow key={r}>{row.map((cell, c) => <TableCell key={c} component={r === 0 ? 'th' : 'td'} sx={r === 0 ? { fontWeight: 700 } : undefined}>{cell}</TableCell>)}</TableRow>
              ))}
            </TableBody>
          </Table>
        </Box>
      )
    case 'accordion':
      return (
        <Box>
          {(block.items ?? []).map((item, i) => typeof item === 'string' ? null : (
            <Accordion key={i} disableGutters variant="outlined" slotProps={{ heading: { component: 'div' } }}>
              <AccordionSummary expandIcon={<ChevronDown size={18} />}><Typography sx={{ fontWeight: 600 }}>{item.title}</Typography></AccordionSummary>
              <AccordionDetails><Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{item.text}</Typography></AccordionDetails>
            </Accordion>
          ))}
        </Box>
      )
    case 'faq':
      return (
        <Accordion disableGutters variant="outlined" slotProps={{ heading: { component: 'div' } }}>
          <AccordionSummary expandIcon={<ChevronDown size={18} />}><Typography sx={{ fontWeight: 600 }}>{block.question}</Typography></AccordionSummary>
          <AccordionDetails><Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{block.answer}</Typography></AccordionDetails>
        </Accordion>
      )
    case 'button':
    case 'link':
      return block.url ? <ActionLink url={block.url} label={block.label ?? block.url} button={block.type === 'button'} /> : null
    case 'image':
    case 'gallery':
      return <MediaImages ids={block.type === 'image' ? (block.mediaId ? [block.mediaId] : []) : (block.mediaIds ?? [])} caption={block.caption} alt={block.alt} preview={preview} />
    case 'file':
    case 'pdf':
    case 'audio':
      return block.mediaId ? <FileBlock mediaId={block.mediaId} label={block.label ?? block.caption ?? t('knowledge.item.download')} audio={block.type === 'audio'} preview={preview} /> : null
    case 'video':
      return block.contentId ? <EmbeddedVideo contentId={block.contentId} /> : null
    case 'workflow_diagram':
      return <WorkflowDiagram title={block.title} steps={block.steps ?? []} />
    case 'embed':
      return block.url ? <ActionLink url={block.url} label={block.title ?? block.url} button={false} /> : null
    default:
      return null
  }
}

/** Glossary terms become links to the glossary (render time only). */
function withGlossary(text: string, glossary: GlossaryTerm[]): ReactNode {
  if (glossary.length === 0 || !text) return text
  const terms = glossary.map((g) => g.term).filter((term) => term.length >= 2)
    .sort((a, b) => b.length - a.length).map((term) => term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))
  if (terms.length === 0) return text
  const pattern = new RegExp(`(?<![\\p{L}\\p{N}])(${terms.join('|')})(?![\\p{L}\\p{N}])`, 'gu')
  const parts: ReactNode[] = []
  let last = 0
  for (const match of text.matchAll(pattern)) {
    const index = match.index ?? 0
    if (index > last) parts.push(text.slice(last, index))
    const term = glossary.find((g) => g.term === match[0])
    parts.push(
      <Tooltip key={index} title={term?.definition ?? ''} describeChild>
        <Box component={RouterLink} to={`/knowledge/glossary#${term?.slug ?? ''}`} sx={{ color: 'primary.main', textDecorationStyle: 'dotted' }}>{match[0]}</Box>
      </Tooltip>,
    )
    last = index + match[0].length
  }
  if (last < text.length) parts.push(text.slice(last))
  return parts
}

function CodeBlock({ text, language }: { text: string; language?: string }) {
  const { t } = useTranslation()
  const [copied, setCopied] = useState(false)
  return (
    <Box sx={{ position: 'relative' }}>
      <Box component="pre" sx={{ m: 0, p: 2, pr: 10, borderRadius: 2, bgcolor: '#0E1726', color: '#E3E7EF', overflowX: 'auto', fontSize: 13, fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace' }}>
        <code data-language={language}>{text}</code>
      </Box>
      <Button size="small" variant="contained" color="inherit" startIcon={<Copy size={14} />} sx={{ position: 'absolute', top: 8, right: 8, color: '#0E1726' }}
        onClick={() => { navigator.clipboard?.writeText(text).then(() => setCopied(true)).catch(() => undefined) }}>
        {copied ? t('knowledge.item.copied') : t('knowledge.item.copy')}
      </Button>
    </Box>
  )
}

function ActionLink({ url, label, button }: { url: string; label: string; button: boolean }) {
  const internal = url.startsWith('/') && !url.startsWith('//')
  const props = internal ? { component: RouterLink, to: url } : { component: 'a', href: url, target: '_blank', rel: 'noopener noreferrer' }
  return (
    <Box>
      <Button {...(props as object)} variant={button ? 'contained' : 'text'} endIcon={internal ? undefined : <ExternalLink size={14} />}>{label}</Button>
    </Box>
  )
}

/** Images are private: each gets a short-lived URL when shown. */
function MediaImages({ ids, caption, alt, preview }: { ids: number[]; caption?: string; alt?: string; preview: boolean }) {
  const { t } = useTranslation()
  const [urls, setUrls] = useState<Record<number, string | null>>({})
  useEffect(() => {
    let alive = true
    ids.forEach((id) => {
      const load = preview ? knowledgeMediaApi.previewUrl(id) : knowledgeApi.downloadUrl(id)
      load.then((u) => { if (alive) setUrls((prev) => ({ ...prev, [id]: u.url })) })
        .catch(() => { if (alive) setUrls((prev) => ({ ...prev, [id]: null })) })
    })
    return () => { alive = false }
  }, [ids, preview])
  return (
    <Box component="figure" sx={{ m: 0 }}>
      <Box sx={{ display: 'grid', gap: 1, gridTemplateColumns: ids.length > 1 ? 'repeat(auto-fill, minmax(200px, 1fr))' : '1fr' }}>
        {ids.map((id) => urls[id] === null
          ? <Typography key={id} variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.item.fileRemoved')}</Typography>
          : urls[id] ? <Box key={id} component="img" src={urls[id] as string} alt={alt ?? caption ?? ''} sx={{ maxWidth: '100%', borderRadius: 2, border: 1, borderColor: 'divider' }} /> : null)}
      </Box>
      {caption && <Typography component="figcaption" variant="caption" sx={{ color: 'text.secondary' }}>{caption}</Typography>}
    </Box>
  )
}

function FileBlock({ mediaId, label, audio, preview }: { mediaId: number; label: string; audio: boolean; preview: boolean }) {
  const { t } = useTranslation()
  const [error, setError] = useState(false)
  const [audioUrl, setAudioUrl] = useState<string | null>(null)
  const open = () => {
    const load = preview ? knowledgeMediaApi.previewUrl(mediaId) : knowledgeApi.downloadUrl(mediaId)
    load.then((u) => {
      if (audio) setAudioUrl(u.url)
      else window.open(u.url, '_blank', 'noopener')
    }).catch(() => setError(true))
  }
  return (
    <Paper variant="outlined" sx={{ p: 1.5, display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
      <FileText size={18} aria-hidden />
      <Typography sx={{ fontWeight: 600, flex: 1, minWidth: 160 }}>{label}</Typography>
      {error ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.item.fileRemoved')}</Typography>
        : audioUrl ? <audio controls src={audioUrl} aria-label={label} />
          : <Button size="small" variant="outlined" startIcon={<Download size={14} />} onClick={open}>{t(audio ? 'knowledge.item.view' : 'knowledge.item.download')}</Button>}
    </Paper>
  )
}

function EmbeddedVideo({ contentId }: { contentId: number }) {
  const [item, setItem] = useState<Awaited<ReturnType<typeof knowledgeApi.get>> | null>(null)
  useEffect(() => { knowledgeApi.get(contentId).then(setItem).catch(() => setItem(null)) }, [contentId])
  if (!item?.video) return null
  return <KnowledgeVideoPlayer contentId={item.id} title={item.title} video={item.video} />
}

/** Interactive workflow: each step opens its guide or EIS page. */
export function WorkflowDiagram({ title, steps }: { title?: string; steps: { title: string; contentId?: number; route?: string }[] }) {
  const { t } = useTranslation()
  return (
    <Box component="nav" aria-label={title}>
      {title && <Typography component="h3" sx={{ fontWeight: 700, mb: 1 }}>{title}</Typography>}
      <Box component="ol" sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexWrap: 'wrap', gap: 1, alignItems: 'center' }}>
        {steps.map((step, i) => {
          const to = step.contentId ? `/knowledge/content/${step.contentId}` : step.route && step.route.startsWith('/') ? step.route : null
          const label = t('knowledge.item.workflowStep', { n: i + 1, title: step.title })
          const chip = (
            <Box sx={{ px: 1.5, py: 1, borderRadius: 2, border: 1, borderColor: to ? 'primary.light' : 'divider', bgcolor: to ? 'action.hover' : 'background.paper', fontWeight: 600, fontSize: 14 }}>
              <Box component="span" sx={{ color: 'text.secondary', mr: 0.75 }}>{i + 1}</Box>{step.title}
            </Box>
          )
          return (
            <Box component="li" key={i} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {to ? <Box component={RouterLink} to={to} aria-label={label} sx={{ textDecoration: 'none', color: 'inherit', borderRadius: 2 }}>{chip}</Box> : chip}
              {i < steps.length - 1 && <Box aria-hidden sx={{ color: 'text.disabled' }}>→</Box>}
            </Box>
          )
        })}
      </Box>
    </Box>
  )
}
