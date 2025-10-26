package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ExperimentalTreatment implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.EXPERIMENTAL_TREATMENT.getId();
    }

    @Override
    public void onFirstObtain(Player player) {
    }

    @Override
    public void onObtain(Player player) {
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), -1);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.EXPERIMENTAL_TREATMENT.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.experimental_treatment.lore.1"),
                Component.translatable("item.isaac_disaster.experimental_treatment.lore.2"),
                Component.translatable("item.isaac_disaster.action.cannot_unequip")
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getSynergyDescription();
    }

    @Override
    public List<Component> getExplain(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getExplain();
    }
}
