package com.vyoog.eisplatform.modules.knowledgebase.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeActor;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeReader;

import java.util.List;
import java.util.Set;

/** Small builders for knowledge tests. */
public final class KnowledgeFixtures {

    public static final KnowledgeActor PUBLISHER = new KnowledgeActor("sub-publisher", "pub@test", true, true);
    public static final KnowledgeActor CONTRIBUTOR = new KnowledgeActor("sub-contributor", "con@test", true, false);
    public static final KnowledgeReader ANONYMOUS = KnowledgeReader.anonymous();

    private static final ObjectMapper JSON = new ObjectMapper();

    private KnowledgeFixtures() {
    }

    public static KnowledgeReader member(long customerId, long organizationId, Long... productIds) {
        return new KnowledgeReader(true, "sub-" + customerId, customerId, organizationId, Set.of(productIds), false);
    }

    public static JsonNode json(String text) {
        try {
            return JSON.readTree(text);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static KnowledgeContentRequest request(KnowledgeContentType type, String title, String text) {
        return request(type, title, text, KnowledgeAudience.PUBLIC, null, null, null, null);
    }

    public static KnowledgeContentRequest request(KnowledgeContentType type, String title, String text,
                                                  KnowledgeAudience audience, List<Long> orgIds, Long productId,
                                                  Long moduleId, Long categoryId) {
        String blocks = "[{\"type\":\"paragraph\",\"text\":" + JSON.valueToTree(text) + "}]";
        return new KnowledgeContentRequest(type, title, "About " + title, null, productId, moduleId, categoryId, null,
            List.of("test"), null, audience, orgIds, false, null, null, null, null, null, false, null, null, null,
            null, json(blocks), null);
    }
}
