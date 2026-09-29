# Business rules — Plan Management

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-CAT-010 | A plan's currency defaults to USD when not sent, both on create and update. | backend | REQ-CAT-002.1 |
| BR-CAT-011 | `usageLimit`, `usagePrice` and `overageCharge` are non-negative when present (`@PositiveOrZero`); all five new fields are optional. | backend | REQ-CAT-002.2, .3 |
| BR-CAT-012 | Currency is a fixed, closed set (USD, EUR, GBP, INR) — an unrecognized value is rejected at the JSON level (invalid enum), not silently accepted. | backend | REQ-CAT-002.1 |
| BR-CAT-013 | None of these fields change any existing subscription, billing or entitlement behavior — they are read-only display data until 07/08 (later sprints) read them. | backend | REQ-CAT-002 (out of scope) |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
