package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RockBottom implements IAbilityEffect {
    public static final List<Attribute> TARGET_ATTRIBUTES = List.of(
            Attributes.ATTACK_DAMAGE,
            Attributes.MOVEMENT_SPEED,
            Attributes.LUCK,
            ModAttributes.BULLET_SPEED.get(),
            ModAttributes.TEARS.get(),
            ModAttributes.TEARS_CORRECTION.get(),
            ModAttributes.BULLET_RANGE.get()
    );

    public static final List<ResourceLocation> ATTR_DATA_RESOURCE = new ArrayList<>();

    static {
        for (Attribute attr : TARGET_ATTRIBUTES){
            String safePath = attr.getDescriptionId().replace('.', '_');
            ResourceLocation res = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, safePath);
            ATTR_DATA_RESOURCE.add(res);
        }
    }

    public static final UUID ROCK_BOTTOM_ATTR_MODIFIER_UUID =
            UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":" + "rock_bottom_attribute_modifier_uuid")
                    .getBytes(StandardCharsets.UTF_8));


    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return true;

        for (int i = 0; i < TARGET_ATTRIBUTES.size(); i++){
            Attribute attr = TARGET_ATTRIBUTES.get(i);
            AttributeInstance inst = player.getAttribute(attr);
            double totalVal = player.getAttributeBaseValue(attr); // 基值

            if (inst == null) continue;

            Set<AttributeModifier> modifiers = inst.getModifiers();

            // 计算所有加区数值
            for (AttributeModifier modifier : modifiers){
                if (modifier.getOperation() != AttributeModifier.Operation.ADDITION) continue;
                if (modifier.getId().equals(ROCK_BOTTOM_ATTR_MODIFIER_UUID)) continue; // skip self

                totalVal += modifier.getAmount();
            }

            // 更新最大值
            double actualVal = totalVal;
            ResourceLocation dataId = ATTR_DATA_RESOURCE.get(i);

            player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(
                    extraData -> {
                        Double highest = extraData.getDouble(dataId);

                        // 若不存在，则存储为当前值
                        if (highest == null){
                            extraData.setDouble(dataId ,actualVal);
                            return;
                        }
                        // 获取补正&存储
                        if (actualVal > highest){
                            extraData.setDouble(dataId, actualVal);
                            highest = actualVal;
                        }

                        // 提供modifier&生效
                        StatManager.setModifier(
                                player,
                                ROCK_BOTTOM_ATTR_MODIFIER_UUID,
                                attr,
                                highest - actualVal, // 补齐差值
                                0.0,
                                null,
                                0
                        );
                    }
            );
        }

        return true;
    }
}
