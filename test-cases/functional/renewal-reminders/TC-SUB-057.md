# TC-SUB-057: The auto-renew switch, the grace notice and the recipients section are accessible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-057 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C100) |
| Acceptance Criterion | [AC-20](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Business rule(s) | [BR-11](../../../docs/02-requirements/FRD/renewal-reminders/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P2 |
| Type | Accessibility |
| Automated | Yes |

## Preconditions
My subscriptions with an individual card in grace, an organization card and the recipients section.

## Steps
1. Run axe on the page with a grace banner and the auto-renew switch.
2. Run axe on the organization section and on the recipients section.
3. Tab to the switch and turn it off.

## Expected Result
No axe violations. The switch has an accessible name ("Auto-renew") and a hint it is described by; the confirmation dialog is labelled by its title; the checkboxes in the recipients section have names such as "Email for {name}".

## Automated coverage
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "has no detectable a11y violations with a grace banner and the auto-renew switch"
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "has no detectable a11y violations"
- `frontend/src/components/subscriptions/RenewalRecipientsSection.test.tsx` — "has no detectable a11y violations"

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
