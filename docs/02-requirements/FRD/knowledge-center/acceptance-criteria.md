# Acceptance criteria — Knowledge Center

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a bookmark to `/knowledge-base` **When** opened **Then** the Knowledge Center opens with header, hero, quick access, recommended, popular guides, featured videos and (signed in) the learning panel | [TC-KNW-040](../../../../test-cases/functional/knowledge-center/TC-KNW-040.md) |
| AC-2 | **Given** a member with access to Valam.ai only **When** they open Recommended for you **Then** Valam.ai content comes first; with no signals, popular content is shown | [TC-KNW-041](../../../../test-cases/functional/knowledge-center/TC-KNW-041.md) |
| AC-3 | **Given** content restricted to organization A **When** a member of B browses, searches or opens its URL **Then** it never appears (404 on direct URL) | [TC-KNW-042](../../../../test-cases/functional/knowledge-center/TC-KNW-042.md) |
| AC-4 | **Given** "How do I create a purchase order?" **When** searched **Then** results are grouped by type (articles, videos, FAQs, workflow, product, module) | [TC-KNW-043](../../../../test-cases/functional/knowledge-center/TC-KNW-043.md) |
| AC-5 | **Given** "software for managing inventory" **When** searched in hybrid mode with a real model **Then** inventory content is found | [TC-KNW-044](../../../../test-cases/functional/knowledge-center/TC-KNW-044.md) (blocked: SS-1) |
| AC-6 | **Given** an article with screenshot, steps, video, PDF, FAQ and glossary terms **When** opened **Then** blocks render in order, glossary terms link, the PDF downloads through a temporary URL | [TC-KNW-045](../../../../test-cases/functional/knowledge-center/TC-KNW-045.md) |
| AC-7 | **Given** a reader clicks "No" **When** they pick a reason and comment **Then** the feedback is stored and appears in analytics | [TC-KNW-046](../../../../test-cases/functional/knowledge-center/TC-KNW-046.md) |
| AC-8 | **Given** an error code page **When** Create support ticket is clicked **Then** the ticket form opens prefilled with the error code, article and product | [TC-KNW-047](../../../../test-cases/functional/knowledge-center/TC-KNW-047.md) |
| AC-9 | **Given** Ask AI assistant **When** clicked **Then** "Coming soon" is shown | [TC-KNW-048](../../../../test-cases/functional/knowledge-center/TC-KNW-048.md) |
| AC-10 | **Given** every Knowledge Center screen and dialog **When** axe runs **Then** no violations; keyboard alone can use them; reduced motion stops animations | [TC-KNW-049](../../../../test-cases/functional/knowledge-center/TC-KNW-049.md) |
