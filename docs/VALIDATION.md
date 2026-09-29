# Validation

## Automated

GitHub Actions workflow: `.github/workflows/android.yml`

It runs:

```
./gradlew :app:testDebugUnitTest :app:assembleDebug --stacktrace
```

and uploads the debug APK when available.

The workflow supports:
- push to `main`
- pull requests
- manual `workflow_dispatch`

## Unit-test coverage

Core engine coverage:
- stop/go traffic detection
- clear-road non-traffic behavior
- piecewise speed calibration
- monotonic calibration rejection
- Full-to-Full fuel cycle including partial fills
- Eco Target suppression in traffic
- Eco Target use of Best Economy reference
- deceleration direction in speed fusion
- economy projection and no backwards projection

Simulation coverage:
- two-hour scenario duration and frame count
- GPS dropout / degraded accuracy behavior
- calibration OCR plateaus
- city traffic scenario reaches the shared TrafficDetector
- clear-road scenario remains outside traffic and accumulates distance

## Simulation before road testing

The app includes a Simulation Lab. Live tracking and simulation both feed the same `DrivePipeline`, which contains speed fusion, traffic detection, Eco Target logic, display calibration and distance/history state.

Synthetic scenarios do not insert fake track points or fuel entries into Room.

The latest completed real ride can be replayed from persisted `track_points`. This makes it possible to tune algorithm behavior repeatedly after a single real ride instead of reproducing the same road condition manually.

## Hardware acceptance

Simulation reduces algorithmic risk but does not replace hardware acceptance. Physical-device validation is still required for:
- real GPS/GNSS behavior
- camera vibration/glare and OCR accuracy
- phone-mount sensor orientation
- screen-off service behavior
- battery and thermal behavior over a two-hour ride

See `KNOWN_LIMITATIONS.md`.
