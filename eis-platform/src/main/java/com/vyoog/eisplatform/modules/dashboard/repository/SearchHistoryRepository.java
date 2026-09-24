package com.vyoog.eisplatform.modules.dashboard.repository;

import com.vyoog.eisplatform.modules.dashboard.model.SearchHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntry, Long> {

    List<SearchHistoryEntry> findTop10ByCustomerIdOrderBySearchedAtDesc(Long customerId);

    void deleteByCustomerIdAndQueryIgnoreCase(Long customerId, String query);

    void deleteByCustomerId(Long customerId);
}
