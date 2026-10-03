package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.integration.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.time.Clock;

/** REQ-INT-001 (C61): the API-key, rate-limit and version filters. The first
 * two are added to the security chain only (SecurityConfig); the version
 * filter runs before Spring Security. */
@Configuration
public class ApiManagementConfig {

    @Bean
    public FilterRegistrationBean<ApiVersionFilter> apiVersionFilter() {
        FilterRegistrationBean<ApiVersionFilter> registration = new FilterRegistrationBean<>(new ApiVersionFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        return new ApiKeyAuthenticationFilter(apiKeyService);
    }

    @Bean
    public RateLimitFilter rateLimitFilter(@Value("${app.rate-limit.per-key:120}") int perKey,
                                           @Value("${app.rate-limit.per-user:300}") int perUser,
                                           @Value("${app.rate-limit.per-ip:60}") int perIp) {
        return new RateLimitFilter(perKey, perUser, perIp, Clock.systemUTC());
    }

    /** Beans of type Filter are auto-registered as servlet filters by Spring
     * Boot; these two belong only in the security chain. */
    @Bean
    public FilterRegistrationBean<ApiKeyAuthenticationFilter> apiKeyFilterRegistration(ApiKeyAuthenticationFilter filter) {
        FilterRegistrationBean<ApiKeyAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
