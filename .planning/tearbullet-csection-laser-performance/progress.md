# Progress: C-Section + Laser Performance Diagnosis

## 2026-08-22
- Read the three user-requested skills and confirmed the project targets Forge 1.20.1.
- Inspected Git status. The attack pipeline and related trigger files are dirty user work and will be preserved.
- Began tracing the active CSectionAttack, FetusBullet, TearBullet, Laser, and TriggerModuleEvents path.
- Completed the trace and cost model. The primary cause is an N-way, same-phase, synchronous full-range `LaserAttack` every four ticks, not merely the extra FetusBullet renderer.
- Corrected the model after tracing the secondary attack prepare stage: `LaserPlusFetus` passes the mutable parent trigger object into a `BULLET_ONLY` request. Prepare reattaches Laser triggers to that same parent, doubling its future laser tick triggers once per burst. This is the confirmed primary defect and produces exponential burst growth.
- Implementation hypothesis: `AttackContext` promises isolation in its constructor comment but retains the caller's `CompositeTrigger`. Make both public constructors take `trigger.copy()` and lock that contract down with a focused regression test.
- Added `AttackContextIsolationTest` first, then changed both `AttackContext` constructors to retain a trigger snapshot. This prevents secondary Laser preparation from appending triggers to the parent FetusBullet.
- The first test execution reached JUnit but failed at Minecraft registry bootstrap because the test accessed `ModBulletColor`. Reworked it to use a plain `ResourceLocation`; temporarily restored the old alias only to observe the intended red assertion before restoring the fix.
- Red test confirmed the bug with `expected: not same but was: <CompositeTrigger: []>`. Restored the defensive copy in both constructors and will now verify green.
- Focused Gradle test passed under the Gradle-managed JDK 17: `AttackContextIsolationTest` reported one test with zero failures and zero errors at 21:47 local time. `git diff --check` also passed for the production and test files.
- Follow-up root cause: the `AttackContext` snapshot isolates the parent fetus state, but `LaserPlusFetus` still selected `BULLET_ONLY`, which posts `AttackContextPrepareEvent` and re-evaluates player `TriggerModule`s for the secondary laser. Added a focused regression test first; it failed at compilation because the explicit RAW selection point did not yet exist, as intended.
- Changed only `LaserPlusFetus` to select `RAW` for secondary lasers. This bypasses all prepare-stage module attachment and preserves the FetusBullet's captured trigger snapshot.
- Green verification: `LaserPlusFetusTest` and `AttackContextIsolationTest` both passed (one test each, zero failures/errors) under the Gradle-managed JDK 17. Scoped `git diff --check` passed.
- OCR delegation located 50 reviewable Java files in the already-dirty workspace. Reviewed the one in this task's scope, `LaserPlusFetus.java`, against its resolved rules; no finding. The new JUnit test was excluded by OCR's default test-path policy and was manually reviewed.
