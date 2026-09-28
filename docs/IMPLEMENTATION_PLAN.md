# Implementation Plan

**Status:** Implementation complete; PR build/device validation pending  
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

## Definition of done for this implementation pass

The repository contains a coherent Android project with all major v0.1 flows represented in code, core engines covered by unit tests, and CI configured to build/test. Hardware behavior (GPS, sensors, OCR accuracy, 2-hour endurance) remains device-validation work and is documented explicitly rather than falsely claimed as verified.
