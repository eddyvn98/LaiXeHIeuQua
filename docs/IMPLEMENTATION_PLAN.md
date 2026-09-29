# Implementation Plan

**Status:** v0.1 implementation complete; simulation/replay validation harness in PR; physical-device validation follows  
**Target:** Android 13+ APK, offline-first  
**Principle:** Prefer stable libraries; custom code only for product-specific logic.

## Library-first stack

- Android Gradle Plugin 9.4.0 / Gradle 9.6 / JDK 17
- Jetpack Compose BOM 2026.09.00 + Material 3
- Activity Compose 1.13.0
- Lifecycle 2.11.0
- Room 2.8.5 + KSP
- Google Play Services Location 21.4.0
- CameraX 1.6.2
- ML Kit Text Recognition 16.0.1
- Vico 3.2.3 for charts
- Tabler icon Android pack 2.2.1
- Kotlin coroutines / Flow through AndroidX/Kotlin dependencies

## Milestones

### M0 — Foundation
- Gradle project
- Android manifest + permissions
- Compose app shell
- Room database
- CI build

### M1 — Tracking
- Fused Location Provider
- foreground tracking service
- accelerometer / gyroscope / rotation vector
- session persistence
- screen-off tracking

### M2 — Speed
- true/fused speed stream
- smoothing
- traffic detector
- Eco Target engine
- Vehicle-Matched Speed profile

### M3 — Fuel
- fuel entry flow
- Full → Full cycle builder
- odometer reconciliation
- confidence
- Best Economy Reference
- current-cycle projection

### M4 — Camera calibration
- CameraX preview + analyzer
- ML Kit OCR
- stable sample collector
- true speed + vehicle speed timestamp pairing
- monotonic piecewise calibration

### M5 — Dashboard / Template Garage
- TFT Sport Bike template
- Premium Segmented template
- template persistence
- switch / favorite / duplicate
- adaptive ghost/economy chart
- Eco Target marker

### M6 — Quality
- unit tests for core engines
- CI assembleDebug + unit tests
- README and operational notes
- known-limitations list

### M7 — Simulation / replay
- extract a shared DrivePipeline used by live and simulated inputs
- deterministic scenario library
- accelerated long-ride soak scenario
- in-app Simulation Lab
- replay latest completed real ride
- simulator-specific unit tests
- no synthetic persistence into real ride/fuel data

## Validation sequence

1. Unit tests and APK build in CI.
2. Run deterministic Simulation Lab scenarios.
3. Run the two-hour scenario at accelerated playback.
4. Install on the target phone.
5. Record a short controlled real ride.
6. Replay that ride repeatedly while tuning thresholds.
7. Run the full two-hour physical battery/thermal/endurance test.

## Definition of done for the next pass

Simulation/replay is considered complete when PR CI is green and the branch merges to `main`. Hardware behavior (GPS, sensors, OCR accuracy, screen-off endurance, battery/thermal) remains physical-device validation and must not be inferred from simulation alone.
