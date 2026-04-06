package com.ozandemiralp.orbit_tracker.service;

import com.ozandemiralp.orbit_tracker.dto.SatelliteDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TleParserService {

    public Map<String, SatelliteDTO> parseTleToMap(String rawTle) {
        Map<String, SatelliteDTO> satelliteMap = new HashMap<>();
        String[] lines = rawTle.split("\\r?\\n");

        for (int i = 0; i < lines.length; i += 3) {
            if (i + 2 < lines.length) {
                String name = lines[i].trim();
                String line1 = lines[i+1];
                String line2 = lines[i+2];

                satelliteMap.put(name.toUpperCase(), new SatelliteDTO(name, line1, line2));
            }
        }
        return satelliteMap;
    }
}