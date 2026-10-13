package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Turns the Keycloak subject stored on a rule or setting into something a person recognises: the account's e-mail when
 * the subject belongs to a customer row, otherwise a short form of the subject. */
@Component
@RequiredArgsConstructor
public class TaxActorLabels {

    private final CustomerRepository customerRepository;

    public String label(String keycloakSub) {
        if (keycloakSub == null || keycloakSub.isBlank()) {
            return null;
        }
        return customerRepository.findByKeycloakSub(keycloakSub).map(c -> c.getEmail()).orElse(
            keycloakSub.length() > 12 ? keycloakSub.substring(0, 8) + "…" : keycloakSub);
    }
}
