package com.myTesi.aloisioUmberto.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenchmarkReport {
    private String testName;
    private String sensorId;
    private String key;
    private long minutesAnalyzed;

    private ComparisonResult mongoResult;
    private ComparisonResult postgresResult;
    private double speedupFactor;
    private String winner;

    @Data
    @Builder
    public static class ComparisonResult {
        private long timeMs;
        private double value;
        private long recordsProcessed;
    }
}