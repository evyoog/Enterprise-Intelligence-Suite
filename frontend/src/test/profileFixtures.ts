import type { ProfileDto } from '../api/profileApi'

/** A complete-enough profile record as GET /me/profile returns it; override what a test needs. */
export function makeProfile(over: Partial<ProfileDto> = {}): ProfileDto {
  return {
    exists: true,
    account: {
      email: 'asha.rao@acme.example', role: 'ORG_ADMIN', organizationName: 'Acme Industries', organizationCode: 'ACME',
      status: 'ACTIVE', memberSince: '2026-03-04T10:00:00Z', nameEditable: true,
    },
    personal: { firstName: 'Asha', lastName: 'Rao', preferredName: null, mobile: '+919876543210', alternatePhone: null, bio: null },
    work: { jobTitle: 'Director', department: null, companyName: 'Acme Industries', industry: null },
    address: { addressLine1: null, addressLine2: null, city: 'Chennai', state: null, postalCode: null, country: 'India' },
    online: { linkedinUrl: null, websiteUrl: null },
    photo: { present: false, updatedAt: null },
    completion: {
      percent: 45, filled: 5, total: 11,
      missing: ['photo', 'department', 'industry', 'addressLine1', 'state', 'postalCode'],
    },
    ...over,
  }
}

/** A platform administrator who has not created a customer record: built from the token. */
export function makeAdminWithoutRecord(): ProfileDto {
  return makeProfile({
    exists: false,
    account: { email: 'root@evyoog.example', role: 'PLATFORM_ADMIN', organizationName: null, organizationCode: null, status: null, memberSince: null, nameEditable: true },
    personal: { firstName: 'Root', lastName: 'Admin', preferredName: null, mobile: null, alternatePhone: null, bio: null },
    work: { jobTitle: null, department: null, companyName: null, industry: null },
    address: { addressLine1: null, addressLine2: null, city: null, state: null, postalCode: null, country: null },
    completion: {
      percent: 0, filled: 0, total: 11,
      missing: ['photo', 'mobile', 'jobTitle', 'department', 'companyName', 'industry', 'addressLine1', 'city', 'state', 'postalCode', 'country'],
    },
  })
}
