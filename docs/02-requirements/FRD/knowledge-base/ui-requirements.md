# UI requirements — Knowledge Base

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Knowledge base | `/knowledge-base` | Public (signed in or not) | Not specified |
| Knowledge base admin | `/admin/knowledge-base` | `MANAGE_KNOWLEDGE_BASE` | Not specified |

The public screen is a search box plus a list; selecting an article shows its full body inline (no separate detail route). The admin screen is a single page (list + a create/edit dialog), mirroring the Service Status admin page's inline pattern rather than a separate edit route.

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Title | Text | Yes | Non-blank | Backend message shown as-is |
| Body | Text (multiline) | Yes | Non-blank | Backend message shown as-is |

## States
- Empty: "No articles match your search." (`knowledgeBase.noResults`)
- Loading: a centered spinner on the public page while the initial search resolves
- Error: the backend's own message shown in a dismissible `Alert` on the admin page

## Accessibility and localization
- Each article list entry is a keyboard-operable `role="button"` (Enter to open).
- The admin create/edit dialog uses labeled `TextField`s.
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `knowledgeBase.*`.
