package com.bolsillo.ahorro.interfaces.rest;

import com.bolsillo.ahorro.application.CreateGoalUseCase;
import com.bolsillo.ahorro.application.DepositContributionUseCase;
import com.bolsillo.ahorro.application.GetGoalUseCase;
import com.bolsillo.ahorro.application.ListGoalsUseCase;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.Money;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final CreateGoalUseCase createGoal;
    private final ListGoalsUseCase listGoals;
    private final GetGoalUseCase getGoal;
    private final DepositContributionUseCase depositContribution;

    public GoalController(
            CreateGoalUseCase createGoal,
            ListGoalsUseCase listGoals,
            GetGoalUseCase getGoal,
            DepositContributionUseCase depositContribution) {
        this.createGoal = createGoal;
        this.listGoals = listGoals;
        this.getGoal = getGoal;
        this.depositContribution = depositContribution;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GoalResponse.from(createGoal.execute(request.name(), new Money(request.targetAmount()))));
    }

    @GetMapping
    public List<GoalResponse> list() {
        return listGoals.execute().stream().map(GoalResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GoalResponse get(@PathVariable String id) {
        return GoalResponse.from(getGoal.execute(GoalId.from(id)));
    }

    @PostMapping("/{id}/contributions")
    public GoalResponse contribute(
            @PathVariable String id, @Valid @RequestBody ContributionRequest request) {
        return GoalResponse.from(
                depositContribution.execute(GoalId.from(id), new Money(request.amount())));
    }
}
