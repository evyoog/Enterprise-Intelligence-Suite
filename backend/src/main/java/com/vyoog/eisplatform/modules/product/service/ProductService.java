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
 * .map(productMapper::toDto) below, which runs AFTER the repository call itself
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

    /** Public storefront listing — ACTIVE products only. */
    public List<ProductDto> listProducts() {
        return productRepository.findByStatus(ProductStatus.ACTIVE).stream()
            .map(productMapper::toDto)
            .toList();
    }

    /** Admin listing — every product regardless of status. */
    public List<ProductDto> listAllProducts() {
        return productRepository.findAll().stream()
            .map(productMapper::toDto)
            .toList();
    }

    public ProductDto getProduct(Long id) {
        return productRepository.findById(id)
            .map(productMapper::toDto)
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
            .map(productMapper::toDto)
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
        // mappedBy = "product" on Product.plans means ProductPlan owns the FK —
        // JPA needs it set on each child before save(), or it has nothing to
        // persist a product_id from.
        product.getPlans().forEach(plan -> plan.setProduct(product));
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
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
        product.setPlatforms(resolvePlatforms(request.platformIds()));

        product.getPlans().clear();
        List<ProductPlan> newPlans = request.plans() == null
            ? List.of()
            : request.plans().stream().map(productMapper::toEntity).toList();
        newPlans.forEach(plan -> plan.setProduct(product));
        product.getPlans().addAll(newPlans);

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
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
