# Workflow — Plan Management

No state machine — plans have no lifecycle of their own beyond the product they belong to (see `product-lifecycle`'s workflow). This feature only adds fields to the existing create/update flow.

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| Plan created/updated with no pricing fields | Same behavior as before this feature | Admin | BR-CAT-013 | None |
| Plan created/updated with pricing fields | Fields saved and returned on the plan | Admin | BR-CAT-010, BR-CAT-011, BR-CAT-012 | None |
