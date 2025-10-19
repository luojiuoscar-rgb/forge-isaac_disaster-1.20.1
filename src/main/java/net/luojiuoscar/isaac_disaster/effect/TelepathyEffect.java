package net.luojiuoscar.isaac_disaster.effect;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TelepathyEffect extends MobEffect {
    private static final UUID TELEPATHY_EFFECT_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:telepathy_effect").getBytes());

    protected TelepathyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        if (!(entity instanceof ServerPlayer player)) {
            entity.removeEffect(ModEffects.TELEPATHY.get());
            return;
        }

        StatManager.modifyHoming(player, 1);
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.addFilter(ColorManager.SPOON_BENDER_FILTER, player)
        );

        AttributeInstance attr = player.getAttribute(ModAttributes.BULLET_RANGE.get());
        if (attr != null) {
            // 先移除旧的同 UUID 的 modifier（防止重复叠加）
            attr.removeModifier(TELEPATHY_EFFECT_UUID);
            // 计算要增加的数值
            double addValue = StatManager.getRangeBonus() * (amplifier + 2);
            AttributeModifier modifier = new AttributeModifier(
                    TELEPATHY_EFFECT_UUID,
                    "telepathy.range_bonus",
                    addValue,
                    AttributeModifier.Operation.ADDITION
            );
            attr.addPermanentModifier(modifier);
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;

        StatManager.modifyHoming(player, -1);
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.removeFilter(ColorManager.SPOON_BENDER_FILTER, player)
        );

        AttributeInstance attr = player.getAttribute(ModAttributes.BULLET_RANGE.get());
        if (attr != null) {
            attr.removeModifier(TELEPATHY_EFFECT_UUID);
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }
}
