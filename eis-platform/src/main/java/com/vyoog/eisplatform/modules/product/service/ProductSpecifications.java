package com.vyoog.eisplatform.modules.product.service;

import com.vyoog.eisplatform.modules.platform.model.Platform;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/** Dynamic predicates for Phase 17's product search — kept as plain
 * composable {@link Specification}s rather than one query-method per filter
 * combination, since the search endpoint accepts any combination of text,
 * category, platform, and status. */
final class ProductSpecifications {

    private ProductSpecifications() {
    }

    /** Case-insensitive substring match across name, description, and
     * category — the same three fields the old client-side filter checked
     * (see ProductsPage's previous `useMemo`), now done in the database. */
    static Specification<Product> textMatches(String query) {
        String like = "%" + query.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
            cb.like(cb.lower(root.get("name")), like),
            cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like),
            cb.like(cb.lower(cb.coalesce(root.get("category"), "")), like));
    }

    static Specification<Product> hasCategory(String category) {
        return (root, cq, cb) -> cb.equal(root.get("category"), category);
    }

    static Specification<Product> hasPlatform(Long platformId) {
        return (root, cq, cb) -> {
            cq.distinct(true);
            Join<Product, Platform> join = root.join("platforms");
            return cb.equal(join.get("id"), platformId);
        };
    }

    static Specification<Product> hasStatus(ProductStatus status) {
        return (root, cq, cb) -> cb.equal(root.get("status"), status);
    }
}
