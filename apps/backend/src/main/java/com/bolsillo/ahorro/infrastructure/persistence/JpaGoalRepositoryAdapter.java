package com.bolsillo.ahorro.infrastructure.persistence;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaGoalRepositoryAdapter implements GoalRepository {

    private final SpringDataGoalJpaRepository springData;
    private final GoalMapper mapper;
    private final EntityManager entityManager;

    public JpaGoalRepositoryAdapter(
            SpringDataGoalJpaRepository springData, GoalMapper mapper, EntityManager entityManager) {
        this.springData = springData;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Goal save(Goal goal) {
        GoalJpaEntity entity = mapper.toEntity(goal);
        if (!springData.existsById(entity.getId())) {
            entity.setVersion(null);
        }
        GoalJpaEntity merged = entityManager.merge(entity);
        entityManager.flush();
        return mapper.toDomain(merged);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Goal> findById(GoalId id) {
        return springData.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Goal> findAll() {
        return springData.findAll().stream().map(mapper::toDomain).toList();
    }
}
