# Acceptance criteria — OIDC Identity-Provider Federation

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** an organization administrator **When** they create an OIDC provider **Then** it is listed for their organization and is disabled until enabled | TC-IAM-<NNN> (not created) |
| AC-2 | **Given** an enabled OIDC provider **When** a member of that organization signs in through it **Then** they are signed in as a `MEMBER` of the organization | TC-IAM-<NNN> (not created) |
| AC-3 | **Given** a saved provider **Then** its client secret is stored encrypted and is not in any configuration file or source code | TC-IAM-<NNN> (not created) |
| AC-4 | **Given** the OIDC provider views are shown **Then** their text exists in `en.json` and `es.json`, they are keyboard operable, and a jest-axe check reports no violations | TC-IAM-<NNN> (not created) |
