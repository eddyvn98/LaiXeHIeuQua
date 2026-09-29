# Known Limitations — v0.1

This checkpoint is a functional Android implementation skeleton, not a claim that motorcycle hardware validation is complete.

## Requires physical-device validation

- 2-hour foreground tracking endurance and battery/thermal behavior.
- GPS quality in tunnels, dense urban streets and poor-sky conditions.
- Camera OCR accuracy across different motorcycle clusters, glare and vibration.
- Camera calibration target of about ±1.5 km/h inside the calibrated range.
- Lean-angle accuracy for each mounting orientation.
- Sensor behavior after the phone mount physically shifts.

## Current algorithm limits

- Linear acceleration is currently used as a magnitude; speed fusion infers sign from GPS movement. A later version can project acceleration onto a calibrated vehicle longitudinal axis.
- Traffic detection is speed-pattern based and intentionally route-independent; unusual low-speed riding can still be classified as stop/go.
- Eco Target is an explainable heuristic using Best Economy Reference + recent stable speed. It is not ECU fuel-flow measurement.
- Current-cycle projection is a behavioral estimate relative to Best Economy Reference. It is not a direct fuel-level measurement.
- OCR reads a center-weighted region of the whole camera frame. A user-adjustable ROI is a future improvement.
- Custom template editing currently supports duplicate/select/favorite foundations; a full visual editor and remote template repository are future work.
- No cloud account/sync, OBD, map matching or route-specific Best logic in v0.1.

## Safety

Camera calibration is intended to run hands-free once positioned. The rider should not type, aim, or otherwise interact with the phone while the motorcycle is moving.
