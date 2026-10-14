package com.vyoog.eisplatform.modules.servicestatus.repository;

import com.vyoog.eisplatform.modules.servicestatus.model.ProblemAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemActionRepository extends JpaRepository<ProblemAction, Long> {

    List<ProblemAction> findByProblemIdOrderByIdAsc(Long problemId);

    long countByProblemIdAndDoneFalse(Long problemId);

    long countByProblemId(Long problemId);
}
