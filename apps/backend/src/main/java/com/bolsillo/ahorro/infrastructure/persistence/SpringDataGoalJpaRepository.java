package com.bolsillo.ahorro.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataGoalJpaRepository extends JpaRepository<GoalJpaEntity, String> {}
