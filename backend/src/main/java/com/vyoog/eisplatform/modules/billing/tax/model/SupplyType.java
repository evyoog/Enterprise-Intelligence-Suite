package com.vyoog.eisplatform.modules.billing.tax.model;

/** BR-14: where the supply is made, relative to the seller. ANY on a rule means the condition is not used. */
public enum SupplyType {
    ANY, INTRA_STATE, INTER_STATE, CROSS_BORDER
}
