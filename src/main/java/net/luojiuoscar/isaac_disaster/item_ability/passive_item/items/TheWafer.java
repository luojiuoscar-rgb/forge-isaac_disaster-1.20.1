package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTrigger;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IHurtTrigger;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheWafer implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_WAFER.getId();
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {

    }

    @Override
    public void onRemove(Player player) {

    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_WAFER.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_wafer.lore.1")
        );
    }
}
