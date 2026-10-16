package com.vyoog.eisplatform.modules.platformadmin.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Platform admin registry settings from {@code eis.platform-admins.*} (C108). Environment variables only,
 * no secret anywhere: S3 access is the AWS default credential chain.
 */
@Component
@Getter
public class PlatformAdminProperties {

    public static final String DEFAULT_LOCATION = "classpath:config/platform-admins.json";
    public static final long DEFAULT_REFRESH_SECONDS = 60;

    /** {@code classpath:...}, {@code file:/absolute/path} or {@code s3://bucket/key}. */
    private final String location;
    private final long refreshSeconds;
    private final String s3Region;
    private final String s3Endpoint;

    public PlatformAdminProperties(
        @Value("${eis.platform-admins.location:" + DEFAULT_LOCATION + "}") String location,
        @Value("${eis.platform-admins.refresh-seconds:60}") long refreshSeconds,
        @Value("${eis.platform-admins.s3.region:}") String s3Region,
        @Value("${eis.platform-admins.s3.endpoint:}") String s3Endpoint) {
        this.location = location == null || location.isBlank() ? DEFAULT_LOCATION : location.strip();
        this.refreshSeconds = Math.max(1, refreshSeconds);
        this.s3Region = s3Region == null ? "" : s3Region.strip();
        this.s3Endpoint = s3Endpoint == null ? "" : s3Endpoint.strip();
    }
}
