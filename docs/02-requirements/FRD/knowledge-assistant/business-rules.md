# Business rules — Knowledge assistant

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-KAST-001 | Until D8 is decided and an assistant provider is configured, `ask` never calls any model and answers not configured. | backend | C75 |
| BR-KAST-002 | (When built) only passages the reader may see (BR-KVS-001) are given to the model; every answer cites them. | backend | REQ-KNW-007.3–.4 |
