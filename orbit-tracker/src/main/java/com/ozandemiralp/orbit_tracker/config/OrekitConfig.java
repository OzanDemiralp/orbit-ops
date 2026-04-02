package com.ozandemiralp.orbit_tracker.config;

import org.orekit.bodies.BodyShape;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DirectoryCrawler;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URL;

@Configuration
public class OrekitConfig {

    public OrekitConfig() {
        /* since this is in the constructor it is guaranteed to work before other beans */
        initializeOrekitData();
    }

    private void initializeOrekitData() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resource = loader.getResource("orekit-data-main");

        if (resource == null) {
            throw new IllegalStateException("Orekit data folder not found in resources!");
        }

        File orekitData = new File(resource.getFile());
        DataContext.getDefault().getDataProvidersManager().clearProviders();
        DataContext.getDefault()
                .getDataProvidersManager()
                .addProvider(new DirectoryCrawler(orekitData));
    }

    @Bean
    public Frame itrf() {
        return FramesFactory.getITRF(IERSConventions.IERS_2010, true);
    }

    @Bean
    public Frame teme() {
        return FramesFactory.getTEME();
    }

    @Bean
    public BodyShape earth(Frame itrf) {
        /* spring knows itrf bean should be ready and injects it here */
        return new OneAxisEllipsoid(
                Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING,
                itrf
        );
    }

}
