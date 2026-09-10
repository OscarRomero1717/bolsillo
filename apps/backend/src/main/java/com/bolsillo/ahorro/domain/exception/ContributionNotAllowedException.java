package com.bolsillo.ahorro.domain.exception;

public class ContributionNotAllowedException extends DomainException {

    public static final String AMOUNT_NOT_POSITIVE = "AMOUNT_NOT_POSITIVE";
    public static final String GOAL_ALREADY_COMPLETED = "GOAL_ALREADY_COMPLETED";
    public static final String CONTRIBUTION_EXCEEDS_REMAINING = "CONTRIBUTION_EXCEEDS_REMAINING";

    public ContributionNotAllowedException(String code, String message) {
        super(code, message);
    }

    public static ContributionNotAllowedException amountNotPositive() {
        return new ContributionNotAllowedException(
                AMOUNT_NOT_POSITIVE, "Contribution amount must be greater than zero");
    }

    public static ContributionNotAllowedException goalAlreadyCompleted() {
        return new ContributionNotAllowedException(
                GOAL_ALREADY_COMPLETED, "Cannot contribute to a completed goal");
    }

    public static ContributionNotAllowedException exceedsRemaining() {
        return new ContributionNotAllowedException(
                CONTRIBUTION_EXCEEDS_REMAINING, "Contribution exceeds remaining amount");
    }
}
