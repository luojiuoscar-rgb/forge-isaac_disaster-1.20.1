package net.luojiuoscar.isaac_disaster.system.freeze.state;

import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-only transient state for frozen-mob impacts and the last attack source.
 * This state is intentionally not persisted or synchronized to clients.
 */
public final class FrozenImpactState {
    public static final double IMPACT_SPEED_THRESHOLD = 0.08D;

    private static final Map<UUID, State> STATES = new HashMap<>();

    private FrozenImpactState() {
    }

    /**
     * Checks the collision produced by the previous entity tick, then captures
     * the movement state that will be used by the next check.
     */
    public static void tick(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        // A normal death animation keeps the entity ticking before removal; it must not create new shatter impacts.
        if (mob.isDeadOrDying()) {
            clear(mob);
            return;
        }
        if (!EntityVisualState.hasFreezeSource(
                mob, net.luojiuoscar.isaac_disaster.effect.ModEffects.FROZEN.getId())) {
            clear(mob);
            return;
        }

        State state = getState(mob, level);

        if (state.hasSnapshot && hasImpact(mob, state)) {
            shatter(mob, level, state);
            clear(mob);
            return;
        }

        Vec3 movement = mob.getDeltaMovement();
        state.horizontalSpeed = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        state.verticalSpeed = movement.y;
        state.wasOnGround = mob.onGround();
        state.hasSnapshot = true;
    }

    /** Records every canceled damage source and applies an immediate horizontal push when possible. */
    public static void recordAttack(Mob mob, DamageSource source, float pushSpeed) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        if (mob.isDeadOrDying()) {
            clear(mob);
            return;
        }

        State state = getState(mob, level);
        state.lastAttackDirectEntity = getUuid(source.getDirectEntity());
        state.lastAttackCausingEntity = getUuid(source.getEntity());

        Vec3 direction = getPushDirection(mob, source.getDirectEntity(), source.getEntity());
        if (direction.lengthSqr() < 1.0E-7D) return;

        // Only an attack with a real directional impulse may claim a later impact kill.
        state.lastPushDirectEntity = state.lastAttackDirectEntity;
        state.lastPushCausingEntity = state.lastAttackCausingEntity;
        Vec3 movement = mob.getDeltaMovement();
        Vec3 impulse = direction.normalize().scale(pushSpeed);
        mob.setDeltaMovement(movement.x + impulse.x, movement.y, movement.z + impulse.z);
        mob.hasImpulse = true;
    }

    public static void clear(Mob mob) {
        if (!(mob.level() instanceof ServerLevel)) {
            return;
        }
        STATES.remove(mob.getUUID());
    }

    public static void clearLevel(ResourceKey<Level> dimension) {
        STATES.entrySet().removeIf(entry -> entry.getValue().dimension.equals(dimension));
    }

    public static void clearAll() {
        STATES.clear();
    }

    private static boolean hasImpact(Mob mob, State state) {
        boolean hitWall = mob.horizontalCollision && state.horizontalSpeed >= IMPACT_SPEED_THRESHOLD;
        boolean landedHard = !state.wasOnGround
                && mob.onGround()
                && state.verticalSpeed <= -IMPACT_SPEED_THRESHOLD;
        return hitWall || landedHard;
    }

    private static void shatter(Mob mob, ServerLevel level, State state) {
        BlockState ice = Blocks.ICE.defaultBlockState();
        Vec3 center = mob.getBoundingBox().getCenter();
        int particleCount = (int) Math.ceil(mob.getBbWidth() * mob.getBbWidth() * mob.getBbHeight() * 32.0D);
        particleCount = Math.max(24, Math.min(256, particleCount));

        level.playSound(null, mob.blockPosition(), ice.getSoundType(level, mob.blockPosition(), mob).getBreakSound(),
                SoundSource.BLOCKS, 1.0F, 1.0F);

        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, ice);
        level.sendParticles(particle, center.x, center.y, center.z, particleCount,
                mob.getBbWidth() * 0.5D, mob.getBbHeight() * 0.5D, mob.getBbWidth() * 0.5D, 0.0D);

        DamageSource source = createShatterSource(level, state);
        mob.hurt(source, Float.MAX_VALUE);
    }

    private static DamageSource createShatterSource(ServerLevel level, State state) {
        var holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageType.FROZEN_SHATTER);
        Entity direct = resolve(level, state.lastPushDirectEntity);
        Entity causing = resolve(level, state.lastPushCausingEntity);
        if (direct == null && causing == null) return new DamageSource(holder);
        return new DamageSource(holder, direct, causing == null ? direct : causing);
    }

    private static Entity resolve(ServerLevel level, UUID id) {
        return id == null ? null : level.getEntity(id);
    }

    private static UUID getUuid(Entity entity) {
        return entity == null ? null : entity.getUUID();
    }

    private static State getState(Mob mob, ServerLevel level) {
        State state = STATES.computeIfAbsent(mob.getUUID(), ignored -> new State(level.dimension()));
        if (state.dimension.equals(level.dimension())) return state;

        State replacement = new State(level.dimension());
        STATES.put(mob.getUUID(), replacement);
        return replacement;
    }

    private static Vec3 getPushDirection(Mob mob, Entity direct, Entity causing) {
        Vec3 direction = getDirection(mob, direct);
        if (direction.lengthSqr() >= 1.0E-7D) return direction;
        return getDirection(mob, causing);
    }

    private static Vec3 getDirection(Mob mob, Entity source) {
        if (source == null) return Vec3.ZERO;
        return mob.getBoundingBox().getCenter().subtract(source.getBoundingBox().getCenter()).multiply(1.0D, 0.0D, 1.0D);
    }

    private static final class State {
        private final ResourceKey<Level> dimension;
        private double horizontalSpeed;
        private double verticalSpeed;
        private boolean wasOnGround;
        private boolean hasSnapshot;
        private UUID lastAttackDirectEntity;
        private UUID lastAttackCausingEntity;
        private UUID lastPushDirectEntity;
        private UUID lastPushCausingEntity;

        private State(ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }
    }
}
