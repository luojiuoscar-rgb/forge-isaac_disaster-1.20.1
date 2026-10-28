package net.luojiuoscar.isaac_disaster.system.explosion;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.ExplosionKnockbackS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.luojiuoscar.isaac_disaster.system.explosion.ExplosionParameters.*;

/** Executes parameterized explosions without invoking vanilla's entity damage or block selection. */
public final class CustomExplosion {
    private static final int RAY_GRID_SIZE = 16;
    private static final double RAY_STEP = 0.3;
    private static final float STEP_ATTENUATION = 0.22500001F;

    private CustomExplosion() { }

    public static boolean explode(ServerLevel level, Vec3 center, DamageSource source, ExplosionParameters parameters) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(center, "center");
        Objects.requireNonNull(source, "damageSource");
        Objects.requireNonNull(parameters, "parameters");
        if (!Double.isFinite(center.x) || !Double.isFinite(center.y) || !Double.isFinite(center.z)) {
            IsaacDisaster.LOGGER.warn("Skipping custom explosion with non-finite center {} in {} (damage source: {})",
                    center, level.dimension().location(), source.getMsgId());
            return false;
        }
        Entity direct = source.getDirectEntity();
        Explosion explosion = new AttributedExplosion(level, center, source, parameters);
        if (ForgeEventFactory.onExplosionStart(level, explosion)) return false;
        level.gameEvent(direct, GameEvent.EXPLODE, center);
        if (explosion.interactsWithBlocks()) {
            explosion.getToBlow().addAll(calculateBlocksToBlow(level, explosion, center, parameters));
        }
        List<Entity> affected = affectedEntities(level, center, source, parameters);
        ForgeEventFactory.onExplosionDetonate(level, explosion, affected, parameters.entityRadius());
        damageEntities(explosion, center, affected, parameters);
        // Server block updates perform authoritative removal; feedback is emitted separately once.
        explosion.finalizeExplosion(false);
        sendFeedback(level, explosion, center, parameters);
        return true;
    }

    /** Selects affected blocks without changing the world. */
    public static Set<BlockPos> calculateBlocksToBlow(Level level, Explosion explosion, Vec3 center,
                                                     ExplosionParameters parameters) {
        Set<BlockPos> result = new HashSet<>();
        if (parameters.blockPower() <= 0) return result;
        ExplosionDamageCalculator calculator = calculator(explosion.getDirectSourceEntity());
        for (int x = 0; x < RAY_GRID_SIZE; x++) {
            for (int y = 0; y < RAY_GRID_SIZE; y++) {
                for (int z = 0; z < RAY_GRID_SIZE; z++) {
                    if (x != 0 && x != 15 && y != 0 && y != 15 && z != 0 && z != 15) continue;
                    Vec3 direction = new Vec3(x / 15.0 * 2 - 1, y / 15.0 * 2 - 1, z / 15.0 * 2 - 1).normalize();
                    float energy = parameters.blockPower() * (0.7F + level.random.nextFloat() * 0.6F);
                    double px = center.x, py = center.y, pz = center.z;
                    while (energy > 0) {
                        BlockPos pos = BlockPos.containing(px, py, pz);
                        if (!level.isInWorldBounds(pos)) break;
                        BlockState state = level.getBlockState(pos);
                        if (parameters.blockDestructionMode() == BlockDestructionMode.IGNORE_RESISTANCE) {
                            if (!state.isAir() && state.getFluidState().isEmpty()
                                    && state.getBlock().getExplosionResistance() < 1200
                                    && state.getDestroySpeed(level, pos) != -1
                                    && calculator.shouldBlockExplode(explosion, level, pos, state, energy)) {
                                result.add(pos);
                            }
                        } else {
                            energy -= calculator.getBlockExplosionResistance(explosion, level, pos, state, level.getFluidState(pos))
                                    .map(resistance -> (resistance + 0.3F) * parameters.blockResistanceMultiplier() * (float) RAY_STEP)
                                    .orElse(0F);
                            if (energy > 0 && calculator.shouldBlockExplode(explosion, level, pos, state, energy)) result.add(pos);
                        }
                        // Protected blocks must not prevent advancement in the ignore-resistance policy.
                        px += direction.x * RAY_STEP;
                        py += direction.y * RAY_STEP;
                        pz += direction.z * RAY_STEP;
                        energy -= STEP_ATTENUATION;
                    }
                }
            }
        }
        return result;
    }

    private static ExplosionDamageCalculator calculator(Entity direct) {
        return direct == null ? new ExplosionDamageCalculator() : new EntityBasedExplosionDamageCalculator(direct);
    }

    private static List<Entity> affectedEntities(ServerLevel level, Vec3 center, DamageSource source,
                                                 ExplosionParameters parameters) {
        if (parameters.entityRadius() <= 0) return new ArrayList<>();
        Entity direct = source.getDirectEntity();
        // A caster is a potential victim. Bombs and familiars are only the direct carrier.
        Entity excluded = direct instanceof LivingEntity ? null : direct;
        return level.getEntities(excluded, new AABB(center, center).inflate(parameters.entityRadius()),
                entity -> eligible(entity, source, parameters));
    }

    private static boolean eligible(Entity entity, DamageSource source, ExplosionParameters parameters) {
        if (entity.isRemoved() || entity.isSpectator()) return false;
        Entity direct = source.getDirectEntity();
        if (entity == direct && !(direct instanceof LivingEntity)) return false;
        if (parameters.ignoreFriendly() && entity instanceof LivingEntity target
                && source.getEntity() instanceof LivingEntity owner && EntityHelper.isFriendly(target, owner)) return false;
        if (parameters.entityImpactMode() == EntityImpactMode.GIGA) {
            return entity instanceof LivingEntity living && living.isAlive() && !living.isInvulnerable();
        }
        return !entity.ignoreExplosion();
    }

    private static void damageEntities(Explosion explosion, Vec3 center, List<Entity> entities,
                                       ExplosionParameters parameters) {
        float radius = parameters.entityRadius();
        if (radius <= 0) return;
        Set<Entity> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        AABB bounds = new AABB(center, center).inflate(radius);
        for (Entity entity : entities) {
            if (!visited.add(entity) || !eligible(entity, explosion.getDamageSource(), parameters)) continue;
            if (!entity.getBoundingBox().intersects(bounds)) continue;
            double distance = Math.sqrt(entity.distanceToSqr(center));
            Vec3 push;
            float damage;
            if (parameters.entityImpactMode() == EntityImpactMode.GIGA) {
                damage = gigaDamage(parameters.centerDamage(), distance, radius);
                Vec3 direction = entity.position().subtract(center).normalize();
                push = new Vec3(direction.x * 2.5, direction.y, direction.z * 2.5);
            } else {
                if (distance > radius) continue;
                double targetY = entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY();
                Vec3 direction = new Vec3(entity.getX() - center.x, targetY - center.y, entity.getZ() - center.z).normalize();
                double impact = Math.max(0, (1 - distance / radius) * Explosion.getSeenPercent(center, entity));
                damage = standardDamage(parameters.centerDamage(), impact);
                double knockback = entity instanceof LivingEntity living
                        ? ProtectionEnchantment.getExplosionKnockbackAfterDampener(living, impact) : impact;
                push = direction.scale(knockback);
            }
            boolean pendingPlayerMotion = entity instanceof ServerPlayer && entity.hurtMarked;
            Vec3 velocityBeforeDamage = entity.getDeltaMovement();
            if (damage > 0) entity.hurt(explosion.getDamageSource(), damage);
            if (push.lengthSqr() <= 0) continue;
            if (entity instanceof ServerPlayer player) {
                if (player.isSpectator() || (player.isCreative() && player.getAbilities().flying)) continue;
                // Finish any prior knockback (or a hurt callback's velocity change) before the impulse.
                // A successful explosion hurt also marks motion without changing the baseline velocity.
                if (pendingPlayerMotion || !velocityBeforeDamage.equals(player.getDeltaMovement())) {
                    player.serverLevel().getChunkSource().broadcastAndSend(player, new ClientboundSetEntityMotionPacket(player));
                }
                player.hurtMarked = false;
                player.setDeltaMovement(player.getDeltaMovement().add(push));
                explosion.getHitPlayers().put(player, push);
            } else {
                entity.setDeltaMovement(entity.getDeltaMovement().add(push));
                entity.hurtMarked = true;
            }
        }
    }

    static float standardDamage(float centerDamage, double impact) {
        if (centerDamage <= 0) return 0;
        impact = Math.max(0, Math.min(1, impact));
        double damage = (impact * impact + impact) / 2 * Math.max(0, centerDamage - 1) + 1;
        return (float) Math.min(centerDamage, damage);
    }

    static float gigaDamage(float centerDamage, double distance, float radius) {
        if (centerDamage <= 0 || radius <= 0) return 0;
        double factor = 1 - distance / radius;
        return (float) Math.min(centerDamage, centerDamage * factor * factor);
    }

    private static void sendFeedback(ServerLevel level, Explosion explosion, Vec3 center, ExplosionParameters parameters) {
        boolean giga = parameters.feedbackMode() == FeedbackMode.GIGA;
        float pitch = giga ? 1 : (1 + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F;
        level.playSound(null, center.x, center.y, center.z,
                giga ? ModSounds.GIGA_BOMB_EXPLOSION.get() : SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS, giga ? 1 : 4, pitch);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center) >= 4096) continue;
            level.sendParticles(player, parameters.power() >= 2 && explosion.interactsWithBlocks()
                            ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION,
                    true, center.x, center.y, center.z, 1, 0, 0, 0, 0);
        }
        for (var player : explosion.getHitPlayers().keySet()) {
            if (player instanceof ServerPlayer serverPlayer) {
                // The client owns its current movement; only add the explosion's impulse.
                ModMessages.sentToPlayer(new ExplosionKnockbackS2CPacket(explosion.getHitPlayers().get(player)), serverPlayer);
            }
        }
    }

    /** Vanilla resolves this owner from the direct entity alone, which loses entity-based familiars. */
    private static final class AttributedExplosion extends Explosion {
        private AttributedExplosion(ServerLevel level, Vec3 center, DamageSource source, ExplosionParameters parameters) {
            super(level, source.getDirectEntity(), source, calculator(source.getDirectEntity()),
                    center.x, center.y, center.z, parameters.power(), parameters.causesFire(), parameters.blockInteraction());
        }

        @Override
        public @Nullable LivingEntity getIndirectSourceEntity() {
            return getDamageSource().getEntity() instanceof LivingEntity owner ? owner : super.getIndirectSourceEntity();
        }
    }
}
