# TC-IAM-036: SAML Federation — Edit Provider — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-036 |
| Requirement ID (required) | [REQ-IAM-005](../../../docs/02-requirements/FRD/saml-federation/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/saml-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-005](TESTPLAN-IAM-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An existing provider.

## Steps
1. Arrange: an existing provider.
2. Act: the administrator saves valid entity id, SSO URL and certificate.
3. Observe the response and the UI.

## Expected Result
The provider shows the new values and a `SAML_PROVIDER_UPDATED` audit record exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/federation/service/SamlProviderServiceTest.java` — "updatingWithNewManualFieldsReplacesTheOldConfiguration"
- `frontend/src/pages/OrganizationSamlProvidersPage.test.tsx` — "sends new manual details, starting from the current values"

**Manual check:** Check a `SAML_PROVIDER_UPDATED` audit record exists.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
