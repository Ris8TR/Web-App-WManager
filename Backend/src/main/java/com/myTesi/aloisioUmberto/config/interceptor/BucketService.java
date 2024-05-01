package com.myTesi.aloisioUmberto.config.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class BucketService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::newBucket);
    }

    private Bucket newBucket(String apiKey) {
        //TODO Verificare se poi il limite va alzato o meno
        Bandwidth bandwidth = Bandwidth.classic(60, Refill.intervally(10, Duration.ofSeconds(60))); //RATE LIMITER LIMIT

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
