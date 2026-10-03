# Screen section: Subscription seats

| Field | Value |
|---|---|
| Requirement | [REQ-SUB-003](../../02-requirements/FRD/subscription-seats/requirement.md) |
| Placement | **My subscriptions** (`/my/subscriptions`) → new **Organization subscriptions** section, one card per organization subscription. No organization subscription detail screen existed; per [C44](../../01-business/roadmap/open-decisions.md#c44) the seats live on the existing subscriptions page rather than a new screen |
| Shown to | Members with `MANAGE_ORGANIZATION` |
| Built | 2026-10-03 — `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.tsx` |

## Card
- Product name, plan, status chip, auto-renew chip and renewal date ([REQ-SUB-004](renewal-reminders.md)).
- **Seats** line: "Seats: 12 in use of 15" with a progress bar (colour turns warning at 90 %).
- Quantity stepper: − / number field / +; minimum = seats in use (shown "Minimum 12 — seats in use"); maximum 100 000. **Save** (enabled when changed).
- Confirmation dialog: "Change seats from 15 to 20?" — "This takes effect immediately. Billing for seat changes is not set up yet, so nothing is charged or credited now." **Change seats** / **Cancel**.
- Success toast "Seats updated."; a refusal shows the backend message.

## Accessibility and i18n
The stepper buttons have labels ("Remove a seat", "Add a seat"); progress bar has an accessible name; dialog labelled; axe test. Strings under `subscriptions.seats.*`.
