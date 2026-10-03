import { Box, Button, Paper, Typography } from '@mui/material'
import { ArrowRight, Gauge, Lock, ShieldCheck } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'

/** The existing support flow: "My tickets" (create and follow tickets,
 * REQ-SUP-001). Signed-out visitors are asked to sign in by that route. */
const SUPPORT_ROUTE = '/support/tickets'

/** C66: "Get started" call to action that leads to the real support flow. */
export function SupportCTA({ title, description }: { title?: string; description?: string }) {
  const { t } = useTranslation()
  return (
    <Paper variant="outlined" component="section" aria-labelledby="support-cta-title" sx={{
      p: { xs: 2.5, sm: 3.5 }, borderRadius: 3, display: 'flex', alignItems: { xs: 'flex-start', md: 'center' },
      justifyContent: 'space-between', gap: 3, flexDirection: { xs: 'column', md: 'row' },
      background: (theme) => theme.palette.mode === 'dark'
        ? 'linear-gradient(120deg, rgba(99,102,241,0.14), rgba(99,102,241,0.04))'
        : 'linear-gradient(120deg, #EEF2FF, #F8FAFF)',
    }}>
      <Box>
        <Typography id="support-cta-title" component="h2" sx={{ fontWeight: 700, fontSize: 18 }}>{title ?? t('ui.supportCta.title')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5, maxWidth: 560 }}>{description ?? t('ui.supportCta.body')}</Typography>
        <Box sx={{ display: 'flex', gap: 2.5, mt: 1.5, flexWrap: 'wrap', color: 'text.secondary' }}>
          {[{ icon: Lock, key: 'secure' }, { icon: Gauge, key: 'scalable' }, { icon: ShieldCheck, key: 'reliable' }].map(({ icon: Icon, key }) => (
            <Box key={key} sx={{ display: 'flex', alignItems: 'center', gap: 0.75, fontSize: 13, fontWeight: 500 }}>
              <Icon size={15} aria-hidden />{t(`ui.supportCta.${key}`)}
            </Box>
          ))}
        </Box>
      </Box>
      <Button component={RouterLink} to={SUPPORT_ROUTE} variant="contained" endIcon={<ArrowRight size={16} />} sx={{ flexShrink: 0 }}>
        {t('ui.supportCta.action')}
      </Button>
    </Paper>
  )
}
