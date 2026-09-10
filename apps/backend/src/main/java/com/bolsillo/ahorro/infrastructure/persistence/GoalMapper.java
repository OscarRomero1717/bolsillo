package com.bolsillo.ahorro.infrastructure.persistence;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.GoalName;
import com.bolsillo.ahorro.domain.model.GoalStatus;
import com.bolsillo.ahorro.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    public Goal toDomain(GoalJpaEntity entity) {
        return Goal.rehydrate(
                GoalId.from(entity.getId()),
                new GoalName(entity.getName()),
                new Money(entity.getTargetAmount()),
                new Money(entity.getCurrentAmount()),
                GoalStatus.valueOf(entity.getStatus()),
                entity.getVersion() == null ? 0L : entity.getVersion());
    }

    public GoalJpaEntity toEntity(Goal goal) {
        return new GoalJpaEntity(
                goal.id().toString(),
                goal.name().value(),
                goal.targetAmount().amount(),
                goal.currentAmount().amount(),
                goal.status().name(),
                goal.version());
    }
}
