package com.vyoog.eisplatform.modules.search.support;

import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * C70: prepares the PostgreSQL database for the search integration tests —
 * drops and recreates the {@code eis_search_it} schema and loads
 * {@code schema.sql} into it (once per test JVM).
 */
public final class PostgresSearchTestSupport {

    public static final String SCHEMA = "eis_search_it";
    private static boolean prepared;

    private PostgresSearchTestSupport() {
    }

    public static synchronized void prepareSchema() throws Exception {
        if (prepared) {
            return;
        }
        String url = System.getenv("EIS_PG_TEST_URL");
        String user = env("EIS_PG_TEST_USER", "postgres");
        String password = env("EIS_PG_TEST_PASSWORD", "postgres");
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
            statement.execute("CREATE SCHEMA " + SCHEMA);
            statement.execute("SET search_path TO " + SCHEMA + ", public");
            ScriptUtils.executeSqlScript(connection,
                new FileSystemResource(Path.of("src/main/resources/db/schema.sql")));
        }
        prepared = true;
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
