package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.integration.service.OutboxService;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.DecideOrderRequest;
import com.vyoog.eisplatform.modules.registration.dto.OrderDto;
import com.vyoog.eisplatform.modules.registration.dto.SubmitOrderRequest;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrderRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 09 Order & Provisioning Management (sprint 2027.1.1), organization
 * purchasing only — an individual customer already gets instant self-serve
 * activation via {@code SubscriptionService#subscribe}; adding an approval
 * step there would only add friction, not value. Any organization member may
 * submit an order; provisioning it (creating/activating the organization's
 * subscription) requires an ORG_ADMIN's decision (MANAGE_ORDERS), so one
 * member can't unilaterally commit the whole organization's subscriptions.
 *
 * <p>09.03 Workflow Orchestration (a generic, configurable workflow
 * engine — trigger/execute/retry/rollback/compensate/escalate arbitrary
 * multi-step workflows) is deliberately NOT built: this platform has exactly
 * one orchestrated process (submit -> decide -> provision), implemented
 * directly below. Building a generic engine with only one real consumer
 * would be pure speculation — see decision C39.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductPlanRepository productPlanRepository;
    private final AuthorizationService authorizationService;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    /** REQ-CAT-005 (C85): per-product audience rule. */
    private final com.vyoog.eisplatform.modules.offering.service.EligibilityService eligibilityService;
    /** REQ-INT-002 (C62): OrderApproved. */
    private final OutboxService outboxService;

    /** Same shape as {@code OrganizationSelfService#resolveMembership} —
     * duplicated rather than shared, matching this module's existing
     * pattern of each service owning its own resolution helper (see
     * {@code SubscriptionService#resolveOwnSubscription}). */
    private OrganizationMember resolveMembership(Long customerId) {
        OrganizationMember member = memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("You are not a member of an organization"));
        Organization organization = organizationRepository.findById(member.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (!authorizationService.organizationInGoodStanding(organization.getStatus())
                || organization.getLifecycleStatus() != OrganizationLifecycleStatus.ACTIVE) {
            throw new ForbiddenException("Your organization's account is not currently active");
        }
        return member;
    }

    private OrganizationMember requireManageOrders(Long customerId) {
        OrganizationMember member = resolveMembership(customerId);
        if (!authorizationService.hasOrganizationPermission(member.getOrgRole(), "MANAGE_ORDERS")) {
            throw new ForbiddenException("You do not have permission to do this");
        }
        return member;
    }

    private Order resolveOwnOrganizationOrder(Long organizationId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!organizationId.equals(order.getOrganizationId())) {
            throw new ResourceNotFoundException("Order not found");
        }
        return order;
    }

    /** 09.01.01.01/.02/.03/.04 Create/Validate/Price/Submit order, folded
     * into one action: there is no separate draft state, and "price" is
     * simply read from the product/plan (no billing engine exists to quote
     * against — see decision C39). */
    @Transactional
    public OrderDto submitOrder(Long customerId, SubmitOrderRequest request) {
        OrganizationMember member = resolveMembership(customerId);
        Product product = productRepository.findById(request.productId())
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        eligibilityService.assertEligible(product.getId(), true);
        if (request.planId() != null) {
            ProductPlan plan = productPlanRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + request.planId()));
            if (!plan.getProduct().getId().equals(product.getId())) {
                throw new IllegalArgumentException("That plan does not belong to this product.");
            }
        }

        Order order = new Order();
        order.setOrganizationId(member.getOrganizationId());
        order.setRequestedByCustomerId(customerId);
        order.setProductId(request.productId());
        order.setPlanId(request.planId());
        order.setStatus(OrderStatus.SUBMITTED);
        order = orderRepository.save(order);

        auditService.recordSuccess("ORDER_SUBMITTED", null, customerId, null,
            "Order", order.getId().toString(), member.getOrganizationId(), "Order submitted for product " + request.productId());
        notifyOrgAdmins(member.getOrganizationId(), "New order awaiting approval",
            "A member has requested " + product.getName() + " for your organization.");

        return toDto(order);
    }

    /** 09.04.01.03 Approve; 09.02.01.01/.03 Provision/Activate service —
     * folded into one call, synchronously (see this class's own javadoc on
     * why there is no separate provisioning step to track). */
    @Transactional
    public OrderDto approveOrder(Long customerId, Long orderId, DecideOrderRequest request) {
        OrganizationMember admin = requireManageOrders(customerId);
        Order order = resolveOwnOrganizationOrder(admin.getOrganizationId(), orderId);
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only a submitted order can be approved.");
        }
        subscriptionService.subscribeOrganization(order.getOrganizationId(), order.getProductId(), order.getPlanId());

        order.setStatus(OrderStatus.APPROVED);
        order.setDecidedByCustomerId(customerId);
        order.setDecidedAt(Instant.now());
        order.setDecisionNote(request.note());
        order = orderRepository.save(order);

        auditService.recordSuccess("ORDER_APPROVED", null, customerId, null,
            "Order", orderId.toString(), admin.getOrganizationId(), "Order approved and provisioned");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", order.getId());
        payload.put("organizationId", order.getOrganizationId());
        payload.put("productId", order.getProductId());
        payload.put("planId", order.getPlanId());
        payload.put("decidedByCustomerId", customerId);
        outboxService.publish(PlatformEventTypes.ORDER_APPROVED, PlatformEventTypes.AGGREGATE_ORDER, order.getId(), payload);
        notifyRequester(order, "Order approved", "Your order has been approved and provisioned.");

        return toDto(order);
    }

    /** 09.04.01.04 Reject. */
    @Transactional
    public OrderDto rejectOrder(Long customerId, Long orderId, DecideOrderRequest request) {
        OrganizationMember admin = requireManageOrders(customerId);
        Order order = resolveOwnOrganizationOrder(admin.getOrganizationId(), orderId);
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only a submitted order can be rejected.");
        }
        order.setStatus(OrderStatus.REJECTED);
        order.setDecidedByCustomerId(customerId);
        order.setDecidedAt(Instant.now());
        order.setDecisionNote(request.note());
        order = orderRepository.save(order);

        auditService.recordSuccess("ORDER_REJECTED", null, customerId, null,
            "Order", orderId.toString(), admin.getOrganizationId(), "Order rejected");
        notifyRequester(order, "Order rejected", "Your order was not approved.");

        return toDto(order);
    }

    /** 09.01.01.06 Cancel order — the requester's own, only while still
     * SUBMITTED (an already-decided order is a historical record, not
     * cancellable — same one-way-past-a-decision reasoning as
     * {@code PrivilegedAccessService}'s request lifecycle). */
    @Transactional
    public OrderDto cancelOrder(Long customerId, Long orderId) {
        OrganizationMember member = resolveMembership(customerId);
        Order order = resolveOwnOrganizationOrder(member.getOrganizationId(), orderId);
        if (!customerId.equals(order.getRequestedByCustomerId())) {
            throw new ResourceNotFoundException("Order not found");
        }
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only a submitted order can be cancelled.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        auditService.recordSuccess("ORDER_CANCELLED", null, customerId, null,
            "Order", orderId.toString(), member.getOrganizationId(), "Order cancelled by requester");

        return toDto(order);
    }

    /** 09.01.01.07 Track order — the caller's own submitted orders. */
    public List<OrderDto> listMyOrders(Long customerId) {
        resolveMembership(customerId);
        return orderRepository.findByRequestedByCustomerIdOrderByCreatedAtDesc(customerId).stream()
            .map(this::toDto)
            .toList();
    }

    /** Every order awaiting this org admin's decision. */
    public List<OrderDto> listPendingOrders(Long customerId) {
        OrganizationMember admin = requireManageOrders(customerId);
        return orderRepository.findByOrganizationIdAndStatusOrderByCreatedAtAsc(admin.getOrganizationId(), OrderStatus.SUBMITTED).stream()
            .map(this::toDto)
            .toList();
    }

    private void notifyOrgAdmins(Long organizationId, String title, String message) {
        memberRepository.findByOrganizationIdAndOrgRoleAndStatus(organizationId, OrgRole.ORG_ADMIN, MembershipStatus.ACTIVE)
            .forEach(admin -> customerRepository.findById(admin.getCustomerId()).ifPresent(customer ->
                notificationService.notify(customer.getId(), customer.getEmail(), NotificationCategory.ORDER, NotificationSeverity.INFO, title, message)));
    }

    private void notifyRequester(Order order, String title, String message) {
        customerRepository.findById(order.getRequestedByCustomerId()).ifPresent(customer ->
            notificationService.notify(customer.getId(), customer.getEmail(), NotificationCategory.ORDER, NotificationSeverity.INFO, title, message));
    }

    private OrderDto toDto(Order order) {
        String productName = productRepository.findById(order.getProductId()).map(Product::getName).orElse("Unknown product");
        String planName = order.getPlanId() == null ? null
            : productPlanRepository.findById(order.getPlanId()).map(ProductPlan::getName).orElse(null);
        String requestedByName = customerRepository.findById(order.getRequestedByCustomerId())
            .map(c -> c.getFirstName() + " " + c.getLastName()).orElse("Unknown");
        String decidedByName = order.getDecidedByCustomerId() == null ? null
            : customerRepository.findById(order.getDecidedByCustomerId()).map(c -> c.getFirstName() + " " + c.getLastName()).orElse(null);
        return new OrderDto(
            order.getId(), order.getProductId(), productName, order.getPlanId(), planName,
            order.getStatus(), order.getRequestedByCustomerId(), requestedByName,
            order.getDecidedByCustomerId(), decidedByName, order.getDecidedAt(), order.getDecisionNote(),
            order.getCreatedAt()
        );
    }
}
