# Task Plan: Bullet Optimization v1 Compatibility Fixes

## Goal
Restore all identified behavioral equivalence issues between the lightweight `BulletState` path and the legacy `TearBullet`/`FetusBullet` path, while preserving the intentionally deferred split/event ordering and keeping trajectory disabled.

## Scope and explicit non-goals
- Fix size semantics, Fetus hit cooldown, collision ordering and extents, homing cadence, controllable fallback steering, client late-spawn prediction, visibility-filtered synchronization, and avoidable hot-path allocations.
- Preserve deferred split execution after event dispatch. Capture the collision-time parent snapshot and keep generation checks; do not reorder split versus event listeners in this round.
- Do not implement or re-enable trajectory position offsets, trajectory velocity changes, trajectory parsing, or trajectory network state.

## Implementation phases

### Phase 1: State and behavior compatibility
- Separate `BulletState` fields for visual render scale, horizontal collision width, and collision height; retain a compatibility accessor only where old effect APIs require it.
- Map ordinary tears to legacy values: render scale = context bullet scale, width/height = `0.2 * scale`.
- Map Fetus to legacy values: render scale = context bullet scale, width = `0.35 * 0.6 * scale`, height = `0.35 * 1.8 * scale`.
- Carry the distinct render scale and collision dimensions through `BulletRenderState` and spawn packets; update billboard/model rendering so each coefficient is applied exactly once.
- Add an authoritative hit-cooldown tick method and decrement it every manager tick. Initialize Fetus cooldown to zero and set it to five only after a successful hit; leave ordinary tear behavior unchanged.
- Preserve the old steering cadence (every fourth authoritative tick), target search backoff, Fetus six-block range and distance-based slowdown. Add the old controllable fallback toward the owner eye/look point when no target is available, including its control range/steer parameters.
- Preserve lifetime semantics around the final tick, and ensure client prediction uses the same age/velocity progression as the server.

### Phase 2: Unified swept collision
- Resolve block and entity contacts for the same movement segment before applying either event; compare normalized path parameters and process the earliest contact first.
- Keep spectral behavior (skip block collision but still test entities), piercing, cancellation, and one-hit-per-target rules.
- Make block collision dimension-aware: use bullet half-extents when testing full blocks through DDA candidates and expand/clip non-full `VoxelShape` boxes accordingly; retain shape fallback for complex blocks.
- Return the actual contact parameter and position from block detection so entity-before-block and block-before-entity cases are deterministic.
- Ensure collision-side effects receive the same center/height dimensions as the legacy entity path.

### Phase 3: Client stream and visual compatibility
- Use `startTick` to catch a late spawn up to the current client/server tick before exposing it for rendering; discard immediately if the catch-up reaches lifetime.
- Keep correction interpolation, slot+generation validation, and continuous local prediction; never snap a visible bullet directly to a correction position.
- Filter spawn, despawn, event, and correction delivery by a server constant visibility radius of 128 blocks around each player. Add a full current-state snapshot when a player enters a stream/range so late subscribers do not miss existing bullets.
- Flush only the bullet render buffer/type owned by the lightweight renderer instead of calling global `endBatch()`.
- Resolve Fetus visual profile/owner skin and slim/wide model selection from the stored visual state where the old renderer did so; retain a deterministic default fallback when no profile is available.

### Phase 4: Hot-path allocation and regression coverage
- Iterate active slots by index without `List.copyOf(active)` per tick; keep swap-remove safe by deferring mutations.
- Replace per-bullet immutable candidate copies and avoidable `EntityGrid.query()` list churn with reusable/visitor-style query buffers whose contents are valid only for the current operation.
- Add focused tests for size mapping and packet/render state, Fetus cooldown recurrence, homing four-tick cadence and controllable fallback, entity-before-block ordering, bullet extents against full/non-full blocks, late spawn catch-up, stream visibility/snapshot behavior, and generation rejection.
- Run the full Gradle test suite, compile check, and `git diff --check`. Record that real in-game 1k-15k performance and visual smoothness still require a client/server pressure run.

## Decisions
| Decision | Rationale |
|----------|-----------|
| Preserve deferred split/event ordering | User confirmed this behavior change is intentional; only snapshot/generation correctness is required. |
| Keep one `BulletState` per active bullet | Maintains the chosen maintainable object model while fixing semantic fields. |
| Use legacy collision dimensions exactly | Prevents visual/collision regressions and preserves Fetus's non-square hitbox. |
| Steering remains four-tick cadence | Matches legacy `TearBullet`/`FetusBullet` behavior and avoids changing homing strength. |
| Visibility radius is a 128-block server constant | Prevents broadcasting every bullet to every player; entering players receive a snapshot. |

## Risks and verification gaps
- A real Forge level is still needed to validate voxel shapes, entity movement, renderer model variants, and packet visibility under latency.
- The 10k+ benchmark must measure server tick time, client frame time, bytes/tick, correction error, and spawn/despawn peaks after these fixes.

## Current status
- Implementation phases 1-4 are complete on `codex/bullet-optimization`.
- Compatibility follow-up for lifetime timing, collision contact position, same-tick Fetus cooldown, and cancellation stop semantics is complete.
- Runtime regression fixes for terminal removal, reconnect/dimension snapshots, controllable synchronization, Fetus steering, center-coordinate adapters, client cache cleanup, visitor rendering, and context isolation are complete.
- The unreferenced top-level `net/minecraft/client/renderer/LevelRenderer.java` copy has been removed.
- No commit or merge has been created.

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| Planning catch-up script unavailable because `python` is not on PATH | 1 | Continued with direct PowerShell/Git inspection; existing progress log retained. |
