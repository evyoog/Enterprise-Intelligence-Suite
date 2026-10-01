package com.vyoog.eisplatform.modules.cart.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** C59 (REQ-MKT-003.2): one server-side cart per signed-in user, so it
 * survives sign-out and is the same on every device. The {@code lastCheckout*}
 * fields make checkout idempotent (BR-10): a repeated Proceed right after a
 * successful one returns the same invoice or orders. */
@Entity
@Table(name = "cart")
@Getter
@Setter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "last_checkout_kind", length = 10)
    private String lastCheckoutKind;

    /** Invoice id, or comma-separated order ids. */
    @Column(name = "last_checkout_ref", length = 500)
    private String lastCheckoutRef;

    @Column(name = "last_checkout_at")
    private Instant lastCheckoutAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("addedAt ASC, id ASC")
    private List<CartItem> items = new ArrayList<>();
}
