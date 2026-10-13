package com.vyoog.eisplatform.modules.billing.tax.repository;

import com.vyoog.eisplatform.modules.billing.tax.model.RuleApprovalStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {

    List<TaxRule> findAllByOrderByCountryCodeAscNameAscVersionDesc();

    List<TaxRule> findByRuleKeyOrderByVersionDesc(String ruleKey);

    Optional<TaxRule> findFirstByRuleKeyOrderByVersionDesc(String ruleKey);

    /** BR-11: the rules the internal engine may use for a country on a date. Components are loaded eagerly. */
    @Query("select distinct r from TaxRule r where r.countryCode = :country and r.approvalStatus = :approved and r.active = true "
        + "and r.effectiveFrom <= :date and (r.effectiveTo is null or r.effectiveTo >= :date)")
    List<TaxRule> findInForce(@Param("country") String country, @Param("approved") RuleApprovalStatus approved, @Param("date") LocalDate date);

    /** BR-23: approved rules of the same country, for the overlap check. */
    List<TaxRule> findByCountryCodeAndApprovalStatus(String countryCode, RuleApprovalStatus status);

    long countByApprovalStatusAndActiveTrue(RuleApprovalStatus status);

    @Query("select count(r) from TaxRule r where r.approvalStatus = :approved and r.active = true and r.effectiveFrom <= :date "
        + "and (r.effectiveTo is null or r.effectiveTo >= :date)")
    long countInForce(@Param("approved") RuleApprovalStatus approved, @Param("date") LocalDate date);

    long countByApprovalStatus(RuleApprovalStatus status);
}
