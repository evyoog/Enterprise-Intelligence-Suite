# Acceptance criteria — Privileged Access (User and Organization Administrator)

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** a signed-in organization member **When** they request `MANAGE_USERS` for 60 minutes with a justification **Then** the request appears in "My requests" as PENDING | [TC-IAM-023](../../../../test-cases/functional/privileged-access/TC-IAM-023.md) |
| AC-2 | **Given** a request with a duration of 0 or 481 minutes **When** it is submitted **Then** the response is 400 and the UI shows the backend message | [TC-IAM-024](../../../../test-cases/functional/privileged-access/TC-IAM-024.md) |
| AC-3 | **Given** a request for `MANAGE_PRIVILEGED_ACCESS` **When** it is submitted **Then** the response is 400 and no request is created | [TC-IAM-025](../../../../test-cases/functional/privileged-access/TC-IAM-025.md) |
| AC-4 | **Given** an organization administrator **When** they open approvals **Then** only PENDING ORGANIZATION-scope requests of their own organization are listed | [TC-IAM-026](../../../../test-cases/functional/privileged-access/TC-IAM-026.md) |
| AC-5 | **Given** an administrator reviewing their own request **When** they approve or reject it **Then** the response is 403 | [TC-IAM-027](../../../../test-cases/functional/privileged-access/TC-IAM-027.md) |
| AC-6 | **Given** a pending request **When** the administrator approves it **Then** its status is APPROVED, its expiry is approval time plus the requested duration, and the requester is notified | [TC-IAM-028](../../../../test-cases/functional/privileged-access/TC-IAM-028.md) |
| AC-7 | **Given** a request already approved or rejected **When** approve or reject is attempted again **Then** the response is 400 "This request has already been decided." | [TC-IAM-029](../../../../test-cases/functional/privileged-access/TC-IAM-029.md) |
| AC-8 | **Given** an approved grant past its expiry **Then** "My requests" shows it as EXPIRED | [TC-IAM-030](../../../../test-cases/functional/privileged-access/TC-IAM-030.md) |
| AC-9 | **Given** the requester's own PENDING or active request **When** they withdraw it **Then** its status is REVOKED | [TC-IAM-031](../../../../test-cases/functional/privileged-access/TC-IAM-031.md) |
| AC-10 | **Given** a signed-in user **When** they open the request form **Then** the permission dropdown lists only permissions that some role grants, never `MANAGE_PRIVILEGED_ACCESS` | [TC-IAM-032](../../../../test-cases/functional/privileged-access/TC-IAM-032.md) |
| AC-11 | **Given** a user who is not an active organization member **When** they open the request form **Then** no ORGANIZATION-scope permission is listed | [TC-IAM-033](../../../../test-cases/functional/privileged-access/TC-IAM-033.md) |
| AC-12 | **Given** the request and approval views are shown **Then** their text exists in `en.json` and `es.json`, they are keyboard operable, and a jest-axe check reports no violations | [TC-IAM-034](../../../../test-cases/functional/privileged-access/TC-IAM-034.md) |
| AC-13 | **Given** an approved, unexpired ORGANIZATION-scope grant **When** the organization administrator opens the approvals card **Then** it is listed under Active grants with the requester's email and expiry, and not for another organization or the platform list | [TC-IAM-042](../../../../test-cases/functional/privileged-access/TC-IAM-042.md) |
| AC-14 | **Given** an active grant **When** an organization or platform administrator revokes it with a note **Then** it disappears from Active grants and no longer grants the permission; a grant that is no longer active is refused with the backend message | [TC-IAM-043](../../../../test-cases/functional/privileged-access/TC-IAM-043.md) |
| AC-15 | **Given** an approved PLATFORM-scope grant past its expiry **When** the platform administrator opens the page **Then** it is not listed as active | [TC-IAM-044](../../../../test-cases/functional/privileged-access/TC-IAM-044.md) |
