# TC-MKT-064: Security: compare is public and read-only; trials and demo requests need sign-in

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-064 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-007, BR-EVL-021](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
No token; a signed-in organization member without Manage billing.

## Steps
1. Call `GET /products/compare` with no token (valid and invalid items).
2. Call `POST /me/trials`, `POST /organization/me/trials` and `POST /me/demo-requests` with no token.
3. As the member without Manage billing call `POST /organization/me/trials`.
4. Check that the compare call changed nothing (no subscription, no ticket, no audit entry).

## Expected Result
Step 1: never 401 (400 or 404 for bad items, 200 for good ones). Step 2: 401 each. Step 3: 403. Step 4: nothing changed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/EvaluationAuthorizationTest.java` — `theComparePageIsOpenToEveryoneAndValidatesItsItems`, `trialsAndDemoRequestsNeedASignedInCustomer`
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `anOrganizationTrialNeedsAnAdminOrManageBillingAndUsesTheOrganizationsSeats`

`EvaluationAuthorizationTest` answers 400 and 404 for the public compare call and 401 for the three POST paths. **Not covered by an automated test:** a 200 compare over HTTP, a compare with a token (the tax estimate), and the read-only guarantee (it is a read-only transaction in `CompareService`).

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
