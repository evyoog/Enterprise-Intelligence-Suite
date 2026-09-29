# Acceptance criteria — Ticket Management

Each criterion maps to at least one test case in `test-cases/functional/ticket-management/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** any authenticated customer **When** they create a ticket **Then** it is OPEN with MEDIUM priority, and they can list/read it | TC-SUP-001 |
| AC-2 | **Given** a ticket owned by another customer **When** the caller tries to read it as their own **Then** the response is a generic 404 | TC-SUP-002 |
| AC-3 | **Given** an OPEN ticket **When** an admin assigns it **Then** it moves to IN_PROGRESS, and its category/priority are updated as given | TC-SUP-003 |
| AC-4 | **Given** any non-terminal ticket **When** an admin escalates it **Then** its status becomes ESCALATED and its priority becomes URGENT | TC-SUP-004 |
| AC-5 | **Given** a RESOLVED ticket **When** an admin closes it, but **Given** a non-RESOLVED ticket **When** an admin tries to close it **Then** only the RESOLVED case succeeds; the other is refused (400) | TC-SUP-005 |
| AC-6 | **Given** a RESOLVED or CLOSED ticket **When** any of categorize/prioritize/assign, escalate, or resolve is attempted again **Then** each is refused (400) | TC-SUP-005 |
