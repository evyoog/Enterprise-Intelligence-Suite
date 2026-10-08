import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Dialog, DialogContent, IconButton, Link as MuiLink, Paper, Typography } from '@mui/material'
import { ExternalLink, FileDown, Play, X } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { productContentApi, type PublicContent, type PublicItem } from '../../api/productContentApi'
import { formatDay, formatSize } from './productContentUtils'

/**
 * The Resources tab of the product page (REQ-CAT-004.9): the published
 * datasheet, documentation links, image gallery, videos and case studies.
 * Files arrive through short-lived signed links: images are shown with the
 * link in the response, downloads ask for a new 5-minute link on click.
 */
export function ProductResources({ productId, content }: { productId: number; content: PublicContent }) {
  const { t, i18n } = useTranslation()
  const [downloadError, setDownloadError] = useState(false)
  const [zoomed, setZoomed] = useState<PublicItem | null>(null)

  const download = async (item: PublicItem) => {
    setDownloadError(false)
    try {
      const link = await productContentApi.download(productId, item.id)
      const a = document.createElement('a')
      a.href = link.url
      a.rel = 'noopener'
      a.click()
    } catch {
      setDownloadError(true)
    }
  }

  const section = (key: string, title: string, items: PublicItem[], render: (i: PublicItem) => React.ReactNode, grid = false) =>
    items.length === 0 ? null : (
      <Box component="section" aria-labelledby={`res-${key}`}>
        <Typography id={`res-${key}`} component="h3" sx={{ fontWeight: 700, fontSize: 18, mb: 1.5 }}>{title}</Typography>
        <Box component="ul" sx={{
          listStyle: 'none', p: 0, m: 0, display: grid ? 'grid' : 'flex', flexDirection: 'column', gap: 1.5,
          gridTemplateColumns: grid ? { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' } : undefined,
        }}>
          {items.map((i) => <Box component="li" key={i.id}>{render(i)}</Box>)}
        </Box>
      </Box>
    )

  const versionLine = (i: PublicItem) => t('productContent.public.versionUpdated', { n: i.version, date: formatDay(i.updatedAt, i18n.language) })

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, pb: 2 }}>
      {downloadError && <Alert severity="error" role="alert" onClose={() => setDownloadError(false)}>{t('productContent.public.downloadFailed')}</Alert>}

      {section('datasheets', t('productContent.public.datasheets'), content.datasheets, (i) => (
        <Paper variant="outlined" sx={{ p: 2, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
          <Box sx={{ flex: 1, minWidth: 200 }}>
            <Typography sx={{ fontWeight: 600 }}>{i.title}</Typography>
            {i.description && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{i.description}</Typography>}
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>{versionLine(i)}</Typography>
          </Box>
          {i.file && (
            <Button variant="contained" startIcon={<FileDown size={16} />} onClick={() => download(i)}
              aria-label={t('productContent.public.downloadItem', { title: i.title, size: `PDF, ${formatSize(i.file.size)}` })}>
              {t('productContent.public.download')}
            </Button>
          )}
        </Paper>
      ))}

      {section('documentation', t('productContent.public.documentation'), content.documentation, (i) => (
        <Paper variant="outlined" sx={{ p: 2, borderRadius: 3 }}>
          <MuiLink component={RouterLink} to={`/knowledge/content/${i.articleRef}`} underline="hover" sx={{ fontWeight: 600 }}>{i.title}</MuiLink>
          {i.description && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{i.description}</Typography>}
        </Paper>
      ))}

      {section('gallery', t('productContent.public.gallery'), content.images, (i) => (
        <Box component="figure" sx={{ m: 0 }}>
          <Box component="button" type="button" onClick={() => setZoomed(i)} aria-label={t('productContent.public.zoomItem', { title: i.title })}
            sx={{ p: 0, border: '1px solid', borderColor: 'divider', borderRadius: 2, overflow: 'hidden', cursor: 'zoom-in', bgcolor: 'background.paper', width: '100%', display: 'block' }}>
            {i.imageUrl && <Box component="img" src={i.imageUrl} alt={i.altText ?? i.title} loading="lazy" sx={{ width: '100%', aspectRatio: '16 / 10', objectFit: 'cover', display: 'block' }} />}
          </Box>
          <Typography component="figcaption" variant="body2" sx={{ mt: 0.75, color: 'text.secondary' }}>{i.title}</Typography>
        </Box>
      ), true)}

      {section('videos', t('productContent.public.videos'), content.videos, (i) => <VideoCard item={i} />, true)}

      {section('cases', t('productContent.public.caseStudies'), content.caseStudies, (i) => (
        <Paper variant="outlined" sx={{ p: 2.5, borderRadius: 3, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            {i.logoUrl && <Box component="img" src={i.logoUrl} alt="" sx={{ height: 40, maxWidth: 120, objectFit: 'contain' }} />}
            <Box>
              <Typography sx={{ fontWeight: 700 }}>{i.title}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{i.customerName}</Typography>
            </Box>
          </Box>
          {i.problem && <Box><Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('productContent.public.problem')}</Typography><Typography>{i.problem}</Typography></Box>}
          {i.result && <Box><Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('productContent.public.result')}</Typography><Typography>{i.result}</Typography></Box>}
          {i.file && (
            <Box><Button size="small" startIcon={<FileDown size={15} />} onClick={() => download(i)}>{t('productContent.public.caseStudyPdf')}</Button></Box>
          )}
        </Paper>
      ))}

      <Dialog open={zoomed !== null} onClose={() => setZoomed(null)} maxWidth="lg" aria-label={zoomed?.title}>
        <DialogContent sx={{ p: 1, position: 'relative' }}>
          <IconButton onClick={() => setZoomed(null)} aria-label={t('productContent.public.close')} sx={{ position: 'absolute', top: 8, right: 8, bgcolor: 'background.paper' }}><X size={18} /></IconButton>
          {zoomed?.imageUrl && <Box component="img" src={zoomed.imageUrl} alt={zoomed.altText ?? zoomed.title} sx={{ maxWidth: '100%', maxHeight: '80vh', display: 'block' }} />}
        </DialogContent>
      </Dialog>
    </Box>
  )
}

