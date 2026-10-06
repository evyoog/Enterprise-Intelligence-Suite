package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.authorization.service.PrivilegedAccessService;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentVersion;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProduct;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Knowledge authorization (REQ-KNW-008, BR-KVS-001). Reuses the existing
 * authorization: platform permissions (with privileged-access grants),
 * the caller's linked customer, organization membership and product access.
 * No new permission system.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeAccessService {

    public static final String CONTRIBUTE = "KNOWLEDGE_CONTRIBUTE";
    public static final String PUBLISH = "MANAGE_KNOWLEDGE_BASE";

    private final AuthorizationService authorizationService;
    private final PrivilegedAccessService privilegedAccessService;
    private final CurrentCustomerResolver customerResolver;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationProductAccessRepository productAccessRepository;
    private final KnowledgeProductRepository knowledgeProductRepository;

    /** The staff member behind an admin request; 403 without a knowledge permission. */
    public KnowledgeActor actor(Authentication auth) {
        KnowledgeActor actor = actorOrNull(auth);
        if (actor == null || !actor.any()) {
            throw KnowledgeException.forbidden("You do not have permission to manage knowledge content.");
        }
        return actor;
    }

    public KnowledgeActor actorOrNull(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Jwt jwt)
            || jwt.hasClaim("api_key_id")) {
            return null;
        }
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String sub = jwt.getSubject();
        boolean publisher = has(authorities, sub, PUBLISH);
        boolean contributor = publisher || has(authorities, sub, CONTRIBUTE);
        return new KnowledgeActor(sub, jwt.getClaimAsString("email"), contributor, publisher);
    }

    private boolean has(List<String> authorities, String sub, String permission) {
        return authorizationService.hasPlatformPermission(authorities, permission)
            || privilegedAccessService.hasActivePlatformGrant(sub, permission);
    }

    public void requirePublisher(KnowledgeActor actor) {
        if (!actor.publisher()) {
            throw KnowledgeException.forbidden("Only a knowledge publisher can do this.");
        }
    }

    /** The reader behind a request (signed out = anonymous). */
    public KnowledgeReader reader(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return KnowledgeReader.anonymous();
        }
        KnowledgeActor actor = actorOrNull(auth);
        boolean staff = actor != null && actor.any();
        Optional<Customer> customer = customerResolver.resolveOptional(jwt);
        Long customerId = customer.map(Customer::getId).orElse(null);
        Long organizationId = null;
        Set<Long> productIds = new HashSet<>();
        if (customerId != null) {
            Optional<OrganizationMember> member = memberRepository.findFirstByCustomerIdAndStatus(customerId,
                MembershipStatus.ACTIVE);
            if (member.isPresent()) {
                organizationId = member.get().getOrganizationId();
                for (OrganizationProductAccess access : productAccessRepository.findByOrganizationMemberId(member.get().getId())) {
                    if (access.getStatus() == MembershipStatus.ACTIVE) {
                        productIds.add(access.getProductId());
                    }
                }
            }
        }
        return new KnowledgeReader(true, jwt.getSubject(), customerId, organizationId, Set.copyOf(productIds), staff);
    }

    /** Knowledge product id → catalog product id, for the product-access rule. */
    public Map<Long, Long> catalogLinks() {
        return knowledgeProductRepository.findAll().stream()
            .filter(p -> p.getCatalogProductId() != null)
            .collect(Collectors.toMap(KnowledgeProduct::getId, KnowledgeProduct::getCatalogProductId));
    }

    /**
     * BR-KVS-001: the live version of {@code item} is visible to {@code reader}.
     * Archived items, items without a live version, before their effective
     * date or after their expiry date are never visible (BR-KCON-003).
     */
    public boolean canSee(KnowledgeArticle item, KnowledgeContentVersion live, KnowledgeReader reader,
                          Function<Long, Long> catalogProductOf, Instant now) {
        if (item == null || live == null || item.getLiveVersionId() == null
            || !live.getId().equals(item.getLiveVersionId())
            || item.getWorkflowState() == com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState.ARCHIVED) {
            return false;
        }
        if (live.getEffectiveAt() != null && live.getEffectiveAt().isAfter(now)) {
            return false;
        }
        if (live.getExpiresAt() != null && !live.getExpiresAt().isAfter(now)) {
            return false;
        }
        if (!audienceIncludes(live.getAudience(), live.getAudienceOrgIds(), reader)) {
            return false;
        }
        if (live.isRequireProductAccess() && !reader.staff()) {
            Long catalogProduct = item.getProductId() == null ? null : catalogProductOf.apply(item.getProductId());
            return catalogProduct != null && reader.productIds().contains(catalogProduct);
        }
        return true;
    }

    static boolean audienceIncludes(KnowledgeAudience audience, String orgIds, KnowledgeReader reader) {
        if (audience == null) {
            return false;
        }
        return switch (audience) {
            case PUBLIC -> true;
            case CUSTOMER -> reader.signedIn();
            case ORGANIZATION -> reader.staff()
                || (reader.organizationId() != null && parseIds(orgIds).contains(reader.organizationId()));
            case ADMIN -> reader.staff();
        };
    }

    static Set<Long> parseIds(String ids) {
        Set<Long> result = new HashSet<>();
        if (ids == null) {
            return result;
        }
        for (String part : ids.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    result.add(Long.parseLong(trimmed));
                } catch (NumberFormatException ignored) {
                    // not an id
                }
            }
        }
        return result;
    }

    static String formatIds(java.util.Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return "," + ids.stream().distinct().sorted().map(String::valueOf).collect(Collectors.joining(",")) + ",";
    }
}
