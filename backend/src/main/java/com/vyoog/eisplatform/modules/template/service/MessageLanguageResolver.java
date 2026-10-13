package com.vyoog.eisplatform.modules.template.service;

import com.vyoog.eisplatform.modules.administration.service.PlatformDefaultsService;
import com.vyoog.eisplatform.modules.preference.repository.CustomerPreferenceRepository;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Which language a message goes out in (REQ-GOV-002, C92): the recipient's own language preference, else the platform
 * default language (TM-2), else English. A person without an account (for example an invited address) gets the platform
 * default.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageLanguageResolver {

    private final CustomerPreferenceRepository preferenceRepository;
    private final CustomerRepository customerRepository;
    private final PlatformDefaultsService platformDefaultsService;

    public String forCustomer(Long customerId) {
        String own = customerId == null ? null : preferenceRepository.findById(customerId).map(p -> p.getLanguage()).orElse(null);
        return supported(own).or(() -> supported(safeDefault())).orElse(MessageCatalog.DEFAULT_LANGUAGE);
    }

    public String forEmail(String email) {
        if (email == null) {
            return forCustomer(null);
        }
        return forCustomer(customerRepository.findByEmailIgnoreCase(email).map(c -> c.getId()).orElse(null));
    }

    private String safeDefault() {
        try {
            return platformDefaultsService.defaultLanguage();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Optional<String> supported(String language) {
        return language != null && MessageCatalog.LANGUAGES.contains(language) ? Optional.of(language) : Optional.empty();
    }
}
