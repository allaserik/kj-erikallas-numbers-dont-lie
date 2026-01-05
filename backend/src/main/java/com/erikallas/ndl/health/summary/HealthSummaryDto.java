package com.erikallas.ndl.health.summary;

public record HealthSummaryDto(
        int heightCm,
        double latestWeightKg,
        double bmi,
        Double weightDelta7d) {
}
