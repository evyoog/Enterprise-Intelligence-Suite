import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { Alert, Box, Link, Tab, Tabs, TextField, Typography } from '@mui/material'
import type { ProfileAccount, ProfileSection } from '../../api/profileApi'
import {
  MAX_LENGTH, PROFILE_TABS, REQUIRED_FIELDS, profileFieldId, sectionsWithErrors, type ProfileForm as ProfileFormValues, type ProfileTab,
} from '../../utils/profileValidation'

interface FieldDef {
  field: string
  autoComplete?: string
  inputMode?: 'tel' | 'url' | 'text'
  type?: string
  multiline?: boolean
  /** Spans both columns. */
  wide?: boolean
  /** Shows `profile.hints.<field>` under the field when it has no error. */
  hint?: boolean
  counter?: boolean
}

const LAYOUT: Record<ProfileSection, FieldDef[]> = {
  personal: [
    { field: 'firstName', autoComplete: 'given-name' },
    { field: 'lastName', autoComplete: 'family-name' },
    { field: 'preferredName', autoComplete: 'nickname', hint: true },
    { field: 'mobile', autoComplete: 'tel', inputMode: 'tel', type: 'tel', hint: true },
    { field: 'alternatePhone', autoComplete: 'off', inputMode: 'tel', type: 'tel', hint: true },
    { field: 'bio', multiline: true, wide: true, counter: true },
  ],
  work: [
    { field: 'jobTitle', autoComplete: 'organization-title' },
    { field: 'department', autoComplete: 'off' },
    { field: 'companyName', autoComplete: 'organization' },
    { field: 'industry', autoComplete: 'off' },
  ],
  address: [
    { field: 'addressLine1', autoComplete: 'address-line1', wide: true },
    { field: 'addressLine2', autoComplete: 'address-line2', wide: true },
    { field: 'city', autoComplete: 'address-level2' },
    { field: 'state', autoComplete: 'address-level1' },
    { field: 'postalCode', autoComplete: 'postal-code', hint: true },
    { field: 'country', autoComplete: 'country-name' },
  ],
  online: [
    { field: 'linkedinUrl', autoComplete: 'off', inputMode: 'url', type: 'url', wide: true, hint: true },
    { field: 'websiteUrl', autoComplete: 'url', inputMode: 'url', type: 'url', wide: true, hint: true },
  ],
}

/**
 * The tabbed profile form (C107): Personal, Work, Address, Online and, when `account` is given,
 * the read-only Account tab. It holds no state of its own: the page (or the admin dialog) owns the
 * draft, so one Save covers every tab. `messages` are the error texts to show, keyed
 * `<section>.<field>`; a tab with an error carries a red dot.
 */
