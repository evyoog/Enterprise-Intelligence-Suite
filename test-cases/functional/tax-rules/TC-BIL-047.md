# TC-BIL-047: Transient provider failures are retried with the same idempotency key; permanent failures are not

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-047 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.35) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-34](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
External mode On, attempts per calculation 3. A provider (or stub) that can be told to fail.

## Steps
1. Make the provider fail twice (HTTP 503 and a timeout) and then answer; generate an invoice.
2. Make the provider return HTTP 429, then 5xx, then a network error in separate runs.
3. Make the provider answer HTTP 400, then HTTP 401.

## Expected Result
Step 1: three attempts, the invoice is finalized, the same transaction reference/idempotency key is used on every attempt, with waits of 250 ms then 500 ms. Step 2: all are transient and retried. Step 3: a permanent failure is not retried: the invoice is blocked with `PROVIDER_REJECTED` after one call; HTTP 401/403 report that the credentials were refused. The attempts maximum is 5.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/ExternalTaxEngineTest.java` — `transientFailuresAreRetriedWithGrowingWaitsAndTheSameKey`, `aPermanentFailureIsNotRetried`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/provider/GenericRestTaxProviderTest.java` — `serverErrorsAndRateLimitsAreTransient`, `clientErrorsArePermanentAndAuthFailuresSayTheCredentialsWereRefused`, `anUnreachableProviderIsATransientFailure`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
