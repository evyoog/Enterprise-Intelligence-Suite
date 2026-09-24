package com.vyoog.eisplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables @CreatedDate / @LastModifiedDate auditing on JPA entities
 * (see modules/product/model/Product.java).
 */
@Configuration
@EnableJpaAuditing
public class DatabaseConfig {
}
