# BR-SUB-010 — Subscription end time

| Field | Value |
|---|---|
| Status | In Review (with [REQ-INT-003](../02-requirements/FRD/platform-tool-sync/requirement.md)); answer Q11 of [C86](../01-business/roadmap/open-decisions.md#c86) |
| Applies to | Every subscription (individual and organization), renewal, expiry job, invoices that print an end date, and every product that checks a subscription |

## Rule
1. A subscription's **end** is a calendar date, stored and sent as the **end of that day at 23:59:00.000 in Asia/Kolkata (+05:30)**: `YYYY-MM-DD 23:59:00.000 +0530`; in messages as `YYYY-MM-DDT23:59:00.000+05:30`.
2. Seconds are `:00` and milliseconds `.000`, exactly as written. The time of day is not taken from the moment of purchase or renewal.
3. The end date is computed from the start or renewal date and the plan period (as [BR-SUB-005](../02-requirements/FRD/subscription-lifecycle/business-rules.md) already defines), and then set to that time on the resulting IST date.
4. A subscription is **effective** while `startsAt ≤ now ≤ endsAt`. After `endsAt` access is denied immediately. There is no trial period and no grace period for now (revisit later by a new decision).
5. The expiry job ([BR-SUB-008](../02-requirements/FRD/subscription-lifecycle/business-rules.md)) uses the same instant; a subscription whose `endsAt` has passed is EXPIRED.
6. Existing subscriptions are corrected once: each `endsAt` becomes 23:59:00.000 +05:30 on the **same IST date** as today's value. The change is reported before it is applied.

## Why
One predictable end time for customers, invoices and every product, independent of the server time zone.

## Effect on existing rules
[BR-SUB-005](../02-requirements/FRD/subscription-lifecycle/business-rules.md) (renew) and BR-SUB-008 (expiry job) keep their meaning; they now use the end time above instead of "now plus 30/365 days at any time of day".
