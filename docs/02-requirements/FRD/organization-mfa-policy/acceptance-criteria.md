# Acceptance criteria — Organization MFA Policy

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** a caller with `MANAGE_ORGANIZATION` **When** they turn the MFA requirement on **Then** `GET /organization/me` returns `mfaRequired: true` and an `MFA_POLICY_CHANGED` audit record exists | TC-IAM-<NNN> (not created) |
| AC-2 | **Given** a caller with `MANAGE_ORGANIZATION` **When** they turn the MFA requirement off **Then** `GET /organization/me` returns `mfaRequired: false` | TC-IAM-<NNN> (not created) |
| AC-3 | **Given** an organization member without `MANAGE_ORGANIZATION` **When** they call `PATCH /organization/me/mfa-policy` **Then** the response is 403 "You do not have permission to do this" and the UI shows that message | TC-IAM-<NNN> (not created) |
| AC-4 | **Given** an organization that requires MFA **When** a member signs in without a one-time password **Then** login is refused with 403 and `organizationMfaRequired: true` | TC-IAM-<NNN> (not created) |
| AC-5 | **Given** members are signed in **When** the requirement is turned on **Then** no existing session is ended | TC-IAM-<NNN> (not created) |
| AC-6 | **Given** the MFA setting is shown **Then** its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations | TC-IAM-<NNN> (not created) |
