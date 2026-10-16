import { describe, expect, it } from 'vitest'
import type { ProfileDto } from '../api/profileApi'
import {
  emptyProfileForm, formToRequest, isIndia, isProfileDirty, isValidLinkedinUrl, isValidWebsiteUrl, normalizePhone, normalizeText,
  profileToForm, sectionsWithErrors, validateField, validateProfile, type ProfileForm,
} from './profileValidation'

function form(patch: { [S in keyof ProfileForm]?: Partial<ProfileForm[S]> } = {}): ProfileForm {
  const base = emptyProfileForm()
  base.personal.firstName = 'Asha'
  base.personal.lastName = 'Rao'
  return {
    personal: { ...base.personal, ...patch.personal }, work: { ...base.work, ...patch.work },
    address: { ...base.address, ...patch.address }, online: { ...base.online, ...patch.online },
  }
}
const codeOf = (f: ProfileForm, key: string) => validateProfile(f)[key]?.code

describe('normalizeText and normalizePhone', () => {
  it('trims and collapses inner whitespace, newlines and tabs', () => {
    expect(normalizeText('  Asha \t  Rao\n ')).toBe('Asha Rao')
    expect(normalizeText(null)).toBe('')
  })
  it('strips spaces, hyphens, dots and parentheses from a phone number', () => {
    expect(normalizePhone(' +91 (98765) 432-10 ')).toBe('+919876543210')
  })
})

describe('validateProfile — names (required, letters/marks/space/.\'’-)', () => {
  it('accepts a normal record', () => {
    expect(validateProfile(form())).toEqual({})
  })
  it.each(["Mary-Jane", "O'Brien", 'O’Neil', 'Dr. Who', 'José', 'Zoë', 'Ananya', 'முருகன்'])('accepts %s', (name) => {
    expect(codeOf(form({ personal: { firstName: name } }), 'personal.firstName')).toBeUndefined()
  })
  it('requires first and last name; blank counts as missing', () => {
    expect(codeOf(form({ personal: { firstName: '   ' } }), 'personal.firstName')).toBe('required')
    expect(codeOf(form({ personal: { lastName: '' } }), 'personal.lastName')).toBe('required')
  })
  it.each(['1Asha', '-Asha', ' .Asha', 'Asha_Rao', 'Asha123', 'A@sha'])('rejects the name %s', (name) => {
    expect(codeOf(form({ personal: { firstName: name } }), 'personal.firstName')).toBe('invalidName')
  })
  it('rejects a name over 100 characters and a preferred name over 60', () => {
    expect(validateProfile(form({ personal: { firstName: 'A'.repeat(101) } }))['personal.firstName']).toEqual({ code: 'maxLength', max: 100 })
    expect(codeOf(form({ personal: { firstName: 'A'.repeat(100) } }), 'personal.firstName')).toBeUndefined()
    expect(validateProfile(form({ personal: { preferredName: 'A'.repeat(61) } }))['personal.preferredName']).toEqual({ code: 'maxLength', max: 60 })
  })
  it('leaves the preferred name optional but applies the name pattern', () => {
    expect(codeOf(form({ personal: { preferredName: '' } }), 'personal.preferredName')).toBeUndefined()
    expect(codeOf(form({ personal: { preferredName: 'Ash3' } }), 'personal.preferredName')).toBe('invalidName')
  })
  it('skips the two names when they are not editable (managed by an identity provider)', () => {
    const f = form({ personal: { firstName: '', lastName: '' } })
    expect(validateProfile(f, { nameEditable: false })).toEqual({})
    expect(Object.keys(validateProfile(f))).toEqual(['personal.firstName', 'personal.lastName'])
  })
})

describe('validateProfile — phones (E.164 after normalisation)', () => {
  it.each(['+919876543210', '+91 98765-43210', '+1 (415) 555.0100', '+4420 7946 0958'])('accepts %s', (p) => {
    expect(codeOf(form({ personal: { mobile: p } }), 'personal.mobile')).toBeUndefined()
  })
  it.each(['9876543210', '+0123456789', '+12345', '+1234567890123456', '+91abc98765', 'call me'])('rejects %s', (p) => {
    expect(codeOf(form({ personal: { mobile: p } }), 'personal.mobile')).toBe('invalidPhone')
  })
  it('applies the same rule to the alternate phone and leaves both optional', () => {
    expect(codeOf(form({ personal: { alternatePhone: '12345' } }), 'personal.alternatePhone')).toBe('invalidPhone')
    expect(validateProfile(form({ personal: { mobile: '', alternatePhone: '' } }))).toEqual({})
  })
})

