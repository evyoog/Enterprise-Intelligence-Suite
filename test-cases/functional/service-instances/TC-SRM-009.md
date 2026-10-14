# TC-SRM-009: A platform administrator changes an instance by hand, with a note

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-009 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-009](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An instance; a platform administrator.

## Steps
1. Change status FAILED with a blank note; then change with a note but no status or health.
2. Set status ACTIVE and health DOWN with the note "Product cannot report".
3. On the screen, open Change by hand, try Save with nothing changed and with no note, then change one field and save.

## Expected Result
Step 1: both refused (400). Step 2: status ACTIVE, health DOWN, last changed by ADMIN, not stale, and the derived status for the product is Major outage. Step 3: "Change the status or the health first." and "Enter a note."; the request sends only what changed; "Instance updated."

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `anAdminChangesAnInstanceByHandWithANoteAndItCountsAsCurrent`
- `frontend/src/pages/admin/AdminServiceInstancesPage.test.tsx` — "requires a note before changing an instance by hand, then sends only what changed", "asks for a change when nothing was changed and shows what the server refuses"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
