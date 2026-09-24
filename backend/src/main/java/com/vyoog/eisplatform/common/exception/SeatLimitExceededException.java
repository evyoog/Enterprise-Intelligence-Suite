package com.vyoog.eisplatform.common.exception;

/** Thrown when an organization has no free licensed seat left — see
 * OrganizationMemberService#assertSeatAvailable. */
public class SeatLimitExceededException extends RuntimeException {

    public SeatLimitExceededException(String message) {
        super(message);
    }
}