/**
 * A video link. YouTube and Vimeo load their privacy-friendly player only
 * after the visitor presses play (nothing is sent to them before); any other
 * link opens in a new tab (BR-PCON-007).
 */
function VideoCard({ item }: { item: PublicItem }) {
  const { t } = useTranslation()
  const [playing, setPlaying] = useState(false)
  if (!item.embedUrl) {
    return (
      <Paper variant="outlined" sx={{ p: 2, borderRadius: 3, display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Typography sx={{ fontWeight: 600 }}>{item.title}</Typography>
        {item.description && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{item.description}</Typography>}
        <Box><Button size="small" component="a" href={item.videoUrl ?? undefined} target="_blank" rel="noopener noreferrer" startIcon={<ExternalLink size={15} />}>
          {t('productContent.public.openVideo')}
        </Button></Box>
      </Paper>
    )
  }
  return (
    <Box>
      <Box sx={{ position: 'relative', aspectRatio: '16 / 9', borderRadius: 2, overflow: 'hidden', bgcolor: 'action.hover', border: '1px solid', borderColor: 'divider' }}>
        {playing ? (
          <Box component="iframe" src={item.embedUrl} title={t('productContent.public.playerTitle', { title: item.title })} allow="encrypted-media; picture-in-picture; fullscreen"
            referrerPolicy="strict-origin-when-cross-origin" allowFullScreen sx={{ position: 'absolute', inset: 0, width: '100%', height: '100%', border: 0 }} />
        ) : (
          <Box component="button" type="button" onClick={() => setPlaying(true)} aria-label={t('productContent.public.watchItem', { title: item.title })}
            sx={{ position: 'absolute', inset: 0, width: '100%', height: '100%', border: 0, p: 0, cursor: 'pointer', bgcolor: 'transparent', display: 'flex', alignItems: 'center', justifyContent: 'center',
              backgroundImage: item.thumbnailUrl ? `url(${item.thumbnailUrl})` : undefined, backgroundSize: 'cover', backgroundPosition: 'center' }}>
            <Box sx={{ width: 56, height: 56, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Play size={24} aria-hidden />
            </Box>
          </Box>
        )}
      </Box>
      <Typography sx={{ fontWeight: 600, mt: 0.75 }}>{item.title}</Typography>
      {item.description && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{item.description}</Typography>}
    </Box>
  )
}
