# TC-IAM-037: SAML Federation — Edit Provider — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-037 |
| Requirement ID (required) | [REQ-IAM-005](../../../docs/02-requirements/FRD/saml-federation/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/saml-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-005](TESTPLAN-IAM-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Manual details with an invalid SSO URL or certificate.

## Steps
1. Arrange: manual details with an invalid SSO URL or certificate.
2. Act: saved.
3. Observe the response and the UI.

## Expected Result
The response is 400 and the dialog shows the backend message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/federation/service/SamlProviderServiceTest.java` — "rejectsManualFieldsWithAnUnparseableCertificate"
- `frontend/src/pages/OrganizationSamlProvidersPage.test.tsx` — "keeps the form open and shows the backend message when the update is refused"

**Manual check:** The backend certificate test uses the create path, which shares the validation; check it through edit too.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
