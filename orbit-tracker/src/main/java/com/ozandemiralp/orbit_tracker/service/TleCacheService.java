package com.ozandemiralp.orbit_tracker.service;

import com.ozandemiralp.orbit_tracker.client.CelestrakClient;
import com.ozandemiralp.orbit_tracker.dto.SatelliteDTO;
import lombok.AllArgsConstructor;
import org.orekit.propagation.analytical.tle.TLE;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class TleCacheService {
    private final CelestrakClient celestrakClient;
    private final TleParserService tleParserService;

    private final Map<String, Mono<Map<String, TLE>>> internalCache = new ConcurrentHashMap<>();

    public Mono<Map<String, TLE>> getSatelliteMap(String group) {
        return internalCache.computeIfAbsent(group, g ->
                celestrakClient.getTleDataByGroup(g)
                        .subscribeOn(Schedulers.boundedElastic())
                        .map(tleParserService::parseTleToMap)
                        .cache(Duration.ofMinutes(90))
        );
    }
}
