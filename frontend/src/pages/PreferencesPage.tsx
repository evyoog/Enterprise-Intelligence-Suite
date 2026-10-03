import { useRef, type ComponentType, type KeyboardEvent, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router-dom'
import { Box, Container, MenuItem, Paper, Select, Switch, TextField, Typography, type SelectChangeEvent } from '@mui/material'
import { alpha } from '@mui/material/styles'
import { Bell, CalendarClock, Globe, Palette } from 'lucide-react'
import { useAuth } from '../auth/AuthProvider'
import { RenewalRemindersCard } from '../components/preferences/RenewalRemindersCard'
import { NotificationPreferencesSection } from '../components/preferences/NotificationPreferencesSection'
import { AccentPicker } from '../components/preferences/AccentPicker'
import { TimeZonePicker } from '../components/preferences/TimeZonePicker'
import { CONTROL_COLUMN, PreferenceRow, PreferenceSection } from '../components/preferences/PreferenceLayout'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { useInAppShell } from '../components/layout/appShellContext'
import { useThemeMode, type ThemeMode } from '../theming/ThemeModeProvider'
import { useLocalePreference, SUPPORTED_REGIONS } from '../theming/LocalePreferenceProvider'
import { DATE_FORMATS, TIME_FORMATS, type DateFormat, type TimeFormat } from '../theming/formatOptions'
import { SUPPORTED_LANGUAGES } from '../i18n'

/** The language's own name ("English", "español"); the navbar keeps the short code labels. */
function languageName(code: string, fallback: string): string {
  try {
    const name = new Intl.DisplayNames([code], { type: 'language' }).of(code)
    return name ? name.charAt(0).toLocaleUpperCase(code) + name.slice(1) : fallback
  } catch {
    return fallback
  }
}

type SectionKey = 'appearance' | 'formats' | 'notifications' | 'renewals'
interface SectionDef { key: SectionKey; icon: ComponentType<{ size?: number | string }>; signedInOnly?: boolean }

const SECTIONS: SectionDef[] = [
  { key: 'appearance', icon: Palette },
  { key: 'formats', icon: Globe },
  { key: 'notifications', icon: Bell, signedInOnly: true },
  { key: 'renewals', icon: CalendarClock, signedInOnly: true },
]

/**
 * "/account/preferences" — Account → Preferences (C68): a settings
 * navigation on the left and the selected section on the right; on phones
 * the navigation becomes a section selector. The selected section is kept in
 * `?section=` so it survives a refresh and can be linked to.
 *
 * Every control writes straight through the providers the rest of the app
 * already uses (ThemeModeProvider, LocalePreferenceProvider, i18n), so a
 * change is visible everywhere immediately; for a signed-in user
 * PreferenceSync persists theme, motion, language, region and time zone to
 * the account (Phase 6). Accent colour and the date/time/week formats have no
 * account field and are kept in this browser (C67). Notifications and renewal
 * reminders use their existing APIs.
 */
export function PreferencesPage() {
  const { t } = useTranslation()
  const inShell = useInAppShell()
  const { isAuthenticated } = useAuth()
  const [params, setParams] = useSearchParams()
  const tabRefs = useRef<Partial<Record<SectionKey, HTMLButtonElement | null>>>({})

  const sections = SECTIONS.filter((s) => isAuthenticated || !s.signedInOnly)
  const requested = params.get('section') as SectionKey | null
  const active: SectionKey = sections.some((s) => s.key === requested) ? requested! : 'appearance'
  const select = (key: SectionKey) => setParams((p) => { p.set('section', key); return p }, { replace: true })

  const onTabKey = (e: KeyboardEvent, index: number) => {
    const step = e.key === 'ArrowDown' ? 1 : e.key === 'ArrowUp' ? -1 : 0
    const edge = e.key === 'Home' ? 0 : e.key === 'End' ? sections.length - 1 : null
    if (!step && edge === null) return
    e.preventDefault()
    const next = sections[edge ?? (index + step + sections.length) % sections.length].key
    select(next)
    tabRefs.current[next]?.focus()
  }

  const content = (
    <>
      <Box component="header" sx={{ mb: 3 }}>
        <Typography component="h1" variant="h4" sx={{ fontSize: { xs: 26, sm: 30 } }}>{t('preferences.title')}</Typography>
        <Typography sx={{ color: 'text.secondary', mt: 0.5 }}>{t('preferences.subtitle')}</Typography>
      </Box>

      {/* Phones: a compact section selector instead of the navigation. */}
      <TextField select size="small" label={t('preferences.nav.selector')} value={active}
        onChange={(e) => select(e.target.value as SectionKey)}
        sx={{ display: { xs: 'flex', md: 'none' }, mb: 2, maxWidth: 360 }}>
        {sections.map((s) => <MenuItem key={s.key} value={s.key}>{t(`preferences.nav.${s.key}`)}</MenuItem>)}
      </TextField>

      <Box sx={{ display: 'grid', gap: 3, alignItems: 'start', gridTemplateColumns: { xs: '1fr', md: '220px minmax(0, 1fr)', lg: '264px minmax(0, 1fr)' } }}>
        <Paper component="nav" variant="outlined" aria-label={t('preferences.nav.heading')}
          sx={{ display: { xs: 'none', md: 'block' }, p: 1.5, borderRadius: 3, position: 'sticky', top: 88 }}>
          <Typography variant="overline" component="p" sx={{ color: 'text.secondary', px: 1, pb: 0.5 }}>{t('preferences.nav.heading')}</Typography>
          <Box role="tablist" aria-orientation="vertical" aria-label={t('preferences.nav.heading')} sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
            {sections.map((s, index) => {
              const selected = s.key === active
              const Icon = s.icon
              return (
                <Box key={s.key} component="button" type="button" role="tab" id={`pref-tab-${s.key}`}
                  ref={(el: HTMLButtonElement | null) => { tabRefs.current[s.key] = el }}
                  aria-selected={selected} aria-controls="pref-panel" tabIndex={selected ? 0 : -1}
                  onClick={() => select(s.key)} onKeyDown={(e: KeyboardEvent) => onTabKey(e, index)}
                  sx={(theme) => ({
                    display: 'flex', gap: 1.25, alignItems: 'flex-start', width: '100%', textAlign: 'left',
                    px: 1.25, py: 1, border: 0, borderRadius: 2, cursor: 'pointer', font: 'inherit',
                    bgcolor: selected ? alpha(theme.palette.primary.main, theme.palette.mode === 'dark' ? 0.16 : 0.08) : 'transparent',
                    color: selected ? 'primary.main' : 'text.primary',
                    '&:hover': { bgcolor: selected ? undefined : 'action.hover' },
                    '&:focus-visible': { outline: '2px solid', outlineColor: 'primary.main', outlineOffset: -2 },
                  })}>
                  <Box component="span" aria-hidden sx={{ display: 'inline-flex', mt: '2px', color: selected ? 'primary.main' : 'text.secondary' }}>
                    <Icon size={17} />
                  </Box>
                  <Box component="span" sx={{ minWidth: 0 }}>
                    <Typography component="span" variant="body2" sx={{ display: 'block', fontWeight: 600, color: 'inherit' }}>
                      {t(`preferences.nav.${s.key}`)}
                    </Typography>
                    <Typography component="span" variant="caption" sx={{ display: { md: 'none', lg: 'block' }, color: 'text.secondary', lineHeight: 1.4 }}>
                      {t(`preferences.nav.${s.key}Hint`)}
                    </Typography>
                  </Box>
                </Box>
              )
            })}
          </Box>
        </Paper>

        <Paper variant="outlined" id="pref-panel" role="tabpanel" aria-labelledby={`pref-tab-${active}`}
          sx={{ p: { xs: 2.5, sm: 4 }, borderRadius: 3, minWidth: 0, containerType: 'inline-size', containerName: 'prefpanel' }}>
          {active === 'appearance' && <AppearancePanel />}
          {active === 'formats' && <FormatsPanel />}
          {active === 'notifications' && <NotificationPreferencesSection />}
          {active === 'renewals' && <RenewalRemindersCard />}
        </Paper>
      </Box>
    </>
  )

  if (inShell) return <Box sx={{ pb: 4 }}>{content}</Box>
  // Signed out, this is a public page with the site navbar.
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <SiteNavbar />
      <Container component="main" id="main-content" maxWidth="xl" sx={{ pt: '112px', pb: 8 }}>{content}</Container>
    </Box>
  )
}

