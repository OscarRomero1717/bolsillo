package com.bolsillo.ahorro.infrastructure.config;

import com.bolsillo.ahorro.application.CreateGoalUseCase;
import com.bolsillo.ahorro.application.DepositContributionUseCase;
import com.bolsillo.ahorro.application.GetGoalUseCase;
import com.bolsillo.ahorro.application.ListGoalsUseCase;
import com.bolsillo.ahorro.domain.port.GoalEventPublisher;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    CreateGoalUseCase createGoalUseCase(GoalRepository goals) {
        return new CreateGoalUseCase(goals);
    }

    @Bean
    ListGoalsUseCase listGoalsUseCase(GoalRepository goals) {
        return new ListGoalsUseCase(goals);
    }

    @Bean
    GetGoalUseCase getGoalUseCase(GoalRepository goals) {
        return new GetGoalUseCase(goals);
    }

    @Bean
    DepositContributionUseCase depositContributionUseCase(
            GoalRepository goals, GoalEventPublisher publisher) {
        return new DepositContributionUseCase(goals, publisher);
    }
}
