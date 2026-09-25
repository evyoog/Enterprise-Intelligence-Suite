# TC-IAM-038: SAML Federation — Edit Provider — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-038 |
| Requirement ID (required) | [REQ-IAM-005](../../../docs/02-requirements/FRD/saml-federation/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/saml-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-005](TESTPLAN-IAM-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | No |

## Preconditions
Metadata XML without a signing certificate.

## Steps
1. Arrange: metadata XML without a signing certificate.
2. Act: saved.
3. Observe the response and the UI.

## Expected Result
The response is 400 "Could not find an IdP signing certificate in the provided metadata".

## Automated coverage
- None

**Manual check:** Edit with metadata XML that has no signing certificate and check the 400 message.

## Actual Result
Not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
