package com.vyoog.eisplatform.modules.product.repository;

import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** {@link JpaSpecificationExecutor} backs Phase 17's search — dynamic
 * name/category/platform/status predicates without hand-writing a query
 * method per combination (see ProductSpecifications). */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByStatus(ProductStatus status);

    /** Looks a product up by its unique name. */
    Optional<Product> findByName(String name);

    /** 03.01.02 Show featured products (sprint 2027.1.2). */
    List<Product> findByStatusAndFeaturedTrue(ProductStatus status);

    /** Platform admin dashboard (C53): a count query instead of loading
     * every row just to call {@code .size()}. */
    long countByStatus(ProductStatus status);

    /** 02.01.02.01 Define product hierarchy — used by ProductStructureUsageGuard
     * to block deleting a product that other products still list as their parent. */
    boolean existsByParentProductId(Long parentProductId);

    /** 02.01.02.03 Define dependencies — same reason, for the other direction
     * of the relationship (Product.dependsOn has no "owning side" query method
     * Spring Data can derive, so this is hand-written JPQL). */
    @Query("select count(p) > 0 from Product p join p.dependsOn d where d.id = :productId")
    boolean existsAsDependencyOf(@Param("productId") Long productId);
}
