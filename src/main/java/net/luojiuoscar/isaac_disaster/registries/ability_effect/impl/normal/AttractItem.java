package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AttractItem implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();

        List<Entity> items = LevelHelper.selectBySquare(
                entity.level(),
                entity.getX(), entity.getY(), entity.getZ(),
                StatManager.getNearbyRange() * 0.75,
                e -> e instanceof ItemEntity
        );

        for (int i = 0; i < items.size(); i++){
            ItemEntity item = (ItemEntity) items.get(i);

            Vec3 playerPos = entity.position();
            Vec3 itemPos = item.position();

            Vec3 direction = playerPos.subtract(itemPos);

            double distance = direction.length();
            if (distance < 0.2) continue; // 防止抖动

            Vec3 motion = direction.normalize().scale(0.2); // 吸附速度

            item.setDeltaMovement(motion);
            item.hurtMarked = true; // 强制同步
        }

        return true;
    }
}
