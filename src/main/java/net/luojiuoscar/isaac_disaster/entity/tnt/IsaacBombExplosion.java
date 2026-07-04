package net.luojiuoscar.isaac_disaster.entity.tnt;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;
import java.util.Set;

/**
 * Runs Isaac bomb explosions through a vanilla-like algorithm with separately configurable damage
 * and block-breaking profiles.
 *
 * <p>The implementation intentionally mirrors Minecraft's explosion math: block positions are
 * sampled from a 16x16x16 ray shell, entity damage uses exposure from {@link Explosion#getSeenPercent(Vec3, Entity)},
 * and knockback is dampened by blast protection. The bomb's center damage replaces vanilla's
 * radius-derived center damage without changing the distance and exposure curve.</p>
 */
public final class IsaacBombExplosion {
    private static final int RAY_GRID_SIZE = 16;
    private static final float RAY_STEP = 0.3F;
    private static final float RAY_STEP_ATTENUATION = 0.22500001F;

    private IsaacBombExplosion() {
    }

    /**
     * Explodes a regular Isaac bomb using its current profile values.
     *
     * @param bomb bomb entity to explode
     */
    public static void explode(IsaacBomb bomb) {
        Level level = bomb.level();
        if (level.isClientSide) return;

        Vec3 center = new Vec3(bomb.getX(), bomb.getY(0.0625D), bomb.getZ());
        BombData profile = bomb.getProfile();
        float explosionRadius = bomb.getPower();

        Explosion explosion = new Explosion(
                level,
                bomb,
                level.damageSources().explosion(bomb, bomb.getOwner()),
                new EntityBasedExplosionDamageCalculator(bomb),
                center.x,
                center.y,
                center.z,
                explosionRadius,
                profile.causesFire(),
                profile.blockInteraction()
        );

        if (ForgeEventFactory.onExplosionStart(level, explosion)) return;

        level.gameEvent(bomb, net.minecraft.world.level.gameevent.GameEvent.EXPLODE, center);

        explosion.clearToBlow();
        if (explosion.interactsWithBlocks()) {
            explosion.getToBlow().addAll(calculateBlocksToBlow(level, explosion, bomb, center));
        }

        float damageRadius = bomb.getDamageRadius();
        List<Entity> affectedEntities = getAffectedEntities(level, bomb, center, damageRadius);
        ForgeEventFactory.onExplosionDetonate(level, explosion, affectedEntities, damageRadius * 2.0F);

        damageEntities(explosion, bomb, center, affectedEntities);
        explosion.finalizeExplosion(true);
        syncExplosionToClients(level, explosion, center, explosionRadius);
    }

    /**
     * Calculates destroyed blocks with vanilla ray sampling, but using the profile's block power and
     * resistance multiplier instead of the damage radius.
     */
    public static Set<BlockPos> calculateBlocksToBlow(Level level, Explosion explosion,
                                                      IsaacBomb bomb, Vec3 center) {
        Set<BlockPos> result = Sets.newHashSet();
        ExplosionDamageCalculator calculator = new EntityBasedExplosionDamageCalculator(explosion.getExploder());

        for (int xIndex = 0; xIndex < RAY_GRID_SIZE; ++xIndex) {
            for (int yIndex = 0; yIndex < RAY_GRID_SIZE; ++yIndex) {
                for (int zIndex = 0; zIndex < RAY_GRID_SIZE; ++zIndex) {
                    if (!isRayShell(xIndex, yIndex, zIndex)) continue;

                    Vec3 direction = normalizeRayDirection(xIndex, yIndex, zIndex);
                    float power = bomb.getBlockPower() * (0.7F + level.random.nextFloat() * 0.6F);
                    double x = center.x;
                    double y = center.y;
                    double z = center.z;

                    while (power > 0.0F) {
                        BlockPos pos = BlockPos.containing(x, y, z);
                        if (!level.isInWorldBounds(pos)) break;

                        BlockState blockState = level.getBlockState(pos);
                        FluidState fluidState = level.getFluidState(pos);

                        power -= getResistanceCost(calculator, explosion, level, pos, blockState, fluidState,
                                bomb.getBlockResistanceMultiplier());

                        if (power > 0.0F && calculator.shouldBlockExplode(explosion, level, pos, blockState, power)) {
                            result.add(pos);
                        }

                        x += direction.x * RAY_STEP;
                        y += direction.y * RAY_STEP;
                        z += direction.z * RAY_STEP;
                        power -= RAY_STEP_ATTENUATION;
                    }
                }
            }
        }

        return result;
    }

