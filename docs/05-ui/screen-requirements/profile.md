# Screen: My profile

**Requirement:** [REQ-TEN-011](../../02-requirements/FRD/my-profile/requirement.md) ([C107](../../01-business/roadmap/open-decisions.md#c107), [C108](../../01-business/roadmap/open-decisions.md#c108))

| Field | Value |
|---|---|
| Route | `/account/profile` (`RequireAuth`; every signed-in person) |
| Code | `frontend/src/pages/ProfilePage.tsx`, `frontend/src/api/profileApi.ts`, `frontend/src/utils/profileValidation.ts`, `components/layout/ProfileCompletionBar` (global bar), admin dialog "Edit profile" |
| Related | [preferences.md](preferences.md) (language and time zone), [application-layout.md](application-layout.md) (profile menu and global bar), [design-system.md](design-system.md) |

## 1. Header card
- **Avatar** with the photo, or the initials when there is none. Buttons *Change photo* and *Remove photo* (Remove only when a photo exists). The browser checks the type (JPEG, PNG, WebP) first, cuts a centred square, scales it to at most 512 x 512 (never enlarging a smaller picture), encodes it as WebP (JPEG when the browser cannot write WebP) at a quality that fits within 1 MB, and uploads it; a refusal from the server is shown under the avatar.
- **Display name** (the preferred name when set, otherwise first and last name), a **role chip** (Platform administrator, Organization administrator, Member, Individual) and the **organization name** when there is one.
- **Completion:** a progress bar with "{filled} of 11 complete" and the percentage; an expandable list of the missing items, each of which opens the tab that holds it and focuses the field.

## 2. Tabs (MUI Tabs: Personal | Work | Address | Online | Account)
One form state for the first four tabs and one sticky **Save** and **Discard** bar. A tab label shows an error badge (with an accessible name such as "Personal, 2 errors") when a field in it is invalid. Rules are those of BR-PRF-005 to BR-PRF-011; the same validators live in `profileValidation.ts`.

| Tab | Field | Required | Rule |
|---|---|---|---|
| Personal | First name | Yes | 1 to 100, letters, marks, space, `. ' ’ -` |
| Personal | Last name | Yes | same |
| Personal | Preferred name | No | up to 60, same pattern; used as the display name |
| Personal | Mobile | No | typed freely, normalised to E.164 (`+` then 7 to 15 digits); spaces, hyphens, dots and parentheses are accepted and removed |
| Personal | Alternate phone | No | same |
| Personal | Bio | No | up to 500, text area with a character counter; line breaks become spaces when saved |
| Work | Job title | No | up to 100 |
| Work | Department | No | up to 100 |
| Work | Company name | No | up to 150 |
| Work | Industry | No | up to 100 (free text) |
| Address | Address line 1, line 2 | No | up to 150 each |
| Address | City, State | No | up to 80 each |
| Address | Postal code | No | up to 12, letters, digits, space, hyphen; exactly 6 digits when the country is India |
| Address | Country | No | up to 80 (free text) |
| Online | LinkedIn | No | `https` only, `linkedin.com` or a subdomain, up to 200 |
| Online | Website | No | `http` or `https`, valid host, up to 200 |
| Account (read-only) | Email, role, organization name and code, membership status, member since | - | Not editable; the sign-in method is not shown in this version. Links: *Security* (`/account/security`), *Preferences* (`/account/preferences`, for language and time zone), and for an organization administrator *People & structure* (`/organization/members`) and the business dashboard |

- Text is trimmed and inner whitespace collapsed on blur and on save; `<`, `>` and control characters show "These characters are not allowed."
- Inputs have visible labels, helper text for the format and `aria-invalid` with the message in the helper text when invalid. Server `fieldErrors` (`personal.firstName`) are put on the matching field and the tab badge, and the first invalid tab is opened.
- When `account.nameEditable` is false the two name fields are read-only with an explanation (true for every person in this version, BR-PRF-016).
- **Unsaved changes:** navigating away (links, the menu, browser back) with changes asks for confirmation; *Discard* restores the saved values. A success toast "Profile saved." follows a save ("Your profile has been created." for the first save of a platform administrator). A 502 shows "Your name could not be updated at the sign-in service, so nothing was saved. Please try again later." and keeps the typed values; server field errors show "Some fields need your attention. They are marked on their tabs."

## 3. States
| State | Behaviour |
|---|---|
| Loading | Skeleton of the header card and the form |
| Error loading | Alert with a Retry button |
| Platform administrator without a record (`exists` false): a holder of `MANAGE_REGISTRATIONS` or a login listed and enabled in the platform administrator registry ([C108](../../01-business/roadmap/open-decisions.md#c108), no frontend change: the server now answers `exists:false` for a listed login instead of 404) | A banner "Your customer profile has not been created. It is optional." and a **Create my profile** action; first and last name are prefilled from the sign-in; the completion bar shows 0 of 11; the photo button is disabled with the note "Create your profile to add a photo."; after the first save the page behaves as for everyone else. No other screen is blocked and the global bar is not shown |
| Person without a record who is not a platform administrator (not a permission holder and not listed in the registry) | The server answers 404; the page shows the load-error state "Could not load your profile." with the server's message and a Retry button |
| Saving | Save disabled with a progress indicator, the form is disabled until the answer |

## 4. Admin: Edit profile dialog
Opened from the organization detail members list and the individuals list (platform administrators with `MANAGE_REGISTRATIONS`). Title "Edit profile", the person's email as a read-only line, the same four tabs without the photo and without the Account tab, Save and Cancel. It calls `/admin/registrations/individuals/{customerId}/profile` and shows server field errors the same way.

## 5. Global bar
See [application-layout.md](application-layout.md#global-profile-completion-bar).

## i18n
`profile.*` (page, tabs, fields, helper texts, errors, banner, toasts, photo), `profile.bar.*` (global bar) and `appShell.nav.profile` in `en.json` and `es.json`.

## Accessibility
Tabs follow the ARIA tabs pattern (arrow keys, Home and End); the missing-items disclosure is a button with `aria-expanded`; progress bars have `role="progressbar"` with an accessible name and value; the photo buttons have names that say what they do; the page passes the automated accessibility check (jest-axe) in both languages.

## Tests
[TESTPLAN-TEN-011](../../../test-cases/functional/my-profile/TESTPLAN-TEN-011.md): `ProfilePage.test.tsx`, `ProfileCompletionBar.test.tsx`, `profileValidation.test.ts`, `AppShell.test.tsx`.
