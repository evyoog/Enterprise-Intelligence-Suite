package com.vyoog.eisplatform.modules.product.service;

import com.vyoog.eisplatform.common.exception.ProductInUseException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.platform.model.Platform;
import com.vyoog.eisplatform.modules.platform.repository.PlatformRepository;
import com.vyoog.eisplatform.modules.product.dto.CategoryFacet;
import com.vyoog.eisplatform.modules.product.dto.PlatformFacet;
import com.vyoog.eisplatform.modules.product.dto.ProductCreateRequest;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.dto.ProductSearchFacetsDto;
import com.vyoog.eisplatform.modules.product.dto.ProductSearchResponse;
import com.vyoog.eisplatform.modules.product.mapper.ProductMapper;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class-level @Transactional(readOnly = true): Product.plans is lazy-loaded, and
 * the mapper walks it while building each ProductDto — that happens in the
 * .map(this::toDtoWithDependencies) below, which runs AFTER the repository call itself
 * returns. Without an open transaction spanning that mapping step too, touching
 * product.getPlans() there would throw LazyInitializationException. createProduct
 * overrides back to a writable transaction since readOnly=true would reject its
 * save().
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final PlatformRepository platformRepository;
    private final List<ProductUsageGuard> usageGuards;

    /** platformIds -> Set<Platform> needs a repository lookup, so it can't be
     * done in the mapper (see ProductMapper's @Mapping(target="platforms", ignore)). */
    private Set<Platform> resolvePlatforms(List<Long> platformIds) {
        return platformIds == null || platformIds.isEmpty()
            ? new HashSet<>()
            : new HashSet<>(platformRepository.findAllById(platformIds));
    }

    /** 02.01.02.03 Define dependencies (sprint 2026.4.1) — same reason as
     * resolvePlatforms above. Refuses a product depending on itself. */
    private Set<Product> resolveDependencies(Long productId, List<Long> dependsOnProductIds) {
        if (dependsOnProductIds == null || dependsOnProductIds.isEmpty()) {
            return new HashSet<>();
        }
        if (productId != null && dependsOnProductIds.contains(productId)) {
            throw new IllegalArgumentException("A product cannot depend on itself.");
        }
        return new HashSet<>(productRepository.findAllById(dependsOnProductIds));
    }

    private ProductDto toDtoWithDependencies(Product product) {
        ProductDto dto = productMapper.toDto(product);
        dto.setDependsOnProductIds(product.getDependsOn().stream().map(Product::getId).sorted().toList());
        return dto;
    }

    /** Public storefront listing — ACTIVE products only. */
    public List<ProductDto> listProducts() {
        return productRepository.findByStatus(ProductStatus.ACTIVE).stream()
            .map(this::toDtoWithDependencies)
            .toList();
    }

    /** Admin listing — every product regardless of status. */
    public List<ProductDto> listAllProducts() {
        return productRepository.findAll().stream()
            .map(this::toDtoWithDependencies)
            .toList();
    }

    public ProductDto getProduct(Long id) {
        return productRepository.findById(id)
            .map(this::toDtoWithDependencies)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    /**
     * Phase 17: replaces the old client-side substring filter (see
     * ProductsPage's previous {@code useMemo}) with a real query — same text
     * match (name/description/category), plus category/platform filters and
     * sorting the client-side version never had. {@code includeInactive}
     * mirrors the existing listProducts/listAllProducts split: false for the
     * public storefront, true for the ADMIN-only catalog search.
     */
    public ProductSearchResponse searchProducts(
            String query, String category, Long platformId, String sortBy, String sortDir, boolean includeInactive) {
        Specification<Product> spec = Specification.where(null);
        if (!includeInactive) {
            spec = spec.and(ProductSpecifications.hasStatus(ProductStatus.ACTIVE));
        }
        if (query != null && !query.isBlank()) {
            spec = spec.and(ProductSpecifications.textMatches(query));
        }
        if (category != null && !category.isBlank()) {
            spec = spec.and(ProductSpecifications.hasCategory(category));
        }
        if (platformId != null) {
            spec = spec.and(ProductSpecifications.hasPlatform(platformId));
        }

        List<ProductDto> items = productRepository.findAll(spec, resolveSort(sortBy, sortDir)).stream()
            .map(this::toDtoWithDependencies)
            .toList();

        List<Product> facetScope = includeInactive
            ? productRepository.findAll()
            : productRepository.findByStatus(ProductStatus.ACTIVE);

        return new ProductSearchResponse(items, buildFacets(facetScope));
    }

    /** Whitelisted against a fixed set of real columns — never passes the
     * caller's own string straight into a Sort property (that would let a
     * request probe or sort by an arbitrary, possibly sensitive entity field). */
    private Sort resolveSort(String sortBy, String sortDir) {
        String property = "price".equalsIgnoreCase(sortBy) ? "price" : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    /** Always computed over the full (status-scoped) catalog, not re-narrowed
     * by the filters already applied to the search results — see
     * ProductSearchFacetsDto's own javadoc for why. */
    private ProductSearchFacetsDto buildFacets(List<Product> products) {
        Map<String, Long> categoryCounts = products.stream()
            .collect(Collectors.groupingBy(
                p -> Optional.ofNullable(p.getCategory()).filter(c -> !c.isBlank()).orElse("Other"),
                Collectors.counting()));
        List<CategoryFacet> categories = categoryCounts.entrySet().stream()
            .map(e -> new CategoryFacet(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(CategoryFacet::category))
            .toList();

        // A record (id, name) rather than the Platform entity itself as the
        // map key — grouping by entity identity would silently depend on
        // every instance coming from the same Hibernate persistence context.
        record PlatformRef(Long id, String name) {
        }
        Map<PlatformRef, Long> platformCounts = products.stream()
            .flatMap(p -> p.getPlatforms().stream())
            .map(pl -> new PlatformRef(pl.getId(), pl.getName()))
            .collect(Collectors.groupingBy(ref -> ref, Collectors.counting()));
        List<PlatformFacet> platforms = platformCounts.entrySet().stream()
            .map(e -> new PlatformFacet(e.getKey().id(), e.getKey().name(), e.getValue()))
            .sorted(Comparator.comparing(PlatformFacet::name))
            .toList();

        return new ProductSearchFacetsDto(categories, platforms);
    }

    /** MapStruct maps a null request.currency() straight through to
     * plan.currency = null, overriding the entity's own USD default (a
     * generated setter is always called, even with null) — same reason
     * createProduct/updateProduct re-apply the ProductStatus default below. */
    private void applyPlanDefaults(List<ProductPlan> plans) {
        plans.forEach(plan -> {
            if (plan.getCurrency() == null) {
                plan.setCurrency(Currency.USD);
            }
        });
    }

    @Transactional
    public ProductDto createProduct(ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        // MapStruct maps a null request.plans() to product.plans = null (not the
        // entity's default empty list) — calling .forEach() on that would NPE, so
        // this can't just trust the field is always a list.
        if (product.getPlans() == null) {
            product.setPlans(new ArrayList<>());
        }
        product.setPlatforms(resolvePlatforms(request.platformIds()));
        product.setDependsOn(resolveDependencies(null, request.dependsOnProductIds()));
        applyShowcase(product, request);
        applyPlanDefaults(product.getPlans());
        // mappedBy = "product" on Product.plans means ProductPlan owns the FK —
        // JPA needs it set on each child before save(), or it has nothing to
        // persist a product_id from.
        product.getPlans().forEach(plan -> plan.setProduct(product));
        Product saved = productRepository.save(product);
        return toDtoWithDependencies(saved);
    }

    /**
     * Tiers are fully replaced on every update, not diffed/merged against the
     * existing rows — a ProductPlan has no identity anything outside this
     * product refers to (no other table has a foreign key to it), so
     * recreating the list from the request is simpler than matching each
     * incoming tier back to an existing row by id. orphanRemoval = true on
     * Product.plans means clearing the collection actually deletes the old
     * rows at flush, not just detaches them.
     */
    @Transactional
    public ProductDto updateProduct(Long id, ProductCreateRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());
        product.setLaunchUrl(request.launchUrl());
        product.setCategory(request.category());
        product.setStatus(request.status() != null ? request.status() : ProductStatus.ACTIVE);
        product.setSsoConnected(request.ssoConnected() != null && request.ssoConnected());
        product.setFeatured(request.featured() != null && request.featured());
        product.setPlatforms(resolvePlatforms(request.platformIds()));
        product.setParentProductId(request.parentProductId());
        product.setVariantLabel(request.variantLabel());
        product.setDependsOn(resolveDependencies(id, request.dependsOnProductIds()));
        applyShowcase(product, request);
        // 02.01.01.03 Version product: every update after creation counts as
        // a new revision — see Product#version's own javadoc.
        product.setVersion((product.getVersion() == null ? 1 : product.getVersion()) + 1);

        product.getPlans().clear();
        List<ProductPlan> newPlans = request.plans() == null
            ? List.of()
            : request.plans().stream().map(productMapper::toEntity).toList();
        applyPlanDefaults(newPlans);
        newPlans.forEach(plan -> plan.setProduct(product));
        product.getPlans().addAll(newPlans);

        Product saved = productRepository.save(product);
        return toDtoWithDependencies(saved);
    }

    /** C66 showcase fields: blank values are stored as null; tags are
     * trimmed, de-duplicated and stored comma-separated (a comma inside a
     * tag is refused). */
    private static void applyShowcase(Product product, ProductCreateRequest request) {
        product.setAccentColor(blankToNull(request.accentColor()) == null ? null
            : request.accentColor().toUpperCase(java.util.Locale.ROOT));
        product.setDocumentationUrl(blankToNull(request.documentationUrl()));
        product.setSupportUrl(blankToNull(request.supportUrl()));
        if (request.featureTags() == null || request.featureTags().isEmpty()) {
            product.setFeatureTags(null);
            return;
        }
        java.util.LinkedHashSet<String> tags = new java.util.LinkedHashSet<>();
        for (String tag : request.featureTags()) {
            String t = tag == null ? "" : tag.trim();
            if (t.contains(",")) {
                throw new IllegalArgumentException("A feature tag cannot contain a comma: " + t);
            }
            if (!t.isEmpty()) {
                tags.add(t);
            }
        }
        product.setFeatureTags(tags.isEmpty() ? null : String.join(",", tags));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** 02.01.01.04 Publish product (sprint 2026.4.1): a named action for
     * moving a product (draft/INACTIVE, or a previously RETIRED one) onto
     * the public storefront — equivalent to updateProduct with
     * status=ACTIVE, but doesn't require re-sending the rest of the form. */
    @Transactional
    public ProductDto publishProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setStatus(ProductStatus.ACTIVE);
        return toDtoWithDependencies(productRepository.save(product));
    }

    /** 02.01.01.05 Retire product (sprint 2026.4.1): pulls it off the
     * storefront and blocks new subscriptions (see SubscriptionService#subscribe,
     * which already only offers ACTIVE products) without touching existing
     * subscriptions, product access or usage history the way deleteProduct's
     * usage guards would require. Reversible via {@link #publishProduct}. */
    @Transactional
    public ProductDto retireProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setStatus(ProductStatus.RETIRED);
        return toDtoWithDependencies(productRepository.save(product));
    }

    /**
     * Unlike status (the intended way to retire a product from the storefront —
     * see ProductStatus's own javadoc), this permanently removes the row. Tiers
     * and platform-grouping links cascade at the DB level (ON DELETE CASCADE on
     * product_plans/product_platforms), but subscription, org access, favorite
     * and usage rows do NOT — those are real customer history, so every
     * registered {@link ProductUsageGuard} is asked first, and the DB's own FK
     * violation (which the H2 test schema, generated from JPA annotations
     * rather than schema.sql, doesn't actually reproduce, unlike real Postgres)
     * is kept only as a defense-in-depth backstop, not the primary check.
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        String reasons = usageGuards.stream()
            .map(guard -> guard.blockingReason(id))
            .flatMap(Optional::stream)
            .collect(Collectors.joining(", "));
        if (!reasons.isEmpty()) {
            throw new ProductInUseException(
                "Cannot delete this product: " + reasons + ". Set its status to INACTIVE instead.");
        }

        try {
            productRepository.delete(product);
            productRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ProductInUseException(
                "Cannot delete a product that has subscriptions, access grants, favorites, or usage history. "
                    + "Set its status to INACTIVE instead.");
        }
    }
}
