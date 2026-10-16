package com.vyoog.eisplatform.modules.platformadmin.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The platform admin registry (C108): who is listed as a platform admin in the JSON file (project file now,
 * S3 later). It only recognises a login for the profile endpoints; it grants no permission and never holds a
 * password.
 *
 * <p>The source is re-read lazily, at most every {@code eis.platform-admins.refresh-seconds}. A failed or
 * invalid refresh keeps the last good snapshot; a failed first load leaves the registry empty (fail closed).
 * Logs name only the source scheme, the error class and the number of enabled entries, never an identity.
 */
@Service
@Slf4j
public class PlatformAdminRegistry {

    private static final JsonMapper MAPPER = JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();

    /** Enabled entries only, indexed for matching. */
    private record Snapshot(List<PlatformAdminEntry> entries, Set<String> subs, Set<String> emails) {
        static final Snapshot EMPTY = new Snapshot(List.of(), Set.of(), Set.of());

        int count() {
            return entries.size();
        }
    }

    private final PlatformAdminSource source;
    private final Clock clock;
    private final long refreshMillis;
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.EMPTY);
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean loaded;
    private volatile long nextRefreshAt;

    @Autowired
    public PlatformAdminRegistry(PlatformAdminSource source, PlatformAdminProperties properties) {
        this(source, properties.getRefreshSeconds(), Clock.systemUTC());
    }

    public PlatformAdminRegistry(PlatformAdminSource source, long refreshSeconds, Clock clock) {
        this.source = source;
        this.refreshMillis = Math.max(1, refreshSeconds) * 1000L;
        this.clock = clock;
    }

    /**
     * True when the token's login is an enabled entry: by Keycloak subject, or by email together with
     * {@code email_verified=true}. An API key (claim {@code api_key_id}) is never a person.
     */
    public boolean isPlatformAdmin(Jwt jwt) {
        if (jwt == null || jwt.hasClaim("api_key_id")) {
            return false;
        }
        refreshIfDue();
        Snapshot current = snapshot.get();
        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank() && current.subs().contains(sub.strip())) {
            return true;
        }
        String email = jwt.getClaimAsString("email");
        return email != null && !email.isBlank() && isTrue(jwt.getClaim("email_verified"))
            && current.emails().contains(normalise(email));
    }

    /** The number of enabled entries currently known. */
    public int enabledCount() {
        refreshIfDue();
        return snapshot.get().count();
    }

    private static boolean isTrue(Object claim) {
        return Boolean.TRUE.equals(claim) || (claim instanceof String s && "true".equalsIgnoreCase(s.strip()));
    }

    private static String normalise(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    // ---- refresh ----

    private void refreshIfDue() {
        if (loaded && clock.millis() < nextRefreshAt) {
            return;
        }
        if (!loaded) {
            lock.lock(); // the very first load: everyone waits for it rather than seeing an empty registry
        } else if (!lock.tryLock()) {
            return; // someone else is refreshing; keep using the current snapshot
        }
        try {
            long now = clock.millis();
            if (loaded && now < nextRefreshAt) {
                return;
            }
            nextRefreshAt = now + refreshMillis; // set first: a slow or failing source is retried only after the interval
            reload();
            loaded = true;
        } finally {
            lock.unlock();
        }
    }

    private void reload() {
        try {
            Snapshot fresh = parse(source.read());
            Snapshot previous = snapshot.getAndSet(fresh);
            if (!loaded || !previous.entries().equals(fresh.entries())) {
                log.info("Platform admin registry loaded: {} enabled entries (source: {})", fresh.count(), source.scheme());
            }
        } catch (Exception e) {
            log.warn("Platform admin registry could not be refreshed (source: {}, error: {}); keeping the last good list of {} enabled entries",
                source.scheme(), e.getClass().getSimpleName(), snapshot.get().count());
        }
    }

    private static Snapshot parse(String json) throws IOException {
        if (json == null || json.isBlank()) {
            throw new IOException("Empty platform admin file");
        }
        PlatformAdminFile file = MAPPER.readValue(json, PlatformAdminFile.class);
        if (file == null) {
            throw new IOException("Empty platform admin file");
        }
        List<PlatformAdminEntry> enabled = file.platformAdmins() == null ? List.of()
            : file.platformAdmins().stream().filter(e -> e != null && e.active()).toList();
        Set<String> subs = new HashSet<>();
        Set<String> emails = new HashSet<>();
        for (PlatformAdminEntry e : enabled) {
            if (e.keycloakSub() != null && !e.keycloakSub().isBlank()) {
                subs.add(e.keycloakSub().strip());
            }
            if (e.email() != null && !e.email().isBlank()) {
                emails.add(normalise(e.email()));
            }
        }
        return new Snapshot(List.copyOf(enabled), Set.copyOf(subs), Set.copyOf(emails));
    }
}