describe('validateProfile — free text', () => {
  it('rejects the characters < and > in every free-text field', () => {
    const fields: [keyof ProfileForm, string][] = [
      ['personal', 'bio'], ['work', 'jobTitle'], ['work', 'department'], ['work', 'companyName'], ['work', 'industry'],
      ['address', 'addressLine1'], ['address', 'addressLine2'], ['address', 'city'], ['address', 'state'], ['address', 'country'],
    ]
    for (const [section, field] of fields) {
      const f = form({ [section]: { [field]: 'a <script> b' } })
      expect(codeOf(f, `${section}.${field}`), `${section}.${field}`).toBe('invalidChars')
    }
  })
  it('rejects control characters but treats newlines and tabs as spaces (collapsed)', () => {
    expect(codeOf(form({ work: { jobTitle: 'Eng\u0007ineer' } }), 'work.jobTitle')).toBe('invalidChars')
    expect(codeOf(form({ personal: { bio: 'Line one\nLine two\t!' } }), 'personal.bio')).toBeUndefined()
  })
  it.each([
    ['personal', 'bio', 500], ['work', 'jobTitle', 100], ['work', 'department', 100], ['work', 'companyName', 150], ['work', 'industry', 100],
    ['address', 'addressLine1', 150], ['address', 'addressLine2', 150], ['address', 'city', 80], ['address', 'state', 80], ['address', 'country', 80],
  ] as const)('%s.%s allows %i characters and not one more', (section, field, max) => {
    expect(codeOf(form({ [section]: { [field]: 'x'.repeat(max) } }), `${section}.${field}`)).toBeUndefined()
    expect(validateProfile(form({ [section]: { [field]: 'x'.repeat(max + 1) } }))[`${section}.${field}`]).toEqual({ code: 'maxLength', max })
  })
  it('counts length after trimming and collapsing', () => {
    expect(codeOf(form({ work: { jobTitle: `  ${'x'.repeat(100)}  ` } }), 'work.jobTitle')).toBeUndefined()
  })
})

describe('validateProfile — postal code', () => {
  it('accepts general postal codes and rejects bad ones', () => {
    for (const ok of ['SW1A 1AA', '90210', 'K1A-0B1', 'ab']) {
      expect(codeOf(form({ address: { country: 'United Kingdom', postalCode: ok } }), 'address.postalCode'), ok).toBeUndefined()
    }
    for (const bad of ['-12345', 'a', '12 34 56 78 90 12', '12#45']) {
      expect(codeOf(form({ address: { country: 'France', postalCode: bad } }), 'address.postalCode'), bad).toBeDefined()
    }
  })
  it('requires exactly 6 digits when the country is India (India or IN, any case)', () => {
    for (const country of ['India', 'india', ' IN ', 'in']) {
      expect(isIndia(country)).toBe(true)
      expect(codeOf(form({ address: { country, postalCode: '600001' } }), 'address.postalCode')).toBeUndefined()
      expect(codeOf(form({ address: { country, postalCode: '60001' } }), 'address.postalCode')).toBe('invalidPostalCodeIndia')
      expect(codeOf(form({ address: { country, postalCode: '6000A1' } }), 'address.postalCode')).toBe('invalidPostalCodeIndia')
    }
    expect(isIndia('Indiana')).toBe(false)
  })
  it('re-evaluates the postal code when the country changes', () => {
    const f = form({ address: { postalCode: 'SW1A 1AA', country: 'India' } })
    expect(codeOf(f, 'address.postalCode')).toBe('invalidPostalCodeIndia')
    f.address.country = 'United Kingdom'
    expect(codeOf(f, 'address.postalCode')).toBeUndefined()
  })
  it('limits the postal code to 12 characters', () => {
    expect(validateProfile(form({ address: { country: 'X', postalCode: 'A'.repeat(13) } }))['address.postalCode']).toEqual({ code: 'maxLength', max: 12 })
  })
})

