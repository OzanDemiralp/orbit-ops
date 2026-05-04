# orbit-tracker

Reactive backend service for satellite position and trajectory calculations.

The service fetches TLE data from CelesTrak, parses and caches it, then uses Orekit for orbital propagation and Earth frame transformations.

## Tech Stack

- Java 21
- Spring Boot 4 (WebFlux)
- Project Reactor
- Orekit
- Lombok

## Features

- Reactive API endpoints with Spring WebFlux.
- TLE retrieval from CelesTrak.
- In-memory TLE cache with 90-minute TTL.
- Position calculation in geodetic coordinates (lat/lon/alt).
- Trajectory generation over one orbital period (capped at 24h).

## Technical Notes

- Reference frame transformation: TEME -> ITRF.
- Earth model is configured as singleton Spring beans.
- Cache uses `ConcurrentHashMap` + Reactor `Mono.cache(Duration)`.

## Prerequisites

- JDK 21 installed and active in your terminal.

Check Java version:

```bash
java -version
```

## Run Locally

From the nested project directory:

```bash
cd orbit-tracker
```

### Windows

```bash
mvnw.cmd spring-boot:run
```

### macOS/Linux

```bash
./mvnw spring-boot:run
```

App default URL:

```text
http://localhost:8080
```

OpenAPI UI:

```text
http://localhost:8080/swagger-ui.html
```

## Tests

### Windows

```bash
.\mvnw.cmd test
```

### macOS/Linux

```bash
./mvnw test
```

## API

Base path:

```text
/api/v1/satellites
```

### Get Current Position

```text
POST /api/v1/satellites/satellitePosition
```

Request body:

```json
{
  "satelliteGroup": "active",
  "satelliteName": "ISS (ZARYA)"
}
```

Response fields:

- `timestamp`
- `latitude`
- `longitude`
- `altitude`
- `velocity`

### Get Trajectory

```text
POST /api/v1/satellites/trajectory
```

Uses the same request body and returns a list of sampled position points.

## Current Status

- Done: TLE client + parser + cache.
- Done: Position and trajectory calculations.
- Done: Reactive processing model.
- In progress: Unit and integration tests.
