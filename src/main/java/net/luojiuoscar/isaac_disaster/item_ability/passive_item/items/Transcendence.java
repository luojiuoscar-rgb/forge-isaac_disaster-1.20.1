package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.EffectId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.EffectDescriptionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Transcendence implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.TRANSCENDENCE.getId();
    }

    @Override
    public void onFirstObtain(Player player) {
    }

    @Override
    public void onObtain(Player player) {
        StatManager.modifyFlyTimeAdder(player, 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyFlyTimeAdder(player, -1);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.TRANSCENDENCE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.fly"),
                Component.translatable("item.isaac_disaster.special.stackable")
        );
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(EffectDescriptionManager.getInstance().getDescriptionFromId(EffectId.TRANSCENDENCE.getId()));

        return description;
    }
}
