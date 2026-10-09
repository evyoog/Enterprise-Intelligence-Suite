package com.vyoog.eisplatform.modules.toolsync.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/** The FIRST result of a tool's call to the platform, answered again when the same call is repeated with the same key (BR-SYN-017). */
@Entity
@Table(name = "mcp_idempotency")
@IdClass(McpIdempotency.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class McpIdempotency {

    @Id
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Id
    @Column(length = 100)
    private String tool;

    @Column(name = "result_json", nullable = false, columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Key implements Serializable {
        private String idempotencyKey;
        private String tool;

        public Key(String idempotencyKey, String tool) {
            this.idempotencyKey = idempotencyKey;
            this.tool = tool;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(idempotencyKey, k.idempotencyKey) && Objects.equals(tool, k.tool);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idempotencyKey, tool);
        }
    }
}
