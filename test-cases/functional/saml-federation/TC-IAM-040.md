# TC-IAM-040: SAML Federation — Edit Provider — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-040 |
| Requirement ID (required) | [REQ-IAM-005](../../../docs/02-requirements/FRD/saml-federation/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/saml-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-005](TESTPLAN-IAM-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A provider id from another organization.

## Steps
1. Arrange: a provider id from another organization.
2. Act: it is edited.
3. Observe the response and the UI.

## Expected Result
The response is 403 "You do not have permission to do this".

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/federation/service/SamlProviderServiceTest.java` — "cannotAccessAnotherOrganizationsProvider"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
