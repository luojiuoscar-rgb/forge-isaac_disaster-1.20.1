package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.Virgo.chance;

public final class Virgo extends PassiveAbility {
    public Virgo(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {}

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.PILL_QUALITY.apply(player, 1);
        StatManager.addTriggerModule(player, ModTriggerModules.VIRGO.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.PILL_QUALITY.apply(player, -1);
        StatManager.addTriggerModule(player, ModTriggerModules.VIRGO.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(Component.translatable("item.isaac_disaster.virgo.lore.1",
                DescriptionHelper.dynamicNumber(player == null ? 25 : chance(player) * 100)),
                Component.translatable("item.isaac_disaster.virgo.lore.2"));
    }
}
