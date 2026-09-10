package com.bolsillo.ahorro.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bolsillo.ahorro.domain.event.DomainEvent;
import com.bolsillo.ahorro.domain.event.GoalCompletedEvent;
import com.bolsillo.ahorro.domain.event.GoalUpdatedEvent;
import com.bolsillo.ahorro.domain.exception.ContributionNotAllowedException;
import java.util.List;
import org.junit.jupiter.api.Test;

class GoalContributeTest {

    @Test
    void validContributionUpdatesCurrentAndStaysOpen() {
        Goal goal = Goal.create("Viaje", Money.of("100"));

        List<DomainEvent> events = goal.contribute(Money.of("40"));

        assertThat(goal.currentAmount().amount()).isEqualByComparingTo("40.00");
        assertThat(goal.status()).isEqualTo(GoalStatus.OPEN);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(GoalUpdatedEvent.class);
    }

    @Test
    void rejectsZeroAmount() {
        Goal goal = Goal.create("Viaje", Money.of("100"));

        assertThatThrownBy(() -> goal.contribute(Money.of("0")))
                .isInstanceOf(ContributionNotAllowedException.class)
                .extracting(ex -> ((ContributionNotAllowedException) ex).code())
                .isEqualTo(ContributionNotAllowedException.AMOUNT_NOT_POSITIVE);
        assertThat(goal.currentAmount().amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsNegativeAmount() {
        Goal goal = Goal.create("Viaje", Money.of("100"));

        assertThatThrownBy(() -> goal.contribute(Money.of("-1")))
                .isInstanceOf(ContributionNotAllowedException.class)
                .extracting(ex -> ((ContributionNotAllowedException) ex).code())
                .isEqualTo(ContributionNotAllowedException.AMOUNT_NOT_POSITIVE);
    }

    @Test
    void rejectsAmountExceedingRemaining() {
        Goal goal = Goal.create("Viaje", Money.of("100"));
        goal.contribute(Money.of("80"));

        assertThatThrownBy(() -> goal.contribute(Money.of("30")))
                .isInstanceOf(ContributionNotAllowedException.class)
                .extracting(ex -> ((ContributionNotAllowedException) ex).code())
                .isEqualTo(ContributionNotAllowedException.CONTRIBUTION_EXCEEDS_REMAINING);
        assertThat(goal.currentAmount().amount()).isEqualByComparingTo("80.00");
        assertThat(goal.status()).isEqualTo(GoalStatus.OPEN);
    }

    @Test
    void rejectsContributionOnCompletedGoal() {
        Goal goal = Goal.create("Viaje", Money.of("100"));
        goal.contribute(Money.of("100"));

        assertThatThrownBy(() -> goal.contribute(Money.of("1")))
                .isInstanceOf(ContributionNotAllowedException.class)
                .extracting(ex -> ((ContributionNotAllowedException) ex).code())
                .isEqualTo(ContributionNotAllowedException.GOAL_ALREADY_COMPLETED);
    }

    @Test
    void exactTargetCompletesGoalAndPublishesBothEvents() {
        Goal goal = Goal.create("Viaje", Money.of("100"));

        List<DomainEvent> events = goal.contribute(Money.of("100"));

        assertThat(goal.status()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(goal.currentAmount()).isEqualTo(goal.targetAmount());
        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isInstanceOf(GoalUpdatedEvent.class);
        assertThat(events.get(1)).isInstanceOf(GoalCompletedEvent.class);
        GoalUpdatedEvent updated = (GoalUpdatedEvent) events.get(0);
        assertThat(updated.status()).isEqualTo(GoalStatus.COMPLETED);
    }
}
