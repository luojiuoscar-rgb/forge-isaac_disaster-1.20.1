package net.luojiuoscar.isaac_disaster.system;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class ScaleUtils {

    public static float getScale(LivingEntity entity) {

        if (entity == null || entity.getAttributes() == null) return 1.0F;

        AttributeInstance attr = entity.getAttribute(ModAttributes.SCALE.get());
        return attr != null ? (float) attr.getValue() : 1.0F;
    }

    public static void setScale(LivingEntity entity, double newScale) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.SCALE.get());
        if (attr != null) {
            attr.setBaseValue(newScale);
            refreshScale(entity);
        }
    }

    public static void refreshScale(LivingEntity entity) {
        entity.refreshDimensions();
    }
}