    private static boolean isRayShell(int xIndex, int yIndex, int zIndex) {
        return xIndex == 0 || xIndex == RAY_GRID_SIZE - 1
                || yIndex == 0 || yIndex == RAY_GRID_SIZE - 1
                || zIndex == 0 || zIndex == RAY_GRID_SIZE - 1;
    }

    private static Vec3 normalizeRayDirection(int xIndex, int yIndex, int zIndex) {
        double x = (xIndex / (RAY_GRID_SIZE - 1.0D) * 2.0D - 1.0D);
        double y = (yIndex / (RAY_GRID_SIZE - 1.0D) * 2.0D - 1.0D);
        double z = (zIndex / (RAY_GRID_SIZE - 1.0D) * 2.0D - 1.0D);

        return new Vec3(x, y, z).normalize();
    }

    private static float getResistanceCost(ExplosionDamageCalculator calculator, Explosion explosion, Level level,
                                           BlockPos pos, BlockState blockState, FluidState fluidState,
                                           float resistanceMultiplier) {
        return calculator.getBlockExplosionResistance(explosion, level, pos, blockState, fluidState)
                .map(resistance -> (resistance + 0.3F) * resistanceMultiplier * RAY_STEP)
                .orElse(0.0F);
    }

    private static List<Entity> getAffectedEntities(Level level, IsaacBomb bomb, Vec3 center, float radius) {
        float diameter = radius * 2.0F;
        int minX = Mth.floor(center.x - diameter - 1.0D);
        int maxX = Mth.floor(center.x + diameter + 1.0D);
        int minY = Mth.floor(center.y - diameter - 1.0D);
        int maxY = Mth.floor(center.y + diameter + 1.0D);
        int minZ = Mth.floor(center.z - diameter - 1.0D);
        int maxZ = Mth.floor(center.z + diameter + 1.0D);

        return level.getEntities(
                bomb,
                new AABB(minX, minY, minZ, maxX, maxY, maxZ)
        );
    }

    private static void damageEntities(Explosion explosion, IsaacBomb bomb, Vec3 center, List<Entity> entities) {
        float diameter = bomb.getDamageRadius() * 2.0F;
        if (diameter <= 0.0F) return;

        for (Entity entity : entities) {
            if (entity.ignoreExplosion()) continue;

            double normalizedDistance = Math.sqrt(entity.distanceToSqr(center)) / diameter;
            if (normalizedDistance > 1.0D) continue;

            double offsetX = entity.getX() - center.x;
            double offsetY = getExplosionTargetY(entity) - center.y;
            double offsetZ = entity.getZ() - center.z;
            double length = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
            if (length == 0.0D) continue;

            Vec3 direction = new Vec3(offsetX / length, offsetY / length, offsetZ / length);
            double exposure = Explosion.getSeenPercent(center, entity);
            double impact = (1.0D - normalizedDistance) * exposure;
            float damage = getExplosionDamage(impact, bomb.getCenterDamage());

            entity.hurt(explosion.getDamageSource(), damage);

            double knockback = impact;
            if (entity instanceof LivingEntity livingEntity) {
                knockback = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, impact);
            }

            Vec3 knockbackVector = direction.scale(knockback);
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackVector));
            if (shouldTrackPlayerKnockback(entity)) {
                Player player = (Player) entity;
                explosion.getHitPlayers().put(player, knockbackVector);
            }
        }
    }

    private static boolean shouldTrackPlayerKnockback(Entity entity) {
        if (!(entity instanceof Player player)) return false;
        return !player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying);
    }

    private static double getExplosionTargetY(Entity entity) {
        return entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY();
    }

    private static float getExplosionDamage(double impact, float centerDamage) {
        return (float) ((impact * impact + impact) / 2.0D * Math.max(0.0F, centerDamage - 1.0F) + 1.0D);
    }

    private static void syncExplosionToClients(Level level, Explosion explosion, Vec3 center, float radius) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(center.x, center.y, center.z) >= 4096.0D) continue;

            player.connection.send(new ClientboundExplodePacket(
                    center.x,
                    center.y,
                    center.z,
                    radius,
                    explosion.getToBlow(),
                    explosion.getHitPlayers().get(player)
            ));
        }
    }
}
