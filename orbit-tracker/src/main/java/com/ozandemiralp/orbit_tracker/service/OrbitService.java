package com.ozandemiralp.orbit_tracker.service;

import com.ozandemiralp.orbit_tracker.dto.SatelliteCurrentPositionRequestDTO;
import com.ozandemiralp.orbit_tracker.dto.SatelliteCurrentPositionResponseDTO;
import com.ozandemiralp.orbit_tracker.dto.SatelliteDTO;
import lombok.AllArgsConstructor;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.frames.Frame;
import org.orekit.frames.StaticTransform;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OrbitService {

    private final TleCacheService tleCacheService;

    private final Frame itrf;
    private final Frame teme;
    private final BodyShape earth;

    public Mono<SatelliteCurrentPositionResponseDTO> getCurrentSatellitePosition(SatelliteCurrentPositionRequestDTO request) {
        return tleCacheService.getSatelliteMap(request.satelliteGroup())
                .publishOn(Schedulers.boundedElastic())
                .map(satelliteMap -> {
                    SatelliteDTO target = findInMap(satelliteMap, request.satelliteName());
                    TLEPropagator propagator = TLEPropagator.selectExtrapolator(new TLE(target.tleLine1(), target.tleLine2()));

                    AbsoluteDate currentDate = new AbsoluteDate(new Date(), TimeScalesFactory.getUTC());
                    return calculatePositionAtDate(propagator, currentDate);
                });    }

    public Mono<List<SatelliteCurrentPositionResponseDTO>> getTrajectory(SatelliteCurrentPositionRequestDTO request) {
        return tleCacheService.getSatelliteMap(request.satelliteGroup())
                .publishOn(Schedulers.boundedElastic())
                .map(satelliteMap -> {
                    SatelliteDTO target = findInMap(satelliteMap, request.satelliteName());
                    TLEPropagator propagator = TLEPropagator.selectExtrapolator(new TLE(target.tleLine1(), target.tleLine2()));

                    double period = propagator.getInitialState().getOrbit().getKeplerianPeriod();
                    double duration = Math.min(period, 86400.0);
                    int steps = 100;
                    double stepSize = duration / steps;

                    List<SatelliteCurrentPositionResponseDTO> trajectory = new ArrayList<>();
                    AbsoluteDate startDate = new AbsoluteDate(new Date(), TimeScalesFactory.getUTC());

                    for (int i = 0; i <= steps; i++) {
                        AbsoluteDate targetDate = startDate.shiftedBy(i * stepSize);
                        trajectory.add(calculatePositionAtDate(propagator, targetDate));
                    }
                    return trajectory;
                });
    }

    private SatelliteDTO findInMap(Map<String, SatelliteDTO> map, String name) {
        SatelliteDTO found = map.get(name.toUpperCase());
        if (found == null) throw new RuntimeException("Satellite not found: " + name);
        return found;
    }

    private SatelliteCurrentPositionResponseDTO calculatePositionAtDate(TLEPropagator propagator, AbsoluteDate date) {
        SpacecraftState currentState = propagator.propagate(date);

        StaticTransform transform = teme.getStaticTransformTo(itrf, date);
        Vector3D positionAtItrf = transform.transformPosition(currentState.getPVCoordinates().getPosition());
        GeodeticPoint point = earth.transform(positionAtItrf, itrf, date);

        return new SatelliteCurrentPositionResponseDTO(
                currentState.getDate().toDate(TimeScalesFactory.getUTC()).toInstant(),
                FastMath.toDegrees(point.getLatitude()),
                FastMath.toDegrees(point.getLongitude()),
                point.getAltitude(),
                currentState.getPVCoordinates().getVelocity().getNorm() / 1000.0
        );
    }
}