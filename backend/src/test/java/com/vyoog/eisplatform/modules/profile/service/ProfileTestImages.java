package com.vyoog.eisplatform.modules.profile.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Builds small real images for the My profile photo tests. */
public final class ProfileTestImages {

    private ProfileTestImages() {
    }

    public static byte[] image(String format, int width, int height) {
        try {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, format, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** A minimal WebP container of the "extended" kind, enough for the declared-size check. */
    public static byte[] webpExtended(int width, int height) {
        byte[] b = new byte[40];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, b, 0, 4);
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, b, 8, 4);
        System.arraycopy("VP8X".getBytes(StandardCharsets.US_ASCII), 0, b, 12, 4);
        b[16] = 10;
        int w = width - 1;
        int h = height - 1;
        b[24] = (byte) w;
        b[25] = (byte) (w >> 8);
        b[26] = (byte) (w >> 16);
        b[27] = (byte) h;
        b[28] = (byte) (h >> 8);
        b[29] = (byte) (h >> 16);
        return b;
    }
}
