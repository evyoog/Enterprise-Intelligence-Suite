package com.vyoog.eisplatform.modules.access.service;

import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * REQ-TEN-005 (C90): once the V030 migration is in place, brings every organization's stored product access in line with
 * its effective access (every entitled product ON for members by default, AM-1; named seats, AM-3 B). Idempotent: a
 * second run changes nothing. Switch off with {@code eis.access.backfill-on-startup=false}.
 */
@Component
@RequiredArgsConstructor
public class AccessBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AccessBackfillRunner.class);

    private final OrganizationRepository organizationRepository;
    private final AccessSyncService accessSyncService;
    private final TransactionTemplate transactionTemplate;

    @Value("${eis.access.backfill-on-startup:true}")
    private boolean enabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        for (Organization organization : organizationRepository.findAll()) {
            try {
                int changes = transactionTemplate.execute(status ->
                    accessSyncService.recomputeOrganization(organization.getId(), null).size());
                if (changes > 0) {
                    log.info("Access backfill: organization {} had {} product access changes", organization.getId(), changes);
                }
            } catch (RuntimeException e) {
                log.warn("Access backfill failed for organization {}: {}", organization.getId(), e.getMessage());
            }
        }
    }
}
