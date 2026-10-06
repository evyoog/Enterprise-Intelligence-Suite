package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vyoog.eisplatform.common.exception.KnowledgeException;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Content blocks (REQ-KNW-002.2, BR-KCON-008): validation and plain text.
 * Blocks are structured JSON — text is stored as text and rendered as text,
 * never as HTML, so stored content cannot inject scripts. Media blocks hold
 * media/content ids only, never storage URLs (BR-KCON-005).
 */
public final class KnowledgeBlocks {

    static final ObjectMapper JSON = new ObjectMapper();

    /** Block type → the fields it may carry. */
    private static final Map<String, Set<String>> FIELDS = Map.ofEntries(
        Map.entry("heading", Set.of("text", "level")),
        Map.entry("paragraph", Set.of("text")),
        Map.entry("bulleted_list", Set.of("items")),
        Map.entry("numbered_list", Set.of("items")),
        Map.entry("quote", Set.of("text", "title")),
        Map.entry("image", Set.of("mediaId", "caption", "alt")),
        Map.entry("gallery", Set.of("mediaIds", "caption")),
        Map.entry("video", Set.of("contentId", "caption")),
        Map.entry("audio", Set.of("mediaId", "caption")),
        Map.entry("file", Set.of("mediaId", "label")),
        Map.entry("pdf", Set.of("mediaId", "label")),
        Map.entry("table", Set.of("rows", "caption")),
        Map.entry("code", Set.of("text", "language")),
        Map.entry("callout", Set.of("text", "title")),
        Map.entry("warning", Set.of("text", "title")),
        Map.entry("note", Set.of("text", "title")),
        Map.entry("step", Set.of("text", "title")),
        Map.entry("accordion", Set.of("items")),
        Map.entry("faq", Set.of("question", "answer")),
        Map.entry("button", Set.of("label", "url")),
        Map.entry("link", Set.of("label", "url")),
        Map.entry("workflow_diagram", Set.of("steps", "title")),
        Map.entry("embed", Set.of("url", "title"))
    );

    public static final Set<String> TYPES = FIELDS.keySet();

    private static final int MAX_BLOCKS = 300;
    private static final int MAX_TEXT = 20000;

    private KnowledgeBlocks() {
    }

