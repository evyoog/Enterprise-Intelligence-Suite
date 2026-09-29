package com.vyoog.eisplatform.modules.auth.repository;

import com.vyoog.eisplatform.modules.auth.model.MfaLoginChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfaLoginChallengeRepository extends JpaRepository<MfaLoginChallenge, String> {

    /** C30: removes sign-ins parked on a user's authenticator when an admin resets it. */
    @org.springframework.transaction.annotation.Transactional
    void deleteByCustomerId(Long customerId);
}
