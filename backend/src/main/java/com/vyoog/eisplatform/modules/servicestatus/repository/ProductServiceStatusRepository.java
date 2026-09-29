package com.vyoog.eisplatform.modules.servicestatus.repository;

import com.vyoog.eisplatform.modules.servicestatus.model.ProductServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductServiceStatusRepository extends JpaRepository<ProductServiceStatus, Long> {
}
