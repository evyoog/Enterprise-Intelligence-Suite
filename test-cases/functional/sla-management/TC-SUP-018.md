# TC-SUP-018: Security: only `MANAGE_SUPPORT_TICKETS` reads or changes the SLA settings

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-018 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-015](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A user with no permissions and a platform administrator.

## Steps
1. Call `GET /admin/support/sla`, `PUT /admin/support/sla/policies` and `PUT /admin/support/sla/settings` with no token.
2. Repeat as a user without `MANAGE_SUPPORT_TICKETS`.
3. Repeat as an administrator.

## Expected Result
401, then 403, then 200.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ServiceStatusSlaAuthorizationTest.java` — `slaSettingsNeedManageSupportTickets`

`/admin/support/sla` answers 401 without a token, 403 without the permission, 200 for a platform admin, and 400 for invalid settings.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
