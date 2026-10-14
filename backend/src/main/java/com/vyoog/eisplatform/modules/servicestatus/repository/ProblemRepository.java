package com.vyoog.eisplatform.modules.servicestatus.repository;

import com.vyoog.eisplatform.modules.servicestatus.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findAllByOrderByCreatedAtDesc();
}
