package com.bolsillo.ahorro.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateGoalRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal targetAmount) {}
