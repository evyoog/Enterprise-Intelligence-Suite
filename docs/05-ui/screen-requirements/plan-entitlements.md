# Screen section: Plan & entitlements

| Field | Value |
|---|---|
| Requirement | [REQ-SUB-002](../../02-requirements/FRD/entitlements/requirement.md) |
| Placement | A new section on the **existing** subscription / My products detail view (individual); the same section on the **existing** organization subscriptions view (organization) — no new top-level screen, per the [C44](../../01-business/roadmap/open-decisions.md#c44) rule of grouping related information rather than one screen per function |
| Permissions | Individual: own subscriptions only. Organization: per existing organization permissions |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md).

## Per-subscription plan card
| Element | Content |
|---|---|
| Product | Name |
| Plan | Name |
| Status | Chip — ACTIVE (success), SUSPENDED (warning), CANCELLED / EXPIRED (neutral) |
| Term end | `expiresAt`, platform date format |
| Included features | Checklist (one line per item in the plan's `includedFeatures` list, with a check icon) |
| Usage limits | Labelled values (for example "API calls: 10,000 / month") with the note **"Usage tracking is not available yet."** — no progress bars or percentage-used indicators, since no usage data exists ([C50](../../01-business/roadmap/open-decisions.md#c50), [C52](../../01-business/roadmap/open-decisions.md#c52)) |

A SUSPENDED, CANCELLED or EXPIRED subscription's card shows no included features or limits — only the status and term end, since it grants nothing ([BR-1](../../02-requirements/FRD/entitlements/business-rules.md)).

## States
Loading skeleton matching the card layout; a subscription with no `includedFeatures` or `usageLimit` set on its plan shows "No included features listed." / "No usage limit set." rather than an empty table.
