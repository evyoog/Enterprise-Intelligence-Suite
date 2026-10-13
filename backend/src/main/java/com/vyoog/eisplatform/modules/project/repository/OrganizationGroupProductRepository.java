package com.vyoog.eisplatform.modules.project.repository;

import com.vyoog.eisplatform.modules.project.model.OrganizationGroupProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationGroupProductRepository extends JpaRepository<OrganizationGroupProduct, Long> {
    List<OrganizationGroupProduct> findByGroupId(Long groupId);

    Optional<OrganizationGroupProduct> findByGroupIdAndProductId(Long groupId, Long productId);

    void deleteByGroupId(Long groupId);
}
