package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaKind;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Allowed file types, folders and size limits per media kind (REQ-KNW-003.4,
 * BR-KVID-002). The list is the proposed one applied on 2026-10-05; SVG is
 * left out because uploads never pass through the backend, so they cannot
 * be sanitised.
 */
final class KnowledgeMediaRules {

    /** extension → allowed content types. */
    static final Map<String, Set<String>> IMAGE = Map.of(
        "png", Set.of("image/png"), "jpg", Set.of("image/jpeg"), "jpeg", Set.of("image/jpeg"),
        "webp", Set.of("image/webp"), "gif", Set.of("image/gif"));

    static final Map<String, Set<String>> DOCUMENT = Map.of(
        "pdf", Set.of("application/pdf"),
        "doc", Set.of("application/msword"),
        "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        "xls", Set.of("application/vnd.ms-excel"),
        "xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        "ppt", Set.of("application/vnd.ms-powerpoint"),
        "pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        "csv", Set.of("text/csv", "application/vnd.ms-excel"),
        "txt", Set.of("text/plain"));

    static final Map<String, Set<String>> AUDIO = Map.of(
        "mp3", Set.of("audio/mpeg"), "m4a", Set.of("audio/mp4", "audio/x-m4a"), "wav", Set.of("audio/wav", "audio/x-wav"));

    static final Map<String, Set<String>> VIDEO = Map.of(
        "mp4", Set.of("video/mp4"), "webm", Set.of("video/webm"), "mov", Set.of("video/quicktime"));

    static final Map<String, Set<String>> SUBTITLE = Map.of("vtt", Set.of("text/vtt"));

    static final long SUBTITLE_MAX_SIZE = 5L * 1024 * 1024;

    private KnowledgeMediaRules() {
    }

    static Map<String, Set<String>> typesOf(KnowledgeMediaKind kind) {
        return switch (kind) {
            case IMAGE, THUMBNAIL -> IMAGE;
            case DOCUMENT, TEMPLATE -> DOCUMENT;
            case AUDIO -> AUDIO;
            case VIDEO_FILE -> VIDEO;
            case SUBTITLE, TRANSCRIPT -> SUBTITLE;
        };
    }

    static long maxSize(KnowledgeMediaKind kind, KnowledgeSettings settings) {
        return switch (kind) {
            case IMAGE, THUMBNAIL -> settings.getImageMaxSize();
            case DOCUMENT, TEMPLATE -> settings.getDocumentMaxSize();
            case AUDIO -> settings.getAudioMaxSize();
            case VIDEO_FILE -> settings.getVideoMaxSize();
            case SUBTITLE, TRANSCRIPT -> SUBTITLE_MAX_SIZE;
        };
    }

    /** Top-level prefix and the allowed sub-folders (first = default). */
    static String prefix(KnowledgeMediaKind kind, String folder) {
        List<String> folders = switch (kind) {
            case VIDEO_FILE -> List.of("tutorials", "training", "troubleshooting", "webinars", "demos");
            case IMAGE -> List.of("screenshots", "diagrams");
            case THUMBNAIL -> List.of("thumbnails");
            case DOCUMENT -> List.of("guides", "manuals", "brochures", "release-notes");
            default -> List.of();
        };
        String root = switch (kind) {
            case VIDEO_FILE -> "videos";
            case IMAGE, THUMBNAIL -> "images";
            case DOCUMENT -> "documents";
            case TEMPLATE -> "templates";
            case AUDIO -> "audio";
            case SUBTITLE, TRANSCRIPT -> "transcripts";
        };
        if (folders.isEmpty()) {
            return root;
        }
        String chosen = folder == null ? folders.get(0) : folder.toLowerCase(Locale.ROOT);
        return root + "/" + (folders.contains(chosen) ? chosen : folders.get(0));
    }

    static String extension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    static String baseContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int semi = contentType.indexOf(';');
        return (semi < 0 ? contentType : contentType.substring(0, semi)).trim().toLowerCase(Locale.ROOT);
    }
}
