package com.vyoog.eisplatform.modules.knowledgebase.service;

import java.util.Set;

/**
 * Who is reading (BR-KVS-001), resolved only from the caller's own token —
 * never from client input.
 *
 * @param signedIn       a valid token was presented
 * @param keycloakSub    token subject, null when signed out
 * @param customerId     linked customer, if any
 * @param organizationId the reader's active organization, if any
 * @param productIds     catalog products the reader has active product access to
 * @param staff          holds a knowledge permission (sees ADMIN-audience and restricted content)
 */
public record KnowledgeReader(boolean signedIn, String keycloakSub, Long customerId, Long organizationId,
                              Set<Long> productIds, boolean staff) {

    public static KnowledgeReader anonymous() {
        return new KnowledgeReader(false, null, null, null, Set.of(), false);
    }
}
