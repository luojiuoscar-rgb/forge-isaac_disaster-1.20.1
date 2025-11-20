package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.attack.type.LaserAttack;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RubberCement implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.RUBBER_CEMENT.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.rubber_cement.lore.1")
        );
    }

    /** 方块碰撞弹射 */
    public static void bounceOnBlock(IsaacAttackHitBlockEvent event) {
        BlockHitResult hit = event.getHitResult();
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal()).normalize();

        // ---------------- TearBullet ----------------
        if (event.getBulletObject() instanceof TearBullet bullet) {
            Vec3 motion = bullet.getVelocity();

            double speed = motion.length();
            if (speed < 1e-6) return;

            Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));
            bullet.setVelocity(reflected);
            bullet.move(MoverType.SELF, reflected.scale(1));
        }
        // ---------------- LaserProjectile ----------------
        else if (event.getBulletObject() instanceof LaserAttack.LaserProjectile laser) {
            Vec3 direction = laser.direction;

            Vec3 reflected = direction.subtract(normal.scale(2 * direction.dot(normal)));
            laser.direction = reflected;
            Vec3 prePos = laser.position;
            laser.position = laser.position.add(reflected.scale(laser.step));

            IsaacDisaster.LOGGER.info("Laser Bounce: normal:{}, direction:{} -> {}, position: {} -> {}",
                    normal, direction, reflected, prePos, laser.position);
        }


        event.setCanceled(true);
    }

    /** 实体碰撞弹射，只处理 TearBullet */
    public static void bounceOnEntity(IsaacAttackAfterHitEvent event) {
        if (!(event.getBulletObject() instanceof TearBullet bullet)) return;
        if (bullet.isPiercing) return;

        double speed = bullet.getVelocity().length();

        // 50% 概率弹向最近敌对生物
        if (Math.random() < 0.5) {
            LivingEntity target = EntityHelper.findNearestTrackingTarget(
                    bullet.level(),
                    bullet.getOwner(),
                    bullet.position(),
                    TearBullet.HOMING_RANGE,
                    e -> !bullet.getDamagedEntities().contains(e.getUUID())
            );

            if (target != null) {
                Vec3 dir = target.getEyePosition().subtract(bullet.position()).normalize();
                bullet.setVelocity(dir.scale(speed));
                event.setCanceled(true);
                return;
            }
        }

        // 否则随机弹射
        double theta = Math.random() * 2 * Math.PI;
        double phi = Math.acos(2 * Math.random() - 1);
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        Vec3 randomDir = new Vec3(x, y, z).normalize();

        bullet.setVelocity(randomDir.scale(speed));
        event.setCanceled(true);
    }
}
