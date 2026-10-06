# Acceptance criteria — Knowledge assistant

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** no assistant configured **When** the panel opens **Then** "Coming soon" is shown and no question is sent | [TC-KNW-053](../../../../test-cases/functional/knowledge-assistant/TC-KNW-053.md) |
| AC-2 | **Given** no assistant configured **When** `POST /knowledge/assistant/ask` is called **Then** it answers ASSISTANT_NOT_CONFIGURED | [TC-KNW-054](../../../../test-cases/functional/knowledge-assistant/TC-KNW-054.md) |
