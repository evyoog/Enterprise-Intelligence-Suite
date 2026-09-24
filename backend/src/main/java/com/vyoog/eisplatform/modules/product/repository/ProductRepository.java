package com.vyoog.eisplatform.modules.product.repository;

import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/** {@link JpaSpecificationExecutor} backs Phase 17's search — dynamic
 * name/category/platform/status predicates without hand-writing a query
 * method per combination (see ProductSpecifications). */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByStatus(ProductStatus status);
}
