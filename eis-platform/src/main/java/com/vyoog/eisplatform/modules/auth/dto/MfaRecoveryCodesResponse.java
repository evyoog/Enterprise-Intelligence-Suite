package com.vyoog.eisplatform.modules.auth.dto;

import java.util.List;

/** Raw recovery codes — only ever returned from enrollment-completion or a
 * deliberate regenerate call, never retrievable again afterward (only
 * hashes are persisted, see MfaRecoveryCode). */
public record MfaRecoveryCodesResponse(List<String> codes) {
}
