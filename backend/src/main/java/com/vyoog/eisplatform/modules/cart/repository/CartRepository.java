package com.vyoog.eisplatform.modules.cart.repository;

import com.vyoog.eisplatform.modules.cart.model.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerId(Long customerId);

    /** Checkout holds this lock so two quick Proceed clicks cannot both
     * create an invoice or order (BR-10). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.customerId = :customerId")
    Optional<Cart> lockByCustomerId(@Param("customerId") Long customerId);
}
