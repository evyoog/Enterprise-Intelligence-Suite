package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** REQ-INT-001 (C92, API-5 B): deprecated endpoints carry Deprecation, Sunset and Link headers. TC-INT-034. */
class ApiDeprecationFilterTest {

    private final ApiDeprecationFilter filter = new ApiDeprecationFilter(List.of(
        new ApiDeprecationFilter.Rule("/v1/", LocalDate.of(2027, 12, 31), "/v2/"),
        new ApiDeprecationFilter.Rule("/legacy-orders", LocalDate.of(2027, 6, 30), null),
        new ApiDeprecationFilter.Rule("", LocalDate.of(2027, 1, 1), null), // ignored: no path
        new ApiDeprecationFilter.Rule("/no-date", null, null)));           // ignored: no removal date

    private MockHttpServletResponse call(String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void aDeprecatedVersionAndItsPathsGetTheHeadersWithASuccessor() throws Exception {
        for (String uri : new String[] {"/v1", "/v1/", "/v1/organization/me"}) {
            MockHttpServletResponse response = call(uri);
            assertThat(response.getHeader("Deprecation")).isEqualTo("true");
            assertThat(response.getHeader("Sunset")).isEqualTo("Fri, 31 Dec 2027 00:00:00 GMT");
            assertThat(response.getHeader("Link")).isEqualTo("</v2/>; rel=\"successor-version\"");
        }
    }

    @Test
    void anEndpointWithoutASuccessorGetsNoLinkAndOtherPathsGetNothing() throws Exception {
        MockHttpServletResponse legacy = call("/legacy-orders/42");
        assertThat(legacy.getHeader("Deprecation")).isEqualTo("true");
        assertThat(legacy.getHeader("Sunset")).isEqualTo("Wed, 30 Jun 2027 00:00:00 GMT");
        assertThat(legacy.getHeader("Link")).isNull();

        for (String uri : new String[] {"/v10/x", "/organization/me", "/legacy-orders-extra", "/no-date"}) {
            MockHttpServletResponse response = call(uri);
            assertThat(response.getHeader("Deprecation")).as(uri).isNull();
            assertThat(response.getHeader("Sunset")).as(uri).isNull();
        }
    }

    @Test
    void nothingIsDeprecatedByDefault() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/anything");
        request.setRequestURI("/v1/anything");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ApiDeprecationFilter(List.of()).doFilter(request, response, new MockFilterChain());
        assertThat(response.getHeader("Deprecation")).isNull();
    }
}
