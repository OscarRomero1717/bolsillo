package com.bolsillo.ahorro.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.bolsillo.ahorro.application.CreateGoalUseCase;
import com.bolsillo.ahorro.application.DepositContributionUseCase;
import com.bolsillo.ahorro.application.fake.InMemoryGoalRepository;
import com.bolsillo.ahorro.domain.event.GoalUpdatedEvent;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.GoalStatus;
import com.bolsillo.ahorro.domain.model.Money;
import com.bolsillo.ahorro.domain.port.GoalEventPublisher;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

class SpringGoalEventPublisherTest {

    @Test
    void depositWithSpringPublisherDoesNotRequireListeners() {
        InMemoryGoalRepository repository = new InMemoryGoalRepository();
        List<Object> published = new ArrayList<>();
        ApplicationEventPublisher springPublisher = published::add;
        DepositContributionUseCase useCase =
                new DepositContributionUseCase(repository, new SpringGoalEventPublisher(springPublisher));
        Goal created = new CreateGoalUseCase(repository).execute("Viaje", Money.of("100"));

        assertThatCode(() -> useCase.execute(created.id(), Money.of("40"))).doesNotThrowAnyException();
        assertThat(published).hasSize(1);
        assertThat(published.get(0)).isInstanceOf(GoalUpdatedEvent.class);
    }
}

@SpringBootTest
class SpringGoalEventPublisherContextTest {

    @Autowired
    private GoalEventPublisher publisher;

    @Test
    void beanIsRegisteredAndPublishWithoutListenersDoesNotFail() {
        assertThat(publisher).isInstanceOf(SpringGoalEventPublisher.class);
        GoalUpdatedEvent event =
                new GoalUpdatedEvent(GoalId.newId(), Money.of("0"), Money.of("100"), GoalStatus.OPEN);
        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
    }
}
