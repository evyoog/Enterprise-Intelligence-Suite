package com.vyoog.eisplatform.modules.auth.repository;

import com.vyoog.eisplatform.modules.auth.model.MfaLoginChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfaLoginChallengeRepository extends JpaRepository<MfaLoginChallenge, String> {
}
