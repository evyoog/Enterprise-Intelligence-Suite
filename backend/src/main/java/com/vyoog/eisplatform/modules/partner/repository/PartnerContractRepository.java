package com.vyoog.eisplatform.modules.partner.repository;

import com.vyoog.eisplatform.modules.partner.model.ContractStatus;
import com.vyoog.eisplatform.modules.partner.model.PartnerContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PartnerContractRepository extends JpaRepository<PartnerContract, Long> {

    Optional<PartnerContract> findByProviderId(Long providerId);

    List<PartnerContract> findByStatusAndEndDateBefore(ContractStatus status, LocalDate date);
}
