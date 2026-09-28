package com.vyoog.eisplatform.modules.partner.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.partner.dto.ApplyAsProviderRequest;
import com.vyoog.eisplatform.modules.partner.dto.CreateOrUpdateContractRequest;
import com.vyoog.eisplatform.modules.partner.dto.PartnerContractDto;
import com.vyoog.eisplatform.modules.partner.dto.ProviderDto;
import com.vyoog.eisplatform.modules.partner.model.ContractStatus;
import com.vyoog.eisplatform.modules.partner.model.ProviderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 14.01 Provider Onboarding (sprint 2027.2.1). */
@SpringBootTest
@ActiveProfiles("test")
class PartnerServiceTest {

    @Autowired
    private PartnerService partnerService;

    private ProviderDto newProvider() {
        return partnerService.apply(new ApplyAsProviderRequest(
            "Acme Cloud " + System.nanoTime(), "Jane Doe", "jane@acme.example", "A cloud reseller."));
    }

    @Test
    void aRegisteredProviderMovesThroughEveryStageInOrder() {
        ProviderDto provider = newProvider();
        assertThat(provider.status()).isEqualTo(ProviderStatus.REGISTERED);

        ProviderDto verified = partnerService.verify(provider.id());
        assertThat(verified.status()).isEqualTo(ProviderStatus.VERIFIED);

        ProviderDto approved = partnerService.approve(provider.id());
        assertThat(approved.status()).isEqualTo(ProviderStatus.APPROVED);

        ProviderDto activated = partnerService.activate(provider.id());
        assertThat(activated.status()).isEqualTo(ProviderStatus.ACTIVE);
    }

    @Test
    void aStageCannotBeSkipped() {
        ProviderDto provider = newProvider();
        assertThatThrownBy(() -> partnerService.approve(provider.id())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> partnerService.activate(provider.id())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectionIsAllowedUntilActiveThenTerminal() {
        ProviderDto provider = newProvider();
        partnerService.verify(provider.id());

        ProviderDto rejected = partnerService.reject(provider.id());
        assertThat(rejected.status()).isEqualTo(ProviderStatus.REJECTED);
        assertThatThrownBy(() -> partnerService.reject(provider.id())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> partnerService.verify(provider.id())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anActiveProviderCanNoLongerBeRejected() {
        ProviderDto provider = newProvider();
        partnerService.verify(provider.id());
        partnerService.approve(provider.id());
        partnerService.activate(provider.id());

        assertThatThrownBy(() -> partnerService.reject(provider.id())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creatingAContractRequiresAnExistingProvider() {
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest(
            "Standard reseller terms", LocalDate.now(), LocalDate.now().plusYears(1));
        assertThatThrownBy(() -> partnerService.createOrUpdateContract(999_999_999L, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void aContractsEndDateMustBeAfterItsStartDate() {
        ProviderDto provider = newProvider();
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest(
            "Standard reseller terms", LocalDate.now(), LocalDate.now());
        assertThatThrownBy(() -> partnerService.createOrUpdateContract(provider.id(), request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void savingAContractAgainEditsTheSameOneAndReactivatesIt() {
        ProviderDto provider = newProvider();
        PartnerContractDto first = partnerService.createOrUpdateContract(provider.id(),
            new CreateOrUpdateContractRequest("Draft terms", LocalDate.now(), LocalDate.now().plusMonths(1)));

        PartnerContractDto edited = partnerService.createOrUpdateContract(provider.id(),
            new CreateOrUpdateContractRequest("Final terms", LocalDate.now(), LocalDate.now().plusYears(1)));

        assertThat(edited.id()).isEqualTo(first.id());
        assertThat(edited.terms()).isEqualTo("Final terms");
        assertThat(edited.status()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(partnerService.getContract(provider.id()).terms()).isEqualTo("Final terms");
    }

    @Test
    void expireOverdueContractsFlipsOnlyPastEndDates() {
        ProviderDto stillActive = newProvider();
        partnerService.createOrUpdateContract(stillActive.id(),
            new CreateOrUpdateContractRequest("Terms", LocalDate.now(), LocalDate.now().plusYears(1)));

        ProviderDto overdue = newProvider();
        PartnerContractDto overdueContract = partnerService.createOrUpdateContract(overdue.id(),
            new CreateOrUpdateContractRequest("Terms", LocalDate.now().minusYears(2), LocalDate.now().minusDays(1)));

        int expiredCount = partnerService.expireOverdueContracts();
        assertThat(expiredCount).isGreaterThanOrEqualTo(1);
        assertThat(partnerService.getContract(overdue.id()).status()).isEqualTo(ContractStatus.EXPIRED);
        assertThat(partnerService.getContract(stillActive.id()).status()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(overdueContract.status()).isEqualTo(ContractStatus.ACTIVE);
    }
}
