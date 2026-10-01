import { SlidersHorizontal as PHSlidersHorizontal } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import {
  Box, Container, FormControl, FormControlLabel, FormLabel, MenuItem, Paper,
  Radio, RadioGroup, Select, Switch, Typography, type SelectChangeEvent,
} from '@mui/material'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { useInAppShell } from '../components/layout/appShellContext'
import { PageHeader } from '../components/layout/PageHeader'
import { useThemeMode, type ThemeMode } from '../theming/ThemeModeProvider'
import { useLocalePreference, SUPPORTED_REGIONS, SUPPORTED_TIMEZONES } from '../theming/LocalePreferenceProvider'
import { SUPPORTED_LANGUAGES } from '../i18n'

/**
 * Phase 6 (2026.3.3): a single, real settings surface for every portal
 * personalization option that previously only lived in the navbar's small
 * dropdown (language, time zone) or didn't exist as a dedicated page at all
 * (theme, region, reduced motion) — every control here reads from and writes
 * straight through the same providers the rest of the app already uses
 * (ThemeModeProvider/LocalePreferenceProvider/i18n), so a change here is
 * visible everywhere immediately, and — for a signed-in customer —
 * PreferenceSync persists it to their own account (see that component's own
 * doc) rather than only this browser's localStorage.
 */
export function PreferencesPage() {
  const { t, i18n } = useTranslation()
  const { mode, setMode, reducedMotion, setReducedMotion } = useThemeMode()
  const { timeZone, setTimeZone, region, setRegion } = useLocalePreference()
  // Signed in, the AppShell provides the frame; signed out, this is a public page.
  const inShell = useInAppShell()

  return (
    <Box sx={inShell ? undefined : { minHeight: '100vh', bgcolor: 'background.default' }}>
      {!inShell && <SiteNavbar />}
      <Container
        component={inShell ? 'div' : 'main'}
        id={inShell ? undefined : 'main-content'}
        maxWidth="sm"
        disableGutters={inShell}
        sx={inShell ? { pb: 4 } : { pt: '112px', pb: 8 }}
      >
        <PageHeader icon={PHSlidersHorizontal} accent="teal" area="account" title={t('preferences.title')} subtitle={t('preferences.subtitle')} />

        <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 2 }}>
            {t('preferences.appearance')}
          </Typography>

          <FormControl sx={{ mb: 3 }}>
            <FormLabel id="theme-mode-label">{t('preferences.theme')}</FormLabel>
            <RadioGroup
              aria-labelledby="theme-mode-label"
              row
              value={mode}
              onChange={(e) => setMode(e.target.value as ThemeMode)}
            >
              <FormControlLabel value="light" control={<Radio />} label={t('theme.light')} />
              <FormControlLabel value="dark" control={<Radio />} label={t('theme.dark')} />
              <FormControlLabel value="system" control={<Radio />} label={t('theme.system')} />
            </RadioGroup>
          </FormControl>

          <FormControlLabel
            control={<Switch checked={reducedMotion} onChange={(e) => setReducedMotion(e.target.checked)} />}
            label={t('preferences.reducedMotion')}
          />
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
            {t('preferences.reducedMotionHint')}
          </Typography>
        </Paper>

        <Paper variant="outlined" sx={{ p: 3 }}>
          <Typography variant="h6" component="h2" sx={{ fontWeight: 700, mb: 2 }}>
            {t('preferences.languageAndFormats')}
          </Typography>

          <FormControl fullWidth sx={{ mb: 2.5 }}>
            <FormLabel id="language-label" sx={{ mb: 0.5 }}>{t('preferences.language')}</FormLabel>
            <Select
              labelId="language-label"
              value={i18n.language}
              onChange={(e: SelectChangeEvent) => i18n.changeLanguage(e.target.value)}
              size="small"
            >
              {SUPPORTED_LANGUAGES.map((lang) => (
                <MenuItem key={lang.code} value={lang.code}>{lang.label}</MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl fullWidth sx={{ mb: 2.5 }}>
            <FormLabel id="region-label" sx={{ mb: 0.5 }}>{t('preferences.region')}</FormLabel>
            <Select
              labelId="region-label"
              value={region ?? ''}
              displayEmpty
              onChange={(e: SelectChangeEvent) => setRegion(e.target.value || null)}
              size="small"
            >
              <MenuItem value="">{t('preferences.browserDefault')}</MenuItem>
              {SUPPORTED_REGIONS.map((r) => (
                <MenuItem key={r.code} value={r.code}>{r.label}</MenuItem>
              ))}
            </Select>
            <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
              {t('preferences.regionHint')}
            </Typography>
          </FormControl>

          <FormControl fullWidth>
            <FormLabel id="timezone-label" sx={{ mb: 0.5 }}>{t('preferences.timeZone')}</FormLabel>
            <Select
              labelId="timezone-label"
              value={timeZone}
              onChange={(e: SelectChangeEvent) => setTimeZone(e.target.value)}
              size="small"
            >
              {SUPPORTED_TIMEZONES.map((zone) => (
                <MenuItem key={zone.code} value={zone.code}>{zone.label}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Paper>
      </Container>
    </Box>
  )
}
