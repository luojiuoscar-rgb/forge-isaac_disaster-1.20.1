package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class Book extends SetAbility {
    public Book(int id) {
        super(Component.translatable("set.isaac_disaster.book"), id, 3);
    }

    @Override
    public void onFirstObtain(Player player){
    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.addTriggerModule(player, ModTriggerModules.BOOK_MOMS_KNIFE.getId(), 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.addTriggerModule(player, ModTriggerModules.BOOK_MOMS_KNIFE.getId(), -1);
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                Component.translatable("set.isaac_disaster.book").append(": ")
                        .append(Component.translatable("set.isaac_disaster.book.lore.1"))
        );
    }
}
