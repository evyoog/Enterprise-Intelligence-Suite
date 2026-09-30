package com.vyoog.eisplatform.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(SeatLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleSeatLimit(SeatLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(ProductInUseException.class)
    public ResponseEntity<Map<String, Object>> handleProductInUse(ProductInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    /** Phase 7: distinct from a generic 401 specifically so the frontend can
     * show an inline OTP field instead of "invalid credentials" — see this
     * exception's own javadoc. */
    @ExceptionHandler(MfaChallengeRequiredException.class)
    public ResponseEntity<Map<String, Object>> handleMfaChallenge(MfaChallengeRequiredException ex) {
        Map<String, Object> body = body(HttpStatus.UNAUTHORIZED, ex.getMessage());
        body.put("mfaRequired", true);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /** Phase 7: distinct from a generic 403 so the frontend can show "your
     * organization requires MFA" rather than a bare permission error. */
    @ExceptionHandler(OrganizationMfaRequiredException.class)
    public ResponseEntity<Map<String, Object>> handleOrganizationMfaRequired(OrganizationMfaRequiredException ex) {
        Map<String, Object> body = body(HttpStatus.FORBIDDEN, ex.getMessage());
        body.put("organizationMfaRequired", true);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /** Phase 2 (2026.3.3): a real Keycloak login just succeeded, but this
     * customer's own Platform TOTP still needs to be verified — the frontend
     * uses {@code mfaChallengeId} to call {@code POST /auth/mfa/verify}. */
    @ExceptionHandler(PlatformMfaChallengeRequiredException.class)
    public ResponseEntity<Map<String, Object>> handlePlatformMfaChallenge(PlatformMfaChallengeRequiredException ex) {
        Map<String, Object> body = body(HttpStatus.UNAUTHORIZED, ex.getMessage());
        body.put("platformMfaRequired", true);
        body.put("mfaChallengeId", ex.getChallengeId());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /** C29: the organization requires MFA and this member has no authenticator
     * yet — the frontend sets one up with {@code mfaEnrollmentChallengeId}. */
    @ExceptionHandler(PlatformMfaEnrollmentRequiredException.class)
    public ResponseEntity<Map<String, Object>> handlePlatformMfaEnrollment(PlatformMfaEnrollmentRequiredException ex) {
        Map<String, Object> body = body(HttpStatus.UNAUTHORIZED, ex.getMessage());
        body.put("platformMfaEnrollmentRequired", true);
        body.put("mfaEnrollmentChallengeId", ex.getChallengeId());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /** Billing & Payments, BR-10: every Razorpay-dependent action refuses
     * with this when a credential is missing (REQ-BIL-001.14). */
    @ExceptionHandler(PaymentGatewayNotConfiguredException.class)
    public ResponseEntity<Map<String, Object>> handleGatewayNotConfigured(PaymentGatewayNotConfiguredException ex) {
        Map<String, Object> body = body(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        body.put("code", "PAYMENT_GATEWAY_NOT_CONFIGURED");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(RazorpayApiException.class)
    public ResponseEntity<Map<String, Object>> handleGatewayError(RazorpayApiException ex) {
        Map<String, Object> body = body(HttpStatus.BAD_GATEWAY, ex.getMessage());
        body.put("code", "PAYMENT_GATEWAY_ERROR");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(InvalidPaymentSignatureException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidSignature(InvalidPaymentSignatureException ex) {
        Map<String, Object> body = body(HttpStatus.BAD_REQUEST, ex.getMessage());
        body.put("code", "SIGNATURE_INVALID");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BillingConflictException.class)
    public ResponseEntity<Map<String, Object>> handleBillingConflict(BillingConflictException ex) {
        Map<String, Object> body = body(HttpStatus.CONFLICT, ex.getMessage());
        body.put("code", "INVALID_STATE");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadInput(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        // The client only ever gets "An unexpected error occurred" — deliberately,
        // to never leak internals (a stack trace, a class name, a SQL fragment) in
        // an HTTP response. Without this log line, that also meant nothing about
        // the failure was recorded anywhere: a real NPE in product creation once
        // produced a 500 with no server-side trace of what happened at all.
        log.error("Unhandled exception on {}", ex.getClass().getSimpleName(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }

    private Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
