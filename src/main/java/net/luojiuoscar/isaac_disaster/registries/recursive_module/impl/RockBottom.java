package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RockBottom implements IRecursiveModule {

    private static final List<Attribute> TARGET_ATTRIBUTES = List.of(
            Attributes.ATTACK_DAMAGE,
            Attributes.MOVEMENT_SPEED,
            Attributes.LUCK,
            ModAttributes.BULLET_SPEED.get(),
            ModAttributes.TEARS.get(),
            ModAttributes.TEARS_CORRECTION.get(),
            ModAttributes.BULLET_RANGE.get()
    );

    private static final List<ResourceLocation> ATTR_DATA_RESOURCE = new ArrayList<>();

    static {
        for (Attribute attr : TARGET_ATTRIBUTES){
            String safePath = attr.getDescriptionId().replace('.', '_');
            ResourceLocation res = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, safePath);
            ATTR_DATA_RESOURCE.add(res);
        }
    }

    private static final UUID ROCK_BOTTOM_ATTR_MODIFIER_UUID =
            UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":" + "rock_bottom_attribute_modifier_uuid")
                    .getBytes(StandardCharsets.UTF_8));


    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        if (!(entity instanceof ServerPlayer player)) return;

        for (int i = 0; i < TARGET_ATTRIBUTES.size(); i++){
            Attribute attr = TARGET_ATTRIBUTES.get(i);
            AttributeInstance inst = entity.getAttribute(attr);
            double totalVal = entity.getAttributeBaseValue(attr); // 基值

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

            entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(
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

    }

    @Override
    public void handleRemove(LivingEntity entity) {
        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                effectModules -> {
                    var recursiveModuleInstance = effectModules.getRecursiveModuleQueue().get(ModRecursiveModule.ROCK_BOTTOM.getId());

                    // 如果不存在或者为0
                    if (recursiveModuleInstance == null || recursiveModuleInstance.stacks == 0){

                        entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(
                                extraData -> {
                                    // 移除所有补正记录
                                    for (ResourceLocation id : ATTR_DATA_RESOURCE){
                                        extraData.removeDouble(id);
                                    }
                                }
                        );
                    }

                }
        );

        // 移除所有的Modifier
        if (entity instanceof ServerPlayer player){
            for (Attribute targetAttribute : TARGET_ATTRIBUTES) {
                StatManager.removeModifier(player, player.getAttribute(targetAttribute),
                        ROCK_BOTTOM_ATTR_MODIFIER_UUID
                );
            }
        }

    }
}
