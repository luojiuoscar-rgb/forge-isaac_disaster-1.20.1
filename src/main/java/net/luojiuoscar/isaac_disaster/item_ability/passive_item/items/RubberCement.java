package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
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

    public static void bounceOnBlock(IsaacAttackHitBlockEvent event) {
        if (!(event.getDirectSource() instanceof TearBullet bullet)) return;
        BlockHitResult hit = event.getHitResult();

        Vec3 motion = bullet.getVelocity();
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal()).normalize();

        double speed = motion.length();
        if (speed < 1e-6) return;

        // 反射方向
        Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));

        // 更新速度为反射向量
        bullet.setVelocity(reflected);

        // 防止立即再次撞到同一个方块
        bullet.move(MoverType.SELF, reflected.scale(0.1));
        event.setCanceled(true);
    }


    public static void bounceOnEntity(IsaacAttackAfterHitEvent event) {
        if (!(event.getDirectSource() instanceof TearBullet bullet)) return;
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
                // 计算方向
                Vec3 dir = target.getEyePosition().subtract(bullet.position()).normalize();
                bullet.setVelocity(dir.scale(speed));
                event.setDiscardAfterHit(false);
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
        event.setDiscardAfterHit(false);
    }


}
