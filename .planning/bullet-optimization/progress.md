# Progress Log

## Session: 2026-09-01

### Phase 1: Requirements and discovery
- **Status:** in_progress
- Actions taken:
  - Read required planning, Minecraft modding, executing-plans, TDD, verification, and OCR delegation skills.
  - Confirmed repository is Forge 1.20.1 and current branch was `master`.
  - Created and switched to `codex/bullet-optimization`.
  - Confirmed worktree already contains unrelated untracked files; these are preserved.
  - Attempted planning session catch-up; host has no `python` executable on PATH.
- Files created/modified:
  - `.planning/bullet-optimization/task_plan.md`
  - `.planning/bullet-optimization/findings.md`
  - `.planning/bullet-optimization/progress.md`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/BulletState.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/BulletManager.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/EntityGrid.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/VoxelDda.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/SweptCollision.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/BulletStream.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/TrackingGroups.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/BulletRuntime.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/IsaacDisaster.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/bullet/ClientBulletRuntime.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/networking/packet/BulletSpawnS2CPacket.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/networking/packet/BulletCorrectionS2CPacket.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/networking/ModMessages.java`

- Server manager is registered on level ticks, but ordinary `BulletAttack` still uses the legacy Entity path pending event/effect adapters.
- Client stream packets and prediction facade are present; rendering integration and lifecycle event migration remain pending.
- Added Javadocs to non-trivial new APIs and replaced the fallback color ID construction with `ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "base")`.
- Added Javadocs to packet codecs/handlers and collision result factories; no hard-coded mod ID remains in the new bullet code.
- Changed invalid optimized-runtime spawn handling to log and discard instead of throwing, with a regression test for null context.
- Verified no direct `throw new` remains in the new optimized bullet/runtime/packet code.
- Migrated ordinary and C-Section attack creation to `BulletState`; manager now performs tracking, DDA block sweep, entity swept hit, deferred split, and end-of-life dispatch.
- Fixed cancellation handling for bounce effects and removed duplicate entity split-trigger counting.
- Migrated ordinary BulletAttack and CSectionAttack creation to BulletState; added tracking, DDA/entity swept collision, lifecycle events, and deferred split execution.
- Added batch spawn/despawn packets, client tick prediction, BulletRenderState, and BulletBatchRenderer collection path.
- Replaced the collector-only path with an `AFTER_PARTICLES` client renderer: ordinary tears are submitted as shared-buffer billboards and Fetus uses a player-model adapter without a projectile entity.
- Added DDA plus `VoxelShape` fallback for non-full collision blocks, spectral entity collision, friendly-target filtering, registered tear damage attribution, and optimized tear knockback handoff.
- Added tracking groups scoped by owner/settings/region, a 24-candidate cap, and a four-tick failed-target retry backoff.
- Added batched corrections for server redirects and four-tick homing reconciliation; ordinary linear tears receive no continuous position packets.
- Captured split position/velocity at hit time so deferred child creation remains correct after bounce effects mutate the parent.
- Reconnected `BulletTickEvent` and updated Fetus laser execution to consume `IBulletObject`, preserving the Fetus trigger snapshot without creating a Fetus entity.

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| Session catch-up | PowerShell script invocation | Report previous context | `python` command unavailable | blocked |

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-09-01 | `python` not recognized | 1 | Continued with direct inspection and logged limitation |
| 2026-09-01 | Git ref creation denied in sandbox | 1 | Re-ran branch creation with approved elevated permission |

## Compatibility follow-up
- Corrected lightweight lifetime progression to match `TearBullet`: the final lifetime tick increments age and dispatches end-of-life without moving once more. The range value remains metadata for split context, matching the legacy entity path's fixed lifetime behavior.
- During swept entity handling, the state position is now set to the exact contact point before events/effects run, then restored to the segment end when the bullet continues. This keeps collision-center-dependent effects compatible with the entity path.
- Rechecked Fetus hit cooldown after each `BeforeHitEntity` event so later candidates in the same precomputed sweep cannot damage during the five-tick interval established by an earlier hit.
- An after-hit cancellation now stops the current movement segment, matching legacy `CollisionResult.STOP` behavior while retaining the correction packet.
- Added a regression assertion that the final lifetime tick does not advance position.

## Final verification
- Full `gradlew.bat --no-daemon test --console plain` passed after the compatibility follow-up.
- `git diff --check` passed. Remaining output consists only of existing line-ending warnings and the unrelated `SoulStateEffect` deprecation warning.

## Bullet count command
- Added operator command `/isd bullet count`.
- `BulletRuntime.activeCount(MinecraftServer)` totals active optimized `BulletState` entries across all loaded dimensions.
- The command reports the count in a feedback message and returns a fixed success result so a zero count is still a successful query.
- Final full `gradlew.bat --no-daemon test --console plain` passed after the command registration and zero-count success handling.
- `git diff --check` passed; only expected line-ending conversion warnings were emitted.

## Compatibility Review Planning
- Reviewed the lightweight manager, state, renderer, stream, packet, collision, tracking, split, and legacy tear/Fetus paths.
- Confirmed the severe size regression is caused by `BulletState.size` being used as both collision extent and render scale, with coefficients applied twice.
- Confirmed additional regressions: Fetus hit cooldown never decrements, block detection suppresses earlier entity hits, block sweep ignores projectile extents, homing steers every tick, controllable fallback is absent, late spawns do not catch up, packets broadcast to all players, and hot paths allocate avoidable copies.
- User accepted the deferred split/event ordering change; the repair plan preserves it.

## Compatibility Fix Implementation
- Added separate `renderScale`, `collisionWidth`, and `collisionHeight` state fields and propagated them through ordinary/Fetus creation, render state, spawn packets, and client rendering.
- Added Fetus owner UUID visual identity, slim/wide player model selection, and Compound Fracture skeleton visual selection for lightweight rendering.
- Added per-tick hit cooldown decrement, restored four-tick homing cadence, and restored controllable owner-look fallback steering.
- Unified block/entity swept collision resolution by earliest path parameter and expanded block/entity tests using the bullet's width/height.
- Added client late-spawn catch-up based on the known stream tick.
- Added per-player 128-block visibility tracking with entry snapshots and range-exit despawns; corrections are filtered by position.
- Reduced hot-path allocations by reusing EntityGrid query buffers and avoiding active-list copies during simulation.

## Verification
- Focused red tests initially failed as expected: cooldown remained 5 and late spawn remained at x=0.
- After implementation, focused BulletState/BulletStream tests passed.
- Fresh `compileJava` passed with Gradle-managed JDK 17 (one unrelated deprecation warning in `SoulStateEffect.java`).
- Fresh full `gradlew.bat --no-daemon test --console plain` passed.
- `git diff --check` passed; only line-ending warnings were reported by Git.

## Verification update
- `gradlew.bat --no-daemon test --tests net.luojiuoscar.isaac_disaster.bullet.* --console plain` passed.
- `gradlew.bat --no-daemon test --console plain` passed before the latest packet additions; latest `compileJava` passed after packet additions.
- Latest `gradlew.bat --no-daemon test --console plain` passed after documentation and ResourceLocation cleanup.
- Latest full `gradlew.bat --no-daemon test --console plain` passed after server migration and batch packet changes.
- Latest full `gradlew.bat --no-daemon test --console plain` passed after client renderer, collision, tracking, Fetus, and batched correction updates.

## Bullet Runtime Regression Fixes
- Added idempotent deferred removal for terminal lightweight collision/lifetime paths while retaining deferred split and event execution order.
- Clear per-player optimized-bullet visibility state on server logout so reconnecting players receive current snapshots.
- Included controllable-only bullets in four-tick tracking/velocity synchronization.
- Removed the Fetus target-box forced-zero-speed branch; direct steering and the configured minimum speed remain active.
- Added center-coordinate write semantics to the mutable bullet adapter. Lightweight states keep center coordinates directly; legacy tear entities convert to bottom coordinates.
- Copied `AttackContext` on lightweight state construction to isolate split/effect execution from later external mutation.
- Added complete client identity metadata cleanup and visitor-based render traversal; the default renderer no longer materializes a per-frame render-state list.
- Deleted the unreferenced top-level `net/minecraft/client/renderer/LevelRenderer.java` source copy.
- Focused `BulletStateTest`, `BulletStreamTest`, and `BulletBounceOnEntityTest` passed with Microsoft JDK 17. The default `JAVA_HOME` was stale and is overridden per Gradle invocation.

## Runtime Regression Fixes

## Visual, tracking, and split compatibility follow-up
- Added shared `TrackingProfile` with legacy hostility ordering, forward-biased candidate search, common target-center geometry, and stronger Fetus steering defaults.
- Enabled controllable steering in the lightweight manager with homing-over-control and owner-look fallback semantics.
- Decoupled spectral block contact from physical block collision so spectral bullets/lasers can trigger one-shot block split while continuing through blocks.
- Restored Cricket's Body priority to `0` and expanded its random spread to `0..90` degrees; Brimstone's existing every-third-sequence gate remains unchanged.
- Moved lightweight rendering to `AFTER_WEATHER`, replaced full-bright vertices/model light with sampled block/sky light, and added a shatter snapshot packet plus shortened client particle lifetime.
- Added regression tests for tracking profile behavior and split priority.

## Verification update
- `gradlew.bat --no-daemon test --tests net.luojiuoscar.isaac_disaster.bullet.* --tests net.luojiuoscar.isaac_disaster.registries.split_module.* --console plain` passed.
- Full `gradlew.bat --no-daemon test --console plain` passed after final changes.
- `git diff --check` passed; only expected LF/CRLF conversion warnings and the pre-existing `SoulStateEffect` deprecation warning remain.
- Added `/isd bullet clear`, restricted to optimized `BulletRuntime` states across loaded dimensions. It sends despawn batches to clients already tracking the cleared states and returns success at zero.
- Added explicit server level/server-stop cleanup and client logout/client-level/null-world cleanup. Client `BulletStream` now uses a server epoch so a reload cannot reuse an earlier world's tick baseline and fast-forward new bullets to expiry.
- Replaced homing's periodic position/velocity correction with an anonymous shared steering sample packet: server selects targets, sends only stream-local handle-to-position samples, and clients replay steering without entity scans, IDs, or UUIDs. Tracking group membership now persists through the steering pass.
- Fixed lightweight block sweep normals and BulletState-specific push-out distance so reflected bullets leave their expanded collision volume. Complex shapes now use the same dimension-expanded fallback; DDA checks adjacent voxels needed by the bullet extents.
- Added focused tests for stream epoch reset, client target-position steering, collision half-extent push-out, and the swept outward face.
- Initial Gradle test run was blocked by stale `JAVA_HOME`; the verified local Java 17 home is `C:\\Users\\16136\\.gradle\\jdks\\eclipse_adoptium-17-amd64-windows\\jdk-17.0.16+8` and is used process-locally for this task.
- Follow-up hardening records tracking sample ticks on the client so each anonymous target sample is consumed at most once, resets stream epoch/tick state on clear, and handles late packets without periodic stale reapplication.
- Complex block fallback now sweeps every `VoxelShape` AABB rather than the aggregate bounds, preserving gaps in stairs, fences, and similar blocks. Starting-inside sweeps receive a deterministic outward face, and optimized block hits push the bullet outside its expanded collision volume before callbacks.
- Final verification after these changes: `gradlew.bat --no-daemon test --console plain` passed; only the pre-existing `SoulStateEffect` deprecation warning and Git line-ending warnings remain.

## Regression hardening follow-up
- Made client stream epochs monotonic: positive packets from an older world are rejected instead of clearing the current prediction stream; compatibility epoch `0` remains non-destructive.
- Updated spawn, correction, despawn, and tracking handlers to skip rejected-epoch payloads completely.
- Removed the unused server pending-spawn cache; newly visible clients continue to receive authoritative active-state snapshots.
- Consumed authoritative despawn identities during visibility synchronization so removals generated during the tick are included in the outgoing batch.
- Added a regression test proving an older epoch cannot clear a live stream entry.

## Verification update
- `gradlew.bat --no-daemon test --tests net.luojiuoscar.isaac_disaster.bullet.BulletStreamTest --console plain` passed.
- Full `gradlew.bat --no-daemon test --console plain` passed.
- `git diff --check` passed; only expected LF/CRLF conversion warnings were emitted.
- World-unload cleanup now retains the last client epoch watermark while clearing state, so delayed packets from the previous world are rejected until a strictly newer stream appears; logout still performs a full epoch reset.
- Added regression coverage for this retained-watermark behavior; the focused and full Gradle test suites both pass.

## Timeline and steering smoothing follow-up
- Spawn snapshots restore server age, traveled distance, previous position, and kinematic state without client-local fast-forward simulation.
- Lightweight bullet positions use collision-center semantics; ordinary and Fetus creation paths apply their half-height conversion once, while rendering consumes the center directly.
- Homing remains on the four-tick target-selection cadence, with per-tick bounded steering and client velocity-sample blending.
- Tracking packets carry the authoritative base acceleration (`BulletState.acceleration()`); the per-tick steering delta remains diagnostic and is not integrated as client acceleration.
- Client slot replacement removes auxiliary tracking/sample metadata for the replaced generation to avoid stale-generation accumulation.
- Verification after the follow-up: full `gradlew.bat --no-daemon test --console plain`, `compileJava`, and `git diff --check` passed. Only the pre-existing `SoulStateEffect` deprecation and Git line-ending warnings remain.

## Fetus and homing behavior repair
- Implementation started from the approved plan.
- Confirmed the current lightweight state has no dedicated base-speed field, no per-source hit-history policy, and only limited steering.
- Confirmed the existing Fetus client protocol has velocity samples but no Fetus position sample; this repair adds an interpolated position path only for visible Fetus entries.
- Added red tests for Fetus direct steering/repeat-hit policy and ordinary close-range braking plus base-speed recovery.
- Added `BulletSteeringMode`, base-speed state and spawn propagation, source-specific hit-history handling, Fetus direct tracking, ordinary distance braking, and interpolated Fetus corrections.
- Rechecked the no-target paths and added per-tick controllable fallback for direct steering plus immediate ordinary cruise-speed recovery after target loss.
- Full Gradle tests and Java compilation pass after the implementation.
