# TC-TEN-088: Cross-organization isolation — another organization's admin cannot act on the request

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-088 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.8, BR-AAC-009) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.onlyManageProductAccessOrOrgAdminCanSeeOrDecideRequestsOfAnotherOrganization`) |

## Preconditions
Two organizations, A and B, each with its own admin and member; a PENDING request raised by organization A's member.

## Steps
1. Organization B's admin calls `approve(orgBAdminId, requestId)` on organization A's request.
2. Organization A's own member (not an admin or *Manage product access* holder) calls `approve(memberId, requestId)`.
3. Organization A's admin calls `approve(adminId, 999999L)` (a nonexistent id).

## Expected Result
Step 1 is refused (`ForbiddenException`) — organization B never learns anything about organization A's request beyond the refusal. Step 2 is also refused (`ForbiddenException`) — lacking `MANAGE_PRODUCT_ACCESS` is refused the same way regardless of organization. Step 3 is a plain `ResourceNotFoundException` (404), identical to what a wrong id in any organization would get.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
