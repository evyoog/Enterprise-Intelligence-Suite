package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.CreditNoteTaxLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditNoteTaxLineRepository extends JpaRepository<CreditNoteTaxLine, Long> {
    List<CreditNoteTaxLine> findByCreditNoteIdOrderBySortOrderAscIdAsc(Long creditNoteId);

    List<CreditNoteTaxLine> findByCreditNoteIdIn(java.util.Collection<Long> creditNoteIds);
}
