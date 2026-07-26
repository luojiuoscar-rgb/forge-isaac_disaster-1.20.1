# Progress: Directional Isaac Flight

## 2026-07-26
- Read the approved plan and relevant skills.
- Confirmed the existing flight, capability, event, networking, key, and config ownership boundaries.
- Created a scoped feature plan so the project-wide memory files remain untouched.
- Added a JUnit 5 test setup and red tests for the shared flight velocity rules.
- Implemented flight capability, shared velocity rules, packets, server lifecycle, client input, V toggle, forced pose, and legacy mayfly migration.
- Diagnosed the movement bug: the custom velocity was still processed by ordinary player gravity, while gravity compensation was combined with the speed cap and could not guarantee both forward motion and lift.
- Added transient no-gravity ownership for the custom thrust state. The controller only releases no-gravity when it acquired it, and Transcendence remains an unlimited source without consuming `FLY_TIME`.
- Removed the empty legacy flight callbacks from `TranscendenceEffect` and replaced remaining fully qualified controller calls with imports.
- `gradlew compileJava test` passes; `git diff --check` and static legacy-entry-point checks pass.
- Raised the default flight speed multiplier and config-screen reset value from `1.0` to `2.0`.
- Restored the old mayfly-derived fall immunity explicitly through a highest-priority `LivingFallEvent` handler for every Isaac flight source, independent of controller state.
- Reduced the public flight configuration to speed multiplier and absolute cap, raised the default cap to `1.0`, and added the missing Chinese config labels.
- Removed the obsolete gravity-compensation pipeline, unused flight-source overload, and repeated client capability lookup; steering remains an internal `0.2` controller constant.
