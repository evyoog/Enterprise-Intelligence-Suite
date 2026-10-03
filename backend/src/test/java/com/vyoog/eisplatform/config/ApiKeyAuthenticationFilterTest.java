package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.integration.service.ApiKeyService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/** REQ-INT-001 (C61): X-API-Key is ignored when an Authorization header is present. */
class ApiKeyAuthenticationFilterTest {

    @Test
    void anAuthorizationHeaderWinsOverAnApiKey() throws Exception {
        ApiKeyService service = mock(ApiKeyService.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(service);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/me/api-keys");
        request.addHeader("Authorization", "Bearer token");
        request.addHeader("X-API-Key", "eis_abcdefgh_" + "z".repeat(40));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verifyNoInteractions(service);
        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
