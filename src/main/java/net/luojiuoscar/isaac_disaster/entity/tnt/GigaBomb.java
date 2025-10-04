package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        BlockPos origin = this.blockPosition();
        int radius = this.getPower(); // 你传入的范围

        // 产生伤害
        dealExplosionDamage(level, this.position(), this.getPower());

        // 破坏方块
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


        Minecraft.getInstance().player.sendSystemMessage(Component.literal("x: "+this.getX()+"y: "+this.getY()
        +"z: "+this.getZ()+"level: "+level));

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.GIGA_BOMB_EXPLOSION.get(), SoundSource.BLOCKS,
                4.0F, 1.0F);

        this.discard(); // 删除 TNT 实体
    }

    private void dealExplosionDamage(Level level, Vec3 center, float radius) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius));

        for (LivingEntity entity : entities) {
            if (entity.isInvulnerable()) continue;

            double distance = entity.position().distanceTo(center);
            if (distance > radius) continue;

            // 距离衰减因子
            double factor = Math.exp(-2.0 * distance / radius); // 距离越远衰减越快

            float damage = (float) (350.0f * factor);
            entity.hurt(level.damageSources().explosion(this, null), damage);

            // 推开效果（方向 = 实体位置 - 爆炸中心）
            Vec3 pushDir = entity.position().subtract(center).normalize();
            double pushStrength = 3.0 * factor + 1.5; // 即使远处也有一定推力
            entity.push(pushDir.x * pushStrength, pushDir.y * pushStrength * 0.5, pushDir.z * pushStrength);
        }

        // 删除掉落物
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class,
                new AABB(center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius));
        for (ItemEntity item : itemEntities) {
            item.discard();
        }
    }
}
