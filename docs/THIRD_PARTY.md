# Third-party libraries

The implementation intentionally prefers maintained libraries over hand-written infrastructure.

| Purpose | Library | Version |
|---|---|---|
| Android build | Android Gradle Plugin | 9.4.0 |
| UI | Jetpack Compose BOM / Material 3 | 2026.09.00 |
| Persistence | Room | 2.8.5 |
| Location | Google Play Services Location | 21.4.0 |
| Camera | CameraX | 1.6.2 |
| OCR | Google ML Kit Text Recognition | 16.0.1 |
| Charts | Vico | 3.2.3 |
| Icons | Composables Tabler Outline | 2.2.1 |
| KSP | Google KSP | 2.3.12 |
| Async | Kotlin Coroutines | 1.10.2 |

The Tabler icon collection is MIT licensed. Vico and AndroidX/Google libraries retain their upstream licenses.

Custom code is kept for product-specific logic only: Eco Target, traffic heuristics, fuel-cycle interpretation, calibration sample filtering, calibration mapping, sport gauges and the compact segmented-number renderer.
