# Workflow — Product Recommendations

No state machine — `featured` is a plain boolean with no lifecycle of its own, and the popular list is recomputed fresh on every read (not a stored, transitioning record).

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| featured = false | true (or back) | Admin (MANAGE_CATALOG) | BR-MKT-001 | Recorded as part of the existing product update audit entry |