    /** Validates and normalises blocks; returns the JSON to store. */
    public static String validate(JsonNode blocks) {
        if (blocks == null || blocks.isNull()) {
            return "[]";
        }
        if (!blocks.isArray()) {
            throw invalid("Blocks must be a list.");
        }
        if (blocks.size() > MAX_BLOCKS) {
            throw invalid("A content item can have at most " + MAX_BLOCKS + " blocks.");
        }
        ArrayNode out = JsonNodeFactory.instance.arrayNode();
        int index = 0;
        for (JsonNode block : blocks) {
            index++;
            if (!block.isObject() || !block.hasNonNull("type")) {
                throw invalid("Block " + index + " has no type.");
            }
            String type = block.get("type").asText();
            Set<String> allowed = FIELDS.get(type);
            if (allowed == null) {
                throw invalid("Block " + index + " has an unknown type: " + type + ".");
            }
            ObjectNode clean = JsonNodeFactory.instance.objectNode();
            clean.put("type", type);
            if (block.hasNonNull("id")) {
                clean.put("id", limit(block.get("id").asText(), 60));
            }
            Iterator<Map.Entry<String, JsonNode>> fields = block.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String name = field.getKey();
                if (name.equals("type") || name.equals("id")) {
                    continue;
                }
                if (!allowed.contains(name)) {
                    throw invalid("Block " + index + " (" + type + ") does not take '" + name + "'.");
                }
                clean.set(name, cleanValue(name, field.getValue(), index));
            }
            out.add(clean);
        }
        return out.toString();
    }

    private static JsonNode cleanValue(String name, JsonNode value, int index) {
        switch (name) {
            case "mediaId", "contentId" -> {
                if (!value.canConvertToLong()) {
                    throw invalid("Block " + index + ": '" + name + "' must be an id.");
                }
                return JsonNodeFactory.instance.numberNode(value.asLong());
            }
            case "level" -> {
                int level = value.asInt(2);
                return JsonNodeFactory.instance.numberNode(Math.max(2, Math.min(4, level)));
            }
            case "mediaIds" -> {
                ArrayNode ids = JsonNodeFactory.instance.arrayNode();
                for (JsonNode id : requireArray(value, name, index)) {
                    if (!id.canConvertToLong()) {
                        throw invalid("Block " + index + ": 'mediaIds' must be ids.");
                    }
                    ids.add(id.asLong());
                }
                return ids;
            }
            case "url" -> {
                String url = value.asText("").trim();
                if (!safeUrl(url)) {
                    throw invalid("Block " + index + ": links must start with https:// or be an EIS page (/...).");
                }
                return JsonNodeFactory.instance.textNode(limit(url, 1000));
            }
            case "items" -> {
                ArrayNode items = JsonNodeFactory.instance.arrayNode();
                for (JsonNode item : requireArray(value, name, index)) {
                    if (item.isObject()) {
                        ObjectNode entry = JsonNodeFactory.instance.objectNode();
                        entry.put("title", limit(item.path("title").asText(""), 500));
                        entry.put("text", limit(item.path("text").asText(""), MAX_TEXT));
                        items.add(entry);
                    } else {
                        items.add(limit(item.asText(""), MAX_TEXT));
                    }
                }
                return items;
            }
            case "rows" -> {
                ArrayNode rows = JsonNodeFactory.instance.arrayNode();
                for (JsonNode row : requireArray(value, name, index)) {
                    ArrayNode cells = JsonNodeFactory.instance.arrayNode();
                    for (JsonNode cell : requireArray(row, name, index)) {
                        cells.add(limit(cell.asText(""), 2000));
                    }
                    rows.add(cells);
                }
                return rows;
            }
            case "steps" -> {
                ArrayNode steps = JsonNodeFactory.instance.arrayNode();
                for (JsonNode step : requireArray(value, name, index)) {
                    ObjectNode entry = JsonNodeFactory.instance.objectNode();
                    entry.put("title", limit(step.path("title").asText(""), 200));
                    if (step.hasNonNull("contentId") && step.get("contentId").canConvertToLong()) {
                        entry.put("contentId", step.get("contentId").asLong());
                    }
                    String route = step.path("route").asText("").trim();
                    if (!route.isEmpty()) {
                        if (!route.startsWith("/") || route.startsWith("//")) {
                            throw invalid("Block " + index + ": a workflow step route must be an EIS page (/...).");
                        }
                        entry.put("route", limit(route, 300));
                    }
                    steps.add(entry);
                }
                return steps;
            }
            default -> {
                if (!value.isValueNode()) {
                    throw invalid("Block " + index + ": '" + name + "' must be text.");
                }
                return JsonNodeFactory.instance.textNode(limit(value.asText(""), MAX_TEXT));
            }
        }
    }

    private static Iterable<JsonNode> requireArray(JsonNode value, String name, int index) {
        if (!value.isArray()) {
            throw invalid("Block " + index + ": '" + name + "' must be a list.");
        }
        return value;
    }

    static boolean safeUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String lower = url.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("https://") || (url.startsWith("/") && !url.startsWith("//"));
    }

    /** One paragraph block holding {@code text} (REQ-KNW-001.7 migration shape). */
    public static String paragraph(String text) {
        ArrayNode out = JsonNodeFactory.instance.arrayNode();
        out.addObject().put("type", "paragraph").put("text", text == null ? "" : text);
        return out.toString();
    }

    /** Plain text of all text in the blocks (body for the old endpoints and search). */
    public static String plainText(String blocksJson) {
        JsonNode blocks = parse(blocksJson);
        StringBuilder sb = new StringBuilder();
        for (JsonNode block : blocks) {
            collect(block, sb);
        }
        String text = sb.toString().trim();
        return text.length() > MAX_TEXT ? text.substring(0, MAX_TEXT) : text;
    }

    private static void collect(JsonNode node, StringBuilder sb) {
        if (node.isTextual()) {
            String text = node.asText();
            if (!text.isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(text);
            }
        } else if (node.isArray()) {
            node.forEach(child -> collect(child, sb));
        } else if (node.isObject()) {
            node.fields().forEachRemaining(e -> {
                if (!e.getKey().equals("type") && !e.getKey().equals("id") && !e.getKey().equals("url")
                    && !e.getKey().equals("route") && !e.getKey().equals("language")) {
                    collect(e.getValue(), sb);
                }
            });
        }
    }

    /** Media ids referenced by the blocks ("used by" in the media library). */
    public static Set<Long> mediaIds(String blocksJson) {
        Set<Long> ids = new LinkedHashSet<>();
        for (JsonNode block : parse(blocksJson)) {
            if (block.has("mediaId")) {
                ids.add(block.get("mediaId").asLong());
            }
            if (block.has("mediaIds")) {
                block.get("mediaIds").forEach(id -> ids.add(id.asLong()));
            }
        }
        return ids;
    }

    /** Content ids referenced by video blocks and workflow steps. */
    public static Set<Long> contentIds(String blocksJson) {
        Set<Long> ids = new LinkedHashSet<>();
        for (JsonNode block : parse(blocksJson)) {
            if (block.has("contentId")) {
                ids.add(block.get("contentId").asLong());
            }
            if (block.has("steps")) {
                block.get("steps").forEach(s -> {
                    if (s.has("contentId")) {
                        ids.add(s.get("contentId").asLong());
                    }
                });
            }
        }
        return ids;
    }

    public static JsonNode parse(String json) {
        if (json == null || json.isBlank()) {
            return JsonNodeFactory.instance.arrayNode();
        }
        try {
            return JSON.readTree(json);
        } catch (JsonProcessingException e) {
            return JsonNodeFactory.instance.arrayNode();
        }
    }

    /** Type fields: a flat object of text (or lists of text) values. */
    public static String validateTypeFields(JsonNode fields) {
        if (fields == null || fields.isNull()) {
            return null;
        }
        if (!fields.isObject()) {
            throw invalid("Type fields must be an object.");
        }
        ObjectNode out = JsonNodeFactory.instance.objectNode();
        fields.fields().forEachRemaining(e -> {
            JsonNode v = e.getValue();
            String key = limit(e.getKey(), 60);
            if (v.isArray()) {
                ArrayNode list = out.putArray(key);
                v.forEach(item -> list.add(limit(item.asText(""), 5000)));
            } else if (v.isValueNode()) {
                out.put(key, limit(v.asText(""), MAX_TEXT));
            } else {
                throw invalid("Type field '" + key + "' must be text or a list of text.");
            }
        });
        return out.toString();
    }

    public static String typeFieldsText(String json) {
        if (json == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        collect(parse(json), sb);
        return sb.toString();
    }

    public static List<String> tagList(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(tags.split(",")).map(String::trim).filter(t -> !t.isEmpty()).distinct().toList();
    }

    private static String limit(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static KnowledgeException invalid(String message) {
        return KnowledgeException.badRequest("INVALID_CONTENT", message);
    }
}
