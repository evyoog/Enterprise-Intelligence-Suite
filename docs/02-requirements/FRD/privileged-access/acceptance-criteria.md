# Acceptance criteria — Privileged Access (User and Organization Administrator)

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** a signed-in organization member **When** they request `MANAGE_USERS` for 60 minutes with a justification **Then** the request appears in "My requests" as PENDING | TC-IAM-<NNN> (not created) |
| AC-2 | **Given** a request with a duration of 0 or 481 minutes **When** it is submitted **Then** the response is 400 and the UI shows the backend message | TC-IAM-<NNN> (not created) |
| AC-3 | **Given** a request for `MANAGE_PRIVILEGED_ACCESS` **When** it is submitted **Then** the response is 400 and no request is created | TC-IAM-<NNN> (not created) |
| AC-4 | **Given** an organization administrator **When** they open approvals **Then** only PENDING ORGANIZATION-scope requests of their own organization are listed | TC-IAM-<NNN> (not created) |
| AC-5 | **Given** an administrator reviewing their own request **When** they approve or reject it **Then** the response is 403 | TC-IAM-<NNN> (not created) |
| AC-6 | **Given** a pending request **When** the administrator approves it **Then** its status is APPROVED, its expiry is approval time plus the requested duration, and the requester is notified | TC-IAM-<NNN> (not created) |
| AC-7 | **Given** a request already approved or rejected **When** approve or reject is attempted again **Then** the response is 400 "This request has already been decided." | TC-IAM-<NNN> (not created) |
| AC-8 | **Given** an approved grant past its expiry **Then** "My requests" shows it as EXPIRED | TC-IAM-<NNN> (not created) |
| AC-9 | **Given** the requester's own PENDING or active request **When** they withdraw it **Then** its status is REVOKED | TC-IAM-<NNN> (not created) |
| AC-10 | **Given** a signed-in user **When** they open the request form **Then** the permission dropdown lists only permissions that some role grants, never `MANAGE_PRIVILEGED_ACCESS` | TC-IAM-<NNN> (not created) |
| AC-11 | **Given** a user who is not an active organization member **When** they open the request form **Then** no ORGANIZATION-scope permission is listed | TC-IAM-<NNN> (not created) |
| AC-12 | **Given** the request and approval views are shown **Then** their text exists in `en.json` and `es.json`, they are keyboard operable, and a jest-axe check reports no violations | TC-IAM-<NNN> (not created) |
