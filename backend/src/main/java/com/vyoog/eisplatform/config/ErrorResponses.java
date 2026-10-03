package com.vyoog.eisplatform.config;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

/** The standard error body (same shape as GlobalExceptionHandler) for
 * responses written directly by servlet filters. */
final class ErrorResponses {

    private ErrorResponses() {
    }

    static void write(HttpServletResponse response, int status, String error, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String body = "{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status + ",\"error\":\"" + error + "\""
            + (code == null ? "" : ",\"code\":\"" + code + "\"")
            + ",\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
        response.getWriter().write(body);
    }
}
