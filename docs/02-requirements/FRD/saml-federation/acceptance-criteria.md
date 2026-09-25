# Acceptance criteria — SAML Federation — Edit Provider

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** an existing provider **When** the administrator changes only its name **Then** the list shows the new name and the connection details are unchanged | TC-<APP-CODE>-<NNN> (not created) |
| AC-2 | **Given** an existing provider **When** the administrator saves valid entity id, SSO URL and certificate **Then** the provider shows the new values and a `SAML_PROVIDER_UPDATED` audit record exists | TC-<APP-CODE>-<NNN> (not created) |
| AC-3 | **Given** manual details with an invalid SSO URL or certificate **When** saved **Then** the response is 400 and the dialog shows the backend message | TC-<APP-CODE>-<NNN> (not created) |
| AC-4 | **Given** metadata XML without a signing certificate **When** saved **Then** the response is 400 "Could not find an IdP signing certificate in the provided metadata" | TC-<APP-CODE>-<NNN> (not created) |
| AC-5 | **Given** an enabled provider **When** it is edited **Then** it stays enabled | TC-<APP-CODE>-<NNN> (not created) |
| AC-6 | **Given** a provider id from another organization **When** it is edited **Then** the response is 403 "You do not have permission to do this" | TC-<APP-CODE>-<NNN> (not created) |
| AC-7 | **Given** the edit dialog is shown **Then** its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations | TC-<APP-CODE>-<NNN> (not created) |
