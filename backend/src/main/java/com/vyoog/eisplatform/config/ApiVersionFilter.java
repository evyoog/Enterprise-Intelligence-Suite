package com.vyoog.eisplatform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * REQ-INT-001.3 (C61, BR-7): every endpoint is also served at {@code /v1/...};
 * the request is presented to everything after this filter (security
 * included) as the unversioned path, so the same rules apply. Every
 * response carries {@code API-Version: 1}. Registered ahead of Spring
 * Security (ApiManagementConfig).
 */
public class ApiVersionFilter extends OncePerRequestFilter {

    public static final String VERSION = "1";
    private static final String PREFIX = "/v" + VERSION;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("API-Version", VERSION);
        String context = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(context.length());
        if (path.equals(PREFIX) || path.startsWith(PREFIX + "/")) {
            chain.doFilter(new Unversioned(request, context + path.substring(PREFIX.length())), response);
            return;
        }
        chain.doFilter(request, response);
    }

    private static final class Unversioned extends HttpServletRequestWrapper {
        private final String uri;

        Unversioned(HttpServletRequest request, String uri) {
            super(request);
            this.uri = uri.equals(request.getContextPath()) ? uri + "/" : uri;
        }

        private static String strip(String value) {
            if (value == null) {
                return null;
            }
            if (value.equals(PREFIX)) {
                return "/";
            }
            return value.startsWith(PREFIX + "/") ? value.substring(PREFIX.length()) : value;
        }

        @Override
        public String getRequestURI() {
            return uri;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer(super.getRequestURL());
            int at = url.indexOf(super.getRequestURI());
            if (at >= 0) {
                url.replace(at, url.length(), uri);
            }
            return url;
        }

        @Override
        public String getServletPath() {
            return strip(super.getServletPath());
        }

        @Override
        public String getPathInfo() {
            return strip(super.getPathInfo());
        }
    }
}
