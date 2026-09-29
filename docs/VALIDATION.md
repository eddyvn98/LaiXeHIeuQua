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

## Unit-test coverage in this checkpoint

- stop/go traffic detection
- clear-road non-traffic behavior
- piecewise speed calibration
- monotonic calibration rejection
- Full-to-Full fuel cycle including partial fills
- Eco Target suppression in traffic
- Eco Target use of Best Economy reference
- deceleration direction in speed fusion
- economy projection and no backwards projection

## Hardware acceptance

Hardware acceptance remains separate from compile/unit validation. See `KNOWN_LIMITATIONS.md`.