/** A compact select for the right-hand control column, labelled by its row. */
function CompactSelect({ id, value, onChange, children }: { id: string; value: string; onChange: (v: string) => void; children: ReactNode }) {
  return (
    <Select size="small" value={value} labelId={`${id}-label`} displayEmpty sx={{ width: CONTROL_COLUMN, maxWidth: '100%' }}
      onChange={(e: SelectChangeEvent) => onChange(e.target.value)}>
      {children}
    </Select>
  )
}

function AppearancePanel() {
  const { t } = useTranslation()
  const { mode, setMode, reducedMotion, setReducedMotion, accent, setAccent } = useThemeMode()
  return (
    <PreferenceSection id="appearance" title={t('preferences.appearance')} description={t('preferences.appearanceDescription')}>
      <PreferenceRow label={t('preferences.theme')} hint={t('preferences.themeHint')} labelId="theme-label"
        control={(
          <CompactSelect id="theme" value={mode} onChange={(v) => setMode(v as ThemeMode)}>
            <MenuItem value="light">{t('theme.light')}</MenuItem>
            <MenuItem value="dark">{t('theme.dark')}</MenuItem>
            <MenuItem value="system">{t('theme.system')}</MenuItem>
          </CompactSelect>
        )} />
      <PreferenceRow label={t('preferences.reducedMotion')} hint={t('preferences.reducedMotionHint')} labelId="reduced-motion-label"
        control={<Switch checked={reducedMotion} onChange={(e) => setReducedMotion(e.target.checked)}
          slotProps={{ input: { 'aria-labelledby': 'reduced-motion-label' } }} />} />
      <PreferenceRow label={t('preferences.accent.label')} hint={t('preferences.accent.hint')} labelId="accent-label">
        <AccentPicker value={accent} onChange={setAccent} labelledBy="accent-label" />
      </PreferenceRow>
    </PreferenceSection>
  )
}

