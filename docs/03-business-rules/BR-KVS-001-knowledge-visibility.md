# BR-KVS-001 — Knowledge audience and visibility

**Status:** Draft (2026-10-05, [C71](../01-business/roadmap/open-decisions.md#c71), [C76](../01-business/roadmap/open-decisions.md#c76)). Used by REQ-KNW-002 to REQ-KNW-007 and by search (REQ-PRT-002/003).

A reader sees a knowledge item (and its media, video, download, search result, recommendation, analytics row or assistant source) only when **all** of these hold:
1. The item is Published, its effective date has passed and its expiry date has not (BR-KCON-003).
2. Its **audience** includes the reader:
   | Audience | Who |
   |---|---|
   | Public | Anyone, including signed-out visitors (**confirm** — today `GET /knowledge-base/**` is public) |
   | Customer | Any signed-in user with a linked Customer |
   | Organization | Members of the listed organizations |
   | Admin | Platform administrators |
   | Developer | **Not specified** who counts as a developer (proposed: anyone holding an API key, REQ-INT-001 — confirm) |
   | Partner | Users linked to an approved provider (14.01) — **confirm** |
   | Roles / groups / products | Holders of the listed platform or organization roles, members of the listed organization groups, or users with **product access** to the listed products (`organization_product_access` / active subscription) |
3. If the item is **restricted to product access**, the reader has product access to its product.

Enforcement reuses existing authorization (JWT, `CurrentCustomerResolver`, organization membership, product access, platform permissions); there is no new permission system. The filter is applied **before ranking** in every query (search, lists, recommendations), and on every playback or download URL request. A record of another organization is never returned, suggested or counted (NFR-003).
