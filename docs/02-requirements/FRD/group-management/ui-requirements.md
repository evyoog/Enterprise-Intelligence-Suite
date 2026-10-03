# UI requirements — Group Management

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Groups (Organization settings; moved from the business dashboard by [C69](../../../01-business/roadmap/open-decisions.md#c69)) | `/organization/settings#groups` | Org admin | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| New group name | Text | Yes (to submit) | Non-blank | Backend message shown as-is (`groups.createError`) |
| Add member | Select, from the caller's own org members not already in the group | No | n/a | `groups.addMemberError` |
| Group member chips | Chip with delete icon | n/a | n/a | `groups.removeMemberError` |
| Delete group | Button | n/a | n/a | `groups.deleteError` |

## States
- Empty: "No groups yet." when the organization has none; "No members in this group yet." per empty group.
- Loading: hides itself (like `OrganizationMembersCard`) on a 403/404 rather than showing an error.
- Error: inline `Alert` at the top of the card.

## Accessibility and localization
- All labels go through `t()` under the `groups.*` namespace, in both `en.json` and `es.json`.
- Delete-group and remove-member controls carry the group/member name in their accessible name.
