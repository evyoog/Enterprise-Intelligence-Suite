package com.vyoog.eisplatform.modules.productcontent.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-CAT-004.6, BR-PCON-007. */
class ProductVideoLinkTest {

    @Test
    void recognisesEveryCommonYouTubeAddress() {
        for (String url : new String[] {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "https://youtube.com/watch?feature=share&v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?t=10", "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ", "https://m.youtube.com/watch?v=dQw4w9WgXcQ"}) {
            ProductVideoLink link = ProductVideoLink.parse(url);
            assertThat(link.provider()).as(url).isEqualTo("YOUTUBE");
            assertThat(link.ref()).isEqualTo("dQw4w9WgXcQ");
            assertThat(link.thumbnailUrl()).isEqualTo("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg");
        }
        assertThat(ProductVideoLink.embedUrl("YOUTUBE", "dQw4w9WgXcQ"))
            .isEqualTo("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ");
    }

    @Test
    void recognisesVimeo() {
        ProductVideoLink link = ProductVideoLink.parse("https://vimeo.com/76979871");
        assertThat(link.provider()).isEqualTo("VIMEO");
        assertThat(link.ref()).isEqualTo("76979871");
        assertThat(ProductVideoLink.parse("https://player.vimeo.com/video/76979871").ref()).isEqualTo("76979871");
        assertThat(ProductVideoLink.embedUrl("VIMEO", "76979871")).isEqualTo("https://player.vimeo.com/video/76979871?dnt=1");
    }

    @Test
    void anyOtherHttpsLinkIsAPlainLinkWithNoEmbed() {
        ProductVideoLink link = ProductVideoLink.parse("https://videos.example.com/demo.mp4");
        assertThat(link.provider()).isEqualTo("EXTERNAL");
        assertThat(link.ref()).isNull();
        assertThat(ProductVideoLink.embedUrl("EXTERNAL", null)).isNull();
        // A look-alike host is not YouTube.
        assertThat(ProductVideoLink.parse("https://notyoutube.com/watch?v=dQw4w9WgXcQ").provider()).isEqualTo("EXTERNAL");
        assertThat(ProductVideoLink.parse("https://youtube.com.evil.example/watch?v=dQw4w9WgXcQ").provider()).isEqualTo("EXTERNAL");
    }

    @Test
    void refusesAnythingThatIsNotHttps() {
        for (String url : new String[] {"http://youtu.be/dQw4w9WgXcQ", "javascript:alert(1)", "ftp://x.example/a", "//x.example/a",
            "not a link", "", "https://user:pw@x.example/a"}) {
            assertThatThrownBy(() -> ProductVideoLink.parse(url)).as(url).isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> ProductVideoLink.parse(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