export function ProfileForm({ form, messages, onChange, onBlur, tab, onTab, nameEditable = true, account, disabled = false }: {
  form: ProfileFormValues
  messages: Record<string, string>
  onChange: (section: ProfileSection, field: string, value: string) => void
  onBlur: (section: ProfileSection, field: string) => void
  tab: ProfileTab
  onTab: (tab: ProfileTab) => void
  nameEditable?: boolean
  account?: ProfileAccount
  disabled?: boolean
}) {
  const { t } = useTranslation()
  const tabs = PROFILE_TABS.filter((k) => k !== 'account' || account)
  const active = tabs.includes(tab) ? tab : tabs[0]
  const withErrors = new Set<string>(sectionsWithErrors(messages))

  const onKeyDown = (e: React.KeyboardEvent) => {
    // Enter in a single-line field must not submit anything; the Save button is the only way.
    if (e.key === 'Enter' && (e.target as HTMLElement).tagName === 'INPUT') e.preventDefault()
  }

  return (
    <Box>
      <Tabs value={active} onChange={(_, v) => onTab(v as ProfileTab)} variant="scrollable" scrollButtons="auto"
        aria-label={t('profile.tabsLabel')} sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        {tabs.map((k) => (
          <Tab key={k} value={k} id={`profile-tab-${k}`} aria-controls={`profile-panel-${k}`}
            aria-label={withErrors.has(k) ? `${t(`profile.tabs.${k}`)} ${t('profile.tabHasErrors')}` : undefined}
            label={(
              <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.75 }}>
                {t(`profile.tabs.${k}`)}
                {withErrors.has(k) && (
                  <Box component="span" aria-hidden data-testid={`tab-error-${k}`}
                    sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'error.main' }} />
                )}
              </Box>
            )} />
        ))}
      </Tabs>

      <Box role="tabpanel" id={`profile-panel-${active}`} aria-labelledby={`profile-tab-${active}`} onKeyDown={onKeyDown}>
        {active === 'account' && account ? <AccountPanel account={account} /> : (
          <>
            <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2.5, maxWidth: 640 }}>
              {t(`profile.tabHints.${active}`)}
            </Typography>
            <Box sx={{ display: 'grid', gap: 2.5, gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 1fr))' }, maxWidth: 760 }}>
              {LAYOUT[active as ProfileSection].map((def) => {
                const section = active as ProfileSection
                const key = `${section}.${def.field}`
                const value = (form[section] as Record<string, string>)[def.field] ?? ''
                const error = messages[key]
                const readOnlyName = !nameEditable && section === 'personal' && (def.field === 'firstName' || def.field === 'lastName')
                const hint = readOnlyName ? t('profile.hints.nameManaged')
                  : def.counter ? t('profile.hints.counter', { count: value.length, max: MAX_LENGTH[def.field] })
                  : def.hint ? t(`profile.hints.${def.field}`) : undefined
                return (
                  <TextField key={key} id={profileFieldId(section, def.field)} size="small" fullWidth
                    label={t(`profile.fields.${def.field}`)} value={value}
                    required={REQUIRED_FIELDS.includes(key)} disabled={disabled || readOnlyName}
                    multiline={def.multiline} minRows={def.multiline ? 3 : undefined}
                    type={def.type} error={Boolean(error)} helperText={error ?? hint}
                    onChange={(e) => onChange(section, def.field, e.target.value)}
                    onBlur={() => onBlur(section, def.field)}
                    sx={{ ...(def.wide ? { gridColumn: { sm: '1 / -1' } } : {}), '& input, & textarea': { scrollMarginBottom: '96px' } }}
                    slotProps={{ htmlInput: { autoComplete: def.autoComplete, inputMode: def.inputMode, maxLength: (MAX_LENGTH[def.field] ?? 40) + 60 } }} />
                )
              })}
            </Box>
            {active === 'personal' && account && (
              <Alert severity="info" icon={false} sx={{ mt: 3, maxWidth: 760 }}>
                {t('profile.preferencesNote')}{' '}
                <Link component={RouterLink} to="/account/preferences">{t('profile.preferencesLink')}</Link>
              </Alert>
            )}
          </>
        )}
      </Box>
    </Box>
  )
}

function Fact({ label, children }: { label: string; children: ReactNode }) {
  return (
    <Box sx={{ display: 'contents' }}>
      <Typography component="dt" sx={{ color: 'text.secondary' }}>{label}</Typography>
      <Typography component="dd" sx={{ m: 0, minWidth: 0, overflowWrap: 'anywhere' }}>{children || '—'}</Typography>
    </Box>
  )
}

function AccountPanel({ account }: { account: ProfileAccount }) {
  const { t, i18n } = useTranslation()
  const since = account.memberSince ? new Intl.DateTimeFormat(i18n.language, { dateStyle: 'long' }).format(new Date(account.memberSince)) : null
  const org = account.organizationName
    ? `${account.organizationName}${account.organizationCode ? ` (${account.organizationCode})` : ''}` : null
  return (
    <Box sx={{ maxWidth: 760 }}>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2.5 }}>{t('profile.tabHints.account')}</Typography>
      <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'max-content 1fr' }, columnGap: 4, rowGap: { xs: 0.25, sm: 1.5 }, m: 0,
        '& dd': { mb: { xs: 1.5, sm: 0 } } }}>
        <Fact label={t('profile.account.email')}>{account.email}</Fact>
        <Fact label={t('profile.account.role')}>{t(`profile.roles.${account.role}`)}</Fact>
        {account.role !== 'PLATFORM_ADMIN' && <Fact label={t('profile.account.organization')}>{org}</Fact>}
        {account.status && <Fact label={t('profile.account.status')}>{t(`profile.status.${account.status}`, { defaultValue: account.status })}</Fact>}
        {account.memberSince && <Fact label={t('profile.account.memberSince')}>{since}</Fact>}
      </Box>
      <Typography variant="body2" sx={{ color: 'text.secondary', mt: 2 }}>{t('profile.account.emailNote')}</Typography>
      <Box component="ul" sx={{ listStyle: 'none', p: 0, mt: 3, display: 'flex', flexWrap: 'wrap', gap: 2.5 }}>
        <li><Link component={RouterLink} to="/account/security">{t('profile.account.linkSecurity')}</Link></li>
        <li><Link component={RouterLink} to="/account/preferences">{t('profile.account.linkPreferences')}</Link></li>
        {account.role === 'ORG_ADMIN' && (
          <>
            <li><Link component={RouterLink} to="/organization/members">{t('profile.account.linkMembers')}</Link></li>
            <li><Link component={RouterLink} to="/organization/business-dashboard">{t('profile.account.linkOrganization')}</Link></li>
          </>
        )}
      </Box>
    </Box>
  )
}
