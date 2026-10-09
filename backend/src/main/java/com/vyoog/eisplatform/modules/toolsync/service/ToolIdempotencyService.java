package com.vyoog.eisplatform.modules.toolsync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.toolsync.config.ToolSyncProperties;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.model.McpIdempotency;
import com.vyoog.eisplatform.modules.toolsync.repository.McpIdempotencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A tool's call is done once, however often it is sent (BR-SYN-017): the first result is stored with the caller's idempotency key and
 * answered again for every repeat, for at least {@code app.sync.idempotency-retention-days} (7) days. The key is scoped to the calling
 * client (a hash of client id and key), so one tool can never read another's results. A {@code retry} answer is not stored: the repeat
 * should try again.
 *
 * <p>The work and its writes run in one transaction that is rolled back when the call does not succeed; the stored result is written
 * after that. Calls with the same key are serialized inside the application (single instance assumed, like the rest of the synchronization).
 */
@Service
public class ToolIdempotencyService {

    private final McpIdempotencyRepository records;
    private final ObjectMapper json;
    private final ToolSyncProperties props;
    private final TransactionTemplate work;
    private final TransactionTemplate store;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public ToolIdempotencyService(McpIdempotencyRepository records, ObjectMapper json, ToolSyncProperties props,
                                  PlatformTransactionManager transactionManager) {
        this.records = records;
        this.json = json;
        this.props = props;
        this.work = new TransactionTemplate(transactionManager);
        this.work.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.store = new TransactionTemplate(transactionManager);
        this.store.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * @param translator turns an exception of the work into the result to answer (and store, unless it is a retry)
     */
    public ToolResult once(String clientId, String key, String tool, Supplier<ToolResult> doWork, Function<RuntimeException, ToolResult> translator) {
        String scoped = scope(clientId, key);
        synchronized (locks.computeIfAbsent(scoped + "/" + tool, k -> new Object())) {
            McpIdempotency existing = records.findById(new McpIdempotency.Key(scoped, tool)).orElse(null);
            if (existing != null) {
                try {
                    return json.readValue(existing.getResultJson(), ToolResult.class);
                } catch (Exception e) {
                    // an unreadable record is treated as absent: the call is done again
                }
            }
            ToolResult result;
            try {
                result = work.execute(status -> {
                    ToolResult r = doWork.get();
                    if (!r.succeeded()) {
                        status.setRollbackOnly();
                    }
                    return r;
                });
            } catch (RuntimeException e) {
                result = translator.apply(e);
            }
            if (!ToolResult.RETRY.equals(result.status())) {
                ToolResult toStore = result;
                store.executeWithoutResult(s -> {
                    McpIdempotency m = new McpIdempotency();
                    m.setIdempotencyKey(scoped);
                    m.setTool(tool);
                    m.setResultJson(write(toStore));
                    records.save(m);
                });
            }
            return result;
        }
    }

    /** Removes records older than the retention period. */
    public int purgeOlderThanRetention() {
        Integer n = store.execute(s -> records.deleteCreatedBefore(Instant.now().minus(Duration.ofDays(Math.max(7, props.getIdempotencyRetentionDays())))));
        return n == null ? 0 : n;
    }

    private String write(ToolResult r) {
        try {
            return json.writeValueAsString(r);
        } catch (Exception e) {
            throw new IllegalStateException("Could not store the result of a tool call", e);
        }
    }

    static String scope(String clientId, String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((clientId + ":" + key).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
