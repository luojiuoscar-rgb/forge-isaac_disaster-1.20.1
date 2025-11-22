package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheSoul implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_SOUL.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.SOUL_HEART.get(), 2);
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.addRecursiveModule(player, ModRecursiveModule.THE_SOUL.getId(), 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.addRecursiveModule(player, ModRecursiveModule.THE_SOUL.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_soul_heart", 2),
                Component.translatable("item.isaac_disaster.the_soul.lore.1")
        );
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.add(EffectManager.SOUL_STATE.getExplainDesc());

        return description;
    }
}
