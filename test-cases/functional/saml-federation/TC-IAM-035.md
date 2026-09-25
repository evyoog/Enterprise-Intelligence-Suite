# TC-IAM-035: SAML Federation — Edit Provider — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-035 |
| Requirement ID (required) | [REQ-IAM-005](../../../docs/02-requirements/FRD/saml-federation/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/saml-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-005](TESTPLAN-IAM-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An existing provider.

## Steps
1. Arrange: an existing provider.
2. Act: the administrator changes only its name.
3. Observe the response and the UI.

## Expected Result
The list shows the new name and the connection details are unchanged.

## Automated coverage
- `frontend/src/pages/OrganizationSamlProvidersPage.test.tsx` — "changes only the name, leaving the connection details untouched"

**Manual check:** Check the saved provider keeps its entity ID, SSO URL and certificate.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
