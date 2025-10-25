package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TinyPlanet implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.TINY_PLANET.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
        StatManager.modifyRangeAdder(player, 2.5, isPermanent);
        StatManager.modifySpectral(player, 1, isPermanent);
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
        StatManager.modifyRangeAdder(player, -2.5, isPermanent);
        StatManager.modifySpectral(player, -1, isPermanent);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.TINY_PLANET.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_range", 2.5*StatManager.getRangeBonus()),
                Component.translatable("item.isaac_disaster.attribute.spectral_bullet"),
                Component.translatable("item.isaac_disaster.tiny_planet.lore.1")
                );
    }
}
