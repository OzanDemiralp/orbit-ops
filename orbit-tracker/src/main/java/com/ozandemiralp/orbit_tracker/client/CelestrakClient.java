package com.ozandemiralp.orbit_tracker.client;

import com.ozandemiralp.orbit_tracker.exception.TleServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
public class CelestrakClient {

    private final WebClient webClient;
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000;

    public CelestrakClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getTleDataByGroup(String group) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/NORAD/elements/gp.php")
                        .queryParam("GROUP", group)
                        .queryParam("FORMAT", "tle")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofMillis(INITIAL_BACKOFF_MS))
                        .maxBackoff(Duration.ofSeconds(10))
                        .doBeforeRetry(signal -> log.warn("Retrying Celestrak request for group: {} (attempt {})",
                                group, signal.totalRetries() + 1)))
                .onErrorMap(ex -> {
                    if (ex instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) ex;
                        return new TleServiceException("Failed to fetch TLE data for group: " + group +
                                " - HTTP " + wcre.getStatusCode(), ex);
                    }
                    return new TleServiceException("Failed to fetch TLE data for group: " + group +
                            " - Connection error", ex);
                });
    }
}
