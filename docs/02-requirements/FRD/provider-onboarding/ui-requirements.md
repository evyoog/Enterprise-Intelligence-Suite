# UI requirements — Provider Onboarding

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Provider application | `/partners/apply` | Public (no account required) | Not specified |
| Partners list | `/admin/partners` | `MANAGE_PARTNERS` | Not specified |
| Partner detail | `/admin/partners/:id` | `MANAGE_PARTNERS` | Not specified |

The application page is a short public form (company name, contact name, contact email, optional description) reachable from the workspace sidebar ("Become a partner"), same visibility as the product catalog and search. The admin list shows every provider with its current status; the detail page shows the provider's own details, the next available lifecycle action (Verify/Approve/Activate, one at a time — see workflow.md), a Reject action while not yet ACTIVE or REJECTED, and a contract form (terms, start date, end date) that upserts the provider's single contract.

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Company name | Text | Yes | Non-empty | - |
| Contact name | Text | Yes | Non-empty | - |
| Contact email | Email | Yes | Valid email format | - |
| Description | Text (multiline) | No | - | - |
| Contract terms | Text (multiline) | Yes (to save) | Non-empty | - |
| Contract start/end date | Date | Yes (to save) | End date after start date (BR-PTR-006), enforced server-side | backend error message shown as-is |

## States
- Submitted (application): "Thanks — your application has been submitted and is now under review." (`partners.apply.submitted`)
- Empty (admin list): "No provider applications yet." (`partners.admin.none`)
- No next action: once ACTIVE or REJECTED, neither a lifecycle button nor Reject is shown
- Error: the backend's own message shown in a dismissible `Alert`

## Accessibility and localization
- Every form field is a labeled MUI `TextField`.
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `partners.*`.
