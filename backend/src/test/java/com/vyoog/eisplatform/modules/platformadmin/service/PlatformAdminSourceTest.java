package com.vyoog.eisplatform.modules.platformadmin.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The platform admin registry sources (C108): classpath, file and S3 (through the fetch seam, no AWS). */
class PlatformAdminSourceTest {

    @TempDir
    Path temp;

    @Test
    void theClasspathSourceReadsAProjectResource() throws IOException {
        PlatformAdminSource source = PlatformAdminSourceFactory.create("classpath:config/platform-admins.json", (b, k) -> "unused");
        assertThat(source.scheme()).isEqualTo("classpath");
        assertThat(source.read()).contains("\"platformAdmins\"");
        assertThat(new ClasspathFilePlatformAdminSource("classpath:/config/platform-admins.json").read()).contains("\"version\"");
    }

    @Test
    void aMissingClasspathResourceIsAnError() {
        assertThatThrownBy(() -> new ClasspathFilePlatformAdminSource("classpath:config/nope.json").read()).isInstanceOf(IOException.class);
    }

    @Test
    void theFileSourceReadsAnAbsolutePathInBothUriForms() throws IOException {
        Path file = temp.resolve("admins.json");
        Files.writeString(file, "{\"platformAdmins\":[]}", StandardCharsets.UTF_8);

        PlatformAdminSource plain = PlatformAdminSourceFactory.create("file:" + file, (b, k) -> "unused");
        PlatformAdminSource triple = PlatformAdminSourceFactory.create("file://" + file, (b, k) -> "unused");
        assertThat(plain.scheme()).isEqualTo("file");
        assertThat(plain.read()).isEqualTo("{\"platformAdmins\":[]}");
        assertThat(triple.read()).isEqualTo("{\"platformAdmins\":[]}");
    }

    @Test
    void aMissingFileIsAnError() {
        PlatformAdminSource source = PlatformAdminSourceFactory.create("file:" + temp.resolve("missing.json"), (b, k) -> "unused");
        assertThatThrownBy(source::read).isInstanceOf(IOException.class);
    }

    @Test
    void theS3SourceSplitsBucketAndKeyAndFetchesThroughTheSeam() throws IOException {
        List<String> calls = new ArrayList<>();
        S3PlatformAdminSource source = new S3PlatformAdminSource("s3://my-bucket/folder/sub/platform-admins.json", (bucket, key) -> {
            calls.add(bucket + "|" + key);
            return "{\"platformAdmins\":[]}";
        });
        assertThat(source.scheme()).isEqualTo("s3");
        assertThat(source.bucket()).isEqualTo("my-bucket");
        assertThat(source.key()).isEqualTo("folder/sub/platform-admins.json");
        assertThat(source.read()).isEqualTo("{\"platformAdmins\":[]}");
        assertThat(calls).containsExactly("my-bucket|folder/sub/platform-admins.json");
        assertThat(PlatformAdminSourceFactory.create("s3://b/k.json", (b, k) -> "x")).isInstanceOf(S3PlatformAdminSource.class);
    }

    @Test
    void aMalformedS3LocationIsRejected() {
        S3ObjectFetcher none = (b, k) -> "x";
        for (String bad : new String[] {"s3://", "s3://bucket", "s3://bucket/", "s3:///key", "http://bucket/key"}) {
            assertThatThrownBy(() -> new S3PlatformAdminSource(bad, none)).as(bad).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void anS3ErrorReachesTheRegistryWhichKeepsTheLastGoodList() {
        boolean[] fail = {false};
        S3PlatformAdminSource source = new S3PlatformAdminSource("s3://b/k.json", (bucket, key) -> {
            if (fail[0]) {
                throw new IOException("denied");
            }
            return "{\"platformAdmins\":[{\"keycloakSub\":\"sub-a\"}]}";
        });
        long[] now = {0};
        Clock clock = new Clock() {
            public java.time.ZoneId getZone() { return java.time.ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId z) { return this; }
            public java.time.Instant instant() { return java.time.Instant.ofEpochMilli(now[0]); }
        };
        PlatformAdminRegistry registry = new PlatformAdminRegistry(source, 60, clock);
        assertThat(registry.enabledCount()).isEqualTo(1);
        fail[0] = true;
        now[0] += 61_000;
        assertThat(registry.enabledCount()).as("S3 error: last good list kept").isEqualTo(1);
    }

    @Test
    void anUnsupportedLocationFailsClosedInsteadOfStoppingTheApplication() {
        for (String bad : new String[] {"ftp://host/file.json", "platform-admins.json", "https://host/x.json"}) {
            PlatformAdminSource source = PlatformAdminSourceFactory.create(bad, (b, k) -> "x");
            assertThat(source.scheme()).isEqualTo("invalid");
            assertThatThrownBy(source::read).isInstanceOf(IOException.class);
            assertThat(new PlatformAdminRegistry(source, 60, Clock.systemUTC()).enabledCount()).isZero();
        }
    }

    @Test
    void thePropertiesDefaultToTheProjectFileAndAMinimumRefresh() {
        PlatformAdminProperties defaults = new PlatformAdminProperties("", 0, null, null);
        assertThat(defaults.getLocation()).isEqualTo("classpath:config/platform-admins.json");
        assertThat(defaults.getRefreshSeconds()).isEqualTo(1);
        assertThat(defaults.getS3Region()).isEmpty();
        assertThat(new PlatformAdminProperties(" s3://b/k ", 60, " eu-west-1 ", "").getLocation()).isEqualTo("s3://b/k");
    }

    @Test
    void theShippedFileParsesHoldsIdentitiesOnlyAndRecognisesNobodyElse() throws IOException {
        // The product owner maintains the real entries in this file, so the test must not depend on which
        // identities are listed: it checks the structure, that no credential-like field is ever present,
        // and that an unrelated login is not recognised.
        PlatformAdminProperties defaults = new PlatformAdminProperties("classpath:config/platform-admins.json", 60, "", "");
        PlatformAdminSource source = PlatformAdminSourceFactory.create(defaults);
        String json = source.read();
        PlatformAdminRegistry registry = new PlatformAdminRegistry(source, 60, Clock.systemUTC());

        var tree = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        assertThat(tree.get("version").asInt()).isEqualTo(1);
        List<String> topLevel = new ArrayList<>();
        tree.fieldNames().forEachRemaining(topLevel::add);
        assertThat(topLevel).as("only the documented top-level fields").containsExactlyInAnyOrder("version", "description", "platformAdmins");
        assertThat(tree.get("platformAdmins").isArray()).isTrue();
        for (var entry : tree.get("platformAdmins")) {
            List<String> entryFields = new ArrayList<>();
            entry.fieldNames().forEachRemaining(entryFields::add);
            assertThat(entryFields).as("identity fields only, never a credential")
                .isSubsetOf("email", "keycloakSub", "displayName", "enabled", "notes");
            assertThat(entry.path("email").asText("").isBlank() && entry.path("keycloakSub").asText("").isBlank())
                .as("every entry names an email or a Keycloak sub").isFalse();
        }
        assertThat(registry.enabledCount()).isLessThanOrEqualTo(tree.get("platformAdmins").size());
        var stranger = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("t").header("alg", "none")
            .subject("00000000-0000-0000-0000-00000000dead")
            .claim("email", "nobody-listed@invalid.example").claim("email_verified", true).build();
        assertThat(registry.isPlatformAdmin(stranger)).isFalse();
    }
}
