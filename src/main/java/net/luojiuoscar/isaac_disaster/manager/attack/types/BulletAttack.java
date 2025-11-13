package net.luojiuoscar.isaac_disaster.manager.attack.types;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.IsaacGetBulletCountEvent;
import net.luojiuoscar.isaac_disaster.manager.attack.IAttackType;
import net.luojiuoscar.isaac_disaster.manager.id.AttackTypeId;
import net.luojiuoscar.isaac_disaster.manager.id.BulletColorId;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.Set;

public class BulletAttack implements IAttackType {
    @Override
    public int getId() {
        return AttackTypeId.BULLET.getId();
    }

    @Override
    public double getPriority() {
        return AttackTypeId.BULLET.getPriority();
    }

    @Override
    public void makeSound(LivingEntity entity){
        entity.level().playSound(
                null,
                entity.blockPosition(),
                ModSounds.TEAR_BULLET_SHOT.get(),
                SoundSource.PLAYERS,
                0.6f,
                1.0f
        );
    }

    public void handleAttack(LivingEntity entity, int colorId, Set<Integer> hitEffects) {
        int count = entity instanceof Player player ? getBulletCount(player) : 1;

        if (count <= 1) {
            shotBullet(entity, colorId, hitEffects);
        } else if (count == 2) {
            shot2Bullet(entity, colorId, hitEffects);
        } else {
            count = Math.min(count, 17);
            float angleInterval = Math.max(11 - count, 5) * 2;
            float curAngle = -angleInterval * (count - 1) / 2.0f;

            for (int i = 0; i < count; i++) {
                shotBullet(entity, entity.getXRot(), entity.getYRot() + curAngle, colorId, hitEffects);
                curAngle += angleInterval;
            }
        }
    }

    // =================== 子弹发射 ===================
    private TearBullet createBullet(LivingEntity shooter, Vec3 spawnPos, float xRot, float yRot, int colorId, Set<Integer> hitEffects) {
        double width = shooter.getBbWidth();
        double forwardOffset = 0.4 * (width / 0.6);
        Vec3 look = Vec3.directionFromRotation(xRot, yRot);
        Vec3 adjustedPos = spawnPos.add(look.scale(forwardOffset));

        TearBullet bullet = new TearBullet(
                shooter.level(),
                shooter,
                getBulletLiftTime(shooter),
                getBulletSpeed(shooter),
                getBulletScale(shooter),
                getDamage(shooter),
                xRot,
                yRot
        );

        bullet.setSpectral(isSpectral(shooter));
        bullet.setPiercing(isPiercing(shooter));
        bullet.setHoming(isHoming(shooter));
        bullet.setControllable(isControllable(shooter));

        bullet.setHitEffectIds(hitEffects);

        bullet.setColor(BulletColorId.getColorById(colorId));
        bullet.setAlpha(BulletColorId.getAlphaById(colorId));

        bullet.moveTo(adjustedPos.x, adjustedPos.y, adjustedPos.z, yRot, xRot);
        bullet.setDeltaMovement(look.scale(getBulletSpeed(shooter)));

        if (!shooter.level().isClientSide)
            MinecraftForge.EVENT_BUS.post(new TearBulletShootEvent(bullet.getOwner(), getId(), hitEffects, bullet));

        return bullet;
    }

    private void shotBullet(LivingEntity entity, int colorId, Set<Integer> hitEffects) {
        shotBullet(entity, entity.getXRot(), entity.getYRot(), colorId, hitEffects);
    }

    private void shotBullet(LivingEntity entity, float xRot, float yRot, int colorId, Set<Integer> hitEffects) {
        if (entity.level().isClientSide()) return;
        Vec3 eyePos = entity.getEyePosition().add(0, entity.getBbHeight() * -0.15, 0);
        TearBullet bullet = createBullet(entity, eyePos, xRot, yRot, colorId, hitEffects);
        entity.level().addFreshEntity(bullet);
    }

    private void shot2Bullet(LivingEntity entity, int colorId, Set<Integer> hitEffects) {
        if (entity.level().isClientSide()) return;
        Vec3 look = entity.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 eyePos = entity.getEyePosition().add(0, entity.getBbHeight() * -0.15, 0);

        TearBullet bullet1 = createBullet(
                entity, eyePos.add(right.scale(0.25)), entity.getXRot(), entity.getYRot(), colorId, hitEffects);
        TearBullet bullet2 = createBullet(
                entity, eyePos.add(right.scale(-0.25)), entity.getXRot(), entity.getYRot(), colorId, hitEffects);

        entity.level().addFreshEntity(bullet1);
        entity.level().addFreshEntity(bullet2);
    }

    // =================== 基础属性 ===================
    private double getBulletSpeed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_SPEED.get());
        return attr != null ? Math.max(attr.getValue(), 0.1) : 1.0;
    }

    private int getBulletLiftTime(LivingEntity entity) {
        double speed = getBulletSpeed(entity);
        AttributeInstance rangeAttr = entity.getAttribute(ModAttributes.BULLET_RANGE.get());
        double range = rangeAttr != null ? rangeAttr.getValue() : 18.0;
        return (int) Math.min(Math.max(1, range / speed), 200);
    }

    private float getBulletScale(LivingEntity entity) {
        double damage = entity.getAttribute(Attributes.ATTACK_DAMAGE) != null ? entity.getAttribute(Attributes.ATTACK_DAMAGE).getValue() : 1.0;
        AttributeInstance extraAttr = entity.getAttribute(ModAttributes.BULLET_SCALE.get());
        float extra = extraAttr != null ? (float) extraAttr.getValue() : 0f;
        float scale = 1.0f;
        if (damage <= 198) {
            scale *= (float) Math.log10(9 + damage / 2);
        } else {
            scale *= Math.min(4f, (float) (2 + Math.log10((damage - 198) / 20)));
        }
        return scale + extra;
    }

    private float getDamage(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        return attr != null ? (float) attr.getValue() : 1f;
    }

    // =================== count ===================
    private int getBulletCount(Player player){
        AttributeInstance bulletCount = player.getAttribute(ModAttributes.BULLET_COUNT.get());
        int[] count = {bulletCount == null ? 1 : (int) bulletCount.getValue()};

        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passive -> {
            int innerEye = passive.getItemCountFromAll(player, ItemId.THE_INNER_EYE.getId());
            int mutantSpider = passive.getItemCountFromAll(player, ItemId.MUTANT_SPIDER.getId());
            int perfectVision = passive.getItemCountFromAll(player, ItemId.PERFECT_VISION.getId());

            if (perfectVision >= 1){
                if (innerEye + mutantSpider == 0){
                    count[0] += 1;
                } else {
                    count[0] += perfectVision - 1;
                }
            }

            if (innerEye + mutantSpider > 0){
                count[0] += innerEye + 2 * mutantSpider + 1;
            }
        });

        IsaacGetBulletCountEvent event = new IsaacGetBulletCountEvent(player, count[0]);
        MinecraftForge.EVENT_BUS.post(event);

        return event.getCount();
    }
}
