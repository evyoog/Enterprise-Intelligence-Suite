package com.vyoog.eisplatform.modules.platformadmin.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** Platform admin registry (C108): matching rules, tolerant parsing, fail-closed and refresh behaviour. No Spring, no AWS. */
class PlatformAdminRegistryTest {

    /** A clock the test moves by hand. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-16T10:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    /** A source whose content (or failure) the test controls and whose reads are counted. */
    private static final class StubSource implements PlatformAdminSource {
        final AtomicReference<String> json = new AtomicReference<>();
        final AtomicReference<IOException> failure = new AtomicReference<>();
        final AtomicInteger reads = new AtomicInteger();

        StubSource(String initial) {
            json.set(initial);
        }

        @Override
        public String read() throws IOException {
            reads.incrementAndGet();
            if (failure.get() != null) {
                throw failure.get();
            }
            return json.get();
        }

        @Override
        public String scheme() {
            return "test";
        }
    }

    private static final String ONE_BY_SUB = """
        {"version":1,"platformAdmins":[{"email":"a@x.example","keycloakSub":"sub-a","displayName":"A","enabled":true,"notes":"n"}]}""";

    private static Jwt jwt(String sub, String email, Object emailVerified) {
        Jwt.Builder b = Jwt.withTokenValue("t").header("alg", "none").subject(sub);
        if (email != null) b.claim("email", email);
        if (emailVerified != null) b.claim("email_verified", emailVerified);
        return b.build();
    }

    private static PlatformAdminRegistry registry(String json) {
        return new PlatformAdminRegistry(new StubSource(json), 60, new MutableClock());
    }

    @Test
    void aLoginMatchesBySubWithoutAnyEmail() {
        PlatformAdminRegistry r = registry(ONE_BY_SUB);
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isTrue();
        assertThat(r.isPlatformAdmin(jwt("sub-a", "someone-else@x.example", false))).as("sub is enough, email is irrelevant").isTrue();
        assertThat(r.isPlatformAdmin(jwt("sub-other", null, null))).isFalse();
    }

    @Test
    void anEmailMatchNeedsEmailVerifiedTrue() {
        PlatformAdminRegistry r = registry(ONE_BY_SUB);
        assertThat(r.isPlatformAdmin(jwt("other", "a@x.example", true))).isTrue();
        assertThat(r.isPlatformAdmin(jwt("other", "a@x.example", "true"))).as("a string true is accepted").isTrue();
        assertThat(r.isPlatformAdmin(jwt("other", "a@x.example", false))).isFalse();
        assertThat(r.isPlatformAdmin(jwt("other", "a@x.example", null))).as("claim missing").isFalse();
        assertThat(r.isPlatformAdmin(jwt("other", "a@x.example", "yes"))).isFalse();
    }

    @Test
    void theEmailComparisonIgnoresCase() {
        PlatformAdminRegistry r = registry("""
            {"platformAdmins":[{"email":"  Mixed.Case@X.Example "}]}""");
        assertThat(r.isPlatformAdmin(jwt("o", "MIXED.case@x.example", true))).isTrue();
    }

    @Test
    void aDisabledEntryIsIgnoredAndAMissingEnabledMeansEnabled() {
        PlatformAdminRegistry r = registry("""
            {"platformAdmins":[
              {"email":"off@x.example","keycloakSub":"sub-off","enabled":false},
              {"email":"on@x.example","keycloakSub":"sub-on"}]}""");
        assertThat(r.isPlatformAdmin(jwt("sub-off", "off@x.example", true))).isFalse();
        assertThat(r.isPlatformAdmin(jwt("sub-on", null, null))).isTrue();
        assertThat(r.isPlatformAdmin(jwt("o", "on@x.example", true))).isTrue();
        assertThat(r.enabledCount()).isEqualTo(1);
    }

    @Test
    void aBlankSubNeverMatches() {
        PlatformAdminRegistry r = registry("""
            {"platformAdmins":[{"email":"a@x.example","keycloakSub":"  "},{"keycloakSub":""}]}""");
        assertThat(r.isPlatformAdmin(jwt("", null, null))).isFalse();
        assertThat(r.isPlatformAdmin(jwt(" ", null, null))).isFalse();
        assertThat(r.isPlatformAdmin(jwt("anything", null, null))).isFalse();
        assertThat(r.isPlatformAdmin(jwt("anything", "", true))).as("a blank email never matches either").isFalse();
    }

