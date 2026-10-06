package com.vyoog.eisplatform.modules.knowledgebase.model;

/** REQ-KNW-002.5 publishing workflow (docs/04-workflows/content-publishing.md).
 * The state describes the working copy; what readers see is the live version
 * ({@link KnowledgeArticle#getLiveVersionId()}), so a published item can be
 * edited as a new draft while the published version stays live. */
public enum KnowledgeWorkflowState {
    DRAFT,
    IN_REVIEW,
    APPROVED,
    SCHEDULED,
    PUBLISHED,
    DEPRECATED,
    ARCHIVED
}