function FormatsPanel() {
  const { t, i18n } = useTranslation()
  const {
    timeZone, setTimeZone, region, setRegion, dateFormat, setDateFormat, timeFormat, setTimeFormat,
  } = useLocalePreference()
  return (
    <PreferenceSection id="language-formats" title={t('preferences.languageAndFormats')} description={t('preferences.languageDescription')}>
      <PreferenceRow label={t('preferences.language')} hint={t('preferences.languageHint')} labelId="language-label"
        control={(
          <CompactSelect id="language" value={i18n.language} onChange={(v) => i18n.changeLanguage(v)}>
            {SUPPORTED_LANGUAGES.map((l) => <MenuItem key={l.code} value={l.code}>{languageName(l.code, l.label)}</MenuItem>)}
          </CompactSelect>
        )} />
      <PreferenceRow label={t('preferences.region')} hint={t('preferences.regionHint')} labelId="region-label"
        control={(
          <CompactSelect id="region" value={region ?? ''} onChange={(v) => setRegion(v || null)}>
            <MenuItem value="">{t('preferences.browserDefault')}</MenuItem>
            {SUPPORTED_REGIONS.map((r) => <MenuItem key={r.code} value={r.code}>{r.label}</MenuItem>)}
          </CompactSelect>
        )} />
      <PreferenceRow label={t('preferences.timeZone')} hint={t('preferences.timeZoneHint')} labelId="timezone-label"
        control={<TimeZonePicker value={timeZone} onChange={setTimeZone} labelId="timezone-label" />} />
      <PreferenceRow label={t('preferences.dateFormat')} hint={t('preferences.dateFormatHint')} labelId="date-format-label"
        control={(
          <CompactSelect id="date-format" value={dateFormat} onChange={(v) => setDateFormat(v as DateFormat)}>
            {DATE_FORMATS.map((f) => <MenuItem key={f} value={f}>{f === 'auto' ? t('preferences.regionDefault') : f}</MenuItem>)}
          </CompactSelect>
        )} />
      <PreferenceRow label={t('preferences.timeFormat')} hint={t('preferences.timeFormatHint')} labelId="time-format-label"
        control={(
          <CompactSelect id="time-format" value={timeFormat} onChange={(v) => setTimeFormat(v as TimeFormat)}>
            {TIME_FORMATS.map((f) => <MenuItem key={f} value={f}>{t(`preferences.timeFormatOption.${f}`)}</MenuItem>)}
          </CompactSelect>
        )} />
    </PreferenceSection>
  )
}
