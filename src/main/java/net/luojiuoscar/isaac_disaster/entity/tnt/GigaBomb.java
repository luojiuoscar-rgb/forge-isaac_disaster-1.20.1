package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GigaBomb extends IsaacBomb{

    public GigaBomb(Level pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner, int power, float scale) {
        super(pLevel, pX, pY, pZ, pOwner, power, scale);
    }

    public GigaBomb(EntityType<IsaacBomb> isaacBombEntityType, Level level) {
        super(isaacBombEntityType, level);
    }

    @Override
    protected void explode() {
        Level level = this.level();
        BlockPos origin = this.blockPosition();
        int radius = this.getPower(); // 你传入的范围

        float threshold = 3600000.0F; // 黑曜石是 3600000F，低于这个就可破坏

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = origin.offset(x, y, z);

                    // 球形范围，只破坏距离内的方块
                    if (pos.distSqr(origin) > radius * radius) continue;

                    BlockState state = level.getBlockState(pos);

                    // 空气跳过
                    if (state.isAir()) continue;

                    // 获取爆炸抗性
                    Explosion dummyExplosion = new Explosion(level, this, this.getX(), this.getY(), this.getZ(), this.getPower(), false, Explosion.BlockInteraction.KEEP);

                    float resistance = state.getExplosionResistance(level, pos, dummyExplosion);

                    if (resistance < threshold) {
                        // 80% 几率不掉落物品
                        boolean drop = level.random.nextDouble() > 0.8;
                        level.destroyBlock(pos, drop, this);
                    }
                }
            }
        }




        this.discard(); // 删除 TNT 实体
    }
}
