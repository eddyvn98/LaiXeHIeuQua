# Road-test upgrade v0.2

This pass responds to physical-device dashboard feedback.

## Drive UI

- Increase daylight readability with brighter primary text, labels and gauge tracks.
- Keep the main speed display as the visual priority.
- Move sub-gauge numeric readouts lower and shorten needles so the needle does not cover the number.
- Add a realtime speed-history chart.
- Add continuously updated moving average speed.

## Trip and ODO

- TRIP is cumulative across separate rides and app restarts.
- Ending a ride does not reset TRIP.
- Long-press TRIP to reset it manually.
- ODO is entered manually in ODO setup.
- ODO no longer depends on camera/speedometer synchronization.
- After manual setup, ODO advances only from validated app-tracked distance.

## Speed and stationary handling

- Live display speed uses GPS + sensor fusion directly.
- Camera calibration is no longer applied to the live display.
- Location request target cadence is 500 ms, while UI animation remains independent.
- Repeated near-zero GPS samples force a zero-velocity correction.
- Distance deltas are rejected when both adjacent GPS samples indicate the vehicle is stopped.
- Implausible GPS jumps and low-quality location samples do not increment TRIP/ODO.

## Efficiency chart

The large chart is now based on movement behavior rather than fuel-fill boundaries.

Realtime movement efficiency considers:
- acceleration smoothness,
- deviation from Eco Target,
- traffic state.

Fuel-cycle data is still retained as long-term ground truth for actual km/L, but the Drive chart no longer waits for refueling events to provide feedback.

## Validation focus

1. Readability outdoors in daylight.
2. Speed response during throttle-on and throttle-off transitions.
3. No phantom speed at a complete stop.
4. No TRIP/ODO growth while stationary.
5. TRIP persistence across multiple rides.
6. Long-press TRIP reset.
7. ODO manual setup and subsequent validated increment.
8. Average speed behavior during moving vs stopped periods.
