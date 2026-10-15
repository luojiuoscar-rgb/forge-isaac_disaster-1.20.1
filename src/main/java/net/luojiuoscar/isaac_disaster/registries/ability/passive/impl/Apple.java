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

import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.Apple.chance;

public final class Apple extends PassiveAbility {
    public Apple(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, 0.5);
        StatManager.addTriggerModule(player, ModTriggerModules.APPLE.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, -0.5);
        StatManager.addTriggerModule(player, ModTriggerModules.APPLE.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                StatManager.TEARS.description(0.5),
                Component.translatable("item.isaac_disaster.apple.lore.1",
                        DescriptionHelper.dynamicNumber(player == null ?
                                100.0 / 15 : chance(player) * 100)),
                Component.translatable("item.isaac_disaster.apple.lore.2", 4)
        );
    }
}
