package com.vyoog.eisplatform.modules.platformadmin.config;

import com.vyoog.eisplatform.modules.platformadmin.service.PlatformAdminProperties;
import com.vyoog.eisplatform.modules.platformadmin.service.PlatformAdminSource;
import com.vyoog.eisplatform.modules.platformadmin.service.PlatformAdminSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Picks the platform admin registry source (project file or S3) from {@code eis.platform-admins.location} (C108). */
@Configuration
public class PlatformAdminConfig {

    @Bean
    PlatformAdminSource platformAdminSource(PlatformAdminProperties properties) {
        return PlatformAdminSourceFactory.create(properties);
    }
}
