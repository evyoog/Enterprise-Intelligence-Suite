// REQ-IAM-007 (C28): per-provider attribute / claim names. Null or blank = use the defaults.
export interface ClaimMapping {
  email?: string | null
  firstName?: string | null
  lastName?: string | null
  displayName?: string | null
}

export type ClaimField = keyof ClaimMapping

export const CLAIM_FIELDS: ClaimField[] = ['email', 'firstName', 'lastName', 'displayName']

/** The names each protocol tries when no custom name is set (matches the backend). */
export const DEFAULT_CLAIMS: Record<'SAML' | 'OIDC', Record<ClaimField, string>> = {
  SAML: {
    email: 'email, emailaddress, mail (or an email-style NameID)',
    firstName: 'firstname, givenname',
    lastName: 'lastname, surname, sn',
    displayName: 'displayname, name, cn',
  },
  OIDC: {
    email: 'email',
    firstName: 'given_name',
    lastName: 'family_name',
    displayName: 'name',
  },
}
