package com.vyoog.eisplatform.modules.profile.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.vyoog.eisplatform.modules.profile.service.ProfileTestImages.image;
import static com.vyoog.eisplatform.modules.profile.service.ProfileTestImages.webpExtended;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** My profile (C107): the photo rules (type, size, magic bytes, dimensions), in isolation. */
class ProfilePhotoInspectorTest {

    private static ProfileException refused(byte[] bytes, String type) {
        return (ProfileException) org.junit.jupiter.api.Assertions.assertThrows(ProfileException.class,
            () -> ProfilePhotoInspector.inspect(bytes, type));
    }

    @Test
    void acceptsJpegPngAndWebpWhoseContentMatchesTheirType() {
        assertThat(ProfilePhotoInspector.inspect(image("jpg", 64, 64), "image/jpeg")).isEqualTo("image/jpeg");
        assertThat(ProfilePhotoInspector.inspect(image("png", 64, 48), "image/png")).isEqualTo("image/png");
        assertThat(ProfilePhotoInspector.inspect(webpExtended(512, 512), "image/webp")).isEqualTo("image/webp");
        assertThat(ProfilePhotoInspector.inspect(image("png", 8, 8), "IMAGE/PNG; charset=binary")).isEqualTo("image/png");
    }

    @Test
    void refusesSvgGifAndAnyOtherDeclaredType() {
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8);
        assertThat(refused(svg, "image/svg+xml").getStatus().value()).isEqualTo(400);
        assertThat(refused(image("gif", 8, 8), "image/gif").getStatus().value()).isEqualTo(400);
        assertThat(refused(image("png", 8, 8), "application/octet-stream").getStatus().value()).isEqualTo(400);
        assertThat(refused(image("png", 8, 8), null).getStatus().value()).isEqualTo(400);
    }

    @Test
    void theMagicBytesDecideNotTheHeader() {
        assertThat(refused("<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8), "image/png").getMessage())
            .contains("not a JPEG, PNG or WebP");
        assertThat(refused("<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8), "image/jpeg").getStatus().value())
            .isEqualTo(400);
        // A real PNG declared as JPEG: the type must match the content.
        assertThat(refused(image("png", 8, 8), "image/jpeg").getMessage()).contains("does not match");
        assertThat(refused(image("jpg", 8, 8), "image/webp").getStatus().value()).isEqualTo(400);
    }

    @Test
    void refusesEmptyAndOversizeFiles() {
        assertThat(refused(new byte[0], "image/png").getStatus().value()).isEqualTo(400);
        byte[] big = new byte[ProfilePhotoInspector.MAX_BYTES + 1];
        big[0] = (byte) 0x89;
        ProfileException tooBig = refused(big, "image/png");
        assertThat(tooBig.getStatus().value()).isEqualTo(413);
        byte[] atLimit = Arrays.copyOf(image("png", 8, 8), 8);
        assertThatThrownBy(() -> ProfilePhotoInspector.inspect(atLimit, "image/png")).isInstanceOf(ProfileException.class); // truncated, not a limit problem
    }

    @Test
    void refusesACorruptImageThatOnlyHasTheRightFirstBytes() {
        byte[] png = image("png", 32, 32);
        byte[] truncated = Arrays.copyOf(png, png.length / 2);
        ProfileException e = refused(truncated, "image/png");
        assertThat(e.getStatus().value()).isEqualTo(400);
        byte[] fakeJpeg = new byte[200];
        fakeJpeg[0] = (byte) 0xFF;
        fakeJpeg[1] = (byte) 0xD8;
        fakeJpeg[2] = (byte) 0xFF;
        assertThat(refused(fakeJpeg, "image/jpeg").getStatus().value()).isEqualTo(400);
    }

    @Test
    void refusesImagesLargerThan4096PixelsInEitherDirection() {
        assertThat(ProfilePhotoInspector.inspect(image("png", 4096, 16), "image/png")).isEqualTo("image/png");
        assertThat(refused(image("png", 4097, 16), "image/png").getMessage()).contains("4096");
        assertThat(refused(image("png", 16, 4097), "image/png").getMessage()).contains("4096");
        assertThat(ProfilePhotoInspector.inspect(webpExtended(4096, 4096), "image/webp")).isEqualTo("image/webp");
        assertThat(refused(webpExtended(4097, 100), "image/webp").getMessage()).contains("4096");
        assertThat(refused(webpExtended(100, 4097), "image/webp").getMessage()).contains("4096");
    }

    @Test
    void readsTheDimensionsOfLossyAndLosslessWebpHeaders() {
        byte[] lossy = new byte[40];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, lossy, 0, 4);
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, lossy, 8, 4);
        System.arraycopy("VP8 ".getBytes(StandardCharsets.US_ASCII), 0, lossy, 12, 4);
        lossy[23] = (byte) 0x9D;
        lossy[24] = 0x01;
        lossy[25] = 0x2A;
        lossy[26] = (byte) (300 & 0xFF);
        lossy[27] = (byte) (300 >> 8);
        lossy[28] = (byte) (200 & 0xFF);
        lossy[29] = (byte) (200 >> 8);
        assertThat(ProfilePhotoInspector.webpSize(lossy)).containsExactly(300, 200);

        byte[] lossless = new byte[40];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, lossless, 0, 4);
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, lossless, 8, 4);
        System.arraycopy("VP8L".getBytes(StandardCharsets.US_ASCII), 0, lossless, 12, 4);
        lossless[20] = 0x2F;
        int bits = (500 - 1) | ((400 - 1) << 14);
        lossless[21] = (byte) bits;
        lossless[22] = (byte) (bits >> 8);
        lossless[23] = (byte) (bits >> 16);
        lossless[24] = (byte) (bits >> 24);
        assertThat(ProfilePhotoInspector.webpSize(lossless)).containsExactly(500, 400);
    }

    @Test
    void aWebpContainerWithAnUnknownChunkIsRefused() {
        byte[] b = webpExtended(10, 10);
        System.arraycopy("JUNK".getBytes(StandardCharsets.US_ASCII), 0, b, 12, 4);
        assertThat(refused(b, "image/webp").getStatus().value()).isEqualTo(400);
    }
}
