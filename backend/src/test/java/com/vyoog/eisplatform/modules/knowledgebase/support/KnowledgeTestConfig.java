package com.vyoog.eisplatform.modules.knowledgebase.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Replaces S3 with {@link InMemoryMediaStorage} for knowledge tests. */
@TestConfiguration
public class KnowledgeTestConfig {

    @Bean
    @Primary
    public InMemoryMediaStorage inMemoryMediaStorage() {
        return new InMemoryMediaStorage();
    }
}
