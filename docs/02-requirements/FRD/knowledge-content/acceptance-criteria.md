# Acceptance criteria — Knowledge content

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a contributor **When** they create an item of any type with blocks and submit it **Then** it is In review and audited | [TC-KNW-009](../../../../test-cases/functional/knowledge-content/TC-KNW-009.md) |
| AC-2 | **Given** an item in review **When** a publisher approves and publishes it **Then** readers in its audience see it, version 1.0 is stored, and search finds it | [TC-KNW-010](../../../../test-cases/functional/knowledge-content/TC-KNW-010.md) |
| AC-3 | **Given** a published item **When** a new draft is edited and published as minor **Then** version 1.1 is live and 1.0 stays viewable and comparable | [TC-KNW-011](../../../../test-cases/functional/knowledge-content/TC-KNW-011.md) |
| AC-4 | **Given** an old version **When** a publisher restores it **Then** a new draft holds its content and history is unchanged | [TC-KNW-012](../../../../test-cases/functional/knowledge-content/TC-KNW-012.md) |
| AC-5 | **Given** a draft **When** Preview is opened **Then** it renders with the Knowledge Center components exactly as readers will see it | [TC-KNW-013](../../../../test-cases/functional/knowledge-content/TC-KNW-013.md) |
| AC-6 | **Given** an item past its expiry date **When** a reader searches or browses **Then** it is not shown | [TC-KNW-014](../../../../test-cases/functional/knowledge-content/TC-KNW-014.md) |
| AC-7 | **Given** a transition not in the workflow **When** it is requested **Then** 409 and nothing changes | [TC-KNW-015](../../../../test-cases/functional/knowledge-content/TC-KNW-015.md) |
| AC-8 | **Given** a module in use **When** a publisher deletes it **Then** it is refused; deactivating it works | [TC-KNW-016](../../../../test-cases/functional/knowledge-content/TC-KNW-016.md) |
