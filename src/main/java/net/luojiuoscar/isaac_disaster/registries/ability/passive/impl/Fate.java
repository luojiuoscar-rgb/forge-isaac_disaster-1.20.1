package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Fate extends PassiveAbility {
    public Fate(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        triggerEffect(ModExecutableEffects.ETERNAL_HEART.get(), player, stack);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.FLY_TIME.apply(player, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.FLY_TIME.apply(player, -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.FLY_TIME.description(1),
                Component.translatable("item.isaac_disaster.fate.lore.1")
        );
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack) {
        List<Component> desc = new ArrayList<>();
        desc.add(EffectManager.ETERNAL_HEART.getExplainDesc());
        return desc;
    }
}
