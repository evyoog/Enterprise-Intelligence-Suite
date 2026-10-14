# TC-SRM-005: Security: the open report path rejects a missing, revoked or other product's key

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-005 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-005, BR-SRM-006](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
Two products P and Q, each with a reporter key; an instance of P; P's key revoked in step 3.

## Steps
1. Report for P's instance with no `X-Reporter-Key`, then with the made-up key `eisrk_nope`.
2. Report for P's instance with Q's valid key.
3. Revoke P's key and report with it.
4. Over HTTP (curl or the dev environment): `POST /api/product-reports/health` with no token and no key, then with a revoked key.

## Expected Result
Steps 1 and 3: refused as an invalid-credentials error (HTTP 401 "Invalid or revoked reporter key.") and nothing is stored. Step 2: the call is accepted as a call but the report is rejected ("unknown instance for this product"; accepted = 0, one error) and P's instance is unchanged. Step 4: 401 in both cases; the path needs no user token but is useless without a valid key.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `aMissingRevokedOrOtherProductsKeyIsRejected`
- `backend/src/test/java/com/vyoog/eisplatform/config/ServiceStatusSlaAuthorizationTest.java` — `theProductReportEndpointNeedsNoTokenButRejectsAMissingOrUnknownKey`

The HTTP mapping is covered: the open path answers 401 for a missing or unknown key.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
