# Screen section: Subscription seats

| Field | Value |
|---|---|
| Requirement | [REQ-SUB-003](../../02-requirements/FRD/subscription-seats/requirement.md) (changed by [C100](../../01-business/roadmap/open-decisions.md#c100) SE-1 to SE-4) |
| Placement | **My subscriptions** (`/my/subscriptions`) → **Organization subscriptions** section, one card per organization subscription. No organization subscription detail screen existed; per [C44](../../01-business/roadmap/open-decisions.md#c44) the seats live on the existing subscriptions page rather than a new screen |
| Shown to | Members with **Manage billing** (`MANAGE_ORGANIZATION_BILLING`), who can change seats, and members with **Manage subscriptions** (`MANAGE_SUBSCRIPTIONS`), who see the cards read-only. Everyone else: the section is hidden (the API refuses the list) |
| Built | 2026-10-03; changed 2026-10-14 — `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.tsx` (seats, auto-renew, grace notice), `RenewalRecipientsSection.tsx` (below it), `AutoRenewControl.tsx`, `GraceNotice.tsx` |

## Section
Header: building icon, title **"Organization subscriptions"** (`subscriptions.seats.sectionTitle`), description "Seats are shared by your organization's active members. An increase takes effect now and is charged for the rest of the term. A decrease takes effect at your next renewal." The [reminder recipients section](renewal-reminders.md#4-renewal-reminder-recipients-organization) follows it for Manage billing holders.

## Card (one per organization subscription)
- Product name, plan, status chip, **Auto-renew on** chip and **Renews on {date}** chip (replaced by **Read-only until {date}** while in grace, see [renewal-reminders.md](renewal-reminders.md)).
- A grace notice (warning alert) while the term has ended with auto-renew off; and, for an ACTIVE subscription with a renewal date, the **Auto-renew** switch (see [renewal-reminders.md](renewal-reminders.md)). Seats **cannot be changed** while the subscription is read-only: "Seats cannot be changed while the subscription is read-only. Renew it first." (`subscriptions.seats.readOnly`).
- **Seats** line: "Seats: 12 in use of 15" with a progress bar (colour turns warning at 90 %). Seats in use are named seats per product ([C90](../../01-business/roadmap/open-decisions.md#c90)).
- **Pending decrease** (info alert, only while `pendingQuantity` is set): **"Falls to {n} seats on {date}. Nothing is credited."** (or "…at your next renewal…" without a date) with a **Withdraw** button for Manage billing holders. Withdraw asks for the current quantity again.
- Seat control for **Manage billing** holders: − / number field "Seats" / +, minimum = seats in use ("Minimum 12 — seats in use"), maximum 100 000, **Save** (enabled when the value is valid and changed). For others: "Only people with the Manage billing permission can change seats." (`subscriptions.seats.viewOnly`) and no controls. A subscription that cannot be changed (not ACTIVE or SUSPENDED) shows "Seats can be changed on active or suspended subscriptions."
- **Estimate** (live, only for an increase): **"Charged now for {days} of {term} days: {amount}"** and "Estimate for {n} added seats. The invoice shows the exact amount, with tax." The amount is added seats × price of one seat × remaining days ÷ term days from the seat summary; the server's invoice is the authority.
- **Decrease note** (only for a decrease): "The decrease takes effect on {date}. Nothing is credited."
- **Confirmation dialog** on Save: title "Change seats from {from} to {to}?". Increase: "The added seats take effect now and are charged on a new invoice." plus the estimate. Decrease: "You keep all {n} seats until your next renewal and nothing is credited. You can withdraw the decrease before then." Buttons **Change seats** / **Cancel**.
- **Result** (success alert, dismissible), by the server's `effect`: increase with an invoice "Seats increased to {n}. An invoice was created for the added seats." with a **View invoices** link (to the organization billing invoices tab); increase without an invoice "Seats changed to {n}."; scheduled "Seats will fall to {n} on {date}. Nothing is credited."; withdrawn "The scheduled decrease was withdrawn. You keep {n} seats."; unchanged "Seats are unchanged." A refusal shows the backend message (for example "3 seats are in use. Remove members before reducing seats.").

## Accessibility and i18n
The stepper buttons have labels ("Remove a seat", "Add a seat"); the progress bar has an accessible name; the dialog is labelled; the estimate and the decrease note are `aria-live="polite"`; axe test. Strings under `subscriptions.seats.*`, `subscriptions.autoRenew.*`, `subscriptions.grace.*` (en and es).

## API used
[subscription-seats.md](../../06-api/api-requirements/subscription-seats.md): `GET /organization/me/subscriptions`, `GET` and `PATCH /organization/me/subscriptions/{id}/seats`, `PATCH /organization/me/subscriptions/{id}/auto-renew`.
