import type { ProfileDto, ProfileSection, ProfileUpdateRequest } from '../api/profileApi'

/**
 * "My profile" (C107): the field rules, mirrored from the server, which stays the source of
 * truth. Every free-text value is trimmed and has its inner whitespace collapsed; a blank value
 * is "not set" (except the two required names); control characters and < > are refused.
 * The UI translates the returned codes with `profile.errors.<code>`.
 */

export const PROFILE_SECTIONS = ['personal', 'work', 'address', 'online'] as const

export const PROFILE_FIELDS = {
  personal: ['firstName', 'lastName', 'preferredName', 'mobile', 'alternatePhone', 'bio'],
  work: ['jobTitle', 'department', 'companyName', 'industry'],
  address: ['addressLine1', 'addressLine2', 'city', 'state', 'postalCode', 'country'],
  online: ['linkedinUrl', 'websiteUrl'],
} as const satisfies Record<ProfileSection, readonly string[]>

/** The tabs of the profile page; the admin dialog shows the first four. */
export const PROFILE_TABS = ['personal', 'work', 'address', 'online', 'account'] as const
export type ProfileTab = (typeof PROFILE_TABS)[number]

export type ProfileForm = { [S in ProfileSection]: Record<(typeof PROFILE_FIELDS)[S][number], string> }

export type ProfileErrorCode =
  | 'required' | 'maxLength' | 'invalidChars' | 'invalidName' | 'invalidPhone'
  | 'invalidPostalCode' | 'invalidPostalCodeIndia' | 'invalidLinkedin' | 'invalidWebsite'

export interface ProfileFieldError { code: ProfileErrorCode; max?: number }
/** Keyed `<section>.<field>`, like the server's fieldErrors. */
export type ProfileErrors = Record<string, ProfileFieldError>

export const MAX_LENGTH: Record<string, number> = {
  firstName: 100, lastName: 100, preferredName: 60, bio: 500,
  jobTitle: 100, department: 100, companyName: 150, industry: 100,
  addressLine1: 150, addressLine2: 150, city: 80, state: 80, postalCode: 12, country: 80,
  linkedinUrl: 200, websiteUrl: 200,
}

export const REQUIRED_FIELDS: readonly string[] = ['personal.firstName', 'personal.lastName']

