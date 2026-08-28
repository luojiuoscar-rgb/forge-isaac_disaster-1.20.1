# Findings and Current Design

## Attack Lifecycle

```text
Builder / Pattern -> Before -> Plan -> Prepare -> Freeze -> Execute
```

- `Before` is attack-level confirmation/cancellation and attack-level effects.
- `Plan` can replace or append the Context group, for example The Wiz.
- `Prepare` attaches final per-projectile modules.
- `Freeze` prevents Context changes from leaking into entity creation.
- `Execute` creates runtime entities; runtime entities receive copied triggers and split sequences.

Pipeline modes identify their entry point:

| Mode | Stages |
|---|---|
| `FULL` | Before, Plan, Prepare, Execute |
| `PLAN_PREPARE_AND_EXECUTE` | Plan, Prepare, Execute |
| `PREPARE_AND_EXECUTE` | Prepare, Execute |
| `EXECUTE_ONLY` | Execute only, after Context freeze |

## AttackContext Contract

- Owner is required; a missing shooter defaults to owner.
- Damage, range, and speed are resolved during construction. Invalid range/speed are sanitized rather than thrown.
- `mainAxis` is the only direction representation and is always finite, non-zero, and normalized.
- `color`, spawn position, trigger additions, split-module additions, and exact-spawn behavior are legal only before freeze.
- `copy()` preserves the source freeze state. `toBuilder().build()` creates a separate mutable Context with copied trigger and split state.
- A derived builder accepts either `mainAxis(...)` or `direction(...)`; `direction(...)` is normalized as the derived Context axis.
- Trajectories are immutable snapshots. `copyTrigger()` and `copySplitSequence()` are the only Context read/derivation APIs for mutable nested state.

## Geometry and Patterns

- `GeometryHelper` owns generic vector work: axis/rotation conversion, axis rotation, lateral axis generation, block-hit normal handling, and plane projection.
- `AbstractAttackPattern` owns Context derivation for patterns.
- Bullet, Laser, Wiz, Ring, Semicircle, and Parasite patterns preserve their former output count/order/geometry while producing main-axis-based child Contexts.
- Entity yaw/pitch remains a Minecraft API/runtime detail, calculated from `mainAxis` when an entity is created.

## Brimstone

- One incoming Context denotes one complete Brimstone main axis.
- `BrimstoneAttack` creates one mutable working Context for that axis and schedules thirteen emissions.
- Each emission updates spawn position and, only when controllable, the current shooter axis. It then uses `shootSingle(workingContext, sequenceIndex)`.
- `LaserProjectile` stores the runtime `sequenceIndex`; split Brimstone children reuse their parent sequence index through `shootSingle`.

## Split System

```text
TriggerModule.attachToBullet
    -> AttackContext.addSplitModule
    -> runtime Tear/Laser copies SplitSequence
    -> collision/end-of-life invokes SplitExecutor
    -> BulletSplitEvent
    -> highest applicable SplitModule priority layer generates child requests
    -> EXECUTE_ONLY child execution
```

- `SplitModule` generates Contexts and applies inheritance; it never creates entities directly.
- `SplitSequence` evaluates only the highest-priority applicable layer for a trigger boundary.
- Child Contexts inherit runtime position, range, damage, and velocity-derived direction through `SplitExecutor`'s reference Context.
- Brimstone child requests bypass generic executor invocation only to preserve their runtime sequence index; all other child requests use normal `EXECUTE_ONLY` execution.
- `IBulletObject.getLastBlockHit()` is the generic runtime source for a block collision's complete `BlockHitResult`.

### The Parasite

- Splits into two perpendicular children.
- Does not trigger on end-of-life; block triggers only on the first block hit.
- Child damage and range are halved.
- Normal tear children may inherit split modules while damage remains at least one; Laser/Brimstone children do not recurse.
- Preserves parent attack family: Laser produces Laser, Brimstone produces Brimstone, otherwise Bullet.

### Cricket's Body

- Produces four children with half parent damage/range.
- Entity/end-of-life behavior uses the legacy world-up spread.
- Block behavior applies only to Laser/Brimstone on the first block hit.
- For a block hit, the incoming main axis is projected onto the impact plane, then four children rotate around the hit normal inside that plane. This prevents children from immediately entering the block.
- Brimstone requires a positive sequence index divisible by three.
- Children receive no inherited split modules, preventing recursive Cricket's Body splitting.

## Follow-up Test Gaps

- Pipeline event order and freeze timing need a recording-based integration test.
- `EXECUTE_ONLY` needs a direct freeze regression test.
- Prepare-time trigger/split/color changes need one end-to-end frozen execution test.
