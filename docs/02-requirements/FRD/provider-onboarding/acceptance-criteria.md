# Acceptance criteria — Provider Onboarding

Each criterion maps to at least one test case in `test-cases/functional/provider-onboarding/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** anyone with no Vyoog account **When** they apply to become a provider **Then** a new REGISTERED provider is created | TC-PTR-001 |
| AC-2 | **Given** a REGISTERED provider **When** an admin verifies, then approves, then activates it in order **Then** it moves through VERIFIED → APPROVED → ACTIVE, one stage at a time | TC-PTR-002 |
| AC-3 | **Given** a REGISTERED provider **When** an admin tries to approve or activate it directly, skipping Verify **Then** the request is refused (400) | TC-PTR-003 |
| AC-4 | **Given** an ACTIVE provider **When** an admin tries to reject it **Then** the request is refused (400); **given** an already-REJECTED provider, rejecting it again is also refused | TC-PTR-004 |
| AC-5 | **Given** a provider with no contract yet **When** an admin creates one **Then** it is ACTIVE with the given terms and dates; **given** an existing contract **When** the admin saves again **Then** the same row is edited, not a new one | TC-PTR-005 |
| AC-6 | **Given** a contract whose end date has passed **When** the scheduled expiry job runs **Then** its status flips to EXPIRED, and other contracts are unaffected | TC-PTR-006 |
