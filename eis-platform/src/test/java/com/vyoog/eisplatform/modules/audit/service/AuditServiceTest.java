package com.vyoog.eisplatform.modules.audit.service;

import com.vyoog.eisplatform.modules.audit.dto.AuditLogPageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuditServiceTest {

    @Autowired
    private AuditService auditService;

    @Test
    void recordSuccessThenSearchFindsItByAction() {
        String action = "TEST_ACTION_" + System.nanoTime();
        auditService.recordSuccess(action, "sub-1", 42L, "actor@example.com", "Thing", "99", 7L, "did a thing");

        AuditLogPageDto page = auditService.search(null, null, action, null, null, 0, 50);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).actorCustomerId()).isEqualTo(42L);
        assertThat(page.items().get(0).organizationId()).isEqualTo(7L);
        assertThat(page.items().get(0).outcome()).isEqualTo("SUCCESS");
    }

    @Test
    void recordFailureHasNoActorIdentityButKeepsTheEmail() {
        String action = "TEST_FAILURE_" + System.nanoTime();
        auditService.recordFailure(action, "failed@example.com", "bad password");

        AuditLogPageDto page = auditService.search(null, null, action, null, null, 0, 50);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).actorCustomerId()).isNull();
        assertThat(page.items().get(0).actorEmail()).isEqualTo("failed@example.com");
        assertThat(page.items().get(0).outcome()).isEqualTo("FAILURE");
    }

    @Test
    void searchFiltersByOrganizationId() {
        String action = "TEST_ORG_FILTER_" + System.nanoTime();
        long orgA = System.nanoTime();
        long orgB = orgA + 1;
        auditService.recordSuccess(action, null, null, null, "Thing", "1", orgA, "in org A");
        auditService.recordSuccess(action, null, null, null, "Thing", "2", orgB, "in org B");

        AuditLogPageDto page = auditService.search(orgA, null, action, null, null, 0, 50);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).organizationId()).isEqualTo(orgA);
    }

    @Test
    void searchPaginatesWithATotalCount() {
        String action = "TEST_PAGINATION_" + System.nanoTime();
        for (int i = 0; i < 5; i++) {
            auditService.recordSuccess(action, null, null, null, "Thing", String.valueOf(i), null, null);
        }

        AuditLogPageDto page = auditService.search(null, null, action, null, null, 0, 2);

        assertThat(page.items()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(5);
    }
}
