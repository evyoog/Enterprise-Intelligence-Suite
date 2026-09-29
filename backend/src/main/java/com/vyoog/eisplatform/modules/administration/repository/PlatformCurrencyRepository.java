package com.vyoog.eisplatform.modules.administration.repository;

import com.vyoog.eisplatform.modules.administration.model.PlatformCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformCurrencyRepository extends JpaRepository<PlatformCurrency, String> {
}
