package com.ozandemiralp.orbit_tracker.service;

import com.ozandemiralp.orbit_tracker.config.OrekitConfig;
import com.ozandemiralp.orbit_tracker.dto.SatelliteCurrentPositionResponseDTO;
import org.junit.jupiter.api.Test;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = {OrekitConfig.class, OrbitService.class})
class OrbitServiceTest {

    @Autowired
    private OrbitService orbitService;

    @MockitoBean
    private TleCacheService tleCacheService;

    @Test
    void testCalculatePositionAtDateWithFixedBaseline() throws Exception {
        String line1 = "1 25544U 98067A   23275.59444444  .00010903  00000-0  19196-3 0  9997";
        String line2 = "2 25544  51.6416 350.3957 0004529 175.7602 249.2078 15.49842407418465";
        TLE lockedTle = new TLE(line1, line2);
        TLEPropagator propagator = TLEPropagator.selectExtrapolator(lockedTle);

        AbsoluteDate fixedTestDate = lockedTle.getDate();

        Method calculateMethod = OrbitService.class.getDeclaredMethod("calculatePositionAtDate", TLEPropagator.class, AbsoluteDate.class);
        calculateMethod.setAccessible(true);

        SatelliteCurrentPositionResponseDTO result = (SatelliteCurrentPositionResponseDTO) calculateMethod.invoke(orbitService, propagator, fixedTestDate);


        assertEquals(45.39827929168997, result.latitude(), 0.00001);
        assertEquals(178.2923696917004, result.longitude(), 0.00001);
        assertEquals(422882.1405532432, result.altitude(), 0.1);
        assertEquals(7.662743040335272, result.velocity(), 0.0001);
    }
}
