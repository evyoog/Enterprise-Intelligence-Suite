# TESTPLAN-KNW-002: Knowledge Center and Knowledge Management

| Field | Value |
|---|---|
| Requirements | REQ-KNW-001 (update) to REQ-KNW-008 |
| Decisions | C71–C78 |
| Build | commit 0ccbd12 and the follow-up docs commit (2026-10-05) |

## Scope
Content model and workflow, knowledge permissions, private S3 media, videos (YouTube, S3, external), Knowledge Center reader pages, knowledge search, analytics, assistant stub. Academy (11b) is prepared only (tables), not tested.

## Environments
- Automated: H2 (`mvn -B verify`), PostgreSQL 16 with pg_trgm and pgvector (`EIS_PG_TEST_URL`), Vitest + jest-axe.
- UAT: a test bucket (private, Block Public Access on) with CORS for the EIS origin, keys only in `config/secrets.env` — see [UAT-KNW scripts](../../UAT/knowledge/README.md).

## Results (2026-10-05)
- Backend: full `mvn -B verify` green including the Postgres tests; knowledge tests 40+.
- Frontend: 324+ tests with axe green; lint unchanged (12 pre-existing errors, none new); build green; bundle scan found no credentials.

## Test cases
| Test case | Feature | Requirement | Criterion | Title | Status |
|---|---|---|---|---|---|
| [TC-KNW-007](../knowledge-base/TC-KNW-007.md) | knowledge-base | REQ-KNW-001 | AC-7 | Existing articles move to the content model unchanged | Automated part passed; manual part pending UAT |
| [TC-KNW-008](../knowledge-base/TC-KNW-008.md) | knowledge-base | REQ-KNW-001 | AC-8 | Old Knowledge Base links open the Knowledge Center | Passed (automated) |
| [TC-KNW-009](../knowledge-content/TC-KNW-009.md) | knowledge-content | REQ-KNW-002 | AC-1 | A contributor creates a draft and submits it for review | Passed (automated) |
| [TC-KNW-010](../knowledge-content/TC-KNW-010.md) | knowledge-content | REQ-KNW-002 | AC-2 | A publisher approves and publishes; readers see version 1.0 and search finds it | Passed (automated) |
| [TC-KNW-011](../knowledge-content/TC-KNW-011.md) | knowledge-content | REQ-KNW-002 | AC-3 | A new draft of published content publishes as 1.1; 1.0 stays viewable and comparable | Passed (automated) |
| [TC-KNW-012](../knowledge-content/TC-KNW-012.md) | knowledge-content | REQ-KNW-002 | AC-4 | Restoring an old version creates a new draft and keeps history | Passed (automated) |
| [TC-KNW-013](../knowledge-content/TC-KNW-013.md) | knowledge-content | REQ-KNW-002 | AC-5 | Preview renders the draft as readers will see it | Pending UAT |
| [TC-KNW-014](../knowledge-content/TC-KNW-014.md) | knowledge-content | REQ-KNW-002 | AC-6 | Expired and not-yet-effective content is hidden | Passed (automated) |
| [TC-KNW-015](../knowledge-content/TC-KNW-015.md) | knowledge-content | REQ-KNW-002 | AC-7 | Transitions outside the workflow are refused | Passed (automated) |
| [TC-KNW-016](../knowledge-content/TC-KNW-016.md) | knowledge-content | REQ-KNW-002 | AC-8 | A module in use cannot be deleted, only deactivated | Passed (automated) |
| [TC-KNW-017](../knowledge-content/TC-KNW-017.md) | knowledge-content | REQ-KNW-002 | BR-KCON-008 | Blocks are validated and never hold script links | Passed (automated) |
| [TC-KNW-018](../knowledge-content/TC-KNW-018.md) | knowledge-content | REQ-KNW-002 | REQ-KNW-002.5 | Scheduled publishing happens when its time comes | Passed (automated) |
| [TC-KNW-019](../knowledge-permissions/TC-KNW-019.md) | knowledge-permissions | REQ-KNW-008 | AC-1 | Readers cannot create, upload, publish or delete | Passed (automated) |
| [TC-KNW-020](../knowledge-permissions/TC-KNW-020.md) | knowledge-permissions | REQ-KNW-008 | AC-2 | A contributor may draft and submit but not publish or delete | Passed (automated) |
| [TC-KNW-021](../knowledge-permissions/TC-KNW-021.md) | knowledge-permissions | REQ-KNW-008 | AC-3 | A publisher runs the whole workflow, audited | Passed (automated) |
| [TC-KNW-022](../knowledge-permissions/TC-KNW-022.md) | knowledge-permissions | REQ-KNW-008 | AC-4 | No Knowledge Management entry points for readers | Passed (automated) |
| [TC-KNW-023](../knowledge-permissions/TC-KNW-023.md) | knowledge-permissions | REQ-KNW-008 | AC-5 | Granting or removing a knowledge permission is audited | Pending UAT |
| [TC-KNW-024](../knowledge-media/TC-KNW-024.md) | knowledge-media | REQ-KNW-003 | AC-1 | Upload URLs are presigned PUTs for new generated keys | Passed (automated) |
| [TC-KNW-025](../knowledge-media/TC-KNW-025.md) | knowledge-media | REQ-KNW-003 | AC-2 | Disallowed types and oversized files are refused | Passed (automated) |
| [TC-KNW-026](../knowledge-media/TC-KNW-026.md) | knowledge-media | REQ-KNW-003 | AC-3 | Completion verifies the stored object | Passed (automated) |
| [TC-KNW-027](../knowledge-media/TC-KNW-027.md) | knowledge-media | REQ-KNW-003 | AC-4 | Downloads only for readers allowed to see content using the file | Passed (automated) |
| [TC-KNW-028](../knowledge-media/TC-KNW-028.md) | knowledge-media | REQ-KNW-003 | AC-5 | An expired presigned URL fails | Automated part passed; manual part pending UAT |
| [TC-KNW-029](../knowledge-media/TC-KNW-029.md) | knowledge-media | REQ-KNW-003 | AC-6 | Deleting a file in use needs confirmation | Passed (automated) |
| [TC-KNW-030](../knowledge-media/TC-KNW-030.md) | knowledge-media | REQ-KNW-003 | AC-7 | No credentials in responses or the built bundle | Automated part passed; manual part pending UAT |
| [TC-KNW-031](../knowledge-media/TC-KNW-031.md) | knowledge-media | REQ-KNW-003 | AC-8 | Uploads never completed are cleaned up | Passed (automated) |
| [TC-KNW-032](../knowledge-videos/TC-KNW-032.md) | knowledge-videos | REQ-KNW-004 | AC-1 | YouTube details without an API key come from oEmbed | Passed (automated) |
| [TC-KNW-033](../knowledge-videos/TC-KNW-033.md) | knowledge-videos | REQ-KNW-004 | AC-2 | With an API key the Data API fills every field | Passed (automated) |
| [TC-KNW-034](../knowledge-videos/TC-KNW-034.md) | knowledge-videos | REQ-KNW-004 | AC-3 | Hosted video upload with progress, verified before saving | Automated part passed; manual part pending UAT |
| [TC-KNW-035](../knowledge-videos/TC-KNW-035.md) | knowledge-videos | REQ-KNW-004 | AC-4 | Wrong formats and oversized videos are refused | Passed (automated) |
| [TC-KNW-036](../knowledge-videos/TC-KNW-036.md) | knowledge-videos | REQ-KNW-004 | AC-5 | Organization B cannot play organization A's restricted video | Passed (automated) |
| [TC-KNW-037](../knowledge-videos/TC-KNW-037.md) | knowledge-videos | REQ-KNW-004 | AC-6 | Transcript and chapters on the player | Automated part passed; manual part pending UAT |
| [TC-KNW-038](../knowledge-videos/TC-KNW-038.md) | knowledge-videos | REQ-KNW-004 | AC-7 | A phrase spoken only in a transcript finds the video | Passed (automated) |
| [TC-KNW-039](../knowledge-videos/TC-KNW-039.md) | knowledge-videos | REQ-KNW-004 | AC-8 | Deleting a hosted video deletes its storage object | Passed (automated) |
| [TC-KNW-040](../knowledge-center/TC-KNW-040.md) | knowledge-center | REQ-KNW-005 | AC-1 | Knowledge Center home | Passed (automated) |
| [TC-KNW-041](../knowledge-center/TC-KNW-041.md) | knowledge-center | REQ-KNW-005 | AC-2 | Recommended for you puts the reader's products first | Pending UAT |
| [TC-KNW-042](../knowledge-center/TC-KNW-042.md) | knowledge-center | REQ-KNW-005 | AC-3 | Organization-restricted content never reaches other organizations | Passed (automated) |
| [TC-KNW-043](../knowledge-center/TC-KNW-043.md) | knowledge-center | REQ-KNW-005 | AC-4 | Search results grouped by type with products and modules | Passed (automated) |
| [TC-KNW-044](../knowledge-center/TC-KNW-044.md) | knowledge-center | REQ-KNW-005 | AC-5 | Meaning-based search | Blocked (SS-1) |
| [TC-KNW-045](../knowledge-center/TC-KNW-045.md) | knowledge-center | REQ-KNW-005 | AC-6 | Article blocks render in order with glossary links and secure downloads | Passed (automated) |
| [TC-KNW-046](../knowledge-center/TC-KNW-046.md) | knowledge-center | REQ-KNW-005 | AC-7 | "No" feedback needs a reason and reaches analytics | Passed (automated) |
| [TC-KNW-047](../knowledge-center/TC-KNW-047.md) | knowledge-center | REQ-KNW-005 | AC-8 | Create support ticket is prefilled from the page | Automated part passed; manual part pending UAT |
| [TC-KNW-048](../knowledge-center/TC-KNW-048.md) | knowledge-center | REQ-KNW-005 | AC-9 | Ask AI assistant says Coming soon | Automated part passed; manual part pending UAT |
| [TC-KNW-049](../knowledge-center/TC-KNW-049.md) | knowledge-center | REQ-KNW-005 | AC-10 | Accessibility of Knowledge Center and Knowledge Management | Passed (automated) |
| [TC-KNW-050](../knowledge-analytics/TC-KNW-050.md) | knowledge-analytics | REQ-KNW-006 | AC-1 | Views, feedback and video events reach analytics | Passed (automated) |
| [TC-KNW-051](../knowledge-analytics/TC-KNW-051.md) | knowledge-analytics | REQ-KNW-006 | AC-2 | Knowledge gaps are detected | Passed (automated) |
| [TC-KNW-052](../knowledge-analytics/TC-KNW-052.md) | knowledge-analytics | REQ-KNW-006 | AC-3 | Analytics show no reader identities | Automated part passed; manual part pending UAT |
| [TC-KNW-053](../knowledge-assistant/TC-KNW-053.md) | knowledge-assistant | REQ-KNW-007 | AC-1 | The assistant panel shows Coming soon | Pending UAT |
| [TC-KNW-054](../knowledge-assistant/TC-KNW-054.md) | knowledge-assistant | REQ-KNW-007 | AC-2 | The ask endpoint answers not configured | Passed (automated) |
