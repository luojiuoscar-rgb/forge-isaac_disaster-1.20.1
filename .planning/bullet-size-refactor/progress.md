# Progress

## 2026-08-29

- Confirmed approved formula and scope.
- Preserving pre-existing changes to ModBulletVisuals.java and codex notes.
- Starting test-first implementation.
- Added calculator and snapshot regression tests. Gradle initially failed because JAVA_HOME pointed at a removed JDK 21; JDK 17 was located at C:\\Program Files\\Microsoft\\jdk-17.0.11.9-hotspot. Focused Gradle invocations exited after daemon startup without producing the requested test report, so verification will be retried after implementation.
- Confirmed the calculator test red state from its JUnit XML: all three cases failed because BulletSizeCalculator did not yet exist.
- Preserved the legacy logarithmic curve only for LaserAttack and BrimstoneAttack so the approved scope does not change laser width behavior.
- Focused tests passed: BulletSizeCalculatorTest (3 tests) and AttackContextBulletSizeSnapshotTest (1 test).
- Full JUnit suite passed: 29 tests, 0 failures, 0 errors. Executed with JDK 17 at C:\\Program Files\\Microsoft\\jdk-17.0.11.9-hotspot.
- Replaced the standalone calculator with AttackContext-owned final scale. The context now calculates at freeze time, supports direct-final or modifier-setter modes, and resets direct overrides for derived builders.
- Updated tear, fetus, normal laser, and Brimstone consumers. Brimstone now derives and freezes one shot context per scheduled beam so its mutable aiming state and finalized scale do not conflict.
- Removed the remaining unused PlayerHelper logarithmic curve. Full forced JUnit run passed: 31 tests, 0 failures, 0 errors.
- Corrected the scale lifecycle: building an AttackContext and setting a modifier now calculate immediately; freeze is again only the mutation lock. The updated focused context test passed under JDK 17.
- Replaced TearBullet preflight and double-inflated collision code with a single per-tick continuous volume sweep. Dynamic EntityDimensions now keep the real AABB synchronized with scale; TearBullet uses 0.2 * scale and FetusBullet uses its 0.35 model calibration.
- Added ProjectileSweep unit coverage for swept bounds, high-speed target crossing, visual-boundary misses, and path ordering. Full offline Gradle test passed with 35 tests, 0 failures, and 0 errors under JDK 17.
- Implemented laser width growth as sqrt(scale) capped at 3x base (0.25 normal, 1.0 Brimstone), removed Dust's extra 1.5 multiplier with finite size clamping, and changed laser entity/block checks to swept volume tests.
- Reordered TearBullet server tick to compute final movement before collision and consume lifetime before collision handling. Bounce responses now reposition at the hit point with epsilon instead of a full-step jump; entity bounce keeps the current gameplay rules while using the entity hit point.
- Verification: `gradlew.bat test --offline` with Java 17 passed, 35 tests, 0 failures, 0 errors.
- Fixed Brimstone to a constant 1.0 width for particles and swept entity collision. TearBullet block sweeps now derive the hit face from the expanded collision shape, place bounce results from the collision-box center, and ignore the same block for the next collision scan. Added pure face-direction regression coverage; full offline Gradle suite passed with 36 tests, 0 failures, and 0 errors under Java 17.
- Replaced TearBullet's per-block expanded-AABB sweep with Forge's world-shape `Entity.collideBoundingBox` clipping. The strongest clipped axis now selects the block-hit normal and an exact AABB surface contact point; bounce pushes the already-safe AABB outward without a block-ignore workaround. Full offline Gradle suite passed with 36 tests, 0 failures, and 0 errors under Java 17.
- Moved the projectile swept-volume helper from `entity.custom` to `helper/ProjectileCollisionHelper`, updated tear/laser callers and relocated its unit coverage. Full offline Gradle suite passed with 36 tests, 0 failures, and 0 errors under Java 17. Static audit identified per-tick world collision/entity queries as essential scaling costs and trajectory-string reparsing plus per-tick event allocation as removable overhead.
- Narrowed TearBullet's normal damage sweep to `LivingEntity` candidates and documented that future bullet-vs-bullet mechanics require a separate opt-in pass. Full offline Gradle suite passed with 36 tests, 0 failures, and 0 errors under Java 17.
- Confirmed the shatter-count test is red after changing its expected values to the approved 1/2/5 curve. Implemented the corresponding reduced count limits, vanilla-style 4x4 UV crops, gravity, inherited lifetime/friction, base motion handling, and egg-range explicit velocity while preserving AABB-wide spawning.
- Verification: `gradlew.bat test --offline --rerun-tasks --console=plain` completed successfully with JDK 17: 40 tests, 0 failures, 0 errors (3 existing skips). `git diff --check` also passed. Game-client validation remains pending.
- Began DamageType-based cooldown and knockback refactor. Added red tests for the tear horizontal impulse and cooldown-tag membership before implementation.
- Added `tear` and `laser` to `minecraft:bypasses_cooldown`, removed their manual invulnerability timer resets, and replaced stale `lastDamageSource` knockback detection with one-tick `LivingHurtEvent` handoff state. Tear now receives a resistance-aware 0.10 horizontal impulse; laser remains zero-knockback. Fetus 5-tick success interval remains unchanged.
- The forced red run failed before test execution because ForgeGradle rebuilt a broken mapping/dependency classpath. The normal JDK 17 offline full run then passed: 43 tests, 0 failures, 0 errors (3 existing skips); `git diff --check` passed.
- Renamed the temporary hurt-to-knockback handoff to `PENDING_KNOCKBACK_RESPONSE`, reduced it to the tear flag and projectile direction, moved the fixed tear strength beside the knockback handler, and renamed `makeDamage` to `applyDamage` across projectile and laser attack types. Final offline Gradle test passed: 43 tests, 0 failures, 0 errors (3 existing skips); `git diff --check` passed.
- Moved the pending response declaration next to the hurt/knockback handlers and made `applyTearImpulse` explicitly public for the existing direct unit test entry point. Incremental offline Gradle test passed: 43 tests, 0 failures, 0 errors (3 existing skips).
- Added a minimum packed-light floor to shatter particles by clamping block and sky light independently to 4 before rendering. Offline Gradle test passed: 43 tests, 0 failures, 0 errors (3 existing skips); `git diff --check` passed.
- Made the Mixin configuration optional (`required: false`) so a failed injection is skipped with a warning instead of preventing startup.
- Restored the original resolved shatter materials after the white-texture diagnostic. Added a temporary depth-test-only diagnostic to the shatter render type, restoring depth testing after the particle batch; `compileJava` passed.
