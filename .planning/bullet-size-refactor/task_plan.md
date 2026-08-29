# Bullet Size Refactor

## Goal

Move the approved square-root size model into AttackContext, snapshotting the modifier and sharing the finalized value across TearBullet, FetusBullet, normal lasers, and Brimstone.

## Phases

- [completed] Replace calculator and legacy curves with AttackContext final-scale state.
- [completed] Route projectile and laser construction through the finalized context scale.
- [completed] Add formula, override, derivation, and laser-calibration regression coverage.
- [completed] Verify the full JUnit suite.
- [completed] Replace projectile collision inflation with dynamic AABBs and continuous volume sweeps.
- [completed] Rework laser width growth, swept laser collision, projectile tick ordering, and bounce contact responses.

## Decisions

- `finalSize = max(0.25, sqrt(max(0, damage) / 2) * (1 + bulletScaleModifier))`.
- No maximum size. Invalid input and non-positive multiplier results use the minimum.
- Split children inherit the multiplier snapshot and recalculate from their own damage.
- Builder accepts only the modifier and computes the final scale immediately. The mutable-context setter either directly overrides that final value or recalculates it from a new modifier; freeze only locks mutations.
- Normal lasers use the finalized scale times 0.25; Brimstone uses the finalized scale directly.
- Projectile collision dimensions use each visual family's world calibration, while homing range remains independent.
