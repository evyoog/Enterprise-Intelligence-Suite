<!--
GITHUB PULL REQUEST TEMPLATE
Place at .github/pull_request_template.md in each repository.
Every field below is required per the SDLC traceability convention — a PR without these IDs cannot be traced end to end.
-->

## Summary
<What does this PR do, in one or two sentences?>

## Traceability
| Field | ID |
|---|---|
| Feature | FTR-<APP-CODE>-<NNN> |
| Function | FUN-<APP-CODE>-<NNN> |
| Requirement(s) | REQ-<APP-CODE>-<NNN> |
| Design | DES-<APP-CODE>-<NNN> |
| Test Case(s) | TC-<APP-CODE>-<NNN> |
| Story | STORY-<APP-CODE>-<NNN> |

## Changes
- <bullet list of what changed>

## Testing Performed
- [ ] Unit tests added/updated and passing
- [ ] Integration tests passing
- [ ] Manually verified against acceptance criteria
- [ ] Security scan (SAST/dependency) clean

## Risk Classification
- [ ] Low — no approval required beyond standard review
- [ ] Medium — reviewed by a second engineer from outside the immediate team
- [ ] High/Critical — requires explicit sign-off referenced here: <approver, date>

## AI-Generated Content
- [ ] This PR contains AI-generated code
  - AI Context Package used: <link/path>
  - Human review confirms generated code matches the Design and Requirement above

## Deployment Notes
<Any migration steps, feature flags, or config changes needed alongside this deploy.>

## Rollback Plan
<How to revert this change if it causes a production issue.>
