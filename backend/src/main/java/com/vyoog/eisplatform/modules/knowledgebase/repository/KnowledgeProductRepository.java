package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KnowledgeProductRepository extends JpaRepository<KnowledgeProduct, Long> {

    List<KnowledgeProduct> findAllByOrderByDisplayOrderAscNameAsc();

    Optional<KnowledgeProduct> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /** The catalog product with this name, to link a seeded knowledge product
     * to it without depending on the product module's internals. */
    @Query(value = "SELECT id FROM products WHERE lower(name) = lower(:name) ORDER BY id LIMIT 1", nativeQuery = true)
    Optional<Long> findCatalogProductIdByName(@Param("name") String name);
}
