package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.support.PostgresSearchTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ActiveProfiles;

/** C70: the pre-C70 search behaviour (GlobalSearchServiceTest) must still
 * hold on the PostgreSQL index engine. */
@ActiveProfiles({"test", "pgtest"})
@EnabledIfEnvironmentVariable(named = "EIS_PG_TEST_URL", matches = ".+")
class PostgresGlobalSearchServiceTest extends GlobalSearchServiceTest {

    @BeforeAll
    static void prepare() throws Exception {
        PostgresSearchTestSupport.prepareSchema();
    }
}
