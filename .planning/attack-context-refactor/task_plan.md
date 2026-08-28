# AttackContext and AttackPattern Refactor

## Goal

Stabilize attack creation and execution around a creation-time `AttackContext` snapshot, a single main-axis direction model, pattern-driven multi-shot geometry, explicit pipeline entry stages, and isolated runtime projectile state.

## Final Status

All planned implementation phases are complete. The next work is ordinary feature development, including additional split-effect items, rather than another mandatory refactor phase.

## Completed Phases

### Phase 1: AttackContext Construction Contract

- [x] Replaced legacy long constructors with `AttackContext.Builder` and `toBuilder()`.
- [x] Resolved damage, range, and speed at creation time.
- [x] Sanitized invalid range/speed without producing zero, NaN, or Infinity.
- [x] Removed generic `attackSequenceIndex`; it remains laser/Brimstone runtime state.
- [x] Made trajectory, trigger, and split-sequence ownership explicit through defensive copies.

### Phase 2: Main-Axis Pattern Model

- [x] Replaced Context rotation and rotation-offset fields with normalized `Vec3 mainAxis`.
- [x] Added `GeometryHelper` for main-axis/rotation conversion, axis rotation, lateral axes, and impact-plane geometry.
- [x] Migrated Bullet, Laser, The Wiz, Ring, Semicircle, and Parasite geometry to patterns using `mainBulletContext`.
- [x] Kept yaw/pitch only at Minecraft entity/runtime boundaries.

### Phase 3: Brimstone Runtime Sequence

- [x] Kept one base Context per Brimstone main axis and one mutable working Context per scheduled 13-shot sequence.
- [x] Refreshed only working position and controllable main axis before each emission.
- [x] Kept `sequenceIndex` local to the sequence and on `LaserProjectile` runtime state.
- [x] Preserved Brimstone child sequence indices through `SplitExecutor -> shootSingle(...)`.

### Phase 4: Pipeline Stage Semantics

- [x] Defined pipeline stages as `Before -> Plan -> Prepare -> Execute`.
- [x] Renamed modes to `FULL`, `PLAN_PREPARE_AND_EXECUTE`, `PREPARE_AND_EXECUTE`, and `EXECUTE_ONLY`.
- [x] Removed `PLAYER_SCHEDULED`; Cursed Eye callbacks use player-primary plan/prepare execution after one release-level Before event.
- [x] Preserved intentional manual Before dispatch and sound ownership for charged attacks.

### Phase 5: Freeze and Runtime Snapshots

- [x] Freeze successful Contexts after Prepare and before Execute; `EXECUTE_ONLY` freezes before direct execution.
- [x] Reject Context mutations after freeze while retaining controlled Prepare APIs.
- [x] Moved mutable trigger/split-sequence execution state to Tear/Laser runtime snapshots.
- [x] Made `toBuilder().build()` the supported mutable derivation path from a frozen Context.

### Follow-up: Split Geometry and Derivation Safety

- [x] Store each bullet's `BlockHitResult` runtime snapshot through `IBulletObject.getLastBlockHit()`.
- [x] Align Cricket's Body laser/Brimstone block splits to the hit plane before four-way spread.
- [x] Preserve Parasite and Cricket's Body inheritance rules through `SplitSequence`.
- [x] Fix `toBuilder().direction(...).build()` so split references can override the inherited main axis without a null-axis failure.

## Current Contracts

| Area | Contract |
|---|---|
| `AttackContext` | Creation data with immutable scalars and one normalized `mainAxis`; mutable only until freeze. |
| `AttackPattern` | Generates child Contexts from `mainBulletContext` and shot count; use Builder derivation for modifications. |
| `AttackPipeline` | Owns stage selection, cancellation, Context finalization, freeze timing, and optional sound playback. |
| `IBulletObject` | Owns runtime triggers, split sequence, hit state, velocity, and Brimstone sequence index where applicable. |
| `SplitModule` | Decides trigger eligibility, child geometry/type, scalar inheritance, and recursive module inheritance; never spawns directly. |

## Remaining Verification Opportunities

- Add recording-style integration tests for pipeline event order and freeze timing.
- Add an `EXECUTE_ONLY` freeze regression test.
- Manually validate new/changed split items in a Forge 1.20.1 client using the IDEA-configured JDK 17 environment.

## Key Decisions

| Decision | Rationale |
|---|---|
| Builder derivation rather than mutable copies | A frozen Context remains a stable snapshot; a derived Context is deliberately mutable and isolated. |
| One Context direction field | `mainAxis` avoids base-angle/offset double-application and is sufficient for all shared attack geometry. |
| Brimstone working Context | Thirteen emissions represent one continuous beam and should not allocate thirteen full Contexts. |
| Runtime-only sequence index | It identifies laser/Brimstone runtime behavior and does not belong to generic creation data. |
| Controlled Prepare mutation, then freeze | Modules can prepare a projectile without allowing later Context mutation to leak into execution. |
| Split modules own item-specific policy | Generic executor remains responsible only for event publication and request execution. |
