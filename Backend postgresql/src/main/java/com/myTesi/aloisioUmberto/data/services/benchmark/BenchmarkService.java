package com.myTesi.aloisioUmberto.data.services.benchmark;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.BenchmarkReport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@RequiredArgsConstructor
public class BenchmarkService {



    private final SensorDataService postgresService;

    private final PostgresSensorDataRepository postgresRepository;


    public Double getAverageCO2FromDb(String sensorId) {
        // Chiama la query nativa del repository
        Double result = postgresRepository.findAverageCO2(sensorId);
        return (result != null) ? result : 0.0;
    }
    public Double getAverageValueFromDb(String sensorId, String key, Date from, Date to) {
        Double result = postgresRepository.findAverageFromJson(sensorId, key, from, to);
        return result != null ? result : 0.0;
    }

    public BenchmarkReport compareAverage(String sensorId, String key, int minutesAgo) {
        // Calcolo date
        java.util.Calendar cal = java.util.Calendar.getInstance();
        Date to = cal.getTime();
        cal.add(java.util.Calendar.MINUTE, -minutesAgo);
        Date from = cal.getTime();


        // --- TEST POSTGRES ---
        long startPostgres = System.currentTimeMillis();
        Double postgresVal = postgresService.getAverageValueFromDb(sensorId, key, from, to);
        long endPostgres = System.currentTimeMillis();
        long postgresDuration = endPostgres - startPostgres;

        // --- COSTruzione del Report ---
        double speedup = (double) postgresDuration; // se mongo è più veloce

        return BenchmarkReport.builder()
                .testName("Moving Average Comparison")
                .sensorId(sensorId)
                .key(key)
                .minutesAnalyzed(minutesAgo)
                .postgresResult(BenchmarkReport.ComparisonResult.builder()
                        .timeMs(postgresDuration).value(postgresVal).recordsProcessed(1).build())
                .speedupFactor(speedup)
                .winner("PostgreSQL")
                .build();
    }
}