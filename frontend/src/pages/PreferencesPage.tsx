import { useAuth } from '../auth/AuthProvider'
import { RenewalRemindersCard } from '../components/preferences/RenewalRemindersCard'
import { NotificationPreferencesSection } from '../components/preferences/NotificationPreferencesSection'
import { AccentPicker } from '../components/preferences/AccentPicker'
import { PreferenceRow, PreferenceSection } from '../components/preferences/PreferenceLayout'
import { useTranslation } from 'react-i18next'
import { Box, Container, Divider, MenuItem, Paper, Select, Switch, Typography, type SelectChangeEvent } from '@mui/material'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { useInAppShell } from '../components/layout/appShellContext'
import { useThemeMode, type ThemeMode } from '../theming/ThemeModeProvider'
import { useLocalePreference, SUPPORTED_REGIONS, SUPPORTED_TIMEZONES } from '../theming/LocalePreferenceProvider'
import { DATE_FORMATS, TIME_FORMATS, WEEK_STARTS, type DateFormat, type TimeFormat, type WeekStart } from '../theming/formatOptions'
import { SUPPORTED_LANGUAGES } from '../i18n'

const SELECT_SX = { width: '100%' }

/**
 * "/preferences" — Account → Preferences (C67 layout). One centred column
 * with four sections: Appearance, Language & Formats, Notifications and
 * Renewal Reminders. Every control writes straight through the providers the
 * rest of the app already uses (ThemeModeProvider, LocalePreferenceProvider,
 * i18n), so a change is visible everywhere immediately; for a signed-in user
 * PreferenceSync persists theme, motion, language, region and time zone to
 * the account (Phase 6). Accent colour and the date/time/week formats have no
 * account field, so they are kept in this browser. Notifications and renewal
 * reminders use their existing APIs.
 */
export function PreferencesPage() {
  const { t, i18n } = useTranslation()
  const { mode, setMode, reducedMotion, setReducedMotion, accent, setAccent } = useThemeMode()
  const {
    timeZone, setTimeZone, region, setRegion, dateFormat, setDateFormat, timeFormat, setTimeFormat, weekStart, setWeekStart,
  } = useLocalePreference()
  // Signed in, the AppShell provides the frame; signed out, this is a public page.
  const inShell = useInAppShell()
  const { isAuthenticated } = useAuth()

  const select = (id: string, value: string, onChange: (value: string) => void, options: { value: string; label: string }[]) => (
    <Select size="small" value={value} onChange={(e: SelectChangeEvent) => onChange(e.target.value)} sx={SELECT_SX}
      labelId={`${id}-label`} displayEmpty>
      {options.map((o) => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
    </Select>
  )

  return (
    <Box sx={inShell ? undefined : { minHeight: '100vh', bgcolor: 'background.default' }}>
      {!inShell && <SiteNavbar />}
      <Container
        component={inShell ? 'div' : 'main'}
        id={inShell ? undefined : 'main-content'}
        maxWidth={false}
        disableGutters={inShell}
        sx={{ maxWidth: 760, mx: 'auto', ...(inShell ? { pb: 4 } : { pt: '112px', pb: 8 }) }}
      >
        <Box component="header" sx={{ mb: 3 }}>
          <Typography component="h1" variant="h4" sx={{ fontSize: { xs: 26, sm: 30 } }}>{t('preferences.title')}</Typography>
          <Typography sx={{ color: 'text.secondary', mt: 0.5 }}>{t('preferences.subtitle')}</Typography>
        </Box>

        <Paper variant="outlined" sx={{ borderRadius: 3 }}>
          <PreferenceSection id="appearance" title={t('preferences.appearance')} description={t('preferences.appearanceDescription')}>
            <PreferenceRow label={t('preferences.theme')} labelId="theme-label"
              control={select('theme', mode, (v) => setMode(v as ThemeMode), [
                { value: 'light', label: t('theme.light') }, { value: 'dark', label: t('theme.dark') }, { value: 'system', label: t('theme.system') },
              ])} />
            <PreferenceRow label={t('preferences.reducedMotion')} hint={t('preferences.reducedMotionHint')} labelId="reduced-motion-label"
              control={<Switch checked={reducedMotion} onChange={(e) => setReducedMotion(e.target.checked)}
                slotProps={{ input: { 'aria-labelledby': 'reduced-motion-label' } }} />} />
            <PreferenceRow label={t('preferences.accent.label')} hint={t('preferences.accent.hint')} labelId="accent-label">
              <AccentPicker value={accent} onChange={setAccent} labelledBy="accent-label" />
            </PreferenceRow>
          </PreferenceSection>

          <Divider />

          <PreferenceSection id="language-formats" title={t('preferences.languageAndFormats')} description={t('preferences.languageDescription')}>
            <PreferenceRow label={t('preferences.language')} labelId="language-label"
              control={select('language', i18n.language, (v) => i18n.changeLanguage(v),
                SUPPORTED_LANGUAGES.map((l) => ({ value: l.code, label: l.label })))} />
            <PreferenceRow label={t('preferences.region')} hint={t('preferences.regionHint')} labelId="region-label"
              control={select('region', region ?? '', (v) => setRegion(v || null),
                [{ value: '', label: t('preferences.browserDefault') }, ...SUPPORTED_REGIONS.map((r) => ({ value: r.code, label: r.label }))])} />
            <PreferenceRow label={t('preferences.timeZone')} labelId="timezone-label"
              control={select('timezone', timeZone, setTimeZone, SUPPORTED_TIMEZONES.map((z) => ({ value: z.code, label: z.label })))} />
            <PreferenceRow label={t('preferences.dateFormat')} labelId="date-format-label"
              control={select('date-format', dateFormat, (v) => setDateFormat(v as DateFormat),
                DATE_FORMATS.map((f) => ({ value: f, label: f === 'auto' ? t('preferences.regionDefault') : f })))} />
            <PreferenceRow label={t('preferences.timeFormat')} labelId="time-format-label"
              control={select('time-format', timeFormat, (v) => setTimeFormat(v as TimeFormat),
                TIME_FORMATS.map((f) => ({ value: f, label: t(`preferences.timeFormatOption.${f}`) })))} />
            <PreferenceRow label={t('preferences.weekStart')} labelId="week-start-label"
              control={select('week-start', weekStart, (v) => setWeekStart(v as WeekStart),
                WEEK_STARTS.map((f) => ({ value: f, label: t(`preferences.weekStartOption.${f}`) })))} />
          </PreferenceSection>

          {isAuthenticated && (
            <>
              <Divider />
              <NotificationPreferencesSection />
              <Divider />
              {/* REQ-SUB-004.8 (C64): renewal reminders, for signed-in users. */}
              <RenewalRemindersCard />
            </>
          )}
        </Paper>
      </Container>
    </Box>
  )
}
