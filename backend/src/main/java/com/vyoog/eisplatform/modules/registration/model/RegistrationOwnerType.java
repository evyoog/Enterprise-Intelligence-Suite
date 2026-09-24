package com.vyoog.eisplatform.modules.registration.model;

/** Whether something (a subscription, an email-verification token) belongs
 * to a lone person or to a company — shared by both so the codebase doesn't
 * grow two parallel INDIVIDUAL/ORGANIZATION enums for the same distinction. */
public enum RegistrationOwnerType {
    INDIVIDUAL,
    ORGANIZATION
}
