package com.bolsillo.ahorro.interfaces.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ContributionRequest(@NotNull @Positive BigDecimal amount) {}
