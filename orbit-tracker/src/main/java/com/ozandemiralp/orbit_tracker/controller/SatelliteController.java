package com.ozandemiralp.orbit_tracker.controller;

import com.ozandemiralp.orbit_tracker.dto.SatelliteCurrentPositionRequestDTO;
import com.ozandemiralp.orbit_tracker.dto.SatelliteCurrentPositionResponseDTO;
import com.ozandemiralp.orbit_tracker.service.OrbitService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/satellites")
@AllArgsConstructor
public class SatelliteController {

    private final OrbitService orbitService;

        @PostMapping("/satellitePosition")
    public Mono<SatelliteCurrentPositionResponseDTO> getSatellitePosition(@RequestBody SatelliteCurrentPositionRequestDTO requestDTO){
        return orbitService.getCurrentSatellitePosition(requestDTO);
        }

        @PostMapping("/trajectory")
    public Mono<List<SatelliteCurrentPositionResponseDTO>> getTrajectory(@RequestBody SatelliteCurrentPositionRequestDTO requestDTO){
        return orbitService.getTrajectory(requestDTO);
        }

}
