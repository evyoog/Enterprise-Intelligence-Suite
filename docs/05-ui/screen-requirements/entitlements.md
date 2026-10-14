# Screen: Entitlements (customer section and admin lookup)

| Field | Value |
|---|---|
| Requirement | [REQ-SUB-002](../../02-requirements/FRD/entitlements/requirement.md) (Approved, [C97](../../01-business/roadmap/open-decisions.md#c97) EN-1, EN-3, EN-5) |
| Business rules | [BR-1 to BR-11](../../02-requirements/FRD/entitlements/business-rules.md) |
| API | [entitlements.md](../../06-api/api-requirements/entitlements.md) — `GET /me/entitlements`, `GET /admin/entitlements/customers/{id}`, `GET /admin/entitlements/organizations/{id}` |
| Built | 2026-10-13 — `frontend/src/components/subscriptions/EntitlementsSection.tsx`, `frontend/src/pages/admin/AdminEntitlementsPage.tsx`, `frontend/src/api/registrationApi.ts` (`entitlementsApi`) |
| Supersedes | [plan-entitlements.md](plan-entitlements.md) (the first draft: a card with a status chip and a usage-limit note) |

Shared presentation: the unified MUI theme ([C45](../../01-business/roadmap/open-decisions.md#c45)), light and dark mode, platform date format. Both places use the same read-only **entitlement card**.

## Entitlement card
One card per ACTIVE subscription. Read-only: no button, switch or field on it changes anything ([C97](../../01-business/roadmap/open-decisions.md#c97) EN-1).

| Element | Content | i18n key |
|---|---|---|
| Product name | Heading level 3 | — (from the catalogue) |
| Owner chip | **Personal** (individual subscription) or **Organization** (an organization subscription the member has access to), outlined | `entitlements.owner.INDIVIDUAL` / `.ORGANIZATION` |
| Plan chip | The plan name, primary, outlined; hidden when the subscription has no plan | — |
| Term line | "Active until {date}" (term end, platform date format) or "No end date"; then " · " and either "Limit: {number}" (thousands separators) or "No limit set" | `entitlements.until`, `entitlements.noEnd`, `entitlements.limit`, `entitlements.noLimit` |
| Included features | One chip per entry of the plan's `includedFeatures`, in a list labelled "Included features"; nothing is shown when the plan lists none | `entitlements.features` |
| **Read-only chip** ([C100](../../01-business/roadmap/open-decisions.md#c100) RR-1 B, [REQ-SUB-002.10](../../02-requirements/FRD/entitlements/requirement.md)) | A warning chip with a lock icon, **"Read-only until {date}"**, next to the plan chip, only while `accessMode` is `READ_ONLY` and `graceEndsAt` is set (the term has ended with auto-renew off and the 7-day grace period runs). A full-access entitlement shows none. The "Active until {date}" line still shows the term end | `entitlements.readOnlyUntil` |

The limit is **the plan's limit only** ([C97](../../01-business/roadmap/open-decisions.md#c97) EN-5). There is no progress bar, percentage or "used" figure anywhere, and no note about usage tracking on the card; each product counts its own usage.

A SUSPENDED, CANCELLED, EXPIRED or still-pending subscription grants nothing (an ACTIVE subscription in its read-only grace period still has a card, with the read-only chip) ([BR-1](../../02-requirements/FRD/entitlements/business-rules.md)), so it has **no card**. It still appears in the subscriptions list above with its status ([my-subscriptions.md](my-subscriptions.md)).

## Section on My subscriptions
| Field | Value |
|---|---|
| Placement | A section at the bottom of the existing **My subscriptions** screen (`/my/subscriptions`), below the subscription list and the Organization subscriptions section. No new screen, per the [C44](../../01-business/roadmap/open-decisions.md#c44) rule |
| Shown to | Any signed-in customer. Their own ACTIVE subscriptions, plus, for an ACTIVE organization member, the organization's ACTIVE subscriptions to products the member has access to |
| Section header | Shield-check icon (emerald), title **"What your plans include"** (`entitlements.title`), description "Read-only: this comes from your active subscriptions and their plans. Usage is counted by each product." (`entitlements.body`) |

| State | Behaviour |
|---|---|
| Loading | A rounded skeleton, 80 px high |
| Loaded, none | "You have no active subscriptions." (`entitlements.none`) |
| Loaded | A grid of entitlement cards, one per subscription |
| Error | An error alert: the backend's message, or "Could not load your entitlements." (`entitlements.loadError`); the rest of the page is unaffected |

## Admin Entitlements lookup
| Field | Value |
|---|---|
| Route | `/admin/entitlements` |
| Sidebar | Admin → Operations → **Entitlements lookup** (`nav` key `entitlementsLookup`), shown only to holders of `VIEW_ENTITLEMENTS` ("View entitlements", platform permission). Others: link hidden, API returns 403 |
| Purpose | Support: see what a customer or an organization is entitled to now. Read-only. Every lookup is recorded in the audit log as `ENTITLEMENTS_VIEWED` |
| Header | Shield-check icon (emerald), title **"Entitlements lookup"**, subtitle "Read-only: what a customer or organization is entitled to now. Each lookup is recorded in the audit log." |

### Form
| Field | Type | Required | Validation | Message (i18n key) |
|---|---|---|---|---|
| Look up | Select: **Customer** or **Organization** (default Customer) | Yes | — | `entitlements.admin.kind`, `.customer`, `.organization` |
| ID | Text, numeric keyboard, max 12 characters | Yes | Digits only, 1 to 12 | helper "The numeric ID"; error "Enter a number" (`entitlements.admin.idHelp`, `.idInvalid`) |
| **Look up** | Submit button | — | Disabled until the ID is valid and while a request is running | `entitlements.admin.lookUp` |

Pressing Enter in the ID field submits. There is no name search; the administrator needs the numeric ID (a search by name or email is **Not built**).

### Results (below the form, announced with `aria-live="polite"`)
| Result | Content |
|---|---|
| Entitlements | The same entitlement cards as above (no status chip: only ACTIVE subscriptions are returned) |
| None | "Nothing is active for this customer or organization." (`entitlements.admin.none`) |
| Unknown ID (404) | Error alert "No customer or organization has that ID." (`entitlements.admin.notFound`) |
| Any other error | The backend's message, or "Could not load your entitlements." |

## Accessibility and i18n
Visible labels; the ID error is in helper text; the results region is a live region; the card feature list has `role="list"` with an accessible name; axe test on the lookup page. All strings are in `frontend/src/i18n/locales/{en,es}.json` under `entitlements.*`.

## Not built
- A customer-facing organization-wide entitlements view: `GET /organization/me/entitlements` exists in the API, but no screen calls it yet.
- A name or email search on the admin lookup.
