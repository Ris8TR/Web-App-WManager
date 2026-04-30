package com.myTesi.aloisioUmberto.data.services.interfaces;


import com.myTesi.aloisioUmberto.data.entities.SensorData;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SensorAnalyticsService {
    double interpolateMissingValue(SensorData pointA, SensorData pointB, long targetTimestamp, String key);

    /**
     * Computes the Simple Moving Average (SMA) to smooth data fluctuations.
     */
    double calculateMovingAverage(List<SensorData> dataList, String key);

    /**
     * Identifies statistical outliers within a dataset based on a Z-Score threshold.
     */
    List<SensorData> detectAnomalies(List<SensorData> dataList, String key, double threshold);

    /**
     * Calculates the first-order derivative (rate of change) between two points.
     */
    double calculateRateOfChange(SensorData current, SensorData previous, String key);

    /**
     * Predicts the next value in a series using Simple Linear Regression.
     */
    double predictNextValue(List<SensorData> dataList, String key);
}
