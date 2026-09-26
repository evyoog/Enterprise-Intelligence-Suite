package com.vyoog.eisplatform.modules.servicestatus.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentRequest;
import com.vyoog.eisplatform.modules.servicestatus.dto.ProductStatusDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.ServiceStatusPageDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.UpdateProductStatusRequest;
import com.vyoog.eisplatform.modules.servicestatus.model.ProductServiceStatus;
import com.vyoog.eisplatform.modules.servicestatus.model.ServiceIncident;
import com.vyoog.eisplatform.modules.servicestatus.model.ServiceStatusValue;
import com.vyoog.eisplatform.modules.servicestatus.repository.ProductServiceStatusRepository;
import com.vyoog.eisplatform.modules.servicestatus.repository.ServiceIncidentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REQ-PRT-001 interim service status page (C20, decisions in C26).
 *
 * <ul>
 *   <li>Platform admins with {@code MANAGE_SERVICE_STATUS} post each product's
 *   status and post/update incidents (the URL gate is in SecurityConfig).</li>
 *   <li>Every signed-in customer sees every active product's status; incident
 *   details only for products their organization (or, for an individual,
 *   their own account) has an ACTIVE subscription to.</li>
 *   <li>{@code app.status-page.enabled} (default true) turns the customer view
 *   off; the admin screen keeps working so status can be prepared.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ServiceStatusService {

    private final ProductServiceStatusRepository statusRepository;
    private final ServiceIncidentRepository incidentRepository;
    private final ProductRepository productRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final OrganizationMemberRepository memberRepository;
    private final AuditService auditService;
    private final boolean enabled;

    public ServiceStatusService(ProductServiceStatusRepository statusRepository,
                                ServiceIncidentRepository incidentRepository,
                                ProductRepository productRepository,
                                ProductSubscriptionRepository subscriptionRepository,
                                OrganizationMemberRepository memberRepository,
                                AuditService auditService,
                                @Value("${app.status-page.enabled:true}") boolean enabled) {
        this.statusRepository = statusRepository;
        this.incidentRepository = incidentRepository;
        this.productRepository = productRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
        this.auditService = auditService;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** The customer view. {@code customerId} is null for a caller with no
     * customer account (e.g. a platform admin), who then sees status only. */
    public ServiceStatusPageDto customerView(Long customerId) {
        if (!enabled) {
            return new ServiceStatusPageDto(false, List.of(), List.of());
        }
        Set<Long> purchased = purchasedProductIds(customerId);
        List<Product> products = activeProducts();
        Map<Long, String> names = products.stream().collect(Collectors.toMap(Product::getId, Product::getName));
        List<ProductStatusDto> statuses = products.stream().map(p -> toStatusDto(p, purchased.contains(p.getId()))).toList();
        List<IncidentDto> incidents = purchased.isEmpty() ? List.of()
            : incidentRepository.findTop50ByProductIdInOrderByStartedAtDesc(purchased).stream()
                .filter(i -> names.containsKey(i.getProductId()))
                .map(i -> toIncidentDto(i, names))
                .toList();
        return new ServiceStatusPageDto(true, statuses, incidents);
    }

    /** The admin view: every active product and the latest incidents. */
    public ServiceStatusPageDto adminView() {
        List<Product> products = activeProducts();
        Map<Long, String> names = products.stream().collect(Collectors.toMap(Product::getId, Product::getName));
        return new ServiceStatusPageDto(enabled,
            products.stream().map(p -> toStatusDto(p, true)).toList(),
            incidentRepository.findTop200ByOrderByStartedAtDesc().stream().map(i -> toIncidentDto(i, names)).toList());
    }

    /** Products the viewer bought and that are not fully operational or have
     * an open incident — used for business-dashboard alerts. */
    public List<ProductStatusDto> affectedPurchasedProducts(Long customerId) {
        if (!enabled) {
            return List.of();
        }
        Set<Long> purchased = purchasedProductIds(customerId);
        return activeProducts().stream()
            .filter(p -> purchased.contains(p.getId()))
            .map(p -> toStatusDto(p, true))
            .filter(s -> s.status() != ServiceStatusValue.OPERATIONAL || s.openIncidents() > 0)
            .toList();
    }

    @Transactional
    public ProductStatusDto updateProductStatus(Long productId, UpdateProductStatusRequest request, String actorKeycloakSub) {
        Product product = requireActiveProduct(productId);
        ProductServiceStatus status = statusRepository.findById(productId).orElseGet(() -> {
            ProductServiceStatus fresh = new ProductServiceStatus();
            fresh.setProductId(productId);
            return fresh;
        });
        ServiceStatusValue previous = status.getStatus();
        status.setStatus(request.status());
        status.setNote(blankToNull(request.note()));
        status.setUpdatedAt(Instant.now());
        status.setUpdatedByKeycloakSub(actorKeycloakSub);
        statusRepository.save(status);
        auditService.recordSuccess("SERVICE_STATUS_CHANGED", actorKeycloakSub, null, null, "Product", productId.toString(), null,
            product.getName() + ": " + previous + " -> " + request.status());
        return toStatusDto(product, true);
    }

    @Transactional
    public IncidentDto createIncident(IncidentRequest request, String actorKeycloakSub) {
        Product product = requireActiveProduct(request.productId());
        ServiceIncident incident = new ServiceIncident();
        incident.setCreatedAt(Instant.now());
        incident.setCreatedByKeycloakSub(actorKeycloakSub);
        apply(incident, request);
        incident = incidentRepository.save(incident);
        auditService.recordSuccess("INCIDENT_POSTED", actorKeycloakSub, null, null, "ServiceIncident", incident.getId().toString(), null,
            product.getName() + ": " + incident.getTitle());
        return toIncidentDto(incident, Map.of(product.getId(), product.getName()));
    }

    @Transactional
    public IncidentDto updateIncident(Long incidentId, IncidentRequest request, String actorKeycloakSub) {
        ServiceIncident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        Product product = requireActiveProduct(request.productId());
        apply(incident, request);
        incident = incidentRepository.save(incident);
        auditService.recordSuccess("INCIDENT_UPDATED", actorKeycloakSub, null, null, "ServiceIncident", incidentId.toString(), null,
            product.getName() + ": " + incident.getTitle() + (incident.getEndedAt() == null ? " (open)" : " (resolved)"));
        return toIncidentDto(incident, Map.of(product.getId(), product.getName()));
    }

    private void apply(ServiceIncident incident, IncidentRequest request) {
        if (request.endedAt() != null && request.endedAt().isBefore(request.startedAt())) {
            throw new IllegalArgumentException("The end time must be after the start time.");
        }
        incident.setProductId(request.productId());
        incident.setTitle(request.title().trim());
        incident.setMessage(request.message().trim());
        incident.setStartedAt(request.startedAt());
        incident.setEndedAt(request.endedAt());
        incident.setUpdatedAt(Instant.now());
    }

    /** Organization member: the organization's ACTIVE subscriptions.
     * Individual: their own ACTIVE subscriptions. */
    private Set<Long> purchasedProductIds(Long customerId) {
        if (customerId == null) {
            return Set.of();
        }
        List<ProductSubscription> subscriptions = memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .map(m -> subscriptionRepository.findByOwnerOrganizationId(m.getOrganizationId()))
            .orElseGet(() -> subscriptionRepository.findByOwnerCustomerId(customerId));
        Set<Long> ids = new HashSet<>();
        for (ProductSubscription s : subscriptions) {
            if (s.getStatus() == SubscriptionStatus.ACTIVE) {
                ids.add(s.getProductId());
            }
        }
        return ids;
    }

    private List<Product> activeProducts() {
        return productRepository.findByStatus(ProductStatus.ACTIVE).stream()
            .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private Product requireActiveProduct(Long productId) {
        return productRepository.findById(productId)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private ProductStatusDto toStatusDto(Product product, boolean purchased) {
        ProductServiceStatus status = statusRepository.findById(product.getId()).orElse(null);
        return new ProductStatusDto(
            product.getId(), product.getName(),
            status == null ? ServiceStatusValue.OPERATIONAL : status.getStatus(),
            status == null ? null : status.getNote(),
            status == null ? null : status.getUpdatedAt(),
            purchased,
            incidentRepository.countByProductIdAndEndedAtIsNull(product.getId()));
    }

    private static IncidentDto toIncidentDto(ServiceIncident i, Map<Long, String> names) {
        return new IncidentDto(i.getId(), i.getProductId(), names.get(i.getProductId()), i.getTitle(), i.getMessage(),
            i.getStartedAt(), i.getEndedAt(), i.getEndedAt() == null);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
