# Acceptance criteria — Configurable Claim Mapping

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** an existing SAML provider with no custom mapping **When** a member signs in **Then** email, first name and last name are read exactly as today | TC-IAM-<NNN> (not created) |
| AC-2 | **Given** a provider with a custom email claim **When** a member signs in **Then** the email is read from that claim | TC-IAM-<NNN> (not created) |
| AC-3 | **Given** any mapping **When** a new federated user signs in **Then** they join as `MEMBER` | TC-IAM-<NNN> (not created) |
| AC-4 | **Given** the mapping fields are shown **Then** their text exists in `en.json` and `es.json`, they are keyboard operable, and a jest-axe check reports no violations | TC-IAM-<NNN> (not created) |