const NAME_PATTERN = /^[\p{L}\p{M}][\p{L}\p{M} .'’-]{0,99}$/u
const PHONE_PATTERN = /^\+[1-9]\d{6,14}$/
const POSTAL_PATTERN = /^[A-Za-z0-9][A-Za-z0-9 -]{1,10}$/
const INDIA_POSTAL_PATTERN = /^\d{6}$/
// eslint-disable-next-line no-control-regex -- refusing control characters is the rule
const CONTROL_OR_BRACKET = /[\u0000-\u001F\u007F-\u009F<>]/

/** Trim and collapse runs of whitespace (including newlines and tabs) to one space. */
export function normalizeText(value: string | null | undefined): string {
  return (value ?? '').replace(/\s+/g, ' ').trim()
}

/** Strips the separators people type in a phone number; the result must still match E.164. */
export function normalizePhone(value: string | null | undefined): string {
  return (value ?? '').replace(/[\s\-.()]/g, '')
}

export function isIndia(country: string | null | undefined): boolean {
  const c = normalizeText(country).toLowerCase()
  return c === 'india' || c === 'in'
}

function parseUrl(value: string): URL | null {
  try { return new URL(value) } catch { return null }
}

export function isValidLinkedinUrl(value: string): boolean {
  const url = parseUrl(value)
  if (!url || url.protocol !== 'https:' || url.username || url.password) return false
  const host = url.hostname.toLowerCase()
  return host === 'linkedin.com' || host.endsWith('.linkedin.com')
}

export function isValidWebsiteUrl(value: string): boolean {
  const url = parseUrl(value)
  if (!url || (url.protocol !== 'http:' && url.protocol !== 'https:') || url.username || url.password) return false
  const host = url.hostname.toLowerCase()
  return /^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$/.test(host)
}

/** The error for one field, or null. `values` is the whole (raw) form, for rules that read a neighbour (postal code and country). */
export function validateField(section: ProfileSection, field: string, raw: string, values: ProfileForm): ProfileFieldError | null {
  const key = `${section}.${field}`
  const isPhone = field === 'mobile' || field === 'alternatePhone'
  const value = isPhone ? normalizePhone(raw) : normalizeText(raw)

  if (value === '') return REQUIRED_FIELDS.includes(key) ? { code: 'required' } : null

  const max = MAX_LENGTH[field]
  if (max !== undefined && value.length > max) return { code: 'maxLength', max }

  if (!isPhone && CONTROL_OR_BRACKET.test(value)) return { code: 'invalidChars' }

  switch (field) {
    case 'firstName': case 'lastName': case 'preferredName':
      return NAME_PATTERN.test(value) ? null : { code: 'invalidName' }
    case 'mobile': case 'alternatePhone':
      return PHONE_PATTERN.test(value) ? null : { code: 'invalidPhone' }
    case 'postalCode':
      if (isIndia(values.address.country)) return INDIA_POSTAL_PATTERN.test(value) ? null : { code: 'invalidPostalCodeIndia' }
      return POSTAL_PATTERN.test(value) ? null : { code: 'invalidPostalCode' }
    case 'linkedinUrl':
      return isValidLinkedinUrl(value) ? null : { code: 'invalidLinkedin' }
    case 'websiteUrl':
      return isValidWebsiteUrl(value) ? null : { code: 'invalidWebsite' }
    default:
      return null
  }
}

/** Every error in the form. With `nameEditable: false` the two names are not checked (they are shown read-only and sent as stored). */
export function validateProfile(form: ProfileForm, opts: { nameEditable?: boolean } = {}): ProfileErrors {
  const nameEditable = opts.nameEditable ?? true
  const errors: ProfileErrors = {}
  for (const section of PROFILE_SECTIONS) {
    for (const field of PROFILE_FIELDS[section]) {
      if (!nameEditable && section === 'personal' && (field === 'firstName' || field === 'lastName')) continue
      const error = validateField(section, field, (form[section] as Record<string, string>)[field] ?? '', form)
      if (error) errors[`${section}.${field}`] = error
    }
  }
  return errors
}

/** The tab a `<section>.<field>` key lives on. */
export function sectionOf(key: string): ProfileSection {
  return key.split('.')[0] as ProfileSection
}

/** Tabs that hold at least one error, in tab order. */
export function sectionsWithErrors(errors: Record<string, unknown>): ProfileSection[] {
  const found = new Set(Object.keys(errors).map(sectionOf))
  return PROFILE_SECTIONS.filter((s) => found.has(s))
}

export function emptyProfileForm(): ProfileForm {
  const form = {} as Record<string, Record<string, string>>
  for (const section of PROFILE_SECTIONS) {
    form[section] = Object.fromEntries(PROFILE_FIELDS[section].map((f) => [f, '']))
  }
  return form as unknown as ProfileForm
}

/** The form's starting values from the server's record (null becomes an empty string). */
export function profileToForm(profile: ProfileDto): ProfileForm {
  const form = emptyProfileForm()
  for (const section of PROFILE_SECTIONS) {
    const source = profile[section] as unknown as Record<string, string | null>
    for (const field of PROFILE_FIELDS[section]) {
      (form[section] as Record<string, string>)[field] = source[field] ?? ''
    }
  }
  return form
}

/** The request body: normalized values, blank as null. The server validates again. */
export function formToRequest(form: ProfileForm): ProfileUpdateRequest {
  const out = {} as Record<string, Record<string, string | null>>
  for (const section of PROFILE_SECTIONS) {
    out[section] = {}
    for (const field of PROFILE_FIELDS[section]) {
      const raw = (form[section] as Record<string, string>)[field] ?? ''
      const value = field === 'mobile' || field === 'alternatePhone' ? normalizePhone(raw) : normalizeText(raw)
      out[section][field] = value === '' ? null : value
    }
  }
  return out as unknown as ProfileUpdateRequest
}

/** True when the draft differs from the saved record once both are normalized. */
export function isProfileDirty(form: ProfileForm, saved: ProfileForm): boolean {
  return JSON.stringify(formToRequest(form)) !== JSON.stringify(formToRequest(saved))
}

/** Where each completion item is edited (the photo lives in the header card, on the first tab). */
export const COMPLETION_ITEM_SECTION: Record<string, ProfileSection> = {
  photo: 'personal', mobile: 'personal',
  jobTitle: 'work', department: 'work', companyName: 'work', industry: 'work',
  addressLine1: 'address', city: 'address', state: 'address', postalCode: 'address', country: 'address',
}

/** The DOM id of a field's input, used to move focus to it. */
export const profileFieldId = (section: ProfileSection, field: string) => `profile-${section}-${field}`
