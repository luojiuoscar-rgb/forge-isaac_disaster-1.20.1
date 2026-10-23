# Custom explosion and War locust — approved implementation plan

Worktree: isaacdisaster worktree b, feature/feature-b. Preserve previous changes. No commit or branch operations. This continues the existing task; no master synchronization over the uncommitted implementation.

- [x] Core: immutable generic parameters, custom execution, LevelHelper entry and compatibility overload; JUnit and Forge integration coverage.
- [x] Migration: IsaacBomb, GigaBomb, Ipecac/Kamikaze/InnerChild; obsolete IsaacBombExplosion adapter removed in review follow-up.
- [x] Locust: five priority-1 locusts, War registration, vanilla attributed explosion source and contact behavior; integration tests.
- [x] Verification: targeted JUnit, GameTests, review and client startup smoke; native visual/audio inspection unavailable due to approval transport failure, recorded as a limitation.

Shared interface contract (implementation-owned decisions):
- package system.explosion; public record ExplosionParameters(float power, float centerDamage, float entityRadius, float blockPower, float blockResistanceMultiplier, BlockDestructionMode blockDestructionMode, EntityImpactMode entityImpactMode, boolean ignoreFriendly, boolean causesFire, Explosion.BlockInteraction blockInteraction, FeedbackMode feedbackMode).
- Nested enums: BlockDestructionMode { RESISTANCE, IGNORE_RESISTANCE }, EntityImpactMode { STANDARD, GIGA }, FeedbackMode { STANDARD, GIGA }.
- Factories standard(float power,float centerDamage), giga(float power). Copy methods withIgnoreFriendly(boolean), withCausesFire(boolean), withBlockInteraction(Explosion.BlockInteraction).
- boolean LevelHelper.explodeCustom(ServerLevel, Vec3, DamageSource, ExplosionParameters).
- CustomExplosion.explode has the same signature; CustomExplosion.calculateBlocksToBlow(Level, Explosion, Vec3, ExplosionParameters) returns Set<BlockPos> for compatibility helpers.
- Standard factory: actual entityRadius=2*power, blockPower=power, resistance multiplier1, normal resistance/impact/feedback, ignoreFriendly=false, fire=false, DESTROY_WITH_DECAY. Giga: center300, entityRadius=power, ignore resistance, GIGA impact/feedback, ignoreFriendly=true, no fire, DESTROY.

Approved semantics: friend exclusion includes knockback; LivingEntity direct source is not automatically excluded. Giga retains current AABB selection, quadratic damage and knockback; protected high-resistance blocks do not stop rays. Old skill damage becomes center damage. War always explodes on collision (even canceled/lethal contact), excludes friends, power4 actual radius8, center20*StatManager.DAMAGE.getBonus(), no bomb item synergies. Follow-up correction: use vanilla explosion damage and cooldown rules; no dedicated damage type or guarantee of additive contact and explosion damage. Existing poison and other variants remain intact.

- [x] Follow-up: delete all tutorial-directory artifacts added by this chat and remove the dedicated War damage type.
- [x] Follow-up: verify vanilla cooldown behavior and focused regression tests (154 JUnit, 27 Forge GameTests passed).

Current review follow-up:
- [x] Return false and log a warning for non-finite explosion centers, before any events or world changes; regression test NaN and both infinities on each axis.
- [x] Move damage formulas into their sole production consumer CustomExplosion; remove ExplosionDamage and unused IsaacBombExplosion adapter, simplifying its obsolete test and regular-bomb delegation.
- [x] Make Death locust override getContactDamage for one 4D hit; update attribution, damage snapshot and canceled-contact coverage.
- [x] Run focused JUnit/GameTests and verify branch, whitespace and tutorial-directory cleanliness (154 JUnit, 27/27 Forge GameTests).
- Keep the additive client knockback packet and attributed Explosion carrier; explain their actual responsibilities with source pointers.

Post-commit knockback fixes (112559e8):
- [x] Reproduce pending mob knockback loss and spectator/creative-flight knockback with captured server packets.
- [x] Synchronize any pending authoritative player velocity before adding explosion impulse; avoid clearing later unrelated updates.
- [x] Exclude spectators and prevent creative-flight impulse/forced motion synchronization, preserving their existing unrelated motion updates.
- [x] Verify additive-only behavior, consecutive explosions, tracker synchronization, focused JUnit/GameTests and review (154 JUnit, 31/31 Forge GameTests).