    @Test
    void anApiKeyTokenIsNeverMatched() {
        PlatformAdminRegistry r = registry(ONE_BY_SUB);
        Jwt apiKey = Jwt.withTokenValue("t").header("alg", "none").subject("sub-a")
            .claim("email", "a@x.example").claim("email_verified", true).claim("api_key_id", "7").build();
        assertThat(r.isPlatformAdmin(apiKey)).isFalse();
    }

    @Test
    void unknownJsonFieldsAreIgnoredAndMissingListsAreEmpty() {
        PlatformAdminRegistry r = registry("""
            {"version":2,"future":{"a":1},"platformAdmins":[{"email":"a@x.example","keycloakSub":"sub-a","level":"gold","extra":[1,2]}]}""");
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isTrue();
        assertThat(registry("{\"version\":1}").enabledCount()).isZero();
        assertThat(registry("{\"platformAdmins\":[null]}").enabledCount()).isZero();
    }

    @Test
    void invalidJsonOnTheFirstLoadMeansNobodyIsAnAdmin() {
        PlatformAdminRegistry r = registry("{not json");
        assertThat(r.isPlatformAdmin(jwt("sub-a", "a@x.example", true))).isFalse();
        assertThat(r.enabledCount()).isZero();
        assertThat(registry("").enabledCount()).isZero();
        assertThat(registry("null").enabledCount()).isZero();
    }

    @Test
    void aFailingSourceOnTheFirstLoadMeansNobodyIsAnAdmin() {
        StubSource source = new StubSource(ONE_BY_SUB);
        source.failure.set(new IOException("unreachable"));
        PlatformAdminRegistry r = new PlatformAdminRegistry(source, 60, new MutableClock());
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isFalse();
    }

    @Test
    void invalidJsonOrAFailureOnRefreshKeepsTheLastGoodList() {
        StubSource source = new StubSource(ONE_BY_SUB);
        MutableClock clock = new MutableClock();
        PlatformAdminRegistry r = new PlatformAdminRegistry(source, 60, clock);
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isTrue();

        source.json.set("{broken");
        clock.advance(Duration.ofSeconds(61));
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).as("invalid JSON: last good kept").isTrue();
        assertThat(source.reads.get()).isEqualTo(2);

        source.failure.set(new IOException("s3 down"));
        clock.advance(Duration.ofSeconds(61));
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).as("source failure: last good kept").isTrue();
        assertThat(source.reads.get()).isEqualTo(3);

        source.failure.set(null);
        source.json.set("{\"platformAdmins\":[{\"keycloakSub\":\"sub-b\"}]}");
        clock.advance(Duration.ofSeconds(61));
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).as("a good refresh replaces the list").isFalse();
        assertThat(r.isPlatformAdmin(jwt("sub-b", null, null))).isTrue();
    }

    @Test
    void theSourceIsReReadOnlyAfterTheRefreshInterval() {
        StubSource source = new StubSource(ONE_BY_SUB);
        MutableClock clock = new MutableClock();
        PlatformAdminRegistry r = new PlatformAdminRegistry(source, 60, clock);

        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isTrue();
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isTrue();
        assertThat(r.enabledCount()).isEqualTo(1);
        assertThat(source.reads.get()).as("loaded once, then served from the snapshot").isEqualTo(1);

        source.json.set("{\"platformAdmins\":[]}");
        clock.advance(Duration.ofSeconds(59));
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).as("59s: not yet due").isTrue();
        assertThat(source.reads.get()).isEqualTo(1);

        clock.advance(Duration.ofSeconds(1));
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).as("60s: re-read, the removal takes effect").isFalse();
        assertThat(source.reads.get()).isEqualTo(2);
    }

    @Test
    void aFailedFirstLoadIsRetriedOnlyAfterTheInterval() {
        StubSource source = new StubSource(ONE_BY_SUB);
        source.failure.set(new IOException("down"));
        MutableClock clock = new MutableClock();
        PlatformAdminRegistry r = new PlatformAdminRegistry(source, 60, clock);

        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isFalse();
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isFalse();
        assertThat(source.reads.get()).isEqualTo(1);

        source.failure.set(null);
        clock.advance(Duration.ofSeconds(60));
        assertThat(r.isPlatformAdmin(jwt("sub-a", null, null))).isTrue();
    }
}
