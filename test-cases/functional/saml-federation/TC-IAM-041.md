# TC-IAM-041: SAML Federation — Edit Provider — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-041 |
| Requirement ID (required) | [REQ-IAM-005](../../../docs/02-requirements/FRD/saml-federation/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/saml-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-005](TESTPLAN-IAM-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The edit dialog is shown.

## Steps
1. Arrange: the edit dialog is shown.
2. Observe the response and the UI.

## Expected Result
Its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations.

## Automated coverage
- `frontend/src/pages/OrganizationSamlProvidersPage.test.tsx` — "has no detectable a11y violations with the edit form open"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
