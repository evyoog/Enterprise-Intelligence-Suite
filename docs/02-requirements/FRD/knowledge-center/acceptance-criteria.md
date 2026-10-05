# Acceptance criteria — Knowledge Center

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a bookmark to `/knowledge-base` **When** opened **Then** the Knowledge Center opens with header, hero, quick access, recommended, popular guides, featured videos and (signed in) the learning panel | To be written with the build |
| AC-2 | **Given** a member with access to Valam.ai only **When** they open Recommended for you **Then** Valam.ai content comes first; with no signals, popular content is shown | To be written with the build |
| AC-3 | **Given** content restricted to organization A **When** a member of B browses, searches or opens its URL **Then** it never appears (404 on direct URL) | To be written with the build |
| AC-4 | **Given** "How do I create a purchase order?" **When** searched **Then** results are grouped by type (articles, videos, FAQs, workflow, product, module) | To be written with the build |
| AC-5 | **Given** "software for managing inventory" **When** searched in hybrid mode with a real model **Then** inventory content is found | To be written with the build (needs SS-1) |
| AC-6 | **Given** an article with screenshot, steps, video, PDF, FAQ and glossary terms **When** opened **Then** blocks render in order, glossary terms link, the PDF downloads through a temporary URL | To be written with the build |
| AC-7 | **Given** a reader clicks "No" **When** they pick a reason and comment **Then** the feedback is stored and appears in analytics | To be written with the build |
| AC-8 | **Given** an error code page **When** Create support ticket is clicked **Then** the ticket form opens prefilled with the error code, article and product | To be written with the build |
| AC-9 | **Given** Ask AI assistant **When** clicked **Then** "Coming soon" is shown | To be written with the build |
| AC-10 | **Given** every Knowledge Center screen and dialog **When** axe runs **Then** no violations; keyboard alone can use them; reduced motion stops animations | To be written with the build |
