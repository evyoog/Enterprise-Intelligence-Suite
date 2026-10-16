# API: My profile (REQ-TEN-011)

Decision [C107](../../01-business/roadmap/open-decisions.md#c107). FRD: [my-profile](../../02-requirements/FRD/my-profile/api-requirements.md). JSON is camelCase. The caller is resolved from the sign-in token, never from an id in the path, query or body (BR-PRF-001). `/me/**` already requires sign-in.

## Endpoints
| Method | Path | Purpose | Permission |
|---|---|---|---|
| GET | `/me/profile` | Own profile → `ProfileDto` | signed in |
| PUT | `/me/profile` | `ProfileUpdateRequest` → `ProfileDto`. Replaces the four sections; creates the record of a platform administrator who has none | signed in |
| GET | `/me/profile/completion` | `{exists, percent, filled, total, missing}` (light; used by the global bar). 200 with `{exists:false, percent:0, filled:0, total:11, missing:[all]}` for a person with no record, never 404 | signed in |
| PUT | `/me/profile/photo` | Multipart, field `file` → `ProfileDto` | signed in |
| DELETE | `/me/profile/photo` | Remove the photo → `ProfileDto` | signed in |
| GET | `/me/profile/photo` | Image bytes with the stored `Content-Type`, `Cache-Control: private, max-age=0, must-revalidate`, an `ETag` and `X-Content-Type-Options: nosniff`; 304 when `If-None-Match` matches; 404 when none. The frontend loads it with `fetch`, the bearer token and an object URL | signed in |
| GET | `/admin/registrations/individuals/{customerId}/profile` | Any existing customer's profile (no photo) | platform `MANAGE_REGISTRATIONS` |
| PUT | `/admin/registrations/individuals/{customerId}/profile` | Same body as `/me/profile`; edits that customer; audit `PROFILE_UPDATED_BY_ADMIN` | platform `MANAGE_REGISTRATIONS` |

## ProfileDto
```json
{
  "exists": true,
  "account": { "email": "", "role": "PLATFORM_ADMIN | ORG_ADMIN | MEMBER | INDIVIDUAL",
               "organizationName": null, "organizationCode": null, "status": null,
               "memberSince": null, "nameEditable": true },
  "personal": { "firstName": "", "lastName": "", "preferredName": null, "mobile": null, "alternatePhone": null, "bio": null },
  "work":     { "jobTitle": null, "department": null, "companyName": null, "industry": null },
  "address":  { "addressLine1": null, "addressLine2": null, "city": null, "state": null, "postalCode": null, "country": null },
  "online":   { "linkedinUrl": null, "websiteUrl": null },
  "photo":    { "present": false, "updatedAt": null },
  "completion": { "percent": 0, "filled": 0, "total": 11, "missing": ["photo", "mobile", "..."] }
}
```
`memberSince` is ISO-8601: the date the person joined the organization, or the date the account was created for a person with no organization. `role` is `PLATFORM_ADMIN` for a holder of `MANAGE_REGISTRATIONS` (even one who also has a customer record), otherwise the organization role (`ORG_ADMIN`, `MEMBER`) or `INDIVIDUAL`; through the admin endpoint it is the viewed person's own role. `status` is the membership status (`ACTIVE`, `SUSPENDED`, `INACTIVE`; the active membership is preferred, else the most recent), null for an individual. `exists` is false only for a signed-in person with no customer record (a platform administrator); the response is then built from the token (email, given name, family name), everything else is null and `completion` is `{percent:0, filled:0, total:11, missing: all keys}`. Completion keys, in order: `photo`, `mobile`, `jobTitle`, `department`, `companyName`, `industry`, `addressLine1`, `city`, `state`, `postalCode`, `country`.

## ProfileUpdateRequest
`{ "personal": {firstName, lastName, preferredName, mobile, alternatePhone, bio}, "work": {jobTitle, department, companyName, industry}, "address": {addressLine1, addressLine2, city, state, postalCode, country}, "online": {linkedinUrl, websiteUrl} }`. No email, role, status or photo. A blank or missing optional field clears the value. The rules are BR-PRF-005 to BR-PRF-011.

## Errors
| Status | When | Body |
|---|---|---|
| 400 | Validation failed (BR-PRF-005 to BR-PRF-011) | `{"timestamp":"…","status":400,"error":"Bad Request","message":"Validation failed","fieldErrors":{"personal.firstName":"…","address.postalCode":"…"}}`; keys are `<section>.<field>`, messages in English, every invalid field in one answer. The handler is scoped to the profile controllers; every error body is `{timestamp, status, error, message}` and errors elsewhere keep the platform's own body |
| 400 | Photo of a wrong type, wrong content (first bytes), empty, undecodable or over 4096 x 4096; a token without an email claim on create | `{status, message}` |
| 401 | Not signed in | - |
| 403 | Admin paths without `MANAGE_REGISTRATIONS`; a personal profile call made with an API key | `{status, message}` |
| 404 | A person who is not a platform administrator has no customer record (`GET`/`PUT /me/profile` and photo calls; not the completion call); unknown `customerId`; no photo on `GET /me/profile/photo` | `{status, message}` |
| 409 | A platform administrator's token email already belongs to a customer row linked to a different login; a platform administrator with no record uploads or removes a photo ("Create your profile before adding a photo"); any customer row already has the token's email (case-insensitive) | `{status, message}` |
| 413 | Photo larger than 1 MB, or a multipart request over the server's upload limit | `{status, message}` |
| 502 | Keycloak could not be updated for a name change; the whole save is rolled back | `{status, message}` |

## Audit
`PROFILE_CREATED`, `PROFILE_UPDATED`, `PROFILE_PHOTO_UPDATED`, `PROFILE_PHOTO_REMOVED`, `PROFILE_UPDATED_BY_ADMIN` (actor: the administrator; entity: the customer). Sections changed only, never values.
