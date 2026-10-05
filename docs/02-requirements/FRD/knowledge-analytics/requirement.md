# REQ-KNW-006 — Knowledge analytics

**Status:** Draft — waits for "Approved"
**Owner:** Product owner

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) (planned; dates unchanged) |
| Requirement ID | REQ-KNW-006 |
| Application | [11 Training & Knowledge Management](../../../01-business/roadmap/applications/11-training-knowledge-management.md) (16 Analytics is P1 and not built; this is knowledge-only) |
| Priority | P0 |

## Summary
Collect knowledge usage events and show them to publishers: views, video views and watch completion, downloads, searches (successful and with no results, reusing C70 search insights), helpfulness, popular items, tickets created from knowledge pages, and **documentation gaps** (frequent searches with no results).

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-006.1 | Events: CONTENT_VIEWED, VIDEO_PLAYED, VIDEO_PROGRESS (25/50/75/100%, seconds watched), DOWNLOADED, FEEDBACK_GIVEN, TICKET_CREATED_FROM_KNOWLEDGE, BOOKMARKED. Stored with content id, version, type, product, module, source type (videos), time, and the reader's organization id; user id only for personal features (recently viewed, progress) — privacy rule open question 1. | Must |
| REQ-KNW-006.2 | Search signals reuse `search_query_log` (C70) with a `scope` of KNOWLEDGE for searches from the Knowledge Center. | Must |
| REQ-KNW-006.3 | Admin analytics: article views, video views, downloads, successful and no-result searches, helpful %, popular topics/content/downloads/videos, tickets from knowledge pages, watch completion and average watch time (where the player reports it), most watched, most searched, by source type; period 7/30/90 days. | Must |
| REQ-KNW-006.4 | **Gap detection:** frequent knowledge searches with zero results (or zero clicks — proposed) are listed as "Knowledge gap detected — '<query>' — N searches — 0 results" with **Create content** (opens the editor with the query as title). Threshold: proposed ≥ 5 searches in the period (confirm). | Must |
| REQ-KNW-006.5 | Lowest-rated content (helpful % with at least N votes, proposed 10). | Must |
| REQ-KNW-006.6 | Organization isolation: analytics for publishers are platform-wide aggregates; no screen lists individual readers. | Must |

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | Store the reader's user id on events (needed for "recently viewed" and progress), and for how long (retention)? | Yes |
| 2 | Gap threshold and whether "no clicks" counts as a gap. | No — confirm in review |
| 3 | Minimum votes for "lowest rated". | No — confirm in review |
