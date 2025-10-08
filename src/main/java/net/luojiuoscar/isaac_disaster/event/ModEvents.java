package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;



@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        // 给所有生物添加Scale
        event.getTypes().forEach(type -> {
            if (Mob.class.isAssignableFrom(type.getBaseClass()) || type == EntityType.PLAYER) {
                event.add(type, ModAttributes.SCALE.get());
            }
        });

        // 给玩家追加的属性
        if (!event.has(EntityType.PLAYER, ModAttributes.BULLET_SPEED.get())) {
            event.add(EntityType.PLAYER, ModAttributes.BULLET_SPEED.get());
            event.add(EntityType.PLAYER, ModAttributes.BULLET_RANGE.get());
            event.add(EntityType.PLAYER, ModAttributes.TEARS.get());
            event.add(EntityType.PLAYER, ModAttributes.TEARS_CORRECTION.get());
            event.add(EntityType.PLAYER, ModAttributes.BULLET_ALPHA.get());
            event.add(EntityType.PLAYER, ModAttributes.BULLET_COLOR.get());
            event.add(EntityType.PLAYER, ModAttributes.BULLET_FILTER.get());
            event.add(EntityType.PLAYER, ModAttributes.BLOCK_BREAKING_SPEED.get());
            event.add(EntityType.PLAYER, ModAttributes.BULLET_SCALE .get());
        }
    }
}
