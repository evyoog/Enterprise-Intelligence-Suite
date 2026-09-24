package com.vyoog.eisplatform.modules.auth.dto;

/** {@code secret} (the Base32 manual-entry key) and {@code qrCodePngBase64}
 * are only ever returned from this one call, in direct response to the
 * customer's own authenticated request — never logged, never persisted in
 * plaintext (see CustomerMfaCredential/TotpSecretCipher). */
public record MfaEnrollResponse(String secret, String otpAuthUri, String qrCodePngBase64) {
}
