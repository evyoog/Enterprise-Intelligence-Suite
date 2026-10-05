# Business rules — Knowledge permissions

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-KPRM-001 | A knowledge write endpoint answers 403 unless the caller holds `KNOWLEDGE_CONTRIBUTE` (contributor actions) or `MANAGE_KNOWLEDGE_BASE` (publisher actions) through a platform role; ADMIN holds both. | backend (`SecurityConfig`, services) | REQ-KNW-008.3 |
| BR-KPRM-002 | Publisher-only actions: approve, reject, schedule, publish, unpublish, deprecate, archive, restore a version, delete, replace media, edit taxonomy, re-index. | backend | REQ-KNW-008.5 |
| BR-KPRM-003 | A contributor may edit only content in Draft (theirs or returned to Draft); content in Review, Scheduled or Published is read-only for them (Not specified beyond this — open question 5). | backend | REQ-KNW-008.5 |
| BR-KPRM-004 | The UI hides what the caller cannot do; the backend never relies on it. | frontend, backend | REQ-KNW-008.3–.4 |
| BR-KPRM-005 | Every grant/removal and every content action is audited with user, time, action, resource and result. | backend (`AuditService`) | REQ-KNW-008.6 |
