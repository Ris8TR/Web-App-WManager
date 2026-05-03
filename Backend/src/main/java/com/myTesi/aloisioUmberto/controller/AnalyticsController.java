package com.myTesi.aloisioUmberto.controller;


import com.myTesi.aloisioUmberto.data.entities.SensorData;

import com.myTesi.aloisioUmberto.data.services.interfaces.SensorAnalyticsService;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/V1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "Analytic") //Name displayed on swagger
public class AnalyticsController {

    private final SensorAnalyticsService analyticsService;
    private final SensorDataService sensorDataService;

    /**
     * Endpoint per ottenere i dati "lisciati" (smoothed) per un grafico di trend.
     */
    @GetMapping("/sensor/{sensorId}/trend")
    public ResponseEntity<List<SensorData>> getSensorTrend(
            @PathVariable String sensorId,
            @RequestHeader("Authorization") String token,
            @RequestParam String key) {

        // 1. Recuperiamo i dati grezzi (es. ultimi 60 minuti)
        // Nota: dovresti implementare un metodo nel repository o nel service per prendere una lista
        List<SensorData> rawData = sensorDataService.getRawDataForSensor(sensorId, 60);

        // 2. Applichiamo la media mobile per il trend
        // Qui potresti restituire un oggetto custom che contiene sia i dati grezzi che quelli smoothed
        return ResponseEntity.ok(rawData); // Per semplicità restituiamo i dati, ma idealmente restituisci un DTO con il trend
    }


    @GetMapping("/sensor/public/{sensorId}/trend")
    public ResponseEntity<List<SensorData>> getPublicSensorTrend(
            @PathVariable String sensorId,
            @RequestParam String key) {

        List<SensorData> rawData = sensorDataService.getRawDataForPublicSensor(sensorId, 60);


        return ResponseEntity.ok(rawData);
    }

    /**
     * Endpoint per identificare le anomalie.
     */
    @GetMapping("/sensor/{sensorId}/anomalies")
    public ResponseEntity<List<SensorData>> getAnomalies(
            @PathVariable String sensorId,
            @RequestHeader("Authorization") String token,
            @RequestParam String key,
            @RequestParam(defaultValue = "3.0") double threshold) {

        List<SensorData> rawData = sensorDataService.getRawDataForSensor(sensorId, 120);
        List<SensorData> anomalies = analyticsService.detectAnomalies(rawData, key, threshold);

        return ResponseEntity.ok(anomalies);
    }

    /**
     * Endpoint per la predizione del prossimo valore.
     */
    @GetMapping("/sensor/{sensorId}/predict")
    public ResponseEntity<Map<String, Object>> getPrediction(
            @PathVariable String sensorId,
            @RequestHeader("Authorization") String token,
            @RequestParam String key) {

        List<SensorData> rawData = sensorDataService.getRawDataForSensor(sensorId, 60);
        double predictedValue = analyticsService.predictNextValue(rawData, key);

        Map<String, Object> response = new HashMap<>();
        response.put("predictedValue", predictedValue);
        response.put("sensorId", sensorId);
        response.put("key", key);

        return ResponseEntity.ok(response);
    }
}