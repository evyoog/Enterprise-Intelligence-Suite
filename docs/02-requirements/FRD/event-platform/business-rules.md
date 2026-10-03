# Business rules — Platform events (REQ-INT-002)

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-1 | An event is written only inside the transaction of the change it describes (`Propagation.MANDATORY`); publishing outside a transaction is a programming error. | backend | .1 |
| BR-2 | A payload never contains secrets, tokens, passwords or card data ([BR-BIL-001](../../../03-business-rules/BR-BIL-001-no-raw-card-data.md), [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md)); payloads carry IDs, statuses, amounts and names only. | backend (publishers) | .2 |
| BR-3 | Event IDs are UUIDs, unique across all events. | database (unique) | .2 |
| BR-4 | Delivery is at least once. A handler that has a receipt for an event ID is not called again for it. | backend (`event_handler_receipt`, unique handler + event) | .3, .4 |
| BR-5 | An event is not delivered while an earlier event (by occurred-at, then ID) of the same aggregate is PENDING or FAILED. | backend (dispatcher) | .5 |
| BR-6 | A failed attempt increments the attempt count, records the error (first 1000 characters) and schedules the next attempt with a doubling delay; at the maximum attempts the status becomes FAILED. | backend | .3 |
| BR-7 | Only a FAILED event can be retried by an administrator; a retry sets it back to PENDING with attempts 0 and next attempt now, and is audited. | backend | .6 |
| BR-8 | Only DELIVERED events are removed by retention; PENDING and FAILED events are kept. | backend | .7 |
