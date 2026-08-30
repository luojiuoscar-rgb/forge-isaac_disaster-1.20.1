# Findings

- `AttackType#getBulletScale` currently combines a logarithmic damage curve with an additive BULLET_SCALE attribute read at projectile creation.
- TearBullet already owns the synchronized scale that drives rendering, bounding box, and collision expansion; FetusBullet overrides only collision coefficients.
- SplitExecutor derives children from the parent AttackContext and changes child damage, so a new immutable context multiplier naturally survives split derivation.
- TearBullet previously overwrote its AABB only in setScale(), but Entity.move() restored the registered fixed dimensions during flight. Its hit test also combined a broad-phase expansion with a second target-AABB expansion, producing a damage range far larger than the sprite at high scale.
- ProjectileSweep now performs a Minkowski-style continuous sweep: target entity and block-shape AABBs expand only by the real projectile half-extents, then the projectile center ray selects the earliest contact.
- Vanilla 1.20.1 `BreakingItemParticle` uses gravity 1.0, inherited friction 0.98 and lifetime, 10% of the base randomized motion plus explicit velocity, and a continuous 4x4 texture crop. Egg explicit velocity is [-0.04, 0.04] on every axis.
- `LivingEntity#getLastDamageSource()` is updated after vanilla calls `knockback`, so it cannot reliably classify the source in `LivingKnockBackEvent`. `LivingHurtEvent` sees the correct source before that point and must provide one-tick handoff state.
