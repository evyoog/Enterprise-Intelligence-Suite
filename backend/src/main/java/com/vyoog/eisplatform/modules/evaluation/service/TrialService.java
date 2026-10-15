package com.vyoog.eisplatform.modules.evaluation.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.service.OrderService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * C102 / REQ-MKT-005 (TR-1): starting a free trial. The rules live in {@link SubscriptionService#startTrial}; this adds who may start one for an
 * organization: an organization admin or a person with Manage billing, the same people who may buy directly (C100 CA-2).
 */
@Service
@RequiredArgsConstructor
public class TrialService {

    private final SubscriptionService subscriptionService;
    private final OrganizationSelfService organizationSelfService;
    private final OrderService orderService;

    public SubscriptionDto startForCustomer(Long customerId, Long productId) {
        return subscriptionService.startTrial(customerId, productId);
    }

    public SubscriptionDto startForOrganization(Long customerId, Long productId) {
        OrganizationMember member = organizationSelfService.requireCaller(customerId);
        if (!orderService.canPurchaseDirectly(member)) {
            throw new ForbiddenException("You do not have permission to do this");
        }
        return subscriptionService.startOrganizationTrial(member.getOrganizationId(), customerId, productId);
    }
}
