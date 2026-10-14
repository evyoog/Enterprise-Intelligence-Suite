# UI requirements — Entitlements (REQ-SUB-002)

UI requirements for this feature live in **docs/05-ui/screen-requirements/**, one file per screen. This file is the index.

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| What your plans include — a section on the existing My subscriptions screen | `/my/subscriptions` | Any signed-in customer (own subscriptions; for an organization member, the products they have access to) | [entitlements.md](../../../05-ui/screen-requirements/entitlements.md#section-on-my-subscriptions) |
| Entitlements lookup (platform admin) | `/admin/entitlements` | Platform permission `VIEW_ENTITLEMENTS` | [entitlements.md](../../../05-ui/screen-requirements/entitlements.md#admin-entitlements-lookup) |

The earlier draft placement ([plan-entitlements.md](../../../05-ui/screen-requirements/plan-entitlements.md), a card per subscription with usage-limit labels and a status chip) is superseded by [entitlements.md](../../../05-ui/screen-requirements/entitlements.md) as built: only ACTIVE subscriptions appear, so there is no status chip for SUSPENDED, CANCELLED or EXPIRED.

The customer section is part of the existing My subscriptions page, and the admin page is the only new route — in line with the C44 rule of grouping related information into existing screens. The organization endpoint `GET /organization/me/entitlements` exists in the API, but no organization-wide entitlements view is on screen yet (Not built; see [entitlements.md](../../../05-ui/screen-requirements/entitlements.md#not-built)).
