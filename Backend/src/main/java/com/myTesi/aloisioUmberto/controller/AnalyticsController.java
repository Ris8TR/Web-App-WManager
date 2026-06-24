package com.myTesi.aloisioUmberto.controller;


import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.entities.SensorData;

import com.myTesi.aloisioUmberto.data.services.interfaces.SensorAnalyticsService;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "Analytic") //Name displayed on swagger
public class AnalyticsController {

    private final SensorAnalyticsService analyticsService;
    private final SensorDataService sensorDataService;
    private final JwtTokenProvider jwtTokenProvider;


    /**
     * Endpoint per ottenere i dati "allisciati" publici.
     */
    @GetMapping("/sensor/public/{sensorId}/trend")
    public ResponseEntity<List<SensorData>> getPublicSensorTrend(@PathVariable String sensorId,@RequestParam String key) {
        List<SensorData> rawData = sensorDataService.getRawDataForPublicSensor(sensorId,  600000);
        return ResponseEntity.ok(rawData);
    }

    /**
     * Endpoint per la predizione del prossimo valore.
     */
    @GetMapping("/sensor/public/{sensorId}/predict")
    public ResponseEntity<Map<String, Object>> getPublicPrediction(@PathVariable String sensorId,@RequestParam String key) {
        List<SensorData> rawData = sensorDataService.getRawDataForPublicSensor(sensorId, 600000);
        double predictedValue = analyticsService.predictNextValue(rawData, key);

        Map<String, Object> response = new HashMap<>();
        response.put("predictedValue", predictedValue);
        response.put("sensorId", sensorId);
        response.put("key", key);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint per ottenere i dati "allisciati" (smoothed) per il grafico.
     */
    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensor/{sensorId}/trend")
    public ResponseEntity<List<SensorData>> getSensorTrend(HttpServletRequest request , @PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        List<SensorData> rawData = sensorDataService.getRawDataForSensor(sensorId, token, 600000);
        return ResponseEntity.ok(rawData);
    }

    /**
     * Endpoint per identificare le anomalie.
     */
    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensor/{sensorId}/anomalies")
    public ResponseEntity<List<SensorData>> getAnomalies(HttpServletRequest request ,@PathVariable String sensorId,@RequestParam String key,@RequestParam(defaultValue = "3.0") double threshold) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        List<SensorData> rawData = sensorDataService.getRawDataForSensor(sensorId, token, 120);
        List<SensorData> anomalies = analyticsService.detectAnomalies(rawData, key, threshold);

        return ResponseEntity.ok(anomalies);
    }

    /**
     * Endpoint per la predizione del prossimo valore.
     */
    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensor/{sensorId}/predict")
    public ResponseEntity<Map<String, Object>> getPrediction(HttpServletRequest request, @PathVariable String sensorId,@RequestParam String key) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        List<SensorData> rawData = sensorDataService.getRawDataForSensor(sensorId, token, 600000);
        double predictedValue = analyticsService.predictNextValue(rawData, key);

        Map<String, Object> response = new HashMap<>();
        response.put("predictedValue", predictedValue);
        response.put("sensorId", sensorId);
        response.put("key", key);

        return ResponseEntity.ok(response);
    }
}