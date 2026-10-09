package com.vyoog.eisplatform.modules.toolsync;

import com.vyoog.eisplatform.modules.toolsync.gateway.ToolGateway;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolUnavailableException;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A tool as the platform sees it, for tests: it records every call, can be "stopped" (unreachable), can be told what to answer, and
 * otherwise behaves like a receiver of contract v1 section 8 (a version that is not newer than the one held is a "duplicate").
 */
public class FakeToolGateway implements ToolGateway {

    public record Call(String productCode, String tool, Map<String, Object> arguments) {
        @SuppressWarnings("unchecked")
        public Map<String, Object> envelope() {
            return (Map<String, Object>) arguments.get("envelope");
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> payload() {
            return (Map<String, Object>) envelope().get("payload");
        }
    }

    public final List<Call> calls = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> stopped = new HashSet<>();
    private final Map<String, Deque<ToolResult>> scripted = new HashMap<>();
    private final Map<String, Long> held = new HashMap<>();

    public synchronized void reset() {
        calls.clear();
        stopped.clear();
        scripted.clear();
        held.clear();
    }

    public synchronized void stop(String productCode) {
        stopped.add(productCode);
    }

    public synchronized void start(String productCode) {
        stopped.remove(productCode);
    }

    /** The next answer(s) for a tool, before the default behaviour applies again. */
    public synchronized void answer(String tool, ToolResult... results) {
        Deque<ToolResult> q = scripted.computeIfAbsent(tool, k -> new ArrayDeque<>());
        for (ToolResult r : results) {
            q.add(r);
        }
    }

    /** Makes the tool lose something it had, so reconcile has drift to find. */
    public synchronized void forget(String productCode, String tenantRef, String aggregateType, String aggregateId) {
        held.remove(productCode + "/" + tenantRef + "/" + aggregateType + "/" + aggregateId);
    }

    private Map<String, Long> heldOf(String productCode, String tenantRef, String type) {
        String prefix = productCode + "/" + tenantRef + "/" + type + "/";
        Map<String, Long> out = new java.util.TreeMap<>();
        held.forEach((k, v) -> {
            if (k.startsWith(prefix)) {
                out.put(k.substring(prefix.length()), v);
            }
        });
        return out;
    }

    @SuppressWarnings("unchecked")
    private ToolResult digest(ToolConnector c, Map<String, Object> args) {
        Map<String, Object> digests = new java.util.LinkedHashMap<>();
        for (String type : (List<String>) args.get("aggregateTypes")) {
            Map<String, Long> mine = heldOf(c.getProductCode(), String.valueOf(args.get("tenantRef")), type);
            digests.put(type, Map.of("count", mine.size(), "hash", com.vyoog.eisplatform.modules.toolsync.service.ToolReconcileService.hash(mine)));
        }
        return ToolResult.applied(null).withData(Map.of("digests", digests));
    }

    private ToolResult versions(ToolConnector c, Map<String, Object> args) {
        String after = (String) args.get("afterId");
        List<Map<String, Object>> list = new ArrayList<>();
        heldOf(c.getProductCode(), String.valueOf(args.get("tenantRef")), String.valueOf(args.get("aggregateType"))).forEach((id, v) -> {
            if (after == null || id.compareTo(after) > 0) {
                list.add(Map.of("id", id, "version", v));
            }
        });
        return ToolResult.applied(null).withData(Map.of("versions", list));
    }

    public List<Call> callsTo(String tool) {
        synchronized (calls) {
            return calls.stream().filter(c -> c.tool().equals(tool)).toList();
        }
    }

    @Override
    public synchronized ToolResult call(ToolConnector connector, String tool, Map<String, Object> arguments) {
        if (stopped.contains(connector.getProductCode())) {
            throw new ToolUnavailableException("connection refused (test)");
        }
        calls.add(new Call(connector.getProductCode(), tool, arguments));
        Deque<ToolResult> q = scripted.get(tool);
        if (q != null && !q.isEmpty()) {
            return q.poll();
        }
        if ("get_state_digest".equals(tool)) {
            return digest(connector, arguments);
        }
        if ("list_aggregate_versions".equals(tool)) {
            return versions(connector, arguments);
        }
        if ("provision_tenant".equals(tool)) {
            return ToolResult.applied(0L).withData(Map.of("schemaVersion", "79"));
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> envelope = (Map<String, Object>) arguments.get("envelope");
        if (envelope != null && envelope.get("version") instanceof Number n) {
            String key = connector.getProductCode() + "/" + envelope.get("tenantRef") + "/" + envelope.get("aggregateType") + "/" + envelope.get("aggregateId");
            Long current = held.get(key);
            if (current != null && n.longValue() <= current) {
                return ToolResult.duplicate(current);
            }
            held.put(key, n.longValue());
            return ToolResult.applied(n.longValue());
        }
        return ToolResult.applied(null);
    }
}
