# TC-MKT-069: Regression: the renewal reminders skip trials

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-069 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-21](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-012](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P1 |
| Type | Regression |
| Automated | No |

## Preconditions
A running trial and a normal auto-renewing subscription, both ending within the reminder window (for example in 2 days), each with a recipient whose reminders are on and whose local send time has passed.

## Steps
1. Run the renewal reminder job (`RenewalReminderService#sendDue`, every 15 minutes) once.
2. Read the notifications and the reminder log (`renewal_reminder_log`) of both owners.

## Expected Result
The normal subscription's owner gets the renewal reminder and a log row; the trial's owner gets **no** renewal reminder and no log row (they get "Your free trial of {product} ends soon" from the trial job instead).

## Automated coverage
None. This case is manual only.

Manual only: no automated test puts a trial through `RenewalReminderService#sendDue`. The skip is one `if (subscription.isTrial()) continue;` in the candidate loop. The existing reminder tests (`RenewalAndRemindersTest`, `SeatsGraceAndRecipientsTest`) cover normal subscriptions and still pass in the full backend run. **Not Run** by hand.

## Actual Result
Not run.

## Status
Not Run

## Linked Defect (if failed)
None.
