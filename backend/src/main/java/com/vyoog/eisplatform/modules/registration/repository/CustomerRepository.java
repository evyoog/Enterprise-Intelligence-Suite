package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("select c from Customer c where lower(c.email) = lower(:email)")
    Optional<Customer> findByEmailIgnoreCase(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByKeycloakSub(String keycloakSub);

    /** The platform admin's "needs a Keycloak user created manually" queue —
     * anyone who has verified their email but has no linked Keycloak
     * identity yet. */
    List<Customer> findByKeycloakSubIsNullAndStatusNot(RegistrationStatus status);
}
