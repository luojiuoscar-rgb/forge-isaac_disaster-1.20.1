package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BabylonEffect extends MobEffect {
    private static final UUID BABYLON_DAMAGE_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:babylon_damage").getBytes());
    private static final UUID BABYLON_SPEED_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:babylon_speed").getBytes());


    public BabylonEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);

        if (!(entity instanceof ServerPlayer player)){
            entity.removeEffect(ModEffects.BABYLON.get());
            return;
        }

        AttributeInstance attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            // 先移除旧的同 UUID 的 modifier
            attr.removeModifier(BABYLON_DAMAGE_UUID);
            // 计算要增加的数值
            double addValue = StatManager.DAMAGE.getBonus() * (1.5 + amplifier);
            AttributeModifier modifier = new AttributeModifier(
                    BABYLON_DAMAGE_UUID,
                    "babylon.damage_bonus",
                    addValue,
                    AttributeModifier.Operation.ADDITION
            );
            attr.addPermanentModifier(modifier);
        }

        AttributeInstance attr2 = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr2 != null) {
            // 先移除旧的同 UUID 的 modifier
            attr2.removeModifier(BABYLON_SPEED_UUID);
            // 计算要增加的数值
            double addValue = StatManager.MOVEMENT_SPEED.getBonus()  * (1 + amplifier * 0.5);
            AttributeModifier modifier = new AttributeModifier(
                    BABYLON_SPEED_UUID,
                    "babylon.speed_bonus",
                    addValue,
                    AttributeModifier.Operation.ADDITION
            );
            attr2.addPermanentModifier(modifier);
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);

        if (!(entity instanceof ServerPlayer player)){
            return;
        }

        AttributeInstance instance1 = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance1 != null) {
            instance1.removeModifier(BABYLON_DAMAGE_UUID);
        }

        AttributeInstance instance2 = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance2 != null) {
            instance2.removeModifier(BABYLON_SPEED_UUID);
        }
    }


    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        // 原有伤害逻辑
        return (amplifier + 1) * StatManager.DAMAGE.getBonus();
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        // 不可被牛奶或药水清除
        return Collections.emptyList();
    }
}
