package com.vyoog.eisplatform.modules.federation.dto;

/** Same "metadata XML OR manual fields" shape as create — see
 * CreateSamlProviderRequest's own javadoc. {@code name} is always
 * updatable directly. */
public record UpdateSamlProviderRequest(
    String name,
    String metadataXml,
    String entityId,
    String ssoUrl,
    String certificatePem
) {
}
