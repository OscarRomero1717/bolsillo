package com.bolsillo.ahorro.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.Money;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JpaGoalRepositoryAdapterTest {

    @Autowired
    private GoalRepository goals;

    @Test
    void saveThenFindAllIncludesTheGoal() {
        Goal created = Goal.create("Meta adapter", Money.of("10"));

        goals.save(created);

        assertThat(goals.findById(created.id())).isPresent();
        assertThat(goals.findAll().stream().map(goal -> goal.id().toString()))
                .contains(created.id().toString());
    }
}
