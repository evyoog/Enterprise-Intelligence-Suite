package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.dashboard.dto.SearchHistoryEntryDto;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SearchHistoryServiceTest {

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Autowired
    private CustomerRepository customerRepository;

    private Long newCustomerId() {
        Customer customer = new Customer();
        customer.setEmail("search-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Search");
        customer.setLastName("Tester");
        customer.setStatus(RegistrationStatus.COMPLETED);
        return customerRepository.save(customer).getId();
    }

    @Test
    void recordSearchThenListReturnsItMostRecentFirst() {
        Long customerId = newCustomerId();

        searchHistoryService.recordSearch(customerId, "ticketing");
        searchHistoryService.recordSearch(customerId, "pms");

        List<SearchHistoryEntryDto> history = searchHistoryService.listRecent(customerId);

        assertThat(history).extracting(SearchHistoryEntryDto::query).containsExactly("pms", "ticketing");
    }

    @Test
    void reSearchingTheSameQueryMovesItToTheTopInsteadOfDuplicating() {
        Long customerId = newCustomerId();

        searchHistoryService.recordSearch(customerId, "ticketing");
        searchHistoryService.recordSearch(customerId, "pms");
        searchHistoryService.recordSearch(customerId, "TICKETING");

        List<SearchHistoryEntryDto> history = searchHistoryService.listRecent(customerId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).query()).isEqualTo("TICKETING");
    }

    @Test
    void blankQueryIsNotRecorded() {
        Long customerId = newCustomerId();

        searchHistoryService.recordSearch(customerId, "   ");

        assertThat(searchHistoryService.listRecent(customerId)).isEmpty();
    }

    @Test
    void clearHistoryRemovesEverythingForThatCustomer() {
        Long customerId = newCustomerId();
        searchHistoryService.recordSearch(customerId, "ticketing");

        searchHistoryService.clearHistory(customerId);

        assertThat(searchHistoryService.listRecent(customerId)).isEmpty();
    }

    @Test
    void historyIsScopedPerCustomer() {
        Long customerA = newCustomerId();
        Long customerB = newCustomerId();
        searchHistoryService.recordSearch(customerA, "only-for-a");

        assertThat(searchHistoryService.listRecent(customerB)).isEmpty();
        assertThat(searchHistoryService.listRecent(customerA)).extracting(SearchHistoryEntryDto::query)
            .containsExactly("only-for-a");
    }
}
