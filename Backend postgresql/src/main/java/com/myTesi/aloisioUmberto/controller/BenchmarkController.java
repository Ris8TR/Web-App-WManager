package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.services.benchmark.BenchmarkService;
import com.myTesi.aloisioUmberto.dto.BenchmarkReport;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/V1/benchmark")
@RequiredArgsConstructor
@Tag(name = "Benchmark")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    @GetMapping("/compare-average")
    public ResponseEntity<BenchmarkReport> compareAverage(
            @RequestParam String sensorId,
            @RequestParam String key,
            @RequestParam int minutesAgo) {

        return ResponseEntity.ok(benchmarkService.compareAverage(sensorId, key, minutesAgo));
    }

    @GetMapping("/simple-average")
    public ResponseEntity<Double> simpleAverage(
            @RequestParam String sensorId) {

        return ResponseEntity.ok(benchmarkService.getAverageCO2FromDb(sensorId));
    }
}