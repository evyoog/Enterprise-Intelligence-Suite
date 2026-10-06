import { Box, Button, List, ListItemButton, ListItemText, Paper, TextField, Typography } from '@mui/material'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { knowledgeApi, knowledgeVideoApi, type PlayInfo, type VideoInfo } from '../../api/knowledgeApi'
import { formatDuration } from './knowledgeUtils'

/**
 * Video player (REQ-KNW-004.6): YouTube embed (privacy-enhanced domain),
 * HTML5 for hosted videos with a short-lived URL from the backend, or the
 * external link. Transcript is searchable; chapters seek where the player
 * supports it. Plays and 25/50/75/100% progress are reported (REQ-KNW-006).
 */
export function KnowledgeVideoPlayer({ contentId, title, video, staffPreview = false }: {
  contentId: number; title: string; video: VideoInfo; staffPreview?: boolean
}) {
  const { t } = useTranslation()
  const [play, setPlay] = useState<PlayInfo | null>(null)
  const [failed, setFailed] = useState(false)
  const [filter, setFilter] = useState('')
  const [start, setStart] = useState(0)
  const ref = useRef<HTMLVideoElement>(null)
  const reported = useRef(new Set<number>())

  useEffect(() => {
    let alive = true
    const load = staffPreview ? knowledgeVideoApi.previewPlay(contentId) : knowledgeApi.playUrl(contentId)
    load.then((p) => { if (alive) setPlay(p) }).catch(() => { if (alive) setFailed(true) })
    return () => { alive = false }
  }, [contentId, staffPreview])

  const report = (type: 'VIDEO_PLAYED' | 'VIDEO_PROGRESS', percent?: number, seconds?: number) => {
    if (staffPreview) return
    knowledgeApi.event({ type, contentId, percent, seconds }).catch(() => undefined)
    if (type === 'VIDEO_PROGRESS' && percent) knowledgeApi.progress(contentId, percent, seconds).catch(() => undefined)
  }

  const onTime = () => {
    const el = ref.current
    if (!el || !el.duration) return
    const percent = (el.currentTime / el.duration) * 100
    for (const mark of [25, 50, 75, 100]) {
      if (percent >= (mark === 100 ? 98 : mark) && !reported.current.has(mark)) {
        reported.current.add(mark)
        report('VIDEO_PROGRESS', mark, Math.round(el.currentTime))
      }
    }
  }

  const seek = (seconds: number) => {
    if (ref.current) {
      ref.current.currentTime = seconds
      ref.current.play().catch(() => undefined)
    } else {
      setStart(seconds)
    }
  }

  const transcriptLines = useMemo(() => (video.transcript ?? '').split('\n').filter((line) => line.trim()), [video.transcript])
  const shown = filter ? transcriptLines.filter((line) => line.toLowerCase().includes(filter.toLowerCase())) : transcriptLines

  let player: React.ReactNode = null
  if (failed) {
    player = <Box sx={{ p: 4, textAlign: 'center', color: '#fff' }}>{t('knowledge.item.videoNotConfigured')}</Box>
  } else if (play?.sourceType === 'YOUTUBE' && play.embedId) {
    player = (
      <Box component="iframe" title={`${t('knowledge.item.watchOnYouTube')}: ${title}`}
        src={`https://www.youtube-nocookie.com/embed/${encodeURIComponent(play.embedId)}?rel=0${start ? `&start=${start}&autoplay=1` : ''}`}
        allow="accelerometer; encrypted-media; gyroscope; picture-in-picture; fullscreen" allowFullScreen
        onLoad={() => { if (!reported.current.has(0)) { reported.current.add(0); report('VIDEO_PLAYED') } }}
        sx={{ border: 0, width: '100%', height: '100%' }} />
    )
  } else if (play?.sourceType === 'AWS_S3' && play.url) {
    player = (
      <video ref={ref} controls preload="metadata" style={{ width: '100%', height: '100%', background: '#000' }} crossOrigin="anonymous"
        aria-label={title} onPlay={() => { if (!reported.current.has(0)) { reported.current.add(0); report('VIDEO_PLAYED') } }} onTimeUpdate={onTime}>
        <source src={play.url} />
        {Object.entries(play.subtitles ?? {}).map(([lang, src]) => <track key={lang} kind="subtitles" srcLang={lang} label={lang.toUpperCase()} src={src} />)}
      </video>
    )
  } else if (play?.sourceType === 'EXTERNAL_URL' && play.url) {
    player = /\.(mp4|webm)(\?|$)/i.test(play.url)
      ? <video ref={ref} controls preload="metadata" src={play.url} style={{ width: '100%', height: '100%', background: '#000' }} aria-label={title}
          onPlay={() => { if (!reported.current.has(0)) { reported.current.add(0); report('VIDEO_PLAYED') } }} onTimeUpdate={onTime} />
      : (
        <Box sx={{ height: '100%', display: 'grid', placeItems: 'center' }}>
          <Button variant="contained" href={play.url} target="_blank" rel="noopener noreferrer"
            onClick={() => report('VIDEO_PLAYED')}>{t('knowledge.item.openVideo')}</Button>
        </Box>
      )
  }

  return (
    <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: video.chapters?.length || transcriptLines.length ? '2fr 1fr' : '1fr' } }}>
      <Box sx={{ aspectRatio: '16 / 9', bgcolor: '#0E1A3A', borderRadius: 3, overflow: 'hidden' }}>{player}</Box>
      {(video.chapters?.length || transcriptLines.length) ? (
        <Paper variant="outlined" sx={{ p: 1.5, display: 'flex', flexDirection: 'column', gap: 1, maxHeight: { lg: 420 }, overflow: 'auto' }}>
          {video.chapters && video.chapters.length > 0 && (
            <Box component="section" aria-label={t('knowledge.item.chapters')}>
              <Typography sx={{ fontWeight: 700, mb: 0.5 }}>{t('knowledge.item.chapters')}</Typography>
              <List dense disablePadding>
                {video.chapters.map((c) => (
                  <ListItemButton key={c.seconds} onClick={() => seek(c.seconds)} sx={{ borderRadius: 1 }}>
                    <ListItemText primary={c.title} secondary={formatDuration(c.seconds)} />
                  </ListItemButton>
                ))}
              </List>
            </Box>
          )}
          {transcriptLines.length > 0 && (
            <Box component="section" aria-label={t('knowledge.item.transcript')}>
              <Typography sx={{ fontWeight: 700, mb: 0.5 }}>{t('knowledge.item.transcript')}</Typography>
              <TextField size="small" fullWidth label={t('knowledge.item.searchTranscript')} value={filter} onChange={(e) => setFilter(e.target.value)} sx={{ mb: 1 }} />
              {shown.map((line, i) => <Typography key={i} variant="body2" sx={{ mb: 0.75 }}>{line}</Typography>)}
            </Box>
          )}
        </Paper>
      ) : null}
    </Box>
  )
}
