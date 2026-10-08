package com.vyoog.eisplatform.modules.cart.service;

import com.vyoog.eisplatform.common.exception.CartConflictException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.repository.BillingDetailsRepository;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.cart.dto.AddCartItemRequest;
import com.vyoog.eisplatform.modules.cart.dto.CartCheckoutResultDto;
import com.vyoog.eisplatform.modules.cart.dto.CartDto;
import com.vyoog.eisplatform.modules.cart.dto.CartIssueDto;
import com.vyoog.eisplatform.modules.cart.dto.CartItemDto;
import com.vyoog.eisplatform.modules.cart.dto.CartPlanOptionDto;
import com.vyoog.eisplatform.modules.cart.dto.CartValidationDto;
import com.vyoog.eisplatform.modules.cart.dto.UpdateCartItemRequest;
import com.vyoog.eisplatform.modules.cart.model.Cart;
import com.vyoog.eisplatform.modules.cart.model.CartItem;
import com.vyoog.eisplatform.modules.cart.repository.CartRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.SubmitOrderRequest;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrderService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * C59, REQ-MKT-003: the signed-in user's server-side cart, purchase
 * validation and checkout. Cart changes are shopping activity and are not
 * audited (BR-11); the checkout reuses the existing subscribe, invoice and
 * order services, which audit themselves.
 *
 * <p>Engineering defaults until REQ-MKT-003's open questions are answered
 * (recorded under C59): a cart may hold several products; an individual's
 * checkout bills them on <b>one</b> invoice (Open question 1); an
 * organization member's checkout submits <b>one order per item</b>, because
 * an order holds one product (REQ-ORD-001); an organization admin's own cart
 * also goes through approval (Open question 2); items never expire (Open
 * question 3).
 */
@Service
@RequiredArgsConstructor
public class CartService {

    public static final String INDIVIDUAL = "INDIVIDUAL";
    public static final String ORGANIZATION_MEMBER = "ORGANIZATION_MEMBER";

    /** BR-10: a repeated checkout within this window after a successful one
     * returns that same checkout instead of "cart is empty". */
    private static final Duration IDEMPOTENCY_WINDOW = Duration.ofMinutes(2);

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductPlanRepository productPlanRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final OrganizationMemberRepository memberRepository;
    private final BillingDetailsRepository billingDetailsRepository;
    private final SubscriptionService subscriptionService;
    private final InvoiceService invoiceService;
    private final OrderService orderService;
    /** REQ-CAT-005 (C85): per-product audience rule. */
    private final com.vyoog.eisplatform.modules.offering.service.EligibilityService eligibilityService;
    /** REQ-INT-002 (C62): CheckoutCompleted. */
    private final com.vyoog.eisplatform.modules.integration.service.OutboxService outboxService;

    @Transactional(readOnly = true)
    public CartDto getCart(Long customerId) {
        return toDto(customerId, cartRepository.findByCustomerId(customerId).orElse(null));
    }

