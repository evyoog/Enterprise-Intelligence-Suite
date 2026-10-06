import { Box, Button, IconButton, MenuItem, Paper, TextField, Tooltip, Typography } from '@mui/material'
import { ArrowDown, ArrowUp, GripVertical, Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { Block } from '../../api/knowledgeApi'
import { BLOCK_TYPES } from './knowledgeUtils'

const lines = (v: string) => v.split('\n').map((x) => x.trim()).filter(Boolean)

/**
 * Rich block editor (prompt 6.3): add, edit, remove and reorder blocks by
 * drag and drop, with Move up / Move down buttons as the keyboard
 * alternative. Blocks hold structured text only — no HTML (BR-KCON-008).
 */
export function BlockEditor({ blocks, onChange, disabled }: { blocks: Block[]; onChange: (b: Block[]) => void; disabled?: boolean }) {
  const { t } = useTranslation()
  const [newType, setNewType] = useState<string>('paragraph')
  const [dragIndex, setDragIndex] = useState<number | null>(null)
  const update = (i: number, patch: Partial<Block>) => onChange(blocks.map((b, j) => (j === i ? { ...b, ...patch } : b)))
  const move = (from: number, to: number) => {
    if (to < 0 || to >= blocks.length) return
    const next = [...blocks]
    const [item] = next.splice(from, 1)
    next.splice(to, 0, item)
    onChange(next)
  }
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.blocks.dragHint')}</Typography>
      {blocks.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.admin.blocks.empty')}</Typography>}
      {blocks.map((block, i) => (
        <Paper key={i} variant="outlined" draggable={!disabled}
          onDragStart={() => setDragIndex(i)} onDragOver={(e) => e.preventDefault()}
          onDrop={() => { if (dragIndex !== null && dragIndex !== i) move(dragIndex, i); setDragIndex(null) }}
          sx={{ p: 1.5, display: 'flex', gap: 1, alignItems: 'flex-start', opacity: dragIndex === i ? 0.5 : 1, borderStyle: dragIndex === i ? 'dashed' : 'solid' }}>
          <Box aria-hidden sx={{ color: 'text.disabled', cursor: disabled ? 'default' : 'grab', pt: 1 }}><GripVertical size={16} /></Box>
          <Box sx={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="overline">{t(`knowledge.admin.blocks.type.${block.type}`)}</Typography>
            <BlockFields block={block} onChange={(patch) => update(i, patch)} disabled={disabled} />
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            <Tooltip title={t('knowledge.admin.blocks.moveUp')}><span><IconButton size="small" aria-label={t('knowledge.admin.blocks.moveUp')} disabled={disabled || i === 0} onClick={() => move(i, i - 1)}><ArrowUp size={16} /></IconButton></span></Tooltip>
            <Tooltip title={t('knowledge.admin.blocks.moveDown')}><span><IconButton size="small" aria-label={t('knowledge.admin.blocks.moveDown')} disabled={disabled || i === blocks.length - 1} onClick={() => move(i, i + 1)}><ArrowDown size={16} /></IconButton></span></Tooltip>
            <Tooltip title={t('knowledge.admin.blocks.remove')}><span><IconButton size="small" aria-label={t('knowledge.admin.blocks.remove')} disabled={disabled} onClick={() => onChange(blocks.filter((_, j) => j !== i))}><Trash2 size={16} /></IconButton></span></Tooltip>
          </Box>
        </Paper>
      ))}
      {!disabled && (
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
          <TextField select size="small" label={t('knowledge.admin.blocks.addLabel')} value={newType} onChange={(e) => setNewType(e.target.value)} sx={{ minWidth: 220 }}>
            {BLOCK_TYPES.map((type) => <MenuItem key={type} value={type}>{t(`knowledge.admin.blocks.type.${type}`)}</MenuItem>)}
          </TextField>
          <Button variant="outlined" startIcon={<Plus size={16} />} onClick={() => onChange([...blocks, { type: newType }])}>{t('knowledge.admin.blocks.add')}</Button>
        </Box>
      )}
    </Box>
  )
}

function BlockFields({ block, onChange, disabled }: { block: Block; onChange: (patch: Partial<Block>) => void; disabled?: boolean }) {
  const { t } = useTranslation()
  const field = (name: keyof Block, label: string, multiline = false) => (
    <TextField size="small" fullWidth disabled={disabled} label={t(`knowledge.admin.blocks.${label}`)} multiline={multiline} minRows={multiline ? 3 : undefined}
      value={(block[name] as string | undefined) ?? ''} onChange={(e) => onChange({ [name]: e.target.value } as Partial<Block>)} />
  )
  const num = (name: 'mediaId' | 'contentId', label: string) => (
    <TextField size="small" type="number" disabled={disabled} label={t(`knowledge.admin.blocks.${label}`)} value={block[name] ?? ''}
      onChange={(e) => onChange({ [name]: e.target.value ? Number(e.target.value) : undefined } as Partial<Block>)} sx={{ maxWidth: 260 }} />
  )
  switch (block.type) {
    case 'heading':
      return (
        <Box sx={{ display: 'flex', gap: 1 }}>
          <TextField select size="small" disabled={disabled} label={t('knowledge.admin.blocks.level')} value={block.level ?? 2} onChange={(e) => onChange({ level: Number(e.target.value) })} sx={{ width: 140 }}>
            {[2, 3, 4].map((l) => <MenuItem key={l} value={l}>H{l}</MenuItem>)}
          </TextField>
          {field('text', 'text')}
        </Box>
      )
    case 'paragraph': case 'quote': return field('text', 'text', true)
    case 'callout': case 'warning': case 'note': case 'step': return <>{field('title', 'title')}{field('text', 'text', true)}</>
    case 'code': return <>{field('language', 'language')}{field('text', 'text', true)}</>
    case 'bulleted_list': case 'numbered_list':
      return <TextField size="small" fullWidth multiline minRows={3} disabled={disabled} label={t('knowledge.admin.blocks.items')}
        value={(block.items ?? []).map((x) => (typeof x === 'string' ? x : x.text)).join('\n')} onChange={(e) => onChange({ items: lines(e.target.value) })} />
    case 'accordion':
      return <TextField size="small" fullWidth multiline minRows={3} disabled={disabled} label={`${t('knowledge.admin.blocks.items')} — ${t('knowledge.admin.blocks.title')} | ${t('knowledge.admin.blocks.text')}`}
        value={(block.items ?? []).map((x) => (typeof x === 'string' ? x : `${x.title} | ${x.text}`)).join('\n')}
        onChange={(e) => onChange({ items: lines(e.target.value).map((l) => { const [title, ...rest] = l.split('|'); return { title: title.trim(), text: rest.join('|').trim() } }) })} />
    case 'faq': return <>{field('question', 'question')}{field('answer', 'answer', true)}</>
    case 'table':
      return <TextField size="small" fullWidth multiline minRows={3} disabled={disabled} label={t('knowledge.admin.blocks.rows')}
        value={(block.rows ?? []).map((r) => r.join(' | ')).join('\n')} onChange={(e) => onChange({ rows: lines(e.target.value).map((l) => l.split('|').map((c) => c.trim())) })} />
    case 'image': return <>{num('mediaId', 'mediaId')}{field('alt', 'alt')}{field('caption', 'caption')}</>
    case 'gallery':
      return <TextField size="small" fullWidth disabled={disabled} label={t('knowledge.admin.blocks.mediaId')}
        value={(block.mediaIds ?? []).join(', ')} onChange={(e) => onChange({ mediaIds: e.target.value.split(',').map((x) => Number(x.trim())).filter((x) => x > 0) })} />
    case 'audio': case 'file': case 'pdf': return <>{num('mediaId', 'mediaId')}{field('label', 'label')}</>
    case 'video': return <>{num('contentId', 'contentId')}{field('caption', 'caption')}</>
    case 'button': case 'link': return <>{field('label', 'label')}{field('url', 'url')}</>
    case 'embed': return <>{field('title', 'title')}{field('url', 'url')}</>
    case 'workflow_diagram':
      return (
        <>
          {field('title', 'title')}
          <TextField size="small" fullWidth multiline minRows={3} disabled={disabled} label={t('knowledge.admin.blocks.steps')}
            value={(block.steps ?? []).map((s) => [s.title, s.contentId ?? '', s.route ?? ''].join(' | ').replace(/( \| )+$/, '')).join('\n')}
            onChange={(e) => onChange({ steps: lines(e.target.value).map((l) => {
              const [title, id, route] = l.split('|').map((x) => x.trim())
              return { title, ...(id && Number(id) > 0 ? { contentId: Number(id) } : {}), ...(route ? { route } : {}) }
            }) })} />
        </>
      )
    default: return null
  }
}
