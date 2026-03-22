package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Placebo extends ActiveAbility {
    private final IAbilityEffect effect = ModAbilityEffects.USE_PILL.get();

    public Placebo(int id, int level) {
        super(id, level);
    }

    @Override
    protected IAbilityEffect getAbilityEffect() {
        return effect;
    }

    @Override
    protected AbilityEffectContext getCtx(ServerPlayer player, ItemStack stack, @Nullable InteractionHand hand, int amplifier) {
        var ctx = super.getCtx(player, stack, hand, amplifier);

        ItemStack pill = null;
        if (hand == InteractionHand.MAIN_HAND){
            pill = player.getOffhandItem();
        }else if (hand == InteractionHand.OFF_HAND){
            pill = player.getMainHandItem();
        }
        if (pill == null) return ctx;

        ctx.set(ContextKeys.ITEM, pill.getItem());

        return ctx;
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.placebo.lore.1"),
                Component.translatable("item.isaac_disaster.placebo.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.placebo.synergy.car_battery.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
