# Lái Xe Hiệu Quả

Android app that helps a rider improve fuel economy over successive refueling cycles.

## Product goal

The primary KPI is simple: **make each fuel cycle travel farther than the user's previous best economy cycle**.

The app combines:
- GPS/Fused Location speed
- motion sensors
- traffic-state detection
- Eco Target Speed
- Full-to-Full fuel tracking
- Best Economy Reference
- camera-based synchronization with the real motorcycle speedometer
- switchable sport-dashboard templates

See:
- `docs/PRODUCT_SPEC_v0.1.md`
- `docs/IMPLEMENTATION_PLAN.md`
- `docs/PROGRESS.md`


## Build

Requirements:
- JDK 17
- Android SDK API 37 / Build Tools 36.0.0

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Main flows

- **Drive:** realtime Fused/GPS speed, Eco Target, traffic state, lean/acceleration, economy comparison.
- **Fuel:** Full-to-Full entries, partial fills, odometer reconciliation, Best Economy Reference.
- **Garage:** built-in and cloned dashboard templates.
- **Sync:** CameraX + ML Kit reads the real speedometer and automatically builds a vehicle-speed calibration profile.

## Validation

See `docs/VALIDATION.md` and `docs/KNOWN_LIMITATIONS.md`.
