# Archived Findings

## Projectile Scale And Collision

- Final projectile scale is stored in `AttackContext`; it is calculated on build
  and recalculated by setters unless direct final-scale override is requested.
- Unified gameplay collision geometry lives in
  `helper/ProjectileCollisionHelper.java`.
- Tear and fetus entities use dynamic AABB dimensions derived from their final
  scale. `getCenter()` is the authoritative tracking and sweep origin; entity
  `position()` remains the bottom-coordinate semantic required by Minecraft.
- Entity collision uses continuous swept tests for `LivingEntity` and projectile
  targets. Block movement uses native `Entity.collideBoundingBox(...)` clipping,
  with the strongest blocked axis providing the reflection face.
- Block bounce moves to the clipped contact position, applies a small epsilon
  along the reflected normal, and processes at most one block bounce per tick.

## Laser And Damage Response

- Ordinary laser width uses the bounded square-root growth function and a maximum
  of three times the base width.
- Brimstone uses fixed width `1.0` and remains spectral; its collision width is
  fixed with the visual width.
- `tear` and `laser` are in `minecraft:bypasses_cooldown`, not
  `bypasses_invulnerability`.
- Forge hurt/knockback handling records a temporary response keyed by entity and
  tick. `LivingKnockBackEvent` cancels vanilla knockback; tear applies a fixed
  horizontal `0.10` impulse reduced by knockback resistance, laser applies none.
- Fetus bullet's existing five-tick successful-hit cooldown and damaged-entity
  tracking remain unchanged.

## Shatter Particles

- `ProjectileCollisionHelper` also owns `normalizeScale`, fragment count, quad
  size, and physical fragment diameter. `ProjectileShatterHelper` is removed.
- Current shatter count curve is approximately 20% of the prior curve: 2 at scale
  1, minimum 1, maximum 5, with square-root growth.
- `TearShatterParticle` uses vanilla-like gravity `1.0`, friction `0.98`, random
  lifetime, 4x4 UV crops, and a physical AABB matching the visible quad.
- Spawn positions are inset by half the fragment diameter and checked with
  `ClientLevel.noCollision`; unsafe candidates are skipped to avoid fragments
  starting inside blocks.
- Particle depth testing remains enabled. This prevents particles rendering over
  living entities, while the projectile entity visuals use a separate RenderType.

## Rendering And Compatibility

- `ProjectileRenderTypes` provides a cached projectile translucent RenderType.
- It keeps depth testing, but uses `COLOR_WRITE` only, so a low-alpha projectile
  does not write an opaque depth entry that can hide shatter particles.
- The implementation exposes Forge 1.20.1 protected RenderStateShard constants
  through a small nested accessor subclass; this is version-sensitive and must be
  rechecked when mappings or Minecraft versions change.
- Visuals using this RenderType: default tear, default fetus, and compound-fracture
  skeleton fetus.
- `BulletVisualResolver` centralizes priority resolution and can filter candidates
  by client renderer availability, keeping body and shatter material selection
  aligned.

## Known Boundaries And Risks

- Full client/game validation of transparent ordering and block-edge shatter
  visuals remains outstanding.
- If a projectile is completely embedded in a block, safe spawn filtering can
  skip some or all fragments. This is intentional defensive behavior.
- Particle RenderType still uses `depthMask(true)`; changing it would affect
  particle depth ordering and should be tested separately.
- `ConcurrentHashMap` RenderType cache is bounded in normal use by registered
  visual textures, but dynamic unbounded texture registration would grow it.
- Do not stage `.planning`, `codex`, `NUL`, or unrelated feature changes while
  preparing a projectile commit.

## Mixin Notes

- Existing mixins use Sponge Mixin 0.8.5 with `required=false` in
  `isaac_disaster.mixin.json`.
- MixinExtras 0.5.5 was researched but not adopted. `@WrapOperation` was
  identified as the preferred future replacement for the frozen-friction
  redirect; no dependency or Mixin migration should be assumed from this archive.
