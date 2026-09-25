# Acceptance criteria — Organization MFA Policy

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** a caller with `MANAGE_ORGANIZATION` **When** they turn the MFA requirement on **Then** `GET /organization/me` returns `mfaRequired: true` and an `MFA_POLICY_CHANGED` audit record exists | [TC-IAM-001](../../../../test-cases/functional/organization-mfa-policy/TC-IAM-001.md) |
| AC-2 | **Given** a caller with `MANAGE_ORGANIZATION` **When** they turn the MFA requirement off **Then** `GET /organization/me` returns `mfaRequired: false` | [TC-IAM-002](../../../../test-cases/functional/organization-mfa-policy/TC-IAM-002.md) |
| AC-3 | **Given** an organization member without `MANAGE_ORGANIZATION` **When** they call `PATCH /organization/me/mfa-policy` **Then** the response is 403 "You do not have permission to do this" and the UI shows that message | [TC-IAM-003](../../../../test-cases/functional/organization-mfa-policy/TC-IAM-003.md) |
| AC-4 | **Given** an organization that requires MFA **When** a member signs in without a one-time password **Then** login is refused with 403 and `organizationMfaRequired: true` | [TC-IAM-004](../../../../test-cases/functional/organization-mfa-policy/TC-IAM-004.md) |
| AC-5 | **Given** members are signed in **When** the requirement is turned on **Then** no existing session is ended | [TC-IAM-005](../../../../test-cases/functional/organization-mfa-policy/TC-IAM-005.md) |
| AC-6 | **Given** the MFA setting is shown **Then** its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations | [TC-IAM-006](../../../../test-cases/functional/organization-mfa-policy/TC-IAM-006.md) |
