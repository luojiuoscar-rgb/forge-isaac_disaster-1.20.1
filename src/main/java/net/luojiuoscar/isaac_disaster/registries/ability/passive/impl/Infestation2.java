package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public final class Infestation2 extends PassiveAbility {
    public Infestation2(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.INFESTATION_2.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.SPIDER_BABY.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.INFESTATION_2.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbilities.SPIDER_BABY.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(Component.translatable("item.isaac_disaster.infestation_2.lore.1"),
                Component.translatable("item.isaac_disaster.infestation_2.lore.2"));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.SPIDER_BABY.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.SPIDER_BABY.get().getExtraDesc();
    }
}
