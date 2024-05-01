package com.myTesi.aloisioUmberto.config.interceptor;


import com.myTesi.aloisioUmberto.exception.ManyRequestException;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.github.bucket4j.Bucket;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final BucketService bucketService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String apiKey = request.getHeader("X-api-key");

        if (apiKey == null || apiKey.isEmpty()) {
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            apiKey = "NORMAL-" + ipAddress;
        }

        Bucket tokenBucket = bucketService.resolveBucket(apiKey);
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));

            response.setStatus(429); //TODO Gestire l'errore in angular
            return false; // Per interrompere il flusso delle richieste
        }
    }
}
