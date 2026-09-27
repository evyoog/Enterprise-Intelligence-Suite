package com.vyoog.eisplatform.modules.administration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 15.01.01 Configure currencies (sprint 2026.4.2). Seeded from the same
 * fixed set {@link com.vyoog.eisplatform.modules.product.model.Currency}
 * already uses for plan pricing (02.05.02.01, sprint 2026.4.1) — this table
 * doesn't replace that enum (changing ProductPlan.currency's type is out of
 * scope; it stays a fixed, validated set), it only lets an admin mark a
 * currency ENABLED or not, which the frontend then uses to narrow the
 * currency choices shown on the plan editor. Disabling one here does not
 * touch a plan that already uses it.
 */
@Entity
@Table(name = "platform_currency")
@Getter
@Setter
public class PlatformCurrency {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;
}