    /** BR-2, BR-3: paid plans only; a product already in the cart has its plan replaced. */
    @Transactional
    public CartDto addItem(Long customerId, AddCartItemRequest request) {
        Product product = productRepository.findById(request.productId())
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        ProductPlan plan = planOf(product, request.planId());
        long price = minorUnits(plan.getPrice());
        if (price <= 0) {
            throw new IllegalArgumentException("Free plans are not added to the cart.");
        }

        Cart cart = cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Cart created = new Cart();
            created.setCustomerId(customerId);
            return created;
        });
        CartItem item = cart.getItems().stream()
            .filter(i -> i.getProductId().equals(product.getId()))
            .findFirst()
            .orElseGet(() -> {
                CartItem created = new CartItem();
                created.setCart(cart);
                created.setProductId(product.getId());
                created.setAddedAt(Instant.now());
                cart.getItems().add(created);
                return created;
            });
        item.setPlanId(plan.getId());
        item.setUnitPriceAtAdd(price);
        item.setCurrency(plan.getCurrency());
        cart.setUpdatedAt(Instant.now());
        return toDto(customerId, cartRepository.save(cart));
    }

    /** BR-4 (change plan within the same product) or BR-7 (confirm a changed price). */
    @Transactional
    public CartDto updateItem(Long customerId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = ownCart(customerId);
        CartItem item = ownItem(cart, itemId);
        Product product = productRepository.findById(item.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (request.planId() != null) {
            ProductPlan plan = planOf(product, request.planId());
            long price = minorUnits(plan.getPrice());
            if (price <= 0) {
                throw new IllegalArgumentException("Free plans are not added to the cart.");
            }
            item.setPlanId(plan.getId());
            item.setUnitPriceAtAdd(price);
            item.setCurrency(plan.getCurrency());
        } else if (Boolean.TRUE.equals(request.confirmPrice())) {
            ProductPlan plan = productPlanRepository.findById(item.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("This plan is no longer available. Choose another plan."));
            item.setUnitPriceAtAdd(minorUnits(plan.getPrice()));
            item.setCurrency(plan.getCurrency());
        } else {
            throw new IllegalArgumentException("planId or confirmPrice is required");
        }
        cart.setUpdatedAt(Instant.now());
        return toDto(customerId, cartRepository.save(cart));
    }

    @Transactional
    public CartDto removeItem(Long customerId, Long itemId) {
        Cart cart = ownCart(customerId);
        CartItem item = ownItem(cart, itemId);
        cart.getItems().remove(item);
        cart.setUpdatedAt(Instant.now());
        return toDto(customerId, cartRepository.save(cart));
    }

    @Transactional
    public void clear(Long customerId) {
        cartRepository.findByCustomerId(customerId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setUpdatedAt(Instant.now());
            cartRepository.save(cart);
        });
    }

    @Transactional(readOnly = true)
    public CartValidationDto validate(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId).orElse(null);
        List<CartIssueDto> issues = cart == null ? List.of() : issuesFor(customerId, cart);
        return new CartValidationDto(issues.isEmpty(), issues);
    }

    /** REQ-MKT-003.8, BR-6/7/9/10. Validates again, then creates the
     * subscriptions and one invoice (individual) or one order per item
     * (organization member), and empties the cart — all in one
     * transaction, under a lock on the cart row. */
    @Transactional
    public CartCheckoutResultDto checkout(Long customerId) {
        Cart cart = cartRepository.lockByCustomerId(customerId).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            if (cart != null && cart.getLastCheckoutAt() != null
                    && cart.getLastCheckoutAt().isAfter(Instant.now().minus(IDEMPOTENCY_WINDOW))) {
                return lastCheckout(cart);
            }
            throw new CartConflictException("CART_EMPTY", "Your cart is empty.", null);
        }
        List<CartIssueDto> issues = issuesFor(customerId, cart);
        if (!issues.isEmpty()) {
            throw new CartConflictException("CART_INVALID", "Some items in your cart need your attention.", issues);
        }

        CartCheckoutResultDto result;
        if (membership(customerId).isPresent()) {
            List<Long> orderIds = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                orderIds.add(orderService.submitOrder(customerId, new SubmitOrderRequest(item.getProductId(), item.getPlanId())).id());
            }
            result = new CartCheckoutResultDto("ORDER", null, orderIds);
            cart.setLastCheckoutKind("ORDER");
            cart.setLastCheckoutRef(orderIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        } else {
            List<Long> subscriptionIds = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                subscriptionIds.add(subscriptionService.subscribeFromCart(customerId, item.getProductId(), item.getPlanId()));
            }
            Invoice invoice = invoiceService.generateForSubscriptions(subscriptionIds);
            if (invoice == null) {
                throw new IllegalStateException("A cart of paid plans produced no invoice");
            }
            result = new CartCheckoutResultDto("INVOICE", invoice.getId(), null);
            cart.setLastCheckoutKind("INVOICE");
            cart.setLastCheckoutRef(invoice.getId().toString());
        }
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("cartId", cart.getId());
        payload.put("customerId", customerId);
        payload.put("result", result.kind());
        payload.put("invoiceId", result.invoiceId());
        payload.put("orderIds", result.orderIds());
        outboxService.publish(com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes.CHECKOUT_COMPLETED,
            com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes.AGGREGATE_CART, cart.getId(), payload);
        cart.setLastCheckoutAt(Instant.now());
        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        return result;
    }

    private CartCheckoutResultDto lastCheckout(Cart cart) {
        if ("INVOICE".equals(cart.getLastCheckoutKind())) {
            return new CartCheckoutResultDto("INVOICE", Long.valueOf(cart.getLastCheckoutRef()), null);
        }
        List<Long> orderIds = Arrays.stream(cart.getLastCheckoutRef().split(",")).map(Long::valueOf).toList();
        return new CartCheckoutResultDto("ORDER", null, orderIds);
    }

    /** BR-6. Issues are calculated on demand and never stored. */
    private List<CartIssueDto> issuesFor(Long customerId, Cart cart) {
        Optional<OrganizationMember> member = membership(customerId);
        Set<Long> productsInCart = cart.getItems().stream().map(CartItem::getProductId).collect(Collectors.toSet());
        var cartCurrency = cart.getItems().isEmpty() ? null : cart.getItems().get(0).getCurrency();
        List<CartIssueDto> issues = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null || product.getStatus() != ProductStatus.ACTIVE) {
                issues.add(new CartIssueDto(item.getId(), "NOT_AVAILABLE", "PRODUCT", null, null, null, null));
                continue;
            }
            ProductPlan plan = productPlanRepository.findById(item.getPlanId())
                .filter(p -> p.getProduct().getId().equals(product.getId()))
                .orElse(null);
            if (plan == null) {
                issues.add(new CartIssueDto(item.getId(), "NOT_AVAILABLE", "PLAN", null, null, null, null));
                continue;
            }
            long price = minorUnits(plan.getPrice());
            if (price != item.getUnitPriceAtAdd() || plan.getCurrency() != item.getCurrency()) {
                issues.add(new CartIssueDto(item.getId(), "PRICE_CHANGED", null, item.getUnitPriceAtAdd(), price, null, null));
            }
            if (item.getCurrency() != cartCurrency) {
                issues.add(CartIssueDto.of(item.getId(), "CURRENCY_MISMATCH"));
            }
            if (!eligibilityService.isEligible(product.getId(), member.isPresent())) {
                issues.add(CartIssueDto.of(item.getId(), "NOT_ELIGIBLE"));
            }
            if (hasActiveSubscription(customerId, member, product.getId())) {
                issues.add(CartIssueDto.of(item.getId(), "ALREADY_SUBSCRIBED"));
            }
            product.getDependsOn().stream()
                .filter(required -> !productsInCart.contains(required.getId()) && !hasActiveSubscription(customerId, member, required.getId()))
                .sorted(Comparator.comparing(Product::getId))
                .forEach(required -> issues.add(new CartIssueDto(item.getId(), "MISSING_DEPENDENCY", null, null, null,
                    required.getId(), required.getName())));
        }
        return issues;
    }

    private boolean hasActiveSubscription(Long customerId, Optional<OrganizationMember> member, Long productId) {
        Optional<ProductSubscription> subscription = member.isPresent()
            ? subscriptionRepository.findByOwnerOrganizationIdAndProductId(member.get().getOrganizationId(), productId)
            : subscriptionRepository.findByOwnerCustomerIdAndProductId(customerId, productId);
        return subscription.map(s -> s.getStatus() == SubscriptionStatus.ACTIVE).orElse(false);
    }

    private Optional<OrganizationMember> membership(Long customerId) {
        return memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE);
    }

    private Cart ownCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }

    /** BR-1: another user's item is a generic 404. */
    private CartItem ownItem(Cart cart, Long itemId) {
        return cart.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }

    private ProductPlan planOf(Product product, Long planId) {
        ProductPlan plan = productPlanRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (!plan.getProduct().getId().equals(product.getId())) {
            throw new IllegalArgumentException("That plan does not belong to this product.");
        }
        return plan;
    }

    private CartDto toDto(Long customerId, Cart cart) {
        boolean organizationMember = membership(customerId).isPresent();
        String continueAs = organizationMember ? ORGANIZATION_MEMBER : INDIVIDUAL;
        // BR-8: tax needs the billing region; the tax engine (REQ-BIL-002) is
        // not built yet, so with billing details the cart shows no tax lines.
        boolean hasBillingDetails = organizationMember
            ? billingDetailsRepository.findByOwnerOrganizationId(membership(customerId).get().getOrganizationId()).isPresent()
            : billingDetailsRepository.findByOwnerCustomerId(customerId).isPresent();
        if (cart == null || cart.getItems().isEmpty()) {
            return new CartDto(List.of(), 0, null, 0, List.of(), !hasBillingDetails, 0, continueAs);
        }
        List<CartItemDto> items = cart.getItems().stream().map(this::toItemDto).toList();
        long subtotal = items.stream().mapToLong(CartItemDto::amount).sum();
        return new CartDto(items, items.size(), items.get(0).currency(), subtotal, List.of(), !hasBillingDetails, subtotal, continueAs);
    }

    private CartItemDto toItemDto(CartItem item) {
        Product product = productRepository.findById(item.getProductId()).orElse(null);
        ProductPlan plan = productPlanRepository.findById(item.getPlanId()).orElse(null);
        long unitPrice = plan != null ? minorUnits(plan.getPrice()) : item.getUnitPriceAtAdd();
        List<CartPlanOptionDto> plans = product == null ? List.of() : product.getPlans().stream()
            .filter(p -> minorUnits(p.getPrice()) > 0)
            .sorted(Comparator.comparing((ProductPlan p) -> p.getSortOrder() == null ? Integer.MAX_VALUE : p.getSortOrder())
                .thenComparing(ProductPlan::getId))
            .map(p -> new CartPlanOptionDto(p.getId(), p.getName(), p.getBillingPeriod().name(), minorUnits(p.getPrice()), p.getCurrency().name()))
            .toList();
        return new CartItemDto(
            item.getId(),
            item.getProductId(),
            product != null ? product.getName() : null,
            product != null ? product.getImageUrl() : null,
            item.getPlanId(),
            plan != null ? plan.getName() : null,
            plan != null ? plan.getBillingPeriod().name() : null,
            unitPrice,
            item.getUnitPriceAtAdd(),
            (plan != null ? plan.getCurrency() : item.getCurrency()).name(),
            unitPrice,
            item.getAddedAt(),
            plans);
    }

    private static long minorUnits(BigDecimal price) {
        return price == null ? 0 : price.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
