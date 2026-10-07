package com.vyoog.eisplatform.modules.productcontent.service;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a pasted video link (REQ-CAT-004.6, BR-PCON-007): https only; YouTube
 * and Vimeo are recognised so the product page can embed them in a
 * privacy-friendly player; any other https link stays a plain link.
 */
public record ProductVideoLink(String provider, String ref, String url, String thumbnailUrl) {

    private static final Pattern YOUTUBE_ID = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    private static final Pattern DIGITS = Pattern.compile("^\\d{1,12}$");

    /** @throws IllegalArgumentException if the link is not a usable https link */
    public static ProductVideoLink parse(String raw) {
        if (raw == null || raw.isBlank() || raw.length() > 1000) {
            throw new IllegalArgumentException("Enter the video link.");
        }
        URI uri;
        try {
            uri = URI.create(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("This is not a valid link.");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("The video link must be a secure https link.");
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        String url = uri.toString();
        Optional<String> youtube = youtubeId(host, uri);
        if (youtube.isPresent()) {
            return new ProductVideoLink("YOUTUBE", youtube.get(), url, "https://i.ytimg.com/vi/" + youtube.get() + "/hqdefault.jpg");
        }
        Optional<String> vimeo = vimeoId(host, uri);
        if (vimeo.isPresent()) {
            return new ProductVideoLink("VIMEO", vimeo.get(), url, null);
        }
        return new ProductVideoLink("EXTERNAL", null, url, null);
    }

    /** The embedded-player address for a recognised provider, else null. */
    public static String embedUrl(String provider, String ref) {
        if ("YOUTUBE".equals(provider) && ref != null) {
            return "https://www.youtube-nocookie.com/embed/" + ref;
        }
        if ("VIMEO".equals(provider) && ref != null) {
            return "https://player.vimeo.com/video/" + ref + "?dnt=1";
        }
        return null;
    }

    private static Optional<String> youtubeId(String host, URI uri) {
        String path = uri.getPath() == null ? "" : uri.getPath();
        String candidate = null;
        if (host.equals("youtu.be")) {
            candidate = firstSegment(path);
        } else if (host.equals("youtube.com") || host.endsWith(".youtube.com") || host.equals("youtube-nocookie.com")
            || host.endsWith(".youtube-nocookie.com")) {
            if (path.equals("/watch")) {
                Matcher m = Pattern.compile("(?:^|&)v=([^&]+)").matcher(uri.getRawQuery() == null ? "" : uri.getRawQuery());
                candidate = m.find() ? m.group(1) : null;
            } else {
                for (String prefix : new String[] {"/embed/", "/shorts/", "/live/", "/v/"}) {
                    if (path.startsWith(prefix)) {
                        candidate = firstSegment(path.substring(prefix.length() - 1));
                    }
                }
            }
        }
        return candidate != null && YOUTUBE_ID.matcher(candidate).matches() ? Optional.of(candidate) : Optional.empty();
    }

    private static Optional<String> vimeoId(String host, URI uri) {
        if (!host.equals("vimeo.com") && !host.endsWith(".vimeo.com")) {
            return Optional.empty();
        }
        String[] parts = (uri.getPath() == null ? "" : uri.getPath()).split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (DIGITS.matcher(parts[i]).matches()) {
                return Optional.of(parts[i]);
            }
        }
        return Optional.empty();
    }

    private static String firstSegment(String path) {
        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        int slash = trimmed.indexOf('/');
        return slash < 0 ? trimmed : trimmed.substring(0, slash);
    }
}
