package com.vyoog.eisplatform.modules.billing.service.gateway;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C98 (REQ-BIL-003.5): AES-256-GCM with a master key from the environment. */
class CredentialCipherTest {

    private static String key(int bytes, char fill) {
        return Base64.getEncoder().encodeToString(String.valueOf(fill).repeat(bytes).getBytes());
    }

    @Test
    void roundTripsAndNeverRepeatsACiphertext() {
        CredentialCipher cipher = new CredentialCipher(key(32, 'a'));
        String a = cipher.encrypt("{\"secret\":\"value\"}");
        String b = cipher.encrypt("{\"secret\":\"value\"}");

        assertThat(cipher.available()).isTrue();
        assertThat(a).isNotEqualTo(b).doesNotContain("value");
        assertThat(cipher.decrypt(a)).isEqualTo("{\"secret\":\"value\"}");
    }

    @Test
    void aDifferentKeyOrTamperedDataCannotBeRead() {
        String encrypted = new CredentialCipher(key(32, 'a')).encrypt("secret");

        assertThat(new CredentialCipher(key(32, 'b')).decrypt(encrypted)).isNull();
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        bytes[bytes.length - 1] ^= 1;
        assertThat(new CredentialCipher(key(32, 'a')).decrypt(Base64.getEncoder().encodeToString(bytes))).isNull();
        assertThat(new CredentialCipher(key(32, 'a')).decrypt("not base64 !!")).isNull();
        assertThat(new CredentialCipher(key(32, 'a')).decrypt(null)).isNull();
    }

    @Test
    void withoutAUsableKeyNothingCanBeStored() {
        for (String bad : new String[] {"", "   ", "not base64!!", key(16, 'a'), key(31, 'a')}) {
            CredentialCipher cipher = new CredentialCipher(bad);
            assertThat(cipher.available()).as("[" + bad + "]").isFalse();
            assertThatThrownBy(() -> cipher.encrypt("x")).isInstanceOf(IllegalStateException.class);
            assertThat(cipher.decrypt("anything")).isNull();
        }
    }
}