describe('URL rules', () => {
  it('LinkedIn: https only, linkedin.com or a subdomain', () => {
    expect(isValidLinkedinUrl('https://www.linkedin.com/in/asha-rao')).toBe(true)
    expect(isValidLinkedinUrl('https://linkedin.com/in/asha')).toBe(true)
    expect(isValidLinkedinUrl('https://in.linkedin.com/in/asha')).toBe(true)
    expect(isValidLinkedinUrl('http://www.linkedin.com/in/asha')).toBe(false)
    expect(isValidLinkedinUrl('https://notlinkedin.com/in/asha')).toBe(false)
    expect(isValidLinkedinUrl('https://linkedin.com.evil.example/in/asha')).toBe(false)
    expect(isValidLinkedinUrl('https://user:pw@linkedin.com/in/asha')).toBe(false)
    expect(isValidLinkedinUrl('linkedin.com/in/asha')).toBe(false)
    expect(isValidLinkedinUrl('javascript:alert(1)')).toBe(false)
  })
  it('website: http or https with a valid host', () => {
    expect(isValidWebsiteUrl('https://www.example.com')).toBe(true)
    expect(isValidWebsiteUrl('http://example.co.in/about?x=1')).toBe(true)
    expect(isValidWebsiteUrl('ftp://example.com')).toBe(false)
    expect(isValidWebsiteUrl('javascript:alert(1)')).toBe(false)
    expect(isValidWebsiteUrl('https://localhost')).toBe(false)
    expect(isValidWebsiteUrl('https://exa mple.com')).toBe(false)
    expect(isValidWebsiteUrl('example.com')).toBe(false)
  })
  it('limits both to 200 characters and leaves them optional', () => {
    const long = `https://www.linkedin.com/in/${'a'.repeat(180)}`
    expect(validateProfile(form({ online: { linkedinUrl: long } }))['online.linkedinUrl']).toEqual({ code: 'maxLength', max: 200 })
    expect(validateProfile(form({ online: { linkedinUrl: '', websiteUrl: '' } }))).toEqual({})
    expect(codeOf(form({ online: { websiteUrl: 'nope' } }), 'online.websiteUrl')).toBe('invalidWebsite')
    expect(codeOf(form({ online: { linkedinUrl: 'https://example.com/x' } }), 'online.linkedinUrl')).toBe('invalidLinkedin')
  })
})

describe('form helpers', () => {
  const dto = {
    exists: true,
    account: { email: 'a@x.example', role: 'MEMBER', organizationName: null, organizationCode: null, status: null, memberSince: null, nameEditable: true },
    personal: { firstName: 'Asha', lastName: 'Rao', preferredName: null, mobile: '+919876543210', alternatePhone: null, bio: null },
    work: { jobTitle: 'Engineer', department: null, companyName: null, industry: null },
    address: { addressLine1: null, addressLine2: null, city: 'Chennai', state: null, postalCode: null, country: 'India' },
    online: { linkedinUrl: null, websiteUrl: null },
    photo: { present: false, updatedAt: null },
    completion: { percent: 27, filled: 3, total: 11, missing: [] },
  } as ProfileDto

  it('maps null to empty strings and back to null, normalised', () => {
    const f = profileToForm(dto)
    expect(f.personal.preferredName).toBe('')
    f.personal.mobile = '+91 98765 43210'
    f.work.jobTitle = '  Senior   Engineer '
    const req = formToRequest(f)
    expect(req.personal.mobile).toBe('+919876543210')
    expect(req.personal.preferredName).toBeNull()
    expect(req.work.jobTitle).toBe('Senior Engineer')
    expect(Object.keys(req)).toEqual(['personal', 'work', 'address', 'online'])
  })
  it('is dirty only when a normalised value differs', () => {
    const saved = profileToForm(dto)
    const draft = profileToForm(dto)
    expect(isProfileDirty(draft, saved)).toBe(false)
    draft.work.jobTitle = ' Engineer  '
    expect(isProfileDirty(draft, saved)).toBe(false)
    draft.work.jobTitle = 'Manager'
    expect(isProfileDirty(draft, saved)).toBe(true)
  })
  it('names the tabs that hold errors, in tab order', () => {
    expect(sectionsWithErrors({ 'online.websiteUrl': 1, 'personal.mobile': 1, 'online.linkedinUrl': 1 })).toEqual(['personal', 'online'])
  })
  it('validates one field against the whole form', () => {
    const f = form({ address: { country: 'India', postalCode: '123' } })
    expect(validateField('address', 'postalCode', '123', f)?.code).toBe('invalidPostalCodeIndia')
  })
})
