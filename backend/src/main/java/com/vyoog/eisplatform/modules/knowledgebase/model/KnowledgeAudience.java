package com.vyoog.eisplatform.modules.knowledgebase.model;

/** BR-KVS-001 audiences built on 2026-10-05. Developer, Partner and the
 * role/group audiences are Not specified (who counts as a developer or
 * partner) and are not offered yet. */
public enum KnowledgeAudience {
    /** Anyone, including signed-out visitors (as before for articles). */
    PUBLIC,
    /** Any signed-in user. */
    CUSTOMER,
    /** Members of the listed organizations only. */
    ORGANIZATION,
    /** Platform knowledge staff (contributors, publishers, administrators). */
    ADMIN
}
