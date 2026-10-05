# Acceptance criteria — Semantic Search

Each criterion maps to at least one test case in `test-cases/functional/semantic-search/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a public article about a topic **When** a hybrid search shares its meaning but not all its words **Then** the article is returned with the label "Similar meaning" and `semanticStatus` USED | TC-PRT-028 |
| AC-2 | **Given** the model is down or slower than the timeout **When** a hybrid search runs **Then** keyword results are returned with `semanticStatus` UNAVAILABLE and the page says so | TC-PRT-029 |
| AC-3 | **Given** support tickets **When** the index is built **Then** no ticket has passages or vectors, and ticket searches never use semantic search | TC-PRT-030 |
| AC-4 | **Given** ai-service **When** `/embed` is called with the stub or without a model **Then** it returns normalised 384-dimension vectors, or 503 | TC-PRT-031 |
| AC-5 | **Given** the quality test set **When** hybrid search runs **Then** its hit@5 is reported with the chosen thresholds, and hybrid p95 is under 800 ms | TC-PRT-027 |
