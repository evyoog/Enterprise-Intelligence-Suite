package com.vyoog.eisplatform.modules.profile.service;

import org.springframework.http.HttpStatus;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Decides whether uploaded bytes may become a profile photo (C107): JPEG, PNG or WebP only (never SVG),
 * at most 1 MB, content type confirmed by the file's own magic bytes and not just the request header, and
 * at most 4096 x 4096 pixels. JPEG and PNG are fully decoded (so a corrupt file is refused); the JDK has no
 * WebP decoder, so a WebP is checked by its container header and its declared dimensions only.
 */
public final class ProfilePhotoInspector {

    public static final int MAX_BYTES = 1_048_576;
    public static final int MAX_DIMENSION = 4096;
    public static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private ProfilePhotoInspector() {
    }

    /** Validates and returns the detected content type (the one that is stored). */
    public static String inspect(byte[] bytes, String declaredContentType) {
        if (bytes == null || bytes.length == 0) {
            throw bad("Choose an image file to upload");
        }
        if (bytes.length > MAX_BYTES) {
            throw new ProfileException(HttpStatus.PAYLOAD_TOO_LARGE, "The photo must be 1 MB or smaller");
        }
        String declared = declaredContentType == null ? "" : declaredContentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
        if (!ALLOWED_TYPES.contains(declared)) {
            throw bad("The photo must be a JPEG, PNG or WebP image");
        }
        String detected = detect(bytes);
        if (detected == null) {
            throw bad("The file is not a JPEG, PNG or WebP image");
        }
        if (!detected.equals(declared)) {
            throw bad("The file content does not match its declared image type");
        }
        int[] size = "image/webp".equals(detected) ? webpSize(bytes) : decode(bytes);
        if (size == null) {
            throw bad("The image could not be read");
        }
        if (size[0] < 1 || size[1] < 1 || size[0] > MAX_DIMENSION || size[1] > MAX_DIMENSION) {
            throw bad("The image must be at most " + MAX_DIMENSION + " x " + MAX_DIMENSION + " pixels");
        }
        return detected;
    }

    static String detect(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
            && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "image/png";
        }
        if (b.length >= 30 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
            && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    /** JPEG/PNG: read the declared size first (so a decompression bomb is refused before decoding), then decode. */
    private static int[] decode(byte[] bytes) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= MAX_DIMENSION && height <= MAX_DIMENSION && width > 0 && height > 0) {
                    reader.read(0);
                }
                return new int[] {width, height};
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /** WebP: canvas size from the VP8 (lossy), VP8L (lossless) or VP8X (extended) header. */
    static int[] webpSize(byte[] b) {
        String chunk = new String(b, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
        switch (chunk) {
            case "VP8X":
                if (b.length < 30) return null;
                return new int[] {1 + le24(b, 24), 1 + le24(b, 27)};
            case "VP8L":
                if (b.length < 25 || (b[20] & 0xFF) != 0x2F) return null;
                int bits = (b[21] & 0xFF) | (b[22] & 0xFF) << 8 | (b[23] & 0xFF) << 16 | (b[24] & 0xFF) << 24;
                return new int[] {1 + (bits & 0x3FFF), 1 + ((bits >> 14) & 0x3FFF)};
            case "VP8 ":
                if (b.length < 30 || (b[23] & 0xFF) != 0x9D || (b[24] & 0xFF) != 0x01 || (b[25] & 0xFF) != 0x2A) return null;
                return new int[] {((b[26] & 0xFF) | (b[27] & 0xFF) << 8) & 0x3FFF, ((b[28] & 0xFF) | (b[29] & 0xFF) << 8) & 0x3FFF};
            default:
                return null;
        }
    }

    private static int le24(byte[] b, int at) {
        return (b[at] & 0xFF) | (b[at + 1] & 0xFF) << 8 | (b[at + 2] & 0xFF) << 16;
    }

    private static ProfileException bad(String message) {
        return new ProfileException(HttpStatus.BAD_REQUEST, message);
    }
}
