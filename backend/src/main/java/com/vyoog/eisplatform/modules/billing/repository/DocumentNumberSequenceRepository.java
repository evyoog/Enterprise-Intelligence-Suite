package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.DocumentNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentNumberSequenceRepository extends JpaRepository<DocumentNumberSequence, DocumentNumberSequence.Key> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DocumentNumberSequence s where s.series = :series and s.financialYear = :fy")
    Optional<DocumentNumberSequence> lockBySeriesAndYear(@Param("series") String series, @Param("fy") String fy);
}
