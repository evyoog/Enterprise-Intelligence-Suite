package com.vyoog.eisplatform.modules.federation.controller;

import com.vyoog.eisplatform.modules.federation.service.SamlProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 4 (2026.3.3): this platform's own SP metadata — deliberately public
 * (no JWT), same as every other pre-authentication SAML/registration
 * endpoint in this app, since the party fetching it is a customer's own IdP
 * administrator configuring their side, who has no Vyoog account or token
 * at all. Publishes nothing sensitive: an entity id, an ACS URL, and a
 * NameID format — no secret, no credential, no internal detail.
 */
@RestController
@RequiredArgsConstructor
public class SamlMetadataController {

    private final SamlProviderService samlProviderService;

    @GetMapping(value = "/saml/{organizationId}/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    public String spMetadata(@PathVariable Long organizationId) {
        return samlProviderService.getSpMetadataXml(organizationId);
    }
}
