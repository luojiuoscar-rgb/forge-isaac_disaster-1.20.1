package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
        if (level.isClientSide) return;

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
        explosion.explode();
        explosion.finalizeExplosion(true);

        double radius = this.getPower(); // 你希望的生物伤害范围
        Vec3 center = this.position();

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius
                )
        );

        for (LivingEntity entity : entities) {
            // 跳过自己
            if (EntityHelper.isFriendly(entity, this.getOwner())) continue;
            if (entity.isInvulnerable()) continue;
            if (!entity.isAlive()) continue;

            // 固定伤害 300，无衰减
            entity.hurt(level.damageSources().explosion(this, this.getOwner()), 300.0f);

            // 推力
            Vec3 pushDir = entity.position().subtract(center).normalize();
            entity.push(pushDir.x * 2.5, pushDir.y, pushDir.z * 2.5);
        }

        this.discard();
    }
}
