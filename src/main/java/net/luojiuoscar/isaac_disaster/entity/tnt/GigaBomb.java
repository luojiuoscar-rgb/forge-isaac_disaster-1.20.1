package net.luojiuoscar.isaac_disaster.entity.tnt;

import com.google.common.collect.Sets;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GigaBomb extends IsaacBomb {

    public GigaBomb(Level pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner, int power, float scale) {
        super(pLevel, pX, pY, pZ, pOwner, power, scale);
    }

    public GigaBomb(EntityType<IsaacBomb> isaacBombEntityType, Level level) {
        super(isaacBombEntityType, level);
    }

    @Override
    protected void explode() {
        Level level = this.level();
        if (level.isClientSide) return;

        float radius = this.getPower();
        Vec3 center = this.position();

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.GIGA_BOMB_EXPLOSION.get(), SoundSource.BLOCKS,
                1.0F, 1.0F);

        Explosion explosion = new Explosion(
                level,
                this,
                this.getX(), this.getY(), this.getZ(),
                this.getPower(),
                false,
                Explosion.BlockInteraction.DESTROY
        );
        // 造成自定义破坏
        Set<BlockPos> custom = calculateExplosionNoResistance(level, center, radius);
        explosion.clearToBlow();
        explosion.getToBlow().addAll(custom);
        explosion.finalizeExplosion(true);

        // make damage
        damageEntities(level, center, radius);

        this.discard();
    }

    public Set<BlockPos> calculateExplosionNoResistance(Level level, Vec3 center, float radius) {
        Set<BlockPos> result = Sets.newHashSet();
        RandomSource random = level.random;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {

                    // 只取边界点
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) continue;

                    double dx = (j / 15.0F * 2.0F - 1.0F);
                    double dy = (k / 15.0F * 2.0F - 1.0F);
                    double dz = (l / 15.0F * 2.0F - 1.0F);

                    double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    dx /= len;
                    dy /= len;
                    dz /= len;

                    // 随机化初始能量
                    float power = radius * (0.7F + random.nextFloat() * 0.6F);

                    double px = center.x;
                    double py = center.y;
                    double pz = center.z;

                    // 沿射线推进
                    for (float step = 0.3F; power > 0.0F; power -= 0.225F) {

                        BlockPos pos = BlockPos.containing(px, py, pz);

                        if (!level.isInWorldBounds(pos)) break;

                        BlockState state = level.getBlockState(pos);

                        if (!state.isAir() && state.getFluidState().isEmpty()) {

                            float resistance = state.getBlock().getExplosionResistance();

                            if (resistance >= 1200) continue;

                            if (state.getDestroySpeed(level, pos) != -1) {
                                result.add(pos);
                            }
                        }

                        px += dx * 0.3;
                        py += dy * 0.3;
                        pz += dz * 0.3;
                    }
                }
            }
        }

        return result;
    }

    private void damageEntities(Level level, Vec3 center, float radius) {
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius
                )
        );

        for (LivingEntity entity : entities) {
            if (EntityHelper.isFriendly(entity, this.getOwner())) continue;
            if (entity.isInvulnerable()) continue;
            if (!entity.isAlive()) continue;

            double dist = Math.sqrt(entity.distanceToSqr(center));
            double t = dist / radius; // 0 ~ 1

            double factor = 1.0 - t;
            factor = factor * factor; // 二次衰减

            float damage = (float)(300.0 * factor);

            entity.hurt(level.damageSources().explosion(this, this.getOwner()), damage);

            Vec3 pushDir = entity.position().subtract(center).normalize();
            entity.push(pushDir.x * 2.5, pushDir.y, pushDir.z * 2.5);
        }
    }
}
