package com.vyoog.eisplatform.modules.auth.repository;

import com.vyoog.eisplatform.modules.auth.model.SsoBridgeSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SsoBridgeSessionRepository extends JpaRepository<SsoBridgeSession, String> {
}
