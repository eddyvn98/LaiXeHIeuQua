# Progress

## 2026-09-29

### Checkpoint 1 — Product decisions + implementation plan
- [x] Replace route-based Best Session with Best Economy Reference
- [x] Define Eco Target Speed
- [x] Define traffic detection by speed pattern
- [x] Define fuel completion/confidence rules
- [x] Define camera+GPS automatic speed calibration
- [x] Define v0.1 acceptance criteria
- [x] Lock library-first implementation strategy
- [x] Android project scaffold
- [x] Tracking / sensors
- [x] Fuel / best economy
- [x] Camera calibration
- [x] Dashboard templates
- [x] Unit tests / CI

### Checkpoint 3 — UI, camera, tests, CI
- [x] TFT Sport dashboard
- [x] Premium Segmented template selection
- [x] Vico adaptive economy-gap chart
- [x] Tabler icon pack integration
- [x] CameraX + ML Kit speed OCR
- [x] automatic stable calibration sample collection
- [x] core engine unit tests
- [x] GitHub Actions build/test/APK artifact

### Checkpoint 4 — hardening / final validation branch
- [x] foreground-service restart/session recovery hardening
- [x] lean zero baseline calibration on sensor start
- [x] signed speed-fusion correction for deceleration
- [x] current fuel-cycle distance + projected economy range
- [x] centered OCR candidate scoring
- [x] Gradle wrapper 9.6.0
- [x] workflow_dispatch + wrapper-based CI
- [x] known limitations / third-party docs
- [x] PR CI green
- [x] merge final checkpoint to main

### Checkpoint 5 — simulation / replay harness
- [x] shared DrivePipeline for live and simulated inputs
- [x] Simulation Lab inside the app
- [x] city / clear-road / accel-brake scenarios
- [x] GPS noise and dropout scenario
- [x] lean/cornering scenario
- [x] calibration OCR plateau scenario
- [x] accelerated 2-hour mixed scenario
- [x] 1x / 5x / 20x / 100x playback
- [x] replay latest completed ride from Room track points
- [x] keep synthetic data out of real trip/fuel storage
- [x] unit tests for simulator and shared pipeline
- [x] PR CI green
- [x] merge simulation checkpoint to main

### Next — physical-device validation
- [ ] install debug APK on target Android phone
- [ ] verify foreground tracking with screen off
- [ ] validate GPS/fused speed on a real ride
- [ ] validate speedometer OCR and automatic calibration
- [ ] validate lean zeroing for the actual phone mount
- [ ] run a 2-hour battery/thermal endurance session
- [ ] replay recorded failures and tune thresholds from evidence
