package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.MyProductDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Individual customer's own products/subscriptions — everything here is
 * scoped to a single {@code ownerCustomerId} passed in by the controller
 * (resolved from the caller's own JWT `sub`, never trusted from the request
 * body — see MeController).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final ProductRepository productRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public List<MyProductDto> listMyProducts(Long customerId) {
        List<Product> activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE);
        Map<Long, ProductSubscription> byProductId = subscriptionRepository.findByOwnerCustomerId(customerId).stream()
            .collect(Collectors.toMap(ProductSubscription::getProductId, s -> s, (a, b) -> a));

        return activeProducts.stream()
            .map(product -> {
                ProductSubscription subscription = byProductId.get(product.getId());
                return new MyProductDto(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    subscription == null ? null : subscription.getStatus()
                );
            })
            .toList();
    }

    public List<SubscriptionDto> listMySubscriptions(Long customerId) {
        return subscriptionRepository.findByOwnerCustomerId(customerId).stream()
            .map(this::toDto)
            .toList();
    }

    /** Phase 19: the business dashboard's own billing section — same
     * mapping as listMySubscriptions, just organization-owned rows instead
     * of individual-owned ones. */
    public List<SubscriptionDto> listOrganizationSubscriptions(Long organizationId) {
        return subscriptionRepository.findByOwnerOrganizationId(organizationId).stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * No payment integration exists yet (see ProductSubscription's own
     * javadoc) — subscribing goes straight to ACTIVE rather than pretending
     * a payment happened. This is the seam a real payment step slots into
     * later: insert it between this method's validation and the
     * status/startedAt assignment below, without changing the API shape.
     */
    @Transactional
    public SubscriptionDto subscribe(Long customerId, Long productId) {
        Product product = productRepository.findById(productId)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductSubscription subscription = subscriptionRepository
            .findByOwnerCustomerIdAndProductId(customerId, productId)
            .orElseGet(() -> {
                ProductSubscription created = new ProductSubscription();
                created.setProductId(productId);
                created.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
                created.setOwnerCustomerId(customerId);
                return created;
            });
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(null);
        subscription = subscriptionRepository.save(subscription);

        customerRepository.findById(customerId).ifPresent(customer ->
            notificationService.notify(customerId, customer.getEmail(), NotificationCategory.SUBSCRIPTION, NotificationSeverity.INFO,
                "Subscribed to " + product.getName(),
                "You're now subscribed to " + product.getName() + "."));
        auditService.recordSuccess("SUBSCRIPTION_CREATED", null, customerId, null,
            "ProductSubscription", productId.toString(), null, "Customer subscribed to product " + productId);

        return toDto(subscription, product.getName());
    }

    private SubscriptionDto toDto(ProductSubscription subscription) {
        String productName = productRepository.findById(subscription.getProductId())
            .map(Product::getName)
            .orElse("Unknown product");
        return toDto(subscription, productName);
    }

    private SubscriptionDto toDto(ProductSubscription subscription, String productName) {
        return new SubscriptionDto(
            subscription.getId(),
            subscription.getProductId(),
            productName,
            subscription.getStatus(),
            subscription.getStartedAt(),
            subscription.getExpiresAt()
        );
    }
}
