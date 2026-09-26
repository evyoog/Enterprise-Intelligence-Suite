# Acceptance criteria — MFA Recovery

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** an organization administrator and an enrolled member of their organization **When** the administrator resets the member's MFA **Then** the authenticator, recovery codes and held sign-ins are removed, the member is notified and an `MFA_RESET_BY_ADMIN` audit record exists | [TC-IAM-050](../../../../test-cases/functional/mfa-recovery/TC-IAM-050.md) |
| AC-2 | **Given** a member of another organization, or a caller without `MANAGE_USERS` **When** a reset is attempted **Then** it is refused with 403 and nothing changes | [TC-IAM-051](../../../../test-cases/functional/mfa-recovery/TC-IAM-051.md) |
| AC-3 | **Given** the administrator's own account, or a user with no authenticator **When** a reset is attempted **Then** it is refused with 400 and the dialog shows the backend message | [TC-IAM-052](../../../../test-cases/functional/mfa-recovery/TC-IAM-052.md) |
| AC-4 | **Given** a platform administrator **When** they reset an account's MFA by email **Then** it is reset (email match ignores case); an unknown email returns 404 | [TC-IAM-053](../../../../test-cases/functional/mfa-recovery/TC-IAM-053.md) |
| AC-5 | **Given** the reset dialog **When** it is open **Then** its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations | [TC-IAM-054](../../../../test-cases/functional/mfa-recovery/TC-IAM-054.md) |
