# REQ-TEN-011 — My profile

**Status:** Approved (2026-10-16, product owner request, [C107](../../../01-business/roadmap/open-decisions.md#c107)); the defaults the owner did not state are listed in C107 (PF-1 to PF-14) as "default, to confirm"
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-16 (request for a profile screen for all, with photo, all missing fields on tabs, validations and a global profile-filling bar)

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) ([C107](../../../01-business/roadmap/open-decisions.md#c107)) |
| Requirement ID | REQ-TEN-011 |
| Application | [05 Customer & Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Functions | None: cross-cutting, like [catalog-showcase](../catalog-showcase/requirement.md). No roadmap function ID is invented |
| Priority | P1 |

## Summary
Every signed-in person (platform administrator, organization administrator, member, individual) has a **My profile** page at `/account/profile` to see and edit their own customer record: personal, work, address and online details, and a profile photo, on five tabs, with the same validation on the server and in the screen. A **global profile-filling bar** under the top bar shows how complete the profile is and links to the page. A platform administrator who has no customer record is not forced to create one; the page offers it. A platform administrator can also view and edit any existing customer's record from the screens that list people.

## Actors
- **Any signed-in person**: reads and edits **only their own** profile and photo.
- **Platform administrator** (a person who holds `MANAGE_REGISTRATIONS`): has no customer record unless they create one (optional). Can create their own record, and can view and edit any existing customer's record (no photo).
- **Organization administrator, member, individual**: as any signed-in person. An organization administrator cannot edit a member's profile.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-011.1 | **Page and route:** `/account/profile` for every signed-in person (`RequireAuth`), reached from the profile menu ("My profile", first item), the avatar and name block, and the global bar. | Must |
| REQ-TEN-011.2 | **Tabs and fields:** *Personal* (first name, last name, preferred name, mobile, alternate phone, bio), *Work* (job title, department, company name, industry), *Address* (address line 1 and 2, city, state, postal code, country), *Online* (LinkedIn, website), *Account* (read-only: email, role, organization name and code, membership status, member since, links). One form, one Save and Discard bar. | Must |
| REQ-TEN-011.3 | **Validation:** every rule of BR-PRF-005 to BR-PRF-011 is enforced on the server (the source of truth) and mirrored in the screen, with inline messages, an error badge on the tab that holds an invalid field, and server field errors mapped back onto the fields. | Must |
| REQ-TEN-011.4 | **Profile photo:** upload, replace and remove (JPEG, PNG or WebP, at most 1 MB, verified content, at most 4096 x 4096; the browser downsizes to 512 x 512). Initials are shown when there is no photo. Stored in the database (table `customer_photo`) and never loaded with the customer. | Must |
| REQ-TEN-011.5 | **Own profile only:** the person is identified from the sign-in token, never from an id in the address or body. Nobody can read or change another person's profile or photo through `/me/profile`. | Must |
| REQ-TEN-011.6 | **Platform administrator without a record (no forced onboarding):** the page shows an optional "Create my profile" action (first and last name prefilled from the sign-in); the first save creates the record. Nothing else on the platform depends on it, and the global bar is not shown to a person without a record. | Must |
| REQ-TEN-011.7 | **Names and the login:** first and last name are required and never blank. A change of name is mirrored to Keycloak; if Keycloak fails the whole save is rolled back and answered with 502. `account.nameEditable` lets the screen show the names read-only when they are managed by an external identity provider (true for everyone in this version, BR-PRF-016). | Must |
| REQ-TEN-011.8 | **Email and role are read-only** in this version. **Language and time zone** stay on [Preferences](../../../05-ui/screen-requirements/preferences.md); the page links to it and does not duplicate it. | Must |
| REQ-TEN-011.9 | **Completion:** 11 equally weighted items (photo, mobile, job title, department, company name, industry, address line 1, city, state, postal code, country), one shared computation, returned with the profile and by a light endpoint. The directory's own completion (BR-DIR-004/005) is unchanged. | Must |
| REQ-TEN-011.10 | **Global profile-filling bar:** under the app bar on every page of the signed-in tool while the person has a record and the profile is under 100%: progress, "Your profile is {percent}% complete", a "Complete profile" button and a dismiss control (kept for the browser session). Hidden on the profile page. Refreshed after a save, a photo change or removal, without polling. | Must |
| REQ-TEN-011.11 | **Header card:** avatar with change and remove buttons, display name (preferred name when set), role chip, organization, the completion bar with "n of 11 complete" and an expandable list of missing items that jump to the right tab. | Must |
| REQ-TEN-011.12 | **Platform administrator edits a customer record:** `GET` and `PUT /admin/registrations/individuals/{customerId}/profile` with the same body and validation (no photo), protected by `MANAGE_REGISTRATIONS`. Reached from an "Edit profile" dialog on the organization detail members list and the individuals list. Creating records for other people stays with registration and invitation. | Must |
| REQ-TEN-011.13 | **Audit:** profile created, updated, photo updated, photo removed, and updated by an administrator, naming the sections changed and never the values. | Must |
| REQ-TEN-011.14 | **Text and accessibility:** all text through i18n in English and Spanish (`profile.*`, `appShell.nav.profile`, `profile.bar.*`); labels, `aria-invalid`, helper text, keyboard-operable tabs, a progress bar with an accessible name. | Must |
| REQ-TEN-011.15 | **Unsaved changes:** leaving the page with unsaved changes asks for confirmation; Discard restores the saved values. | Should |

## Out of scope (this version)
Changing the email address; changing language or time zone here (Preferences keeps them); an organization administrator editing a member's profile; a platform administrator creating a record for **another** person (registration and invitation do that); photo upload for another person; moving the photo to S3 (decision KC-3 is open); making the profile mandatory or blocking sign-in or any page until it is complete; changing the directory completion (BR-DIR-004/005); profile history or versioning; public or shared profile cards; profile fields for organizations (they stay on the organization edit screens).

## Dependencies
Registration and the `customer` table (REQ-TEN-001), member lifecycle (REQ-TEN-002), organizations directory (REQ-TEN-007, whose completion is deliberately separate), Keycloak admin client, audit, the application layout ([application-layout.md](../../../05-ui/screen-requirements/application-layout.md)) and Preferences. Federated sign-in ([saml-federation](../saml-federation/requirement.md), [oidc-federation](../oidc-federation/requirement.md)) for the name-ownership rule (BR-PRF-016: sign-in creates names once and never overwrites them).
