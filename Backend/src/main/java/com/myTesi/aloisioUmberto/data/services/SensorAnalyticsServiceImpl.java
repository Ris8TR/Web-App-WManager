package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorAnalyticsService;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SensorAnalyticsServiceImpl implements SensorAnalyticsService {

    private final SensorDataService sensorDataService;


    // ========================================================================
    //  STAGE 1: DATA CLEANING & IMPUTATION (Interpolazione)
    // ========================================================================

    /**
     * Algorithm: Linear Interpolation
     * Purpose: Estimates a missing value between two known data points.
     * Use Case: Filling gaps in time-series data caused by sensor downtime.
     *
     * @param pointA The previous known data point.
     * @param pointB The next known data point.
     * @param targetTimestamp The timestamp for which we want to estimate the value.
     * @param key The payload key to interpolate.
     * @return The interpolated value.
     */
    public double interpolateMissingValue(SensorData pointA, SensorData pointB, long targetTimestamp, String key) {
        double y1 = getNumericValue(pointA, key);
        double y2 = getNumericValue(pointB, key);
        long x1 = pointA.getTimestamp().getTime();
        long x2 = pointB.getTimestamp().getTime();

        if (x2 == x1) return y1;

        // Linear interpolation formula: y = y1 + ((x - x1) * (y2 - y1) / (x2 - x1))
        return y1 + ((double) (targetTimestamp - x1) * (y2 - y1) / (x2 - x1));
    }

    // ========================================================================
    //  STAGE 2: SMOOTHING & ANOMALY DETECTION (Pulizia e Sicurezza)
    // ========================================================================

    /**
     * Algorithm: Simple Moving Average (SMA)
     * Purpose: Reduces high-frequency noise to reveal underlying trends.
     *
     * @param dataList List of sensor readings.
     * @param key The payload key to average.
     * @return The arithmetic mean.
     */
    public double calculateMovingAverage(List<SensorData> dataList, String key) {
        if (dataList == null || dataList.isEmpty()) return 0.0;

        return dataList.stream()
                .mapToDouble(data -> getNumericValue(data, key))
                .average()
                .orElse(0.0);
    }

    /**
     * Algorithm: Z-Score Anomaly Detection
     * Purpose: Identifies statistical outliers.
     *
     * @param dataList List of sensor readings.
     * @param key The payload key to analyze.
     * @param threshold The number of standard deviations (e.g., 3.0) to trigger an anomaly.
     * @return A list of data points flagged as anomalies.
     */
    public List<SensorData> detectAnomalies(List<SensorData> dataList, String key, double threshold) {
        if (dataList == null || dataList.size() < 2) return Collections.emptyList();

        double mean = calculateMovingAverage(dataList, key);

        // Calculate Variance
        double variance = dataList.stream()
                .mapToDouble(data -> Math.pow(getNumericValue(data, key) - mean, 2))
                .sum() / dataList.size();

        double stdDev = Math.sqrt(variance);

        return dataList.stream()
                .filter(data -> {
                    if (stdDev == 0) return false;
                    double zScore = Math.abs((getNumericValue(data, key) - mean) / stdDev);
                    return zScore > threshold;
                })
                .collect(Collectors.toList());
    }

    // ========================================================================
    //  STAGE 3: DYNAMICS & PREDICTION (Analisi Avanzata)
    // ========================================================================

    /**
     * Algorithm: Rate of Change (First-order Derivative)
     * Purpose: Measures the velocity of change.
     * Use Case: Detecting rapid increases (e.g., sudden temperature spikes).
     *
     * @param current The most recent data point.
     * @param previous The preceding data point.
     * @param key The payload key to analyze.
     * @return Change per second.
     */
    public double calculateRateOfChange(SensorData current, SensorData previous, String key) {
        double valCurrent = getNumericValue(current, key);
        double valPrevious = getNumericValue(previous, key);

        // Time difference in seconds
        long timeDiffSeconds = Math.abs(current.getTimestamp().getTime() - previous.getTimestamp().getTime()) / 1000;

        if (timeDiffSeconds == 0) return 0.0;

        // Delta Value / Delta Time
        return (valCurrent - valPrevious) / timeDiffSeconds;
    }

    /**
     * Algorithm: Simple Linear Regression
     * Purpose: Predicts the next value based on historical trend.
     *
     * @param dataList Historical data points (must be sorted by time).
     * @param key The payload key to predict.
     * @return The predicted value for the next time step.
     */
    public double predictNextValue(List<SensorData> dataList, String key) {
        if (dataList == null || dataList.size() < 2) return 0.0;

        int n = dataList.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i; // x is treated as the discrete time index
            double y = getNumericValue(dataList.get(i), key);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        // Calculating slope (m) and intercept (b) for the line y = mx + b
        double denominator = (n * sumX2 - sumX * sumX);
        if (denominator == 0) return 0.0;

        double m = (n * sumXY - sumX * sumY) / denominator;
        double b = (sumY - m * sumX) / n;

        // Predict for the next index (n)
        return m * n + b;
    }

    // ========================================================================
    //  PRIVATE HELPERS
    // ========================================================================

    /**
     * Safely extracts a double value from the sensor data payload.
     */
    private double getNumericValue(SensorData data, String key) {
        if (data.getPayload() == null) return 0.0;
        Object val = data.getPayload().get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return 0.0;
    }
}