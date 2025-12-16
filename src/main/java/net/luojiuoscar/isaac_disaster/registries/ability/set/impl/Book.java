package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
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
    }

    @Override
    public void onRemoveEffect(Player player) {
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                Component.translatable("set.isaac_disaster.book").append(": ")
                        .append(Component.translatable("set.isaac_disaster.book.lore.1"))
        );
    }
}
