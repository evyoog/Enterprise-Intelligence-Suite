package com.vyoog.eisplatform.modules.offering.repository;

import com.vyoog.eisplatform.modules.offering.model.Offering;
import com.vyoog.eisplatform.modules.offering.model.OfferingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferingRepository extends JpaRepository<Offering, Long> {

    List<Offering> findByStatusOrderByNameAsc(OfferingStatus status);

    boolean existsByNameIgnoreCase(String name);

    List<Offering> findAllByOrderByNameAsc();
}
