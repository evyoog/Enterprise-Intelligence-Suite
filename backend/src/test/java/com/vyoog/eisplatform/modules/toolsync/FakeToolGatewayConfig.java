package com.vyoog.eisplatform.modules.toolsync;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Replaces the MCP gateway with {@link FakeToolGateway} in the tests of the synchronization. */
@TestConfiguration
public class FakeToolGatewayConfig {

    @Bean
    @Primary
    public FakeToolGateway fakeToolGateway() {
        return new FakeToolGateway();
    }
}
