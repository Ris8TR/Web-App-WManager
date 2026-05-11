package com.myTesi.aloisioUmberto.data.services.benchmark;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.BenchmarkReport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Date;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class BenchmarkService {

    private final SensorDataService mongoService;
    private final MongoTemplate mongoTemplate;




    // Classe per trasportare i dati del benchmark
    public class AggregationMetrics {
        public Double averageValue;
        public Long count;
        public long executionTimeMillis;

        @Override
        public String toString() {
            return String.format("--- Benchmark Result ---\nAvg: %.2f\nDocs Found: %d\nTime: %d ms\n------------------------",
                    averageValue, count, executionTimeMillis);
        }
    }

    public AggregationMetrics getAverageCO2WithMetrics(String sensorId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 1. Filtra per il sensore
        Criteria criteria = Criteria.where("sensorId").is(sensorId);

        // 2. Aggrega
        // Usiamo la dot notation corretta per accedere al campo dentro la Map payload
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group()
                        .avg("payload.CO2").as("avgValue")
                        .count().as("totalCount")
        );

        // 3. Esegui - ATTENZIONE: Usiamo "SensorData" come definito nell'entità
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "SensorData", Map.class);
        Map<String, Object> resultMap = results.getUniqueMappedResult();

        stopWatch.stop();

        AggregationMetrics metrics = new AggregationMetrics();

        if (resultMap == null) {
            // Se resultMap è null, lo stage match non ha trovato nulla
            System.err.println("DEBUG: Nessun documento trovato per sensorId: " + sensorId);
            metrics.averageValue = 0.0;
            metrics.count = 0L;
        } else {
            // Se troviamo qualcosa, estraiamo i dati
            // Nota: MongoDB potrebbe restituire Long o Integer per il count, usiamo Number per sicurezza
            metrics.averageValue = resultMap.get("avgValue") != null ? ((Number) resultMap.get("avgValue")).doubleValue() : 0.0;
            metrics.count = resultMap.get("totalCount") != null ? ((Number) resultMap.get("totalCount")).longValue() : 0L;

            // Debug per vedere cosa sta effettivamente restituendo Mongo
            System.out.println("DEBUG: Risultato grezzo da Mongo: " + resultMap);
        }

        metrics.executionTimeMillis = stopWatch.getTotalTimeMillis();
        System.out.println(metrics.toString());

        return metrics;
    }

    public BenchmarkReport compareAverage(String sensorId, String key, int minutesAgo) {
        // Calcolo date
        java.util.Calendar cal = java.util.Calendar.getInstance();
        Date to = cal.getTime();
        cal.add(java.util.Calendar.MINUTE, -minutesAgo);
        Date from = cal.getTime();

        // --- TEST MONGODB ---
        long startMongo = System.currentTimeMillis();
        Double mongoVal = mongoService.getAverageValueFromDb(sensorId, key, from, to);
        long endMongo = System.currentTimeMillis();
        long mongoDuration = endMongo - startMongo;


        // --- COSTruzione del Report ---
        double speedup = (double)  mongoDuration;

        return BenchmarkReport.builder()
                .testName("Moving Average Comparison")
                .sensorId(sensorId)
                .key(key)
                .minutesAnalyzed(minutesAgo)
                .mongoResult(BenchmarkReport.ComparisonResult.builder()
                        .timeMs(mongoDuration).value(mongoVal).recordsProcessed(1).build())
                .speedupFactor(speedup)
                .winner("MongoDB")
                .build();
    }
}