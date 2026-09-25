# Acceptance criteria — SAML Federation — Edit Provider

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** an existing provider **When** the administrator changes only its name **Then** the list shows the new name and the connection details are unchanged | [TC-IAM-035](../../../../test-cases/functional/saml-federation/TC-IAM-035.md) |
| AC-2 | **Given** an existing provider **When** the administrator saves valid entity id, SSO URL and certificate **Then** the provider shows the new values and a `SAML_PROVIDER_UPDATED` audit record exists | [TC-IAM-036](../../../../test-cases/functional/saml-federation/TC-IAM-036.md) |
| AC-3 | **Given** manual details with an invalid SSO URL or certificate **When** saved **Then** the response is 400 and the dialog shows the backend message | [TC-IAM-037](../../../../test-cases/functional/saml-federation/TC-IAM-037.md) |
| AC-4 | **Given** metadata XML without a signing certificate **When** saved **Then** the response is 400 "Could not find an IdP signing certificate in the provided metadata" | [TC-IAM-038](../../../../test-cases/functional/saml-federation/TC-IAM-038.md) |
| AC-5 | **Given** an enabled provider **When** it is edited **Then** it stays enabled | [TC-IAM-039](../../../../test-cases/functional/saml-federation/TC-IAM-039.md) |
| AC-6 | **Given** a provider id from another organization **When** it is edited **Then** the response is 403 "You do not have permission to do this" | [TC-IAM-040](../../../../test-cases/functional/saml-federation/TC-IAM-040.md) |
| AC-7 | **Given** the edit dialog is shown **Then** its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations | [TC-IAM-041](../../../../test-cases/functional/saml-federation/TC-IAM-041.md) |
