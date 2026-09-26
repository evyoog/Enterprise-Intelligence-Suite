# Acceptance criteria — Interim Service Status Page

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** the setting is on **When** a platform administrator posts a product status with a note **Then** every signed-in customer sees it on `/status`, and a product with no posted status shows Operational | [TC-PRT-001](../../../../test-cases/functional/service-status-page/TC-PRT-001.md) |
| AC-2 | **Given** the setting is on **When** a platform administrator posts an incident with title, message, start and optional end time **Then** customers who purchased the product see its details, other customers do not | [TC-PRT-002](../../../../test-cases/functional/service-status-page/TC-PRT-002.md) |
| AC-3 | **Given** `app.status-page.enabled` is off **When** a customer opens `/status` **Then** no status or incidents are returned and the page says it is turned off; the admin view still works | [TC-PRT-003](../../../../test-cases/functional/service-status-page/TC-PRT-003.md) |
| AC-4 | **Given** an open incident **When** the administrator resolves it **Then** it shows as resolved; an end time before the start is refused with 400 and the message is shown | [TC-PRT-004](../../../../test-cases/functional/service-status-page/TC-PRT-004.md) |
| AC-5 | **Given** a customer without a token, or a user without `MANAGE_SERVICE_STATUS` **When** they call the customer or admin endpoints **Then** they get 401 or 403; an administrator gets 200, and an unknown product 404 | [TC-PRT-005](../../../../test-cases/functional/service-status-page/TC-PRT-005.md) |
| AC-6 | **Given** a purchased product in a major outage **When** its organization admin opens the business dashboard **Then** an error alert names the product, and it disappears when the product is Operational again; every change is audited | [TC-PRT-006](../../../../test-cases/functional/service-status-page/TC-PRT-006.md) |
| AC-7 | **Given** the status pages **When** they are shown **Then** their text exists in `en.json` and `es.json`, they are keyboard operable, and jest-axe reports no violations | [TC-PRT-007](../../../../test-cases/functional/service-status-page/TC-PRT-007.md) |
