# Acceptance criteria — Tax Rules and Calculation (REQ-BIL-002)

Source for test cases in `test-cases/functional/tax-rules/` (created when the feature is built).

| ID | Requirement | Given / When / Then |
|---|---|---|
| AC-1 | REQ-BIL-002.1 | **Given** a platform admin **when** they create a tax rule with a region, tax name, rate, method and effective-from date **then** it is saved and listed; **when** rate is outside 0–100 or has more than 2 decimals **then** the save is refused with a field error. |
| AC-2 | REQ-BIL-002.2, BR-2 | **Given** an enabled rule for region India effective 2026-10-01 **when** an admin tries to create a second enabled rule for India effective the same date **then** it is refused as a conflict. |
| AC-3 | REQ-BIL-002.3, BR-4 | **Given** a region whose method is Tax service and the tax service is not configured **when** an invoice is generated **then** the admin rate for that region is used and the invoice records "Admin rate" as the method used. |
| AC-4 | REQ-BIL-002.3 | **Given** a region whose method is Tax service, the service is configured and reachable **when** an invoice is generated **then** the service's result is used and the invoice records "Tax service" as the method used. |
| AC-5 | REQ-BIL-002.4, BR-3 | **Given** a region with no enabled tax rule **when** an invoice is generated **then** its tax amount is 0 and the invoice shows "No tax rule for this region". |
| AC-6 | REQ-BIL-002.5, BR-5 | **Given** a finalized invoice with tax calculated under an old rate **when** an admin later changes that region's rate **then** the already-finalized invoice's tax name, rate and amount are unchanged. |
| AC-7 | REQ-BIL-002.6 | **Given** no tax-service credentials in the common secrets file **when** the admin opens a region's tax-method control **then** Tax service shows "Not configured — falls back to admin rate". |
| AC-8 | REQ-BIL-002.7 | **Given** any tax-rule create, edit, enable or disable action **then** an audit entry records the action and the acting admin. |
| AC-9 | REQ-BIL-002.1, BR-6 | **Given** a user without `MANAGE_BILLING` **when** they call the tax-rule API **then** the response is 403. |
