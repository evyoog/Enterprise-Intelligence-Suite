# TC-SUP-030: Security: customers never see the internal incident fields

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-030 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-002](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
An incident with severity, internal notes and an assignee for a product; customer X purchased the product, customer Y did not.

## Steps
1. X and Y read `GET /me/service-status`.
2. Inspect the fields of the customer incident record.

## Expected Result
X sees the incident's title, message and times; Y sees none; neither receives severity, phase, internal notes, assignee, resolution summary, tickets or problem.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/ServiceStatusServiceTest.java` — `everySignedInCustomerSeesEveryProductsStatusButIncidentDetailsOnlyForPurchasedOnes`
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `aNewIncidentIsLoggedAndTheInternalFieldsStayOffTheStatusPage`

The absence of internal fields is asserted on the record type; no test reads the JSON of `/me/service-status` for an incident with internal data.

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
