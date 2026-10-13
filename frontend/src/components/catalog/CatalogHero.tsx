import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Typography } from '@mui/material'
import { CreditCard, ShieldCheck, Zap } from 'lucide-react'

/** C94 (REQ-CAT-003.11): the catalog's top banner: label, title, subtitle, the three trust points and the page actions. */
export function CatalogHero({ actions }: { actions?: ReactNode }) {
  const { t } = useTranslation()
  const trust = [
    { icon: ShieldCheck, title: t('catalog.hero.trust1Title'), text: t('catalog.hero.trust1Text') },
    { icon: CreditCard, title: t('catalog.hero.trust2Title'), text: t('catalog.hero.trust2Text') },
    { icon: Zap, title: t('catalog.hero.trust3Title'), text: t('catalog.hero.trust3Text') },
  ]
  return (
    <Box component="section" aria-labelledby="catalog-title" sx={{
      position: 'relative', overflow: 'hidden', borderRadius: 4, mb: 3, px: { xs: 2.5, md: 5 }, py: { xs: 3, md: 4.5 },
      border: '1px solid', borderColor: 'divider',
      background: (theme) => theme.palette.mode === 'dark'
        ? 'linear-gradient(120deg, rgba(99,102,241,.18) 0%, rgba(139,92,246,.10) 55%, rgba(15,23,42,0) 100%)'
        : 'linear-gradient(120deg, #EEF2FF 0%, #F5F3FF 55%, #FFFFFF 100%)',
    }}>
      <Box sx={{ position: 'relative', zIndex: 1, maxWidth: 640 }}>
        <Typography variant="overline" sx={{ color: 'primary.main', fontWeight: 700, letterSpacing: 1.2 }}>{t('catalog.title')}</Typography>
        <Typography id="catalog-title" component="h1" sx={{ fontWeight: 800, fontSize: { xs: 28, md: 38 }, lineHeight: 1.15, mt: 0.5 }}>
          {t('catalog.hero.title')}
        </Typography>
        <Typography sx={{ color: 'text.secondary', mt: 1.5, maxWidth: 560 }}>{t('catalog.hero.subtitle')}</Typography>
        <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, mt: 2.5, display: 'flex', flexWrap: 'wrap', gap: { xs: 1.5, md: 3 } }}>
          {trust.map(({ icon: Icon, title, text }) => (
            <Box component="li" key={title} sx={{ display: 'flex', alignItems: 'center', gap: 1.25 }}>
              <Box sx={{ width: 36, height: 36, borderRadius: 2, display: 'grid', placeItems: 'center', bgcolor: 'background.paper', color: 'primary.main', border: '1px solid', borderColor: 'divider' }}>
                <Icon size={18} aria-hidden />
              </Box>
              <Box>
                <Typography sx={{ fontWeight: 700, fontSize: 13, lineHeight: 1.2 }}>{title}</Typography>
                <Typography sx={{ color: 'text.secondary', fontSize: 12 }}>{text}</Typography>
              </Box>
            </Box>
          ))}
        </Box>
        {actions && <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 3 }}>{actions}</Box>}
      </Box>
      {/* A plain decorative graphic: stacked product windows. */}
      <Box aria-hidden sx={{ display: { xs: 'none', md: 'block' }, position: 'absolute', right: 40, top: '50%', transform: 'translateY(-50%)', width: 300, height: 190 }}>
        <svg viewBox="0 0 300 190" width="300" height="190" fill="none">
          <rect x="70" y="10" width="220" height="140" rx="14" fill="#6366F1" opacity=".18" />
          <rect x="40" y="30" width="220" height="140" rx="14" fill="#fff" stroke="#C7D2FE" />
          <rect x="56" y="48" width="70" height="10" rx="5" fill="#6366F1" opacity=".5" />
          <rect x="56" y="70" width="188" height="8" rx="4" fill="#E0E7FF" />
          <rect x="56" y="86" width="150" height="8" rx="4" fill="#E0E7FF" />
          <rect x="56" y="108" width="52" height="40" rx="8" fill="#8B5CF6" opacity=".35" />
          <rect x="116" y="108" width="52" height="40" rx="8" fill="#6366F1" opacity=".5" />
          <rect x="176" y="108" width="52" height="40" rx="8" fill="#3B82F6" opacity=".35" />
          <circle cx="262" cy="34" r="22" fill="#6366F1" />
          <path d="M254 34l6 6 11-12" stroke="#fff" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </Box>
    </Box>
  )
}
