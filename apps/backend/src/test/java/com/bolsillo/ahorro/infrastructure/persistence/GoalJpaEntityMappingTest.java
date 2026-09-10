package com.bolsillo.ahorro.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.bolsillo.ahorro.domain.model.Goal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class GoalJpaEntityMappingTest {

    @Test
    void mapsGoalsTableAndSnakeCaseColumns() throws NoSuchFieldException {
        Table table = GoalJpaEntity.class.getAnnotation(Table.class);
        assertThat(GoalJpaEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(table).isNotNull();
        assertThat(table.name()).isEqualTo("goals");
        assertThat(columnName("targetAmount")).isEqualTo("target_amount");
        assertThat(columnName("currentAmount")).isEqualTo("current_amount");
    }

    @Test
    void domainGoalIsNotAJpaEntity() {
        assertThat(Goal.class.getAnnotation(Entity.class)).isNull();
    }

    private static String columnName(String fieldName) throws NoSuchFieldException {
        Field field = GoalJpaEntity.class.getDeclaredField(fieldName);
        return field.getAnnotation(Column.class).name();
    }
}
