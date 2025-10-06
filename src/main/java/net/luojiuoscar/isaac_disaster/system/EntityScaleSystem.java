package net.luojiuoscar.isaac_disaster.system;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntityScaleSystem {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new EntityScaleSystem());
    }

    /**
     * 给所有生物添加自定义属性 SCALE
     */
    @SubscribeEvent
    public void onEntityAttributeModify(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(type -> {
            if (event.has(type, ModAttributes.SCALE.get())) return;
            event.add(type, ModAttributes.SCALE.get());
        });
    }


    /**
     * 外部调用刷新实体尺寸
     */
    public static void refreshEntityScale(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            entity.refreshDimensions();
        }
    }
}
