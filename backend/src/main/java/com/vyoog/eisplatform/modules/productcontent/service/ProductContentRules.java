package com.vyoog.eisplatform.modules.productcontent.service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Allowed files for product content (BR-PCON-002). SVG and GIF are left out:
 * uploads never pass through the backend, so an SVG cannot be sanitised
 * (same reason as REQ-KNW-003.4).
 */
final class ProductContentRules {

    /** extension → allowed content types. */
    static final Map<String, Set<String>> IMAGE = Map.of(
        "png", Set.of("image/png"), "jpg", Set.of("image/jpeg"), "jpeg", Set.of("image/jpeg"), "webp", Set.of("image/webp"));

    static final Map<String, Set<String>> PDF = Map.of("pdf", Set.of("application/pdf"));
    /** Hosted videos (PC-3, C89): only formats every browser plays. MOV is left out. */
    static final Map<String, Set<String>> VIDEO = Map.of("mp4", Set.of("video/mp4"), "webm", Set.of("video/webm"));
    /** The provider value of a video file uploaded to the product storage (no link). */
    static final String HOSTED = "HOSTED";

    private ProductContentRules() {
    }

    static String extension(String fileName) {
        int dot = fileName == null ? -1 : fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    static String baseContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int semi = contentType.indexOf(';');
        return (semi < 0 ? contentType : contentType.substring(0, semi)).trim().toLowerCase(Locale.ROOT);
    }

    static String cleanFileName(String fileName) {
        String name = fileName == null ? "" : fileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }
}
