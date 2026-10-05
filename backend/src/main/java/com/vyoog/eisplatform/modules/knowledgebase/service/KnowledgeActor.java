package com.vyoog.eisplatform.modules.knowledgebase.service;

/**
 * A knowledge staff member acting in Knowledge Management (REQ-KNW-008).
 * Contributor = KNOWLEDGE_CONTRIBUTE; publisher = MANAGE_KNOWLEDGE_BASE
 * (reused, C71). ADMIN holds both.
 */
public record KnowledgeActor(String keycloakSub, String email, boolean contributor, boolean publisher) {

    public boolean any() {
        return contributor || publisher;
    }
}
